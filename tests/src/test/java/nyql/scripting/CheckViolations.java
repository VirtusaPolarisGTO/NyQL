package nyql.scripting;

import com.virtusa.gto.nyql.configs.ConfigBuilder;
import com.virtusa.gto.nyql.configs.ConfigParser;
import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.exceptions.NyConfigurationException;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.exceptions.NyInitializationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author iweerarathna
 */
@Test(groups = "scripts")
public class CheckViolations {

    private static NyQLInstance createNyQLFrom(String resourcePath, File folder) throws IOException, NyConfigurationException {
        System.setProperty("com.virtusa.gto.insight.nyql.autoBootstrap", "false");
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            //Map<String, Object> configs = ConfigParser.parseAndResolve(new File("./configs/nyql-test.json"));
            Map<String, Object> configs = ConfigParser.parseAndResolve(inputStream);
            ConfigBuilder configBuilder = ConfigBuilder.instance().setupFrom(configs)
                    .setTheScriptFolder(folder);

            configBuilder.doCheckCacheValidation(true);
            return NyQLInstance.create(configBuilder.build());
        }
    }

    public void testViolationsScript() throws Exception {
        NyInitializationException ex = null;
        try (NyQLInstance nyQLInstance = createNyQLFrom("test/nyql-test-viol1.json",
                new File("./scripts/scripts/violations/t1"))) {
            nyQLInstance.execute("cachedScript");
        } catch (NyException x) {
            ex = findNyInitException(x);
        } finally {
            Assert.assertNotNull(ex);
        }
    }

    public void testViolationsSessions() throws Exception {
        NyInitializationException ex = null;
        try (NyQLInstance nyQLInstance = createNyQLFrom("test/nyql-test-viol1.json",
                new File("./scripts/scripts/violations/t2"))) {
            nyQLInstance.execute("abulkinsert");
        } catch (NyException x) {
            ex = findNyInitException(x);
        } finally {
            Assert.assertNotNull(ex);
        }
    }

    public void testViolationsHierarchy() throws Exception {
        NyInitializationException ex = null;
        try (NyQLInstance nyQLInstance = createNyQLFrom("test/nyql-test-viol1.json",
                new File("./scripts/scripts/violations/t3"))) {
            nyQLInstance.execute("hierarchyScript");
        } catch (Throwable x) {
            ex = findNyInitException(x);
        } finally {
            Assert.assertNotNull(ex);
        }
    }

    private NyInitializationException findNyInitException(Throwable e) {
        if (e instanceof NyInitializationException) {
            return (NyInitializationException) e;
        } else if (e.getCause() != null) {
            return findNyInitException(e.getCause());
        } else {
            return null;
        }
    }
}
