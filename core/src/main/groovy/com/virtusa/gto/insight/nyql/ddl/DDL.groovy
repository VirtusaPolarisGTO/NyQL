package com.virtusa.gto.insight.nyql.ddl

import com.virtusa.gto.insight.nyql.QResultProxy
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.QScriptList
import com.virtusa.gto.insight.nyql.model.QSession
import groovy.transform.ToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
@ToString(includes = ["tables"])
class DDL {

    private static final Logger LOGGER = LoggerFactory.getLogger(DDL.class)

    final QSession session
    //final DSLContext dslContext

    Map<String, DTable> tables = [:]
    List<DTable> tablesToDrop = []

    public DDL(QSession theSession) {
        session = theSession
        //dslContext = session.dslContext
    }

    DDL TEMP_TABLE(String name, @DelegatesTo(DTable) Closure closure) {
        DTable dTable = new DTable(name: name, temporary: true)

        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        DTable rTable = code()
        tables.put(rTable.name, rTable)

        return this
    }

    DDL TEMP_TABLE(String name, boolean ifNotExists, @DelegatesTo(DTable) Closure closure) {
        DTable dTable = new DTable(name: name, temporary: true, ifNotExist: ifNotExists)

        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        DTable rTable = code()
        tables.put(rTable.name, rTable)

        return this
    }

    DDL TABLE(String name, @DelegatesTo(DTable) Closure closure) {
        DTable dTable = new DTable()

        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        DTable rTable = code()
        tables.put(rTable.name, rTable)
        return this
    }

    DDL DROP_TEMP_TABLE(String name) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name, temporary: true)
        }
        rTable.temporary = true
        tablesToDrop.add(rTable)

        return this
    }

    DDL DROP_TABLE(String name) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name)
        }
        tablesToDrop.add(rTable)
        return this
    }

    QScriptList createScripts() {
        QScriptList scriptList = new QScriptList()
        List<QScript> list = new LinkedList<>()
        tables.each {k, t -> list.addAll(callCreateTable(t)) }
        tablesToDrop.each { list.addAll(callDropTable(it)) }
        scriptList.scripts = list
        return scriptList
    }

    private List<QScript> callCreateTable(DTable dTable) {
        LOGGER.debug('Executing table creation command...')
        List<QResultProxy> proxies = session.dbFactory.createTranslator().___ddls().___createTable(dTable)
        return proxies.stream().map({ session.scriptRepo.parse(it, session) }).collect(Collectors.toList());
    }

    private List<QScript> callDropTable(DTable dTable) {
        LOGGER.debug('Executing table drop command...')
        List<QResultProxy> proxies = session.dbFactory.createTranslator().___ddls().___dropTable(dTable)
        return proxies.stream().map({ session.scriptRepo.parse(it, session) }).collect(Collectors.toList());
    }
}
