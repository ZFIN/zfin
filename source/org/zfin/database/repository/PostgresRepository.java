package org.zfin.database.repository;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.zfin.database.DatabaseLock;
import org.zfin.database.PostgresSession;
import org.zfin.database.SysOpenDb;
import org.zfin.database.SysSession;
import org.zfin.database.presentation.DatabaseFormBean;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.SysmasterHibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Repository for connecting to sysmaster database, the database that holds info
 * about the locks, sessions and other meta data.
 */
public class PostgresRepository {

    public static List<DatabaseLock> getLocks() {
        Session session = SysmasterHibernateUtil.getSession();
        Query query = session.createQuery("select lock from DatabaseLock lock " +
                "where lock.dbsName = :dbName");
        query.setString("dbName", ZfinPropertiesEnum.DB_NAME.toString());
        return query.list();
    }

    public static List<PostgresSession> getSystemDatabases(DatabaseFormBean formBean) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PostgresSession.class);
        if (formBean.getOrderBy() != null)
            criteria.addOrder(Order.asc(formBean.getOrderBy()));
        else
            criteria.addOrder(Order.asc("dbname"));
        return criteria.list();
    }

    public static List<PostgresSession> getAllSessions(DatabaseFormBean formBean) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PostgresSession.class);
        criteria.add(Restrictions.eq("dbname", ZfinPropertiesEnum.DB_NAME.value()));

        if (formBean.isActive())
            criteria.add(Restrictions.eq("state", "active"));

        criteria.addOrder(Order.desc("dateLastUsed"));
        return (List<PostgresSession>) criteria.list();
    }

    private static List<SysSession> createSystemSessions(List<Object[]> list) {
        List<SysSession> sessions = new ArrayList<SysSession>(list.size());
        for (Object[] object : list) {
            SysSession session = new SysSession();
            session.setSid((Integer) object[0]);
            session.setUserName((String) object[1]);
            session.setUid((Short) object[2]);
            session.setPid((Integer) object[3]);
            session.setHostname((String) object[4]);
            Date startDate = (Date) object[18];
            session.setStartDate(toPstFromGMT(startDate));
            SysOpenDb sysOpenDb = new SysOpenDb();
            sysOpenDb.setName((String) object[21]);
            sysOpenDb.setIsolation((Short) object[25]);
            sysOpenDb.setSid((Integer) object[19]);

            session.setSysOpenDb(sysOpenDb);
            sessions.add(session);
        }
        return sessions;
    }

    public static Date toPstFromGMT(Date date) {
        TimeZone pst = TimeZone.getTimeZone("PST");
        return new Date(date.getTime() + pst.getRawOffset());
    }


    public static List<String> getAllDbNames() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PostgresSession.class);
        criteria.setProjection(Projections.distinct(Projections.property("dbname")));
        criteria.add(Restrictions.isNotNull("dbname"));
        //String hql  = "select distinct dbname from PostgresSession";
        //Query query = session.createQuery(hql);
        return criteria.list();
    }

    public static SysSession getSessionDetail(int id) {
        Session session = SysmasterHibernateUtil.getSession();
        SysSession sysSession = (SysSession) session.get(SysSession.class, id);

        return sysSession;
    }
}
