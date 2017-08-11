package nyql.parsing;

import com.virtusa.gto.nyql.engine.NyQL;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class BulksTest extends AbstractTest {

    public void testBulkInsert() throws Exception {
        assertQueries(NyQL.parse("bulks/bulkInserts"));
    }

    public void testBulkUpdate() throws Exception {
        assertQueries(NyQL.parse("bulks/bulkUpdates"));
    }

    public void testBulkDelete() throws Exception {
        assertQueries(NyQL.parse("bulks/bulkDeletes"));
    }
}
