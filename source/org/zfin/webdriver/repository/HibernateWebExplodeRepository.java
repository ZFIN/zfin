package org.zfin.webdriver.repository;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.zfin.database.DbSystemUtil;
import org.zfin.framework.HibernateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class that retrieves WebExplode contents by using native SQL:
 * A Connection object is taken from Session.connection().
 * No commit is done here as transactions are handled on a higher level (servlet filter or controller).
 */
public class HibernateWebExplodeRepository implements WebExplodeRepository {

    private final Logger LOG = Logger.getLogger(HibernateWebExplodeRepository.class);

    public String getWebExplodeContents(String apgName, String queryString) throws SQLException{
        Session session = HibernateUtil.currentSession();
        Connection conn = session.connection();
        StringBuilder query = new StringBuilder();
        if (apgName == null || apgName.length() == 0) {
            query.append("execute function WebExplode(");
            query.append("'object");
        } else {
            query.append("execute function getWebExplode('");
            query.append(apgName);
        }
        query.append("','");
        query.append(queryString);
        query.append("')");
        PreparedStatement statement = null;
        String result = null;
        try {
            statement = conn.prepareStatement(query.toString());
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                String message = "Could not find any record for  " + query.toString();
                LOG.info(message);
            } else {
                result = rs.getString(1);
            }
            rs.close();
        }
        catch (SQLException sqle) {
            LOG.error("could not run getwebExplode() Query: ", sqle);
            LOG.error(DbSystemUtil.getLockInfo());
            throw sqle;
        }
        finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
