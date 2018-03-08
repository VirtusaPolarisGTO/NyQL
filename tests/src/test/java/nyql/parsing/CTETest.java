package nyql.parsing;

import org.testng.annotations.Test;

/**
 * @author iweerarathna
 */
@Test(groups = {"parsing"})
public class CTETest extends AbstractTest {
    public void testCteBasic() throws Exception {
        assertQueries(nyql().parse("cte/withq"));
    }

    public void testCteRecursive() throws Exception {
        assertQueries(nyql().parse("cte/withrq"));
    }
}
