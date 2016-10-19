package nyql.scripting;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = "scripts")
public class ScriptTest {

    public void testVariables() throws NyException {
        NyQL.execute("scripts/variable_test");
    }

    public void testTransactions() throws NyException {
        NyQL.execute("scripts/transactions");
    }

}
