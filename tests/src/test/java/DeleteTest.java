import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class DeleteTest extends AbstractTest {

    @Test
    public void testDelete() throws NyException {
        assertQueries(NyQL.parse("delete/basic"));
    }

    @Test
    public void testJoins() throws NyException {
        assertQueries(NyQL.parse("delete/joins"));
    }
}
