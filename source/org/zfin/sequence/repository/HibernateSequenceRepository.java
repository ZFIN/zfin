/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Transcript;
import org.zfin.orthology.Species;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.Origination;

import java.util.*;

public class HibernateSequenceRepository implements SequenceRepository {

    private final Logger logger = Logger.getLogger(HibernateSequenceRepository.class);

    public ForeignDB getForeignDBByName(ForeignDB.AvailableName dbName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ForeignDB.class);
        criteria.add(Restrictions.eq("dbName", dbName));
        return (ForeignDB) criteria.uniqueResult();
    }

    public ReferenceDatabase getReferenceDatabase(ForeignDB.AvailableName foreignDBName, ForeignDBDataType.DataType type, ForeignDBDataType.SuperType superType, Species organism) {

        String hql = " from ReferenceDatabase referenceDatabase " +
                " where referenceDatabase.foreignDB.dbName = :dbName " +
                " and referenceDatabase.foreignDBDataType.dataType = :type" +
                " and referenceDatabase.foreignDBDataType.superType = :superType" +
                " and referenceDatabase.organism  = :organism" +
                " ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("dbName", foreignDBName.toString());
        query.setString("type", type.toString());
        query.setString("superType", superType.toString());
        query.setString("organism", organism.toString());

        return (ReferenceDatabase) query.uniqueResult();
    }

    public ReferenceDatabase getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName foreignDBName,
                                                                   ForeignDBDataType.DataType type) {
        return getReferenceDatabase(foreignDBName, type, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
    }

    public List<ReferenceDatabase> getSequenceReferenceDatabases(ForeignDB.AvailableName name, ForeignDBDataType.DataType type) {

        String hql = " from ReferenceDatabase referenceDatabase " +
                " where referenceDatabase.foreignDB.dbName = :dbName " +
                " and referenceDatabase.foreignDBDataType.dataType = :type" +
                " and referenceDatabase.foreignDBDataType.superType = :superType" +
                " and referenceDatabase.organism  = :organism" +
                " ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("dbName", name.toString());
        query.setString("type", type.toString());
        query.setString("superType", ForeignDBDataType.SuperType.SEQUENCE.toString());
        query.setString("organism", Species.ZEBRAFISH.toString());

        return (List<ReferenceDatabase>) query.list();
    }

    ;

    public Accession getAccessionByAlternateKey(String number, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Accession.class);
        criteria.add(Restrictions.eq("number", number));
        if (referenceDatabases != null && referenceDatabases.length > 0 && referenceDatabases[0] != null) {
            criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        }
        return (Accession) criteria.uniqueResult();
    }

    public List<Accession> getAccessionsByNumber(String number) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Accession.class);
        criteria.add(Restrictions.eq("number", number));
        return criteria.list();
    }


    /**
     * Explicitly do not get transcripts.
     *
     * @param referenceDatabases Reference databases to view.
     * @return
     */
    public Map<String, MarkerDBLink> getUniqueMarkerDBLinks(ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        criteria.addOrder(Order.asc("accessionNumber"));
        List<MarkerDBLink> dbLinks = criteria.list();

        HashMap<String, MarkerDBLink> returnMap = new HashMap<String, MarkerDBLink>();

        // todo: this should be logged somewhere else possible and not be tied directly to microarray
        for (MarkerDBLink markerDBLink : dbLinks) {
            if (false == returnMap.containsKey(markerDBLink.getAccessionNumber())) {
                returnMap.put(markerDBLink.getAccessionNumber(), markerDBLink);
            } else {
                if (markerDBLink.getMarker().isInTypeGroup(Marker.TypeGroup.CDNA_AND_EST)) {
                    logger.warn("CDNA/EST accession references more than 1 link:" + markerDBLink.getAccessionNumber());
                } else { // if is in genedom or otherwise, we don't really care
                    logger.debug("Accession references >1 links: " + markerDBLink.getAccessionNumber());
                }
            }

        }

        return returnMap;
    }

    /**
     * Get unique acccessions for a given set of databases.
     *
     * @param referenceDatabases
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<String> getAccessions(ReferenceDatabase... referenceDatabases) {
        Set<String> results = new HashSet<String>();

        String hql = "" +
                " select dbl.accessionNumber from DBLink dbl where dbl.referenceDatabase in (:referenceDatabases) ";
        results.addAll(HibernateUtil.currentSession().createQuery(hql)
                .setParameterList("referenceDatabases", referenceDatabases)
                .list());

        return results;
    }

    public MultiValueMap getMarkerDBLinks(ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        criteria.addOrder(Order.asc("accessionNumber"));
        List<MarkerDBLink> dbLinks = criteria.list();

        MultiValueMap returnMap = new MultiValueMap();
        for (MarkerDBLink markerDBLink : dbLinks) {
            returnMap.put(markerDBLink.getAccessionNumber(), markerDBLink);
        }

        return returnMap;
    }

    public DBLink getDBLinkByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (DBLink) session.get(DBLink.class, zdbID);
    }

    public List<DBLink> getDBLinksForAccession(String accessionString) {
        return getDBLinksForAccession(accessionString, true);
    }

    public List<DBLink> getDBLinksForAccession(String accessionString, boolean include, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();

        String hql = "select dbl from DBLink dbl "
                + "where dbl.accessionNumber = :accessionNumber ";

        if (referenceDatabases.length > 0) {
            if (include == true) {
                hql += "and dbl.referenceDatabase in (:referenceDatabases) ";
            } else {
                hql += "and dbl.referenceDatabase not in (:referenceDatabases) ";
            }
        }
        hql += "order by dbl.referenceDatabase asc, dbl.accessionNumber asc ";

        Query query = session.createQuery(hql);

        query.setString("accessionNumber", accessionString);
//        query.setString("sequenceType",ForeignDBDataType.SuperType.SEQUENCE.toString()) ;
        if (referenceDatabases.length > 0) {
            query.setParameterList("referenceDatabases", referenceDatabases);
        }
        return query.list();
    }

    public List<MarkerDBLink> getMarkerDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber", accessionString));
        if (referenceDatabases != null && referenceDatabases.length > 0 && referenceDatabases[0] != null) {
            criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list();
    }


    @SuppressWarnings("unchecked")
    public List<String> getGenbankCdnaDBLinks() {
        return (List<String>) HibernateUtil.currentSession().createSQLQuery("" +
                "select  dbl.dblink_acc_num from db_link dbl , marker m, marker_type_group_member gm " +
                "where dbl.dblink_fdbcont_zdb_id in  " +
                "(  " +
                "   select  " +
                "   fdbc.fdbcont_zdb_id  " +
                "   from foreign_db_contains fdbc, foreign_db db, foreign_db_data_type dt  " +
                "   where " +
                "   db.fdb_db_name in( 'GenBank', 'RefSeq')  " +
                "   and " +
                "   dt.fdbdt_data_type = 'RNA'  " +
                "   and " +
                "   fdbc.fdbcont_fdb_db_id = db.fdb_db_pk_id  " +
                "   and " +
                "   fdbc.fdbcont_fdbdt_id = dt.fdbdt_pk_id  " +
                ")  " +
                "and m.mrkr_zdb_id=dbl.dblink_linked_recid " +
                "and gm.mtgrpmem_mrkr_type=m.mrkr_type " +
                "and gm.mtgrpmem_mrkr_type_group in ('GENEDOM','CDNA_AND_EST') " +
                " ").list();
    }

    /**
     * from getZfinGbAcc.pl, sql_xpat
     * <p/>
     * Select cDNA that is encoded by genes with expression (that are not microRNA).
     * <p/>
     * 1 - select genes with expression that are not microRNA (~10K)
     * 2 - select small segments encoded by those genes (??)
     * 3 - return RNA for a small set of databases (GenBank, Vega_Trans, PREVEGA, RefSeq) (~ 41K) (of 131K)
     *
     * @return List of DBLink accessions.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getGenbankXpatCdnaDBLinks() {
        // this currently takes 30 seconds, returns about 41K records
        Set<String> results = new HashSet<String>();
        results.addAll((List<String>) HibernateUtil.currentSession().createSQLQuery("" +
                "     select dbl.dblink_acc_num  from db_link dbl, foreign_db_contains, foreign_db, foreign_db_data_type  " +
                "      where dblink_fdbcont_zdb_id = fdbcont_zdb_id  " +
                "      and fdb_db_name in ('GenBank','Vega_Trans','PREVEGA','RefSeq')  " +
                "      and fdbdt_data_type = 'RNA' " +
                "      and fdbcont_fdbdt_id = fdbdt_pk_id " +
                "      and fdbcont_fdb_db_id = fdb_db_pk_id " +
                "      and  " +
                "      exists ( " +
                "      select mr.mrel_mrkr_1_zdb_id " +
                "      from marker_relationship mr, marker g,  expression_experiment ee " +
                "      where dbl.dblink_linked_recid = mr.mrel_mrkr_2_zdb_id " +
                "      and g.mrkr_zdb_id=mr.mrel_mrkr_1_zdb_id " +
                "      and mr.mrel_type='gene encodes small segment' " +
                "      and g.mrkr_name[1,8] <> 'microRNA' " +
                "      and ee.xpatex_gene_zdb_id =  g.mrkr_zdb_id " +
                "      and exists (select er.xpatres_zdb_id from expression_result er where er.xpatres_xpatex_zdb_id = ee.xpatex_zdb_id) " +
                "      union " +
                "      select g.mrkr_zdb_id " +
                "      from expression_experiment ee, marker g " +
                "      where dbl.dblink_linked_recid = ee.xpatex_gene_zdb_id " +
                "      and g.mrkr_name[1,8] <> 'microRNA' " +
                "      and ee.xpatex_gene_zdb_id =  g.mrkr_zdb_id " +
                "      and exists (select er.xpatres_zdb_id from expression_result er where er.xpatres_xpatex_zdb_id = ee.xpatex_zdb_id) " +
                "     ) " +
                "").list());
        return results;
    }

    @SuppressWarnings("unchecked")
    public List<String> getGenbankSequenceDBLinks() {
        return (List<String>) HibernateUtil.currentSession().createSQLQuery("" +
                " select dblink_acc_num " +
                "from db_link " +
                "where dblink_fdbcont_zdb_id in " +
                "(" +
                "   select " +
                "   fdbcont_zdb_id " +
                "   from foreign_db_contains, foreign_db, foreign_db_data_type " +
                "   where fdb_db_name = 'GenBank' " +
                "   and fdbdt_super_type = 'sequence' " +
                "   and fdbcont_fdbdt_id = fdbdt_pk_id " +
                "   and fdbcont_fdb_db_id = fdb_db_pk_id " +
                ")   " +
                "").list();
    }

    public List<DBLink> getDBLinks(String accessionString, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DBLink.class);
        criteria.add(Restrictions.eq("accessionNumber", accessionString));
        if (referenceDatabases != null && referenceDatabases.length > 0 && referenceDatabases[0] != null) {
            criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list();
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TranscriptDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber", accessionString));
        if (referenceDatabases != null && referenceDatabases.length > 0 && referenceDatabases[0] != null) {
            criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list();
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, Transcript transcript) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TranscriptDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber", accessionString));
        criteria.add(Restrictions.eq("transcript", transcript));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list();
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForTranscript(Transcript transcript,
                                                                    ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TranscriptDBLink.class);
        criteria.add(Restrictions.eq("transcript", transcript));
        criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list();
    }

    public DBLink getDBLinkByAlternateKey(String accessionString, String dataZdbID,
                                          ReferenceDatabase referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DBLink.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("referenceDatabase", referenceDatabases));
        criteria.add(Restrictions.eq("accessionNumber", accessionString));
        return (DBLink) criteria.uniqueResult();
    }

    public FeatureDBLink getFeatureDBLinkByAlternateKey(String accessionString, String dataZdbID,
                                                        ReferenceDatabase referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(FeatureDBLink.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("referenceDatabase", referenceDatabases));
        criteria.add(Restrictions.eq("accessionNumber", accessionString));
        return (FeatureDBLink) criteria.uniqueResult();
    }

    public List<MarkerDBLink> getDBLinksForMarkerExcludingReferenceDatabases(Marker marker,
                                                                             ForeignDBDataType.DataType refType,
                                                                             ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.eq("marker", marker));
        criteria.add(Restrictions.not(Restrictions.in("referenceDatabase", referenceDatabases)));
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("marker"));
        List<MarkerDBLink> markerDBLinkList = criteria.list();
        List<MarkerDBLink> returnList = new ArrayList<MarkerDBLink>();
        for (MarkerDBLink markerDBLink : markerDBLinkList) {
            if (markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType().equals(refType)) {
                returnList.add(markerDBLink);
            }
        }
        return returnList;
    }


    public List<MarkerDBLink> getDBLinksForMarker(Marker marker, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.eq("marker", marker));
        if (referenceDatabases.length > 0) {
            criteria.add(Restrictions.in("referenceDatabase", referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("marker"));
        return criteria.list();
    }

    /**
     * Saves a collection of MarkerDBLinks.
     * The try catch block is within the loop because I want to report on failures directly.
     * Notes from here for massive batch inserts: http://docs.jboss.org/hibernate/core/3.3/reference/en/html/batch.html
     *
     * @param dbLinksToAdd   List of DBLinks to add
     * @param attributionPub Publication to attribute
     */
    public void addDBLinks(Collection<MarkerDBLink> dbLinksToAdd, Publication attributionPub, int commitChunk) {
        if (CollectionUtils.isNotEmpty(dbLinksToAdd)) {
            Session session = HibernateUtil.currentSession();
            try {
                InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
                int size = dbLinksToAdd.size();
                int counter = 0;
                for (MarkerDBLink dbLink : dbLinksToAdd) {
                    session.save(dbLink);
                    logger.debug("adding dblink[" + dbLink.getZdbID() + "]:\n" + dbLink.getAccessionNumber() + " db: " + dbLink.getReferenceDatabase().getForeignDB().getDbName() + " markerID[" + dbLink.getMarker().getZdbID() + "]  [" + counter + "/" + (size - 1) + "]");
                    logger.debug("ADDED dblink:\n" + dbLink.getAccessionNumber() + " db: " +
                            dbLink.getReferenceDatabase().getForeignDB().getDbName() + "[" + counter + "/" + (size - 1) + "]");

                    if (counter % commitChunk == 0 && counter != 0) {
                        logger.debug("flushing links[" + commitChunk + "]" + " [" + counter + "/" + (size - 1) + "]");
                        session.flush();
                    }
                    ++counter;
                }


                if (attributionPub != null) {
                    for (MarkerDBLink dbLink : dbLinksToAdd) {
                        ir.insertRecordAttribution(dbLink.getZdbID(), attributionPub.getZdbID());
                    }
                }
                logger.debug("flushing links[" + commitChunk + "]" + " [" + counter + "/" + (size) + "]");
                session.flush();
            } catch (Exception e) {
                logger.error("failed to save MarkerDBLinks", e);
            }
        }
        dbLinksToAdd.clear();

    }

    /**
     * Removes dbLinks and associated RecordAttribution and ZdbID data.
     *
     * @param dbLinksToRemove DBLinks to remove
     */
    public int removeDBLinks(Set<DBLink> dbLinksToRemove) {

        logger.debug("dbLinksToRemove.size: " + dbLinksToRemove.size());

        if (dbLinksToRemove.size() == 0) {
            return 0;
        }

        Session session = HibernateUtil.currentSession();
        session.flush();  // without this, it fails
        logger.debug("flushed");


        List<String> dbLinkZdbIdsToDelete = new ArrayList<String>();
        for (DBLink dbLink : dbLinksToRemove) {
            dbLinkZdbIdsToDelete.add(dbLink.getZdbID());
        }

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        if (dbLinkZdbIdsToDelete.size() > 0) {
            ir.deleteActiveDataByZdbID(dbLinkZdbIdsToDelete);
            dbLinksToRemove.clear();
        }
        session.flush();  // test

        String hql = "" +
                "delete from MarkerDBLink dbl where dbl.id in (:dbLinks)";
        Query query = session.createQuery(hql);
        query.setParameterList("dbLinks", dbLinkZdbIdsToDelete);
        return query.executeUpdate();

    }

    public int removeAccessionByNumber(String accessionNumber) {
        String hql = "" +
                "delete from Accession a where a.number = :accessionNumber ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("accessionNumber", accessionNumber);
        return query.executeUpdate();
    }

    /**
     * Pulled from markerview.apg, line 2524:...
     * This is a union of 3 statements:
     * 1 - get marker DBLinks with a direct sequence reference database
     * 2 - get marker DBLinks with a relation to linked gene  todo: specify type
     * 3 - get marker DBLinks with a relation to linked clone todo: specify type
     *
     * @param marker                Marker to get sequences from
     * @param referenceDatabaseType Type of reference database to pull markers from
     * @return A list of all associated DBLinks.
     */
    public MarkerDBLinkList getAllSequencesForMarkerAndType(Marker marker, ForeignDBDataType.DataType referenceDatabaseType) {
        Session session = HibernateUtil.currentSession();

        MarkerDBLinkList dbLinks = new MarkerDBLinkList();
        String hql = "" +
                "from MarkerDBLink dbl " +
                " where dbl.marker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType = :superType " +
                " and dbl.referenceDatabase.foreignDBDataType.dataType = :type " +
                " order by dbl.referenceDatabase.foreignDB.dbName , dbl.accessionNumber ";

        Query query = session.createQuery(hql);
        query.setString("markerZdbID", marker.getZdbID());
        query.setString("superType", "sequence");
        query.setString("type", referenceDatabaseType.toString());
        dbLinks.addAll(query.list());


        String hql1 = "select dbl " +
                "from MarkerDBLink dbl, MarkerRelationship mr" +
                " where mr.secondMarker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType = :superType " +
                " and dbl.referenceDatabase.foreignDBDataType.dataType  = :type and mr.firstMarker.zdbID = dbl.marker.zdbID " +
                " and mr.firstMarker.markerType = :markerType ";
        // todo: and marker_relation type = .....
//        " and mr.type = :markerRelationshipType " ;
        query = session.createQuery(hql1);
        query.setString("markerZdbID", marker.getZdbID());
        query.setString("superType", "sequence");
        query.setString("type", referenceDatabaseType.name());
        query.setString("markerType", Marker.Type.GENE.name());
        // todo: and marker_relation type = .....
//        query.setString("markerRelationshipType", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT.name()) ;
        dbLinks.addAll(query.list());


        String hql2 = "select dbl " +
                "from MarkerDBLink dbl, MarkerRelationship mr" +
                " where mr.secondMarker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType = :superType " +
                " and dbl.referenceDatabase.foreignDBDataType.dataType = :type and mr.firstMarker.zdbID = dbl.marker.zdbID ";
        // todo: and first marker type to clone somehow
//        " and :markerTypeGrroup in (mr.firstMarker.markerType.typeGroupStrings)    " ;
//        " and mr.type = :markerRelationshipType " ;
        // todo: and marker_relation type = .....
        query = session.createQuery(hql2);
        query.setString("markerZdbID", marker.getZdbID());
        query.setParameter("superType", ForeignDBDataType.SuperType.SEQUENCE);
        query.setParameter("type", referenceDatabaseType);
//        query.setString("markerTypeGroup",Marker.TypeGroup.CLONE.name()) ;
        dbLinks.addAll(query.list());

        return dbLinks;
    }


