package com.virtusa.gto.insight.nyql.utils;

/**
 * @author IWEERARATHNA
 */
public interface TriConsumer<P, Q, R> {

    void consume(P first, Q second, R third) throws Exception;

}
