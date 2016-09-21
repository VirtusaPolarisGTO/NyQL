package com.virtusa.gto.insight.nyql
/**
 * @author Isuru Weerarathna
 */
class Column {

    QContext _ctx = null
    Table _owner = null

    String __name = ""
    String __alias = null

    def alias(String newName) {
        _ctx?.renameColumn(__alias, newName, this)
        __alias = newName
        return this
    }

    def __aliasDefined() {
        return __alias != null
    }

}
