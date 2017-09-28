package com.virtusa.gto.nyql.engine.transform

import com.virtusa.gto.nyql.engine.impl.NyQLResult
import com.virtusa.gto.nyql.engine.impl.QJdbcExecutor
import com.virtusa.gto.nyql.model.QPagedScript
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import java.sql.ResultSet
import java.sql.ResultSetMetaData
/**
 * @author iweerarathna
 */
@CompileStatic
class NyJdbcIterable implements Iterable<NyQLResult>, Iterator<NyQLResult>, Closeable {

    private QJdbcExecutor jdbcExecutor
    private ResultSet resultSet
    private QPagedScript script

    private int cc = 0
    private Map<Integer, String> cols = [:]

    @PackageScope
    NyJdbcIterable(ResultSet resultSet, QJdbcExecutor parent, QPagedScript pagedScript) {
        this.resultSet = resultSet
        this.jdbcExecutor = parent
        this.script = pagedScript
    }

    @Override
    Iterator<NyQLResult> iterator() {
        return this.start()
    }

    @Override
    void close() throws IOException {
        if (!resultSet.isClosed()) {
            resultSet.close()
        }

        if (resultSet.statement != null) {
            resultSet.statement.close()
        }
        jdbcExecutor.closeConnection()

        // remove the references as well, so GC can collect immediately
        jdbcExecutor = null
        script = null
        resultSet = null
    }

    @Override
    synchronized boolean hasNext() {
        boolean avail = resultSet.next()
        if (!avail) {
            close()
        }
        avail
    }

    @Override
    synchronized NyQLResult next() {
        int ps = script.pageSize
        int curr = 0

        NyQLResult nyQLResult = new NyQLResult()
        while (resultSet.next()) {
            Map<String, Object> row = [:]
            for (int i = 1; i <= cc; i++) {
                row.put(cols[i], resultSet.getObject(i))
            }
            nyQLResult.add(row)
            curr++

            // return only maximum of specified rows
            if (curr >= ps) {
                break
            }
        }
        return nyQLResult
    }

    private NyJdbcIterable start() {
        if (cc == 0) {
            ResultSetMetaData metaData = resultSet.getMetaData()
            int cc = metaData.columnCount
            Map<Integer, String> cols = [:]
            for (int i = 1; i <= cc; i++) {
                cols.put(i, metaData.getColumnLabel(i))
            }
        }
        return this
    }
}
