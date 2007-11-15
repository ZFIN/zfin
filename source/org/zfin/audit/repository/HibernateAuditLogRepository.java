package org.zfin.audit.repository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.zfin.audit.AuditLogItem;
import org.zfin.framework.HibernateUtil;

import java.util.List;

/**
 * Implementation of the audit log repository for database access.
 */
public class HibernateAuditLogRepository implements AuditLogRepository {

    public AuditLogItem getLatestAuditLogItem(String recordID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(AuditLogItem.class);
        query.add(Restrictions.eq("zdbID", recordID));
        query.setMaxResults(1);

        List<AuditLogItem> items = (List<AuditLogItem>) query.list();
        if (items == null || items.isEmpty())
            return null;
        else
            return items.get(0);
    }

    public List<AuditLogItem> getAuditLogItems(String recordID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(AuditLogItem.class);
        query.add(Restrictions.eq("zdbID", recordID));
        query.addOrder(Order.desc("dateUpdated"));
        List<AuditLogItem> list = query.list();
        return list;
    }
}
