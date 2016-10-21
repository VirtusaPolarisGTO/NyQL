package nyql.tests;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.io.File;
import java.util.List;

/**
 * @author IWEERARATHNA
 */
public class GVisitor {

    public static void main(String[] args) throws Exception {
        File file = new File("./examples/trans.groovy");
        String content = FileUtils.readFileToString(file);
        GroovyCodeVisitor visitor = new MyVisitor();
        List<ASTNode> astNodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, false, content);
        astNodes.get(0).visit(visitor);
    }

    private static class MyVisitor extends CodeVisitorSupport {

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            super.visitMethodCallExpression(call);
            //System.out.println(call.getMethodAsString());
            //System.out.println(call.getObjectExpression());
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            super.visitVariableExpression(expression);
            if (expression.getLineNumber() > 0) {
                System.out.println("Variable: " + expression.getName() + " @ line " + expression.getLineNumber());
            }
            //System.out.println(expression.);
        }
    }

}
