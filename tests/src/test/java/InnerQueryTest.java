import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class InnerQueryTest extends AbstractTest {

    @Test
    public void testTargetTable() throws NyException {
        assertQueries(NyQL.parse("innerquery/target_iq"));
    }

}
