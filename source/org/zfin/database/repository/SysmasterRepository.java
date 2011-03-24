package org.zfin.database.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.zfin.database.DatabaseLock;
import org.zfin.framework.SysmasterHibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.List;

/**
 * Repository for connecting to sysmaster database, the database that holds info
 * about the locks, sessions and other meta data.
 */
public class SysmasterRepository {

    public static List<DatabaseLock> getLocks() {
        Session session = SysmasterHibernateUtil.getSession();
        Query query = session.createQuery("select lock from DatabaseLock lock " +
                "where lock.dbsName = :dbName");
        query.setString("dbName", ZfinPropertiesEnum.DB_NAME.toString());
        return query.list();
    }
}
