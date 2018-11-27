package nyql.parsing;

import com.virtusa.gto.nyql.configs.Configurations;
import com.virtusa.gto.nyql.configs.NyConfigV2;
import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

@Test(groups = {"parsing"})
public class MultiSchemaTest {

    private NyQLInstance nyQLInstance;

    @BeforeTest
    public void beginTest() throws Exception {
        nyQLInstance = NyQLInstance.create("test", new File("./configs/nyql2-multischema.json"));
    }

    public void testMultiSchema() throws NyException {
        AbstractTest abstractTest = new AbstractTest();
        abstractTest.assertQueries(nyQLInstance.parse("schema"), "mysql");
    }

    @AfterTest
    public void tearDownTest() throws Exception {
        if (nyQLInstance != null) {
            nyQLInstance.shutdown();
        }
    }
}
