import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class DDLTest extends AbstractTest {

    @Test
    public void testTempTable() throws NyException {
        assertQueries(NyQL.parse("ddl/temp_tables"));
    }

    @Test
    public void testNormalTable() throws NyException {
        assertQueries(NyQL.parse("ddl/normal_tables"));
    }

}
