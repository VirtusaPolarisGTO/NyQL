package nyql.parsing;

import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class DDLTest extends AbstractTest {

    public void testTempTable() throws NyException {
        assertQueries(nyql().parse("ddl/temp_tables"));
    }

    public void testNormalTable() throws NyException {
        assertQueries(nyql().parse("ddl/normal_tables"));
    }

}
