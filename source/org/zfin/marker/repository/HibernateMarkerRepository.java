package org.zfin.marker.repository;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.presentation.ConstructComponentPresentation;
import org.zfin.database.HibernateUpgradeHelper;
import org.zfin.expression.*;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.Location;
import org.zfin.mapping.MarkerLocation;
import org.zfin.marker.*;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerRelationshipPresentationTransformer;
import org.zfin.marker.service.MarkerRelationshipSupplierPresentationTransformer;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.orthology.Ortholog;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Organization;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.service.TranscriptService;
import org.zfin.util.NumberAwareStringComparator;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.hibernate.criterion.CriteriaSpecification.DISTINCT_ROOT_ENTITY;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.marker.MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT;
import static org.zfin.repository.RepositoryFactory.*;


@Log4j2
@Repository
public class HibernateMarkerRepository implements MarkerRepository {

    private final static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private final static PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    // utilities
    private final MarkerDBLinksTransformer markerDBLinkTransformer = new MarkerDBLinksTransformer();

    public Marker getMarker(Marker marker) {
        return getMarker(marker.getZdbID());
    }

    @Override
    public Marker getMarker(String id) {
        Marker marker = getMarkerByID(id);
        if (marker == null) {
            String replacedID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(id);
            if (replacedID != null) {
                marker = getMarkerByID(replacedID);
                log.debug("found a replaced zdbID for: " + id + "->" + replacedID);
            }
        }
        return marker;
    }

    public Marker getMarkerByID(String zdbID) {
        Session session = currentSession();
        return session.get(Marker.class, zdbID);
    }

    @Override
    public List<Marker> getMarkersByZdbIDs(List<String> zdbIDs) {
        String hql = "select m from Marker m where m.zdbID in (:IDs) ";
        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameterList("IDs", zdbIDs);
        return query.list();
    }

    @Override
    public List<Marker> getMarkersByZdbIDsJoiningAliases(List<String> zdbIDs) {
        String hql = "select m from Marker m join fetch m.aliases where m.zdbID in (:IDs) ";
        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameterList("IDs", zdbIDs);
        return query.list();
    }

    public SNP getSNPByID(String zdbID) {
        Session session = currentSession();
        return session.get(SNP.class, zdbID);
    }

    public ConstructCuration getConstructByID(String zdbID) {
        Session session = currentSession();

        return session.get(ConstructCuration.class, zdbID);
    }

    public AllianceGeneDesc getGeneDescByMkr(Marker marker) {
        Session session = currentSession();

        String hql = """
            select  agd from AllianceGeneDesc agd
            where agd.gene= :geneID
            """;

        Query<AllianceGeneDesc> query = session.createQuery(hql, AllianceGeneDesc.class);
        query.setParameter("geneID", marker.getZdbID());
        return query.uniqueResult();
    }

    @Override
    public Marker getMarkerOrReplacedByID(String zdbID) {
        Marker marker = getMarkerByID(zdbID);
        if (marker != null) {
            return marker;
        }
        String replacedZdbID = infrastructureRepository.getReplacedZdbID(zdbID);
        marker = getMarkerByID(replacedZdbID);
        return marker;
    }

    public Marker getGeneByID(String zdbID) {
        if (!zdbID.startsWith("ZDB-GENE")) {
            if (!zdbID.contains("RNAG")) {
                return null;
            }
        }
        return HibernateUtil.currentSession().get(Marker.class, zdbID);
    }

    public Clone getCloneById(String zdbID) {
        Session session = currentSession();
        return session.get(Clone.class, zdbID);
    }

    public Transcript getTranscriptByZdbID(String zdbID) {
        return currentSession().get(Transcript.class, zdbID);
    }

