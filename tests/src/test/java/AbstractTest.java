import com.virtusa.gto.insight.nyql.QResultProxy;
import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.model.QScript;
import com.virtusa.gto.insight.nyql.model.QScriptList;
import com.virtusa.gto.insight.nyql.model.QScriptResult;
import junit.framework.AssertionFailedError;
import org.junit.*;

import java.io.File;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author IWEERARATHNA
 */
public class AbstractTest {

    public void assertQueries(Object objects) {
        Object val = objects;
        if (objects instanceof QScriptResult) {
            val = ((QScriptResult) objects).getScriptResult();
        }

        if (val instanceof List) {
            assertScript((List<Object>)val);
        } else {
            throw new RuntimeException("Expecting a list!");
        }
    }

    public void assertScript(List<Object> objects) {
        for (int i = 0; i < objects.size(); i += 2) {
            deriveProxy(objects.get(i), objects.get(i + 1));
        }
    }

    private void deriveProxy(Object obj, Object gen) {
        if (obj instanceof QResultProxy) {
            compare ((QResultProxy)obj, String.valueOf(gen));
        } else if (obj instanceof QScriptList) {
            if (!(gen instanceof List)) {
                throw new AssertionFailedError("Script List must have a list as input!");
            }

            List res = (List)gen;
            for (int i = 0; i < ((QScriptList) obj).getScripts().size(); i++) {
                QScript qScript = ((QScriptList) obj).getScripts().get(i);
                compare(qScript.getProxy(), String.valueOf(res.get(i)));
            }

        } else if (obj instanceof QScript) {
            compare (((QScript) obj).getProxy(), String.valueOf(gen));
        } else {
            throw new RuntimeException("Unknown result type from test script!");
        }
    }

    private void compare(QResultProxy proxy, String query) {
        String q1 = proxy.getQuery().replace("\n", "").replace("\t", "").trim();
        String q2 = query.replace("\n", "").trim();
        System.out.println("Generated Query:");
        System.out.println(q1);
        //System.out.println("Expected:");
        //System.out.println(q2);
        Assert.assertEquals(q2, q1);
    }

    @Before
    public void doBefore() {
        System.out.println("***********************************************************************************************");
        System.out.println("  Running Tests @ " + this.getClass().getSimpleName());
        System.out.println("***********************************************************************************************");
    }

    @After
    public void doAfter() {
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("  Test Run Completed @ " + this.getClass().getSimpleName());
        System.out.println("-----------------------------------------------------------------------------------------------");
    }

    @BeforeClass
    public static void setupTests() {
        //if (NyQL.hasConfigured()) {
            System.setProperty("com.virtusa.gto.insight.nyql.autoBootstrap", "true");
            //NyQL.configure(new File("./nyql.json"), true);
        //}
    }

    @AfterClass
    public static void tearDownTests() {
        //System.out.println("Shutting down test.");
        //NyQL.shutdown();
    }
}
