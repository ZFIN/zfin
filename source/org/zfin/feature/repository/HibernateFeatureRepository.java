package org.zfin.feature.repository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.DataAlias;
import org.zfin.mutant.Feature;

import static org.zfin.framework.HibernateUtil.currentSession;


/**
 * Hibernate implementation of the Antibody Repository.
 */
public class HibernateFeatureRepository implements FeatureRepository {


    public Feature getFeatureByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Feature) session.get(Feature.class, zdbID);
    }

    public DataAlias getSpecificDataAlias(Feature feature, String alias) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(DataAlias.class);
        criteria.add(Restrictions.eq("feature", feature));
        criteria.add(Restrictions.eq("alias", alias));
        return (DataAlias) criteria.uniqueResult();
    }

}

