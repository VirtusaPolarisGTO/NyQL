package nyql.parsing;

import com.virtusa.gto.nyql.engine.NyQL;
import com.virtusa.gto.nyql.engine.impl.NyQLResult;
import com.virtusa.gto.nyql.exceptions.NyException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Test(groups = {"parsing"})
public class ResultTest extends AbstractTest {

    public void testResults() throws NyException {
        NyQLResult result = NyQL.execute("scripts/aselect");
        result.mutateToInt("id");
        result.mutateToBool("aboolCol");
        result.mutateToDouble("price");
        result.mutateToInt("year");

        Assert.assertEquals(result.getField(0, "id"), 1);
        Assert.assertEquals(result.asString(0, "title"), "item-1");
        Assert.assertEquals(result.asBool(1, "aboolCol"), Boolean.FALSE);
        Assert.assertEquals(result.getField(1, "aboolCol"), false);
        Assert.assertEquals(result.getField(2, "aboolCol"), null);
        Assert.assertEquals(result.getField(3, "aboolCol"), false);
        Assert.assertEquals(result.getField(4, "aboolCol"), true);
        Assert.assertEquals(result.getField(5, "aboolCol"), true);

        Assert.assertEquals(result.getField(2, "aboolCol", true), null);
        Assert.assertEquals(result.getField(2, "aboolColNonExist", true), true);

        Assert.assertEquals(result.getField(0, "price"), 2.34);
        Assert.assertEquals(result.getField(4, "price"), null);
        Assert.assertEquals(result.getField(3, "price"), 0.0);
        Assert.assertEquals(result.getField(2, "price"), 34.5);

        try {
            result.asBool(0, "aaa");
            Assert.fail("Getting boolean value from non existing column should fail!");
        } catch (NyException ex) {
            // exception expected
        }

        try {
            result.affectedCount();
            Assert.fail("Affected count shoud fail, but did not!");
        } catch (NyException ex) {
            // exception expected
        }

        try {
            result.affectedKeys();
            Assert.fail("Affected keys shoud fail, but did not!");
        } catch (NyException ex) {
            // exception expected
        }
    }

    public void testInsertResults() throws NyException {
        NyQLResult result = NyQL.execute("scripts/ainsert");
        Assert.assertEquals(result.affectedCount(), 1);
        try {
            result.affectedKeys();
            Assert.fail("Affected keys shoud fail, but did not!");
        } catch (NyException ex) {
            // exception expected
        }

        try {
            result.affectedCounts();
            Assert.fail("Affected counts shoud fail, but did not!");
        } catch (NyException ex) {
            // exception expected
        }

        NyQLResult result2 = NyQL.execute("scripts/ainsert_keys");
        Assert.assertEquals(result2.affectedCount(), 2);
        List<Integer> items = new ArrayList<>();
        items.add(10);
        items.add(11);
        Assert.assertEquals(result2.affectedKeys(), items);
    }

    public void testBulkInserts() throws NyException {
        NyQLResult result = NyQL.execute("scripts/abulkinsert");
        List<Integer> items = new ArrayList<>();
        items.add(1);
        items.add(0);
        items.add(1);
        Assert.assertEquals(result.affectedCounts(), items);

        try {
            result.affectedCount();
            Assert.fail("Affected count shoud fail, but did not!");
        } catch (NyException ex) {
            // exception expected.
        }
    }
}
