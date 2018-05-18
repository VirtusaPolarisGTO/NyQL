package com.virtusa.gto.nyql.engine.repo;

import com.virtusa.gto.nyql.configs.Configurations;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.exceptions.NyInitializationException;
import com.virtusa.gto.nyql.model.QSource;
import com.virtusa.gto.nyql.model.units.ParamList;
import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author iweerarathna
 */
class ScriptCacheValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptCacheValidator.class);

    private final Configurations configurations;

    ScriptCacheValidator(Configurations configurations) {
        this.configurations = configurations;
    }

    public void check(Collection<QSource> sources) throws NyException {
        if (sources == null || sources.isEmpty()) {
            return;
        }

        LOGGER.info("Script cache validation started. (this may take several minutes)...");
        int errorCount = 0;
        Map<String, Boolean> cacheStatus = new HashMap<>();
        Map<String, Set<String>> scriptCalls = new HashMap<>();
        for (QSource source : sources) {
            ParseVisitor visitor = new ParseVisitor();
            GroovyCodeSource codeSource = source.getCodeSource();

            if (hasCode(source)) {
                List<ASTNode> astNodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION,
                        false,
                        codeSource.getScriptText());
                if (astNodes != null) {
                    for (ASTNode node : astNodes) {
                        if (!(node instanceof ClassNode)) {
                            node.visit(visitor);
                        }
                    }
                }

                cacheStatus.put(source.getId(), visitor.cached);
                scriptCalls.put(source.getId(), new HashSet<>(visitor.scriptCalls));
                Level level = checkVisitor(visitor, source);
                if (level == Level.ERROR) errorCount++;
            } else {
                LOGGER.error("  > Script '{}' is empty!", source.getId());
                errorCount++;
            }
        }

        // analyze script dependency graph to check cache errors...
        errorCount += checkCacheHierarchy(cacheStatus, scriptCalls);

        LOGGER.info("Script cache validation finished.");
        if (errorCount > 0) {
            throw new NyInitializationException("There are " + errorCount + " caching error(s) in scripts! " +
                    "Check the logs and fix to continue.");
        }
    }

    private boolean hasCode(QSource source) {
        return source.getCodeSource().getScriptText() != null
                && source.getCodeSource().getScriptText().trim().length() > 0;
    }

    private int checkCacheHierarchy(Map<String, Boolean> cacheStatus,
                                     Map<String, Set<String>> scriptCalls) {
        Map<String, Boolean> memo = new HashMap<>();
        int errors = 0;
        for (Map.Entry<String, Set<String>> entry : scriptCalls.entrySet()) {
            Set<String> calls = entry.getValue();
            if (cacheStatus.get(entry.getKey())) {  // set cached = true
                for (String c : calls) {
                    Set<String> traversed = new HashSet<>();
                    if (!calcCache(c, cacheStatus, scriptCalls, memo, traversed)) {
                        LOGGER.error("  > Script '{}' is cached, but one of its " +
                                "dependent script is non-cached!", entry.getKey());
                        printNonCachedScripts(traversed, cacheStatus);
                        errors++;
                        break;
                    }
                }

            }
        }
        return errors;
    }

    private void printNonCachedScripts(Set<String> scripts, Map<String, Boolean> cachedStatus) {
        for (String scr : scripts) {
            if (!cachedStatus.get(scr)) {
                LOGGER.error("     - {} [NOT-CACHED]", scr);
            }
        }
    }

    private boolean calcCache(String scriptId, Map<String, Boolean> cacheStatus,
                           Map<String, Set<String>> scriptCalls,
                           Map<String, Boolean> memo, Set<String> traversed) {
        if (traversed.contains(scriptId)) {
            return cacheStatus.get(scriptId);
        }

        traversed.add(scriptId);

        if (memo.containsKey(scriptId)) {
            return memo.get(scriptId);
        }

        Set<String> calls = scriptCalls.get(scriptId);
        if (calls.isEmpty()) {
            return cacheStatus.getOrDefault(scriptId, true);
        } else {
            for (String call : calls) {
                if (!calcCache(call, cacheStatus, scriptCalls, memo, traversed)) {
                    return false;
                }
            }
            return true;
        }
    }

    private Level checkVisitor(ParseVisitor visitor, QSource source) throws NyException {
        if (visitor.cached && !isCacheable(visitor)) {
            LOGGER.error("  > Script '{}' is non-cacheable, but found as cached!", source.getId());
            return Level.ERROR;
        } else if (!visitor.cached && isCacheable(visitor)) {
            LOGGER.warn("  > Script '{}' can be cacheable. Verify it and make it cacheable, if possible.", source.getId());
            return Level.WARN;
        }
        return Level.OK;
    }

    private boolean isCacheable(ParseVisitor visitor) {
        if (visitor.sessionUsed) {
            return false;
        }
        return !visitor.dslCalls.contains("script")
                && !visitor.dslCalls.contains("RUN");
    }

    private enum Level {
        ERROR, WARN, OK
    }

    private static class ParseVisitor extends CodeVisitorSupport {

        private static final String EMPTY = "";
        private boolean sessionUsed = false;
        private Set<String> sessionVars = new HashSet<>();
        private List<String> dslCalls = new ArrayList<>();
        private Set<String> scriptCalls = new HashSet<>();
        private List<Map<String, String>> params = new ArrayList<>();
        private boolean insideScriptCall = false;
        private boolean cached = false;

        @Override
        public void visitDeclarationExpression(DeclarationExpression expression) {
            super.visitDeclarationExpression(expression);
            if (!cached) {
                cached = expression.getVariableExpression().getName().equals("do_cache")
                        && expression.getAnnotations().size() > 0;
            }
        }

        @Override
        public void visitConstantExpression(ConstantExpression expression) {
            if (insideScriptCall) {
                scriptCalls.add(expression.getText());
            }
            super.visitConstantExpression(expression);
        }

        @Override
        public void visitPropertyExpression(PropertyExpression expression) {
            if (isSessionVar(expression.getObjectExpression())) {
                sessionVars.add(expression.getPropertyAsString());
            } else {
                String parents = derivePropertyChain(expression.getObjectExpression());
                if (parents.startsWith("$SESSION.")) {
                    String suffix = parents.substring("$SESSION.".length());
                    sessionVars.add(suffix + "." + expression.getPropertyAsString());
                }
            }
            super.visitPropertyExpression(expression);
        }

        private String derivePropertyChain(Expression expression) {
            if (expression instanceof PropertyExpression) {
                return derivePropertyChain(((PropertyExpression) expression).getObjectExpression()) + "." +
                        ((PropertyExpression) expression).getPropertyAsString();
            } else if (isSessionVar(expression)) {
                return expression.getText();
            }
            return EMPTY;
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            if (!sessionUsed && isSessionVar(expression)) {
                sessionUsed = true;
            }
            super.visitVariableExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (isDsl(call.getObjectExpression())) {
                dslCalls.add(call.getMethodAsString());
            }

            if (call.getMethodAsString().equals("$IMPORT")
                    || call.getMethodAsString().equals("RUN")
                    || call.getMethodAsString().equals("$IMPORT_SAFE")) {
                call.getObjectExpression().visit(this);
                call.getMethod().visit(this);
                insideScriptCall = isFirstArgConstant(call.getArguments());
                call.getArguments().visit(this);
                insideScriptCall = false;
            } else if (isParam(call.getMethodAsString())) {
                Map<String, String> p = new HashMap<>();
                p.put("name", getFirstArgConstant(call.getArguments()));
                if (call.getMethodAsString().equals("PARAMLIST") || call.getMethodAsString().equals("PARAM_LIST")) {
                    p.put("type", ParamList.class.getSimpleName());
                } else {
                    p.put("type", "AParam");
                }
                params.add(p);
            } else {
                super.visitMethodCallExpression(call);
            }
        }

        private boolean isParam(String method) {
            return method.equals("PARAM")
                    || method.equals("PARAMLIST")
                    || method.equals("PARAM_LIST")
                    || method.equals("PARAM_DATE")
                    || method.equals("PARAM_TIMESTAMP")
                    || method.equals("PARAM_BINARY");
        }

        private boolean isDsl(Expression expression) {
            if (expression instanceof VariableExpression) {
                return ((VariableExpression) expression).getName().equals("$DSL");
            }
            return false;
        }

        private boolean isFirstArgConstant(Expression expression) {
            if (expression instanceof ArgumentListExpression) {
                ArgumentListExpression ale = (ArgumentListExpression)expression;
                if (ale.getExpressions().size() > 0) {
                    return ale.getExpression(0) instanceof ConstantExpression;
                }
            }
            return false;
        }

        private String getFirstArgConstant(Expression expression) {
            if (expression instanceof ArgumentListExpression) {
                ArgumentListExpression ale = (ArgumentListExpression)expression;
                if (ale.getExpressions().size() > 0 && ale.getExpression(0) instanceof ConstantExpression) {
                    return ((ConstantExpression) ale.getExpression(0)).getText();
                }
            }
            return null;
        }

        private boolean isSessionVar(Expression expression) {
            if (expression instanceof VariableExpression) {
                return ((VariableExpression) expression).getName().equals("$SESSION");
            }
            return false;
        }
    }

}