//    public TreeSet<MarkerDBLink> getSequenceDBLinksForMarker(Marker marker) {
//        Session session = HibernateUtil.currentSession() ;
//
//        TreeSet<MarkerDBLink> dbLinks = new TreeSet<MarkerDBLink>() ;
//
//        String hql = "" +
//                "from MarkerDBLink dbl " +
//                " where dbl.marker.zdbID = :markerZdbID and dbl.referenceDatabase.superType = :superType ";
//
//        Query query = session.createQuery(hql) ;
//        query.setString("markerZdbID",marker.getZdbID()) ;
//        query.setString("superType","sequence") ;
//        for (Object o : query.list() ) {
//            MarkerDBLink dblink = (MarkerDBLink)o;
//            //todo: evenentually there should be a display group for marker linked sequences..?
//            if (!dblink.isInDisplayGroup(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE)
//                    && !dblink.isInDisplayGroup(DisplayGroup.GroupName.STEM_LOOP))
//                dbLinks.add(dblink);
//        }
//        return dbLinks ;
//    }
//
//
//    public TreeSet<TranscriptDBLink> getSequenceDBLinksForTranscript(Transcript transcript) {
//        Session session = HibernateUtil.currentSession() ;
//
//        TreeSet<TranscriptDBLink> dbLinks = new TreeSet<TranscriptDBLink>() ;
//
//        String hql = "" +
//                "from TranscriptDBLink dbl " +
//                " where dbl.transcript.zdbID = :transcriptZdbID and dbl.referenceDatabase.superType = :superType ";
//
//        Query query = session.createQuery(hql) ;
//        query.setString("transcriptZdbID",transcript.getZdbID()) ;
//        query.setString("superType","sequence") ;
//        for (Object o : query.list() ) {
//            TranscriptDBLink dblink = (TranscriptDBLink)o;
//            //todo: evenentually there should be a display group for marker linked sequences..?
//            if (!dblink.isInDisplayGroup(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE)
//                    && !dblink.isInDisplayGroup(DisplayGroup.GroupName.STEM_LOOP))
//                dbLinks.add(dblink);
//        }
//        return dbLinks ;
//    }


    public MarkerDBLinkList getNonSequenceMarkerDBLinksForMarker(Marker marker) {
        Session session = HibernateUtil.currentSession();
        String hql = "select dbl " +
                "from MarkerDBLink dbl " +
                " where dbl.marker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType <> :superType";
        Query query = session.createQuery(hql);
        query.setString("markerZdbID", marker.getZdbID());
        query.setString("superType", ForeignDBDataType.SuperType.SEQUENCE.toString());


        MarkerDBLinkList dbLinks = new MarkerDBLinkList();
        dbLinks.addAll(query.list());

        return dbLinks;
    }

    public List<DBLink> getSummaryMarkerDBLinksForMarker(Marker marker) {
        Session session = HibernateUtil.currentSession();
        String hql = "select dbl from MarkerDBLink dbl" +
                " where dbl.marker.zdbID = :markerZdbID " +
                "   and dbl.referenceDatabase.foreignDBDataType.superType = :superType" +
                "   and dbl.referenceDatabase.foreignDB.dbName <> :geo" +
                "   and dbl.referenceDatabase.foreignDB.dbName <> :zfespresso";
        Query query = session.createQuery(hql);
        query.setString("markerZdbID", marker.getZdbID());
        query.setString("superType", ForeignDBDataType.SuperType.SUMMARY_PAGE.toString());

        query.setString("geo", ForeignDB.AvailableName.GEO.toString());
        query.setString("zfespresso", ForeignDB.AvailableName.ZF_ESPRESSO.toString());

        //the app page code also excludes ArrayExpress, but that doesn't seem to be
        //in the database anymore, and it's commented out of the enum, so I'lll leave
        //it out.

        return query.list();
    }

    public DBLink getDBLink(String markerZdbID, String accession, String referenceDBName) {
        Session session = HibernateUtil.currentSession();
        String hql = "from DBLink mdbl where mdbl.accessionNumber = :accession " +
                " and mdbl.dataZdbID = :markerZdbID " +
                " and mdbl.referenceDatabase.foreignDB.dbName = :referenceDBName";
        Query query = session.createQuery(hql);
        query.setString("accession", accession);
        query.setString("markerZdbID", markerZdbID);
        query.setString("referenceDBName", referenceDBName);
        return (DBLink) query.uniqueResult();
    }

    @Override
    public DBLink getDBLink(String featureZDbID, String accession) {
        Session session = HibernateUtil.currentSession();
        String hql = "from DBLink mdbl where mdbl.accessionNumber = :accession " +
                " and mdbl.dataZdbID = :markerZdbID ";

        Query query = session.createQuery(hql);
        query.setString("accession", accession);
        query.setString("markerZdbID", featureZDbID);

        return (DBLink) query.uniqueResult();
    }

    public List<ReferenceDatabase> getReferenceDatabasesWithInternalBlast() {
        Session session = HibernateUtil.currentSession();
        String hql = "select rd from ReferenceDatabase rd join rd.primaryBlastDatabase blast " +
                " where blast.origination.type in (:externalTypes) " +
                " order by rd.foreignDB.dbName asc ";
        Query query = session.createQuery(hql);
        List<Origination.Type> types = new ArrayList<Origination.Type>();
        types.add(Origination.Type.CURATED);
        types.add(Origination.Type.LOADED);
        query.setParameterList("externalTypes", types);
        return (List<ReferenceDatabase>) query.list();
    }

    public Map<String, List<DBLink>> getDBLinksForAccessions(Collection<String> accessionNumbers) {
        Map<String, List<DBLink>> returnMap = new HashMap<String, List<DBLink>>();
        if (true == CollectionUtils.isEmpty(accessionNumbers)) {
            return returnMap;
        }

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DBLink.class);
        criteria.add(Restrictions.in("accessionNumber", accessionNumbers));
        List<DBLink> dbLinks = criteria.list();

        // cheaper to remap then to do further queries
        for (DBLink dbLink : dbLinks) {
            List<DBLink> dbLinksForAccession = returnMap.get(dbLink.getAccessionNumber());
            if (dbLinksForAccession == null) {
                List<DBLink> dbLinkList = new ArrayList<DBLink>();
                dbLinkList.add(dbLink);
                returnMap.put(dbLink.getAccessionNumber(), dbLinkList);
            } else {
                dbLinksForAccession.add(dbLink);
            }
        }

        return returnMap;
    }


    /**
     * Retrieves all marker ids with sequence information (accession numbers)
     *
     * @param firstNIds number of sequences to be returned
     * @return list of markers
     */
    public List<String> getAllNSequences(int firstNIds) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct dataZdbID from DBLink " +
                "where referenceDatabase.foreignDBDataType.superType = :superType " +
                " and dataZdbID not like :transcript " +
                " group by dataZdbID  " +
                " having count(accessionNumber) > 1   ";

        Query query = session.createQuery(hql);
        query.setParameter("superType", ForeignDBDataType.SuperType.SEQUENCE);
        query.setString("transcript", "ZDB-TSCRIPT%");
        if (firstNIds > 0)
            query.setMaxResults(firstNIds);

        return (List<String>) query.list();
    }


    /**
     * TODO:
     * Find dblink where referenceDatabase belongs to "marker linked sequence"
     * <p/>
     * and
     * <p/>
     * dblink is on the second related marker of type 'gene contains small segment', ' clone contains small segment',
     * , or 'gene encodes small segment' and the clone is not chimeric
     * <p/>
     * <p/>
     * dblink is on the first related marker of type 'clone contains gene' and the clone is not chimeric
     *
     * @param zdbID
     * @param superType
     * @return
     */
    @Override
    public List<DBLink> getDBLinksForMarker(String zdbID, ForeignDBDataType.SuperType superType) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct dbl from DBLink dbl  " +
                "where dbl.referenceDatabase.foreignDBDataType.superType = :superType " +
                " and dbl.dataZdbID = :markerZdbId " +
