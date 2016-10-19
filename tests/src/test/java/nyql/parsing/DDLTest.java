package nyql.parsing;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class DDLTest extends AbstractTest {

    public void testTempTable() throws NyException {
        assertQueries(NyQL.parse("ddl/temp_tables"));
    }

    public void testNormalTable() throws NyException {
        assertQueries(NyQL.parse("ddl/normal_tables"));
    }

}
