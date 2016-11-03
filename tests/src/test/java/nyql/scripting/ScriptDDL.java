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
public class ScriptDDL {

    private NyQLInstance nyQLInstance;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void startup() {
        System.setProperty("com.virtusa.gto.insight.nyql.autoBootstrap", "false");
        Map<String, Object> configs = (Map<String, Object>) new JsonSlurper().parse(new File("./configs/nyql-test.json"), StandardCharsets.UTF_8.name());
        ConfigBuilder configBuilder = ConfigBuilder.instance().setupFrom(configs);

        configBuilder.addExecutor(TUtils.executorWithMax(2, 1));
        nyQLInstance = NyQLInstance.create(configBuilder.build());
    }

    @AfterClass
    public void teardown() {
        nyQLInstance.shutdown();
    }

    public void testScriptDDL() throws NyException {
        nyQLInstance.execute("scripts/schema_script");
    }

    public void testASelect() throws NyException {
        nyQLInstance.executeToJSON("scripts/aselect");
    }


}
