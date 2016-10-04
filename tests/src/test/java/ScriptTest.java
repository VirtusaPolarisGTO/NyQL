import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.exceptions.NyException;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class ScriptTest {

    public void testVariables() throws NyException {
        NyQL.execute("scripts/variable_test");
    }

}
