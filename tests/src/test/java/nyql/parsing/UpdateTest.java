package nyql.parsing;

import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class UpdateTest extends AbstractTest {

    public void testBasic() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("alwaysTrue", true);
        //data.put("singleList", Arrays.asList(1));
        //data.put("doubleList", Arrays.asList(1, 2));

        assertQueries(NyQL.parse("updates/basic", data));
    }

    public void testSpecials() throws Exception {
        assertQueries(NyQL.parse("updates/specials"));
    }

}
