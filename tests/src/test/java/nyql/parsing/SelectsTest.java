package nyql.parsing;

import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class SelectsTest extends AbstractTest {

    public void testBasic() throws Exception {
        assertQueries(nyql().parse("selects/basic"));
    }

    public void testValueTables() throws Exception {
        assertQueries(nyql().parse("selects/valuetables"));
    }

}
