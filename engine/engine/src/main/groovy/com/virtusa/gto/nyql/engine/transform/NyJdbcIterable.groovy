package com.virtusa.gto.nyql.engine.transform

import com.virtusa.gto.nyql.engine.impl.NyQLResult
import com.virtusa.gto.nyql.engine.impl.QJdbcExecutor
import com.virtusa.gto.nyql.model.QPagedScript
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.ResultSet
import java.sql.ResultSetMetaData
/**
 * @author iweerarathna
 */
@CompileStatic
class NyJdbcIterable implements Iterable<NyQLResult>, Iterator<NyQLResult>, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyJdbcIterable)

    private QJdbcExecutor jdbcExecutor
    private ResultSet resultSet
    private QPagedScript script

    private int columnCount = 0
    private Map<Integer, String> cols = [:]
    private boolean started = false
    private boolean closed = false

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
        LOGGER.debug('Closing jdbc pagination iterator.')
        QJdbcExecutor.onCloseInvoke(script, resultSet.statement)

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
        closed = true
    }

    @Override
    synchronized boolean hasNext() {
        return !closed
    }

    @Override
    synchronized NyQLResult next() {
        if (!started) {
            start()
        }

        int ps = script.pageSize
        int curr = 0

        NyQLResult nyQLResult = new NyQLResult()
        nyQLResult.setFetchedColumns(cols.values())
        boolean limitReached = false

        while (resultSet.next()) {
            Map<String, Object> row = [:]
            for (int i = 1; i <= columnCount; i++) {
                row.put(cols[i], resultSet.getObject(i))
            }
            nyQLResult.add(row)
            curr++

            // return only maximum of specified rows
            if (curr >= ps) {
                limitReached = true
                break
            }
        }

        if (!limitReached) {
            // we have read all records
            this.close()
        }
        return nyQLResult
    }

    private NyJdbcIterable start() {
        if (columnCount == 0) {
            ResultSetMetaData metaData = resultSet.getMetaData()
            columnCount = metaData.columnCount
            for (int i = 1; i <= columnCount; i++) {
                cols.put(i, metaData.getColumnLabel(i))
            }

        }
        started = true
        return this
    }
}
