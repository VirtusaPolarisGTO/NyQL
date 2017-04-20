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
public class RelativeScripts {

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

    public void testRelativeScriptPaths() throws NyException {
        nyQLInstance.execute("scripts/foo/nested");
    }

}
