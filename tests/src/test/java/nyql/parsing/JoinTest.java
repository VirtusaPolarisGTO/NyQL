package nyql.parsing;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class JoinTest extends AbstractTest {

    public void testBasic() throws NyException {
        assertQueries(NyQL.parse("joins/simple"));
    }

    public void testImports() throws NyException {
        assertQueries(NyQL.parse("joins/import_test"));
    }
}
