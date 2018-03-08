package nyql.parsing;

import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.annotations.Test;

/**
 * @author IWEERARATHNA
 */
@Test(groups = {"parsing"})
public class DeleteTest extends AbstractTest {

    public void testDelete() throws NyException {
        assertQueries(nyql().parse("delete/basic"));
    }

    public void testJoins() throws NyException {
        assertQueries(nyql().parse("delete/joins"));
    }

    public void testTruncate() throws NyException {
        assertQueries(nyql().parse("delete/truncate"));
    }
}
