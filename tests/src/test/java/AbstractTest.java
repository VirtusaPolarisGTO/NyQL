import com.virtusa.gto.insight.nyql.QResultProxy;
import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.model.QScript;
import com.virtusa.gto.insight.nyql.model.QScriptResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

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
            QResultProxy proxy = deriveProxy(objects.get(i));
            String query = String.valueOf(objects.get(i + 1));

            String q1 = proxy.getQuery().replace("\n", "").trim();
            String q2 = query.replace("\n", "").trim();
            System.out.println("Generated Query:");
            System.out.println(q1);
            //System.out.println("Expected:");
            //System.out.println(q2);
            Assert.assertEquals(q2, q1);
        }
    }

    private QResultProxy deriveProxy(Object obj) {
        if (obj instanceof QResultProxy) {
            return (QResultProxy)obj;
        } else if (obj instanceof QScript) {
            return ((QScriptResult) obj).getProxy();
        } else {
            throw new RuntimeException("Unknown result type from test script!");
        }
    }

    @BeforeClass
    public static void setupTests() {
        System.setProperty("nyql.autoConfig", "false");
        NyQL.configure(new File("./nyql.json"));
    }

    @AfterClass
    public static void tearDownTests() {
        System.out.println("Shutting down test.");
        NyQL.shutdown();
    }
}
