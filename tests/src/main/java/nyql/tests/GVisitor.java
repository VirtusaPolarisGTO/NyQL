package nyql.tests;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;

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
            System.out.println(call.getMethodAsString());
            System.out.println(call.getArguments());
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
