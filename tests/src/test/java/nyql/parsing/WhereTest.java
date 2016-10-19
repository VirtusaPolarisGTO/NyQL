package nyql.parsing;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class WhereTest extends AbstractTest {

    public void testBasic() throws NyException {
        Map<String, Object> data = new HashMap<>();
        data.put("emptyList", new ArrayList<>());
        data.put("singleList", Collections.singletonList(1));
        data.put("doubleList", Arrays.asList(1, 2));

        assertQueries(NyQL.parse("where/basic_where", data));
    }

    public void testImport() throws NyException {
//        Map<String, Object> data = new HashMap<>();
//        data.put("emptyList", new ArrayList<>());
//        data.put("singleList", Arrays.asList(1));
//        data.put("doubleList", Arrays.asList(1, 2));

        assertQueries(NyQL.parse("where/where_import"));
    }
}
