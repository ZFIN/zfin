package org.zfin.marker.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;
import org.zfin.ExternalNote;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.database.DbSystemUtil;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureFigure;
import org.zfin.expression.Image;
import org.zfin.expression.TextOnlyFigure;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.MappedMarker;
import org.zfin.marker.*;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerRelationshipPresentationTransformer;
import org.zfin.ontology.GenericTerm;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.Species;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.service.SequenceService;
import org.zfin.util.NumberAwareStringComparator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;


public class HibernateMarkerRepository implements MarkerRepository {

    private static Logger logger = Logger.getLogger(HibernateMarkerRepository.class);
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private final SequenceService sequenceService = new SequenceService();

    public Marker getMarker(Marker marker) {
        Session session = currentSession();
        return (Marker) session.get(Marker.class, marker.getZdbID());
    }

    public Marker getMarkerByID(String zdbID) {
        Session session = currentSession();
        return (Marker) session.get(Marker.class, zdbID);
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
                , Hibernate.STRING
        ));
        return (Marker) criteria.uniqueResult();
    }

    public Marker getMarkerByAbbreviation(String abbreviation) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.eq("abbreviation", abbreviation));
        return (Marker) criteria.uniqueResult();
    }

    public Marker getMarkerByName(String name) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.add(Restrictions.eq("name", name));
        return (Marker) criteria.uniqueResult();
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

    public TreeSet<String> getLG(Marker marker) {
        Session session = currentSession();
        TreeSet<String> lgList = new TreeSet<String>();

        // a) add self panel mapping
        for (MappedMarker mm : marker.getDirectPanelMappings()) {
            if (mm != null) {
                lgList.add(mm.getLg());
            }
        }

        // b) add related(second) marker panel mapping
        Query query = session.createQuery(
                "select mm.lg " +
                        "from MappedMarker mm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where fm.zdbID = :zdbId " +
                        "   and sm.zdbID = mm.marker.zdbID " +
                        "   and mr.type in (:firstRelationship, :secondRelationship, :thirdRelationship)");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);
        query.setParameter("thirdRelationship", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // c) add related(first) marker panel mapping
        query = session.createQuery(
                "select mm.lg " +
                        "from MappedMarker mm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where sm.zdbID = :zdbId " +
                        "   and fm.zdbID = mm.marker.zdbID " +
                        "   and mr.type in (:firstRelationship, :secondRelationship) ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // d) add allele panel mapping
        query = session.createQuery(
                "select mm.lg" +
                        "  from MappedMarker mm, FeatureMarkerRelationship fmr " +
                        " where fmr.marker.zdbID = :zdbId " +
                        "   and fmr.feature.zdbID = mm.marker.zdbID " +
                        "   and fmr.type = :relationship ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("relationship", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        lgList.addAll(query.list());

        // e) add self linkage mapping
        query = session.createQuery(
                "select l.lg " +
                        "from Linkage l join l.linkageMemberMarkers as m " +
                        " where m.zdbID = :zdbId ");
        query.setParameter("zdbId", marker.getZdbID());
        lgList.addAll(query.list());

        // f) add related(second) marker linkage mapping
        query = session.createQuery(
                "select l.lg " +
                        "from Linkage l join l.linkageMemberMarkers as lm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where fm.zdbID = :zdbId " +
                        "   and sm.zdbID = lm.zdbID " +
                        "   and mr.type in (:firstRelationship, :secondRelationship, :thirdRelationship)");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);
        query.setParameter("thirdRelationship", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // g) add related(first) marker linkage mapping
        query = session.createQuery(
                "select l.lg " +
                        "from Linkage l join l.linkageMemberMarkers as lm, MarkerRelationship mr join mr.firstMarker as fm" +
                        "     join mr.secondMarker as sm        " +
                        " where sm.zdbID = :zdbId " +
                        "   and fm.zdbID = lm.zdbID " +
                        "   and mr.type in (:firstRelationship, :secondRelationship) ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        query.setParameter("secondRelationship", MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT);

        lgList.addAll(query.list());

        // h) add allele linkage mapping
        query = session.createQuery(
                "select l.lg" +
                        "  from Linkage l join l.linkageMemberFeatures as lf, FeatureMarkerRelationship fmr " +
                        " where fmr.marker.zdbID = :zdbId " +
                        "   and fmr.feature.zdbID = lf.zdbID " +
                        "   and fmr.type in (:firstRelationship, :secondRelationship, :thirdRelationship) ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("firstRelationship", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        query.setParameter("secondRelationship", FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT);
        query.setParameter("thirdRelationship", FeatureMarkerRelationshipTypeEnum.MARKERS_MISSING);
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
            secondSegmentRelationships = new HashSet<MarkerRelationship>();
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
            Set<PublicationAttribution> pubattr = new HashSet<PublicationAttribution>();
            pubattr.add(pa);
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
            firstMarkerRelationships = new HashSet<MarkerRelationship>();
            firstMarkerRelationships.add(mrel);
            gene.setFirstMarkerRelationships(firstMarkerRelationships);
        } else
            firstMarkerRelationships.add(mrel);

        Set<MarkerRelationship> secondSegmentRelationships = segment.getSecondMarkerRelationships();
        if (secondSegmentRelationships == null) {
            secondSegmentRelationships = new HashSet<MarkerRelationship>();
            secondSegmentRelationships.add(mrel);
            segment.setSecondMarkerRelationships(secondSegmentRelationships);
        } else
            secondSegmentRelationships.add(mrel);

        //now deal with attribution
        RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(mrel.getZdbID(), sourceZdbID);
    }


    public DataNote addMarkerDataNote(Marker marker, String note, Person curator) {
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
        } else dataNotes.add(dnote);


        HibernateUtil.currentSession().save(dnote);
        logger.debug("dnote zdb_id: " + dnote.getZdbID());
        return dnote;
    }

    public AntibodyExternalNote addAntibodyExternalNote(Antibody antibody, String note, String sourceZdbID) {
        logger.debug("enter addExtDataNote");
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        Person currentUser = Person.getCurrentSecurityUser();
        AntibodyExternalNote externalNote = new AntibodyExternalNote();
        externalNote.setAntibody(antibody);
        externalNote.setNote(note);
        externalNote.setType(ExternalNote.Type.ANTIBODY.toString());
        HibernateUtil.currentSession().save(externalNote);
        if (!sourceZdbID.equals("")) {
            PublicationAttribution pa = new PublicationAttribution();
            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            Publication publication = pr.getPublication(sourceZdbID);
            pa.setPublication(publication);
            pa.setDataZdbID(externalNote.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            Set<PublicationAttribution> pubattr = new HashSet<PublicationAttribution>();
            pubattr.add(pa);
            externalNote.setPubAttributions(pubattr);
            if (antibody.getExternalNotes() == null) {
                Set<AntibodyExternalNote> abExtNote = new HashSet<AntibodyExternalNote>();
                abExtNote.add(externalNote);
                antibody.setExternalNotes(abExtNote);
            } else {
                antibody.getExternalNotes().add(externalNote);
            }
            currentSession().save(pa);

            addMarkerPub(antibody, publication);
        }
        ir.insertUpdatesTable(antibody, "notes", "", currentUser, note, "");
        return externalNote;
    }

    public void createOrUpdateOrthologyExternalNote(Marker gene, String note) {
        logger.debug("add orthology note");
        Person currentUser = Person.getCurrentSecurityUser();
        if (currentUser == null)
            throw new RuntimeException("Cannot add an orthology note without an authenticated user");

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        if (gene.getOrthologyNotes() == null || gene.getOrthologyNotes().size() == 0) {
            OrthologyNote extnote = new OrthologyNote();
            extnote.setMarker(gene);
            extnote.setNote(note);
            extnote.setType(ExternalNote.Type.ORTHOLOGY.toString());
            HibernateUtil.currentSession().save(extnote);
            PersonAttribution pa = new PersonAttribution();
            pa.setPerson(currentUser);
            pa.setDataZdbID(extnote.getZdbID());
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            HibernateUtil.currentSession().save(pa);
            Set<PersonAttribution> personAttributions = new HashSet<PersonAttribution>();
            personAttributions.add(pa);
            extnote.setPersonAttributions(personAttributions);
            Set<OrthologyNote> markerExternalNotes = new HashSet<OrthologyNote>();
            markerExternalNotes.add(extnote);
            gene.setOrthologyNotes(markerExternalNotes);
        } else {
            OrthologyNote extNote = gene.getOrthologyNotes().iterator().next();
            String oldNote = gene.getOrthologyNotes().iterator().next().getNote();
            extNote.setNote(note);
            ir.insertUpdatesTable(gene, "notes", "", currentUser, note, oldNote);
        }
    }

    public void editAntibodyExternalNote(String notezdbid, String note) {
        logger.debug("enter addExtDataNote");
        ExternalNote extnote = RepositoryFactory.getInfrastructureRepository().getExternalNoteByID(notezdbid);
        extnote.setNote(note);
        HibernateUtil.currentSession().update(extnote);
    }


    /**
     * Create a new alias for a given marker. IF no alias is found no alias is crerated.
     *
     * @param marker      valid marker object.
     * @param alias       alias string
     * @param publication publication
     * @return The created markerAlias
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
            Set<MarkerAlias> markerAliases = new HashSet<MarkerAlias>();
            markerAliases.add(markerAlias);
            marker.setAliases(markerAliases);
        } else
            marker.getAliases().add(markerAlias);

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

            if (marker.getMarkerType().getType() == Marker.Type.ATB)
                addMarkerPub(marker, publication);
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
        if (marker == null)
            throw new RuntimeException("No marker object provided.");
        if (alias == null)
            throw new RuntimeException("No alias object provided.");
        // check that the alias belongs to the marker
        if (!marker.getAliases().contains(alias))
            throw new RuntimeException("Alias '" + alias + "' does not belong to the marker '" + marker + "'! " +
                    "Cannot remove such an alias.");
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
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        ir.deleteActiveDataByZdbID(mrel.getZdbID());

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

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution recordAttribution = ir.getRecordAttribution(aliasZdbID, attributionZdbID, RecordAttribution.SourceType.STANDARD);
        Person currentUser = Person.getCurrentSecurityUser();
        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(aliasZdbID);
            pa.setSourceType(PublicationAttribution.SourceType.STANDARD);
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(attributionZdbID);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubAttrbs = new HashSet<PublicationAttribution>();
            pubAttrbs.add(pa);
            MarkerAlias markerAlias = new MarkerAlias();
            markerAlias.setPublications(pubAttrbs);
            currentSession().save(pa);
            addMarkerPub(marker, publication);
        }
        ir.insertUpdatesTable(marker, "", "new attribution, data alias: " + alias.getAlias() + " with pub: " + attributionZdbID, currentUser, attributionZdbID, "");
    }

    public void addMarkerRelationshipAttribution(MarkerRelationship mrel, Publication attribution, Marker marker) {

        String attributionZdbID = attribution.getZdbID();
        String relZdbID = mrel.getZdbID();

        if (attributionZdbID.equals(""))
            throw new RuntimeException("Cannot attribute this alias with a blank pub.");

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution recordAttribution = ir.getRecordAttribution(relZdbID, attributionZdbID, RecordAttribution.SourceType.STANDARD);
        Person currentUser = Person.getCurrentSecurityUser();

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(relZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(attributionZdbID);
            pa.setPublication(publication);
            currentSession().save(pa);
            currentSession().refresh(mrel);
            addMarkerPub(marker, publication);
        }
        ir.insertUpdatesTable(marker, "", "new attribution, marker relationship: " + mrel.getZdbID() + " with pub: " + attributionZdbID, currentUser, attributionZdbID, "");
    }

    public void addMarkerPub(Marker marker, Publication publication) {
        if (publication == null)
            throw new RuntimeException("Cannot attribute this marker with a blank pub.");

        String markerZdbID = marker.getZdbID();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution recordAttribution = ir.getRecordAttribution(markerZdbID, publication.getZdbID(), RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setDataZdbID(markerZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubAttrbs = new HashSet<PublicationAttribution>();
            pubAttrbs.add(pa);
            Marker mrkr = new Marker();
            mrkr.setPublications(pubAttrbs);
            currentSession().save(pa);
        }
    }

    public MarkerDBLink getDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb) {

        Set<MarkerDBLink> markerDBLinks = marker.getDbLinks();
        for (MarkerDBLink markerDBLink : markerDBLinks) {
            if (
                    markerDBLink.getAccessionNumber().equals(accessionNumber)
                            &&
                            markerDBLink.getReferenceDatabase().equals(refdb)
                    ) {
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
        } else
            marker.getDbLinks().add(mdb);
        currentSession().save(mdb);
        if (StringUtils.isNotEmpty(attributionZdbID)) {
            RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(mdb.getZdbID(), attributionZdbID);
        }

        //accessions will end up in the fast search table associated with the marker
        runMarkerNameFastSearchUpdate(marker);

        return mdb;
    }

    public void addOrthoDBLink(Orthologue orthologue, EntrezProtRelation accession) {
        if (accession == null)
            return;

        if (orthologue.getOrganism() == Species.MOUSE) {
            for (EntrezMGI mgiOrthologue : accession.getEntrezAccession().getRelatedMGIAccessions()) {
                if (mgiOrthologue != null) {
                    OrthologueDBLink oldb = new OrthologueDBLink();
                    oldb.setOrthologue(orthologue);
                    oldb.setAccessionNumber(accession.getEntrezAccession().getEntrezAccNum());
                    oldb.setReferenceDatabase(sequenceService.getEntrezGeneMouseRefDB());
                    currentSession().save(oldb);
                    OrthologueDBLink mgioldb = new OrthologueDBLink();
                    mgioldb.setOrthologue(orthologue);
                    mgioldb.setAccessionNumber(mgiOrthologue.getMgiAccession().replaceAll("MGI:", ""));
                    mgioldb.setReferenceDatabase(mgiOrthologue.getRefDB());
                    currentSession().save(mgioldb);
                }
            }
        }
        if (orthologue.getOrganism() == Species.HUMAN) {

            for (EntrezOMIM omimOrthologue : accession.getEntrezAccession().getRelatedOMIMAccessions()) {
                if (omimOrthologue != null) {
                    OrthologueDBLink humoldb = new OrthologueDBLink();
                    humoldb.setOrthologue(orthologue);
                    humoldb.setAccessionNumber(accession.getEntrezAccession().getEntrezAccNum());
                    humoldb.setReferenceDatabase(sequenceService.getEntrezGeneHumanRefDB());
                    currentSession().save(humoldb);
                    OrthologueDBLink omimoldb = new OrthologueDBLink();
                    omimoldb.setOrthologue(orthologue);
                    omimoldb.setAccessionNumber(omimOrthologue.getOmimAccession().replaceAll("MIM:", ""));
                    omimoldb.setReferenceDatabase(sequenceService.getOMIMHumanOrthologue());
                    currentSession().save(omimoldb);
                }
            }
        }


    }

    public MarkerHistory getLastMarkerHistory(Marker marker, MarkerHistory.Event event) {
        Session session = currentSession();

        //flush here to ensure that triggers for marker inserts and updates are run.
        session.flush();

        Criteria criteria = session.createCriteria(MarkerHistory.class);
        criteria.add(Restrictions.eq("marker.zdbID", marker.getZdbID()));
        // Todo: Check this carefully
        if (event != null)
            criteria.add(Restrictions.eq("event", event.toString()));
        criteria.addOrder(Property.forName("date").desc());
        // very dangerous as the trigger creates two history records, one for a name change (no alias available)
        // and one for an abbrev change with an associated alias generation
        criteria.setMaxResults(1);
        return (MarkerHistory) criteria.uniqueResult();
    }

    public MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason reason, MarkerAlias alias) {
        MarkerHistory history = new MarkerHistory();
        history.setDate(new Date());
        history.setName(newMarker.getName());
        history.setAbbreviation(newMarker.getAbbreviation());
        history.setMarker(newMarker);
        history.setEvent(event.toString());
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
    public void runMarkerNameFastSearchUpdate(Marker marker) {
        Session session = currentSession();
        Connection connection = session.connection();
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
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
        }
    }

    public void createMarker(Marker marker, Publication pub) {
        if (marker == null)
            throw new RuntimeException("No marker object provided.");
        if (marker.getMarkerType() == null)
            throw new RuntimeException("Cannot create a new marker without a type.");
        if (marker.getOwner() == null)
            throw new RuntimeException("Cannot create a new marker without an owner.");
        if (pub == null)
            throw new RuntimeException("Cannot create a new marker without a publication.");

        currentSession().save(marker);
        // Need to flush here to make the trigger fire as that will
        // create a MarkerHistory record needed.
        currentSession().flush();

        //add publication to attribution list.
        RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(marker.getZdbID(), pub.getZdbID());

        // run procedure for fast search table
        runMarkerNameFastSearchUpdate(marker);
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
        if (group == null)
            return null;
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

    @SuppressWarnings("unchecked")
    public List<Marker> getMarkersByAbbreviationGroupAndAttribution(String name, Marker.TypeGroup markerType, String pubZdbId) {
        List<Marker> markerList = new ArrayList<Marker>();

        MarkerTypeGroup group = getMarkerTypeGroupByName(markerType.name());
        if (group == null)
            return null;
        MarkerType[] types = new MarkerType[group.getTypeStrings().size()];
        int index = 0;
        for (String type : group.getTypeStrings()) {
            types[index++] = getMarkerTypeByName(type);
        }

        String hql = " select distinct m from Marker m , PublicationAttribution pa "
                + " where lower(m.abbreviation) like lower(:name)  "
                + " and pa.dataZdbID = m.zdbID  "
                + " and pa.sourceZdbID = :publicationZdbId "
                + " and m.markerType in (:types)  ";
//                + " order by m.abbreviationOrder asc " ;
        markerList.addAll(HibernateUtil.currentSession()
                .createQuery(hql)
                .setString("name", "%" + name + "%")
                .setString("publicationZdbId", pubZdbId)
                .setParameterList("types", types)
                .list());

        Collections.sort(markerList, new MarkerAbbreviationComparator(name));

        return markerList;
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
        if (totalCount == 0)
            return new PaginationResult<HighQualityProbe>(0, null);

        if (includeSubstructures)
            return new PaginationResult<HighQualityProbe>(totalCount, null);

        String sqlQueryStr = " select distinct(stat.fstat_feat_zdb_id), probe.mrkr_abbrev, gene.mrkr_zdb_id," +
                "                       gene.mrkr_abbrev,gene.mrkr_abbrev_order  " +
                "from feature_stats as stat, marker as gene, marker as probe " +
                "     where fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_subterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = gene.mrkr_zdb_id " +
                "           and fstat_feat_zdb_id = probe.mrkr_zdb_id " +
                "           and fstat_type = :type" +
                "     order by gene.mrkr_abbrev_order ";
        SQLQuery sqlQquery = session.createSQLQuery(sqlQueryStr);
        sqlQquery.setString("aoterm", aoTerm.getZdbID());
        sqlQquery.setString("type", "High-Quality-Probe");
        sqlQquery.setFirstResult(pagination.getFirstRecord() - 1);
        sqlQquery.setMaxResults(pagination.getMaxDisplayRecords());
        List<Object[]> objs = sqlQquery.list();
        List<Marker> hqpRecords = new ArrayList<Marker>();
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
                "           and fstat_subterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = gene.mrkr_zdb_id " +
                "           and fstat_feat_zdb_id = probe.mrkr_zdb_id " +
                "           and fstat_type = :type " +
                "           and fstat_fig_zdb_id = fig.fig_zdb_id " +
                "           and fstat_pub_zdb_id = pub.zdb_id " +
                "           and fstat_img_zdb_id = img.img_zdb_id " +
                "     order by gene.mrkr_abbrev_order ";
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
        while (scrollableResults.next() && list.size() < pagination.getMaxDisplayRecords() + 1) {
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
            if (label != null && label.equals(Figure.Type.TOD.toString()))
                figure = new TextOnlyFigure();
            else
                figure = new FigureFigure();

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
            if (hqpRecords.contains(highQualityProbeStats.getProbe()))
                populateProbeStatisticsRecord(highQualityProbeStats, list, aoTerm);
        }
        // remove the last entity as it is beyond the display limit.
        if (list.size() > pagination.getMaxDisplayRecords())
            list.remove(list.size() - 1);
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
        String hql = "select m from PublicationAttribution pa , Marker m " +
                " where pa.dataZdbID=m.zdbID and pa.publication.zdbID= :pubZdbID  " +
                " and pa.sourceType= :sourceType and m.zdbID like :markerType " +
                " order by m.abbreviationOrder ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubZdbID", publication.getZdbID());
        query.setString("sourceType", RecordAttribution.SourceType.STANDARD.toString());
        // yes, this is a hack, should use typeGroup, I guess
        query.setParameter("markerType", "ZDB-" + type + "%");
        return query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Marker> getMarkersForAttribution(String publicationZdbID) {
        String hql = "" +
                " select distinct m from Marker m , RecordAttribution ra " +
                " where ra.dataZdbID=m.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID " +
                " order by m.abbreviationOrder " +
                " ";

        return (List<Marker>) HibernateUtil.currentSession().createQuery(hql)
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

        if (record == null || record.getProbe() == null)
            return;

        HighQualityProbe probeStats;
        if (list.size() == 0) {
            probeStats = new HighQualityProbe(record.getProbe(), aoTerm);
            list.add(probeStats);
        } else
            probeStats = list.get(list.size() - 1);

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
        if (gene != null)
            newProbeStats.addGene(gene);
        Figure figure = record.getFigure();
        if (figure != null)
            newProbeStats.addFigure(figure);
        Publication publication = record.getPublication();
        if (publication != null)
            newProbeStats.addPublication(publication);
        Image image = record.getImage();
        if (image != null)
            newProbeStats.addImage(image);

        if (isNew)
            list.add(newProbeStats);
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

        if (record == null || record.getProbe() == null)
            return;

        HighQualityProbe probeStats;
        if (list.size() == 0) {
            probeStats = new HighQualityProbe(record.getProbe(), aoTerm);
            list.add(probeStats);
        } else
            probeStats = list.get(list.size() - 1);

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
        if (gene != null)
            newProbeStats.addGene(gene);
        Figure figure = record.getFigure();
        if (figure != null)
            newProbeStats.addFigure(figure);
        Publication publication = record.getPublication();
        if (publication != null)
            newProbeStats.addPublication(publication);
        Image image = record.getImage();
        if (image != null)
            newProbeStats.addImage(image);

        if (isNew)
            list.add(newProbeStats);
    }


    public MarkerType getMarkerTypeByName(String name) {
        Session session = currentSession();
        MarkerType type = (MarkerType) session.load(MarkerType.class, name);
        if (type == null || type.getName() == null) {
            return null;
        }
        return type;
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
    public void renameMarker(Marker marker, Publication publication, MarkerHistory.Reason reason) {
        //update marker history reason
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        MarkerHistory mhist = mr.getLastMarkerHistory(marker, MarkerHistory.Event.REASSIGNED);
        mhist.setReason(reason);
        mr.runMarkerNameFastSearchUpdate(marker);

        if (mhist.getMarkerAlias() == null) {
            logger.error("No Marker Alias created! ");
            throw new RuntimeException("No Marker History record found! Trigger did not run.");
        }
        //add record attribution for previous name if the abbreviation was changed
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        logger.info("marker history: " + mhist);
        logger.info("marker alias: " + mhist.getMarkerAlias());
        logger.info("publication: " + publication);

        ir.insertRecordAttribution(mhist.getMarkerAlias().getZdbID(), publication.getZdbID());

    }

    /**
     * Retrieve marker types by marker type groups
     *
     * @param typeGroup type group
     * @return list of marker types
     */
    public List<MarkerType> getMarkerTypesByGroup(Marker.TypeGroup typeGroup) {
        if (typeGroup == null)
            return null;
        MarkerTypeGroup group = getMarkerTypeGroupByName(typeGroup.name());
        List<MarkerType> markerTypes = new ArrayList<MarkerType>();
        for (String type : group.getTypeStrings()) {
            markerTypes.add(getMarkerTypeByName(type));
        }
        return markerTypes;
    }

    /**
     * Retrieve gene for a given Morpholino which is targeting it.
     *
     * @param morpholino valid Morpholino of Marker object.
     * @return the target gene of the Morpholino
     */
    public List<Marker> getTargetGenesForMorpholino(Marker morpholino) {
        if (morpholino == null)
            return null;

        Session session = currentSession();
        Criteria criteria = session.createCriteria(MarkerRelationship.class);
        criteria.add(Restrictions.eq("firstMarker", morpholino));
        criteria.add(Restrictions.eq("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE));
        List<MarkerRelationship> markerRelations = criteria.list();
        if (markerRelations == null)
            return null;

        List<Marker> targetGenes = new ArrayList<Marker>(markerRelations.size());
        for (MarkerRelationship relationship : markerRelations) {
            targetGenes.add(relationship.getSecondMarker());
        }
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
        if (firstN < 0)
            return null;
        if (firstN == 0)
            return getAllMarkers();

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
        if (firstNIds > 0)
            query.setMaxResults(firstNIds);
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

    @Override
    public List<PreviousNameLight> getPreviousNamesLight(final Marker gene) {
        String sql = "  " +
                " select da.dalias_alias, ra.recattrib_source_zdb_id " +
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
                        previousNameLight.setAlias(tuple[0].toString());
                        if (tuple[1] != null) {
                            previousNameLight.setPublicationZdbID(tuple[1].toString());
                        }

                        return previousNameLight;
                    }

                    @Override
                    public List transformList(List list) {

                        Collections.sort(list);

                        return super.transformList(list);    //To change body of overridden methods use File | Settings | File Templates.
                    }
                })
                .list();
    }

    @Override
    public String getVariantForSnp(String zdbID) {
        String sql = "select mrkrseq_variation from marker_sequence where mrkrseq_mrkr_zdb_id=:markerZdbID";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbID", zdbID)
                .uniqueResult().toString();
    }

    @Override
    public List<MarkerSequence> getMarkerSequences(Marker marker) {
        return HibernateUtil.currentSession().createCriteria(MarkerSequence.class)
                .add(Restrictions.eq("marker", marker))
                .list();
    }

    @Override
    public List<MarkerRelationshipPresentation> getRelatedMarkerOrderDisplayExcludeTypes(Marker marker, boolean is1to2, MarkerRelationship.Type... typesNotIn) {
        String sql1To2 = " 	select mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order, mrkrtype_type_display,  " +
                "	       mreltype_1_to_2_comments, " +
                "          '<a href=\"/action/marker/view/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
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
                "          '<a href=\"/action/marker/view/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
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


        ResultTransformer resultTransformer = new MarkerRelationshipPresentationTransformer(is1to2);
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
                "          '<a href=\"/action/marker/view/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
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
                "          '<a href=\"/action/marker/view/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
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

    public List<LinkDisplay> getMarkerDBLinksFast(Marker marker, DisplayGroup.GroupName groupName) {
        ResultTransformer transformer = new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                LinkDisplay linkDisplay = new LinkDisplay();
                linkDisplay.setMarkerZdbID(tuple[0].toString());
                linkDisplay.setAccession(tuple[1].toString());
                linkDisplay.setReferenceDatabaseName(tuple[2].toString());
                linkDisplay.setUrlPrefix(tuple[3].toString());
                if (tuple[4] != null) {
                    linkDisplay.setUrlSuffix(tuple[4].toString());
                }
                if (tuple[5] != null) {
                    linkDisplay.setPublicationZdbID(tuple[5].toString());
                }
                if (tuple[6] != null) {
                    linkDisplay.setSignificance(Integer.valueOf(tuple[6].toString()));
                }
                return linkDisplay;
            }
        };
        String sql = "select dbl.dblink_linked_recid,dbl.dblink_acc_num,fdb.fdb_db_name,fdb.fdb_db_query,fdb.fdb_url_suffix, " +
                "ra.recattrib_source_zdb_id, fdb.fdb_db_significance " +
                "from db_link dbl  " +
                "join foreign_db_contains_display_group_member m on m.fdbcdgm_fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id " +
                "join foreign_db_contains_display_group g on g.fdbcdg_pk_id=m.fdbcdgm_group_id " +
                "join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
                "join foreign_db fdb on fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id " +
                "left outer join record_attribution ra on ra.recattrib_data_zdb_id=dbl.dblink_zdb_id " +
                "where g.fdbcdg_name= :displayGroup " +
                "and " +
                "dbl.dblink_linked_recid= :markerZdbId ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setParameter("displayGroup", groupName.toString())
                .setResultTransformer(transformer);

        List<LinkDisplay> linkDisplay = transformer.transformList(query.list());
        Collections.sort(linkDisplay, new Comparator<LinkDisplay>() {
            @Override
            public int compare(LinkDisplay linkA, LinkDisplay linkB) {
                int compare;
                compare = linkA.getLink().compareTo(linkB.getLink());
                if (compare != 0) return compare;

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
                "	       mreltype_1_to_2_comments, " +
                "          '<a href=\"/action/marker/view/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
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
                "	       mreltype_2_to_1_comments, " +
                "          '<a href=\"/action/marker/view/'||mrkr_zdb_id||'\">'|| mrkr_abbrev || '</a>' , " +
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
                " " ;
        return HibernateUtil.currentSession().createQuery(hql)
                .setParameter("type",markerType.name())
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
                " " ;
        List<Marker> markers = HibernateUtil.currentSession().createQuery(hql)
                .setParameterList("types",types)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        Marker m = new Marker();
                        m.setAbbreviation(tuple[0].toString());
                        m.setZdbID(tuple[1].toString());
                        return m ;
                    }
                })
                .list()
                ;
        Map<String,String> markerCandidates = new HashMap<String,String>();
        for(Marker m : markers){
            markerCandidates.put(m.getAbbreviation(),m.getZdbID());
        }
        return markerCandidates;
    }
}
