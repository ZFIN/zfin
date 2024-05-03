package org.zfin.ontology.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMarkerGoTermEvidenceRepository;

/**
 *
 */
public class HibernateMarkerGoTermEvidenceRepository implements MarkerGoTermEvidenceRepository {

    protected Logger logger = LogManager.getLogger(HibernateMarkerGoTermEvidenceRepository.class);

    @Override
    @SuppressWarnings("unchecked")
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerAbbreviation(String abbreviation) {
        String hql = "from MarkerGoTermEvidence ev where ev.marker.abbreviation like :abbreviation " +
                     " order by zdbID ";

        return (List<MarkerGoTermEvidence>) HibernateUtil.currentSession().createQuery(hql)
            .setParameter("abbreviation", abbreviation)
            .list();
    }

    @Override
    public MarkerGoTermEvidence getMarkerGoTermEvidenceByZdbID(String zdbID) {
        Query<MarkerGoTermEvidence> criteria = HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence where zdbID = :zdbID", MarkerGoTermEvidence.class);
        criteria.setParameter("zdbID", zdbID);
        criteria.setMaxResults(1);
        return criteria.uniqueResult();
    }

    @Override
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbID(String zdbID) {
        Query<MarkerGoTermEvidence> query = HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence where marker.zdbID = :zdbID", MarkerGoTermEvidence.class);
        query.setParameter("zdbID", zdbID);
        return query.list();
    }

    @Override
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForPubZdbID(String publicationID) {
        List<MarkerGoTermEvidence> resultList = HibernateUtil.currentSession().createQuery("""
                        select distinct ev
                        from MarkerGoTermEvidence ev
                        left join fetch ev.inferredFrom 
                        join fetch ev.marker 
                        where ev.source.zdbID = :pubZdbID 
                        order by ev.marker.abbreviation , ev.goTerm.termName 
                        """, MarkerGoTermEvidence.class)
                .setParameter("pubZdbID", publicationID)
                .list();
        // Use LinkedHashSet to distinctify and preserve order
        Set<MarkerGoTermEvidence> distinctResults = new LinkedHashSet<>(resultList);
        return new ArrayList<>(distinctResults);
    }

    @Override
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForPubZdbIDs(List<String> publicationIDs) {
        return publicationIDs
            .stream()
            .map(this::getMarkerGoTermEvidencesForPubZdbID)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    @Override
    public List<MarkerGoTermEvidence> getMarkerGoTermEvidencesForMarkerZdbIDOrdered(String markerID) {
        return HibernateUtil.currentSession().createQuery(" " +
                                                          " from MarkerGoTermEvidence ev where ev.marker.zdbID = :markerZdbID " +
                                                          " order by ev.goTerm.ontology , marker.abbreviation , ev.goTerm.termName " +
                                                          "", MarkerGoTermEvidence.class)
            .setParameter("markerZdbID", markerID)
            .list();
    }

    @Override
    public GoEvidenceCode getGoEvidenceCode(String evidenceCode) {
        return (GoEvidenceCode) HibernateUtil.currentSession().createQuery("from GoEvidenceCode where code = :code", GoEvidenceCode.class)
            .setParameter("code", evidenceCode).uniqueResult();
    }

    @Override
    public MarkerGoTermEvidence getNdExistsForGoGeneEvidenceCode(MarkerGoTermEvidence markerGoTermEvidenceToAdd) {
        String hql = """
            from MarkerGoTermEvidence
            where marker.zdbID = :markerID
            AND goTerm.zdbID = :termID
            AND evidenceCode.code = :code
                        """;
        Query<MarkerGoTermEvidence> criteria = HibernateUtil.currentSession().createQuery(hql, MarkerGoTermEvidence.class);
        criteria.setParameter("markerID", markerGoTermEvidenceToAdd.getMarker().getZdbID());
        criteria.setParameter("termID", markerGoTermEvidenceToAdd.getGoTerm().getZdbID());
        criteria.setParameter("code", GoEvidenceCodeEnum.ND.name());
        final MarkerGoTermEvidence markerGoTermEvidence = criteria.uniqueResult();
        if (markerGoTermEvidence == null)
            return null;
        // check if extensions are the same
        if (markerGoTermEvidenceToAdd.getAnnotationExtensions().equals(markerGoTermEvidence.getAnnotationExtensions()))
            return markerGoTermEvidence;
        return null;
    }

    @Override
    public GafOrganization getGafOrganization(GafOrganization.OrganizationEnum organizationEnum) {
        Query<GafOrganization> query = HibernateUtil.currentSession().createQuery("from GafOrganization where organization = :organization", GafOrganization.class);
        query.setParameter("organization", organizationEnum.toString());
        return query.uniqueResult();
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
    public void addEvidence(MarkerGoTermEvidence markerGoTermEvidenceToAdd, boolean isInternalLoad) {

        // has this marker already a non-root term annotation for external loads
        if (markerGoTermEvidenceToAdd.getGoTerm().isRoot() && !isInternalLoad) {
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

        if (markerGoTermEvidenceToAdd.getNoctuaModelId() != null) {
            NoctuaModel noctuaModel = getMarkerGoTermEvidenceRepository().getNoctuaModel(markerGoTermEvidenceToAdd.getNoctuaModelId());
            if (noctuaModel == null) {
                noctuaModel = markerGoTermEvidenceToAdd.getNoctuaModels().iterator().next();
                getMarkerGoTermEvidenceRepository().saveNoctualModel(noctuaModel);
            }
            markerGoTermEvidenceToAdd.setNoctuaModels(Set.of(noctuaModel));
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
        if (markerGoTermEvidenceToAdd.getQualifierRelation() != null) {
            hql += "  and ev.qualifierRelation = :qualifierRelation ";
        } else {
            hql += " and ev.qualifierRelation is null ";
        }
        Query query = HibernateUtil.currentSession().createQuery(hql)
            .setParameter("pub", markerGoTermEvidenceToAdd.getSource())
            .setParameter("marker", markerGoTermEvidenceToAdd.getMarker())
            .setParameter("evidenceCode", markerGoTermEvidenceToAdd.getEvidenceCode());

        if (markerGoTermEvidenceToAdd.getFlag() != null) {
            query.setParameter("flag", markerGoTermEvidenceToAdd.getFlag().toString());
        }
        if (markerGoTermEvidenceToAdd.getQualifierRelation() != null) {
            query.setParameter("qualifierRelation", markerGoTermEvidenceToAdd.getQualifierRelation());

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
            .createNativeQuery(sql)
            .setParameter("markerZdbId", m.getZdbID())
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
