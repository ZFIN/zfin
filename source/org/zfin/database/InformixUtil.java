package org.zfin.database;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Util class to run Informix specific sql, e.g. calling stored procedures.
 */
public class InformixUtil {

    private static Logger LOG = Logger.getLogger(InformixUtil.class);

    /**
     * Generic procedure call to Informix.
     *
     * @param procedureName informix procedure name
     * @param arguments     array of arguments that are passed into the procedure call.
     */
    public static void runInformixProcedure(String procedureName, String... arguments) {
        Session session = currentSession();
        // ensure that all records are being processed before this call.
        session.flush();
        Connection connection = session.connection();
        CallableStatement statement = null;
        StringBuffer sql = new StringBuffer("EXECUTE PROCEDURE ");
        sql.append(procedureName);
        if (arguments != null) {
            sql.append("(");
            for (String argument : arguments) {
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
        }
        catch (SQLException exception) {
            LOG.error("could not run " + procedureName + "()", exception);
            throw new RuntimeException(exception);
        }
        finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException e) {
                LOG.error("could not run " + procedureName + "()", e);
            }
        }
    }

}
