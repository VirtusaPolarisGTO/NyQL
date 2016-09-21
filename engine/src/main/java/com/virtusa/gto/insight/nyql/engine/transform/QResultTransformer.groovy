package com.virtusa.gto.insight.nyql.engine.transform

import java.util.function.Function

/**
 * @author IWEERARATHNA
 */
trait QResultTransformer<T, R> implements Function<T, R> {

    long convertUpdateResult(long val) {
        return val;
    }

}
