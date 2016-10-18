import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class CombineTest extends AbstractTest {

    @Test
    public void testUnion() throws Exception {
        assertQueries(NyQL.parse("combines/basic_union"));
    }

}
