package nyql.parsing;

import com.virtusa.gto.insight.nyql.QResultProxy;
import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.model.QScript;
import com.virtusa.gto.insight.nyql.model.QScriptList;
import com.virtusa.gto.insight.nyql.model.QScriptResult;
import com.virtusa.gto.insight.nyql.model.blocks.AParam;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.util.List;

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
            compare ((QResultProxy)obj, gen);
        } else if (obj instanceof QScriptList) {
            if (!(gen instanceof List)) {
                throw new AssertionError("Script List must have a list as input!");
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

    private void compare(QResultProxy proxy, Object query) {
        String q1 = proxy.getQuery().replace("\n", "").replace("\t", "").trim();

        String q2;
        List pList = null;
        if (query instanceof List) {
            q2 = ((List) query).get(0).toString().replace("\n", "").trim();
            pList = (List) ((List) query).get(1);
        } else {
            q2 = String.valueOf(query).replace("\n", "").trim();
        }
        System.out.println("Generated Query:");
        System.out.println(q1);
        Assert.assertEquals(q2, q1);

        List<AParam> paramList = proxy.getOrderedParameters();
        assertLists(paramList, pList);
    }

    private void assertLists(List first, List second) {
        if (first == null || first.isEmpty()) {
            if (second != null && !second.isEmpty()) {
                Assert.assertEquals(0, second.size(), "Original does not have parameters at all!");
                return;
            } else {
                return;
            }
        } else if (second == null || second.isEmpty()) {
            Assert.assertEquals(first.size(), 0, "Original has more parameters than expected!");
            return;
        }
        Assert.assertEquals(first.size(), second.size(), "Parameters size are different!");
        for (int i = 0; i < first.size(); i++) {
            AParam param = (AParam) first.get(i);
            if (!param.get__name().equals(second.get(i))) {
                Assert.assertEquals(param.get__name(), second.get(i), "Parameter #" + (i+1) + " are not equal!");
            }
        }
    }

    @BeforeClass
    public void doBefore() {
        System.out.println("***********************************************************************************************");
        System.out.println("  Running Tests @ " + this.getClass().getSimpleName());
        System.out.println("***********************************************************************************************");
    }

    @AfterClass
    public void doAfter() {
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("  Test Run Completed @ " + this.getClass().getSimpleName());
        System.out.println("-----------------------------------------------------------------------------------------------");
    }

    @BeforeSuite
    public static void setupTests() {
        //if (NyQL.hasConfigured()) {
            //System.setProperty("com.virtusa.gto.insight.nyql.autoBootstrap", "true");
            NyQL.configure(new File("./nyql.json"));
        //}
    }

    @AfterSuite
    public static void tearDownTests() {
        System.out.println("Shutting down test.");
        NyQL.shutdown();
    }
}
