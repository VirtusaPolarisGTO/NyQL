import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class WhereTest extends AbstractTest {

    @Test
    public void testBasic() throws NyException {
        Map<String, Object> data = new HashMap<>();
        data.put("emptyList", new ArrayList<>());
        data.put("singleList", Arrays.asList(1));
        data.put("doubleList", Arrays.asList(1, 2));

        assertQueries(NyQL.parse("where/basic_where", data));
    }

}
