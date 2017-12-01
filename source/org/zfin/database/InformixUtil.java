package org.zfin.database;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.LoggingUtil;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Util class to run Informix specific sql, e.g. calling stored procedures.
 */
public class InformixUtil {

    private static Logger LOG = Logger.getLogger(InformixUtil.class);
    public static final String PIPE = "|";

    /**
     * Generic procedure call to Informix.
     *
     * @param procedureName informix procedure name
     * @param arguments     array of arguments that are passed into the procedure call.
     */
    public static void runInformixProcedure(final String procedureName, final String... arguments) {
        final LoggingUtil loggingUtil = new LoggingUtil(LOG);
        Session session = currentSession();
        // ensure that all records are being processed before this call.
        session.flush();
        session.doWork(connection -> {
            CallableStatement statement = null;
            StringBuffer sql = new StringBuffer();
            if (ZfinPropertiesEnum.USE_POSTGRES.value().equals("true"))
                sql = sql.append("SELECT ");
            else
                sql = sql.append("EXECUTE PROCEDURE ");
            sql.append(procedureName);
            if (arguments != null) {
                sql.append("(");
                for (String ignored : arguments) {
                    sql.append("?,");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(")");
            }
            try {
                statement = connection.prepareCall(sql.toString());
                if (arguments != null) {
                    int index = 1;
                    for (String argument : arguments) {
                        statement.setString(index, argument);
                        index++;
                    }
                }
                statement.execute();
                String fullFunctionCall = sql.toString();
                if (arguments != null) {
                    for (String argument : arguments) {
                        fullFunctionCall = fullFunctionCall.replaceFirst("\\?", argument);
                    }
                }
                String newline = System.getProperty("line.separator");
                loggingUtil.logDuration("Duration of procedure execution: " + newline + fullFunctionCall);
            } catch (SQLException exception) {
                LOG.error("could not run " + procedureName + "()", exception);
                LOG.error(DbSystemUtil.getLockInfo());
                throw new RuntimeException(exception);
            } finally {
                try {
                    if (statement != null)
                        statement.close();
                } catch (SQLException e) {
                    LOG.error("could not run " + procedureName + "()", e);
                }
            }
        });
    }

    public static String runDBFunction(final String functionName, final String... arguments) {
        final LoggingUtil loggingUtil = new LoggingUtil(LOG);
        Session session = currentSession();
        // ensure that all records are being processed before this call.
        session.flush();
        String value = session.doReturningWork(connection -> {
            CallableStatement statement = null;
            StringBuffer sql = new StringBuffer();
            if (ZfinPropertiesEnum.USE_POSTGRES.value().equals("true"))
                sql = sql.append("SELECT ");
            else
                sql = sql.append("EXECUTE PROCEDURE ");
            sql.append(functionName);
            if (arguments != null) {
                sql.append("(");
                for (String ignored : arguments) {
                    sql.append("?,");
                }
                sql.deleteCharAt(sql.length() - 1);
                sql.append(")");
            }
            try {
                statement = connection.prepareCall(sql.toString());
                if (arguments != null) {
                    int index = 1;
                    for (String argument : arguments) {
                        statement.setString(index, argument);
                        index++;
                    }
                }
                String fullFunctionCall = sql.toString();
                if (arguments != null) {
                    for (String argument : arguments) {
                        fullFunctionCall = fullFunctionCall.replaceFirst("\\?", argument);
                    }
                }
                String newline = System.getProperty("line.separator");
                loggingUtil.logDuration("Duration of procedure execution: " + newline + fullFunctionCall);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString(1);
                } else {
                    return null;
                }
            } catch (SQLException exception) {
                LOG.error("could not run " + functionName + "()", exception);
                LOG.error(DbSystemUtil.getLockInfo());
                throw new RuntimeException(exception);
            } finally {
                try {
                    if (statement != null)
                        statement.close();
                } catch (SQLException e) {
                    LOG.error("could not run " + functionName + "()", e);
                }
            }
        });
        return value;
    }

    public static String getUnloadRecord(String... columns) {
        if (columns == null)
            return null;
        StringBuffer buffer = new StringBuffer();
        for (String column : columns) {
            buffer.append(column);
            buffer.append(PIPE);
        }
        buffer.append(System.getProperty("line.separator"));
        return buffer.toString();
    }

}
