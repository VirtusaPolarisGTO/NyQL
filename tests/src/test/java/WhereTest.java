import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class WhereTest extends AbstractTest {

    @Test
    public void testBasic() throws NyException {
        assertQueries(NyQL.parse("where/basic_where"));
    }

}