//                " order by dbl.referenceDatabase.foreignDB.significance, dbl.referenceDatabase.foreignDB.dbName , dbl.accessionNumberDisplay "+
                " ";

        Query query = session.createQuery(hql);
        query.setParameter("superType", superType);
        query.setString("markerZdbId", zdbID);
        return query.list();
    }

    @Override
    public int getNumberDBLinks(Marker marker) {
        String sql = " select count(*) from ( " +
                " select dblink_acc_num, fdb_db_name " +
                "  from db_link, foreign_db_contains, foreign_db, " +
                "               foreign_db_contains_display_group_member, foreign_db_contains_display_group " +
                "  where dblink_linked_recid = :markerZdbId " +
                "    and fdbcont_fdb_db_id = fdb_db_pk_id " +
                "    and dblink_fdbcont_zdb_id = fdbcont_zdb_id " +
                "    and fdbcdg_name = 'marker linked sequence' " +
                "    and fdbcdg_pk_id = fdbcdgm_group_id " +
                "    and fdbcdgm_fdbcont_zdb_id = fdbcont_zdb_id " +
//                "    -- and fdb_db_name != 'ZFIN_PROT' " +
                "  UNION " +
                "  select dblink_acc_num, fdb_db_name " +
                "  from db_link, foreign_db_contains, marker_relationship, foreign_db, " +
                "             foreign_db_contains_display_group_member, foreign_db_contains_display_group " +
                "  where mrel_mrkr_1_zdb_id = :markerZdbId " +
                "    and fdbcont_fdb_db_id = fdb_db_pk_id " +
                "    and dblink_linked_recid = mrel_mrkr_2_zdb_id " +
                "    and dblink_fdbcont_zdb_id = fdbcont_zdb_id " +
                "    and fdbcdg_name = 'marker linked sequence' " +
                "    and fdbcdg_pk_id = fdbcdgm_group_id " +
                "    and fdbcdgm_fdbcont_zdb_id = fdbcont_zdb_id " +
//                "    -- and fdb_db_name != 'ZFIN_PROT' " +
                "    and mrel_type in ('gene contains small segment', " +
                "		      'clone contains small segment', " +
                "		      'gene encodes small segment') " +
                "    and mrel_mrkr_2_zdb_id not in ('$chimeric_clone_list') " +
                "  UNION " +
                "  select dblink_acc_num, fdb_db_name " +
                "  from db_link, foreign_db_contains,foreign_db, marker_relationship, " +
                "             foreign_db_contains_display_group_member, foreign_db_contains_display_group " +
                "  where mrel_mrkr_2_zdb_id = :markerZdbId " +
                "    and dblink_linked_recid = mrel_mrkr_1_zdb_id " +
                "    and dblink_fdbcont_zdb_id = fdbcont_zdb_id " +
                "    and fdbcdg_name = 'marker linked sequence' " +
                "    and fdbcdg_pk_id = fdbcdgm_group_id " +
                "    and fdbcdgm_fdbcont_zdb_id = fdbcont_zdb_id " +
                "    and fdbcont_fdb_db_id = fdb_db_pk_id " +
                "    and mrel_type in ('clone contains gene') " +
                "    and mrel_mrkr_1_zdb_id not in ('$chimeric_clone_list') " +
                "    ) ";
        return Integer.parseInt(HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", marker.getZdbID())
                .uniqueResult().toString());
    }

    @Override
    public List<DBLink> getDBLinksForMarkerAndDisplayGroup(Marker marker, DisplayGroup.GroupName groupName) {
//        ResultTransformer transformer = new BasicTransformerAdapter() {
//            @Override
//            public Object transformTuple(Object[] tuple, String[] aliases) {
//                DBLink linkDisplay = new MarkerDBLink();
//                linkDisplay.setZdbID(tuple[0].toString());
//                HibernateUtil.currentSession().refresh(linkDisplay);
//                return linkDisplay;
//            }
//        };
//        String sql = "select distinct dbl.dblink_zdb_id from db_link dbl  " +
//                "join foreign_db_contains_display_group_member m on m.fdbcdgm_fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id " +
//                "join foreign_db_contains_display_group g on g.fdbcdg_pk_id=m.fdbcdgm_group_id " +
//                "join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
//                "join foreign_db fdb on fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id " +
//                "where g.fdbcdg_name= :displayGroup " +
//                "and " +
//                "dbl.dblink_linked_recid= :markerZdbId ";
//        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
//                .setParameter("markerZdbId", marker.getZdbID())
//                .setParameter("displayGroup", groupName.toString())
//                .setResultTransformer(transformer)
//                ;

        String hql = "select distinct dbl from DBLink dbl  " +
                "join dbl.referenceDatabase.displayGroups dg " +
                "where dg.groupName = :displayGroup " +
                "and " +
                "dbl.dataZdbID = :markerZdbId ";
        Query query = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setParameter("displayGroup", groupName.toString());
        return query.list();
    }

    @Override
    public List<TranscriptDBLink> getTranscriptDBLinksForMarkerAndDisplayGroup(Transcript transcript, DisplayGroup.GroupName groupName) {
        String hql = "select distinct dbl from TranscriptDBLink dbl  " +
                "join dbl.referenceDatabase.displayGroups dg " +
                "where dg.groupName = :displayGroup " +
                "and " +
                "dbl.dataZdbID = :markerZdbId ";
        Query query = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("markerZdbId", transcript.getZdbID())
                .setParameter("displayGroup", groupName.toString());
        return query.list();
    }

    @Override
    public List<MarkerDBLink> getDBLinksForFirstRelatedMarker(Marker marker, DisplayGroup.GroupName groupName, MarkerRelationship.Type... markerRelationshipTypes) {

        String hql = " select distinct dbl " +
                " from DBLink dbl, DisplayGroup dg, ReferenceDatabase ref,  " +
                " MarkerRelationship  mr  " +
                " where dg.groupName = :displayGroup " +
                " and dbl.referenceDatabase=ref " +
                " and dg in elements(ref.displayGroups) " +
                " and mr.secondMarker.zdbID=dbl.dataZdbID " +
                " and mr.markerRelationshipType.name in (:types) " +
                " and mr.firstMarker.zdbID = :markerZdbId " +
                " ";

        Set<String> types = new HashSet<String>();
        if (markerRelationshipTypes.length != 0) {
            for (MarkerRelationship.Type type : markerRelationshipTypes) {
                types.add(type.toString());
            }
        } else {
            for (MarkerRelationship.Type type : MarkerRelationship.Type.values()) {
                types.add(type.toString());
            }

        }
        Query query = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setParameter("displayGroup", groupName.toString())
                .setParameterList("types", types);
        return query.list();
    }

    @Override
    public List<MarkerDBLink> getDBLinksForSecondRelatedMarker(Marker marker, DisplayGroup.GroupName groupName, MarkerRelationship.Type... markerRelationshipTypes) {
        String hql = " select distinct dbl " +
                " from DBLink dbl, DisplayGroup dg, ReferenceDatabase ref,  " +
                " MarkerRelationship  mr  " +
                " where dg.groupName = :displayGroup " +
                " and dbl.referenceDatabase=ref " +
                " and dg in elements(ref.displayGroups) " +
                " and mr.firstMarker.zdbID=dbl.dataZdbID " +
                " and mr.markerRelationshipType.name in (:types) " +
                " and mr.secondMarker.zdbID = :markerZdbId " +
                " ";

        Set<String> types = new HashSet<String>();
        if (markerRelationshipTypes.length != 0) {
            for (MarkerRelationship.Type type : markerRelationshipTypes) {
                types.add(type.toString());
            }
        } else {
            for (MarkerRelationship.Type type : MarkerRelationship.Type.values()) {
                types.add(type.toString());
            }

        }
        Query query = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("markerZdbId", marker.getZdbID())
                .setParameter("displayGroup", groupName.toString())
                .setParameterList("types", types);
        return query.list();
    }

    @Override
    public Collection<String> getDBLinkAccessionsForMarker(Marker marker, ForeignDBDataType.DataType dataType) {
        String hql = "  select dbl.accessionNumber from DBLink dbl " +
                "  where dbl.dataZdbID = :markerZdbID   " +
                "  and dbl.referenceDatabase.foreignDBDataType.dataType = :dataType " +
                " ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("markerZdbID", marker.getZdbID())
                .setString("dataType", dataType.toString())
                .list();
    }

    @Override
    public Collection<String> getDBLinkAccessionsForEncodedMarkers(Marker marker, ForeignDBDataType.DataType dataType){
        String hql = "  select dbl.accessionNumber from MarkerRelationship mr join mr.firstMarker m , MarkerDBLink dbl " +
                "  where mr.firstMarker.zdbID = :markerZdbID   " +
                "  and mr.secondMarker.zdbID = dbl.dataZdbID  " +
                "  and mr.type = :markerType  " +
                "  and dbl.referenceDatabase.foreignDBDataType.dataType = :dataType " +
                " ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("markerZdbID", marker.getZdbID())
                .setString("dataType", dataType.toString())
                .setString("markerType", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT.toString() )
                .list();
    }

    /**
     * select dbl.dblink_acc_num,m.mrkr_zdb_id
from db_link dbl
join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id
join foreign_db fdb on fdb.fdb_db_pk_id=fdbc.fdbcont_fdb_db_id
join foreign_db_data_type dt on fdbc.fdbcont_fdbdt_id=dt.fdbdt_pk_id
join marker m on dbl.dblink_linked_recid=m.mrkr_zdb_id
where
dt.fdbdt_data_type='RNA'
and
dt.fdbdt_super_type='sequence'
--and  fdb.fdb_db_name='GenBank'
and
m.mrkr_type in ('GENE','GENEP','EST','CDNA')
     * @return Map&lt;accession,ZdbID&gt;
     */
    @Override
    public Map<String, String> getGeoAccessionCandidates() {
        String hql = " " +
                "  select dbl.accessionNumber,dbl.dataZdbID from MarkerDBLink dbl " +
                "  where dbl.referenceDatabase.foreignDBDataType.dataType = :dataType " +
                "  and dbl.referenceDatabase.foreignDBDataType.superType = :superType " +
                "  and dbl.marker.markerType.name in (:types) " +
                "" ;
        List<String> types = new ArrayList<String>();
        types.add(Marker.Type.CDNA.name());
        types.add(Marker.Type.EST.name());
        types.add(Marker.Type.GENE.name());
        types.add(Marker.Type.GENEP.name());
        List<DBLink> dblinks = HibernateUtil.currentSession().createQuery(hql)
                .setParameterList("types", types)
                .setParameter("dataType", ForeignDBDataType.DataType.RNA )
                .setParameter("superType", ForeignDBDataType.SuperType.SEQUENCE)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                       MarkerDBLink dbLink = new MarkerDBLink();
                        dbLink.setAccessionNumber(tuple[0].toString());
                        dbLink.setDataZdbID(tuple[1].toString());
                        return dbLink;
                    }
                })
                .list();
        Map<String,String>  accessionCandidates = new HashMap<String,String>();
        for(DBLink dbLink : dblinks){
            accessionCandidates.put(dbLink.getAccessionNumber(),dbLink.getDataZdbID());
        }

        return accessionCandidates;
    }

    @Override
    public List<MarkerDBLink> getWeakReferenceDBLinks(Marker gene,MarkerRelationship.Type type1,MarkerRelationship.Type type2) {
        String hql = " select distinct dbl " +
                " from DBLink dbl, DisplayGroup dg, ReferenceDatabase ref,  MarkerRelationship  ctmr, MarkerRelationship gtmr   " +
                " where ctmr.firstMarker.zdbID=dbl.dataZdbID " +
                " and dg.groupName = :displayGroup " +
                " and gtmr.firstMarker.zdbID= :markerZdbId " +
                " and dbl.referenceDatabase=ref " +
                " and dg in elements(ref.displayGroups) " +
                " and gtmr.secondMarker.zdbID = ctmr.secondMarker.zdbID " +
                " and gtmr.type = :type1 " +
                " and ctmr.type = :type2 " +
                " ";

        //     " and gtmr.type = 'gene produces transcript' " +
//                " and ctmr.type = 'clone contains transcript' " +

        Query query = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("markerZdbId", gene.getZdbID())
                .setParameter("type1", type1.toString())
                .setParameter("type2", type2.toString())
                .setParameter("displayGroup", DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE.toString()) ;
        return query.list();
    }
}


