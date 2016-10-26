package com.virtusa.gto.nyql
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