    public Transcript getTranscriptByName(String name) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Transcript> cr = cb.createQuery(Transcript.class);
        Root<Transcript> root = cr.from(Transcript.class);
        cr.select(root).where(cb.equal(root.get("name"), name));
        return session.createQuery(cr).uniqueResult();
    }

    public Transcript getTranscriptByVegaID(String vegaID) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TranscriptDBLink> cr = cb.createQuery(TranscriptDBLink.class);
        Root<TranscriptDBLink> root = cr.from(TranscriptDBLink.class);
        cr.select(root).where(cb.equal(root.get("accessionNumber"), vegaID));
        return getTranscriptByZdbID(session.createQuery(cr).setMaxResults(1).uniqueResult().getTranscript().getZdbID());
    }

    public List<ConstructComponent> getConstructComponent(String constructID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                    select cc from ConstructComponent cc       
                    where cc.constructZdbID = :pubID    
                    order by cc.componentOrder 
                    """;

        Query<ConstructComponent> query = session.createQuery(hql, ConstructComponent.class);
        query.setParameter("pubID", constructID);

        return query.list();

    }

    @Override
    public void deleteConstructComponents(String constructZdbID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                    delete from ConstructComponent cc       
                    where cc.constructZdbID = :constructID    
                    """;

        Query query = session.createQuery(hql);
        query.setParameter("constructID", constructZdbID);

        query.executeUpdate();

    }

    public List<ProteinToPDB> getPDB(String uniProtID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select  ptp from ProteinToPDB ptp where ptp.uniProtID = :uniProtID ";

        Query<ProteinToPDB> query = session.createQuery(hql, ProteinToPDB.class);
        query.setParameter("uniProtID", uniProtID);

        return query.list();

    }


    public List<String> getTranscriptTypes() {
        Session session = currentSession();
        String hql = "select t.transcriptType from Transcript t group by t.transcriptType ";
        Query<String> query = session.createQuery(hql, String.class);
        return query.list();
    }

    public Marker getMarkerByAbbreviationIgnoreCase(String abbreviation) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Marker> cr = cb.createQuery(Marker.class);
        Root<Marker> root = cr.from(Marker.class);
        cr.where(cb.equal(cb.upper(root.get("abbreviation")), abbreviation.toUpperCase()));
        return session.createQuery(cr).uniqueResult();
    }

    public Marker getMarkerByAbbreviation(String abbreviation) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Marker> cr = cb.createQuery(Marker.class);
        Root<Marker> root = cr.from(Marker.class);
        cr.where(cb.equal(root.get("abbreviation"), abbreviation));
        return session.createQuery(cr).uniqueResult();
    }

    public SequenceTargetingReagent getSequenceTargetingReagentByAbbreviation(String abbreviation) {
        Session session = currentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<SequenceTargetingReagent> cr = cb.createQuery(SequenceTargetingReagent.class);
        Root<SequenceTargetingReagent> root = cr.from(SequenceTargetingReagent.class);
        cr.where(cb.equal(root.get("abbreviation"), abbreviation));
        return session.createQuery(cr).uniqueResult();
    }

    public Marker getMarkerByName(String name) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Marker> cr = cb.createQuery(Marker.class);
        Root<Marker> root = cr.from(Marker.class);
        cr.where(cb.equal(root.get("name"), name));
        return session.createQuery(cr).uniqueResult();
    }

    public List<Marker> getMarkersByAbbreviation(String name) {
        List<Marker> markerList = currentSession().createQuery("from Marker where upper(abbreviation) like :name || '%' order by abbreviationOrder asc ", Marker.class).setParameter("name", name.toUpperCase()).list();


        List<Marker> markerListContains = currentSession().createQuery("from Marker where upper(abbreviation) like  '%' || :name || '%' order by abbreviationOrder asc ", Marker.class).setParameter("name", name.toUpperCase()).list();
        markerListContains.forEach(familyName -> {
            if (!markerList.contains(familyName)) markerList.add(familyName);
        });
        return markerList;
    }

    public List<Marker> getGenesByAbbreviation(String name) {
        List<Marker> markerList =
            currentSession().createQuery("from Marker where upper(abbreviation) like :name || '%' " +
                                         " AND zdbID like 'ZDB-GENE%' order by abbreviationOrder asc ", Marker.class)
                .setParameter("name", name.toUpperCase())
                .list();


        List<Marker> markerListContains =
            currentSession().createQuery("from Marker where upper(abbreviation) like  '%' || :name || '%' " +
                                         " AND zdbID like 'ZDB-GENE%' order by abbreviationOrder asc ", Marker.class)
                .setParameter("name", name.toUpperCase())
                .list();
        markerListContains.forEach(familyName -> {
            if (!markerList.contains(familyName)) markerList.add(familyName);
        });
        return markerList;
    }

    // ToDo: Replace with existing method
    public Marker getGeneByAbbreviation(String name) {
        Session session = currentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Marker> cr = cb.createQuery(Marker.class);
        Root<Marker> root = cr.from(Marker.class);
        Predicate[] predicates = new Predicate[2];
        predicates[0] = cb.like(root.get("zdbID"), "ZDB-GENE-%");
        predicates[1] = cb.equal(root.get("abbreviation"), name);
        cr.select(root).where(predicates);

        return session.createQuery(cr).uniqueResult();
    }


    public MarkerRelationship getMarkerRelationship(Marker marker1, Marker marker2, MarkerRelationship.Type type) {
        Session session = currentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MarkerRelationship> cr = cb.createQuery(MarkerRelationship.class);
        Root<MarkerRelationship> root = cr.from(MarkerRelationship.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("firstMarker"), marker1));
        predicates.add(cb.equal(root.get("secondMarker"), marker2));
        if (type != null) predicates.add(cb.equal(root.get("type"), type));
        cr.select(root).where(predicates.toArray(Predicate[]::new));
        return session.createQuery(cr).uniqueResult();
    }

    @Override
    public MarkerRelationship getMarkerRelationship(Marker marker1, Marker marker2) {
        return getMarkerRelationship(marker1, marker2, null);
    }

    public List<Marker> getMarkersForRelation(String featureRelationshipName, String publicationZdbID) {
        String sql = """
            SELECT DISTINCT mrkr_zdb_id FROM marker,marker_type_group_member, record_attribution
            WHERE mrkr_zdb_id = recattrib_data_zdb_id
            AND recattrib_source_zdb_id=:pubZdbID
            AND mrkr_type=mtgrpmem_mrkr_type AND mtgrpmem_mrkr_type_group='CONSTRUCT_COMPONENTS'
            """;


        List<String> markerZdbIds = (List<String>) HibernateUtil.currentSession().createSQLQuery(sql).setParameter("pubZdbID", publicationZdbID)

            .list();
        List<Marker> markers = new ArrayList<>();
        for (String zdbId : markerZdbIds) {
            Marker m = HibernateUtil.currentSession().get(Marker.class, zdbId);
            markers.add(m);
        }
        return markers;
    }


    public MarkerAlias getSpecificDataAlias(Marker marker, String alias) {
        Session session = currentSession();
        String hql = "from MarkerAlias ma where ma.marker = :marker and ma.alias = :alias ";
        Query<MarkerAlias> query = session.createQuery(hql, MarkerAlias.class);
        query.setParameter("marker", marker);
        query.setParameter("alias", alias);
        List<MarkerAlias> list = query.list();
        if (CollectionUtils.isEmpty(list)) return null;
        return list.get(0);
    }


    public MarkerRelationship getMarkerRelationshipByID(String zdbID) {
        Session session = currentSession();
        return session.get(MarkerRelationship.class, zdbID);
    }


    public List<MarkerRelationship> getMarkerRelationshipsByPublication(String publicationZdbID) {
        List<MarkerRelationship.Type> markerRelationshipList = new ArrayList<>();
        markerRelationshipList.add(MarkerRelationship.Type.PROMOTER_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CONTAINS_REGION);

        Session session = currentSession();
        String hql = """
            select distinct mr from MarkerRelationship as mr, PublicationAttribution as attribution
            where  attribution.dataZdbID = mr.zdbID
            AND mr.type in (:markerRelationshipType)
            AND attribution.publication.zdbID = :pubID
            """;

        Query<MarkerRelationship> query = session.createQuery(hql, MarkerRelationship.class);
        query.setParameter("pubID", publicationZdbID);
        query.setParameterList("markerRelationshipType", markerRelationshipList);
        return query.list();
    }

    public List<Transcript> getTranscriptsForNonCodingGenes() {

        List<MarkerType> markerTypes = getMarkerTypesByGroup(Marker.TypeGroup.RNAGENE);

        String hql = """
            select t from MarkerRelationship mr1,  Marker m, Transcript t
                       where mr1.firstMarker.zdbID = m.zdbID
                       and mr1.secondMarker.zdbID = t.zdbID
                       and mr1.firstMarker.markerType in (:markerType)
                       and mr1.type = :markerRelationshipType1
                       """;


        return currentSession().createQuery(hql, Transcript.class).setParameter("markerRelationshipType1", GENE_PRODUCES_TRANSCRIPT).setParameterList("markerType", markerTypes).list();
    }

    public List<Transcript> getAllNonCodingTranscripts() {
        List<TranscriptType.Type> typeList = new ArrayList<>();
        typeList.add(TranscriptType.Type.ABERRANT_PROCESSED_TRANSCRIPT);
        typeList.add(TranscriptType.Type.PSEUDOGENIC_TRANSCRIPT);
        typeList.add(TranscriptType.Type.ANTISENSE);
        typeList.add(TranscriptType.Type.NCRNA);
        typeList.add(TranscriptType.Type.SNORNA);
        typeList.add(TranscriptType.Type.SNRNA);
        typeList.add(TranscriptType.Type.SCRNA);
        typeList.add(TranscriptType.Type.MIRNA);

        Session session = currentSession();
        String hql = "select distinct mr from Transcript as mr " +

                     "where mr.transcriptType.type in (:transcriptType) ";
        Query<Transcript> query = session.createQuery(hql, Transcript.class);
        query.setParameterList("transcriptType", typeList);
        return query.list();
    }

    public List<String> getMarkerRelationshipTypesForMarkerEdit(Marker marker, Boolean interacts) {

        List<String> mTypeGroup = new ArrayList<>();
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            mTypeGroup.add("GENEDOM");
            mTypeGroup.add("FEATURE");
        }
        if (marker.isInTypeGroup(Marker.TypeGroup.RNAGENE)) {
            mTypeGroup.add("GENEDOM");
            mTypeGroup.add("FEATURE");
            mTypeGroup.add("RNAGENE");
        }
        if (marker.isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)) {
            mTypeGroup.add("NONTSCRBD_REGION");
        }
        Session session = currentSession();
        if (interacts) {
            String hql = "select mr.name from MarkerRelationshipType mr where mr.firstMarkerTypeGroup.name in (:mTypeGroup) and mr.name like '%interacts%'";
            Query<String> query = session.createQuery(hql, String.class);
            query.setParameterList("mTypeGroup", mTypeGroup);
            return query.list();
        } else {
            String hql = "select mr.name from MarkerRelationshipType mr " +
                         " where mr.firstMarkerTypeGroup.name in (:mTypeGroup) and mr.name not like '%interacts%'";
            Query<String> query = session.createQuery(hql, String.class);
            query.setParameterList("mTypeGroup", mTypeGroup);
            return query.list();
        }


    }

    public TreeSet<String> getLG(Marker marker) {
        Session session = currentSession();
        String hql = "select distinct gl.chromosome from GenomeLocation gl where gl.entityID = :entityID ";
        Query<String> query = session.createQuery(hql, String.class);
        query.setParameter("entityID", marker.getZdbID());
        return new TreeSet<>(query.list());
    }


    public MarkerRelationship addMarkerRelationship(MarkerRelationship mrel, String sourceZdbID) {

        Marker marker1 = mrel.getFirstMarker();
        Marker marker2 = mrel.getSecondMarker();

        //update the two markers with the relationships
        Set<MarkerRelationship> firstMarkerRelationships = marker1.getFirstMarkerRelationships();
        if (firstMarkerRelationships == null) {
            firstMarkerRelationships = new HashSet<>();
            firstMarkerRelationships.add(mrel);
            marker1.setFirstMarkerRelationships(firstMarkerRelationships);
        } else {
            firstMarkerRelationships.add(mrel);
        }

        Set<MarkerRelationship> secondSegmentRelationships = marker2.getSecondMarkerRelationships();
        if (secondSegmentRelationships == null) {
            secondSegmentRelationships = new HashSet<>();
            secondSegmentRelationships.add(mrel);
            marker2.setSecondMarkerRelationships(secondSegmentRelationships);
        } else {
            secondSegmentRelationships.add(mrel);
        }

        currentSession().save(mrel);
        currentSession().flush();
        currentSession().refresh(mrel);

        String updateComment = "Creating relationship \"" + mrel.getFirstMarker().getAbbreviation()
                               + " " + mrel.getMarkerRelationshipType().getFirstToSecondLabel()
                               + " " + mrel.getSecondMarker().getAbbreviation()
                               + "\" with Attribution: " + sourceZdbID;
        log.debug(updateComment);
        InfrastructureService.insertUpdate(mrel.getFirstMarker(), updateComment);
        InfrastructureService.insertUpdate(mrel.getSecondMarker(), updateComment);

        //now deal with attribution
        if (sourceZdbID != null && sourceZdbID.length() > 0) {
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(sourceZdbID);
            addMarkerRelationshipAttribution(mrel, publication);
        }

        return mrel;
    }


    //if we end up with more than a couple of these, it should get
    //generalized..

    public void addSmallSegmentToGene(Marker gene, Marker segment, String sourceZdbID) {

        MarkerRelationship mrel = new MarkerRelationship();
        mrel.setType(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
        mrel.setFirstMarker(gene);
        mrel.setSecondMarker(segment);
        currentSession().save(mrel);

        //update the two markers with the relationships
        Set<MarkerRelationship> firstMarkerRelationships = gene.getFirstMarkerRelationships();
        if (firstMarkerRelationships == null) {
            firstMarkerRelationships = new HashSet<>();
            firstMarkerRelationships.add(mrel);
            gene.setFirstMarkerRelationships(firstMarkerRelationships);
        } else {
            firstMarkerRelationships.add(mrel);
        }

        Set<MarkerRelationship> secondSegmentRelationships = segment.getSecondMarkerRelationships();
        if (secondSegmentRelationships == null) {
            secondSegmentRelationships = new HashSet<>();
            secondSegmentRelationships.add(mrel);
            segment.setSecondMarkerRelationships(secondSegmentRelationships);
        } else {
            secondSegmentRelationships.add(mrel);
        }

        //now deal with attribution
        infrastructureRepository.insertRecordAttribution(mrel.getZdbID(), sourceZdbID);
    }

    public void updateMarkerPublicNote(Marker marker, String note) {
        infrastructureRepository.insertUpdatesTable(marker.getZdbID(), "public note", marker.getPublicComments(), note, "");
        marker.setPublicComments(note);
    }

    public DataNote addMarkerDataNote(Marker marker, String note) {
        Person curator = ProfileService.getCurrentSecurityUser();
        log.debug("enter addMarDataNote");
        DataNote dnote = new DataNote();
        dnote.setDataZdbID(marker.getZdbID());
        log.debug("markerZdbId for datanote: " + marker.getZdbID());
        dnote.setCurator(curator);
        dnote.setDate(new Date());
        dnote.setNote(note);
        log.debug("data note curator: " + curator);
        Set<DataNote> dataNotes = marker.getDataNotes();
        if (dataNotes == null) {
            dataNotes = new HashSet<>();
            dataNotes.add(dnote);
            marker.setDataNotes(dataNotes);
        } else {
            dataNotes.add(dnote);
        }


        HibernateUtil.currentSession().save(dnote);
        log.debug("dnote zdb_id: " + dnote.getZdbID());
        return dnote;
    }

    public AntibodyExternalNote addAntibodyExternalNote(Antibody antibody, String note, String sourceZdbID) {
        log.debug("enter addExtDataNote");
        AntibodyExternalNote externalNote = new AntibodyExternalNote();
        externalNote.setAntibody(antibody);
        externalNote.setNote(note);
        if (!sourceZdbID.equals("")) {
            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            Publication publication = pr.getPublication(sourceZdbID);
            externalNote.setPublication(publication);
            addMarkerPub(antibody, publication);
        }
        HibernateUtil.currentSession().save(externalNote);
        if (antibody.getExternalNotes() == null) {
            Set<AntibodyExternalNote> abExtNote = new HashSet<>();
            abExtNote.add(externalNote);
            antibody.setExternalNotes(abExtNote);
        } else {
            antibody.getExternalNotes().add(externalNote);
        }
        infrastructureRepository.insertUpdatesTable(antibody, "notes", "", note, "");
        return externalNote;
    }

    public OrthologyNote createOrUpdateOrthologyExternalNote(Marker gene, String note) {
        log.debug("add orthology note");
        Person currentUser = ProfileService.getCurrentSecurityUser();
        if (currentUser == null) {
            throw new RuntimeException("Cannot add an orthology note without an authenticated user");
        }

        OrthologyNote extnote;
        if (gene.getOrthologyNotes() == null || gene.getOrthologyNotes().size() == 0) {
            extnote = new OrthologyNote();
            extnote.setMarker(gene);
            extnote.setNote(note);
            extnote.setPublication(RepositoryFactory.getPublicationRepository().getPublication("ZDB-PUB-990507-16"));
            HibernateUtil.currentSession().save(extnote);
            PersonAttribution pa = new PersonAttribution();
            pa.setPerson(currentUser);
            pa.setDataZdbID(extnote.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            HibernateUtil.currentSession().save(pa);
            Set<PersonAttribution> personAttributions = new HashSet<>();
            personAttributions.add(pa);
            extnote.setPersonAttributions(personAttributions);
            Set<OrthologyNote> markerExternalNotes = new HashSet<>();
            markerExternalNotes.add(extnote);
            gene.setOrthologyNotes(markerExternalNotes);
        } else {
            extnote = gene.getOrthologyNotes().iterator().next();
            String oldNote = gene.getOrthologyNotes().iterator().next().getNote();
            extnote.setNote(note);
            infrastructureRepository.insertUpdatesTable(gene, "notes", "", note, oldNote);
        }
        return extnote;
    }

    /**
     * Create a new alias for a given marker. If no alias is found no alias is created.
     * If alias already exists do not create a new one and return null.
     *
     * @param marker      valid marker object.
     * @param alias       alias string
     * @param publication publication
     * @return The created markerAlias or null if it already exists.
     */
    public MarkerAlias addMarkerAlias(Marker marker, String alias, Publication publication) {
        //first handle the alias..
        MarkerAlias markerAlias = new MarkerAlias();
        markerAlias.setMarker(marker);
        String groupName = DataAliasGroup.Group.ALIAS.toString();
        DataAliasGroup group = infrastructureRepository.getDataAliasGroupByName(groupName);
        markerAlias.setAliasGroup(group);  //default for database, hibernate tries to insert null
        markerAlias.setAlias(alias);
        if (marker.getAliases() == null) {
            Set<MarkerAlias> markerAliases = new HashSet<>();
            markerAliases.add(markerAlias);
            marker.setAliases(markerAliases);
        } else {
            // if alias exists do not add continue...
            if (!marker.getAliases().add(markerAlias)) {
                return null;
            }
        }

        currentSession().save(markerAlias);

        //now handle the attribution
        String updateComment;
        if (publication != null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setDataZdbID(markerAlias.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubattr = new HashSet<>();
            pubattr.add(pa);

            markerAlias.setPublications(pubattr);
            currentSession().save(pa);

            if (marker.getMarkerType().getType() == Marker.Type.ATB) {
                addMarkerPub(marker, publication);
            }
            updateComment = "Added alias: '" + markerAlias.getAlias() + "' attributed to publication: '" + publication.getZdbID() + "'";
        } else {
            updateComment = "Added alias: '" + markerAlias.getAlias() + " with no attribution";
        }

        InfrastructureService.insertUpdate(marker, updateComment);
        return markerAlias;
    }

    public void deleteMarkerAlias(Marker marker, MarkerAlias alias) {
        if (marker == null) {
            throw new RuntimeException("No marker object provided.");
        }
        if (alias == null) {
            throw new RuntimeException("No alias object provided.");
        }
        // check that the alias belongs to the marker
        if (!marker.getAliases().contains(alias)) {
            throw new RuntimeException("Alias '" + alias + "' does not belong to the marker '" + marker + "'! " + "Cannot remove such an alias.");
        }
        // remove the ZDB active data record with cascade.

        String hql = "delete from MarkerHistory  mh where mh.markerAlias.zdbID = :zdbID ";
        Query query = currentSession().createQuery(hql);
        query.setParameter("zdbID", alias.getZdbID());

        currentSession().flush();

        int removed = query.executeUpdate();

        infrastructureRepository.deleteActiveDataByZdbID(alias.getZdbID());
        currentSession().flush();

        hql = "delete from MarkerAlias ma where ma.dataZdbID = :zdbID ";
        query = currentSession().createQuery(hql);
        query.setParameter("zdbID", alias.getZdbID());

        removed = query.executeUpdate();
        currentSession().flush();

        currentSession().refresh(marker);
    }

    public void deleteMarkerRelationship(MarkerRelationship mrel) {
        infrastructureRepository.deleteActiveDataByZdbID(mrel.getZdbID());
    }

    public void deleteConstructComponentByID(String constructID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from ConstructComponent cc where cc.constructZdbID=:constructID");
        query.setParameter("constructID", constructID);
        query.executeUpdate();
        currentSession().flush();
    }

    public void addDataAliasAttribution(DataAlias alias, Publication attribution, Marker marker) {
        String attributionZdbID;
        if (attribution == null) {
            throw new RuntimeException("Cannot attribute this alias with a blank pub.");
        }
        attributionZdbID = attribution.getZdbID();

        if (StringUtils.isEmpty(attributionZdbID)) {
            throw new RuntimeException("Cannot attribute this alias with a blank pub.");
        }

        String aliasZdbID = alias.getZdbID();

        RecordAttribution recordAttribution = infrastructureRepository.getRecordAttribution(aliasZdbID, attributionZdbID, RecordAttribution.SourceType.STANDARD);
        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(aliasZdbID);
            pa.setSourceType(PublicationAttribution.SourceType.STANDARD);
            pa.setPublication(attribution);
            Set<PublicationAttribution> pubAttrbs = new HashSet<>();
            pubAttrbs.add(pa);
            MarkerAlias markerAlias = new MarkerAlias();
            markerAlias.setPublications(pubAttrbs);
            currentSession().save(pa);
            currentSession().refresh(alias);
            addMarkerPub(marker, attribution);
        }
        infrastructureRepository.insertUpdatesTable(marker, "", "new attribution, data alias: " + alias.getAlias() + " with pub: " + attributionZdbID, attributionZdbID, "");
    }

    public void addGenomeLocationAttribution(Location genomeLocation, String publicationID) {
        Publication publication = getPublicationRepository().getPublication(publicationID);
        addGenomeLocationAttribution(genomeLocation, publication);
    }

    public void addGenomeLocationAttribution(Location genomeLocation, Publication attribution) {

        String attributionZdbID = attribution.getZdbID();
        String relZdbID = genomeLocation.getZdbID();

        if (attributionZdbID.equals("")) {
            throw new RuntimeException("Cannot attribute this location with a blank pub.");
        }

        RecordAttribution recordAttribution = infrastructureRepository.getRecordAttribution(relZdbID, attributionZdbID, RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(relZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(attribution);
            currentSession().save(pa);
            currentSession().refresh(genomeLocation);

            infrastructureRepository.insertUpdatesTable(genomeLocation.getZdbID(), "", "new attribution publication to genome location " + attributionZdbID + " to " + relZdbID);
        }

    }

    public void synchronizeGenomeLocationAttributions(MarkerLocation genomeLocation, Set<String> publicationIDsToSync) {
        //ADD
        for (String publicationID : publicationIDsToSync) {
            addGenomeLocationAttribution(genomeLocation, publicationID);
        }

        //DELETE
        List<RecordAttribution> existingRecordAttributions = infrastructureRepository.getRecordAttributionsForType(genomeLocation.getZdbID(), RecordAttribution.SourceType.STANDARD);
        Set<RecordAttribution> toDeleteRecordAttributions = new HashSet<>(existingRecordAttributions).stream().filter(recordAttribution -> !publicationIDsToSync.contains(recordAttribution.getSourceZdbID())).collect(Collectors.toSet());

        for (RecordAttribution reference : toDeleteRecordAttributions) {
            RepositoryFactory.getInfrastructureRepository().deleteRecordAttribution(reference.getDataZdbID(), reference.getSourceZdbID());
            genomeLocation.removeReference(reference);
        }
    }

    public void addMarkerRelationshipAttribution(MarkerRelationship mrel, Publication attribution) {

        String attributionZdbID = attribution.getZdbID();
        String relZdbID = mrel.getZdbID();

        if (attributionZdbID.equals("")) {
            throw new RuntimeException("Cannot attribute this alias with a blank pub.");
        }

        RecordAttribution recordAttribution = infrastructureRepository.getRecordAttribution(relZdbID, attributionZdbID, RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(relZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(attribution);
            currentSession().save(pa);
            currentSession().refresh(mrel);
            addMarkerPub(mrel.getFirstMarker(), attribution);
            addMarkerPub(mrel.getSecondMarker(), attribution);
        }
        infrastructureRepository.insertUpdatesTable(mrel.getFirstMarker(), "", "new attribution, marker relationship: " + mrel.getZdbID() + " with pub: " + attributionZdbID, attributionZdbID, "");
        infrastructureRepository.insertUpdatesTable(mrel.getSecondMarker(), "", "new attribution, marker relationship: " + mrel.getZdbID() + " with pub: " + attributionZdbID, attributionZdbID, "");
    }

    public MarkerLocation addMarkerLocation(MarkerLocation markerLocation) {
        Session session = HibernateUtil.currentSession();
        session.save(markerLocation);
        return markerLocation;
    }

    public MarkerLocation getMarkerLocationByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(MarkerLocation.class, zdbID);
    }

    public MarkerLocation saveMarkerLocation(MarkerLocation markerLocation) {
        Session session = HibernateUtil.currentSession();
        session.update(markerLocation);
        session.flush();
        return markerLocation;
    }

    public int deleteMarkerLocation(String zdbID) {
        String hql = "delete from MarkerLocation ml where ml.zdbID = :ID ";
        Query query = currentSession().createQuery(hql);
        query.setParameter("ID", zdbID);
        int removed = query.executeUpdate();
        currentSession().flush();
        return removed;
    }

    public void addDBLinkAttribution(DBLink dbLink, Publication attribution, String dataZdbId) {
        String linkId = dbLink.getZdbID();
        String attrId = attribution.getZdbID();

        RecordAttribution recordAttribution = infrastructureRepository.getRecordAttribution(linkId, attrId, RecordAttribution.SourceType.STANDARD);
        if (recordAttribution != null) {
            return;
        }

        infrastructureRepository.insertPublicAttribution(linkId, attrId);
        infrastructureRepository.insertUpdatesTable(dataZdbId, "", "new attribution, marker dblink: " + linkId + " with pub: " + attrId, attrId, "");
    }

    public void addDBLinkAttribution(DBLink dbLink, Publication attribution, Marker marker) {
        addDBLinkAttribution(dbLink, attribution, marker.getZdbID());
    }

    public void addMarkerPub(Marker marker, Publication publication) {
        if (publication == null) {
            throw new RuntimeException("Cannot attribute this marker with a blank pub.");
        }

        String markerZdbID = marker.getZdbID();
        RecordAttribution recordAttribution = infrastructureRepository.getRecordAttribution(markerZdbID, publication.getZdbID(), RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setDataZdbID(markerZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubAttrbs = new HashSet<>();
            pubAttrbs.add(pa);
            Marker mrkr = new Marker();
            mrkr.setPublications(pubAttrbs);
            currentSession().save(pa);
        }
    }

    public MarkerDBLink getDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb) {

        Set<MarkerDBLink> markerDBLinks = marker.getDbLinks();
        for (MarkerDBLink markerDBLink : markerDBLinks) {
            if (markerDBLink.getAccessionNumber().equals(accessionNumber) && markerDBLink.getReferenceDatabase().equals(refdb)) {
                return markerDBLink;
            }
        }

        return null;
    }

    public DBLink addDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID) {
        MarkerDBLink mdb = new MarkerDBLink();
        mdb.setMarker(marker);
        mdb.setAccessionNumber(accessionNumber);
        //mdb.setAccessionNumberDisplay(accessionNumber);
        mdb.setReferenceDatabase(refdb);
        Set<MarkerDBLink> markerDBLinks = marker.getDbLinks();
        if (markerDBLinks == null) {
            markerDBLinks = new HashSet<>();
            markerDBLinks.add(mdb);
            marker.setDbLinks(markerDBLinks);
        } else {
            marker.getDbLinks().add(mdb);
        }
        currentSession().save(mdb);
        if (StringUtils.isNotEmpty(attributionZdbID)) {
            infrastructureRepository.insertRecordAttribution(mdb.getZdbID(), attributionZdbID);
        }

        String updateComment = "Adding dblink " + mdb.getReferenceDatabase().getForeignDB().getDisplayName() + ":" + mdb.getAccessionNumber();
        updateComment += StringUtils.isNotBlank(attributionZdbID) ? (" with attribution " + attributionZdbID) : " without attribution";
        InfrastructureService.insertUpdate(marker, updateComment);

        return mdb;
    }

    public MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason reason, MarkerAlias alias) {
        MarkerHistory history = new MarkerHistory();
        history.setDate(new Date());
        history.setName(newMarker.getName());
        history.setSymbol(newMarker.getAbbreviation());
        history.setMarker(newMarker);
        history.setEvent(event);
        history.setOldMarkerName(oldMarker.getName());
        // The reason should be passed
        history.setReason(reason);
        history.setMarkerAlias(alias);
        return history;
    }

    public List<MarkerFamilyName> getMarkerFamilyNamesBySubstring(String substring) {

        List<MarkerFamilyName> families = currentSession().createQuery("from MarkerFamilyName where upper(markerFamilyName) like :name || '%' order by 1 asc ", MarkerFamilyName.class).setParameter("name", substring.toUpperCase()).list();


        List<MarkerFamilyName> familiesContains = currentSession().createQuery("from MarkerFamilyName where upper(markerFamilyName) like  '%' || :name || '%' order by 1 asc ", MarkerFamilyName.class).setParameter("name", substring.toUpperCase()).list();
        familiesContains.forEach(familyName -> {
            if (!families.contains(familyName)) families.add(familyName);
        });
        return families;
    }

    public MarkerFamilyName getMarkerFamilyName(String name) {
        return currentSession().get(MarkerFamilyName.class, name);
    }

    public void createMarker(Marker marker, Publication pub, boolean insertUpdate) {
        if (marker == null) {
            throw new RuntimeException("No marker object provided.");
        }
        if (marker.getName() == null) {
            throw new RuntimeException("Cannot create a new marker without a name.");
        }
        if (marker.getMarkerType() == null) {
            throw new RuntimeException("Cannot create a new marker without a type.");
        }
        if (pub == null) {
            throw new RuntimeException("Cannot create a new marker without a publication.");
        }

        marker.setOwner(ProfileService.getCurrentSecurityUser());
        if (!marker.getOwner().getAccountInfo().getRoot()) {
            throw new RuntimeException("Non-root user cannot create a marker");
        }
        currentSession().save(marker);
        // Need to flush here to make the trigger fire as that will
        // create a MarkerHistory record needed.
        //   currentSession().flush();

        //add publication to attribution list.
        infrastructureRepository.insertRecordAttribution(marker.getZdbID(), pub.getZdbID());

        if (insertUpdate) {
            infrastructureRepository.insertUpdatesTable(marker, "New " + marker.getType().name(), "");
        }

    }

    public void createMarker(Marker marker, Publication pub) {
        createMarker(marker, pub, true);
    }

    /**
     * Checks if a gene has a small segment relationship with a given small segment.
     *
     * @param associatedMarker Gene
     * @param smallSegment     small segment marker
     * @return boolean
     */
    public boolean hasSmallSegmentRelationship(Marker associatedMarker, Marker smallSegment) {
        Session session = currentSession();

        String hql = """
            from MarkerRelationship
            where firstMarker = :firstMarker
            AND secondMarker = :secondMarker
            AND (type = :type1 or type = :type2 or type = :type3)
            """;
        Query<MarkerRelationship> query = session.createQuery(hql, MarkerRelationship.class);
        query.setParameter("firstMarker", associatedMarker);
        query.setParameter("secondMarker", smallSegment);
        query.setParameter("type1", MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT);
        query.setParameter("type2", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
        query.setParameter("type3", MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT);
        List<MarkerRelationship> rels = query.list();
        return !CollectionUtils.isEmpty(rels);
    }

    public boolean hasTranscriptRelationship(Marker associatedMarker, Marker transcript) {
        Session session = currentSession();

        String hql = "from MarkerRelationship where firstMarker = :firstMarker AND secondMarker = :secondMarker AND type = :type1 ";
        Query<MarkerRelationship> query = session.createQuery(hql, MarkerRelationship.class);
        query.setParameter("firstMarker", associatedMarker);
        query.setParameter("secondMarker", transcript);
        query.setParameter("type1", GENE_PRODUCES_TRANSCRIPT);
        List<MarkerRelationship> rels = query.list();
        return !CollectionUtils.isEmpty(rels);
    }

    public List<Marker> getMarkersByAbbreviationAndGroup(String name, Marker.TypeGroup markerType) {

        MarkerTypeGroup group = getMarkerTypeGroupByName(markerType.name());
        if (group == null) {
            return null;
        }

        List<MarkerType> types = group.getTypeStrings().stream().map(this::getMarkerTypeByName).collect(toList());
        // a slight speed improvement and more fine-grained sorting control (if needed)
        String hql = " select distinct m from Marker m  "
                     + " where m.abbreviation like :name  "
                     + " and m.markerType in (:types)  ";
//                + " order by m.abbreviationOrder asc " ;

        List<Marker> markerList = new ArrayList<>(HibernateUtil.currentSession().createQuery(hql, Marker.class).setParameter("name", "%" + name + "%").setParameterList("types", types).list());
        markerList.sort(new MarkerAbbreviationComparator(name));

        return markerList;
    }


    public List<Marker> getConstructsByAttribution(String name) {
        String hql = "select distinct m from Marker m , PublicationAttribution pa " +
                     "where lower(m.name) like lower(:name)  and pa.dataZdbID = m.zdbID  and m.markerType like '%CONS%'  ";
//                + " order by m.abbreviationOrder asc " ;
        List<Marker> markerList = new ArrayList<>(HibernateUtil.currentSession().createQuery(hql, Marker.class).setParameter("name", "%" + name + "%").list());
        markerList.sort(new MarkerAbbreviationComparator(name));
        return markerList;
    }

    public Marker getMarkerByAbbreviationAndAttribution(String name, String pubZdbId) {
        String hql = " select distinct m from Marker m , PublicationAttribution pa " +
                     "where trim(m.abbreviation) = trim(:name)  and pa.dataZdbID = m.zdbID  and pa.sourceZdbID = :publicationZdbId ";

        Session session = currentSession();
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("name", name);
        query.setParameter("publicationZdbId", pubZdbId);

        return ((Marker) query.uniqueResult());

    }

    public List<String> getPolymeraseNames() {
        String hql = " select c.polymeraseName from Clone c group by c.polymeraseName ";
        Session session = currentSession();
        return session.createQuery(hql, String.class).list();
    }

    public MarkerAlias getMarkerAlias(String aliasZdbID) {
        Session session = currentSession();
        return session.get(MarkerAlias.class, aliasZdbID);
    }

    public List<TranscriptTypeStatusDefinition> getAllTranscriptTypeStatusDefinitions() {
        Session session = currentSession();
        Query<TranscriptTypeStatusDefinition> query = session.createQuery("from TranscriptTypeStatusDefinition", TranscriptTypeStatusDefinition.class);
        return query.list();
    }

    public List<TranscriptType> getAllTranscriptTypes() {
        Session session = currentSession();
        Query<TranscriptType> query = session.createQuery("from TranscriptType order by order", TranscriptType.class);
        return query.list();
    }

    public TranscriptType getTranscriptTypeForName(String typeString) {
        Session session = currentSession();
        return session.createQuery("from TranscriptType where type = :type", TranscriptType.class).setParameter("type", typeString).uniqueResult();
    }

    public TranscriptStatus getTranscriptStatusForName(String statusString) {
        Session session = currentSession();
        Query<TranscriptStatus> criteria = session.createQuery("from TranscriptStatus where status = :status", TranscriptStatus.class);
        criteria.setParameter("status", statusString);
        return criteria.uniqueResult();
    }

    public boolean getGeneHasGOEvidence(Marker gene) {
        String hql = "select count( mgte) from MarkerGoTermEvidence mgte where mgte.marker.zdbID = :geneZdbID ";
        Query<Number> query = currentSession().createQuery(hql, Number.class);
        query.setParameter("geneZdbID", gene.getZdbID());
        return (query.uniqueResult().longValue() > 0);
    }


    public boolean getGeneHasExpressionImages(Marker gene) {
        String hql = """
            select count( fig ) from ExpressionExperiment2 ee
            join ee.figureStageSet er
            join er.figure fig
            join fig.images ims
            where ee.gene.zdbID = :geneZdbID
            """;
        Query<Number> query = currentSession().createQuery(hql, Number.class);
        query.setParameter("geneZdbID", gene.getZdbID());
        return (query.uniqueResult().longValue() > 0);
    }

    public boolean getGeneHasExpression(Marker gene) {
        String hql = """
            select count( er) from ExpressionExperiment2 ee
            join ee.figureStageSet er
            join er.figure fig
            where ee.gene.zdbID = :geneZdbID
            """;
        Query<Number> query = currentSession().createQuery(hql, Number.class);
        query.setParameter("geneZdbID", gene.getZdbID());
        return (query.uniqueResult().longValue() > 0);
    }

    public boolean getGeneHasPhenotype(Marker gene) {
        String sql = "SELECT count(phenox_pk_id) " +
                     "FROM mutant_fast_search, phenotype_experiment " +
                     "WHERE mfs_data_zdb_id = :geneZdbID " +
                     "AND mfs_genox_zdb_id = phenox_genox_zdb_id ";
        Query query = currentSession().createSQLQuery(sql);
        query.setParameter("geneZdbID", gene.getZdbID());

        return (((Number) query.uniqueResult()).longValue() > 0);
    }

    public boolean getGeneHasPhenotypeImage(Marker gene) {
        String sql = """
            SELECT phenox_fig_zdb_id
            FROM mutant_fast_search, phenotype_experiment, image
            WHERE mfs_data_zdb_id = :geneZdbID
            AND mfs_genox_zdb_id = phenox_genox_zdb_id
            AND phenox_fig_zdb_id = img_fig_zdb_id
            """;
        Query query = currentSession().createSQLQuery(sql);
        query.setParameter("geneZdbID", gene.getZdbID());
        return (query.list().size() > 0);
    }

    public List<String> getVectorNames() {
        String hql = " select c.vector.name  from Clone c group by c.vector.name ";
        Session session = currentSession();
        return session.createQuery(hql, String.class).list();
    }

    public List<String> getProbeLibraryNames() {
        String hql = " select c.probeLibrary.name  from Clone c group by c.probeLibrary.name ";
        Session session = currentSession();
        return session.createQuery(hql, String.class).list();
    }

    public List<ProbeLibrary> getProbeLibraries() {
        String hql = " from ProbeLibrary  ";
        Session session = currentSession();
        return session.createQuery(hql, ProbeLibrary.class).list();
    }

    public ProbeLibrary getProbeLibrary(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(ProbeLibrary.class, zdbID);
    }

    public List<String> getDigests() {
        String hql = " select c.digest  from Clone c group by c.digest  ";
        Session session = currentSession();
        return session.createQuery(hql, String.class).list();
    }

    public List<String> getCloneSites() {
        String hql = " select c.cloningSite from Clone c group by c.cloningSite ";
        Session session = currentSession();
        return session.createQuery(hql, String.class).list();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<HighQualityProbe> getHighQualityProbeStatistics(GenericTerm aoTerm, PaginationBean pagination, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();
        String hqlCount;
        if (includeSubstructures) {
            hqlCount = "select count(distinct stat.probe) " +
                       "     from HighQualityProbeAOStatistics stat " +
                       "     where stat.superterm.zdbID = :aoterm ";
        } else {
            hqlCount = "select count(distinct stat.probe) " +
                       "     from HighQualityProbeAOStatistics stat " +
                       "     where stat.superterm.zdbID = :aoterm and " +
                       "           stat.subterm.zdbID = :aoterm ";
        }
        Query<Number> query = session.createQuery(hqlCount, Number.class);
        query.setParameter("aoterm", aoTerm.getZdbID());
        int totalCount = query.uniqueResult().intValue();


        // if no antibodies found return here
        if (totalCount == 0) {
            return new PaginationResult<>(0, null);
        }

        String sqlQueryStr = """
            select distinct(stat.fstat_feat_zdb_id), probe.mrkr_abbrev as probeAbbrev, gene.mrkr_zdb_id,gene.mrkr_abbrev,gene.mrkr_abbrev_order
            from feature_stats as stat, marker as gene, marker as probe
            where fstat_superterm_zdb_id = :aoterm
            and fstat_gene_zdb_id = gene.mrkr_zdb_id
            and fstat_feat_zdb_id = probe.mrkr_zdb_id
            and fstat_type = :type
            """;
        if (!includeSubstructures) {
            sqlQueryStr += "  and fstat_subterm_zdb_id = :aoterm ";
        }
        sqlQueryStr += "order by gene.mrkr_abbrev_order ";

        NativeQuery sqlQquery = session.createNativeQuery(sqlQueryStr);
        sqlQquery.setParameter("aoterm", aoTerm.getZdbID());
        sqlQquery.setParameter("type", "High-Quality-Probe");
        sqlQquery.setFirstResult(pagination.getFirstRecord() - 1);
        sqlQquery.setMaxResults(pagination.getMaxDisplayRecordsInteger());
        List<Object[]> objs = sqlQquery.list();
        List<Marker> hqpRecords = new ArrayList<>();
        for (Object[] objects : objs) {
            Marker probe = new Marker();
            probe.setZdbID((String) objects[0]);
            probe.setAbbreviation((String) objects[1]);
            hqpRecords.add(probe);
        }
        // loop over all antibodyAOStatistic records until the given number of distinct antibodies from the pagination
        // bean is reached.
/*
            if (includeSubstructures)
                hql = "  from HighQualityProbeAOStatistics stat fetch all properties" +
                        "     where stat.superterm = :aoterm";
            else
*/
/*
        hql = "  from HighQualityProbeAOStatistics stat " +
                "     where stat.superterm.zdbID = :aoterm " +
                "           and stat.subterm.zdbID = :aoterm " +
                "     order by stat.gene.abbreviationOrder ";

*/
        String sqlQueryAllStr = """
            select stat.fstat_feat_zdb_id, probe.mrkr_abbrev as probeSymbol, gene.mrkr_zdb_id,
            gene.mrkr_abbrev,stat.fstat_fig_zdb_id, fig.fig_label, stat.fstat_pub_zdb_id,
            probe.mrkr_type, gene.mrkr_abbrev_order, pub.zdb_id, pub.pub_mini_ref,
            gene.mrkr_name, probe.mrkr_name as probeName, img.img_zdb_id
            from feature_stats as stat
            join marker as gene on fstat_gene_zdb_id = gene.mrkr_zdb_id
            join marker as probe on fstat_feat_zdb_id = probe.mrkr_zdb_id
            join figure as fig on fstat_fig_zdb_id = fig.fig_zdb_id
            join publication as pub on fstat_pub_zdb_id = pub.zdb_id
            left outer join image as img on fstat_img_zdb_id = img.img_zdb_id
            where fstat_superterm_zdb_id = :aoterm
            and fstat_type = :type
            """;
        if (!includeSubstructures) {
            sqlQueryAllStr += "  and fstat_subterm_zdb_id = :aoterm ";
        }
        sqlQueryAllStr += "order by gene.mrkr_abbrev_order ";

        NativeQuery sqlAllQquery = session.createNativeQuery(sqlQueryAllStr);
        sqlAllQquery.setParameter("aoterm", aoTerm.getZdbID());
        sqlAllQquery.setParameter("type", "High-Quality-Probe");
        ScrollableResults scrollableResults = sqlAllQquery.scroll();
        if (pagination.getFirstRecord() == 1) {
            scrollableResults.beforeFirst();
        } else {
            scrollableResults.setRowNumber(pagination.getFirstRecord() - 1);
        }
        List<HighQualityProbe> list = new ArrayList<>();
        // Since the number of entities that manifest a single record are comprised of
        // multiple single records (differ by figures, genes, pubs) from the database we have to aggregate
        // them into single entities. Need to populate one more entity than requestAjaed to collect
        // all information pertaining to that record. Have to remove that last entity.
        // When paginating from a place other than the start of all records we have to go through
        // the worst case scenario list (assuming there is only one record per probe) and then
        // loop over the results and check if the probe is in the list of probes from above
        while (scrollableResults.next() && list.size() < pagination.getMaxDisplayRecordsInteger() + 1) {
            Object[] record = scrollableResults.get();
            HighQualityProbeAOStatistics highQualityProbeStats = new HighQualityProbeAOStatistics();
            Marker probe = new Marker();
            probe.setZdbID((String) record[0]);
            probe.setAbbreviation((String) record[1]);
            probe.setName((String) record[12]);
            probe.setMarkerType(getProbeType((String) record[7]));
            highQualityProbeStats.setProbe(probe);
            Marker gene = new Marker();
            gene.setZdbID((String) record[2]);
            gene.setAbbreviation((String) record[3]);
            gene.setName((String) record[11]);
            gene.setMarkerType(getGenedomType());
            highQualityProbeStats.setGene(gene);
            String figID = (String) record[4];
            String label = (String) record[5];
            Figure figure;
            if (label != null && label.equals(FigureType.TOD.toString())) {
                figure = new TextOnlyFigure();
            } else {
                figure = new FigureFigure();
            }

            figure.setZdbID(figID);
            figure.setLabel(label);
            highQualityProbeStats.setFigure(figure);
            Publication pub = new Publication();
            pub.setZdbID((String) record[9]);
            pub.setShortAuthorList((String) record[10]);
            highQualityProbeStats.setPublication(pub);
            if (record[13] != null) {
                Image image = new Image();
                image.setZdbID((String) record[13]);
                highQualityProbeStats.setImage(image);
            }
            if (hqpRecords.contains(highQualityProbeStats.getProbe())) {
                populateProbeStatisticsRecord(highQualityProbeStats, list, aoTerm);
            }
        }
        // remove the last entity as it is beyond the display limit.
        if (list.size() > pagination.getMaxDisplayRecordsInteger()) {
            list.remove(list.size() - 1);
        }
        scrollableResults.close();
        return new PaginationResult<>(totalCount, list);

    }

    private MarkerType getGenedomType() {
        MarkerType type = new MarkerType();
        type.setType(Marker.Type.GENE);
        Set<Marker.TypeGroup> typeGroups = new HashSet<>();
        typeGroups.add(Marker.TypeGroup.GENEDOM);
        type.setTypeGroups(typeGroups);
        return type;
    }

    private MarkerType getProbeType(String typeStr) {
        MarkerType type = new MarkerType();
        type.setType(Marker.Type.getType(typeStr));
        Set<Marker.TypeGroup> typeGroups = new HashSet<>();
        typeGroups.add(Marker.TypeGroup.getType(typeStr));
        type.setTypeGroups(typeGroups);
        return type;
    }


    @Override
    public List<Marker> getMarkersForStandardAttributionAndType(Publication publication, String type) {
        List<String> types = new ArrayList<>();
        if (type.equals("MRPHLNO")) {

            types.add(Marker.Type.MRPHLNO.name());
            types.add(Marker.Type.TALEN.name());
            types.add(Marker.Type.CRISPR.name());

        } else {

            types.add(Marker.Type.GENE.name());
            List<MarkerType> mkrType = getMarkerTypesByGroup(Marker.TypeGroup.GENEDOM);
            for (MarkerType markerType : mkrType) {
                types.add(markerType.getName());
            }


        }
        String hql = """
            select m from PublicationAttribution pa , Marker m where pa.dataZdbID=m.zdbID and pa.publication.zdbID= :pubZdbID
            and pa.sourceType= :sourceType and m.markerType.name in (:types) order by m.abbreviationOrder
            """;
        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameter("pubZdbID", publication.getZdbID());
        query.setParameter("sourceType", RecordAttribution.SourceType.STANDARD.toString());
        // yes, this is a hack, should use typeGroup, I guess
        query.setParameterList("types", types);
        return query.list();
    }


    @Override
    public List<Marker> getMarkersForAttribution(String publicationZdbID) {
        String hql = """
            select distinct m from Marker m , RecordAttribution ra
            left join fetch m.aliases
            left join fetch m.markerType
            where ra.dataZdbID=m.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID
            order by m.abbreviationOrder
            """;

        return currentSession().createQuery(hql, Marker.class).setParameter("pubZdbID", publicationZdbID).setParameter("standard", RecordAttribution.SourceType.STANDARD.toString()).list();
    }


    @Override
    public List<ConstructCuration> getConstructsForAttribution(String publicationZdbID) {
        String hql = "select distinct m from ConstructCuration m , RecordAttribution ra " +
                     "where ra.dataZdbID=m.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID";

        return currentSession().createQuery(hql, ConstructCuration.class).setParameter("pubZdbID", publicationZdbID).setParameter("standard", RecordAttribution.SourceType.STANDARD.toString()).list();
    }

    public List<Publication> getHighQualityProbePublications(GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql;
        hql = """
            select distinct stat.publication
            from HighQualityProbeAOStatistics stat
            where stat.superterm = :aoterm and
            stat.subterm = :aoterm
            order by stat.publication.publicationDate
            """;
        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameter("aoterm", anatomyTerm);
        return query.list();
    }

    /**
     * Create a list of AntibodyStatistics objects from antibodyAOStatistics record.
     * This logic groups the objects accordingly.
     *
     * @param record AntibodyAOStatistics
     * @param aoTerm anatom term
     * @param list   antibodyStatistics objects to be manipulated.
     */
    private void populateProbeStatisticsRecord(HighQualityProbeAOStatistics record, List<HighQualityProbe> list, GenericTerm aoTerm) {

        if (record == null || record.getProbe() == null) {
            return;
        }

        HighQualityProbe probeStats;
        if (list.size() == 0) {
            probeStats = new HighQualityProbe(record.getProbe(), aoTerm);
            list.add(probeStats);
        } else {
            probeStats = list.get(list.size() - 1);
        }

        // if antibody from records is the same as the one on the statistics object
        // add new info to that object.
        HighQualityProbe newProbeStats;
        boolean isNew = false;
        if (record.getProbe().equals(probeStats.getProbe())) {
            newProbeStats = probeStats;
        } else {
            newProbeStats = new HighQualityProbe(record.getProbe(), probeStats.getAnatomyTerm());
            isNew = true;
        }

        Marker gene = record.getGene();
        if (gene != null) {
            newProbeStats.addGene(gene);
        }
        Figure figure = record.getFigure();
        if (figure != null) {
            newProbeStats.addFigure(figure);
        }
        Publication publication = record.getPublication();
        if (publication != null) {
            newProbeStats.addPublication(publication);
        }
        Image image = record.getImage();
        if (image != null) {
            newProbeStats.addImage(image);
        }

        if (isNew) {
            list.add(newProbeStats);
        }
    }

    public MarkerType getMarkerTypeByName(String name) {
        Session session = currentSession();
        MarkerType type = session.load(MarkerType.class, name);
        if (type == null || type.getName() == null) {
            return null;
        }
        return type;
    }

    public MarkerType getMarkerTypeByDisplayName(String displayName) {
        Session session = currentSession();
        return session.createQuery("from MarkerType where displayName = :name", MarkerType.class).setParameter("name", displayName).uniqueResult();
    }

    public MarkerTypeGroup getMarkerTypeGroupByName(String name) {
        Session session = currentSession();
        MarkerTypeGroup markerTypeGroup = session.get(MarkerTypeGroup.class, name);
        if (markerTypeGroup == null || markerTypeGroup.getName() == null) {
            return null;
        }
        return markerTypeGroup;
    }

    /**
     * Rename an existing marker. This entails to
     * to provide a reason, a publication on which basis this is done.
     * This will run a script to populate a fastsearch table for renamed markers.
     */
    public void renameMarker(Marker marker, Publication publication, MarkerHistory.Reason reason, String oldSymbol, String oldGeneName) {
        //update marker history reason
        log.debug("Got to rename marker: " + marker.getAbbreviation() + " " + marker.getZdbID() + " " + marker.getName());
        MarkerHistory history = new MarkerHistory();
        history.setReason(reason);
        history.setName(oldGeneName);
        history.setOldMarkerName(oldSymbol);
        history.setSymbol(marker.getAbbreviation());
        history.setMarker(marker);
        history.setEvent(MarkerHistory.Event.REASSIGNED);
        MarkerAlias alias = getMarkerRepository().addMarkerAlias(marker, marker.getAbbreviation(), publication);
        history.setMarkerAlias(alias);
        getInfrastructureRepository().insertMarkerHistory(history);
        infrastructureRepository.insertRecordAttribution(alias.getZdbID(), publication.getZdbID());
    }

    /**
     * Retrieve marker types by marker type groups
     *
     * @param typeGroup type group
     * @return list of marker types
     */
    public List<MarkerType> getMarkerTypesByGroup(Marker.TypeGroup typeGroup) {
        if (typeGroup == null) {
            return null;
        }
        MarkerTypeGroup group = getMarkerTypeGroupByName(typeGroup.name());
        List<MarkerType> markerTypes = new ArrayList<>();
        for (String type : group.getTypeStrings()) {
            markerTypes.add(getMarkerTypeByName(type));
        }
        return markerTypes;
    }

    /**
     * Retrieve gene for a given sequence targeting reagent which is targeting it.
     * Target genes are ordered by gene abbreviation
     *
     * @param stReagent valid sequence targeting reagent of Marker object.
     * @return the target gene of the sequence targeting reagent
     */
    public List<Marker> getTargetGenesAsMarkerForSequenceTargetingReagent(SequenceTargetingReagent stReagent) {
        if (stReagent == null) {
            return null;
        }
        Session session = currentSession();
        String hql = """
            select rel.secondMarker from MarkerRelationship as rel
            where rel.firstMarker = :sequenceTargetingReagent and rel.type = :type
            order by rel.secondMarker.abbreviationOrder
            """;
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("sequenceTargetingReagent", stReagent);
        query.setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        return query.list();
    }


    /**
     * Checks to see if a marker with the abbreviation given is already in the database.
     * The check is case insensitive.
     *
     * @param abbreviation string
     * @return true/false
     */
    @Override
    public boolean isMarkerExists(String abbreviation) {

        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Marker> cr = cb.createQuery(Marker.class);
        Root<Marker> root = cr.from(Marker.class);
        cr.where(cb.equal(cb.upper(root.get("abbreviation")), abbreviation.toUpperCase()));
        return session.createQuery(cr).uniqueResult() != null;
    }

    /**
     * Retrieves firstN markers of each marker type.
     * If firstN = 0 retrieve all markers
     * If firstN < 0 return null
     *
     * @param firstN number of markers to be returned
     * @return list of markers
     */
    @Override
    public List<String> getNMarkersPerType(int firstN) {
        if (firstN < 0) {
            return null;
        }
        if (firstN == 0) {
            return getAllMarkers();
        }

        Session session = HibernateUtil.currentSession();
        List<MarkerType> markerTypes = getAllMarkerTypes();

        // we have about 20 different marker types
        List<String> allIds = new ArrayList<>(20 * firstN);
        for (MarkerType markerType : markerTypes) {
            String hql = "select zdbID from Marker where markerType = :markerType order by zdbID";
            Query<String> query = session.createQuery(hql, String.class);
            query.setParameter("markerType", markerType);
            query.setMaxResults(firstN);
            allIds.addAll(query.list());
        }
        return allIds;
    }

    /**
     * Retrieve all distinct marker types used in the marker table
     *
     * @return list of marker types
     */
    public List<MarkerType> getAllMarkerTypes() {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct markerType from Marker";
        Query<MarkerType> query = session.createQuery(hql, MarkerType.class);
        return query.list();
    }

    /**
     * Retrieve all gene ids of genes that have a SwissProt external note.
     *
     * @param firstNIds number of records to be returned
     * @return list of gene ids
     */
    @Override
    public List<String> getNMarkersWithUniProtNote(int firstNIds) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select distinct accession.dataZdbID from DBLink accession, ExternalNote note
            where accession.referenceDatabase.foreignDB.dbName = :sourceName
            and note.externalDataZdbID = accession.zdbID
            order by accession.dataZdbID
            """;
        Query<String> query = session.createQuery(hql, String.class);
        query.setParameter("sourceName", "UniProtKB");
        if (firstNIds > 0) {
            query.setMaxResults(firstNIds);
        }
        return query.list();
    }

    /**
     * Retrieves all markers if no number is given or the first N markers for firstN = 0;
     *
     * @return list of markers
     */
    @Override
    public List<String> getAllMarkers() {
        Session session = HibernateUtil.currentSession();
        String hql = "select zdbID from Marker order by zdbID";
        Query<String> query = session.createQuery(hql, String.class);
        return query.list();
    }


    @Override
    public boolean getHasMarkerHistory(String zdbId) {

        String hqlCount = "SELECT count(mh) FROM MarkerHistory mh WHERE marker.zdbID = :zdbID ";
        Query<Number> query = HibernateUtil.currentSession().createQuery(hqlCount, Number.class);
        query.setParameter("zdbID", zdbId);
        return query.uniqueResult().intValue() > 0;
    }

    @Override
    public List<PreviousNameLight> getPreviousNamesLight(final Marker gene) {
        String sql = """
             SELECT da.dalias_alias, ra.recattrib_source_zdb_id, da.dalias_zdb_id
                FROM data_alias da
                JOIN alias_group ag ON da.dalias_group_id=ag.aliasgrp_pk_id
                LEFT OUTER JOIN record_attribution ra ON ra.recattrib_data_zdb_id=da.dalias_zdb_id
                WHERE dalias_data_zdb_id = :markerZdbID
                AND aliasgrp_pk_id = dalias_group_id
                AND aliasgrp_name = 'alias'
            """;
        NativeQuery query = currentSession().createSQLQuery(sql);
        query.setParameter("markerZdbID", gene.getZdbID());
        HibernateUpgradeHelper.setTupleResultAndListTransformer(query, (Object[] tuple, String[] aliases) -> {
            String pureAliasName = (String) tuple[0];
            String publicationZdbID = (String) tuple[1];
            String aliasZdbID = (String) tuple[2];

            PreviousNameLight previousNameLight = new PreviousNameLight(gene.getAbbreviation());
            previousNameLight.setMarkerZdbID(gene.getZdbID());
            previousNameLight.setPureAliasName(pureAliasName);
            previousNameLight.setAliasZdbID(aliasZdbID);

            if (publicationZdbID != null) {
                previousNameLight.setPublicationZdbID(publicationZdbID);
                previousNameLight.setPublicationCount(1);
            }

            return previousNameLight;
        }, (List list) -> {
            Map<String, PreviousNameLight> map = new HashMap<>();
            for (Object o : list) {
                PreviousNameLight previousName = (PreviousNameLight) o;
                PreviousNameLight previousNameStored = map.get(previousName.getAlias());

                //if it hasn't been stored, it's the first occurrence of this alias text, store it
                if (previousNameStored == null) {
                    map.put(previousName.getAlias(), previousName);
                } else {  //if it's already been stored, just increment the pub count
                    previousNameStored.setPublicationCount(previousNameStored.getPublicationCount() + previousName.getPublicationCount());
                    map.put(previousNameStored.getAlias(), previousNameStored);
                }
            }

            list = new ArrayList(map.values());

            Collections.sort(list);

            return list;
        });
        return query.list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayExcludeTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... typesNotIn) {
        String sql1To2 = """
            select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,
            mreltype_1_to_2_comments,
            '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' ,
            ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,
            src.srcurl_url, src.srcurl_display_text , mrel_zdb_id
            from marker_relationship
            inner join marker_relationship_type on mrel_type = mreltype_name
            inner join marker on mrel_mrkr_2_zdb_id = mrkr_zdb_id
            inner join marker_types on mrkr_type = marker_type
            left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id
            left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_2_zdb_id
            left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id
            left outer join db_link on mrel_mrkr_2_zdb_id = dblink_linked_recid
            and (dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' or dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37')
            where mrel_mrkr_1_zdb_id = :markerZdbId
            """;
        if (typesNotIn.length > 0) {
            sql1To2 += "	   and mrel_type not in (:types) ";
        }
        sql1To2 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";

        String sql2To1 = """
            select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,
            mreltype_2_to_1_comments,
            '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' ,
            ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,
            src.srcurl_url, src.srcurl_display_text  , mrel_zdb_id
            from marker_relationship
            inner join marker_relationship_type on mrel_type = mreltype_name
            inner join marker on mrel_mrkr_1_zdb_id = mrkr_zdb_id
            inner join marker_types on mrkr_type = marker_type
            left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id
            left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_1_zdb_id
            left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id
            where mrel_mrkr_2_zdb_id = :markerZdbId
            """;

        if (typesNotIn.length > 0) {
            sql2To1 += "	   and mrel_type not in (:types) ";
        }
        sql2To1 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";


        ResultTransformer resultTransformer = new MarkerRelationshipSupplierPresentationTransformer(is1to2);
        String sql = (is1to2 ? sql1To2 : sql2To1);
        Query query = HibernateUtil.currentSession().createSQLQuery(sql).setParameter("markerZdbId", marker.getZdbID()).setResultTransformer(resultTransformer);
        if (typesNotIn.length > 0) {
            List<String> types = new ArrayList<>();
            for (MarkerRelationship.Type type : typesNotIn) {
                types.add(type.toString());
            }
            query.setParameterList("types", types);
        }

        return (List<MarkerRelationshipPresentation>) resultTransformer.transformList(query.list());
    }

    @Override
    public List<Marker> getMarkersByAlias(String key) {
        String hql = " select ma.marker from MarkerAlias ma where ma.aliasLowerCase = :alias ";

        return HibernateUtil.currentSession().createQuery(hql, Marker.class).setParameter("alias", key.toLowerCase()).list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayForTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types) {
        String sql1To2 = """
            select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,
            mreltype_1_to_2_comments, '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' ,
            ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,
            src.srcurl_url, src.srcurl_display_text  , mrel_zdb_id
            from marker_relationship
            inner join marker_relationship_type on mrel_type = mreltype_name
            inner join marker on mrel_mrkr_2_zdb_id = mrkr_zdb_id
            inner join marker_types on mrkr_type = marker_type
            left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id
            left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_2_zdb_id
            left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id
            where mrel_mrkr_1_zdb_id = :markerZdbId
            """;
        if (types.length > 0) {
            sql1To2 += "	   and mrel_type in (:types) ";
        }
        sql1To2 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order";

        String sql2To1 = """
            select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,
            mreltype_2_to_1_comments, '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' ,
            ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,
            src.srcurl_url, src.srcurl_display_text , mrel_zdb_id
            from marker_relationship
            inner join marker_relationship_type on mrel_type = mreltype_name
            inner join marker on mrel_mrkr_1_zdb_id = mrkr_zdb_id
            inner join marker_types on mrkr_type = marker_type
            left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id
            left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_1_zdb_id
            left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id
            where mrel_mrkr_2_zdb_id = :markerZdbId
            """;
        if (types.length > 0) {
            sql2To1 += "	   and mrel_type in (:types) ";
        }
        sql2To1 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";
        ResultTransformer resultTransformer = new MarkerRelationshipSupplierPresentationTransformer(is1to2);
        String sql = (is1to2 ? sql1To2 : sql2To1);
        Query query = HibernateUtil.currentSession().createNativeQuery(sql).setParameter("markerZdbId", marker.getZdbID()).unwrap(org.hibernate.query.Query.class).setResultTransformer(resultTransformer);
        if (types.length > 0) {
            Set<String> typeStrings = new HashSet<>();
            for (MarkerRelationship.Type type : types) {
                log.debug("type: " + type.toString());
                typeStrings.add(type.toString());
            }
            query.setParameterList("types", typeStrings);
        }

        List<MarkerRelationshipPresentation> list = resultTransformer.transformList(query.list());
        return list;
    }

    private class MarkerDBLinksTransformer implements ResultTransformer {
        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            LinkDisplay linkDisplay = new LinkDisplay();
            linkDisplay.setDataType(tuple[0].toString());

            if (tuple[1] != null) {
                linkDisplay.setLength(tuple[1].toString());
            }

            linkDisplay.setMarkerZdbID(tuple[2].toString());

            linkDisplay.setAccession(tuple[3].toString());

            linkDisplay.setReferenceDatabaseName(tuple[4].toString());

            linkDisplay.setUrlPrefix(tuple[5].toString());

            if (tuple[6] != null) {
                linkDisplay.setUrlSuffix(tuple[6].toString());
            }
            if (tuple[7] != null) {
                MarkerReferenceBean reference = new MarkerReferenceBean();
                reference.setZdbID(tuple[7].toString());

                if (tuple.length > 11 && tuple[11] != null) {
                    reference.setTitle(tuple[11].toString());
                }
                if (tuple.length > 9 && tuple[9] != null) {
                    reference.setDataZdbID(tuple[9].toString());
                }
                linkDisplay.addReference(reference);
            }
            if (tuple[8] != null) {
                linkDisplay.setSignificance(Integer.valueOf(tuple[8].toString()));

            }
            linkDisplay.setDblinkZdbID(tuple[9].toString());

            if (tuple.length > 10) {
                linkDisplay.setReferenceDatabaseZdbID(tuple[10].toString());

            }
            if (tuple.length > 12 && tuple[12] != null) {

                linkDisplay.setTypeOrder(Integer.valueOf(tuple[12].toString()));

            }

            if (tuple.length > 12 && tuple[13] != null) {
                linkDisplay.setAccNumDisplay(tuple[13].toString());

            }
            if (tuple.length > 11 && tuple[11] != null) {
                linkDisplay.setAssociatedGeneID(tuple[11].toString());
            }
            return linkDisplay;
        }

        @Override
        public List transformList(List list) {
            Map<String, LinkDisplay> linkMap = new HashMap<>();
            for (Object o : list) {
                LinkDisplay display = (LinkDisplay) o;
                String linkKey = display.getAccession() + ":" + display.getReferenceDatabaseZdbID();
                LinkDisplay displayStored = linkMap.get(linkKey);
                if (displayStored != null) {
                    displayStored.addReferences(display.getReferences());
                    linkMap.put(linkKey, displayStored);
                } else {
                    linkMap.put(linkKey, display);
                }

            }

            return new ArrayList<>(linkMap.values());
        }
    }

    public MarkerDBLink getMarkerDBLink(String linkId) {
        Session session = HibernateUtil.currentSession();
        return session.get(MarkerDBLink.class, linkId);
    }

    public String getABRegID(String zdbID) {
        List<String> abregIDList = getABRegIDs(zdbID);
        if (abregIDList == null || abregIDList.size() == 0) {
            return null;
        }
        return abregIDList.get(0);
    }

    public List<String> getABRegIDs(String zdbID) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<DBLink> root = query.from(DBLink.class);
        query.select(root.get("accessionNumber")).where(cb.and(cb.equal(root.get("dataZdbID"), zdbID), cb.like(root.get("accessionNumber"), "AB%"))).orderBy(cb.desc(root.get("accessionNumber")));
        return session.createQuery(query).getResultList();
    }

    public List<LinkDisplay> getMarkerLinkDisplay(String dbLinkId) {
        String sql = """
            SELECT fdbdt.fdbdt_data_type, dbl.dblink_length, dbl.dblink_linked_recid, dbl.dblink_acc_num, fdb.fdb_db_display_name, fdb.fdb_db_query, fdb.fdb_url_suffix,
            ra.recattrib_source_zdb_id, fdb.fdb_db_significance, dbl.dblink_zdb_id, fdbc.fdbcont_zdb_id, pub.title, fdbdt.fdbdt_display_order, dbl.dblink_acc_num_display
            FROM db_link dbl
            JOIN foreign_db_contains fdbc ON dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id
            JOIN foreign_db fdb ON fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
            JOIN foreign_db_data_type fdbdt ON fdbdt.fdbdt_pk_id = fdbc.fdbcont_fdbdt_id
            LEFT OUTER JOIN record_attribution ra ON ra.recattrib_data_zdb_id=dbl.dblink_zdb_id
            JOIN publication pub ON ra.recattrib_source_zdb_id=pub.zdb_id
            WHERE dbl.dblink_zdb_id = :dbLinkId
            """;

        Query query = HibernateUtil.currentSession().createSQLQuery(sql).setParameter("dbLinkId", dbLinkId).setResultTransformer(markerDBLinkTransformer);

        return markerDBLinkTransformer.transformList(query.list());
    }

    public List<LinkDisplay> getMarkerDBLinksFast(Marker marker, DisplayGroup.GroupName groupName) {
        if (groupName == null) {
            //short circuit null pointer exception by returning empty array
            return new ArrayList<>();
        }

        String sql = """
            select fdbdt.fdbdt_data_type,dbl.dblink_length,dbl.dblink_linked_recid,dbl.dblink_acc_num,fdb.fdb_db_display_name,fdb.fdb_db_query,fdb.fdb_url_suffix,
            ra.recattrib_source_zdb_id, fdb.fdb_db_significance, dbl.dblink_zdb_id, fdbc.fdbcont_zdb_id, pub.title, fdbdt.fdbdt_display_order, dbl.dblink_acc_num_display
            from db_link dbl
            join foreign_db_contains_display_group_member m on m.fdbcdgm_fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id
            join foreign_db_contains_display_group g on g.fdbcdg_pk_id=m.fdbcdgm_group_id
            join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id
            join foreign_db_data_type fdbdt on fdbdt.fdbdt_pk_id = fdbc.fdbcont_fdbdt_id
            join foreign_db fdb on fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
            left outer join record_attribution ra on ra.recattrib_data_zdb_id=dbl.dblink_zdb_id
            left outer join publication pub on ra.recattrib_source_zdb_id=pub.zdb_id
            where g.fdbcdg_name= :displayGroup
            and
            dbl.dblink_linked_recid= :markerZdbId
            """;
        // case 7586 suppress OTTDARG's and ENSDARGG's on transcript pages
        if (marker.getZdbID().startsWith("ZDB-TSCRIPT")) {
            sql += " and fdb.fdb_db_name != 'VEGA' ";
        }
        if (marker.getZdbID().startsWith("ZDB-GENE")) {
            sql += " and dbl.dblink_acc_num not like  'ENSDARP%' ";
        }
        Query query = HibernateUtil.currentSession().createSQLQuery(sql).setParameter("markerZdbId", marker.getZdbID()).setParameter("displayGroup", groupName.toString()).setResultTransformer(markerDBLinkTransformer);

        List<LinkDisplay> linkDisplay = markerDBLinkTransformer.transformList(query.list());
        linkDisplay.sort((linkA, linkB) -> {
            int compare;
            if (linkA.getTypeOrder() != null & linkB.getTypeOrder() != null) {
                compare = linkA.getTypeOrder().compareTo(linkB.getTypeOrder());
                if (compare != 0) return compare;
            }

            if (linkA.getSignificance() != null & linkB.getSignificance() != null) {
                compare = linkA.getSignificance().compareTo(linkB.getSignificance());
                if (compare != 0) return compare;
            }

            if (linkA.getLength() != null & linkB.getLength() != null) {
                compare = linkA.getLength().compareTo(linkB.getLength());
                if (compare != 0) return compare;
            } else if (linkA.getLength() != null & (linkB.getLength() == null)) {
                return 1;
            } else if (linkA.getLength() == null & (linkB.getLength() != null)) {
                return -1;
            }

            compare = linkA.getReferenceDatabaseName().compareTo(linkB.getReferenceDatabaseName());
            if (compare != 0) return compare;

            NumberAwareStringComparator numberAwareStringComparator = new NumberAwareStringComparator();
            return numberAwareStringComparator.compare(linkA.getDisplayName(), linkB.getDisplayName());
        });


        return linkDisplay;
    }


    @Override
    public List<MarkerRelationshipPresentation> getRelatedMarkerDisplayForTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types) {
        String sql1To2 = """
            select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,
            mreltype_1_to_2_comments, mrkr_name, ra.recattrib_source_zdb_id , mrel_zdb_id
            from marker_relationship
            inner join marker_relationship_type on mrel_type = mreltype_name
            inner join marker on mrel_mrkr_2_zdb_id = mrkr_zdb_id
            inner join marker_types on mrkr_type = marker_type
            left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id
            where mrel_mrkr_1_zdb_id = :markerZdbId
            """;
        if (types.length > 0) {
            sql1To2 += "	   and mrel_type in (:types) ";
        }
        sql1To2 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order";

        String sql2To1 = """
            select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display, mreltype_2_to_1_comments, mrkr_name,
            ra.recattrib_source_zdb_id , mrel_zdb_id
            from marker_relationship
            inner join marker_relationship_type on mrel_type = mreltype_name
            inner join marker on mrel_mrkr_1_zdb_id = mrkr_zdb_id
            inner join marker_types on mrkr_type = marker_type
            left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id
            where mrel_mrkr_2_zdb_id = :markerZdbId
            """;
        if (types.length > 0) {
            sql2To1 += "	   and mrel_type in (:types) ";
        }
        sql2To1 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";


        ResultTransformer resultTransformer = new MarkerRelationshipPresentationTransformer(is1to2);
        String sql = (is1to2 ? sql1To2 : sql2To1);
        Query query = HibernateUtil.currentSession().createSQLQuery(sql).setParameter("markerZdbId", marker.getZdbID()).setResultTransformer(resultTransformer);
        if (types.length > 0) {
            Set<String> typeStrings = new HashSet<>();
            for (MarkerRelationship.Type type : types) {
                typeStrings.add(type.toString());
            }
            query.setParameterList("types", typeStrings);
        }

        List<MarkerRelationshipPresentation> list = resultTransformer.transformList(query.list());
        return list;
    }

    @Override
    public List<GeneProductsBean> getGeneProducts(String zdbID) {
        String sql = """
            SELECT dblink_acc_num, REPLACE(extnote_note,'CTRL','<br>')
            FROM external_note, db_link
            WHERE extnote_data_zdb_id = dblink_zdb_id
            AND dblink_linked_recid = :markerZdbID
            """;

        List<Tuple> tupleList = HibernateUtil.currentSession().createNativeQuery(sql, Tuple.class).setParameter("markerZdbID", zdbID).list();

        return tupleList.stream().map(tuple -> {
            GeneProductsBean geneProductsBean = new GeneProductsBean();
            geneProductsBean.setAccession(tuple.get(0).toString());
            if (tuple.get(1) != null) {
                geneProductsBean.setComment(tuple.get(1).toString());
            }
            return geneProductsBean;
        }).collect(toList());
    }


    public List<InterProProtein> getInterProForMarker(Marker marker) {
        Session session = currentSession();
        String sql = """
            SELECT  distinct ip FROM InterProProtein ip, MarkerToProtein mtp, ProteinToInterPro pti
            WHERE ip.ipID = pti.interProID
            AND pti.uniProtID = mtp.mtpUniProtID
            AND mtp.marker = :marker order by ip.ipType
            """;
        Query<InterProProtein> query = session.createQuery(sql, InterProProtein.class);
        query.setParameter("marker", marker);
        return query.list();
    }

    @Override
    public void insertInterProForMarker(String markerZdbID, String uniprot) {
        Session session = currentSession();
        String sql = " insert into marker_to_protein (mtp_mrkr_zdb_id, mtp_uniprot_id) " +
                     " values (:markerZdbID, :uniprot) ";
        Query query = session.createNativeQuery(sql);
        query.setParameter("markerZdbID", markerZdbID);
        query.setParameter("uniprot", uniprot);
        query.executeUpdate();
    }

    @Override
    public void deleteInterProForMarker(String markerZdbID, String uniprot) {
        Session session = currentSession();
        String sql = " delete from marker_to_protein " +
                     " where mtp_mrkr_zdb_id=:markerZdbID and mtp_uniprot_id=:uniprot ";
        Query query = session.createNativeQuery(sql);
        query.setParameter("markerZdbID", markerZdbID);
        query.setParameter("uniprot", uniprot);
        query.executeUpdate();
    }


    public List<String> getIPNames(String uniprot) {
        String sql = """
            select ip_name from marker_to_protein, protein_to_interpro,interpro_protein
            where mtp_uniprot_id=:uniprot and mtp_uniprot_id=pti_uniprot_id and pti_interpro_id=ip_interpro_id
            order by ip_name
            """;
        return HibernateUtil.currentSession().createNativeQuery(sql, Tuple.class).setParameter("uniprot", uniprot).list().stream().map(tuple -> (String) tuple.get(0)).collect(toList());
    }


    @Override
    public List<String> getProteinType(Marker gene) {
        String sql = """
            select distinct ip_name from marker_to_protein, protein_to_interpro,interpro_protein
            where mtp_mrkr_zdb_id=:markerzdb and mtp_uniprot_id=pti_uniprot_id and pti_interpro_id=ip_interpro_id
            order by ip_name
            """;

        return HibernateUtil.currentSession().createNativeQuery(sql).setParameter("markerzdb", gene.getZdbID()).list();
    }


    @Override
    public boolean isFromChimericClone(String zdbID) {
        String sql = """
            SELECT count(*) AS count
            FROM marker_relationship
            JOIN clone ON clone_mrkr_zdb_id = mrel_mrkr_2_zdb_id
            WHERE mrel_mrkr_1_zdb_id = :markerZdbID
            AND clone_problem_type = 'Chimeric'
            """;
        long result = (Long) HibernateUtil.currentSession().createSQLQuery(sql).addScalar("count", LongType.INSTANCE).setParameter("markerZdbID", zdbID).uniqueResult();
        return result > 0;
    }

    @Override
    public boolean cloneHasSnp(Clone clone) {
        String sql = """
            SELECT DISTINCT snpdattr_pub_zdb_id
            FROM snp_download_attribution, snp_download
            WHERE snpd_mrkr_zdb_id=:markerZdbID
            AND snpd_pk_id=snpdattr_snpd_pk_id
            ORDER BY snpdattr_pub_zdb_id
            """;
        Object result = HibernateUtil.currentSession().createSQLQuery(sql).setParameter("markerZdbID", clone.getZdbID()).setMaxResults(1).uniqueResult();
        return result != null;
    }

    @Override
    public List<MarkerSupplier> getSuppliersForMarker(String zdbID) {
        /*
        ToDo: This is not working. probably because of the compositeKey in XML
        Guess we first have to turn this in Hibernate Annotation and then
        can call the jpa logic
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MarkerSupplier> cr = cb.createQuery(MarkerSupplier.class);
        Root<MarkerSupplier> root = cr.from(MarkerSupplier.class);
        cr.select(root).where(cb.equal(root.get("dataZdbID"), zdbID));
        return session.createQuery(cr).getResultList();
*/
        String hql = "from MarkerSupplier where dataZdbID = :zdbID";
        return HibernateUtil.currentSession().createQuery(hql, MarkerSupplier.class).setParameter("zdbID", zdbID).list();
    }

    @Override
    public boolean markerExistsForZdbID(String zdbID) {
        return getMarkerByID(zdbID) != null;
    }

    @Override
    public List<String> getMarkerZdbIdsForType(Marker.Type markerType) {
        String hql = """
            select m.zdbID from Marker m
            where m.markerType.name = :type
            """;
        return HibernateUtil.currentSession().createQuery(hql, String.class).setParameter("type", markerType.name()).list();
    }

    // abbrev, zdbID
    @Override
    public Map<String, String> getGeoMarkerCandidates() {
        List<String> types = new ArrayList<>();
        types.add(Marker.Type.CDNA.name());
        types.add(Marker.Type.EST.name());
        types.add(Marker.Type.GENE.name());
        types.add(Marker.Type.GENEP.name());

        String hql = " select m.abbreviation, m.zdbID from Marker m where m.markerType.name in (:types) ";
        List<Tuple> markerTuple = HibernateUtil.currentSession().createQuery(hql, Tuple.class).setParameterList("types", types).list();

        Map<String, String> markerCandidates = new HashMap<>();
        markerTuple.forEach(tuple -> markerCandidates.put((String) tuple.get(0), (String) tuple.get(1)));
        return markerCandidates;
    }

    /**
     * From case 6582.
     * Pull the transgenic construct though the transcript onto the gene
     * select m.* from marker_relationship mr1
     * join marker_relationship mr2 on mr1.mrel_mrkr_2_zdb_id=mr2.mrel_mrkr_2_zdb_id
     * join marker m on m.mrkr_zdb_id=mr2.mrel_mrkr_1_zdb_id
     * where mr1.mrel_mrkr_1_zdb_id='ZDB-GENE-030710-1'
     * and mr1.mrel_type='gene produces transcript'
     * and mr2.mrel_type in ('promoter of','coding sequence of','contains engineered region')
     * ;
     */
    @Override
    public List<Marker> getConstructsForGene(Marker gene) {

        List<MarkerRelationship.Type> markerRelationshipList = new ArrayList<>();
        markerRelationshipList.add(MarkerRelationship.Type.PROMOTER_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CONTAINS_REGION);

        String hql = """
            select m from MarkerRelationship mr1 , MarkerRelationship  mr2, Marker m
            where mr1.secondMarker.zdbID=mr2.secondMarker.zdbID 
            and m.zdbID=mr2.firstMarker.zdbID 
            and mr1.firstMarker.zdbID = :markerZdbID
            and mr1.type = :markerRelationshipType1
            and mr2.type in (:markerRelationshipType2)
            """;

        return HibernateUtil.currentSession().createQuery(hql, Marker.class).setParameter("markerZdbID", gene.getZdbID()).setParameter("markerRelationshipType1", GENE_PRODUCES_TRANSCRIPT).setParameterList("markerRelationshipType2", markerRelationshipList).list();
    }

    public List<Marker> getGenesforTranscript(Marker tscript) {

        String hql = """
            select m from MarkerRelationship mr1,  Marker m
            where mr1.firstMarker.zdbID=m.zdbID
            and mr1.secondMarker.zdbID = :markerZdbID
            and mr1.type = :markerRelationshipType1
            """;

        return HibernateUtil.currentSession().createQuery(hql, Marker.class).setParameter("markerZdbID", tscript.getZdbID()).setParameter("markerRelationshipType1", GENE_PRODUCES_TRANSCRIPT).list();
    }


    public Marker getGeneforTranscript(Marker tscript) {

        String hql = """
            select m from MarkerRelationship mr1,  Marker m
            where mr1.firstMarker.zdbID=m.zdbID
            and mr1.secondMarker.zdbID = :markerZdbID
            and mr1.type = :markerRelationshipType1
            """;

        return (Marker) HibernateUtil.currentSession().createQuery(hql).setParameter("markerZdbID", tscript.getZdbID()).setParameter("markerRelationshipType1", GENE_PRODUCES_TRANSCRIPT).setMaxResults(1).uniqueResult();

    }

    public List<Marker> getCodingSequence(Marker gene) {

        String hql = """
            select m from MarkerRelationship mr1,  Marker m
            where mr1.secondMarker.zdbID=m.zdbID
            and mr1.firstMarker.zdbID = :markerZdbID
            and mr1.type = :markerRelationshipType1
            """;

        return currentSession().createQuery(hql, Marker.class).setParameter("markerZdbID", gene.getZdbID()).setParameter("markerRelationshipType1", MarkerRelationship.Type.CODING_SEQUENCE_OF).list();
    }

    @Override
    public SequenceTargetingReagent getSequenceTargetingReagent(String markerID) {
        Session session = currentSession();
        return session.get(SequenceTargetingReagent.class, markerID);
    }

    @Override
    public List<SequenceTargetingReagent> getSequenceTargetingReagents(List<String> markerIDs) {
        Session session = currentSession();
        String hql = "select str from SequenceTargetingReagent str where str.zdbID in (:markerIDs)";
        return session.createQuery(hql, SequenceTargetingReagent.class)
                .setParameterList("markerIDs", markerIDs)
                .list();
    }

    @Override
    public List<SequenceTargetingReagent> getRecentSequenceTargetingReagents(int limit) {
        Session session = currentSession();
        String sql = """
            select seq_mrkr_zdb_id
            from marker_sequence
            order by get_date_from_id(seq_mrkr_zdb_id, 'YYYY-MM-DD')  desc
            limit :limit
        """;
        List<String> markerIDs = session.createSQLQuery(sql).setParameter("limit", limit).list();
        List<SequenceTargetingReagent> markers = getSequenceTargetingReagents(markerIDs);

        //map the markers to the order of the markerIDs
        Map<String, SequenceTargetingReagent> markerMap = new HashMap<>();
        for (SequenceTargetingReagent marker : markers) {
            markerMap.put(marker.getZdbID(), marker);
        }
        List<SequenceTargetingReagent> orderedMarkers = new ArrayList<>();
        for (String markerID : markerIDs) {
            orderedMarkers.add(markerMap.get(markerID));
        }
        return orderedMarkers;
    }

    @Override
    public SequenceTargetingReagent getSequenceTargetingReagentBySequence(Marker.Type type, String sequence) {
        return getSequenceTargetingReagentBySequence(type, sequence, null);
    }

    @Override
    public SequenceTargetingReagent getSequenceTargetingReagentBySequence(Marker.Type type, String sequence1, String sequence2) {
        String hql = "select str from SequenceTargetingReagent str where str.markerType.name = :type ";
        if (sequence2 == null) {
            hql += "and str.sequence.sequence = :sequence1 ";
        } else {
            hql += "and ( (str.sequence.sequence = :sequence1 and str.sequence.secondSequence = :sequence2) or (str.sequence.sequence = :sequence2 and str.sequence.secondSequence = :sequence1) )";
        }

        Query<SequenceTargetingReagent> query = HibernateUtil.currentSession().createQuery(hql, SequenceTargetingReagent.class).setParameter("type", type.toString()).setParameter("sequence1", sequence1);
        if (sequence2 != null) {
            query.setParameter("sequence2", sequence2);
        }

        // a database constraint should be enforcing that STRs are unique by sequence. So for convenience
        // just return the first result or null.
        List<SequenceTargetingReagent> results = query.list();
        if (results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Genotype getStrainForTranscript(String zdbID) {

        // TODO: just use where clauses
        String hql = """ 
            select g from Genotype g, ProbeLibrary pl , MarkerRelationship mr, Clone c
            where pl=c.probeLibrary
            and g=pl.strain
            and mr.firstMarker=c
            and mr.secondMarker.zdbID = :zdbID
            and mr.type = :mrType
            """;

        return HibernateUtil.currentSession().createQuery(hql, Genotype.class)
            .setParameter("zdbID", zdbID)
            .setParameter("mrType", MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT)
            .uniqueResult();
    }


    @Override
    public List<LinkDisplay> getVegaGeneDBLinksTranscript(Marker gene, DisplayGroup.GroupName summaryPage) {

        if (!gene.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            log.error("method only to be used with GENEDOM: " + gene);
            return new ArrayList<>();
        }

        String sql = """
            SELECT DISTINCT fdbdt.fdbdt_data_type, dbl.dblink_length, dbl.dblink_linked_recid, dbl.dblink_acc_num,
            fdb.fdb_db_name, fdb.fdb_db_query, fdb.fdb_url_suffix, ra.recattrib_source_zdb_id, fdb.fdb_db_significance,
            dbl.dblink_zdb_id, fdbc.fdbcont_zdb_id
            FROM db_link dbl
            JOIN foreign_db_contains fdbc ON dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id
            JOIN foreign_db fdb ON fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
            JOIN foreign_db_data_type fdbdt ON fdbc.fdbcont_fdbdt_id=fdbdt.fdbdt_pk_id
            JOIN marker_relationship mr ON mr.mrel_mrkr_2_zdb_id=dbl.dblink_linked_recid
            LEFT OUTER JOIN record_attribution ra ON ra.recattrib_data_zdb_id=dbl.dblink_zdb_id
            WHERE mr.mrel_mrkr_1_zdb_id = :markerZdbId
            AND mr.mrel_type='gene produces transcript'
            AND fdb.fdb_db_name='VEGA'
            """;

        Query query = HibernateUtil.currentSession().createSQLQuery(sql).setParameter("markerZdbId", gene.getZdbID()).setResultTransformer(markerDBLinkTransformer);

        List<LinkDisplay> linkDisplay = markerDBLinkTransformer.transformList(query.list());
        return linkDisplay;
    }

    public List<LinkDisplay> getAllVegaGeneDBLinksTranscript() {

        String sql = """
            SELECT DISTINCT fdbdt.fdbdt_data_type, dbl.dblink_length, dbl.dblink_linked_recid, dbl.dblink_acc_num,
            fdb.fdb_db_name, fdb.fdb_db_query, fdb.fdb_url_suffix, ra.recattrib_source_zdb_id, fdb.fdb_db_significance,
            dbl.dblink_zdb_id, fdbc.fdbcont_zdb_id, mr.mrel_mrkr_1_zdb_id
            FROM db_link dbl
            JOIN foreign_db_contains fdbc ON dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id
            JOIN foreign_db fdb ON fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
            JOIN foreign_db_data_type fdbdt ON fdbc.fdbcont_fdbdt_id=fdbdt.fdbdt_pk_id
            JOIN marker_relationship mr ON mr.mrel_mrkr_2_zdb_id=dbl.dblink_linked_recid
            LEFT OUTER JOIN record_attribution ra ON ra.recattrib_data_zdb_id=dbl.dblink_zdb_id
            WHERE mr.mrel_type='gene produces transcript'
            AND upper(fdb.fdb_db_name) in ('VEGA','VEGA_TRANS')
            """;

        Query query = HibernateUtil.currentSession().createSQLQuery(sql).setResultTransformer(markerDBLinkTransformer);

        List list = query.list();
        List<LinkDisplay> linkDisplay = markerDBLinkTransformer.transformList(list);
        return linkDisplay;
    }

    /**
     * Retrieve all engineered region markers.
     */
    public List<Marker> getAllEngineeredRegions() {
        Session session = currentSession();
        String hql = "from Marker m where m.markerType.name = :name order by m.abbreviationOrder";
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("name", Marker.Type.EREGION.toString());
        return query.list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getClonesForGeneTranscripts(String zdbID) {

        String sql = """
            SELECT m.mrkr_abbrev, m.mrkr_zdb_id, m.mrkr_abbrev_order, mt.mrkrtype_type_display, mrt.mreltype_2_to_1_comments,
            '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , 
            sup.idsup_acc_num, src.srcurl_url, src.srcurl_display_text , mrct.mrel_zdb_id
            FROM marker_relationship mrgt
            JOIN marker_relationship mrct ON mrct.mrel_mrkr_2_zdb_id=mrgt.mrel_mrkr_2_zdb_id
            JOIN marker_relationship_type mrt ON mrt.mreltype_name=mrct.mrel_type
            JOIN marker m ON mrct.mrel_mrkr_1_zdb_id=m.mrkr_zdb_id
            JOIN marker_types mt ON m.mrkr_type = mt.marker_type
            LEFT OUTER JOIN record_attribution ra ON ra.recattrib_data_zdb_id=mrct.mrel_zdb_id
            LEFT OUTER JOIN int_data_supplier sup ON sup.idsup_data_zdb_id=mrct.mrel_mrkr_1_zdb_id
            LEFT OUTER JOIN source_url src ON sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id
            WHERE mrgt.mrel_mrkr_1_zdb_id = :markerZdbId
            AND mrct.mrel_type='clone contains transcript'
            AND mrgt.mrel_type='gene produces transcript'
            ORDER BY m.mrkr_type ASC , m.mrkr_abbrev_order ASC
            """;

        return HibernateUtil.currentSession().createSQLQuery(sql).setParameter("markerZdbId", zdbID).setResultTransformer(new MarkerRelationshipSupplierPresentationTransformer(true)).list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getWeakReferenceMarker(String zdbID, MarkerRelationship.Type type1, MarkerRelationship.Type type2) {
        return getWeakReferenceMarker(zdbID, type1, type2, null);
    }

    @Override
    public List<MarkerRelationshipPresentation> getWeakReferenceMarker(String zdbID, MarkerRelationship.Type type1, MarkerRelationship.Type type2, String resultType) {

        String sql = """
            SELECT m.mrkr_abbrev, m.mrkr_zdb_id, m.mrkr_abbrev_order, mt.mrkrtype_type_display, 
            mrt.mreltype_2_to_1_comments, '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' ,
            ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num, 
            src.srcurl_url, src.srcurl_display_text , mrct.mrel_zdb_id
            FROM marker_relationship mrgt
            JOIN marker_relationship mrct ON mrct.mrel_mrkr_2_zdb_id=mrgt.mrel_mrkr_2_zdb_id
            JOIN marker_relationship_type mrt ON mrt.mreltype_name=mrct.mrel_type
            JOIN marker m ON mrct.mrel_mrkr_1_zdb_id=m.mrkr_zdb_id
            JOIN marker_types mt ON m.mrkr_type = mt.marker_type
            LEFT OUTER JOIN record_attribution ra ON ra.recattrib_data_zdb_id=mrct.mrel_zdb_id
            LEFT OUTER JOIN int_data_supplier sup ON sup.idsup_data_zdb_id=mrct.mrel_mrkr_1_zdb_id
            LEFT OUTER JOIN source_url src ON sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id
            WHERE mrgt.mrel_mrkr_1_zdb_id = :markerZdbId
            AND mrct.mrel_type=:markerRelationshipType1
            AND mrgt.mrel_type=:markerRelationshipType2
            ORDER BY m.mrkr_type ASC , m.mrkr_abbrev_order ASC
            """;

        List<MarkerRelationshipPresentation> markers = HibernateUtil.currentSession().createSQLQuery(sql).setParameter("markerZdbId", zdbID).setParameter("markerRelationshipType1", type1.toString()).setParameter("markerRelationshipType2", type2.toString()).setResultTransformer(new MarkerRelationshipSupplierPresentationTransformer(true)).list();

        if (resultType != null) {
            for (MarkerRelationshipPresentation markerRelationshipPresentation : markers) {
                markerRelationshipPresentation.setRelationshipType(resultType);
            }
        }


        return markers;
    }

    /**
     * Retrieve list of mutants and transgenics being associated with a gene
     *
     * @param geneID gene ID
     * @return list of genotype (non-wt)
     */
    public List<Genotype> getMutantsAndTgsByGene(String geneID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct mutant from Genotype mutant, GenotypeFeature genoFtr, FeatureMarkerRelationship fmRel
            where fmRel.marker.zdbID = :geneID
            and fmRel.feature = genoFtr.feature
            and genoFtr.genotype = mutant
            and mutant.wildtype = 'f'
            order by mutant.nameOrder
            """;

        Query<Genotype> query = session.createQuery(hql, Genotype.class);
        query.setParameter("geneID", geneID);
        return query.list();
    }

    /**
     * Retrieve list of Feature objects with the features created with a TALEN or CRISPR
     *
     * @param sequenceTargetingReagent (TALEN or CRISPR)
     * @return list of Feature
     */
    public List<Feature> getFeaturesBySTR(Marker sequenceTargetingReagent) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct feat from Feature feat, FeatureMarkerRelationship fmRel
            where fmRel.marker = :str
            and fmRel.feature = feat
            and fmRel.type = 'created by'
            order by feat.name
            """;

        Query<Feature> query = session.createQuery(hql, Feature.class);
        query.setParameter("str", sequenceTargetingReagent);
        return query.list();
    }

    //TODO: separate query and transformer. THe latter should go into MarkerService
    @Override
    public List<SupplierLookupEntry> getSupplierNamesForString(String lookupString) {
        String hql = " select o FROM Organization o where lower(o.name) like :lookupString order by o.name  ";
        List<Organization> organizations = HibernateUtil.currentSession().createQuery(hql, Organization.class).setParameter("lookupString", "%" + lookupString.toLowerCase() + "%").list();
        return organizations.stream().map(org -> {
            SupplierLookupEntry supplierSuggestionList = new SupplierLookupEntry();
            supplierSuggestionList.setId(org.getZdbID());
            supplierSuggestionList.setLabel(org.getName());
            supplierSuggestionList.setValue(org.getName());
            return supplierSuggestionList;
        }).collect(Collectors.toList());
    }


    public List<String> getMarkerTypesforRelationship(String relType) {
        Session session = currentSession();
        String hql = "select mr.secondMarkerTypeGroup.name from MarkerRelationshipType mr where mr.name =:relType";
        Query<String> query = session.createQuery(hql, String.class);
        query.setParameter("relType", relType);
        String markerTypeGroup = query.uniqueResult();

        String sqlQuery = "SELECT  mtgrpmem_mrkr_type AS type FROM marker_type_group_member m WHERE m.mtgrpmem_mrkr_type_group = :markerTypeGroup";
        List<Tuple> markerTypes = HibernateUtil.currentSession().createNativeQuery(sqlQuery, Tuple.class).setParameter("markerTypeGroup", markerTypeGroup).list();
        return markerTypes.stream().map(tuple -> (String) tuple.get(0)).collect(toList());

    }

    public List<LookupEntry> getRelationshipTargetsForString(String lookupString) {
        return getMarkerSuggestionList(lookupString, Marker.TypeGroup.GENEDOM_AND_NTR, Marker.TypeGroup.SMALLSEG, Marker.TypeGroup.SMALLSEG_NO_ESTCDNA, Marker.TypeGroup.TRANSCRIPT);
    }


    public List<LookupEntry> getConstructComponentsForString(String lookupString, String zdbId) {


        String sqlQuery = """
            SELECT mrkr_abbrev AS abbrev, mrkr_type AS type FROM marker, record_attribution ra,marker_type_group_member m
            WHERE lower(mrkr_abbrev) LIKE :lookupString
            AND mrkr_type=m.mtgrpmem_mrkr_type AND m.mtgrpmem_mrkr_type_group IN ('CONSTRUCT_COMPONENTS')
            AND mrkr_zdb_id=ra.recattrib_data_zdb_id AND ra.recattrib_source_type = :standard AND ra.recattrib_source_zdb_id = :pubZdbID
            UNION
            SELECT cv_term_name AS abbrev,cv_name_definition AS type FROM controlled_vocabulary
            WHERE lower(cv_term_name) LIKE :lookupString
            """;


        List<Object[]> results = HibernateUtil.currentSession().createSQLQuery(sqlQuery).setParameter("lookupString", "%" + lookupString.toLowerCase() + "%").setParameter("pubZdbID", zdbId).setParameter("standard", RecordAttribution.SourceType.STANDARD.toString()).list();

        List<LookupEntry> targetGeneSuggestionList = new ArrayList<>();
        for (Object[] objects : results) {
            LookupEntry probe = new LookupEntry();
            probe.setLabel(objects[0] + " (" + objects[1] + ")");
            probe.setValue((String) objects[0]);
            targetGeneSuggestionList.add(probe);
        }
        return targetGeneSuggestionList;


    }


    public List<Marker> getRelatedMarkersForTypes(Marker marker, MarkerRelationship.Type... types) {
        Query<Marker> query = HibernateUtil.currentSession().createQuery("""
            select m from  Marker as m, MarkerRelationship as rel
            where rel.firstMarker = :marker  and rel.secondMarker = m and rel.type in :relationshipTypes
            order by rel.markerRelationshipType, m.markerType, m.abbreviationOrder
            """, Marker.class);
        query.setParameter("marker", marker);
        query.setParameterList("relationshipTypes", types);
        List<Marker> list = query.list();
        query = HibernateUtil.currentSession().createQuery("""
            select m from  Marker as m, MarkerRelationship as rel
            where rel.secondMarker = :marker  and rel.firstMarker = m and
            rel.type in :relationshipTypes
            order by rel.markerRelationshipType, m.markerType, m.abbreviationOrder
            """, Marker.class);
        query.setParameter("marker", marker);
        query.setParameterList("relationshipTypes", types);
        list.addAll(query.list());
        return list;
    }


    @Override
    public List<Marker> getRelatedGenesViaTranscript(Marker marker, MarkerRelationship.Type relType1, MarkerRelationship.Type relType2) {
        Query<Marker> query = HibernateUtil.currentSession().createQuery("""
            select distinct m from  Marker as m, MarkerRelationship as rel1,MarkerRelationship as rel2
            where rel1.firstMarker = :marker and
            rel1.type =:relType1 and
            rel1.secondMarker=rel2.secondMarker
            and rel2.type=:relType2 and
            rel2.firstMarker=m
            """, Marker.class);
        query.setParameter("marker", marker);
        query.setParameter("relType1", relType1);
        query.setParameter("relType2", relType2);

        List<Marker> list = query.list();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }


    @Override
    public Optional<Marker> getMarkerByFeature(Feature feature) {
        String hql = """
                     select fmr.marker from FeatureMarkerRelationship as fmr
                     where
                     fmr.feature = :feature
                     and fmr.type in (:types)
                     """;
        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameter("feature", feature);
        query.setParameterList("types", (new FeatureMarkerRelationshipTypeEnum[]{FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF}));
        List<Marker> list = query.list();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public String getAccessionNumber(Marker marker, Database.AvailableAbbrev database) {
        String hql = "from MarkerDBLink " +
                     "where referenceDatabase.primaryBlastDatabase.abbrev = :database " +
                     "and marker = :marker ";
        Query<MarkerDBLink> query = HibernateUtil.currentSession().createQuery(hql, MarkerDBLink.class);
        query.setParameter("database", database);
        query.setParameter("marker", marker);
        List<MarkerDBLink> dbLinks = query.list();
        if (CollectionUtils.isEmpty(dbLinks)) {
            return null;
        }
        if (CollectionUtils.isNotEmpty(dbLinks) && dbLinks.size() > 1) {
            log.error("More than one accession number found for " + marker.getAbbreviation() + " and Database " + database.toString());
        }
        return dbLinks.get(0).getAccessionNumber();
    }

    @Override
    public int deleteMarkerDBLinksNotInList(ReferenceDatabase referenceDatabase, List<String> ids) {
        List<MarkerDBLink> dbLinks = getMarkerDBLinksNotInList(referenceDatabase, ids);
        if (CollectionUtils.isEmpty(dbLinks)) {
            return 0;
        }
        return deleteMarkerDBLinksFromList(dbLinks);
    }

    @Override
    public int deleteMarkerDBLinksByIDList(ReferenceDatabase referenceDatabase, List<String> ids) {
        List<MarkerDBLink> dbLinks = getMarkerDBLinksInList(referenceDatabase, ids);
        if (CollectionUtils.isEmpty(dbLinks)) {
            return 0;
        }
        return deleteMarkerDBLinksFromList(dbLinks);
    }

    @Override
    public int addMarkerDBLinks(final ReferenceDatabase referenceDatabase, List<String> geneIdList) {
        String hql = "from MarkerDBLink where referenceDatabase = :database and accessionNumber in (:list) ";
        Query<MarkerDBLink> query = HibernateUtil.currentSession().createQuery(hql, MarkerDBLink.class);
        query.setParameter("database", referenceDatabase);
        query.setParameter("list", geneIdList);
        List<MarkerDBLink> dbLinks = query.list();
        if (CollectionUtils.isEmpty(dbLinks)) {
            Query<Marker> query1 = currentSession().createQuery("from Marker where zdbID in (:idList)", Marker.class);
            query1.setParameter("idList", geneIdList);
            List<Marker> markerList = query1.list();
            markerList.forEach(gene -> {
                MarkerDBLink signafishLink = new MarkerDBLink();
                signafishLink.setMarker(gene);
                signafishLink.setLinkInfo(String.format("Uncurated: signafish load for %tF", new Date()));
                signafishLink.setAccessionNumber(gene.getZdbID());
                signafishLink.setAccessionNumberDisplay(gene.getZdbID());
                signafishLink.setReferenceDatabase(referenceDatabase);
                HibernateUtil.currentSession().save(signafishLink);
                RecordAttribution recAttr = new RecordAttribution();
                recAttr.setDataZdbID(signafishLink.getZdbID());
                recAttr.setSourceZdbID("ZDB-PUB-160316-7");
                recAttr.setSourceType(RecordAttribution.SourceType.STANDARD);
                HibernateUtil.currentSession().save(recAttr);
            });
        }
        return 0;
    }

    public Long getSignafishLinkCount(ReferenceDatabase referenceDatabase) {
        String hql = "select count (m) from MarkerDBLink m where m.referenceDatabase = :database ";
        Query<Number> query = currentSession().createQuery(hql, Number.class);
        query.setParameter("database", referenceDatabase);
        return (Long) query.uniqueResult();
    }


    //then determine the aliases for the construct and set the alias on the ConstructComponentPresentation object.
    //delimit aliases by a ","
    public ConstructComponentPresentation getConstructComponentsForDisplay(String zdbID) {
        ConstructComponentPresentation ccp = new ConstructComponentPresentation();
        ccp.setConstruct(getConstructByID(zdbID));
        ccp.setConstructComponent(getConstructComponent(zdbID));


        return ccp;

    }

    //    changed this code
    public List<ConstructComponentPresentation> getConstructComponents(String zdbID) {
        String sqlCount = " SELECT MAX(cc_cassette_number) FROM construct_component WHERE cc_construct_zdb_id=:zdbID ";
        Query query = currentSession().createSQLQuery(sqlCount);
        query.setParameter("zdbID", zdbID);

        String sql = " SELECT a.construct_name,a.construct_comments,a.construct_zdb_id FROM construct a  WHERE a.construct_zdb_id =:zdbID ";
        return HibernateUtil.currentSession().createSQLQuery(sql).setParameter("zdbID", zdbID).setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public ConstructComponentPresentation transformTuple(Object[] tuple, String[] aliases) {
                ConstructComponentPresentation constructComponentPresentation = new ConstructComponentPresentation();

                if (tuple[1] != null) {
                    constructComponentPresentation.setConstructComments(tuple[1].toString());
                }

                constructComponentPresentation.setConstructZdbID(tuple[2].toString());

                constructComponentPresentation.setConstructCuratorNotes(DTOMarkerService.getCuratorNoteDTOs(getMarkerByID(tuple[2].toString())));
                constructComponentPresentation.setConstructAliases(getPreviousNamesLight(getMarkerByID(tuple[2].toString())));
                constructComponentPresentation.setConstructSequences(DTOMarkerService.getSupportingSequenceDTOs(getMarkerByID(tuple[2].toString())));

                return constructComponentPresentation;
            }
        }).list();

    }

    @Override
    public List<LookupEntry> getMarkerSuggestionList(String lookupString, Marker.TypeGroup... groups) {
        List<MarkerType> markerTypes = new ArrayList<>();
        for (Marker.TypeGroup group : groups) {
            markerTypes.addAll(getMarkerTypesByGroup(group));
        }

        String hql = " select marker from Marker marker where lower(marker.abbreviation) like :lookupString ";
        if (!markerTypes.isEmpty()) {
            hql += "and marker.markerType in (:markerType)  ";
        }
        hql += "order by marker.abbreviation  ";

        return HibernateUtil.currentSession().createQuery(hql).setParameter("lookupString", "%" + lookupString.toLowerCase() + "%").setParameterList("markerType", markerTypes).setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] targetGeneAbrevs) {
                Marker targetGene = (Marker) tuple[0];
                LookupEntry targetGeneSuggestionList = new LookupEntry();
                targetGeneSuggestionList.setId(targetGene.getZdbID());
                targetGeneSuggestionList.setLabel(targetGene.getAbbreviation());
                targetGeneSuggestionList.setValue(targetGene.getAbbreviation());
                return targetGeneSuggestionList;
            }
        }).list();
    }

    @Override
    public List<LookupEntry> getGeneSuggestionList(String lookupString) {
        return getMarkerSuggestionList(lookupString, Marker.TypeGroup.GENEDOM_AND_NTR);
    }

    @Override
    public List<TranscriptPresentation> getTranscriptsForGeneId(String geneZdbId) {

        String hql = """
            select markerRelationship.secondMarker.zdbID, markerRelationship.secondMarker.name
            from MarkerRelationship markerRelationship where markerRelationship.type = :type
            and markerRelationship.firstMarker.zdbID = :geneID
            order by markerRelationship.secondMarker.abbreviationOrder
            """;

        return HibernateUtil.currentSession().createQuery(hql).setParameter("type", GENE_PRODUCES_TRANSCRIPT).setParameter("geneID", geneZdbId).setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public TranscriptPresentation transformTuple(Object[] tuple, String[] aliases) {
                TranscriptPresentation transcriptPresentation = new TranscriptPresentation();
                transcriptPresentation.setZdbID(tuple[0].toString());
                transcriptPresentation.setName(tuple[1].toString());
                return transcriptPresentation;
            }

        }).list();
    }

    @Override
    public List<SequenceTargetingReagentLookupEntry> getSequenceTargetingReagentForString(String lookupString, String type) {
        String hql = " select mo from Marker mo " +
                     "where " +
                     "lower(mo.abbreviation) like :lookupString " +
                     "and mo.markerType.name = :type " +
                     "order by mo.abbreviation  ";
        return HibernateUtil.currentSession().createQuery(hql)
            .setParameter("lookupString", "%" + lookupString.toLowerCase() + "%")
            .setParameter("type", type)
            .setResultTransformer(new BasicTransformerAdapter() {
                @Override
                public Object transformTuple(Object[] tuple, String[] sequenceTargetingReagents) {
                    Marker str = (Marker) tuple[0];
                    SequenceTargetingReagentLookupEntry strSuggestionList = new SequenceTargetingReagentLookupEntry();
                    strSuggestionList.setId(str.getZdbID());
                    strSuggestionList.setLabel(str.getAbbreviation());
                    strSuggestionList.setValue(str.getAbbreviation());
                    return strSuggestionList;
                }
            })
            .list()
            ;
    }

    @Override
    public List<TargetGenePresentation> getTargetGenesForSequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        if (sequenceTargetingReagent == null) {
            return null;
        }

        String hql = """
            select markerRelationship.secondMarker.zdbID, markerRelationship.secondMarker.abbreviation from MarkerRelationship markerRelationship
            where markerRelationship.type = :type
            and markerRelationship.firstMarker.zdbID = :sequenceTargetingReagentID
            order by markerRelationship.secondMarker.abbreviationOrder
            """;

        return HibernateUtil.currentSession().createQuery(hql).setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE).setParameter("sequenceTargetingReagentID", sequenceTargetingReagent.getZdbID()).setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public TargetGenePresentation transformTuple(Object[] tuple, String[] aliases) {
                TargetGenePresentation targetGenePresentation = new TargetGenePresentation();
                targetGenePresentation.setZdbID(tuple[0].toString());
                targetGenePresentation.setSymbol(tuple[1].toString());
                return targetGenePresentation;
            }

        }).list();
    }

    public List<Marker> getSecondMarkersByFirstMarkerAndMarkerRelationshipType(Marker firstMarker, MarkerRelationship.Type relationshipType) {
        if (firstMarker == null) {
            return null;
        }

        String hql = "select rel.secondMarker from MarkerRelationship as rel  " +
                     "where rel.firstMarker = :firstMarker " +
                     "and rel.type = :type " +
                     "order by rel.secondMarker.abbreviationOrder";

        Session session = currentSession();
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("firstMarker", firstMarker);
        query.setParameter("type", relationshipType);

        return query.list();
    }

    public List<MarkerRelationship> getMarkerRelationshipBySecondMarker(Marker secondMarker) {
        if (secondMarker == null) {
            return null;
        }

        String hql = "select rel from MarkerRelationship as rel  " +
                     "where rel.secondMarker = :secondMarker " +
                     "order by rel.firstMarker.abbreviationOrder";

        Session session = currentSession();
        Query<MarkerRelationship> query = session.createQuery(hql, MarkerRelationship.class);
        query.setParameter("secondMarker", secondMarker);
        return query.list();
    }

    /**
     * Retrieve marker that has relationships for a given set of markers.
     * The marker relationships must contain all the given markers and no additional markers.
     *
     * @param secondMarkerAbbreviations
     * @return
     */
    @Override
    public List<Marker> getMarkerWithRelationshipsBySecondMarkers(Set<String> secondMarkerAbbreviations) {
        if (secondMarkerAbbreviations == null || secondMarkerAbbreviations.size() == 0) {
            return null;
        }

        long size = secondMarkerAbbreviations.size();

        String hql = """
                from Marker m where m.zdbID in (
                    select rel.firstMarker.zdbID  
                    from MarkerRelationship as rel   
                    where rel.secondMarker.abbreviation IN (:secondMarkers)
                    group by rel.firstMarker.zdbID
                   having count(distinct rel.secondMarker) = :secondMarkerCount
               )  
            """;

        Session session = currentSession();
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("secondMarkers", secondMarkerAbbreviations);
        query.setParameter("secondMarkerCount", size);
        return query.list();
    }

    @Override
    public PaginationResult<Marker> getRelatedMarker(Marker marker, Set<MarkerRelationship.Type> types, PaginationBean paginationBean) {
        if (marker == null) {
            return null;
        }
        // second related elements
        String hql = "select rel.secondMarker from MarkerRelationship as rel  " +
                     "where rel.firstMarker = :firstMarker " +
                     "and rel.type in (:typeList) " +
                     "order by rel.secondMarker.abbreviationOrder";

        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameter("firstMarker", marker);
        query.setParameterList("typeList", types);

        PaginationResult<Marker> markerPaginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(paginationBean.getMaxDisplayRecordsInteger(), query.scroll());

        // second related elements
        hql = "select rel.firstMarker from MarkerRelationship as rel  " +
              "where rel.secondMarker = :secondMarker " +
              "and rel.type in (:typeList) " +
              "order by rel.firstMarker.abbreviationOrder";

        Query<Marker> query2 = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query2.setParameter("secondMarker", marker);
        query2.setParameterList("typeList", types);

        PaginationResult<Marker> markerPaginationResult1 = PaginationResultFactory.createResultFromScrollableResultAndClose(paginationBean.getMaxDisplayRecordsInteger(), query2.scroll());
        markerPaginationResult.add(markerPaginationResult1);
        return markerPaginationResult;
    }

    @Override
    public List<OmimPhenotype> getOmimPhenotype(Marker marker) {
        Session session = HibernateUtil.currentSession();
        String sql = "FROM OmimPhenotype " +
                     "WHERE ortholog.zebrafishGene = :gene " +
                     "AND ortholog.ncbiOtherSpeciesGene.organism.commonName = :organism ";

        Query<OmimPhenotype> query = session.createQuery(sql, OmimPhenotype.class);
        query.setParameter("gene", marker);
        query.setParameter("organism", Species.Type.HUMAN.toString());
        return query.list();
    }

    Map<String, List<Marker>> zfinOrthologMap = new HashMap<>();

    public List<Marker> getZfinOrtholog(String humanAbbrev) {
        if (zfinOrthologMap.size() > 0) {
            return zfinOrthologMap.get(humanAbbrev);
        }

        Session session = HibernateUtil.currentSession();
        String sql = """
            select ortho FROM Ortholog as ortho
            join fetch ortho.zebrafishGene
            join fetch ortho.ncbiOtherSpeciesGene
            WHERE ortho.organism.commonName = :organism
            """;

        Query<Ortholog> query = session.createQuery(sql, Ortholog.class);
        query.setParameter("organism", Species.Type.HUMAN.toString());
        List<Ortholog> list = query.list();

        zfinOrthologMap = list.stream().collect(groupingBy(ortholog -> ortholog.getNcbiOtherSpeciesGene().getAbbreviation(), Collectors.mapping(Ortholog::getZebrafishGene, toList())));
        return zfinOrthologMap.get(humanAbbrev);
    }

    public void addConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, Marker marker, String pubID) {
        //      HibernateUtil.createTransaction();

        if (!promMarker.isEmpty()) {
            for (Marker promMarkers : promMarker) {
                MarkerRelationship mRel = getMarkerRelationship(marker, promMarkers, MarkerRelationship.Type.PROMOTER_OF);
                if (mRel == null) {
                    MarkerRelationship promMRel = new MarkerRelationship();
                    promMRel.setFirstMarker(marker);
                    promMRel.setSecondMarker(promMarkers);
                    promMRel.setType(MarkerRelationship.Type.PROMOTER_OF);
                    currentSession().save(promMRel);
                    addMarkerRelationshipAttribution(promMRel, pr.getPublication(pubID));
                }
                // ir.insertRecordAttribution(promMRel.getZdbID(),pubID);

            }
        }
        if (!codingMarker.isEmpty()) {
            for (Marker codingMarkers : codingMarker) {
                MarkerRelationship mRel = getMarkerRelationship(marker, codingMarkers, MarkerRelationship.Type.CODING_SEQUENCE_OF);
                if (mRel == null) {
                    MarkerRelationship codingRel = new MarkerRelationship();
                    codingRel.setFirstMarker(marker);
                    codingRel.setSecondMarker(codingMarkers);
                    codingRel.setType(MarkerRelationship.Type.CODING_SEQUENCE_OF);
                    currentSession().save(codingRel);
                    addMarkerRelationshipAttribution(codingRel, pr.getPublication(pubID));
                }
                //    ir.insertRecordAttribution(codingRel.getZdbID(),pubID);

                //

            }
        }
        currentSession().flush();
        //       flushAndCommitCurrentSession();

    }

    @Override
    public void addConstructComponent(int cassetteNumber, int ccOrder, String constructId, String ccValue, ConstructComponent.Type type, String ccCategory, String ccZdbID) {
        ConstructComponent ccs = new ConstructComponent();
        ccs.setComponentCassetteNum(cassetteNumber);
        ccs.setComponentOrder(ccOrder);
        ccs.setConstructZdbID(constructId);
        ccs.setComponentValue(ccValue);
        ccs.setType(type);
        ccs.setComponentCategory(ccCategory);
        ccs.setComponentZdbID(ccZdbID);
        currentSession().save(ccs);
        currentSession().flush();
    }

    public void updateCuratorNote(Marker marker, DataNote note, String newNote) {
        if (!marker.getDataNotes().contains(note)) {
            log.error("Note " + note.getZdbID() + " not associated with marker " + marker.getZdbID());
            return;
        }

        String oldNote = note.getNote();
        note.setNote(newNote);
        currentSession().save(note);
        InfrastructureService.insertUpdate(marker, "curator note", oldNote, newNote);
    }

    public void removeCuratorNote(Marker marker, DataNote note) {
        //   log.info("remove curator note: " + noteDTO.getNoteData() + " - " + noteDTO.getZdbID());

        Set<DataNote> dataNotes = marker.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            if (dataNote.getZdbID().equals(note.getZdbID())) {
                InfrastructureService.insertUpdate(marker, "removed curator note " + dataNote.getNote());
                HibernateUtil.currentSession().delete(dataNote);
                return;
            }
        }
        log.error("note not found with zdbID: " + note.getZdbID());
    }

    public int getCrisprCount(String geneAbbrev) {
        Session session = currentSession();
        String hql = """
            select rel.firstMarker from MarkerRelationship as rel
            where rel.secondMarker.zdbID = :geneAbbrev and rel.type = :type
            and rel.firstMarker.markerType like '%CRISPR%'  order by rel.secondMarker.abbreviationOrder
            """;
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("geneAbbrev", geneAbbrev);
        query.setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);

        List<Marker> targetGenes = query.list();
        return targetGenes.size();
    }

    @Override
    public MarkerHistory getMarkerHistory(String zdbID) {
        return (MarkerHistory) HibernateUtil.currentSession().load(MarkerHistory.class, zdbID);
    }

    public DBLink addDBLinkWithLenth(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID, int length) {
        if (length < 1) {
            return addDBLink(marker, accessionNumber, refdb, attributionZdbID);
        }
        MarkerDBLink mdb = new MarkerDBLink();
        mdb.setMarker(marker);
        mdb.setAccessionNumber(accessionNumber);
        mdb.setReferenceDatabase(refdb);
        mdb.setLength(length);
        Set<MarkerDBLink> markerDBLinks = marker.getDbLinks();
        if (markerDBLinks == null) {
            markerDBLinks = new HashSet<>();
            markerDBLinks.add(mdb);
            marker.setDbLinks(markerDBLinks);
        } else {
            marker.getDbLinks().add(mdb);
        }
        currentSession().save(mdb);
        if (StringUtils.isNotEmpty(attributionZdbID)) {
            infrastructureRepository.insertRecordAttribution(mdb.getZdbID(), attributionZdbID);
        }

        String updateComment = "Adding dblink " + mdb.getReferenceDatabase().getForeignDB().getDisplayName() + ":" + mdb.getAccessionNumber();
        updateComment += StringUtils.isNotBlank(attributionZdbID) ? (" with attribution " + attributionZdbID) : " without attribution";
        InfrastructureService.insertUpdate(marker, updateComment);

        return mdb;
    }


    @Override
    public List<Marker> getMarkerByGroup(Marker.TypeGroup group, int number) {
        MarkerTypeGroup type = getMarkerTypeGroupByName(group.name());
        String hql = "select marker from Marker as marker ";
        // only do the eager join when retrieving all records otherwise the
        // number of records setting won't work, i.e. Hibernate would not set the
        // max number of records.
        if (number < 1) {
            hql += "left join fetch marker.dbLinks ";
            hql += "left join fetch marker.aliases ";
        }
        hql += "where marker.markerType.name in (:names) and mrkr_abbrev not like :withdrawn ";
        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameterList("names", type.getTypeStrings());
        query.setParameter("withdrawn", Marker.WITHDRAWN + "%");
        if (number > 0) {
            query.setFirstResult(0);
            query.setMaxResults(number);
        }
        return query.setResultTransformer(DISTINCT_ROOT_ENTITY).list();
    }

    @Override
    public List<Marker> getWithdrawnMarkers() {
        String hql = "from Marker where mrkr_abbrev like :withdrawn ";
        Query<Marker> query = HibernateUtil.currentSession().createQuery(hql, Marker.class);
        query.setParameter("withdrawn", Marker.WITHDRAWN + "%");
        return query.list();
    }


    @Override
    public Map<String, GenericTerm> getSoTermMapping() {
        String hql = "select so from ZfinSoTerm so left join fetch so.soTerm";

        List<ZfinSoTerm> terms = HibernateUtil.currentSession().createQuery(hql, ZfinSoTerm.class).list();
        Map<String, GenericTerm> map = new HashMap<>(terms.size());
        for (ZfinSoTerm term : terms) {
            map.put(term.getEntityName(), term.getSoTerm());
        }
        return map;
    }

    @Override
    public void copyStrSequence(SequenceTargetingReagent str1, SequenceTargetingReagent str2) {
        String seq1 = str1.getSequence().getSequence();
        HibernateUtil.currentSession().createSQLQuery(
                "UPDATE marker_sequence " +
                "SET seq_sequence = :seq_sequence " +
                "WHERE seq_mrkr_zdb_id = :zdbID ")
            .setParameter("seq_sequence", seq1)
            .setParameter("zdbID", str2.getZdbID())
            .executeUpdate();
        String seq2;
        if (str1.getSequence().getSecondSequence() != null) {
            seq2 = str1.getSequence().getSecondSequence();
            HibernateUtil.currentSession().createSQLQuery(
                    "UPDATE marker_sequence " +
                    "SET seq_sequence_2 = :seq_sequence_2 " +
                    "WHERE seq_mrkr_zdb_id = :zdbID ")
                .setParameter("seq_sequence_2", seq2)
                .setParameter("zdbID", str2.getZdbID())
                .executeUpdate();
        }
    }

    @Override
    public List<LookupEntry> getRegionListForString(String lookupString, String type) {
        String hql = " select region from Marker region " +
                     "where " +
                     "lower(region.abbreviation) like :lookupString " +
                     "and region.markerType.name = :type " +
                     "order by region.abbreviation  ";
        return HibernateUtil.currentSession().createQuery(hql)
            .setParameter("lookupString", "%" + lookupString.toLowerCase() + "%")
            .setParameter("type", type)
            .setResultTransformer(new BasicTransformerAdapter() {
                @Override
                public Object transformTuple(Object[] tuple, String[] regions) {
                    Marker reg = (Marker) tuple[0];
                    LookupEntry regionSuggestionList = new LookupEntry();
                    regionSuggestionList.setId(reg.getZdbID());
                    regionSuggestionList.setLabel(reg.getAbbreviation());
                    regionSuggestionList.setValue(reg.getAbbreviation());
                    return regionSuggestionList;
                }
            })
            .list()
            ;
    }

    @Override
    public ZfinSoTerm getSoIdByMarkerType(String markerType) {
        String hql = " select so from ZfinSoTerm so where so.entityName = :markerType ";

        Query<ZfinSoTerm> query = HibernateUtil.currentSession().createQuery(hql, ZfinSoTerm.class);
        query.setParameter("markerType", markerType);
        return query.uniqueResult();
    }

    @Override
    public String getDbsnps(String cloneId) {
        String sql = "SELECT snpd_rs_acc_num FROM snp_download WHERE snpd_mrkr_zdb_id = :cloneId ";

        List<Tuple> dbsnps = HibernateUtil.currentSession().createNativeQuery(sql, Tuple.class).setParameter("cloneId", cloneId).list();
        StringBuilder sb = new StringBuilder();
        for (Tuple dbsnp : dbsnps) {
            sb.append(dbsnp.get(0));
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public List<AntibodyLookupEntry> getAntibodyForString(String lookupString, String type) {
        String hql = " select ab from Marker ab " +
                     "where " +
                     "lower(ab.abbreviation) like :lookupString " +
                     "and ab.markerType.name = :type " +
                     "order by ab.abbreviation  ";
        return HibernateUtil.currentSession().createQuery(hql)
            .setParameter("lookupString", "%" + lookupString.toLowerCase() + "%")
            .setParameter("type", type)
            .setResultTransformer(new BasicTransformerAdapter() {
                @Override
                public Object transformTuple(Object[] tuple, String[] sequenceTargetingReagents) {
                    Marker antibody = (Marker) tuple[0];
                    AntibodyLookupEntry abSuggestionList = new AntibodyLookupEntry();
                    abSuggestionList.setId(antibody.getZdbID());
                    abSuggestionList.setLabel(antibody.getAbbreviation());
                    abSuggestionList.setValue(antibody.getAbbreviation());
                    return abSuggestionList;
                }
            })
            .list()
            ;
    }

    @Override
    public Set<Antibody> getAntibodies(Set<String> antibodyIds) {

        String hql = " select ab from Antibody ab where ab.zdbID in (:IDs) order by ab.abbreviation  ";
        Query<Antibody> query = HibernateUtil.currentSession().createQuery(hql, Antibody.class);
        query.setParameterList("IDs", antibodyIds);
        return new HashSet<>(query.list());
    }

    public TranscriptSequence getTranscriptSequence(Transcript transcript) {
        Session session = HibernateUtil.currentSession();
        String hqlSeq = " select ts from  TranscriptSequence ts where ts.zdbID =:tsID";
        Query<TranscriptSequence> query = session.createQuery(hqlSeq, TranscriptSequence.class);
        query.setParameter("tsID", transcript.getZdbID());
        return query.uniqueResult();
    }

    @Override
    public MarkerRelationshipType getMarkerRelationshipType(String name) {
        return HibernateUtil.currentSession().get(MarkerRelationshipType.class, name);
    }

    @Override
    public List<FluorescentProtein> getAllFluorescentProteins() {
        Session session = HibernateUtil.currentSession();
        String hql = "select protein from FluorescentProtein protein order by size(protein.efgs) desc ";
        Query<FluorescentProtein> query = session.createQuery(hql, FluorescentProtein.class);
        return query.getResultList();
    }

    @Override
    public List<FluorescentMarker> getAllFluorescentEfgs() {
        Session session = HibernateUtil.currentSession();
        String hql = "select efl from FluorescentMarker efl  where efl.efg.markerType.name = :type order by efl.efg.abbreviation";
        Query<FluorescentMarker> query = session.createQuery(hql, FluorescentMarker.class);
        query.setParameter("type", Marker.Type.EFG.toString());
        return query.getResultList();
    }

    @Override
    public List<FluorescentMarker> getAllFluorescentConstructs() {
        Session session = HibernateUtil.currentSession();
        String hql = "select efl from FluorescentMarker efl where efl.efg.markerType.name in (:type)";
        Query<FluorescentMarker> query = session.createQuery(hql, FluorescentMarker.class);
        List<String> types = List.of(Marker.Type.ETCONSTRCT.toString(), Marker.Type.GTCONSTRCT.toString(), Marker.Type.TGCONSTRCT.toString());
        query.setParameterList("type", types);
        return query.getResultList();
    }

    public List<String> getProblemTypes() {
        String hql = " select c.type from CloneProblem c ";
        Session session = currentSession();
        return session.createQuery(hql, String.class).list();
    }

    @Override
    public List<FluorescentProtein> getFluorescentProteins(String query) {
        String hql = " from FluorescentProtein where lower(name) like :query or lower(uuid) = :exactQuery";
        Session session = currentSession();
        Query<FluorescentProtein> query1 = session.createQuery(hql, FluorescentProtein.class);
        query1.setParameter("query", "%" + query.toLowerCase() + "%");
        query1.setParameter("exactQuery", query.toLowerCase());
        return query1.list();
    }

    @Override
    public FluorescentProtein getFluorescentProteinByName(String name) {
        String hql = " from FluorescentProtein where name like :name ";
        Session session = currentSession();
        Query<FluorescentProtein> query = session.createQuery(hql, FluorescentProtein.class);
        query.setParameter("name", name);
        List<FluorescentProtein> list = query.list();
        if (list.size() > 1) {
            log.error("More than one fluorescent protein found for " + name);
        }
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void addFluorescentProtein(FluorescentProtein newProtein) {
        HibernateUtil.currentSession().save(newProtein);
    }

    @Override
    public FluorescentProtein getFluorescentProtein(Long identifier) {
        return HibernateUtil.currentSession().get(FluorescentProtein.class, identifier);
    }

    private final Map<Marker, List<Transcript>> markerTranscriptMap = new HashMap<>();

    @Override
    public void updateMarkerName(String markerZdbID, String newName) {
        Session session = HibernateUtil.currentSession();
        Marker marker = getMarkerByID(markerZdbID);
        marker.setName(newName);
        session.update(marker);
    }

    public Map<Marker, List<Transcript>> getAllTranscripts(Pagination pagination) {
        if (!markerTranscriptMap.isEmpty()) {
            return markerTranscriptMap;
        }

        String hql = """
            select mrel, transcript from MarkerRelationship as mrel
            LEFT OUTER JOIN mrel.secondMarker as transcript
            LEFT OUTER JOIN transcript.status as transcriptStatus
            where mrel.type = :type
            """;
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }

        Query<Tuple> query = HibernateUtil.currentSession().createQuery(hql, Tuple.class);
        query.setParameter("type", GENE_PRODUCES_TRANSCRIPT.toString());
        List<Tuple> response = query.getResultList();
        response.forEach(tuple -> {
            List<Transcript> list = markerTranscriptMap.computeIfAbsent(tuple.get(0, MarkerRelationship.class).getFirstMarker(), marker -> new ArrayList<>());
            list.add(tuple.get(1, Transcript.class));
        });
        return markerTranscriptMap;
    }

    private static Map<Marker, List<MarkerDBLink>> markerListMap = null;

    @Override
    public Map<Marker, List<MarkerDBLink>> getAllPlasmids(DisplayGroup.GroupName... groupNames) {
        if (markerListMap != null) {
            return markerListMap;
        }
        String hql = """
            select link from MarkerDBLink link, DisplayGroupMember mem, DisplayGroup g
            where mem.displayGroup = g
            AND mem in elements(link.referenceDatabase.displayGroupMembers)
             """;
        if (groupNames != null) {
            hql += "AND g.groupName in (:displayGroup)";
        }
        Query<MarkerDBLink> query = HibernateUtil.currentSession().createQuery(hql, MarkerDBLink.class);
        if (groupNames != null) {
            query.setParameterList("displayGroup", Arrays.asList(groupNames));
        }
        List<MarkerDBLink> links = query.list();
        markerListMap = links.stream().collect(groupingBy(MarkerDBLink::getMarker));
        return markerListMap;
    }

    private Map<Marker, List<TranscriptBean>> markerTranscriptBeanMap = null;

    @Override
    public Map<Marker, List<TranscriptBean>> getAllTranscriptBeans(Pagination pagination) {
        if (markerTranscriptBeanMap != null) {
            return markerTranscriptBeanMap;
        }
        String hql = """
            from Transcript
            """;
        List<Transcript> transcriptList = HibernateUtil.currentSession().createQuery(hql, Transcript.class).getResultList();

        markerTranscriptBeanMap = transcriptList.stream().map(transcript -> {
            TranscriptBean bean = new TranscriptBean();
            bean.setTranscript(transcript);
            bean.setRelatedGenes(TranscriptService.getRelatedGenes(transcript));
            bean.setStrain(transcript.getStrain());
            bean.setNonReferenceStrains(TranscriptService.getNonReferenceStrainsForTranscript(transcript));
            return bean;
        }).collect(groupingBy(TranscriptBean::getTranscript));
        return markerTranscriptBeanMap;
    }

    private int deleteMarkerDBLinksFromList(List<MarkerDBLink> dbLinks) {
        List<String> ids = dbLinks.stream().map(MarkerDBLink::getZdbID).toList();
        Session session = HibernateUtil.currentSession();

        CriteriaDelete<ActiveData> delete = session.getCriteriaBuilder().createCriteriaDelete(ActiveData.class);
        delete.where(delete.from(ActiveData.class).get("zdbID").in(ids));

        return session.createQuery(delete).executeUpdate();
    }

    private List<MarkerDBLink> getMarkerDBLinksNotInList(ReferenceDatabase referenceDatabase, List<String> ids) {
        Session session = currentSession();
        CriteriaBuilder cb = HibernateUtil.currentSession().getCriteriaBuilder();
        CriteriaQuery<MarkerDBLink> query = cb.createQuery(MarkerDBLink.class);
        Root<MarkerDBLink> root = query.from(MarkerDBLink.class);
        query.where(cb.and(cb.equal(root.get("referenceDatabase"), referenceDatabase), root.get("accessionNumber").in(ids).not()));

        return session.createQuery(query).getResultList();
    }

    private List<MarkerDBLink> getMarkerDBLinksInList(ReferenceDatabase referenceDatabase, List<String> ids) {
        Session session = currentSession();
        CriteriaBuilder cb = HibernateUtil.currentSession().getCriteriaBuilder();
        CriteriaQuery<MarkerDBLink> query = cb.createQuery(MarkerDBLink.class);
        Root<MarkerDBLink> root = query.from(MarkerDBLink.class);
        query.where(cb.and(cb.equal(root.get("referenceDatabase"), referenceDatabase), root.get("accessionNumber").in(ids)));

        return session.createQuery(query).getResultList();
    }

}

