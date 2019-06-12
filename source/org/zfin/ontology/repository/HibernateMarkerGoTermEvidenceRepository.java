package org.zfin.ontology.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.zfin.datatransfer.go.FpInferenceGafParser;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 */
public class HibernateMarkerGoTermEvidenceRepository implements MarkerGoTermEvidenceRepository {

    protected Logger logger = LogManager.getLogger(HibernateMarkerGoTermEvidenceRepository.class);

    @Override
    @SuppressWarnings("unchecked")
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerAbbreviation(String abbreviation) {
        String hql = "from MarkerGoTermEvidence ev where ev.marker.abbreviation like :abbreviation " +
                " order by zdbID ";

        return (List<MarkerGoTermEvidence>) HibernateUtil.currentSession().createQuery(hql)
                .setString("abbreviation", abbreviation)
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
        return (List<MarkerGoTermEvidence>)
                HibernateUtil.currentSession().createQuery(" " +
                        " from MarkerGoTermEvidence ev" +
                        " left join fetch ev.inferredFrom " +
                        " join fetch ev.marker " +
                        " where ev.source = :pubZdbID " +
                        " order by ev.marker.abbreviation , ev.goTerm.termName " +
                        "")
                        .setString("pubZdbID", publicationID)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
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

    @Override
    @SuppressWarnings("unchecked")
    public GoEvidenceCode getGoEvidenceCode(String evidenceCode) {
        return (GoEvidenceCode) HibernateUtil.currentSession().createCriteria(GoEvidenceCode.class).add(Restrictions.eq("code", evidenceCode)).uniqueResult();
    }

    @Override
    public MarkerGoTermEvidence getNdExistsForGoGeneEvidenceCode(MarkerGoTermEvidence markerGoTermEvidenceToAdd) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class);
        criteria.add(Restrictions.eq("marker.zdbID", markerGoTermEvidenceToAdd.getMarker().getZdbID()));
        criteria.add(Restrictions.eq("goTerm.zdbID", markerGoTermEvidenceToAdd.getGoTerm().getZdbID()));
        criteria.add(Restrictions.eq("evidenceCode.code", GoEvidenceCodeEnum.ND.name()));
        return (MarkerGoTermEvidence) criteria.uniqueResult();
    }

    @Override
    public GafOrganization getGafOrganization(GafOrganization.OrganizationEnum organizationEnum) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(GafOrganization.class);
        criteria.add(Restrictions.eq("organization", organizationEnum.toString()));
        return (GafOrganization) criteria.uniqueResult();
    }

    @Override
    public String isValidMarkerGoTerm(MarkerGoTermEvidence markerGoTermEvidenceToAdd) {
        if (markerGoTermEvidenceToAdd.getGoTerm().isRoot()) {
            String hql = "select count(*) from MarkerGoTermEvidence where " +
                    "marker = :marker AND goTerm != :goTerm AND goTerm.ontology = :ontology";
            Query query = HibernateUtil.currentSession().createQuery(hql);
            query.setParameter("marker", markerGoTermEvidenceToAdd.getMarker());
            query.setParameter("goTerm", markerGoTermEvidenceToAdd.getGoTerm());
            query.setParameter("ontology", markerGoTermEvidenceToAdd.getGoTerm().getOntology());
            Long count = (Long) query.uniqueResult();
            if (count > 0)
                return "Cannot add root-term annotation " + markerGoTermEvidenceToAdd.getGoTerm().getOboID() +
                        " for marker '" + markerGoTermEvidenceToAdd.getMarker().getAbbreviation() +
                        "' as there are already non-root term annotations existent.";
        }
        return null;
    }

    @Override
    public void addEvidence(MarkerGoTermEvidence markerGoTermEvidenceToAdd) {

        // has this marker already a non-root term annotation?
        if (markerGoTermEvidenceToAdd.getGoTerm().isRoot()) {
            String hql = "select count(*) from MarkerGoTermEvidence where " +
                    "marker = :marker AND goTerm != :goTerm AND goTerm.ontology = :ontology";
            Query query = HibernateUtil.currentSession().createQuery(hql);
            query.setParameter("marker", markerGoTermEvidenceToAdd.getMarker());
            query.setParameter("goTerm", markerGoTermEvidenceToAdd.getGoTerm());
            query.setParameter("ontology", markerGoTermEvidenceToAdd.getGoTerm().getOntology());
            Long count = (Long) query.uniqueResult();
            if (count > 0)
                logger.info("Cannot add root-term annotation " + markerGoTermEvidenceToAdd.getGoTerm().getOboID() +
                        " for marker '" + markerGoTermEvidenceToAdd.getMarker().getAbbreviation() +
                        "' as there are already non-root term annotations existent.");
            return;
        }

        HibernateUtil.currentSession().save(markerGoTermEvidenceToAdd);
        // have to do this after we add inferences
        if (CollectionUtils.isNotEmpty(markerGoTermEvidenceToAdd.getInferredFrom())) {
            for (InferenceGroupMember inference : markerGoTermEvidenceToAdd.getInferredFrom()) {
                inference.setMarkerGoTermEvidenceZdbID(markerGoTermEvidenceToAdd.getZdbID());
                HibernateUtil.currentSession().save(inference);
            }
        }

        if (CollectionUtils.isNotEmpty(markerGoTermEvidenceToAdd.getGoTermAnnotationExtnGroup())) {
            for (MarkerGoTermAnnotationExtnGroup mgtaeGroup : markerGoTermEvidenceToAdd.getGoTermAnnotationExtnGroup()) {
                mgtaeGroup.setMgtaegMarkerGoEvidence(markerGoTermEvidenceToAdd);
                HibernateUtil.currentSession().save(mgtaeGroup);
                for (MarkerGoTermAnnotationExtn mgtaedata : mgtaeGroup.getMgtAnnoExtns()) {
                    if (mgtaedata.getAnnotExtnGroupID().getId() != null) {
                        HibernateUtil.currentSession().save(mgtaedata);
                    }
                }
            }
        }

    }


    @Override
    public void removeEvidence(MarkerGoTermEvidence markerGoTermEvidenceToRemove) {
        HibernateUtil.currentSession().delete(markerGoTermEvidenceToRemove);
    }

    @Override
    public void updateEvidence(MarkerGoTermEvidence markerGoTermEvidence) {
        HibernateUtil.currentSession().update(markerGoTermEvidence);

        if (CollectionUtils.isNotEmpty(markerGoTermEvidence.getGoTermAnnotationExtnGroup())) {
            for (MarkerGoTermAnnotationExtnGroup mgtaeGroup : markerGoTermEvidence.getGoTermAnnotationExtnGroup()) {
                mgtaeGroup.setMgtaegMarkerGoEvidence(markerGoTermEvidence);
                HibernateUtil.currentSession().save(mgtaeGroup);
                for (MarkerGoTermAnnotationExtn mgtaedata : mgtaeGroup.getMgtAnnoExtns()) {

                    if (mgtaedata.getAnnotExtnGroupID().getId() != null) {
                        HibernateUtil.currentSession().save(mgtaedata);
                    }
                }
            }
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<String> getEvidencesForGafOrganization(GafOrganization gafOrganization) {
        String hql = " select ev.zdbID from MarkerGoTermEvidence ev where ev.gafOrganization = :gaf ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setParameter("gaf", gafOrganization)
                .list();
    }


    /**
     * Get marker go term evidences that have duplicate go, marker, pub, evdicencecode, flag, and inferences
     *
     * @param markerGoTermEvidenceToAdd
     * @return Returns list of almost matching.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<MarkerGoTermEvidence> getLikeMarkerGoTermEvidencesButGo(MarkerGoTermEvidence markerGoTermEvidenceToAdd) {
        String hql = " select ev from MarkerGoTermEvidence ev " +
                "  where  " +
                "  ev.source = :pub " +
                "  and " +
                "  ev.marker = :marker " +
                "  and " +
                "  ev.evidenceCode = :evidenceCode " +
                " ";

        if (markerGoTermEvidenceToAdd.getFlag() != null) {
            hql += "  and ev.flag = :flag ";
        } else {
            hql += " and ev.flag is null ";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("pub", markerGoTermEvidenceToAdd.getSource())
                .setParameter("marker", markerGoTermEvidenceToAdd.getMarker())
                .setParameter("evidenceCode", markerGoTermEvidenceToAdd.getEvidenceCode());

        if (markerGoTermEvidenceToAdd.getFlag() != null) {
            query.setString("flag", markerGoTermEvidenceToAdd.getFlag().toString());
        }

        return (List<MarkerGoTermEvidence>) query.list();
    }

    @Override
    public int deleteMarkerGoTermEvidenceByZdbIDs(List<String> zdbIDs) {

        String hql = " delete from MarkerGoTermEvidence ev where ev.zdbID in (:zdbIDs) ";

        int deleted = HibernateUtil.currentSession().createQuery(hql)
                .setParameterList("zdbIDs", zdbIDs)
                .executeUpdate();

        return deleted;
    }

    @Override
    public int getEvidenceForMarkerCount(Marker m) {
        String sql = " select count(distinct mrkrgoev_term_zdb_id) " +
                "          from marker_go_term_evidence " +
                "         where mrkrgoev_mrkr_zdb_id = :markerZdbId   ";
        return Integer.parseInt(HibernateUtil.currentSession()
                .createSQLQuery(sql)
                .setString("markerZdbId", m.getZdbID())
                .uniqueResult().toString());
    }

    /**
     * select first 1 mrkrgoev_zdb_id, term_name, mrkrgoev_gflag_name
     * from term,go_evidence_code, marker_go_term_evidence
     * where mrkrgoev_mrkr_zdb_id = '$OID'
     * and mrkrgoev_term_zdb_id = term_zdb_id
     * and mrkrgoev_evidence_code = goev_code
     * and term_ontology = '$ontology'
     * order by goev_display_order, term_name
     *
     * @param m
     * @return
     */
    @Override
    public MarkerGoTermEvidence getFirstEvidenceForMarkerOntology(Marker m, Ontology ontology) {
        String hql = " select ev from MarkerGoTermEvidence ev " +
                " where ev.marker = :marker " +
                " and ev.goTerm.ontology = :ontology" +
                " order by ev.evidenceCode.order, ev.goTerm.termName " +
                " ";

        return (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery(hql)
                .setParameter("marker", m)
                .setParameter("ontology", ontology)
                .setMaxResults(1)
                .uniqueResult();

    }

    public SortedSet<GenericTerm> getGOtermsInferedFromZDBid(String zdbID) {
        if (zdbID == null)
            return null;
        String inferredFromSTR = "ZFIN:" + zdbID;
        String hql = " select infGrpMem from InferenceGroupMember infGrpMem " +
                "  where infGrpMem.inferredFrom = :inferredFrom ";

        List<InferenceGroupMember> inferenceGroupMembers = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("inferredFrom", inferredFromSTR)
                .list();

        SortedSet<GenericTerm> genericTerms = new TreeSet<GenericTerm>();
        for (InferenceGroupMember inferenceGroupMember : inferenceGroupMembers) {
            String markerGoTermEvidenceZdbID = inferenceGroupMember.getMarkerGoTermEvidenceZdbID();
            MarkerGoTermEvidence markerGoTermEvidence = getMarkerGoTermEvidenceByZdbID(markerGoTermEvidenceZdbID);
            GenericTerm genericTerm = markerGoTermEvidence.getGoTerm();
            genericTerms.add(genericTerm);
        }

        return genericTerms;
    }

    @Override
    public NoctuaModel getNoctuaModel(String modelID) {
        return (NoctuaModel) HibernateUtil.currentSession().get(NoctuaModel.class, modelID);
    }

    @Override
    public void saveNoctualModel(NoctuaModel noctuaModel) {
        HibernateUtil.currentSession().save(noctuaModel);
    }
}
