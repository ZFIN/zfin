package org.zfin.marker.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.zfin.framework.HibernateUtil;
import static org.zfin.framework.HibernateUtil.currentSession;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.MappedMarker;
import org.zfin.marker.*;
import org.zfin.mutant.FeatureMarkerRelationship;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.Species;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class HibernateMarkerRepository implements MarkerRepository {

    private static Logger LOG = Logger.getLogger(HibernateMarkerRepository.class);


    public Marker getMarker(Marker marker) {
        Session session = currentSession();
        return (Marker) session.get(Marker.class, marker.getZdbID());
    }

    public Marker getMarkerByID(String zdbID) {
        Session session = currentSession();
        return (Marker) session.get(Marker.class, zdbID);
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
        List<Marker> markerList = new ArrayList<Marker>() ;
        Session session = currentSession();

        Criteria criteria1 = session.createCriteria(Marker.class);
        criteria1.add(Restrictions.like("abbreviation", name, MatchMode.START));
        criteria1.addOrder(Order.asc("abbreviation")) ;
        markerList.addAll(criteria1.list()) ;

        Criteria criteria2 = session.createCriteria(Marker.class);
        criteria2.add(Restrictions.like("abbreviation", name, MatchMode.ANYWHERE));
        criteria2.add(Restrictions.not(Restrictions.like("abbreviation", name, MatchMode.START)));
        criteria2.addOrder(Order.asc("abbreviation")) ;
        markerList.addAll(criteria2.list()) ;
        return markerList ;
    }


    public MarkerRelationship getSpecificMarkerRelationship(Marker firstMarker, Marker secondMarker, MarkerRelationship.Type type) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(MarkerRelationship.class);
        criteria.add(Restrictions.eq("firstMarker", firstMarker));
        criteria.add(Restrictions.eq("secondMarker", secondMarker));
        criteria.add(Restrictions.eq("type", type));
        return (MarkerRelationship) criteria.uniqueResult();
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
                        "   and sm.zdbID = mm.markerId " +
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
                        "   and fm.zdbID = mm.markerId " +
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
                        "   and fmr.featureZdbId = mm.markerId " +
                        "   and fmr.type = :relationship ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("relationship", FeatureMarkerRelationship.IS_ALLELE_OF);
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
                        "   and fmr.featureZdbId = lf.zdbID " +
                        "   and fmr.type = :relationship ");

        query.setParameter("zdbId", marker.getZdbID());
        query.setParameter("relationship", FeatureMarkerRelationship.IS_ALLELE_OF);
        lgList.addAll(query.list());

        return lgList;
    }

    //if we end up with more than a couple of these, it should get
    //generalized..
    public void addSmallSegmentToGene(Marker segment, Marker gene, String sourceZdbID) {

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

    public void addMarkerDataNote(Marker marker, String note, Person curator) {
        LOG.debug("enter addMarDataNote");
        DataNote dnote = new DataNote();
        dnote.setDataZdbID(marker.getZdbID());
        LOG.debug("markerZdbId for datanote: " + marker.getZdbID());
        dnote.setCurator(curator);
        dnote.setDate(new Date());
        dnote.setNote(note);
        LOG.debug("data note curator: " + curator.toString());
        Set<DataNote> dataNotes = marker.getDataNotes();
        if (dataNotes == null) {
            dataNotes = new HashSet<DataNote>();
            dataNotes.add(dnote);
            marker.setDataNotes(dataNotes);
        } else dataNotes.add(dnote);

        HibernateUtil.currentSession().save(dnote);
        LOG.debug("dnote zdb_id: " + dnote.getZdbID());
    }

    public void addMarkerAlias(Marker marker, String alias, String attributionZdbID) {
        //first handle the alias..
        MarkerAlias markerAlias = new MarkerAlias();
        markerAlias.setMarker(marker);
        markerAlias.setGroup(MarkerAlias.Group.ALIAS);  //default for database, hibernate tries to insert null
        markerAlias.setAlias(alias);
        if (marker.getAliases() == null) {
            Set<MarkerAlias> markerAliases = new HashSet<MarkerAlias>();
            markerAliases.add(markerAlias);
            marker.setAliases(markerAliases);
        } else marker.getAliases().add(markerAlias);

        currentSession().save(markerAlias);

        //now handle the attribution
        RecordAttribution recattrib = new RecordAttribution();
        recattrib.setDataZdbID(markerAlias.getZdbID());
        recattrib.setSourceZdbID(attributionZdbID);
        currentSession().save(recattrib);
    }

    public void addDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID) {
        MarkerDBLink mdb = new MarkerDBLink();
        mdb.setMarker(marker);
        mdb.setAccessionNumber(accessionNumber);
        mdb.setReferenceDatabase(refdb);
        Set<MarkerDBLink> markerDBLinks = marker.getDbLinks();
        if (markerDBLinks == null) {
            markerDBLinks = new HashSet<MarkerDBLink>();
            markerDBLinks.add(mdb);
            marker.setDbLinks(markerDBLinks);
        } else
            marker.getDbLinks().add(mdb);
        currentSession().save(mdb);
        RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(mdb.getZdbID(), attributionZdbID);
    }

    public void addOrthoDBLink(Orthologue orthologue, EntrezProtRelation accession) {
        /*OrthologueDBLink odb = new OrthologueDBLink();
        odb.setOrthologue(orthologue);
        odb.setAccessionNumber(accession.getNumber());
        odb.setReferenceDatabase(accession.getReferenceDatabase());
        currentSession().save(odb);*/
        if (accession == null)
            return;
        /*for (EntrezProtRelation accessionOrthologue : accession) {
            if (accessionOrthologue != null) {
                OrthologueDBLink oldb = new OrthologueDBLink();
                oldb.setOrthologue(orthologue);
                oldb.setAccessionNumber(accessionOrthologue.getEntrezAccession().getEntrezAccNum());
                oldb.setReferenceDatabase(accessionOrthologue.getRefDB());
                currentSession().save(oldb);
            }*/

        if (orthologue.getOrganism() == Species.MOUSE) {
            for (EntrezMGI mgiOrthologue : accession.getEntrezAccession().getRelatedMGIAccessions()) {
                if (mgiOrthologue != null) {
                    OrthologueDBLink oldb = new OrthologueDBLink();
                    oldb.setOrthologue(orthologue);
                    oldb.setAccessionNumber(accession.getEntrezAccession().getEntrezAccNum());
                    oldb.setReferenceDatabase(accession.getMouserefDB());
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
                    humoldb.setReferenceDatabase(accession.getHumanrefDB());
                    currentSession().save(humoldb);
                    OrthologueDBLink omimoldb = new OrthologueDBLink();
                    omimoldb.setOrthologue(orthologue);
                    omimoldb.setAccessionNumber(omimOrthologue.getOmimAccession().replaceAll("MIM:", ""));
                    omimoldb.setReferenceDatabase(omimOrthologue.getRefDB());
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

    public MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason resason) {
        if (event == MarkerHistory.Event.RENAMED) {
            MarkerHistory history = new MarkerHistory();
            history.setDate(new Date());
            history.setName(newMarker.getName());
            history.setAbbreviation(newMarker.getAbbreviation());
            history.setMarker(newMarker);
            history.setEvent(event.name());
            history.setOldMarkerName(oldMarker.getName());
            // The reason should be passed
            history.setReason(resason);
            currentSession().save(history);
            return history;
        }
        return null;
    }

    public List<MarkerFamilyName> getMarkerFamilyNamesBySubstring(String substring) {

        ArrayList<MarkerFamilyName> families = new ArrayList<MarkerFamilyName>();

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

    public void save(Object o) {
        Session session = currentSession();
        session.beginTransaction();
        session.saveOrUpdate(o);
        session.getTransaction().commit();
    }

    /**
     * This executes the regen_names_marker() procedure.
     * Since InformixDialect does not support stored procedures
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
            LOG.info("Execute stored procedure: " + sql + " with the argument " + zdbID);
        } catch (SQLException e) {
            LOG.error("Could not run: " + sql, e);
        } finally {
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.error(e);
                }
        }
    }

    public void createMarker(Marker marker, Publication pub) {
        if (marker == null)
            throw new RuntimeException("No marker object provided.");
        if (pub == null)
            throw new RuntimeException("Cannot create a new marker without a publication.");

        currentSession().save(marker);
        // Need to flush here to make the trigger fire as that will
        // create a MarkerHistory record needed.
        currentSession().flush();

        //add publication to attribution list.
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        ir.insertRecordAttribution(marker.getZdbID(), pub.getZdbID());

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
        MarkerTypeGroup markerTypeGroup = (MarkerTypeGroup) session.load(MarkerTypeGroup.class, name);
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
            LOG.error("No Marker Alias created! ");
            throw new RuntimeException("No Marker History record found! Trigger did not run.");
        }
        //add record attribution for previous name if the abbrevation was changed
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        LOG.info("marker history: " + mhist);
        LOG.info("marker alias: " + mhist.getMarkerAlias());
        LOG.info("publication: " + publication);

        ir.insertRecordAttribution(mhist.getMarkerAlias().getZdbID(), publication.getZdbID());

    }

}
