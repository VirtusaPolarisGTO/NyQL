package nyql.parsing;

import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class InnerQueryTest extends AbstractTest {

    public void testTargetTable() throws NyException {
        assertQueries(nyql().parse("innerquery/target_iq"));
    }

    public void testProjection() throws NyException {
        assertQueries(nyql().parse("innerquery/projection_innerq"));
    }

    public void testInnerQuery() throws NyException {
        assertQueries(nyql().parse("innerquery/inner_query"));
    }

}
