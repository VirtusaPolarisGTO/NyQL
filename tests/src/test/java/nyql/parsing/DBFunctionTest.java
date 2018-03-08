package nyql.parsing;

import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class DBFunctionTest extends AbstractTest {

    public void testFunctions() throws Exception {
        assertQueries(nyql().parse("dbfunctions/simple"));
    }

}
