import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class UpdateTest extends AbstractTest {

    @Test
    public void testBasic() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("alwaysTrue", true);
        //data.put("singleList", Arrays.asList(1));
        //data.put("doubleList", Arrays.asList(1, 2));

        assertQueries(NyQL.parse("updates/basic", data));
        assertQueries(NyQL.parse("updates/basic", data));
        assertQueries(NyQL.parse("updates/basic", data));
    }

    @Test
    public void testSpecials() throws Exception {
        assertQueries(NyQL.parse("updates/specials"));
    }

}
