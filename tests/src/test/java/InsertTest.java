import com.virtusa.gto.insight.nyql.engine.NyQL;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class InsertTest extends AbstractTest {

    @Test
    public void testBasic() throws Exception {
        assertQueries(NyQL.parse("inserts/basic"));
    }

}
