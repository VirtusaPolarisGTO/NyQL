import com.virtusa.gto.nyql.QContextType;
import com.virtusa.gto.nyql.configs.ConfigBuilder;
import com.virtusa.gto.nyql.ddl.DKeyIndexType;
import com.virtusa.gto.nyql.ddl.DKeyType;

import java.util.Collections;

/**
 * @author IWEERARATHNA
 */
public class ConfigTest {

    public void test() {
        ConfigBuilder configBuilder = ConfigBuilder.instance();
        configBuilder.activateDb("mysql")
                .addDefaultImporter(DKeyType.class.getName())
                .addDefaultImporters(DKeyIndexType.class.getName())
                .addDefaultImporters(Collections.singletonList(QContextType.class.getName()))
                .doCacheCompiledScripts()
                .doCacheGeneratedQueries()
                .addTranslator("com.virtusa.gto.insight.nyql.db.mysql.MySql")
                .addTranslators(Collections.singletonList("com.virtusa.gto.insight.nyql.db.pg.Postgre"));



    }

}
