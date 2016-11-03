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
public class ScriptDDL {

    private NyQLInstance nyQLInstance;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void startup() {
        nyQLInstance = SUtils.createNyQL(2, 1);
    }

    @AfterClass
    public void teardown() {
        nyQLInstance.shutdown();
    }

    public void testScriptDDL() throws NyException {
        nyQLInstance.execute("scripts/schema_script");
    }

    public void testASelect() throws NyException {
        nyQLInstance.executeToJSON("scripts/aselect");
    }


}
