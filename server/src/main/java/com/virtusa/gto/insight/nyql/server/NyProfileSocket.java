package com.virtusa.gto.insight.nyql.server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author IWEERARATHNA
 */
@WebSocket
public class NyProfileSocket {


    /**
     * Maximum number of threads that can be emit at once.
     */
    private static final int MAX_THREADS = 2;

    /**
     * Queue holding all connected sessions.
     */
    private static final Queue<Session> queue = new ConcurrentLinkedQueue<>();

    /**
     * Pool running for emits.
     */
    private static final ExecutorService POOL = Executors.newFixedThreadPool(MAX_THREADS);

    private static final Gson GSON = new Gson();

    @OnWebSocketConnect
    public void connected(Session session) {
        queue.add(session);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        queue.remove(session);
    }


    /**
     * Sends the given message to all clients asynchronously.
     *
     * @param msg input message to send.
     */
    static void sendToAll(final Object msg) {
        POOL.execute(() -> {
            for (Session s : queue) {
                try {
                    s.getRemote().sendString(GSON.toJson(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Closes all active connection.
     */
    static void closeAllConnections() {
        for (Session s : queue) {
            s.close();
        }

        queue.clear();
        POOL.shutdownNow();
    }

}
