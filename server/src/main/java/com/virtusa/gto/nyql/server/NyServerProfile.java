package com.virtusa.gto.nyql.server;

import com.virtusa.gto.nyql.model.QProfiling;
import com.virtusa.gto.nyql.model.QScript;
import com.virtusa.gto.nyql.model.QSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class NyServerProfile implements QProfiling {
    @Override
    public void start(Map options) {

    }

    @Override
    public void doneParsing(String scriptId, long elapsed, QSession session) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "Parse");
        data.put("id", scriptId);
        data.put("time", elapsed);

        NyProfileSocket.sendToAll(data);
    }

    @Override
    public void doneExecuting(QScript script, long elapsed) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "Parse");
        data.put("id", script.getId());
        data.put("time", elapsed);

        NyProfileSocket.sendToAll(data);
    }

    @Override
    public void close() throws IOException {

    }
}
