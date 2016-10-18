import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class DBFunctionTest extends AbstractTest {

    @Test
    public void testFunctions() throws Exception {
        assertQueries(NyQL.parse("dbfunctions/simple"));
    }

}
