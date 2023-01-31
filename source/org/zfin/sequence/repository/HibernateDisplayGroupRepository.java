package org.zfin.sequence.repository;

import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class HibernateDisplayGroupRepository implements DisplayGroupRepository {


    // for display groups

    public DisplayGroup getDisplayGroupByName(DisplayGroup.GroupName groupName) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<DisplayGroup> cr = cb.createQuery(DisplayGroup.class);
        Root<DisplayGroup> root = cr.from(DisplayGroup.class);
        cr.select(root).where(cb.equal(root.get("groupName"), groupName));
        return session.createQuery(cr).uniqueResult();
    }

    public List<ReferenceDatabase> getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName... groupNames) {
        Session session = HibernateUtil.currentSession();
        String hql = """
                select rd from ReferenceDatabase rd join rd.displayGroups dg  
                where dg.groupName in (:groupNames) 
                order by rd.foreignDB.dbName , rd.foreignDBDataType.dataType """;
        Query<ReferenceDatabase> query = session.createQuery(hql, ReferenceDatabase.class);
        query.setParameterList("groupNames", groupNames);
        return query.list();
    }

}
