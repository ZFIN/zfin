package org.zfin.audit.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.zfin.audit.AuditLogItem;
import org.zfin.framework.HibernateUtil;

import java.util.List;

/**
 * Implementation of the audit log repository for database access.
 */
public class HibernateAuditLogRepository implements AuditLogRepository {

    public AuditLogItem getLatestAuditLogItem(String recordID) {
        Session session = HibernateUtil.currentSession();
        Query<AuditLogItem> query = session.createQuery("from AuditLogItem where zdbID = :zdbID order by dateUpdated desc", AuditLogItem.class);
        query.setParameter("zdbID", recordID);
        query.setMaxResults(1);

        List<AuditLogItem> items = query.list();
        if (items == null || items.isEmpty())
            return null;
        else
            return items.get(0);
    }

    public List<AuditLogItem> getAuditLogItems(String recordID) {
        Session session = HibernateUtil.currentSession();
        Query<AuditLogItem> query = session.createQuery("from AuditLogItem where zdbID = :zdbID order by dateUpdated desc", AuditLogItem.class);
        query.setParameter("zdbID", recordID);
        List<AuditLogItem> list = query.list();
        return list;
    }
}
