package nyql.parsing;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class DBFunctionTest extends AbstractTest {

    public void testFunctions() throws Exception {
        assertQueries(NyQL.parse("dbfunctions/simple"));
    }

}
