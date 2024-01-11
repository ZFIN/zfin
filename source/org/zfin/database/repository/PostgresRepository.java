package org.zfin.database.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.zfin.database.PostgresSession;
import org.zfin.database.presentation.DatabaseFormBean;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.List;

/**
 * Repository for connecting to sysmaster database, the database that holds info
 * about the locks, sessions and other meta data.
 */
public class PostgresRepository {

    public static List<PostgresSession> getSystemDatabases(DatabaseFormBean formBean) {
        Session session = HibernateUtil.currentSession();
        String fromPostgresSession = "from PostgresSession order by dbname";
        Query<PostgresSession> query = session.createQuery(fromPostgresSession, PostgresSession.class);
        return query.list();
    }

    public static List<PostgresSession> getAllSessions(DatabaseFormBean formBean) {
        Session session = HibernateUtil.currentSession();
        String hql = "from PostgresSession where dbname = :dbname";
        if (formBean.isActive())
            hql += " AND state = state";
        hql += " order by dateLastUsed";
        Query<PostgresSession> query = session.createQuery(hql, PostgresSession.class);
        query.setParameter("dbname", ZfinPropertiesEnum.DB_NAME.value());
        if (formBean.isActive())
            query.setParameter("state", "active");

        return query.list();
    }

    public static List<String> getAllDbNames() {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("select dbname from PostgresSession where dbname is not null");
        return query.list();
    }

}
