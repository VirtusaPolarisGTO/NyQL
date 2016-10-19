package nyql.utils;

import com.virtusa.gto.insight.nyql.engine.impl.QDummyExecutor;
import com.virtusa.gto.insight.nyql.model.QExecutor;
import com.virtusa.gto.insight.nyql.model.QExecutorFactory;

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

//    public TestExecutorFactory(int maxInvocations, int maxReuseInvocations) {
//        this.maxInvocations = maxInvocations;
//        this.maxReuseInvocations = maxReuseInvocations;
//    }

    @Override
    public void init(Map options) {
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
        return new QDummyExecutor();
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
        return new QDummyExecutor();
    }

    @Override
    public void shutdown() {

    }
}
