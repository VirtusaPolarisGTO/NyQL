package com.virtusa.gto.insight.nyql.engine.impl;

import com.virtusa.gto.insight.nyql.utils.TriConsumer;

import java.sql.PreparedStatement;

/**
 * @author IWEERARATHNA
 */
interface JdbcConsumer extends TriConsumer<PreparedStatement, Integer, Object> {



    default void assign(PreparedStatement statement, Integer index, Object value) throws Exception {
        consume(statement, index, value);
    }

}
