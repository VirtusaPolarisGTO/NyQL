package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */
class Tables extends HashMap<String, Table> {

    def rename(String oldKey, String newKey) {
        Table oVal = get(oldKey)
        remove(oldKey)
        put(newKey, oVal)
    }

}
