package org.zfin.sequence.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;

@Repository
public class HibernateDisplayGroupRepository implements DisplayGroupRepository {


    // for display groups
    public DisplayGroup getDisplayGroupByName(DisplayGroup.GroupName groupName) {
        Session session = HibernateUtil.currentSession();
        Query<DisplayGroup> criteria = session.createQuery("from DisplayGroup where groupName = :groupName", DisplayGroup.class);
        criteria.setParameter("groupName", groupName);
        return criteria.uniqueResult();
    }

    public List<ReferenceDatabase> getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName... groupNames) {
        Session session = HibernateUtil.currentSession();

        String hql =
            "select rd from ReferenceDatabase rd " +
            "join rd.displayGroupMembers dgs " +
            "join dgs.displayGroup dg " +
            "where dg.groupName in (:groupNames) " +
            "order by rd.foreignDB.dbName, rd.foreignDBDataType.dataType";
        Query query = session.createQuery(hql, ReferenceDatabase.class);
        query.setParameterList("groupNames", groupNames);
        return query.list();
    }

}
