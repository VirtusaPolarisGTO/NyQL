import com.virtusa.gto.insight.nyql.AbstractClause;
import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class ProjectionTest extends AbstractTest {

    @Test
    public void testBasic() throws NyException {
        Map<String, Object> data = new HashMap<>();
        data.put("trueCondition", true);
        data.put("abc", "Hello");

        assertQueries(NyQL.parse("projection/basic_projection", data));
        assertQueries(NyQL.parse("projection/basic_projection", data));
        assertQueries(NyQL.parse("projection/basic_projection", data));
        assertQueries(NyQL.parse("projection/basic_projection", data));
    }

    @Test
    public void testImports() throws NyException {
        assertQueries(NyQL.parse("projection/import_projection"));
        assertQueries(NyQL.parse("projection/import_projection"));
        assertQueries(NyQL.parse("projection/import_projection"));
        assertQueries(NyQL.parse("projection/import_projection"));
        assertQueries(NyQL.parse("projection/import_projection"));
    }

    @Test
    public void testFuncs() throws NyException {
        Map<String, Object> data = new HashMap<>();
        data.put("listOfInt", Arrays.asList(1, 2, 3));

        assertQueries(NyQL.parse("projection/func_projection", data));
        assertQueries(NyQL.parse("projection/func_projection", data));
        assertQueries(NyQL.parse("projection/func_projection", data));
        assertQueries(NyQL.parse("projection/func_projection", data));
        assertQueries(NyQL.parse("projection/func_projection", data));
    }


}
