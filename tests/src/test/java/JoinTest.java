import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class JoinTest extends AbstractTest {

    @Test
    public void testBasic() throws NyException {
        assertQueries(NyQL.parse("joins/simple"));
    }

    @Test
    public void testImports() throws NyException {
        assertQueries(NyQL.parse("joins/import_test"));
    }
}
