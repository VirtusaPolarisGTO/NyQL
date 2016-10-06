package com.virtusa.gto.insight.nyql

/**
 * @author Isuru Weerarathna
 */
class FunctionColumn extends Column {

    def _wrapper

    String _func

    List<Object> _columns

    boolean _setOfCols = false
}
