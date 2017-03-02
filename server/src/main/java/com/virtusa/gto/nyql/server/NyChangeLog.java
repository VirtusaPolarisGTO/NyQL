package com.virtusa.gto.nyql.server;

import com.virtusa.gto.nyql.engine.NyQLInstance;
import com.virtusa.gto.nyql.engine.impl.QJdbcExecutorFactory;
import com.virtusa.gto.nyql.exceptions.NyException;
import com.virtusa.gto.nyql.model.QExecutorFactory;
import com.virtusa.gto.nyql.model.impl.QProfExecutorFactory;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;

/**
 * @author iweerarathna
 */
class NyChangeLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(NyChangeLog.class);

    private final NyQLInstance nyQLInstance;

    NyChangeLog(NyQLInstance nyQLInstance) {
        this.nyQLInstance = nyQLInstance;
    }

    void execute() throws Exception {
        File masterFile = null;
        String changelog = NyServer.readEnv("NYQL_LIQUIBASE_CHANGELOG_FILE", null);
        if (changelog == null) {
            LOGGER.info("No liquibase changelog file specified. Skipping db migration.");
            return;
        }

        LOGGER.info("[LIQUIBASE] Liquibase changelog specified [" + changelog + "]");
        masterFile = new File(changelog);
        String basePath = masterFile.getParent();
        QExecutorFactory qExecutorFactory = nyQLInstance.getConfigurations().getExecutorRegistry().defaultExecutorFactory();
        QJdbcExecutorFactory jdbcExecutorFactory = findJdbcFactory(qExecutorFactory);
        if (jdbcExecutorFactory != null) {
            Connection connection = null;
            Database database = null;
            try {
                connection = jdbcExecutorFactory.getJdbcPool().getConnection();
                database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                Liquibase liquibase = new Liquibase(masterFile.getAbsolutePath(),
                        new FileSystemResourceAccessor(basePath), database);

                //Writer writer = new Slf4jWriter("LIQUIBASE");
                //Writer writer = new PrintWriter(System.out);
                //liquibase.update(new Contexts(), writer);
                liquibase.update(new Contexts());

                database.commit();
                LOGGER.info("[LIQUIBASE] Database changelog execution completed.");

            } finally {
                if (database != null) {
                    database.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } else {
            throw new NyException("Unknown NyQL JDBC configuration!");
        }
    }

    private QJdbcExecutorFactory findJdbcFactory(QExecutorFactory executorFactory) {
        if (executorFactory instanceof QJdbcExecutorFactory) {
            return (QJdbcExecutorFactory) executorFactory;
        } else if (executorFactory instanceof QProfExecutorFactory) {
            return findJdbcFactory(((QProfExecutorFactory) executorFactory).childFactory());
        }
        return null;
    }

//    private static class Slf4jWriter extends Writer {
//
//        private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jWriter.class);
//
//        private final String topic;
//
//        private Slf4jWriter(String theTopic) {
//            topic = theTopic;
//        }
//
//        @Override
//        public void write(char[] cbuf, int off, int len) throws IOException {
//            if (len > 0) {
//                LOGGER.debug("[" + topic + "] " + String.valueOf(cbuf));
//            }
//        }
//
//        @Override
//        public void write(String str) throws IOException {
//            if (str.trim().length() > 0) {
//                LOGGER.debug("[" + topic + "] " + str.trim());
//            }
//        }
//
//        @Override
//        public void flush() throws IOException {
//            // do nothing
//        }
//
//        @Override
//        public void close() throws IOException {
//            // do nothing
//        }
//    }

}
