package org.zfin.sequence.repository;

import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.framework.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 */
public class HibernateDisplayGroupRepository implements DisplayGroupRepository{


    // for display groups
    public DisplayGroup getDisplayGroupByName(DisplayGroup.GroupName groupName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DisplayGroup.class) ;
        criteria.add(Restrictions.eq("groupName",groupName));
        return (DisplayGroup) criteria.uniqueResult() ;
    }

    public List<ReferenceDatabase> getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName... groupNames) {
        Session session = HibernateUtil.currentSession();
        String hql = "" +
                "select rd from ReferenceDatabase rd join rd.displayGroups dg  " +
                "where dg.groupName in (:groupNames) " +
                "order by rd.foreignDB.dbName , rd.foreignDBDataType.dataType " ;
        Query query = session.createQuery(hql) ;
        query.setParameterList("groupNames", groupNames) ;
        return (List< ReferenceDatabase> ) query.list() ;
    }

}
