package nyql.tests;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author IWEERARATHNA
 */
public class GVisitor {

    public static void main(String[] args) throws Exception {
        File file = new File("./examples/select.groovy");
        findCalls(file);
    }

    static List<String> findCalls(File file) throws Exception {
        String content = FileUtils.readFileToString(file);
        MyVisitor visitor = new MyVisitor();
        List<ASTNode> astNodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, false, content);
        for (ASTNode node : astNodes) {
            if (!(node instanceof ClassNode)) {
                node.visit(visitor);
            }
        }
        return visitor.callList;
    }

    private static class MyVisitor extends CodeVisitorSupport {

        private final List<String> callList = new LinkedList<>();
        private boolean insideMCall = false;

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (call.getMethodAsString().equals("$IMPORT")
                    || call.getMethodAsString().equals("RUN")) {
                call.getObjectExpression().visit(this);
                call.getMethod().visit(this);
                insideMCall = true;
                call.getArguments().visit(this);

            } else {
                super.visitMethodCallExpression(call);
            }
            insideMCall = false;
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
