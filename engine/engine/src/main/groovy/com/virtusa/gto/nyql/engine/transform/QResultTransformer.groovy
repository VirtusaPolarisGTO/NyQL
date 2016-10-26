package com.virtusa.gto.nyql.engine.transform

import java.util.function.Function

/**
 * @author IWEERARATHNA
 */
interface QResultTransformer<T, R> extends Function<T, R> {

    long convertUpdateResult(long val)

}
