package nyql.tests;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilePhase;

import java.io.File;
import java.util.*;

/**
 * @author IWEERARATHNA
 */
public class GVisitor {

    public static void main(String[] args) throws Exception {
        File file = new File("C:\\Projects\\work\\scripts\\database\\work\\scripts\\dashboard" +
                "\\violation_breakdown\\join\\partials\\common_join_release_violation_summary.groovy");
        System.out.println(scan(file));
    }

    static ScriptInfo scan(File file) throws Exception {
        String content = FileUtils.readFileToString(file);
        MyVisitor visitor = new MyVisitor();
        List<ASTNode> astNodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, false, content);
        for (ASTNode node : astNodes) {
            if (!(node instanceof ClassNode)) {
                node.visit(visitor);
            }
        }
        //System.out.println(visitor.tables);
        ScriptInfo scriptInfo = new ScriptInfo();
        scriptInfo.setCalls(new HashSet<>(visitor.callList));
        scriptInfo.setQueryType(visitor.queryType);
        scriptInfo.setTables(visitor.getTables());
        return scriptInfo;
    }

    private static class MyVisitor extends CodeVisitorSupport {

        private final Set<String> tables = new LinkedHashSet<>();
        private String queryType;
        private final List<String> callList = new LinkedList<>();
        private boolean insideMCall = false;

        public List<String> getCallList() {
            return callList;
        }

        public Set<String> getTables() {
            return tables;
        }

        public String getQueryType() {
            return queryType;
        }

        @Override
        public void visitPropertyExpression(PropertyExpression expression) {
            super.visitPropertyExpression(expression);
            //System.out.println(expression.getObjectExpression() + "." + expression.getPropertyAsString());
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (call.getMethodAsString().equals("$IMPORT") || call.getMethodAsString().equals("RUN")) {
                call.getObjectExpression().visit(this);
                call.getMethod().visit(this);
                insideMCall = true;
                call.getArguments().visit(this);
                insideMCall = false;
                return;

            } else if (call.getMethodAsString().equals("TARGET")
                    || call.getMethodAsString().equals("EXPECT")
                    || call.getMethodAsString().endsWith("JOIN")) {
                filterTargetClause(call);
            } else if (call.getMethodAsString().equals("TABLE")) {
                filterTableCall(call);
            } else {
                if (isDSL(call.getObjectExpression())) {
                    queryType = call.getMethodAsString();
                }
            }
            super.visitMethodCallExpression(call);
            insideMCall = false;
        }

        private void filterTableCall(MethodCallExpression expression) {
            if (expression.getArguments() instanceof ArgumentListExpression) {
                ArgumentListExpression argList = (ArgumentListExpression)expression.getArguments();
                argList.forEach(ex -> {
                    if (ex instanceof ConstantExpression) {
                        tables.add(ex.getText());
                    }
                });
            }
        }

        private void filterTargetClause(MethodCallExpression expression) {
            if (expression.getArguments() instanceof ArgumentListExpression) {
                ArgumentListExpression argList = (ArgumentListExpression)expression.getArguments();
                argList.forEach(ex -> {
                    if (ex instanceof MethodCallExpression) {
                        MethodCallExpression call = (MethodCallExpression)ex;
                        if (!call.getMethodAsString().equals("TABLE")
                                && call.getObjectExpression() instanceof VariableExpression) {
                            tables.add(((VariableExpression) call.getObjectExpression()).getName());
                        }
                    }
                });
            }
        }

        private boolean isDSL(Expression expression) {
            return expression instanceof VariableExpression && ((VariableExpression) expression).getName().equals("$DSL");
        }

        @Override
        public void visitConstantExpression(ConstantExpression expression) {
            if (insideMCall) {
                callList.add(expression.getText());
            }
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            super.visitVariableExpression(expression);
            if (expression.getLineNumber() > 0) {
                //System.out.println("Variable: " + expression.getName() + " @ line " + expression.getLineNumber());
            }
            //System.out.println(expression.);
        }
    }

}
