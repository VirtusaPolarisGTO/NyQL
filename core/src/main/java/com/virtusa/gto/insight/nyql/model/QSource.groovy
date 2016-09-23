package com.virtusa.gto.insight.nyql.model
/**
 * @author IWEERARATHNA
 */
class QSource {

    private final String id
    private final File file
    private boolean doCache = false

    QSource(String theId, File theFile) {
        id = theId
        file = theFile
    }

    String getId() {
        return id
    }

    File getFile() {
        return file
    }

    synchronized boolean getDoCache() {
        return doCache
    }

    synchronized void setDoCache(boolean doCache) {
        this.doCache = doCache
    }
}
