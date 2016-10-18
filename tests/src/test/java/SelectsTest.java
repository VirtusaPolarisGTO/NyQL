import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class SelectsTest extends AbstractTest {

    @Test
    public void testBasic() throws Exception {
        assertQueries(NyQL.parse("selects/basic"));
    }

}
