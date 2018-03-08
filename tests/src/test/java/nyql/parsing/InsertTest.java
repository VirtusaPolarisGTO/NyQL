package nyql.parsing;

import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class InsertTest extends AbstractTest {

    public void testBasic() throws Exception {
        assertQueries(nyql().parse("inserts/basic"));
    }

    public void testSelectInserts() throws Exception {
        assertQueries(nyql().parse("inserts/select_inserts"));
    }
}
