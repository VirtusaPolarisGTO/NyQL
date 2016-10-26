package nyql.parsing;

import com.virtusa.gto.nyql.engine.NyQL;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class CombineTest extends AbstractTest {

    public void testUnion() throws Exception {
        assertQueries(NyQL.parse("combines/basic_union"));
    }

}
