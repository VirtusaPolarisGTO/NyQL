package nyql.parsing;

import com.virtusa.gto.nyql.engine.NyQL;
import org.testng.annotations.Test;

/**
 * @author iweerarathna
 */
@Test(groups = {"parsing"})
public class CTETest extends AbstractTest {
    public void testCteBasic() throws Exception {
        assertQueries(NyQL.parse("cte/withq"));
    }

    public void testCteRecursive() throws Exception {
        assertQueries(NyQL.parse("cte/withrq"));
    }
}
