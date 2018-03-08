package nyql.parsing;

import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class CombineTest extends AbstractTest {

    public void testUnion() throws Exception {
        assertQueries(nyql().parse("combines/basic_union"));
    }

}
