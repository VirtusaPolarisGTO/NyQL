package nyql.parsing;

import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class BulksTest extends AbstractTest {

    public void testBulkInsert() throws Exception {
        assertQueries(nyql().parse("bulks/bulkInserts"));
    }

    public void testBulkUpdate() throws Exception {
        assertQueries(nyql().parse("bulks/bulkUpdates"));
    }

    public void testBulkDelete() throws Exception {
        assertQueries(nyql().parse("bulks/bulkDeletes"));
    }
}
