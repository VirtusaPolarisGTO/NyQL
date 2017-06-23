package com.virtusa.gto.nyql.db.maria

import com.virtusa.gto.nyql.db.TranslatorOptions
import com.virtusa.gto.nyql.db.mysql.MySql

/**
 * @author iweerarathna
 */
class MariaSql extends MySql {

    MariaSql() {
        super()
    }

    MariaSql(TranslatorOptions theOptions) {
        super(theOptions)
    }
}
