package com.virtusa.gto.nyql.profilers

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.model.QProfiling
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QSession
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
/**
 * Calls web http end point with script execution data asynchronously.
 *
 * @author IWEERARATHNA
 */
class QHttpProfiler implements QProfiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(QHttpProfiler)

    private static final int DEF_POOL = 3
    private static final String POST = 'POST'
    private static final String POOL_SIZE = 'poolSize'

    private ExecutorService POOL

    private URL url
    private String httpMethod



    @Override
    void start(Map options) {
        if (!options.containsKey('url')) {
            throw new NyConfigurationException('Mandatory input url parameter does not exist!')
        }

        POOL = Executors.newFixedThreadPool((int)(options[POOL_SIZE] == null ? DEF_POOL : options[POOL_SIZE]))
        url = new URL(options['url'].toString())
        httpMethod = options['httpMethod'] ?: POST
    }

    @Override
    void doneParsing(final String scriptId, final long elapsed, final QSession session) {
        POOL.submit(new Runnable() {
            @Override
            void run() {
                OutputStream os
                HttpURLConnection con

                try {
                    con = (HttpURLConnection) url.openConnection()
                    con.setDoOutput(true)
                    con.setRequestMethod(httpMethod)
                    con.setRequestProperty('Content-Type', 'application/json')

                    String data = new JsonBuilder([operation: 'parsing', scriptId: scriptId,
                                                   elapsed: elapsed, time: System.currentTimeMillis()]).toString()
                    os = con.getOutputStream()
                    os.write(data.getBytes())
                    os.flush()
                } catch (Exception ex) {
                    LOGGER.error("Error occurred while", ex)
                } finally {
                    if (os != null) {
                        os.close()
                    }
                    if (con != null) {
                        con.disconnect()
                    }
                }
            }
        })
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        POOL.submit(new Runnable() {
            @Override
            void run() {
                OutputStream os
                HttpURLConnection con

                try {
                    con = (HttpURLConnection) url.openConnection()
                    con.setDoOutput(true)
                    con.setRequestMethod(httpMethod)
                    con.setRequestProperty('Content-Type', 'application/json')

                    String data = new JsonBuilder([operation: 'execution', scriptId: script.id,
                                                   elapsed: elapsed, time: System.currentTimeMillis()]).toString()
                    os = con.getOutputStream()
                    os.write(data.getBytes())
                    os.flush()
                } finally {
                    if (os != null) {
                        os.close()
                    }
                    if (con != null) {
                        con.disconnect()
                    }
                }
            }
        })
    }

    @Override
    void close() throws IOException {
        POOL.shutdown()
        LOGGER.debug('Closed http profiler.')
    }
}
