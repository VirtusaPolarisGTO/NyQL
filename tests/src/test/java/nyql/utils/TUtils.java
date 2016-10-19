package nyql.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class TUtils {

    public static Map<String, Object> executorWithMax(int max, int maxReuse) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        map.put("factory", TestExecutorFactory.class.getName());

        map.put("max", max);
        map.put("maxReuse", maxReuse);
        return map;
    }

}
