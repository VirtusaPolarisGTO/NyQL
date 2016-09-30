import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class LimitTest extends AbstractTest {

    @Test
    public void testLimit() throws NyException {
        assertQueries(NyQL.parse("limit/basic"));
    }

}
