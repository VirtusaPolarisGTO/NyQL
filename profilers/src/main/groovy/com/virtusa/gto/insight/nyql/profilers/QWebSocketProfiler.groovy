package com.virtusa.gto.insight.nyql.profilers

import com.virtusa.gto.insight.nyql.model.QProfiling
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QSession
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.log.Log
import org.eclipse.jetty.util.log.Logger
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer
import org.slf4j.LoggerFactory

import javax.websocket.server.ServerContainer

/**
 * @author IWEERARATHNA
 */
class QWebSocketProfiler implements QProfiling {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(QWebSocketProfiler)

    private static final int PORT = 9029

    private Server server
    private ServerConnector connector
    private ServletContextHandler context
    private ServerContainer wsContainer

    @Override
    void start() {
        if (server != null && server.isRunning()) {
            return
        }

        LOGGER.debug("Starting profiler @ localhost:$PORT ...")
        Log.setLog(new JettyNoLog())
        server = new Server()
        connector = new ServerConnector(server)
        connector.setPort(PORT)
        server.addConnector(connector)

        context = new ServletContextHandler(ServletContextHandler.SESSIONS)
        context.setContextPath('/')
        server.setHandler(context)

        wsContainer = WebSocketServerContainerInitializer.configureContext(context)
        wsContainer.addEndpoint(WSServer)

        server.start()
    }

    @Override
    void doneParsing(String scriptId, long elapsed, QSession session) {
        WSServer.sendToAll(String.valueOf([id: scriptId, time: elapsed]))
    }

    @Override
    void doneExecuting(QScript script, long elapsed) {
        WSServer.sendToAll(String.valueOf([id: script.id, time: elapsed]))
    }

    @Override
    void close() throws IOException {
        LOGGER.debug("Shutting down profiler web socket!")
        if (context != null) {
            context.shutdown()
        }
        if (connector != null) {
            connector.shutdown()
        }
        if (server != null) {
            server.stop()
        }
        LOGGER.debug("Successfully shutdown web-socket.")
    }

    private static class JettyNoLog implements Logger {

        @Override
        String getName() {
            return "nyql-none"
        }

        @Override
        void warn(String s, Object... objects) {

        }

        @Override
        void warn(Throwable throwable) {

        }

        @Override
        void warn(String s, Throwable throwable) {

        }

        @Override
        void info(String s, Object... objects) {

        }

        @Override
        void info(Throwable throwable) {

        }

        @Override
        void info(String s, Throwable throwable) {

        }

        @Override
        boolean isDebugEnabled() {
            return false
        }

        @Override
        void setDebugEnabled(boolean b) {

        }

        @Override
        void debug(String s, Object... objects) {

        }

        @Override
        void debug(String s, long l) {

        }

        @Override
        void debug(Throwable throwable) {

        }

        @Override
        void debug(String s, Throwable throwable) {

        }

        @Override
        Logger getLogger(String s) {
            return this
        }

        @Override
        void ignore(Throwable throwable) {

        }
    }
}
