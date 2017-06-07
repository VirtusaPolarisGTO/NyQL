package nyql.scripting;

import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author iweerarathna
 */
@Test(groups = "scripts")
public class Upsertings {

    private NyQLInstance nyQLInstance;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void startup() {
        nyQLInstance = SUtils.createNyQL(2, 2);
    }

    @AfterClass
    public void teardown() {
        nyQLInstance.shutdown();
    }

    public void testUpserts() throws NyException {
        nyQLInstance.execute("scripts/upserting");
    }

    public void testInsertsOr() throws NyException {
        nyQLInstance.execute("scripts/insertingor");
    }
}
