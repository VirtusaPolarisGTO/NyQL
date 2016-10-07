import com.virtusa.gto.insight.nyql.configs.ConfigBuilder;
import org.junit.Test;

/**
 * @author IWEERARATHNA
 */
public class ConfigTest {

    @Test
    public void test() {
        ConfigBuilder builder = ConfigBuilder.instance().activateDb("mysql");
        builder.build();
        //builder.activateDb("sss");
    }

}
