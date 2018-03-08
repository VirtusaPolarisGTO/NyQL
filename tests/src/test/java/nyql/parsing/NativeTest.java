package nyql.parsing;

import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author Isuru Weerarathna
 */
@Test(groups = {"parsing"})
public class NativeTest extends AbstractTest {

    public void testNativeStr() throws NyException {
        // added string native queries
        assertQueries(nyql().parse("natives/strnative"));
    }

    public void testNativeMap() throws NyException {
        assertQueries(nyql().parse("natives/mapnative"));
    }
}
