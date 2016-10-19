package nyql.parsing;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class InsertTest extends AbstractTest {

    public void testBasic() throws Exception {
        assertQueries(NyQL.parse("inserts/basic"));
    }

    public void testSelectInserts() throws Exception {
        assertQueries(NyQL.parse("inserts/select_inserts"));
    }
}
