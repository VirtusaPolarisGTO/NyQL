import com.virtusa.gto.insight.nyql.engine.NyQL;
import com.virtusa.gto.insight.nyql.engine.Quickly;
import com.virtusa.gto.insight.nyql.model.QScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @author IWEERARATHNA
 */
public class DBExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBExecutor.class);

    public static void main(String[] args) throws Exception {
        Map<String, Object> data = new HashMap<>();
        List<Integer> teams = asList(1410, 1411);
        List<Integer> modules = asList(97389, 97390, 97391);

        data.put("teamIDs", teams);
        data.put("moduleIDs", modules);
        data.put("filmId", 250);

        Object result = NyQL.execute("insight/codebranch", data);
        System.out.println(result);

        //NyQL.execute("")
        //Quickly.configOnce();
        //parse();
    }

    private static void parse() throws Exception {
        Map<String, Object> data = new HashMap<>();
        List<Integer> teams = asList(1410, 1411);
        List<Integer> modules = asList(97389, 97390, 97391);

        data.put("teamIDs", teams);
        data.put("moduleIDs", modules);
        data.put("filmId", 250);

        QScript result = Quickly.parse("select", data);
        System.out.println(result);
    }

    private static void execute() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("minRentals", 25);
        data.put("customerId", 2);
        data.put("filmId", 250);

        Object result = Quickly.execute("top_customers", data);
        if (result instanceof List) {
            for (Object row : (List)result) {
                LOGGER.debug(row.toString());
            }
        }
    }

    @SafeVarargs
    private static <T> List<T> asList(T... items) {
        List<T> list = new LinkedList<T>();
        Collections.addAll(list, items);
        return list;
    }

}
