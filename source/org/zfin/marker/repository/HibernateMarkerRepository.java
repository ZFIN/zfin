package org.zfin.marker.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;
import org.zfin.ExternalNote;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.presentation.ConstructComponentPresentation;
import org.zfin.database.DbSystemUtil;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureFigure;
import org.zfin.expression.Image;
import org.zfin.expression.TextOnlyFigure;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.*;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerRelationshipPresentationTransformer;
import org.zfin.marker.service.MarkerRelationshipSupplierPresentationTransformer;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
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
import org.zfin.util.NumberAwareStringComparator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;


@Repository
public class HibernateMarkerRepository implements MarkerRepository {

    private static Logger logger = Logger.getLogger(HibernateMarkerRepository.class);
    private final static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private final static PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    // utilities
    private MarkerDBLinksTransformer markerDBLinkTransformer = new MarkerDBLinksTransformer();

    public Marker getMarker(Marker marker) {
        Session session = currentSession();
        return (Marker) session.get(Marker.class, marker.getZdbID());
    }

    public Marker getMarkerByID(String zdbID) {
        Session session = currentSession();
        return (Marker) session.get(Marker.class, zdbID);
    }


    public SNP getSNPByID(String zdbID) {
        Session session = currentSession();

        return (SNP) session.get(SNP.class, zdbID);
    }

    public ConstructCuration getConstructByID(String zdbID) {
        Session session = currentSession();

        return (ConstructCuration) session.get(ConstructCuration.class, zdbID);
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
        if (!zdbID.startsWith("ZDB-GENE")) return null;
        return (Marker) HibernateUtil.currentSession().createCriteria(Marker.class)
                .add(Restrictions.eq("zdbID", zdbID))
                .uniqueResult();
    }

    public Clone getCloneById(String zdbID) {
        Session session = currentSession();
        return (Clone) session.get(Clone.class, zdbID);
    }

    public Transcript getTranscriptByZdbID(String zdbID) {
        return (Transcript) currentSession().get(Transcript.class, zdbID);
    }

