package org.zfin.feature.repository;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.DataAlias;
import org.zfin.mutant.Feature;

import java.util.List;

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

    /**
     * Retrieve a list of all feature for a given publication.
     * Features need to be directly attributed to the publication in question.
     * @param publicationID publication
     * @return list of features
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByPublication(String publicationID) {
        Session session = currentSession();
        String hql = "select feature from Feature as feature, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = feature.zdbID AND " +
                "      attribution.publication.zdbID = :pubID " +
                "      order by feature.abbreviationOrder";

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationID);
        return (List<Feature>) query.list();
    }

}

