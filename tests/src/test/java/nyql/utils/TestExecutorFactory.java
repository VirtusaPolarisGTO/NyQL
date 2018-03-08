package nyql.utils;

import com.virtusa.gto.nyql.configs.Configurations;
import com.virtusa.gto.nyql.engine.impl.QDummyExecutor;
import com.virtusa.gto.nyql.model.DbInfo;
import com.virtusa.gto.nyql.model.QExecutor;
import com.virtusa.gto.nyql.model.QExecutorFactory;

import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class TestExecutorFactory implements QExecutorFactory {

    private int maxInvocations;
    private int maxReuseInvocations;
    private int totalInvocations = 0;
    private int totalReuseInvocations = 0;
    private final Object lock = new Object();
    private Configurations nyConfigs;

//    public TestExecutorFactory(int maxInvocations, int maxReuseInvocations) {
//        this.maxInvocations = maxInvocations;
//        this.maxReuseInvocations = maxReuseInvocations;
//    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public DbInfo init(Map options, Configurations configurations) {
        nyConfigs = configurations;
        if (options.containsKey("max")) {
            maxInvocations = Integer.parseInt(options.get("max").toString());
        } else {
            throw new AssertionError("Maximum invocation is not specified!");
        }

        if (options.containsKey("maxReuse")) {
            maxReuseInvocations = Integer.parseInt(options.get("maxReuse").toString());
        } else {
            throw new AssertionError("Maximum reuse invocation is not specified!");
        }
        return DbInfo.UNRESOLVED;
    }

    @Override
    public QExecutor create() {
        synchronized (lock) {
            ++totalInvocations;
            if (totalInvocations > maxInvocations) {
                throw new AssertionError("Executor invoked more than it requested! " +
                        "[" + totalInvocations + " > " + maxInvocations + "]");
            }
        }
        return new QDummyExecutor(nyConfigs);
    }

    @Override
    public QExecutor createReusable() {
        synchronized (lock) {
            ++totalReuseInvocations;
            if (totalReuseInvocations > maxReuseInvocations) {
                throw new AssertionError("Executor invoked more than it requested for reusable executors!" +
                        " [" + totalReuseInvocations + " > " + maxReuseInvocations + "]");
            }
        }
        return new QDummyExecutor(nyConfigs);
    }

    @Override
    public void shutdown() {

    }
}
