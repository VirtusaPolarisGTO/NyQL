package nyql.parsing;

import com.virtusa.gto.nyql.engine.NyQL;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
@Test(groups = {"parsing"})
public class UpsertsTest extends AbstractTest {

    public void testBasic() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Yeah");
        //data.put("singleList", Arrays.asList(1));
        //data.put("doubleList", Arrays.asList(1, 2));

        assertQueries(NyQL.parse("upserts/upserts", data));
    }

    public void testInsertOrLoad() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Yeah");

        assertQueries(NyQL.parse("upserts/insertOr", data));
    }


}
