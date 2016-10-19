package nyql.parsing;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class InnerQueryTest extends AbstractTest {

    public void testTargetTable() throws NyException {
        assertQueries(NyQL.parse("innerquery/target_iq"));
    }

    public void testProjection() throws NyException {
        assertQueries(NyQL.parse("innerquery/projection_innerq"));
    }

}
