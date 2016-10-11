package com.virtusa.gto.insight.nyql.profilers

import javax.websocket.*
import javax.websocket.server.ServerEndpoint
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author IWEERARATHNA
 */
@ServerEndpoint("/nyql/")
class WSServer {

    /**
     * Maximum number of threads that can be emit at once.
     */
    private static final int MAX_THREADS = 2

    /**
     * Queue holding all connected sessions.
     */
    private static final Queue<Session> queue = new ConcurrentLinkedQueue<>()

    /**
     * Pool running for emits.
     */
    private static final ExecutorService POOL = Executors.newFixedThreadPool(MAX_THREADS)

    @OnMessage
    public void onMessage(Session session, String msg) {
        // don't do anything here.
    }

    @OnOpen
    public void onOpen(Session session) {
        queue.add(session)
    }

    @OnError
    public void error(Session session, Throwable t) {
        queue.remove(session)
    }

    @OnClose
    public void onClose(Session session) {
        queue.remove(session)
    }

    /**
     * Sends the given message to all clients asynchronously.
     *
     * @param msg input message to send.
     */
    static void sendToAll(final Object msg) {
        POOL.execute(new Runnable() {
            @Override
            void run() {
                for (Session s : queue) {
                    s.asyncRemote.sendObject(msg)
                }
            }
        })
    }

    /**
     * Closes all active connection.
     */
    static void closeAllConnections() {
        for (Session s : queue) {
            s.close()
        }

        queue.clear()
        POOL.shutdown()
    }

}
