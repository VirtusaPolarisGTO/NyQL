import com.virtusa.gto.insight.nyql.AbstractClause;
import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class ProjectionTest extends AbstractTest {

    @Test
    public void testBasic() throws NyException {
        Object result = NyQL.parse("projection/basic_projection");
        assertQueries(result);
    }

    @Test
    public void testImports() throws NyException {
        assertQueries(NyQL.parse("projection/import_projection"));
    }

    @Test
    public void testFuncs() throws NyException {
        assertQueries(NyQL.parse("projection/func_projection"));
    }


}
