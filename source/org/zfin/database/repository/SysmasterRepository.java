package org.zfin.database.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.object.SqlQuery;
import org.zfin.database.DatabaseLock;
import org.zfin.database.SysDatabase;
import org.zfin.database.SysOpenDb;
import org.zfin.database.SysSession;
import org.zfin.database.presentation.DatabaseFormBean;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.SysmasterHibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.*;

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

    public static List<SysDatabase> getSystemDatabases(DatabaseFormBean formBean) {
        Session session = SysmasterHibernateUtil.getSession();
        Criteria criteria = session.createCriteria(SysDatabase.class);
        if (formBean.getOrderBy() != null)
            criteria.addOrder(Order.asc(formBean.getOrderBy()));
        else
            criteria.addOrder(Order.asc("name"));
        return criteria.list();
    }

    public static List<SysSession> getAllSessions(DatabaseFormBean formBean) {
        Session session = SysmasterHibernateUtil.getSession();
        Criteria criteria = session.createCriteria(SysSession.class);

        String dbname = formBean.getDbname();
        if (StringUtils.isNotEmpty(dbname))
            criteria.add(Restrictions.eq("sysOpenDb.name", dbname));

        String orderBy = formBean.getOrderBy();
        if (orderBy != null) {
            if (orderBy.equals("instance"))
                criteria.addOrder(Order.desc("sysOpenDb.name"));
            else
                criteria.addOrder(Order.desc(orderBy));
        } else
            criteria.addOrder(Order.desc("connected"));


        return (List<SysSession>) criteria.list();
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


    public static List<SysDatabase> getAllDbNames() {
        Session session = SysmasterHibernateUtil.getSession();
        Criteria criteria = session.createCriteria(SysDatabase.class);
        return criteria.list();
    }

    public static SysSession getSessionDetail(int id) {
        Session session = SysmasterHibernateUtil.getSession();
        SysSession sysSession = (SysSession) session.get(SysSession.class, id);

        return sysSession;
    }
}
