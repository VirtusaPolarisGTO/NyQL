package com.virtusa.gto.nyql.ddl

import com.virtusa.gto.nyql.QResultProxy
import com.virtusa.gto.nyql.model.QScript
import com.virtusa.gto.nyql.model.QScriptList
import com.virtusa.gto.nyql.model.QSession
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

/**
 * @author IWEERARATHNA
 */
@ToString(includes = ['tables'])
class DDL {

    private static final Logger LOGGER = LoggerFactory.getLogger(DDL)

    final QSession session
    //final DSLContext dslContext

    Map<String, DTable> tables = [:]
    List<DTable> tablesToDrop = []

    DDL(QSession theSession) {
        session = theSession
        //dslContext = session.dslContext
    }

    @CompileStatic
    DDL TEMP_TABLE(String name, @DelegatesTo(value = DTable, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        DTable dTable = new DTable(name: name, temporary: true)

        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        tables.put(dTable.name, dTable)

        this
    }

    @CompileStatic
    DDL TEMP_TABLE(String name, boolean ifNotExists, @DelegatesTo(value = DTable, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        DTable dTable = new DTable(name: name, temporary: true, ifNotExist: ifNotExists)

        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        tables.put(dTable.name, dTable)

        this
    }

    DDL TABLE(String name, @DelegatesTo(DTable) Closure closure) {
        DTable dTable = new DTable(name: name, temporary: false)

        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        DTable rTable = code()
        tables.put(rTable.name, rTable)
        this
    }

    DDL TABLE(String name, boolean ifNotExists, @DelegatesTo(DTable) Closure closure) {
        DTable dTable = new DTable(name: name, temporary: false, ifNotExist: ifNotExists)

        def code = closure.rehydrate(dTable, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        DTable rTable = code()
        tables.put(rTable.name, rTable)
        this
    }

    @CompileStatic
    DDL DROP_TEMP_TABLE(String name) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name, temporary: true)
        }
        rTable.temporary = true
        tablesToDrop.add(rTable)

        this
    }

    @CompileStatic
    DDL DROP_TEMP_TABLE(String name, boolean ifExists) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name, temporary: true, ifNotExist: ifExists)
        }
        rTable.temporary = true
        tablesToDrop.add(rTable)

        this
    }

    DDL DROP_TABLE(String name) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name)
        }
        tablesToDrop.add(rTable)
        this
    }

    DDL DROP_TABLE(String name, boolean ifExists) {
        DTable rTable
        if (tables.containsKey(name)) {
            rTable = tables[name]
        } else {
            rTable = new DTable(name: name, ifNotExist: ifExists)
        }
        tablesToDrop.add(rTable)
        this
    }

    @CompileStatic
    QScriptList createScripts() {
        QScriptList scriptList = new QScriptList()
        List<QScript> list = new LinkedList<>()
        tables.each {k, t -> list.addAll(callCreateTable(t)) }
        tablesToDrop.each { list.addAll(callDropTable(it)) }
        scriptList.scripts = list
        scriptList
    }

    @CompileStatic
    private List<QScript> callCreateTable(DTable dTable) {
        LOGGER.debug('Executing table creation command...')
        List<QResultProxy> proxies = session.dbFactory.createTranslator().___ddls().___createTable(dTable)
        proxies.stream().map { session.scriptRepo.parse(it, session) }.collect(Collectors.toList())
    }

    @CompileStatic
    private List<QScript> callDropTable(DTable dTable) {
        LOGGER.debug('Executing table drop command...')
        List<QResultProxy> proxies = session.dbFactory.createTranslator().___ddls().___dropTable(dTable)
        proxies.stream().map { session.scriptRepo.parse(it, session) }.collect(Collectors.toList())
    }
}
