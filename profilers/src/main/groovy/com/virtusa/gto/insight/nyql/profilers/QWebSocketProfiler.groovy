package com.virtusa.gto.insight.nyql.profilers

import com.virtusa.gto.insight.nyql.model.QProfiling
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author IWEERARATHNA
 */
@Deprecated
class QWebSocketProfiler implements QProfiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(QWebSocketProfiler)

    /**
     * default port of web socket.
     */
    //private static final int PORT = 9029

    private static final String TYPE_PARSE = 'Parse'
    private static final String TYPE_EXECUTE = 'Execute'

//    private Server server
//    private ServerConnector connector
//    private ServletContextHandler context
//    private ServerContainer wsContainer

    @Override
    void start(Map options) {
//        if (server != null && server.isRunning()) {
//            return
//        }
//
//        int port = options.port ?: PORT
//        LOGGER.debug("Starting profiler @ localhost:$port ...")
//
//        // we don't need jetty logs printing
//        Log.setLog(new JettyNoLog())
//
//        // initialize jetty server.
//        server = new Server()
//        connector = new ServerConnector(server)
//        connector.setPort(port)
//        server.addConnector(connector)
//
//        context = new ServletContextHandler(ServletContextHandler.SESSIONS)
//        context.setContextPath('/')
//        server.setHandler(context)
//
//        wsContainer = WebSocketServerContainerInitializer.configureContext(context)
//        wsContainer.addEndpoint(WSServer)
//
//        // start the server.
//        server.start()
    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        WSServer.sendToAll(String.valueOf([type: TYPE_PARSE, id: scriptId, time: elapsed]))
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        WSServer.sendToAll(String.valueOf([type: TYPE_EXECUTE, id: script.id, time: elapsed, query: script.proxy?.query]))
    }

    @Override
    void close() throws IOException {
        LOGGER.debug('Shutting down profiler web socket!')
        WSServer.closeAllConnections()
//        if (context != null) {
//            context.shutdown()
//        }
//        if (connector != null) {
//            connector.shutdown()
//        }
//        if (server != null) {
//            server.stop()
//        }
        LOGGER.debug('Successfully shutdown web-socket.')
    }

}
