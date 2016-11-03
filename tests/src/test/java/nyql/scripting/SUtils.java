package nyql.scripting;

import com.virtusa.gto.nyql.configs.ConfigBuilder;
import com.virtusa.gto.nyql.configs.ConfigParser;
import com.virtusa.gto.nyql.engine.NyQLInstance;
import nyql.utils.TUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
class SUtils {

    @SuppressWarnings("unchecked")
    static NyQLInstance createNyQL(int maxExecutors, int maxReuseExecutors) {
        System.setProperty("com.virtusa.gto.insight.nyql.autoBootstrap", "false");
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("nyql-test.json")) {
            //Map<String, Object> configs = ConfigParser.parseAndResolve(new File("./configs/nyql-test.json"));
            Map<String, Object> configs = ConfigParser.parseAndResolve(inputStream);
            ConfigBuilder configBuilder = ConfigBuilder.instance().setupFrom(configs);

            configBuilder.addExecutor(TUtils.executorWithMax(maxExecutors, maxReuseExecutors));
            return NyQLInstance.create(configBuilder.build());
        } catch (Exception ex) {
            throw new RuntimeException("Error loading configuration json from classpath!", ex);
        }

    }

}
