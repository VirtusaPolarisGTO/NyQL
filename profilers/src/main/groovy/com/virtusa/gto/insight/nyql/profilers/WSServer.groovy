package com.virtusa.gto.insight.nyql.profilers

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author IWEERARATHNA
 */
@ServerEndpoint("/nyql/")
class WSServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WSServer)

    private static Queue<Session> queue = new ConcurrentLinkedQueue<>()

    private static final Executor POOL = Executors.newFixedThreadPool(3)

    @OnMessage
    public void onMessage(Session session, String msg) {

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

}