    public Transcript getTranscriptByName(String name) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Transcript.class);
        criteria.add(Restrictions.eq("name", name));
        return (Transcript) criteria.uniqueResult();
    }

    public Transcript getTranscriptByVegaID(String vegaID) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(TranscriptDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber", vegaID));
        criteria.setMaxResults(1);
        TranscriptDBLink dblink = (TranscriptDBLink) criteria.uniqueResult();
        return getTranscriptByZdbID(dblink.getTranscript().getZdbID());
    }

    public List<ConstructComponent> getConstructComponent(String constructID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select  cc from ConstructComponent cc " +
                "      where cc.constructZdbID = :pubID " +
                "   order by cc.componentOrder ";

        Query query = session.createQuery(hql);
        query.setString("pubID", constructID);

        return (List<ConstructComponent>) query.list();

    }


    public List<String> getTranscriptTypes() {
        Session session = currentSession();

        String hql = "select t.transcriptType from Transcript t group by t.transcriptType ";
        Query query = session.createQuery(hql);


        return query.list();
    }

    public Marker getMarkerByAbbreviationIgnoreCase(String abbreviation) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.sqlRestriction("lower({alias}.mrkr_abbrev) = lower(?) "
                , abbreviation.toLowerCase()
                , StandardBasicTypes.STRING
        ));
        return (Marker) criteria.uniqueResult();
    }

    public Marker getMarkerByAbbreviation(String abbreviation) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.eq("abbreviation", abbreviation));
        return (Marker) criteria.uniqueResult();
    }

    public SequenceTargetingReagent getSequenceTargetingReagentByAbbreviation(String abbreviation) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(SequenceTargetingReagent.class);
        criteria.add(Restrictions.eq("abbreviation", abbreviation));
        return (SequenceTargetingReagent) criteria.uniqueResult();
    }

    public Marker getMarkerByName(String name) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.eq("name", name));
        return (Marker) criteria.uniqueResult();
    }

    //this is kind of awful...
    public List<Marker> getMarkersByZdbIdPrefix(String prefix) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.like("zdbID", prefix + "%"));
        criteria.setFetchMode("aliases", FetchMode.JOIN);
        List<Marker> markerList = new ArrayList<Marker>();
        markerList.addAll(criteria.list());
        return markerList;
    }

    public List<Marker> getMarkersByAbbreviation(String name) {
        List<Marker> markerList = new ArrayList<Marker>();
        Session session = currentSession();

        Criteria criteria1 = session.createCriteria(Marker.class);
        criteria1.add(Restrictions.ilike("abbreviation", name, MatchMode.START));
        criteria1.addOrder(Order.asc("abbreviationOrder"));
        markerList.addAll(criteria1.list());

        Criteria criteria2 = session.createCriteria(Marker.class);
        criteria2.add(Restrictions.ilike("abbreviation", name, MatchMode.ANYWHERE));
        criteria2.add(Restrictions.not(Restrictions.ilike("abbreviation", name, MatchMode.START)));
        criteria2.addOrder(Order.asc("abbreviationOrder"));
        markerList.addAll(criteria2.list());
        return markerList;
    }

    public List<Marker> getGenesByAbbreviation(String name) {
        List<Marker> markerList = new ArrayList<Marker>();
        Session session = currentSession();

        Criteria criteria1 = session.createCriteria(Marker.class);
        criteria1.add(Restrictions.ilike("abbreviation", name, MatchMode.START));
        criteria1.add(Restrictions.like("zdbID", "ZDB-GENE-", MatchMode.START));
        criteria1.addOrder(Order.asc("abbreviationOrder"));
        markerList.addAll(criteria1.list());

        Criteria criteria2 = session.createCriteria(Marker.class);
        criteria2.add(Restrictions.ilike("abbreviation", name, MatchMode.ANYWHERE));
        criteria2.add(Restrictions.not(Restrictions.ilike("abbreviation", name, MatchMode.START)));
        criteria2.add(Restrictions.like("zdbID", "ZDB-GENE-", MatchMode.START));
        criteria2.addOrder(Order.asc("abbreviationOrder"));
        markerList.addAll(criteria2.list());
        return markerList;
    }

    public Marker getGeneByAbbreviation(String name) {
        Session session = currentSession();
        Criteria criteria1 = session.createCriteria(Marker.class);
        criteria1.add(Restrictions.like("zdbID", "ZDB-GENE%"));
        criteria1.add(Restrictions.eq("abbreviation", name));
        try {
            return (Marker) criteria1.uniqueResult();
        } catch (HibernateException e) {
            logger.debug("unable to return marker for abbrev [" + name + "]");
            return null;
        }
    }


    public MarkerRelationship getMarkerRelationship(Marker marker1, Marker marker2, MarkerRelationship.Type type) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(MarkerRelationship.class);
        criteria.add(Restrictions.eq("firstMarker", marker1));
        criteria.add(Restrictions.eq("secondMarker", marker2));
        criteria.add(Restrictions.eq("type", type));
        return (MarkerRelationship) criteria.uniqueResult();
    }

    public List<Marker> getMarkersForRelation(String featureRelationshipName, String publicationZdbID) {
        String sql = "select distinct mrkr_zdb_id " +
                "    from marker,marker_type_group_member, " +
                "    record_attribution" +
                "    where mrkr_zdb_id = recattrib_data_zdb_id" +
                "    and recattrib_source_zdb_id=:pubZdbID " +
                "    and mrkr_type=mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group='CONSTRUCT_COMPONENTS'";


        List<String> markerZdbIds = (List<String>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("pubZdbID", publicationZdbID)

                .list();
        List<Marker> markers = new ArrayList<Marker>();
        for (String zdbId : markerZdbIds) {
            Marker m = (Marker) HibernateUtil.currentSession().get(Marker.class, zdbId);
            markers.add(m);
        }
        return markers;
    }


    public MarkerAlias getSpecificDataAlias(Marker marker, String alias) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(DataAlias.class);
        criteria.add(Restrictions.eq("marker", marker));
        criteria.add(Restrictions.eq("alias", alias));
        return (MarkerAlias) criteria.uniqueResult();
    }


    public MarkerRelationship getMarkerRelationshipByID(String zdbID) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(MarkerRelationship.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (MarkerRelationship) criteria.uniqueResult();
    }


    public List<MarkerRelationship> getMarkerRelationshipsByPublication(String publicationZdbID) {
        List<MarkerRelationship.Type> markerRelationshipList = new ArrayList<MarkerRelationship.Type>();
        markerRelationshipList.add(MarkerRelationship.Type.PROMOTER_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CONTAINS_REGION);

        Session session = currentSession();
        String hql = "select distinct mr from MarkerRelationship as mr, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = mr.zdbID AND " +
                "mr.type in (:markerRelationshipType)AND " +
                "attribution.publication.zdbID = :pubID ";

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationZdbID);
        query.setParameterList("markerRelationshipType", markerRelationshipList);
        List<MarkerRelationship> markerRelationships = (List<MarkerRelationship>) query.list();

        // order
        /*Collections.sort(markerRelationships, new Comparator<MarkerRelationship>(){
            @Override
            public int compare(MarkerRelationship o1, MarkerRelationship o2) {
                return o1.getFirstMarker().getAbbreviationOrder().compareTo(o2.getFirstMarker().getAbbreviationOrder()) ;
            }
        });*/
        return markerRelationships;
    }

    public List<MarkerRelationship> getMarkerRelationshipTypesForMarkerEdit(String grpName) {
        List<MarkerRelationship.Type> markerRelationshipList = new ArrayList<MarkerRelationship.Type>();
        markerRelationshipList.add(MarkerRelationship.Type.PROMOTER_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CONTAINS_REGION);

        Session session = currentSession();
        String hql = "select distinct mr from MarkerRelationship as mr " +
                "where mr.type not in (:markerRelationshipType)";


        Query query = session.createQuery(hql);

        query.setParameterList("markerRelationshipType", markerRelationshipList);
        List<MarkerRelationship> markerRelationshipTypes = (List<MarkerRelationship>) query.list();

        return markerRelationshipTypes;
    }

    public TreeSet<String> getLG(Marker marker) {
        Session session = currentSession();
        TreeSet<String> lgList = new TreeSet<String>();

        Query query = session.createSQLQuery("select sfclg_chromosome from sequence_feature_chromosome_location_generated " +
                "where sfclg_data_zdb_id = :mrkrZdbId order by 1");
        query.setParameter("mrkrZdbId", marker.getZdbID());

        lgList.addAll(query.list());

        return lgList;

    }


    public MarkerRelationship addMarkerRelationship(MarkerRelationship mrel, String sourceZdbID) {

        Marker marker1 = mrel.getFirstMarker();
        Marker marker2 = mrel.getSecondMarker();

        //update the two markers with the relationships
        Set<MarkerRelationship> firstMarkerRelationships = marker1.getFirstMarkerRelationships();
        if (firstMarkerRelationships == null) {
            firstMarkerRelationships = new HashSet<MarkerRelationship>();
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
        logger.debug(updateComment);
        InfrastructureService.insertUpdate(mrel.getFirstMarker(), updateComment);
        InfrastructureService.insertUpdate(mrel.getSecondMarker(), updateComment);

        //now deal with attribution
        if (sourceZdbID != null && sourceZdbID.length() > 0) {
//            currentSession().flush() ;
//            RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(mrel.getZdbID(), sourceZdbID);
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(sourceZdbID);
            pa.setDataZdbID(mrel.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            //mrel.setPublications(pubattr);
            currentSession().save(pa);
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(sourceZdbID);
            pa.setPublication(publication);

            addMarkerPub(marker1, publication);
            addMarkerPub(marker2, publication);

            currentSession().refresh(mrel);
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
        logger.debug("enter addMarDataNote");
        DataNote dnote = new DataNote();
        dnote.setDataZdbID(marker.getZdbID());
        logger.debug("markerZdbId for datanote: " + marker.getZdbID());
        dnote.setCurator(curator);
        dnote.setDate(new Date());
        dnote.setNote(note);
        logger.debug("data note curator: " + curator);
        Set<DataNote> dataNotes = marker.getDataNotes();
        if (dataNotes == null) {
            dataNotes = new HashSet<DataNote>();
            dataNotes.add(dnote);
            marker.setDataNotes(dataNotes);
        } else {
            dataNotes.add(dnote);
        }


        HibernateUtil.currentSession().save(dnote);
        logger.debug("dnote zdb_id: " + dnote.getZdbID());
        return dnote;
    }

    public AntibodyExternalNote addAntibodyExternalNote(Antibody antibody, String note, String sourceZdbID) {
        logger.debug("enter addExtDataNote");
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
        logger.debug("add orthology note");
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

    public void editAntibodyExternalNote(String notezdbid, String note) {
        logger.debug("enter addExtDataNote");
        ExternalNote extnote = infrastructureRepository.getExternalNoteByID(notezdbid);
        extnote.setNote(note);
        HibernateUtil.currentSession().update(extnote);
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
            if (!marker.getAliases().add(markerAlias))
                return null;
        }

        currentSession().save(markerAlias);

        //now handle the attribution
        String updateComment;
        if (publication != null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setDataZdbID(markerAlias.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubattr = new HashSet<PublicationAttribution>();
            pubattr.add(pa);

            markerAlias.setPublications(pubattr);
            currentSession().save(pa);

            if (marker.getMarkerType().getType() == Marker.Type.ATB) {
                addMarkerPub(marker, publication);
            }
            updateComment = "Added alias: '" + markerAlias.getAlias() + "' attributed to publication: '"
                    + publication.getZdbID() + "'";
        } else {
            updateComment = "Added alias: '" + markerAlias.getAlias() + " with no attribution";
        }

        InfrastructureService.insertUpdate(marker, updateComment);
        runMarkerNameFastSearchUpdate(marker);
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
            throw new RuntimeException("Alias '" + alias + "' does not belong to the marker '" + marker + "'! " +
                    "Cannot remove such an alias.");
        }
        // remove the ZDB active data record with cascade.

        String hql = "delete from MarkerHistory  mh " +
                " where mh.markerAlias = :zdbID ";
        Query query = currentSession().createQuery(hql);
        query.setString("zdbID", alias.getZdbID());

        currentSession().flush();

        int removed = query.executeUpdate();

        infrastructureRepository.deleteActiveDataByZdbID(alias.getZdbID());
        currentSession().flush();

        hql = "delete from MarkerAlias ma " +
                " where ma.dataZdbID = :zdbID ";
        query = currentSession().createQuery(hql);
        query.setString("zdbID", alias.getZdbID());

        removed = query.executeUpdate();
        currentSession().flush();

        currentSession().refresh(marker);

        // run the fast search table script so the alias is not showing up any more.
        runMarkerNameFastSearchUpdate(marker);
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
        String attributionZdbID = null;
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

    public void addMarkerRelationshipAttribution(MarkerRelationship mrel, Publication attribution, Marker marker) {

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
            addMarkerPub(marker, attribution);
        }
        infrastructureRepository.insertUpdatesTable(marker, "", "new attribution, marker relationship: " + mrel.getZdbID() + " with pub: " + attributionZdbID, attributionZdbID, "");
    }

    public void addDBLinkAttribution(DBLink dbLink, Publication attribution, Marker marker) {
        String linkId = dbLink.getZdbID();
        String attrId = attribution.getZdbID();

        RecordAttribution recordAttribution = infrastructureRepository.getRecordAttribution(linkId, attrId, RecordAttribution.SourceType.STANDARD);
        if (recordAttribution != null) {
            return;
        }

        infrastructureRepository.insertPublicAttribution(linkId, attrId);
        infrastructureRepository.insertUpdatesTable(marker, "", "new attribution, marker dblink: " + linkId + " with pub: " + attrId, attrId, "");
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
            if (markerDBLink.getAccessionNumber().equals(accessionNumber) &&
                    markerDBLink.getReferenceDatabase().equals(refdb)) {
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
            markerDBLinks = new HashSet<MarkerDBLink>();
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

        //accessions will end up in the fast search table associated with the marker
        runMarkerNameFastSearchUpdate(marker);

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

    @SuppressWarnings("unchecked")
    public List<MarkerFamilyName> getMarkerFamilyNamesBySubstring(String substring) {

        List<MarkerFamilyName> families = new ArrayList<MarkerFamilyName>();

        //first put on the "starts with" matches
        Session session = currentSession();
        Criteria criteria = session.createCriteria(MarkerFamilyName.class);
        criteria.add(Restrictions.ilike("markerFamilyName", substring, MatchMode.START));
        criteria.addOrder(Property.forName("markerFamilyName").asc());
        families.addAll(criteria.list());

        //then follow with the "contains" matches, while excluding "starts with"
        criteria = session.createCriteria(MarkerFamilyName.class);
        criteria.add(Restrictions.ilike("markerFamilyName", substring, MatchMode.ANYWHERE));
        criteria.add(Restrictions.not(Restrictions.ilike("markerFamilyName", substring, MatchMode.START)));
        criteria.addOrder(Property.forName("markerFamilyName").asc());

        families.addAll(criteria.list());

        return families;

    }

    public MarkerFamilyName getMarkerFamilyName(String name) {
        //first put on the "starts with" matches
        Session session = currentSession();
        Criteria criteria = session.createCriteria(MarkerFamilyName.class);
        criteria.add(Restrictions.eq("markerFamilyName", name));
        return (MarkerFamilyName) criteria.uniqueResult();
    }

    /**
     * This executes the regen_names_marker() procedure.
     * Since Informix Dialect does not support stored procedures
     * we create our own callable statement and then execute it
     * via straight JDBC.
     *
     * @param marker Marker
     */
    public void runMarkerNameFastSearchUpdate(final Marker marker) {
        Session session = currentSession();
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                CallableStatement statement = null;
                String sql = "execute procedure regen_names_marker(?)";
                try {
                    statement = connection.prepareCall(sql);
                    String zdbID = marker.getZdbID();
                    statement.setString(1, zdbID);
                    statement.execute();
                    logger.info("Execute stored procedure: " + sql + " with the argument " + zdbID);
                } catch (SQLException e) {
                    logger.error("Could not run: " + sql, e);
                    logger.error(DbSystemUtil.getLockInfo());
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            logger.error(e);
                        }
                    }
                }
            }
        });
    }

    public void createMarker(Marker marker, Publication pub, boolean insertUpdate) {
        if (marker.getName() == null) {
            throw new RuntimeException("Cannot create a new marker without a name.");
        }
        if (marker == null) {
            throw new RuntimeException("No marker object provided.");
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

        // run procedure for fast search table
        runMarkerNameFastSearchUpdate(marker);
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
    @SuppressWarnings("unchecked")
    public boolean hasSmallSegmentRelationship(Marker associatedMarker, Marker smallSegment) {
        Session session = currentSession();

        String hql = "from MarkerRelationship where firstMarker = :firstMarker AND secondMarker = :secondMarker " +
                " AND (type = :type1 or type = :type2 or type = :type3) ";
        Query query = session.createQuery(hql);
        query.setParameter("firstMarker", associatedMarker);
        query.setParameter("secondMarker", smallSegment);
        query.setParameter("type1", MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT);
        query.setParameter("type2", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
        query.setParameter("type3", MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT);
        List<MarkerRelationship> rels = (List<MarkerRelationship>) query.list();
        return !CollectionUtils.isEmpty(rels);
    }

    public boolean hasTranscriptRelationship(Marker associatedMarker, Marker transcript) {
        Session session = currentSession();

        String hql = "from MarkerRelationship where firstMarker = :firstMarker AND secondMarker = :secondMarker " +
                " AND type = :type1 ";
        Query query = session.createQuery(hql);
        query.setParameter("firstMarker", associatedMarker);
        query.setParameter("secondMarker", transcript);
        query.setParameter("type1", MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        List<MarkerRelationship> rels = (List<MarkerRelationship>) query.list();
        return !CollectionUtils.isEmpty(rels);
    }

    @SuppressWarnings("unchecked")
    public List<Marker> getMarkersByAbbreviationAndGroup(String name, Marker.TypeGroup markerType) {
        List<Marker> markerList = new ArrayList<Marker>();
        Session session = currentSession();

        MarkerTypeGroup group = getMarkerTypeGroupByName(markerType.name());
        if (group == null) {
            return null;
        }
        MarkerType[] types = new MarkerType[group.getTypeStrings().size()];
        int index = 0;
        for (String type : group.getTypeStrings()) {
            types[index++] = getMarkerTypeByName(type);
        }

        // a slight speed improvement and more fine-grained sorting control (if needed)
        String hql = " select distinct m from Marker m  "
                + " where m.abbreviation like :name  "
                + " and m.markerType in (:types)  ";
//                + " order by m.abbreviationOrder asc " ;
        markerList.addAll(HibernateUtil.currentSession()
                .createQuery(hql)
                .setString("name", "%" + name + "%")
                .setParameterList("types", types)
                .list());

        Collections.sort(markerList, new MarkerAbbreviationComparator(name));

        return markerList;

//        Criteria criteria1 = session.createCriteria(Marker.class);
//        criteria1.add(Restrictions.like("abbreviation", name, MatchMode.START));
//        criteria1.addOrder(Order.asc("abbreviationOrder"));
//        criteria1.add(Restrictions.in("markerType", types));
//        markerList.addAll(criteria1.list());
//
//        Criteria criteria2 = session.createCriteria(Marker.class);
//        criteria2.add(Restrictions.like("abbreviation", name, MatchMode.ANYWHERE));
//        criteria2.add(Restrictions.not(Restrictions.like("abbreviation", name, MatchMode.START)));
//        criteria2.addOrder(Order.asc("abbreviationOrder"));
//        criteria2.add(Restrictions.in("markerType", types));
//        markerList.addAll(criteria2.list());
//        return markerList;
    }


    public List<Marker> getConstructsByAttribution(String name) {
        List<Marker> markerList = new ArrayList<Marker>();

        /*MarkerTypeGroup group = getMarkerTypeGroupByName(markerType.name());
        if (group == null)
            return null;
        MarkerType[] types = new MarkerType[group.getTypeStrings().size()];
        int index = 0;
        for (String type : group.getTypeStrings()) {
            types[index++] = getMarkerTypeByName(type);
        }*/

        String hql = " select distinct m from Marker m , PublicationAttribution pa "
                + " where lower(m.abbreviation) like lower(:name)  "
                + " and pa.dataZdbID = m.zdbID  "
                + " and m.markerType like '%CONS%'  ";
//                + " order by m.abbreviationOrder asc " ;
        markerList.addAll(HibernateUtil.currentSession()
                .createQuery(hql)
                .setString("name", "%" + name + "%")


                .list());

        Collections.sort(markerList, new MarkerAbbreviationComparator(name));

        return markerList;
    }

    public Marker getMarkerByAbbreviationAndAttribution(String name, String pubZdbId) {
        List<Marker> markerList = new ArrayList<Marker>();


        String hql = " select distinct m from Marker m , PublicationAttribution pa "
                + " where trim(m.abbreviation) = trim(:name)  "
                + " and pa.dataZdbID = m.zdbID  "
                + " and pa.sourceZdbID = :publicationZdbId ";

//                + " order by m.abbreviationOrder asc " ;
        Session session = currentSession();
        Query query = session.createQuery(hql);
        query.setString("name", name);
        query.setString("publicationZdbId", pubZdbId);

        return ((Marker) query.uniqueResult());

    }

    // clone methods

    public List<String> getPolymeraseNames() {
        String hql = " select c.polymeraseName from Clone c group by c.polymeraseName ";
        Session session = currentSession();
        return session.createQuery(hql).list();
    }

    public MarkerAlias getMarkerAlias(String aliasZdbID) {
        Session session = currentSession();
        return (MarkerAlias) session.get(MarkerAlias.class, aliasZdbID);
    }

    public List<TranscriptTypeStatusDefinition> getAllTranscriptTypeStatusDefinitions() {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(TranscriptTypeStatusDefinition.class);
        //criteria.addOrder(Order.asc("order"));
        return criteria.list();
    }

    public List<TranscriptType> getAllTranscriptTypes() {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(org.zfin.marker.TranscriptType.class);
        criteria.addOrder(Order.asc("order"));
        return criteria.list();

    }

    public TranscriptType getTranscriptTypeForName(String typeString) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(org.zfin.marker.TranscriptType.class);
        criteria.add(Restrictions.eq("type", typeString));
        return (TranscriptType) criteria.uniqueResult();
    }

    public TranscriptStatus getTranscriptStatusForName(String statusString) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(org.zfin.marker.TranscriptStatus.class);
        criteria.add(Restrictions.eq("status", statusString));
        return (TranscriptStatus) criteria.uniqueResult();
    }

    public boolean getGeneHasGOEvidence(Marker gene) {
        String hql = " " +
                " select count( mgte) from MarkerGoTermEvidence mgte " +
                " where mgte.marker.zdbID = :geneZdbID ";
        Query query = currentSession().createQuery(hql);
        query.setString("geneZdbID", gene.getZdbID());
        return (((Number) query.uniqueResult()).longValue() > 0);
    }


    public boolean getGeneHasExpressionImages(Marker gene) {
        String hql = " " +
                " select count( figs ) from ExpressionExperiment ee " +
                " join ee.expressionResults er " +
                " join er.figures figs " +
                " join figs.images ims " +
                " where ee.gene.zdbID = :geneZdbID ";
        Query query = currentSession().createQuery(hql);
        query.setString("geneZdbID", gene.getZdbID());
        return (((Number) query.uniqueResult()).longValue() > 0);
    }

    public boolean getGeneHasExpression(Marker gene) {
        String hql = " " +
                " select count( er) from ExpressionExperiment ee " +
                " join ee.expressionResults er " +
                " join er.figures figs " +
                " where ee.gene.zdbID = :geneZdbID ";
        Query query = currentSession().createQuery(hql);
        query.setString("geneZdbID", gene.getZdbID());
        return (((Number) query.uniqueResult()).longValue() > 0);
    }

    public boolean getGeneHasPhenotype(Marker gene) {
        String sql = "select count(phenox_pk_id) " +
                "from mutant_fast_search, phenotype_experiment " +
                "where mfs_mrkr_zdb_id = :geneZdbID " +
                "and mfs_genox_zdb_id = phenox_genox_zdb_id ";
        Query query = currentSession().createSQLQuery(sql);
        query.setString("geneZdbID", gene.getZdbID());

        return (((Number) query.uniqueResult()).longValue() > 0);
    }

    public boolean getGeneHasPhenotypeImage(Marker gene) {
        String sql = " " +
                "select phenox_fig_zdb_id " +
                "from mutant_fast_search, phenotype_experiment, image " +
                "where mfs_mrkr_zdb_id = :geneZdbID " +
                "and mfs_genox_zdb_id = phenox_genox_zdb_id " +
                "and phenox_fig_zdb_id = img_fig_zdb_id";
        Query query = currentSession().createSQLQuery(sql);
        query.setString("geneZdbID", gene.getZdbID());
        return (query.list().size() > 0);
    }

    public List<String> getVectorNames() {
        String hql = " select c.vector.name  from Clone c group by c.vector.name ";
        Session session = currentSession();
        return session.createQuery(hql).list();
    }

    public List<String> getProbeLibraryNames() {
        String hql = " select c.probeLibrary.name  from Clone c group by c.probeLibrary.name ";
        Session session = currentSession();
        return session.createQuery(hql).list();
    }

    public List<ProbeLibrary> getProbeLibraries() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ProbeLibrary.class);
        return criteria.list();
    }

    public ProbeLibrary getProbeLibrary(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ProbeLibrary.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (ProbeLibrary) criteria.uniqueResult();
    }

    public List<String> getDigests() {
        String hql = " select c.digest  from Clone c group by c.digest  ";
        Session session = currentSession();
        return session.createQuery(hql).list();
    }

    public List<String> getCloneSites() {
        String hql = " select c.cloningSite from Clone c group by c.cloningSite ";
        Session session = currentSession();
        return session.createQuery(hql).list();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<HighQualityProbe> getHighQualityProbeStatistics(GenericTerm aoTerm, PaginationBean pagination, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();
        String hql = null;
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
        Query query = session.createQuery(hqlCount);
        query.setParameter("aoterm", aoTerm.getZdbID());
        int totalCount = ((Number) query.uniqueResult()).intValue();


        // if no antibodies found return here
        if (totalCount == 0) {
            return new PaginationResult<>(0, null);
        }

        String sqlQueryStr = " select distinct(stat.fstat_feat_zdb_id), probe.mrkr_abbrev as probeAbbrev, gene.mrkr_zdb_id," +
                "                       gene.mrkr_abbrev,gene.mrkr_abbrev_order  " +
                "from feature_stats as stat, marker as gene, marker as probe " +
                "     where fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = gene.mrkr_zdb_id " +
                "           and fstat_feat_zdb_id = probe.mrkr_zdb_id " +
                "           and fstat_type = :type ";
        if (!includeSubstructures) {
            sqlQueryStr += "  and fstat_subterm_zdb_id = :aoterm ";
        }
        sqlQueryStr += "order by gene.mrkr_abbrev_order ";

        SQLQuery sqlQquery = session.createSQLQuery(sqlQueryStr);
        sqlQquery.setString("aoterm", aoTerm.getZdbID());
        sqlQquery.setString("type", "High-Quality-Probe");
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
        String sqlQueryAllStr = " select stat.fstat_feat_zdb_id, probe.mrkr_abbrev as probeSymbol, gene.mrkr_zdb_id," +
                "                       gene.mrkr_abbrev,stat.fstat_fig_zdb_id, fig.fig_label, stat.fstat_pub_zdb_id, " +
                "                       probe.mrkr_type, gene.mrkr_abbrev_order, pub.zdb_id, pub.pub_mini_ref," +
                "                       gene.mrkr_name, probe.mrkr_name as probeName, img.img_zdb_id  " +
                "from feature_stats as stat, marker as gene, marker as probe, figure as fig, publication as pub, " +
                "     OUTER image as img " +
                "     where fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = gene.mrkr_zdb_id " +
                "           and fstat_feat_zdb_id = probe.mrkr_zdb_id " +
                "           and fstat_type = :type " +
                "           and fstat_fig_zdb_id = fig.fig_zdb_id " +
                "           and fstat_pub_zdb_id = pub.zdb_id " +
                "           and fstat_img_zdb_id = img.img_zdb_id ";
        if (!includeSubstructures) {
            sqlQueryAllStr += "  and fstat_subterm_zdb_id = :aoterm ";
        }
        sqlQueryAllStr += "order by gene.mrkr_abbrev_order ";

        SQLQuery sqlAllQquery = session.createSQLQuery(sqlQueryAllStr);
        sqlAllQquery.setString("aoterm", aoTerm.getZdbID());
        sqlAllQquery.setString("type", "High-Quality-Probe");
        ScrollableResults scrollableResults = sqlAllQquery.scroll();
        if (pagination.getFirstRecord() == 1) {
            scrollableResults.beforeFirst();
        } else {
            scrollableResults.setRowNumber(pagination.getFirstRecord() - 1);
        }
        List<HighQualityProbe> list = new ArrayList<HighQualityProbe>();
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
            Figure figure = null;
            if (label != null && label.equals(Figure.Type.TOD.toString())) {
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
        return new PaginationResult<HighQualityProbe>(totalCount, list);

    }

    private MarkerType getGenedomType() {
        MarkerType type = new MarkerType();
        type.setType(Marker.Type.GENE);
        Set<Marker.TypeGroup> typeGroups = new HashSet<Marker.TypeGroup>();
        typeGroups.add(Marker.TypeGroup.GENEDOM);
        type.setTypeGroups(typeGroups);
        return type;
    }

    private MarkerType getProbeType(String typeStr) {
        MarkerType type = new MarkerType();
        type.setType(Marker.Type.getType(typeStr));
        Set<Marker.TypeGroup> typeGroups = new HashSet<Marker.TypeGroup>();
        typeGroups.add(Marker.TypeGroup.getType(typeStr));
        type.setTypeGroups(typeGroups);
        return type;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Marker> getMarkersForStandardAttributionAndType(Publication publication, String type) {
        List<String> types = new ArrayList<String>();
        if (type == "MRPHLNO") {

            types.add(Marker.Type.MRPHLNO.name());
            types.add(Marker.Type.TALEN.name());
            types.add(Marker.Type.CRISPR.name());

        } else {

            types.add(Marker.Type.GENE.name());

        }
        String hql = "select m from PublicationAttribution pa , Marker m " +
                " where pa.dataZdbID=m.zdbID and pa.publication.zdbID= :pubZdbID  " +
                " and pa.sourceType= :sourceType and m.markerType.name in (:types) " +
                " order by m.abbreviationOrder ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubZdbID", publication.getZdbID());
        query.setString("sourceType", RecordAttribution.SourceType.STANDARD.toString());
        // yes, this is a hack, should use typeGroup, I guess
        query.setParameterList("types", types);
        return query.list();
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Marker> getMarkersForAttribution(String publicationZdbID) {
        String hql = "" +
                " select distinct m from Marker m , RecordAttribution ra " +
                " left join fetch m.aliases " +
                " left join fetch m.markerType " +
                " where ra.dataZdbID=m.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID " +
                " order by m.abbreviationOrder " +
                " ";

        return (List<Marker>) HibernateUtil.currentSession().createQuery(hql)
                .setString("pubZdbID", publicationZdbID)
                .setString("standard", RecordAttribution.SourceType.STANDARD.toString())
                .list();
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<ConstructCuration> getConstructsForAttribution(String publicationZdbID) {
        String hql = "" +
                " select distinct m from ConstructCuration m , RecordAttribution ra " +
                " where ra.dataZdbID=m.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID " +
                " ";

        return (List<ConstructCuration>) HibernateUtil.currentSession().createQuery(hql)
                .setString("pubZdbID", publicationZdbID)
                .setString("standard", RecordAttribution.SourceType.STANDARD.toString())
                .list();
    }

    @SuppressWarnings("unchecked")
    public List<Publication> getHighQualityProbePublications(GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql;
        hql = "select distinct stat.publication" +
                "     from HighQualityProbeAOStatistics stat " +
                "     where stat.superterm = :aoterm and " +
                "           stat.subterm = :aoterm" +
                "     order by stat.publication.publicationDate ";
        Query query = session.createQuery(hql);
        query.setParameter("aoterm", anatomyTerm);
        return (List<Publication>) query.list();
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

    /**
     * Create a list of AntibodyStatistics objects from antibodyAOStatistics record.
     * This logic groups the objects accordingly.
     *
     * @param record AntibodyAOStatistics
     * @param aoTerm anatom term
     * @param list   antibodyStatistics objects to be manipulated.
     */
    private void populateProbeStatisticsRecordOld(HighQualityProbeAOStatistics record, List<HighQualityProbe> list, GenericTerm aoTerm) {

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
        MarkerType type = (MarkerType) session.load(MarkerType.class, name);
        if (type == null || type.getName() == null) {
            return null;
        }
        return type;
    }

    public MarkerType getMarkerTypeByDisplayName(String displayName) {
        Session session = currentSession();

        return (MarkerType) session.createCriteria(MarkerType.class)
                .add(Restrictions.eq("displayName", displayName)).uniqueResult();

    }

    public MarkerTypeGroup getMarkerTypeGroupByName(String name) {
        Session session = currentSession();
        MarkerTypeGroup markerTypeGroup = (MarkerTypeGroup) session.get(MarkerTypeGroup.class, name);
        if (markerTypeGroup == null || markerTypeGroup.getName() == null) {
            return null;
        }
        return markerTypeGroup;
    }

    /**
     * Rename an existing marker. This entails to
     * to provide a reason, a publication on which basis this is done.
     * This will run a script to populate a fastsearch table for renamed markers.
     *
     * @param marker
     * @param publication
     * @param reason
     */
    public void renameMarker(Marker marker, Publication publication, MarkerHistory.Reason reason, String oldSymbol, String oldGeneName) {
        //update marker history reason
        logger.debug("Got to rename marker: " + marker.getAbbreviation() + " " + marker.getZdbID() + " " + marker.getName());
        MarkerHistory history = new MarkerHistory();
        history.setReason(reason);
        history.setName(oldGeneName);
        history.setOldMarkerName(oldSymbol);
        history.setSymbol(marker.getAbbreviation());
        history.setMarker(marker);
        history.setEvent(MarkerHistory.Event.REASSIGNED);
        MarkerAlias alias = getMarkerRepository().addMarkerAlias(marker, marker.getAbbreviation(), publication);
        history.setMarkerAlias(alias);

        getMarkerRepository().runMarkerNameFastSearchUpdate(marker);
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
        List<MarkerType> markerTypes = new ArrayList<MarkerType>();
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
        Marker sequenceTargetingReagent = (Marker) stReagent;
        Session session = currentSession();
        String hql = "select rel.secondMarker from MarkerRelationship as rel  " +
                "where rel.firstMarker = :sequenceTargetingReagent and rel.type = :type " +
                "order by rel.secondMarker.abbreviationOrder";
        Query query = session.createQuery(hql);
        query.setParameter("sequenceTargetingReagent", sequenceTargetingReagent);
        query.setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        List<Marker> targetGenes = (List<Marker>) query.list();
        return targetGenes;
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
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.eq("abbreviation", abbreviation.toLowerCase()));
        Marker marker = (Marker) criteria.uniqueResult();
        return marker != null;
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
        List<String> allIds = new ArrayList<String>(20 * firstN);
        for (MarkerType markerType : markerTypes) {
            String hql = "select zdbID from Marker where markerType = :markerType " +
                    "order by zdbID";
            Query query = session.createQuery(hql);
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
        Query query = session.createQuery(hql);
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
        String hql = "select distinct accession.dataZdbID from DBLink accession, ExternalNote note" +
                " where accession.referenceDatabase.foreignDB.dbName = :sourceName " +
                " and note.externalDataZdbID = accession.zdbID " +
                " order by accession.dataZdbID";
        Query query = session.createQuery(hql);
        query.setString("sourceName", "UniProtKB");
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
        Query query = session.createQuery(hql);
        return query.list();
    }


    @Override
    public boolean getHasMarkerHistory(String zdbId) {
        return Integer.valueOf(HibernateUtil.currentSession()
                .createSQLQuery(
                        "select count(*) from marker_history where mhist_mrkr_zdb_id = :zdbID ")
                .setString("zdbID", zdbId)
                .uniqueResult().toString())
                > 0;

    }

/*
    public Map<String,List<String>> getPromotorConstructMap() {
        String sql = "select mrel_mrkr_1_zdb_id, mrkr_abbrev from marker_relationship, marker " +
                "where mrel_type = \"promoter of\"  " +
                "  and mrkr_zdb_id ; ";
        return Map<String,List<String>> HibernateUtil.currentSession().createSQLQuery(sql).setResultTransformer()
    }
*/

    @Override
    public List<PreviousNameLight> getPreviousNamesLight(final Marker gene) {
        String sql = "  " +
                " select da.dalias_alias, ra.recattrib_source_zdb_id, da.dalias_zdb_id " +
                "    from data_alias da " +
                "    join alias_group ag on da.dalias_group_id=ag.aliasgrp_pk_id " +
                "    left outer join record_attribution ra on ra.recattrib_data_zdb_id=da.dalias_zdb_id  " +
                "    where dalias_data_zdb_id = :markerZdbID " +
                "    and aliasgrp_pk_id = dalias_group_id " +
                "    and aliasgrp_name = 'alias' " +
                " ";
        return (List<PreviousNameLight>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbID", gene.getZdbID())
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        PreviousNameLight previousNameLight = new PreviousNameLight(gene.getAbbreviation());
                        previousNameLight.setMarkerZdbID(gene.getZdbID());
                        previousNameLight.setPureAliasName(tuple[0].toString());
                        if (gene.getZdbID().startsWith("ZDB-GENE")) {
                            previousNameLight.setAlias("<i>" + tuple[0].toString() + "</i>");
                        } else {
                            if (gene.getZdbID().contains("CONSTRCT")) {
                                previousNameLight.setAlias("<i>" + tuple[0].toString() + "</i>");
                            } else {
                                previousNameLight.setAlias(tuple[0].toString());
                            }
                        }
                        previousNameLight.setAliasZdbID(tuple[2].toString());
                        if (tuple[1] != null) {
                            previousNameLight.setPublicationZdbID(tuple[1].toString());
                            previousNameLight.setPublicationCount(1);
                        }

                        return previousNameLight;
                    }

                    @Override
                    public List transformList(List list) {
                        Map<String, PreviousNameLight> map = new HashMap<String, PreviousNameLight>();
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
                    }
                })
                .list();
    }


    @Override
    public List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayExcludeTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... typesNotIn) {
        String sql1To2 = " 	select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,  " +
                "	       mreltype_1_to_2_comments, " +
                "          '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
                "          ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,  " +
                "          src.srcurl_url, src.srcurl_display_text , mrel_zdb_id  " +
                " 	  from marker_relationship  " +
                "	       inner join marker_relationship_type " +
                "                 on mrel_type = mreltype_name " +
                "               inner join marker " +
                "                 on mrel_mrkr_2_zdb_id = mrkr_zdb_id  " +
                "	       inner join marker_types " +
                "                 on mrkr_type = marker_type " +
                "	       left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id " +
                "	       left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_2_zdb_id " +
                "	       left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id  " +
                "	 where mrel_mrkr_1_zdb_id = :markerZdbId  ";
        if (typesNotIn.length > 0) {
            sql1To2 += "	   and mrel_type not in (:types) ";
        }
        sql1To2 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";

        String sql2To1 = " select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,  " +
                "	       mreltype_2_to_1_comments, " +
                "          '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
                "          ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,  " +
                "          src.srcurl_url, src.srcurl_display_text  , mrel_zdb_id  " +
                " 	  from marker_relationship " +
                "	       inner join marker_relationship_type " +
                "             	  on mrel_type = mreltype_name " +
                "               inner join marker " +
                "                  on mrel_mrkr_1_zdb_id = mrkr_zdb_id  " +
                "	       inner join marker_types " +
                "            	  on mrkr_type = marker_type " +
                "	       left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id " +
                "	       left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_1_zdb_id " +
                "	       left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id  " +
                "	 where mrel_mrkr_2_zdb_id = :markerZdbId  ";

        if (typesNotIn.length > 0) {
            sql2To1 += "	   and mrel_type not in (:types) ";
        }
        sql2To1 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";


        ResultTransformer resultTransformer = new MarkerRelationshipSupplierPresentationTransformer(is1to2);
        String sql = (is1to2 ? sql1To2 : sql2To1);
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setResultTransformer(resultTransformer);
        if (typesNotIn.length > 0) {
            List<String> types = new ArrayList<String>();
            for (MarkerRelationship.Type type : typesNotIn) {
                types.add(type.toString());
            }
            query.setParameterList("types", types);
        }

        List<MarkerRelationshipPresentation> list = resultTransformer.transformList(query.list());
        return list;
    }

    @Override
    public List<Marker> getMarkersByAlias(String key) {
        String hql = " select ma.marker from MarkerAlias ma where ma.aliasLowerCase = :alias ";

        return (List<Marker>) HibernateUtil.currentSession().createQuery(hql)
                .setString("alias", key.toLowerCase())
                .list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayForTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types) {
        String sql1To2 = " 	select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,  " +
                "	       mreltype_1_to_2_comments, " +
                "          '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
                "          ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,  " +
                "          src.srcurl_url, src.srcurl_display_text  , mrel_zdb_id  " +
                " 	  from marker_relationship  " +
                "	       inner join marker_relationship_type " +
                "                 on mrel_type = mreltype_name " +
                "               inner join marker " +
                "                 on mrel_mrkr_2_zdb_id = mrkr_zdb_id  " +
                "	       inner join marker_types " +
                "                 on mrkr_type = marker_type " +
                "	       left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id " +
                "	       left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_2_zdb_id " +
                "	       left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id  " +
                "	 where mrel_mrkr_1_zdb_id = :markerZdbId  ";
        if (types.length > 0) {
            sql1To2 += "	   and mrel_type in (:types) ";
        }
        sql1To2 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order";

        String sql2To1 = " select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,  " +
                "	       mreltype_2_to_1_comments, " +
                "          '<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
                "          ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,  " +
                "          src.srcurl_url, src.srcurl_display_text , mrel_zdb_id   " +
                " 	  from marker_relationship " +
                "	       inner join marker_relationship_type " +
                "             	  on mrel_type = mreltype_name " +
                "               inner join marker " +
                "                  on mrel_mrkr_1_zdb_id = mrkr_zdb_id  " +
                "	       inner join marker_types " +
                "            	  on mrkr_type = marker_type " +
                "	       left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id " +
                "	       left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrel_mrkr_1_zdb_id " +
                "	       left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id  " +
                "	 where mrel_mrkr_2_zdb_id = :markerZdbId  ";
        if (types.length > 0) {
            sql2To1 += "	   and mrel_type in (:types) ";
        }
        sql2To1 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";
        ResultTransformer resultTransformer = new MarkerRelationshipSupplierPresentationTransformer(is1to2);
        String sql = (is1to2 ? sql1To2 : sql2To1);
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setResultTransformer(resultTransformer);
        if (types.length > 0) {
            Set<String> typeStrings = new HashSet<String>();
            for (MarkerRelationship.Type type : types) {
                logger.debug("type: " + type.toString());
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
            return linkDisplay;
        }

        @Override
        public List transformList(List list) {
            Map<String, LinkDisplay> linkMap = new HashMap<>();
            for (Object o : list) {
                LinkDisplay display = (LinkDisplay) o;
                LinkDisplay displayStored = linkMap.get(display.getAccession());
                if (displayStored != null) {
                    displayStored.addReferences(display.getReferences());
                    linkMap.put(displayStored.getAccession(), displayStored);
                } else {
                    linkMap.put(display.getAccession(), display);
                }

            }

            return new ArrayList<LinkDisplay>(linkMap.values());
        }
    }

    public MarkerDBLink getMarkerDBLink(String linkId) {
        Session session = HibernateUtil.currentSession();
        return (MarkerDBLink) session.get(MarkerDBLink.class, linkId);
    }

    public List<LinkDisplay> getMarkerLinkDisplay(String dbLinkId) {
        String sql = "select fdbdt.fdbdt_data_type, dbl.dblink_length, dbl.dblink_linked_recid, dbl.dblink_acc_num, fdb.fdb_db_display_name, fdb.fdb_db_query, fdb.fdb_url_suffix, " +
                "ra.recattrib_source_zdb_id, fdb.fdb_db_significance, dbl.dblink_zdb_id, fdbc.fdbcont_zdb_id, pub.title, fdbdt.fdbdt_display_order " +
                "from db_link dbl  " +
                "join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
                "join foreign_db fdb on fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id " +
                "join foreign_db_data_type fdbdt on fdbdt.fdbdt_pk_id = fdbc.fdbcont_fdbdt_id " +
                "left outer join record_attribution ra on ra.recattrib_data_zdb_id=dbl.dblink_zdb_id " +
                "join publication pub on ra.recattrib_source_zdb_id=pub.zdb_id " +
                "where dbl.dblink_zdb_id = :dbLinkId ";

        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("dbLinkId", dbLinkId)
                .setResultTransformer(markerDBLinkTransformer);

        return markerDBLinkTransformer.transformList(query.list());
    }

    public List<LinkDisplay> getMarkerDBLinksFast(Marker marker, DisplayGroup.GroupName groupName) {
        String sql = "select fdbdt.fdbdt_data_type,dbl.dblink_length,dbl.dblink_linked_recid,dbl.dblink_acc_num,fdb.fdb_db_display_name,fdb.fdb_db_query,fdb.fdb_url_suffix, " +
                "ra.recattrib_source_zdb_id, fdb.fdb_db_significance, dbl.dblink_zdb_id, fdbc.fdbcont_zdb_id, pub.title, fdbdt.fdbdt_display_order " +
                "from db_link dbl  " +
                "join foreign_db_contains_display_group_member m on m.fdbcdgm_fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id " +
                "join foreign_db_contains_display_group g on g.fdbcdg_pk_id=m.fdbcdgm_group_id " +
                "join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
                "join foreign_db_data_type fdbdt on fdbdt.fdbdt_pk_id = fdbc.fdbcont_fdbdt_id " +
                "join foreign_db fdb on fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id " +
                "left outer join record_attribution ra on ra.recattrib_data_zdb_id=dbl.dblink_zdb_id " +
                "join publication pub on ra.recattrib_source_zdb_id=pub.zdb_id " +
                "where g.fdbcdg_name= :displayGroup " +
                "and " +
                "dbl.dblink_linked_recid= :markerZdbId ";
        // case 7586 suppress OTTDARG's and ENSDARGG's on transcript pages
        if (marker.getZdbID().startsWith("ZDB-TSCRIPT")) {
            sql += " and fdb.fdb_db_name != 'VEGA' ";
        }
        if (marker.getZdbID().startsWith("ZDB-GENE")) {
            sql += " and dbl.dblink_acc_num not like  'ENSDARP%' ";
        }
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setParameter("displayGroup", groupName.toString())
                .setResultTransformer(markerDBLinkTransformer);

        List<LinkDisplay> linkDisplay = markerDBLinkTransformer.transformList(query.list());
        Collections.sort(linkDisplay, new Comparator<LinkDisplay>() {
            @Override
            public int compare(LinkDisplay linkA, LinkDisplay linkB) {
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
            }
        });


        return linkDisplay;
    }


    @Override
    public List<MarkerRelationshipPresentation> getRelatedMarkerDisplayForTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... types) {
        String sql1To2 = " 	select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,  " +
                "	       mreltype_1_to_2_comments, mrkr_name, " +
                "          ra.recattrib_source_zdb_id , mrel_zdb_id " +
                " 	  from marker_relationship  " +
                "	       inner join marker_relationship_type " +
                "                 on mrel_type = mreltype_name " +
                "               inner join marker " +
                "                 on mrel_mrkr_2_zdb_id = mrkr_zdb_id  " +
                "	       inner join marker_types " +
                "                 on mrkr_type = marker_type " +
                "	       left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id " +
                "	 where mrel_mrkr_1_zdb_id = :markerZdbId  ";
        if (types.length > 0) {
            sql1To2 += "	   and mrel_type in (:types) ";
        }
        sql1To2 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order";

        String sql2To1 = " select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,  " +
                "	       mreltype_2_to_1_comments, mrkr_name, " +
                "          ra.recattrib_source_zdb_id , mrel_zdb_id " +
                " 	  from marker_relationship " +
                "	       inner join marker_relationship_type " +
                "             	  on mrel_type = mreltype_name " +
                "               inner join marker " +
                "                  on mrel_mrkr_1_zdb_id = mrkr_zdb_id  " +
                "	       inner join marker_types " +
                "            	  on mrkr_type = marker_type " +
                "	       left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrel_zdb_id " +
                "	 where mrel_mrkr_2_zdb_id = :markerZdbId  ";
        if (types.length > 0) {
            sql2To1 += "	   and mrel_type in (:types) ";
        }
        sql2To1 += "      order by mrel_type, mrkrtype_type_display, mrkr_abbrev_order ";


        ResultTransformer resultTransformer = new MarkerRelationshipPresentationTransformer(is1to2);
        String sql = (is1to2 ? sql1To2 : sql2To1);
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setResultTransformer(resultTransformer);
        if (types.length > 0) {
            Set<String> typeStrings = new HashSet<String>();
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
        String sql = " select dblink_acc_num, REPLACE(extnote_note,'CTRL','<br>') " +
                " from external_note, db_link " +
                " where extnote_data_zdb_id = dblink_zdb_id " +
                " and dblink_linked_recid = :markerZdbID ";
        List<GeneProductsBean> geneProducts = HibernateUtil.currentSession().createSQLQuery(sql)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        GeneProductsBean geneProductsBean = new GeneProductsBean();
                        geneProductsBean.setAccession(tuple[0].toString());
                        if (tuple[1] != null) {
                            geneProductsBean.setComment(tuple[1].toString());
                        }

                        return geneProductsBean;
                    }
                })
                .setString("markerZdbID", zdbID)
                .list();
        return geneProducts;
    }

    @Override
    public boolean isFromChimericClone(String zdbID) {
        String sql = "   select 't' " +
                "    from marker_relationship " +
                "    join clone on clone_mrkr_zdb_id = mrel_mrkr_2_zdb_id " +
                "    where mrel_mrkr_1_zdb_id = :markerZdbID " +
                "    and clone_problem_type = 'Chimeric'";
        Object result = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbID", zdbID)
                .setMaxResults(1)
                .uniqueResult();
        return result != null;
    }

    @Override
    public boolean cloneHasSnp(Clone clone) {
        String sql = "  select distinct snpdattr_pub_zdb_id " +
                "  from snp_download_attribution, snp_download " +
                "  where snpd_mrkr_zdb_id=:markerZdbID " +
                "  and snpd_pk_id=snpdattr_snpd_pk_id " +
                "  order by snpdattr_pub_zdb_id";
        Object result = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbID", clone.getZdbID())
                .setMaxResults(1)
                .uniqueResult();
        return result != null;
    }

    @Override
    public List<MarkerSupplier> getSuppliersForMarker(String zdbID) {
        return HibernateUtil.currentSession().createCriteria(MarkerSupplier.class)
                .add((Restrictions.eq("dataZdbID", zdbID)))
                .list();
    }

    @Override
    public boolean markerExistsForZdbID(String zdbID) {
        return null
                !=
                HibernateUtil.currentSession()
                        .createSQLQuery("select m.mrkr_zdb_id from marker m where m.mrkr_zdb_id = :markerZdbID")
                        .setString("markerZdbID", zdbID)
                        .setMaxResults(1)
                        .uniqueResult()
                ;
    }

    @Override
    public List<String> getMarkerZdbIdsForType(Marker.Type markerType) {
        String hql = " " +
                " select m.zdbID " +
                " from Marker m " +
                " where m.markerType.name = :type " +
                " ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setParameter("type", markerType.name())
                .list()
                ;
    }

    // abbrev, zdbID
    @Override
    public Map<String, String> getGeoMarkerCandidates() {
        List<String> types = new ArrayList<String>();
        types.add(Marker.Type.CDNA.name());
        types.add(Marker.Type.EST.name());
        types.add(Marker.Type.GENE.name());
        types.add(Marker.Type.GENEP.name());

        String hql = " " +
                " select m.abbreviation, m.zdbID " +
                " from Marker m " +
                " where m.markerType.name in (:types) " +
                " ";
        List<Marker> markers = HibernateUtil.currentSession().createQuery(hql)
                .setParameterList("types", types)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        Marker m = new Marker();
                        m.setAbbreviation(tuple[0].toString());
                        m.setZdbID(tuple[1].toString());
                        return m;
                    }
                })
                .list();
        Map<String, String> markerCandidates = new HashMap<String, String>();
        for (Marker m : markers) {
            markerCandidates.put(m.getAbbreviation(), m.getZdbID());
        }
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
     *
     * @param gene
     * @return
     */
    @Override
    public List<Marker> getConstructsForGene(Marker gene) {

        List<MarkerRelationship.Type> markerRelationshipList = new ArrayList<MarkerRelationship.Type>();
        markerRelationshipList.add(MarkerRelationship.Type.PROMOTER_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        markerRelationshipList.add(MarkerRelationship.Type.CONTAINS_REGION);

        String hql = " select m from MarkerRelationship mr1 , MarkerRelationship  mr2, Marker m " +
                " where mr1.secondMarker.zdbID=mr2.secondMarker.zdbID " +
                " and m.zdbID=mr2.firstMarker.zdbID " +
                " and mr1.firstMarker.zdbID = :markerZdbID " +
                " and mr1.type = :markerRelationshipType1 " +
                " and mr2.type in (:markerRelationshipType2) " +
                "  ";

        return HibernateUtil.currentSession().createQuery(hql)
                .setString("markerZdbID", gene.getZdbID())
                .setParameter("markerRelationshipType1", MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT)
                .setParameterList("markerRelationshipType2", markerRelationshipList)
                .list()
                ;
    }


    public List<Marker> getCodingSequence(Marker gene) {


        String hql = " select m from MarkerRelationship mr1,  Marker m " +
                " where mr1.secondMarker.zdbID=m.zdbID " +
                " and mr1.firstMarker.zdbID = :markerZdbID " +
                " and mr1.type = :markerRelationshipType1 " +
                " ";


        return HibernateUtil.currentSession().createQuery(hql)
                .setString("markerZdbID", gene.getZdbID())
                .setParameter("markerRelationshipType1", MarkerRelationship.Type.CODING_SEQUENCE_OF)

                .list()
                ;
    }

    @Override
    public SequenceTargetingReagent getSequenceTargetingReagent(String markerID) {
        Session session = currentSession();
        return (SequenceTargetingReagent) session.get(SequenceTargetingReagent.class, markerID);
    }

    @Override
    public SequenceTargetingReagent getSequenceTargetingReagentBySequence(Marker.Type type, String sequence) {
        return getSequenceTargetingReagentBySequence(type, sequence, null);
    }

    @Override
    public SequenceTargetingReagent getSequenceTargetingReagentBySequence(Marker.Type type, String sequence1, String sequence2) {
        String hql = "select str from SequenceTargetingReagent str " +
                "where str.markerType.name = :type ";
        if (sequence2 == null) {
            hql += "and str.sequence.sequence = :sequence1 ";
        } else {
            hql +=
                    "and ( " +
                            "   (str.sequence.sequence = :sequence1 and str.sequence.secondSequence = :sequence2) " +
                            "   or " +
                            "   (str.sequence.sequence = :sequence2 and str.sequence.secondSequence = :sequence1) " +
                            ")";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("type", type.toString())
                .setParameter("sequence1", sequence1);
        if (sequence2 != null) {
            query.setParameter("sequence2", sequence2);
        }

        // a database constraint should be enforcing that STRs are unique by sequence. So for convenience
        // just return the first result or null.
        List results = query.list();
        if (results.size() > 0) {
            return (SequenceTargetingReagent) results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Genotype getStrainForTranscript(String zdbID) {

        // TODO: just use where clauses
        String hql = " select g from Genotype g, ProbeLibrary pl , MarkerRelationship mr, Clone c   " +
                "where pl=c.probeLibrary " +
                "and g=pl.strain " +
                "and mr.firstMarker=c " +
                "and mr.secondMarker.zdbID = :zdbID " +
                "and mr.type = :mrType  ";

        return (Genotype) HibernateUtil.currentSession().createQuery(hql)
                .setString("zdbID", zdbID)
                .setParameter("mrType", MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT)
                .uniqueResult();
    }


    @Override
    public List<LinkDisplay> getVegaGeneDBLinksTranscript(Marker gene, DisplayGroup.GroupName summaryPage) {

        if (false == gene.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            logger.error("method only to be used with GENEDOM: " + gene.toString());
            return new ArrayList<LinkDisplay>();
        }

        String sql = "    select distinct fdbdt.fdbdt_data_type, dbl.dblink_length, " +
                "        dbl.dblink_linked_recid," +
                "        dbl.dblink_acc_num," +
                "        fdb.fdb_db_name," +
                "        fdb.fdb_db_query," +
                "        fdb.fdb_url_suffix," +
                "        ra.recattrib_source_zdb_id," +
                "        fdb.fdb_db_significance," +
                "        dbl.dblink_zdb_id " +
                "    from" +
                "        db_link dbl  " +
                "    join" +
                "        foreign_db_contains fdbc " +
                "            on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
                "    join" +
                "        foreign_db fdb " +
                "            on fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id " +
                "    join" +
                "        foreign_db_data_type fdbdt " +
                "            on fdbc.fdbcont_fdbdt_id=fdbdt.fdbdt_pk_id " +
                "    join " +
                "    marker_relationship mr " +
                "    on mr.mrel_mrkr_2_zdb_id=dbl.dblink_linked_recid" +
                "    left outer join" +
                "        record_attribution ra " +
                "            on ra.recattrib_data_zdb_id=dbl.dblink_zdb_id " +
                "    where" +
                "       mr.mrel_mrkr_1_zdb_id = :markerZdbId " +
                "        and " +
                "        mr.mrel_type='gene produces transcript'" +
                "        and " +
                "        fdb.fdb_db_name='VEGA'" + // Ensembl
                " ";

        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("markerZdbId", gene.getZdbID())
                .setResultTransformer(markerDBLinkTransformer);

        List<LinkDisplay> linkDisplay = markerDBLinkTransformer.transformList(query.list());
        return linkDisplay;
    }

    /**
     * Retrieve all engineered region markers.
     *
     * @return
     */
    public List<Marker> getAllEngineeredRegions() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.eq("markerType.name", Marker.Type.EREGION.toString()));
        criteria.addOrder(Order.asc("abbreviationOrder"));
        return (List<Marker>) criteria.list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getClonesForGeneTranscripts(String zdbID) {

        String sql = " select m.mrkr_abbrev, m.mrkr_zdb_id, m.mrkr_abbrev_order, mt.mrkrtype_type_display, " +
                "mrt.mreltype_2_to_1_comments, " +
                "'<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
                "ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,  " +
                "src.srcurl_url, src.srcurl_display_text , mrct.mrel_zdb_id   " +
                "from marker_relationship mrgt " +
                "join marker_relationship mrct on mrct.mrel_mrkr_2_zdb_id=mrgt.mrel_mrkr_2_zdb_id " +
                "join marker_relationship_type mrt on mrt.mreltype_name=mrct.mrel_type " +
                "join marker m on mrct.mrel_mrkr_1_zdb_id=m.mrkr_zdb_id " +
                "join marker_types mt on m.mrkr_type = mt.marker_type " +
                "left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrct.mrel_zdb_id " +
                "left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrct.mrel_mrkr_1_zdb_id " +
                "left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id  " +
                "where mrgt.mrel_mrkr_1_zdb_id = :markerZdbId " +
                "and mrct.mrel_type='clone contains transcript' " +
                "and mrgt.mrel_type='gene produces transcript' " +
                "order by m.mrkr_type asc , m.mrkr_abbrev_order asc ";

        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", zdbID)
                .setResultTransformer(new MarkerRelationshipSupplierPresentationTransformer(true))
                .list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getWeakReferenceMarker(String zdbID, MarkerRelationship.Type type1, MarkerRelationship.Type type2) {
        return getWeakReferenceMarker(zdbID, type1, type2, null);
    }

    @Override
    public List<MarkerRelationshipPresentation> getWeakReferenceMarker(String zdbID
            , MarkerRelationship.Type type1
            , MarkerRelationship.Type type2
            , String resultType) {

        String sql = " select m.mrkr_abbrev, m.mrkr_zdb_id, m.mrkr_abbrev_order, mt.mrkrtype_type_display, " +
                "mrt.mreltype_2_to_1_comments, " +
                "'<a href=\"/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
                "ra.recattrib_source_zdb_id, sup.idsup_supplier_zdb_id , sup.idsup_acc_num,  " +
                "src.srcurl_url, src.srcurl_display_text , mrct.mrel_zdb_id   " +
                "from marker_relationship mrgt " +
                "join marker_relationship mrct on mrct.mrel_mrkr_2_zdb_id=mrgt.mrel_mrkr_2_zdb_id " +
                "join marker_relationship_type mrt on mrt.mreltype_name=mrct.mrel_type " +
                "join marker m on mrct.mrel_mrkr_1_zdb_id=m.mrkr_zdb_id " +
                "join marker_types mt on m.mrkr_type = mt.marker_type " +
                "left outer join record_attribution ra on ra.recattrib_data_zdb_id=mrct.mrel_zdb_id " +
                "left outer join int_data_supplier sup on sup.idsup_data_zdb_id=mrct.mrel_mrkr_1_zdb_id " +
                "left outer join source_url src on sup.idsup_supplier_zdb_id=src.srcurl_source_zdb_id  " +
                "where mrgt.mrel_mrkr_1_zdb_id = :markerZdbId " +
//                "and mrct.mrel_type='clone contains transcript' " +
//                "and mrgt.mrel_type='gene produces transcript' "  +
                "and mrct.mrel_type=:markerRelationshipType1 " +
                "and mrgt.mrel_type=:markerRelationshipType2  " +
                "order by m.mrkr_type asc , m.mrkr_abbrev_order asc ";

        List<MarkerRelationshipPresentation> markers = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", zdbID)
                .setString("markerRelationshipType1", type1.toString())
                .setString("markerRelationshipType2", type2.toString())
                .setResultTransformer(new MarkerRelationshipSupplierPresentationTransformer(true))
                .list();

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

        String hql = "select distinct mutant from Genotype mutant, GenotypeFeature genoFtr, FeatureMarkerRelationship fmRel " +
                "      where fmRel.marker.zdbID = :geneID " +
                "        and fmRel.feature = genoFtr.feature" +
                "        and genoFtr.genotype = mutant" +
                "        and mutant.wildtype = 'f'" +
                "   order by mutant.nameOrder ";

        Query query = session.createQuery(hql);
        query.setString("geneID", geneID);

        return (List<Genotype>) query.list();
    }

    /**
     * Retrieve list of Feature objects with the features created with a TALEN or CRISPR
     *
     * @param sequenceTargetingReagent (TALEN or CRISPR)
     * @return list of Feature
     */
    public List<Feature> getFeaturesBySTR(SequenceTargetingReagent sequenceTargetingReagent) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct feat from Feature feat, FeatureMarkerRelationship fmRel " +
                "      where fmRel.marker = :str " +
                "        and fmRel.feature = feat" +
                "        and fmRel.type = 'created by'" +
                "   order by feat.name ";

        Query query = session.createQuery(hql);
        query.setParameter("str", sequenceTargetingReagent);

        return (List<Feature>) query.list();
    }

    @Override
    public List<SupplierLookupEntry> getSupplierNamesForString(String lookupString) {
        String hql = " select o FROM Organization o " +
                "where " +
                "lower(o.name) like :lookupString " +
                "order by o.name  ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("lookupString", "%" + lookupString.toLowerCase() + "%")
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] supplierNames) {
                        Organization o = (Organization) tuple[0];
                        SupplierLookupEntry supplierSuggestionList = new SupplierLookupEntry();
                        supplierSuggestionList.setId(o.getZdbID());
                        supplierSuggestionList.setLabel(o.getName());
                        supplierSuggestionList.setValue(o.getName());
                        return supplierSuggestionList;
                    }
                })
                .list()
                ;
    }

    @Override
    public List<TargetGeneLookupEntry> getTargetGenesWithNoTranscriptForString(String lookupString) {

         List<MarkerType> markerTypes = getMarkerTypesByGroup(Marker.TypeGroup.GENEDOM_AND_NTR);
        String hql = " select targetGene from Marker targetGene " +
                "where " +
                "lower(targetGene.abbreviation) like :lookupString " +
                "and targetGene.markerType in (:markerType)  " +
                "order by targetGene.abbreviation  ";

        return HibernateUtil.currentSession().createQuery(hql)
                .setString("lookupString", "%" + lookupString.toLowerCase() + "%")
                .setParameterList("markerType", markerTypes)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] targetGeneAbrevs) {
                        Marker targetGene = (Marker) tuple[0];
                        TargetGeneLookupEntry targetGeneSuggestionList = new TargetGeneLookupEntry();
                        targetGeneSuggestionList.setId(targetGene.getZdbID());
                        targetGeneSuggestionList.setLabel(targetGene.getAbbreviation());
                        targetGeneSuggestionList.setValue(targetGene.getAbbreviation());
                        return targetGeneSuggestionList;
                    }
                })
                .list()
                ;
    }






    public List<LookupEntry> getConstructComponentsForString(String lookupString, String zdbId) {


        String sqlQuery = "select mrkr_abbrev as abbrev, mrkr_type as type from marker, record_attribution ra,marker_type_group_member m " +
                "where " +
                "lower(mrkr_abbrev) like :lookupString " +
                "and mrkr_type=m.mtgrpmem_mrkr_type and m.mtgrpmem_mrkr_type_group in ('CONSTRUCT_COMPONENTS') " +
                "and mrkr_zdb_id=ra.recattrib_data_zdb_id and ra.recattrib_source_type = :standard and ra.recattrib_source_zdb_id = :pubZdbID " +
                "UNION " +
                "select cv_term_name as abbrev,cv_name_definition as type from controlled_vocabulary " +
                "where lower(cv_term_name) like :lookupString ";


        List<Object[]> results = HibernateUtil.currentSession().createSQLQuery(sqlQuery)
                .setString("lookupString", "%" + lookupString.toLowerCase() + "%")
                .setString("pubZdbID", zdbId)
                .setString("standard", RecordAttribution.SourceType.STANDARD.toString())
                .list();

        List<LookupEntry> targetGeneSuggestionList = new ArrayList<>();
        for (Object[] objects : results) {
            TargetGeneLookupEntry probe = new TargetGeneLookupEntry();
            probe.setLabel((String) objects[0] + " (" + (String) objects[1] + ")");
            probe.setValue((String) objects[0]);
            targetGeneSuggestionList.add(probe);
        }
        return targetGeneSuggestionList;


    }


    public List<Marker> getMarkersContainedIn(Marker marker, MarkerRelationship.Type... types) {
        Query query = HibernateUtil.currentSession().createQuery(
                "select m from  Marker as m, MarkerRelationship as rel " +
                        "where rel.firstMarker = :marker  and rel.secondMarker = m and " +
                        "rel.type in :relationshipTypes");
        query.setParameter("marker", marker);
        query.setParameterList("relationshipTypes", types);
        List<Marker> list = (List<Marker>) query.list();
        query = HibernateUtil.currentSession().createQuery(
                "select m from  Marker as m, MarkerRelationship as rel " +
                        "where rel.secondMarker = :marker  and rel.firstMarker = m and " +
                        "rel.type in :relationshipTypes");
        query.setParameter("marker", marker);
        query.setParameterList("relationshipTypes", types);
        list.addAll((List<Marker>) query.list());
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }


    @Override
    public List<Marker> getRelatedGenesViaTranscript(Marker marker, MarkerRelationship.Type relType1, MarkerRelationship.Type relType2) {
        Query query = HibernateUtil.currentSession().createQuery(
                "select distinct m from  Marker as m, MarkerRelationship as rel1,MarkerRelationship as rel2 " +
                        "where rel1.firstMarker = :marker and " +
                        "rel1.type =:relType1 and " +
                        "rel1.secondMarker=rel2.secondMarker " +
                        "and rel2.type=:relType2 and " +
                        " rel2.firstMarker=m");
        query.setParameter("marker", marker);
        query.setParameter("relType1", relType1);
        query.setParameter("relType2", relType2);

        List<Marker> list = (List<Marker>) query.list();
        //list.addAll((List<Marker>) query.list());
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }


    @Override
    public Marker getMarkerByFeature(Feature feature) {
        String hql = "select fmr.marker from FeatureMarkerRelationship as fmr " +
                "where " +
                " fmr.feature = :feature " +
                " and fmr.type in (:types)";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("feature", feature);
        query.setParameterList("types", (new FeatureMarkerRelationshipTypeEnum[]{FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF}));
        return (Marker) query.uniqueResult();
    }

    @Override
    public String getAccessionNumber(Marker marker, Database.AvailableAbbrev database) {
        String hql = "from MarkerDBLink " +
                "where referenceDatabase.primaryBlastDatabase.abbrev = :database " +
                "and marker = :marker ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("database", database);
        query.setParameter("marker", marker);
        List<MarkerDBLink> dbLinks = (List<MarkerDBLink>) query.list();
        if (CollectionUtils.isEmpty(dbLinks)) {
            return null;
        }
        if (CollectionUtils.isNotEmpty(dbLinks) && dbLinks.size() > 1) {
            logger.error("More than one accession number found for " + marker.getAbbreviation() + " and Database " + database.toString());
        }
        return dbLinks.get(0).getAccessionNumber();
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
        String sqlCount = " select MAX(cc_cassette_number) from construct_component where cc_construct_zdb_id=:zdbID ";
        Query query = currentSession().createSQLQuery(sqlCount);
        query.setString("zdbID", zdbID);
        Session session = HibernateUtil.currentSession();
        final int maxCassettes = (Integer) query.uniqueResult();

        String sql = " select a.construct_name,a.construct_comments,a.construct_zdb_id" +
                " from construct a " +
                " where a.construct_zdb_id =:zdbID ";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("zdbID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
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
                })
                .list()
                ;

    }

    @Override
    public List<TargetGeneLookupEntry> getGenesForMerge(String lookupString) {
        String hql = " select gene from Marker gene " +
                "where " +
                "lower(gene.abbreviation) like :lookupString " +
                "and gene.markerType.name in (:type1 , :type2) " +
                "order by targetGene.abbreviation  ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("lookupString", "%" + lookupString.toLowerCase() + "%")
                .setString("type1", "GENE")
                .setString("type2", "GENEP")
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] targetGeneAbrevs) {
                        Marker geneMergedInto = (Marker) tuple[0];
                        TargetGeneLookupEntry geneSuggestionList = new TargetGeneLookupEntry();
                        geneSuggestionList.setId(geneMergedInto.getZdbID());
                        geneSuggestionList.setLabel(geneMergedInto.getAbbreviation());
                        geneSuggestionList.setValue(geneMergedInto.getAbbreviation());
                        return geneSuggestionList;
                    }
                })
                .list()
                ;
    }

    @Override
    public List<TranscriptPresentation> getTranscriptsForGeneId(String geneZdbId) {
        Session session = HibernateUtil.currentSession();

        String hql = "select markerRelationship.secondMarker.zdbID, markerRelationship.secondMarker.name from MarkerRelationship markerRelationship " +
                "      where markerRelationship.type = :type  " +
                "        and markerRelationship.firstMarker.zdbID = :geneID " +
                "   order by markerRelationship.secondMarker.abbreviationOrder ";

        return HibernateUtil.currentSession().createQuery(hql)
                .setParameter("type", MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT)
                .setString("geneID", geneZdbId)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public TranscriptPresentation transformTuple(Object[] tuple, String[] aliases) {
                        TranscriptPresentation transcriptPresentation = new TranscriptPresentation();
                        transcriptPresentation.setZdbID(tuple[0].toString());
                        transcriptPresentation.setName(tuple[1].toString());
                        return transcriptPresentation;
                    }

                })
                .list();
    }

    @Override
    public List<SequenceTargetingReagentLookupEntry> getSequenceTargetingReagentForString(String lookupString, String type) {
        String hql = " select mo from Marker mo " +
                "where " +
                "lower(mo.abbreviation) like :lookupString " +
                "and mo.markerType.name = :type " +
                "order by mo.abbreviation  ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("lookupString", "%" + lookupString.toLowerCase() + "%")
                .setString("type", type)
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

        Session session = HibernateUtil.currentSession();

        String hql = "select markerRelationship.secondMarker.zdbID, markerRelationship.secondMarker.abbreviation from MarkerRelationship markerRelationship " +
                "      where markerRelationship.type = :type  " +
                "        and markerRelationship.firstMarker.zdbID = :sequenceTargetingReagentID " +
                "   order by markerRelationship.secondMarker.abbreviationOrder ";

        return HibernateUtil.currentSession().createQuery(hql)
                .setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE)
                .setString("sequenceTargetingReagentID", sequenceTargetingReagent.getZdbID())
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public TargetGenePresentation transformTuple(Object[] tuple, String[] aliases) {
                        TargetGenePresentation targetGenePresentation = new TargetGenePresentation();
                        targetGenePresentation.setZdbID(tuple[0].toString());
                        targetGenePresentation.setSymbol(tuple[1].toString());
                        return targetGenePresentation;
                    }

                })
                .list();
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
        Query query = session.createQuery(hql);

        query.setParameter("firstMarker", firstMarker);
        query.setParameter("type", relationshipType);

        return (List<Marker>) query.list();
    }

    public List<MarkerRelationship> getMarkerRelationshipBySecondMarker(Marker secondMarker) {
        if (secondMarker == null) {
            return null;
        }

        String hql = "select rel from MarkerRelationship as rel  " +
                "where rel.secondMarker = :secondMarker " +
                "order by rel.firstMarker.abbreviationOrder";

        Session session = currentSession();
        Query query = session.createQuery(hql);

        query.setParameter("secondMarker", secondMarker);


        return (List<MarkerRelationship>) query.list();
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

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("firstMarker", marker);
        query.setParameterList("typeList", types);

        PaginationResult<Marker> markerPaginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(paginationBean.getMaxDisplayRecordsInteger(), query.scroll());

        // second related elements
        hql = "select rel.firstMarker from MarkerRelationship as rel  " +
                "where rel.secondMarker = :secondMarker " +
                "and rel.type in (:typeList) " +
                "order by rel.firstMarker.abbreviationOrder";

        Query query2 = HibernateUtil.currentSession().createQuery(hql);
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

        Query query = session.createQuery(sql);
        query.setParameter("gene", marker);
        query.setParameter("organism", Species.Type.HUMAN.toString());
        return query.list();
    }

    public List<Marker> getZfinOrtholog(String humanAbbrev) {
        Session session = HibernateUtil.currentSession();
        String sql = "select zebrafishGene FROM Ortholog ortholog " +
                "WHERE ortholog.symbol = :symbol " +
                "AND ortholog.organism.commonName = :organism ";

        Query query = session.createQuery(sql);
        query.setParameter("symbol", humanAbbrev);
        query.setParameter("organism", Species.Type.HUMAN.toString());
        return query.list();
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
                    addMarkerRelationshipAttribution(promMRel, pr.getPublication(pubID), marker);
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
                    addMarkerRelationshipAttribution(codingRel, pr.getPublication(pubID), marker);
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
            logger.error("Note " + note.getZdbID() + " not associated with marker " + marker.getZdbID());
            return;
        }

        String oldNote = note.getNote();
        note.setNote(newNote);
        currentSession().save(note);
        InfrastructureService.insertUpdate(marker, "curator note", oldNote, newNote);
    }

    public void removeCuratorNote(Marker marker, DataNote note) {
        //   logger.info("remove curator note: " + noteDTO.getNoteData() + " - " + noteDTO.getZdbID());

        Set<DataNote> dataNotes = marker.getDataNotes();
        for (DataNote dataNote : dataNotes) {
            if (dataNote.getZdbID().equals(note.getZdbID())) {
                HibernateUtil.createTransaction();
                InfrastructureService.insertUpdate(marker, "removed curator note " + dataNote.getNote());
                HibernateUtil.currentSession().delete(dataNote);
                HibernateUtil.flushAndCommitCurrentSession();
                return;
            }
        }
        logger.error("note not found with zdbID: " + note.getZdbID());
    }

    public int getCrisprCount(String geneAbbrev) {
        Session session = currentSession();
        String hql = "select rel.firstMarker from MarkerRelationship as rel  " +
                "where rel.secondMarker.zdbID = :geneAbbrev and rel.type = :type " +
                "and rel.firstMarker.markerType like '%CRISPR%'  order by rel.secondMarker.abbreviationOrder";
        Query query = session.createQuery(hql);
        query.setParameter("geneAbbrev", geneAbbrev);
        query.setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);

        List<Marker> targetGenes = (List<Marker>) query.list();
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

        //accessions will end up in the fast search table associated with the marker
        runMarkerNameFastSearchUpdate(marker);

        return mdb;
    }

    @Override
    public List<Marker> getMarkerByGroup(Marker.TypeGroup group, int number) {
        MarkerTypeGroup type = getMarkerTypeGroupByName(group.name());
        String hql = "from Marker as marker " +
                "where marker.markerType.name in (:names) order by marker.abbreviationOrder ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("names", type.getTypeStrings());
        if (number > 0)
            query.setMaxResults(number);
        return query.list();
    }

    @Override
    public Map<String, GenericTerm> getSoTermMapping() {
        String hql = "from ZfinSoTerm";

        List<ZfinSoTerm> terms = HibernateUtil.currentSession().createQuery(hql).list();
        Map<String, GenericTerm> map = new HashMap<>(terms.size());
        for (ZfinSoTerm term : terms) {
            map.put(term.getEntityName(), getOntologyRepository().getTermByOboID(term.getOboID()));
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
                .setString("seq_sequence", seq1)
                .setString("zdbID", str2.getZdbID())
                .executeUpdate();
        String seq2;
        if (str1.getSequence().getSecondSequence() != null) {
            seq2 = str1.getSequence().getSecondSequence();
            HibernateUtil.currentSession().createSQLQuery(
                    "UPDATE marker_sequence " +
                            "SET seq_sequence_2 = :seq_sequence_2 " +
                            "WHERE seq_mrkr_zdb_id = :zdbID ")
                    .setString("seq_sequence_2", seq2)
                    .setString("zdbID", str2.getZdbID())
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
                .setString("lookupString", "%" + lookupString.toLowerCase() + "%")
                .setString("type", type)
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

}

