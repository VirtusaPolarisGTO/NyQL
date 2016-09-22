import com.virtusa.gto.insight.nyql.engine.Quickly;
import com.virtusa.gto.insight.nyql.model.QScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class DBExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBExecutor.class);

    public static void main(String[] args) throws Exception {
        Quickly.configOnce();

        parse();
    }

    private static void parse() throws Exception {
        File srcDir = new File("C:\\Projects\\insight5\\nyql\\core\\src\\examples\\sakila");
        QScript result = Quickly.parse(srcDir, "all_rentals_of_customer");
        System.out.println(result.getProxy().getQuery());
        System.out.println(result.getProxy().getOrderedParameters());
    }

    private static void execute() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("minRentals", 25);
        data.put("customerId", 2);
        data.put("filmId", 250);

        File srcDir = new File("C:\\Projects\\insight5\\nyql\\core\\src\\examples\\sakila");
        Object result = Quickly.execute(srcDir, "customer_details", data);
        if (result instanceof List) {
            for (Object row : (List)result) {
                LOGGER.debug(row.toString());
            }
        }
    }


}
