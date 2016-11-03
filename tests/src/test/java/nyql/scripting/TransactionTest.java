package nyql.scripting;

import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = "scripts")
public class TransactionTest {

    private NyQLInstance nyQLInstance;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void startup() {
        nyQLInstance = SUtils.createNyQL(1, 1);
    }

    @AfterClass
    public void teardown() {
        nyQLInstance.shutdown();
    }

    public void testTransactions() throws NyException {
        nyQLInstance.execute("scripts/transactions");
    }
}
