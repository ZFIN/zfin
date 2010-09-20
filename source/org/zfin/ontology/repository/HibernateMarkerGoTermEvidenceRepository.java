package org.zfin.ontology.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.MarkerGoTermEvidence;

import java.util.List;

/**
 */
public class HibernateMarkerGoTermEvidenceRepository implements MarkerGoTermEvidenceRepository{


    @Override
    @SuppressWarnings("unchecked")
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerAbbreviation(String abbreviation) {
        String hql = "from MarkerGoTermEvidence ev where ev.marker.abbreviation like :abbreviation " +
                " order by zdbID ";

        return (List<MarkerGoTermEvidence>) HibernateUtil.currentSession().createQuery(hql)
                .setString("abbreviation",abbreviation)
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public MarkerGoTermEvidence getMarkerGoTermEvidenceByZdbID(String zdbID) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        criteria.setMaxResults(1);
        return (MarkerGoTermEvidence) criteria.uniqueResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbID(String zdbID) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class);
        criteria.add(Restrictions.eq("marker.zdbID", zdbID));
        return (List<MarkerGoTermEvidence>) criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForPubZdbID(String publicationID) {
        return  (List<MarkerGoTermEvidence>)
                HibernateUtil.currentSession().createQuery(" " +
                        " from MarkerGoTermEvidence ev where ev.source = :pubZdbID " +
                        " order by ev.marker.abbreviation , ev.goTerm.termName " +
                        "")
                        .setString("pubZdbID", publicationID)
                        .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbIDOrdered(String markerID) {
        return (List<MarkerGoTermEvidence>)
                HibernateUtil.currentSession().createQuery(" " +
                        " from MarkerGoTermEvidence ev where ev.marker.zdbID = :markerZdbID " +
                        " order by ev.goTerm.ontology , marker.abbreviation , ev.goTerm.termName " +
                        "")
                        .setString("markerZdbID", markerID)
                        .list();
    }
}
