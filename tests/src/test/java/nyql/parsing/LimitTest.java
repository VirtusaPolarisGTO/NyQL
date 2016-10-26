package nyql.parsing;

import com.virtusa.gto.nyql.engine.NyQL;
import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class LimitTest extends AbstractTest {

    public void testLimit() throws NyException {
        assertQueries(NyQL.parse("limit/basic"));
    }

}
