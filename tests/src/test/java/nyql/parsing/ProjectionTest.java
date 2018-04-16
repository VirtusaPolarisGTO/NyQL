package nyql.parsing;

import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class ProjectionTest extends AbstractTest {

    public void testBasic() throws NyException {
        Map<String, Object> data = new HashMap<>();
        data.put("trueCondition", true);
        data.put("abc", "Hello");

        assertQueries(nyql().parse("projection/basic_projection", data));
    }

    public void testImports() throws NyException {
        assertQueries(nyql().parse("projection/import_projection"));
    }

    public void testFuncs() throws NyException {
        Map<String, Object> data = new HashMap<>();
        data.put("listOfInt", Arrays.asList(1, 2, 3));

        assertQueries(nyql().parse("projection/func_projection", data));
    }

    public void testFuncs2() throws NyException {
        assertQueries(nyql().parse("projection/func2_projection"));
    }

    public void testFuncsDates() throws NyException {
        assertQueries(nyql().parse("projection/funcdt_projection"));
    }

    public void testNyqlExtension() throws NyException {
        assertQueries(nyql().parse("projection/test"));
    }

}
