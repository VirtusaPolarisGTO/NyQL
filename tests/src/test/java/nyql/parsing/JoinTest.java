package nyql.parsing;

import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class JoinTest extends AbstractTest {

    public void testBasic() throws NyException {
        assertQueries(nyql().parse("joins/simple"));
    }

    public void testImports() throws NyException {
        assertQueries(nyql().parse("joins/import_test"));
    }
}
