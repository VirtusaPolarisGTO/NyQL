package nyql.parsing;

import com.virtusa.gto.nyql.engine.NyQL;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class SelectsTest extends AbstractTest {

    public void testBasic() throws Exception {
        assertQueries(NyQL.parse("selects/basic"));
    }

}
