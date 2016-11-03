package nyql.scripting;

import com.virtusa.gto.nyql.configs.ConfigBuilder;
import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.exceptions.NyException;
import groovy.json.JsonSlurper;
import nyql.utils.TUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
@Test(groups = "scripts")
public class ScriptTest {

    private NyQLInstance nyQLInstance;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void startup() {
        System.setProperty("com.virtusa.gto.insight.nyql.autoBootstrap", "false");
        Map<String, Object> configs = (Map<String, Object>) new JsonSlurper().parse(new File("./configs/nyql-test.json"), StandardCharsets.UTF_8.name());
        ConfigBuilder configBuilder = ConfigBuilder.instance().setupFrom(configs);

        configBuilder.addExecutor(TUtils.executorWithMax(1, 1));
        nyQLInstance = NyQLInstance.create(configBuilder.build());
    }

    @AfterClass
    public void teardown() {
        nyQLInstance.shutdown();
    }

    public void testVariables() throws NyException {
        nyQLInstance.execute("scripts/variable_test");
    }

}
