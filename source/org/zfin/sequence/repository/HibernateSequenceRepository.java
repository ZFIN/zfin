/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository ;

import org.zfin.sequence.*;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.Origination;
import org.zfin.framework.HibernateUtil;
import org.zfin.orthology.Species;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.properties.ZfinProperties;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.*;

public class HibernateSequenceRepository implements SequenceRepository {

    private final Logger logger = Logger.getLogger(HibernateSequenceRepository.class) ;

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
                " " ;
        Query query = HibernateUtil.currentSession().createQuery(hql) ;
        query.setString("dbName",foreignDBName.toString()) ;
        query.setString("type",type.toString()) ;
        query.setString("superType", superType.toString() ) ;
        query.setString("organism",organism.toString()) ;

        return (ReferenceDatabase) query.uniqueResult();
    }

    public ReferenceDatabase getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName foreignDBName,
                                                                   ForeignDBDataType.DataType type) {
        return getReferenceDatabase(foreignDBName,type, ForeignDBDataType.SuperType.SEQUENCE,Species.ZEBRAFISH) ;
    }

    public Accession getAccessionByAlternateKey(String number, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Accession.class);
        criteria.add(Restrictions.eq("number",number));
        if(referenceDatabases!=null && referenceDatabases.length>0 && referenceDatabases[0]!=null){
            criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        }
        return (Accession) criteria.uniqueResult();
    }

    public List<Accession> getAccessionsByNumber(String number) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Accession.class);
        criteria.add(Restrictions.eq("number",number));
        return  criteria.list();
    }


    /**
     * Explicitly do not get transcripts.
     * @param referenceDatabases
     * @return
     */
    public Map<String, MarkerDBLink> getUniqueMarkerDBLinks(ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        criteria.addOrder(Order.asc("accessionNumber"));
        List<MarkerDBLink> dbLinks = criteria.list() ;

        HashMap<String,MarkerDBLink> returnMap = new HashMap<String,MarkerDBLink>() ;

        // todo: this should be logged somewhere else possible and not be tied directly to microarray
        for(MarkerDBLink markerDBLink : dbLinks ) {
            if(false==returnMap.containsKey( markerDBLink.getAccessionNumber())){
                returnMap.put(markerDBLink.getAccessionNumber(),markerDBLink) ;
            }
            else
            {
                if(markerDBLink.getMarker().isInTypeGroup(Marker.TypeGroup.CDNA_AND_EST)){
                    logger.warn("CDNA/EST accession references more than 1 link:" + markerDBLink.getAccessionNumber());
                }
                else{ // if is in genedom or otherwise, we don't really care
                    logger.debug("Accession references >1 links: "+ markerDBLink.getAccessionNumber());
                }
            }

        }

        return returnMap ;
    }

    public MultiValueMap getMarkerDBLinks(ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        criteria.addOrder(Order.asc("accessionNumber"));
        List<MarkerDBLink> dbLinks = criteria.list() ;

        MultiValueMap returnMap = new MultiValueMap() ;
        for(MarkerDBLink markerDBLink : dbLinks ) {
            returnMap.put(markerDBLink.getAccessionNumber(),markerDBLink) ;
        }

        return returnMap ;
    }

    public DBLink getDBLinkByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (DBLink) session.get(DBLink.class, zdbID);
    }

    public List<DBLink> getDBLinksForAccession(String accessionString) {
        return getDBLinksForAccession(accessionString,true) ;
    }

    public List<DBLink> getDBLinksForAccession(String accessionString,boolean include, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();

        String hql = "select dbl from DBLink dbl "
                +  "where dbl.accessionNumber = :accessionNumber "
                ;

        if(referenceDatabases.length>0){
            if(include==true){
                hql += "and dbl.referenceDatabase in (:referenceDatabases) ";
            }
            else{
                hql += "and dbl.referenceDatabase not in (:referenceDatabases) ";
            }
        }
        hql += "order by dbl.referenceDatabase asc, dbl.accessionNumber asc " ;

        Query query = session.createQuery(hql) ;

        query.setString("accessionNumber",accessionString) ;
//        query.setString("sequenceType",ForeignDBDataType.SuperType.SEQUENCE.toString()) ;
        if(referenceDatabases.length>0){
            query.setParameterList("referenceDatabases",referenceDatabases) ;
        }
        return query.list() ;
    }

    public List<MarkerDBLink> getMarkerDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber",accessionString));
        if(referenceDatabases!=null && referenceDatabases.length>0 && referenceDatabases[0]!=null){
            criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list() ;
    }

    public List<DBLink> getDBLinks(String accessionString, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DBLink.class);
        criteria.add(Restrictions.eq("accessionNumber",accessionString));
        if(referenceDatabases!=null && referenceDatabases.length>0 && referenceDatabases[0]!=null){
            criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list() ;
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TranscriptDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber",accessionString));
        if(referenceDatabases!=null && referenceDatabases.length>0 && referenceDatabases[0]!=null){
            criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list() ;
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, Transcript transcript) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TranscriptDBLink.class);
        criteria.add(Restrictions.eq("accessionNumber",accessionString));
        criteria.add(Restrictions.eq("transcript",transcript));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list() ;
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForTranscript(Transcript transcript,
                                                                    ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TranscriptDBLink.class);
        criteria.add(Restrictions.eq("transcript",transcript));
        criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        criteria.addOrder(Order.asc("accessionNumber"));
        return criteria.list() ;
    }

    public DBLink getDBLinkByAlternateKey(String accessionString, String dataZdbID,
                                          ReferenceDatabase referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DBLink.class);
        criteria.add(Restrictions.eq("dataZdbID",dataZdbID));
        criteria.add(Restrictions.eq("referenceDatabase",referenceDatabases));
        criteria.add(Restrictions.eq("accessionNumber",accessionString));
        return (DBLink) criteria.uniqueResult() ;
    }

    public List<MarkerDBLink> getDBLinksForMarkerExcludingReferenceDatabases(Marker marker,
                                                                             ForeignDBDataType.DataType refType,
                                                                             ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.eq("marker",marker));
        criteria.add(Restrictions.not(Restrictions.in("referenceDatabase",referenceDatabases)));
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("marker"));
        List<MarkerDBLink> markerDBLinkList = criteria.list() ;
        List<MarkerDBLink> returnList = new ArrayList<MarkerDBLink>() ;
        for(MarkerDBLink markerDBLink: markerDBLinkList){
            if(markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType().equals(refType)){
                returnList.add(markerDBLink) ;
            }
        }
        return returnList ;
    }


    public List<MarkerDBLink> getDBLinksForMarker(Marker marker, ReferenceDatabase... referenceDatabases) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.eq("marker",marker));
        if(referenceDatabases.length>0){
            criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        }
        criteria.addOrder(Order.asc("referenceDatabase"));
        criteria.addOrder(Order.asc("marker"));
        return criteria.list() ;
    }

    /**
     * Saves a collection of MarkerDBLinks.
     * The try catch block is within the loop because I want to report on failures directly.  
     * @param dbLinksToAdd  List of DBLinks to add
     * @param attributionPub Publication to attribute
     */
    public void addDBLinks(Collection<MarkerDBLink> dbLinksToAdd, Publication attributionPub, int commitChunk){
        Session session = HibernateUtil.currentSession();
        if(dbLinksToAdd!=null){
            InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository() ;
            int size = dbLinksToAdd.size() ;
//            ZdbIdGenerator generator = new ZdbIdGenerator() ;
            try{

                long startTime ;
                long currentTime  ;

//                Set<String> zdbIDs = generator.generateZdbIDs( (SessionImplementor) session,dbLinksToAdd.size(),"DBLINK",true,false);
//                Set<String> zdbIDs = new HashSet<String>() ;

//                Iterator<String> zdbIDIter = zdbIDs.iterator() ;
//                String currentZdbID ;
//                Iterator<String> zdbIDIter = zdbIDs.iterator() ;
                long totalStartTime = System.currentTimeMillis() ;
                long totalFinishTime ;


                int counter = 0 ;
                for(MarkerDBLink dbLink: dbLinksToAdd){
//                    currentZdbID = zdbIDIter.next() ; 
//                    dbLink.setZdbID(currentZdbID) ; 
                    session.save(dbLink) ;
                    logger.debug("adding dblink["+dbLink.getZdbID()+"]:\n"+dbLink.getAccessionNumber() + " db: "+ dbLink.getReferenceDatabase().getForeignDB().getDbName() + " markerID[" +dbLink.getMarker().getZdbID() +"]  ["+counter +"/"+ (size-1)+"]") ;
                    logger.debug("ADDED dblink:\n"+dbLink.getAccessionNumber() + " db: "+
                            dbLink.getReferenceDatabase().getForeignDB().getDbName() + "["+counter +"/"+ (size-1)+"]") ;

                    if(counter%commitChunk==0 && counter !=0){
                        logger.debug("flushing links["+commitChunk+"]"+" ["+counter +"/"+ (size-1)+"]") ;
                        startTime = System.currentTimeMillis() ;
                        session.flush();
                        currentTime = System.currentTimeMillis() ;
                        logger.debug("flushing link time["+((currentTime-startTime)/(1000.0f))+"]"+" ["+counter +"/"+ (size-1)+"]") ;
                    }
                    ++counter ;
                }

                session.flush();

                if(attributionPub!=null){
                    for(MarkerDBLink dbLink: dbLinksToAdd){
                        ir.insertRecordAttribution(dbLink.getZdbID(),attributionPub.getZdbID());
                    }
                }
                logger.debug("flushing links["+commitChunk+"]"+" ["+counter +"/"+ (size)+"]") ;
                startTime = System.currentTimeMillis() ;
                session.flush();
                currentTime = System.currentTimeMillis() ;
                logger.debug("flushing link time["+((currentTime-startTime)/(1000.0f))+"]"+" ["+counter +"/"+ (size)+"]") ;


                totalFinishTime = System.currentTimeMillis() ;
                logger.debug("total process time:" + ((totalFinishTime-totalStartTime)/1000.0f) ) ;


            } catch(Exception e){
                logger.error("failed to save MarkerDBLinks") ;
//                logger.error("failed to save MarkerDBLink ["+dbLink+"]",e) ;
//                    return ;
            }

        }
        dbLinksToAdd.clear();

    }

    /**
     * Removes dbLinks and associated RecordAttribution and ZdbID data.
     * @param dbLinksToRemove DBLinks to remove
     */
    public int removeDBLinks(Set<DBLink> dbLinksToRemove) {

        logger.debug("dbLinksToRemove.size: " + dbLinksToRemove.size()) ;

        if(dbLinksToRemove.size()==0){
            return 0 ; 
        }

        Session session = HibernateUtil.currentSession() ;
        session.flush();  // without this, it fails
        logger.debug("flushed" ) ;



        List<String> dbLinkZdbIdsToDelete = new ArrayList<String>() ;
        for(DBLink dbLink: dbLinksToRemove){
            dbLinkZdbIdsToDelete.add(dbLink.getZdbID()) ;
        }

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository() ;
        if(dbLinkZdbIdsToDelete.size() > 0){
            ir.deleteActiveDataByZdbID(dbLinkZdbIdsToDelete);
            dbLinksToRemove.clear();
        }
        session.flush();  // test

        String hql = "" +
                "delete from MarkerDBLink dbl where dbl.id in (:dbLinks)"  ;
        Query query = session.createQuery(hql) ;
        query.setParameterList("dbLinks",dbLinkZdbIdsToDelete) ;
        return query.executeUpdate() ;

    }

    public int removeAccessionByNumber(String accessionNumber) {
        String hql = "" +
                "delete from Accession a where a.number = :accessionNumber "  ;
        Query query = HibernateUtil.currentSession().createQuery(hql) ;
        query.setString("accessionNumber",accessionNumber) ;
        return query.executeUpdate() ;
    }

    /**
     * Pulled from markerview.apg, line 2524:...
     * This is a union of 3 statements:
     * 1 - get marker DBLinks with a direct sequence reference database
     * 2 - get marker DBLinks with a relation to linked gene  todo: specify type
     * 3 - get marker DBLinks with a relation to linked clone todo: specify type
     * @param marker Marker to get sequences from
     * @param referenceDatabaseType Type of reference database to pull markers from
     * @return A list of all associated DBLinks.
     */
    public MarkerDBLinkList getAllSequencesForMarkerAndType(Marker marker,ForeignDBDataType.DataType referenceDatabaseType) {
        Session session = HibernateUtil.currentSession() ;

        MarkerDBLinkList dbLinks = new MarkerDBLinkList() ;
        String hql = "" +
                "from MarkerDBLink dbl " +
                " where dbl.marker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType = :superType "+
                " and dbl.referenceDatabase.foreignDBDataType.dataType = :type " +
                " order by dbl.referenceDatabase.foreignDB.dbName , dbl.accessionNumber ";

        Query query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        query.setString("superType","sequence") ;
        query.setString("type",referenceDatabaseType.toString()) ;
        dbLinks.addAll(query.list()) ;


        String hql1 = "select dbl " +
                "from MarkerDBLink dbl, MarkerRelationship mr" +
                " where mr.secondMarker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType = :superType "+
                " and dbl.referenceDatabase.foreignDBDataType.dataType  = :type and mr.firstMarker.zdbID = dbl.marker.zdbID " +
                " and mr.firstMarker.markerType = :markerType " ;
        // todo: and marker_relation type = .....
//        " and mr.type = :markerRelationshipType " ;
        query = session.createQuery(hql1) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        query.setString("superType","sequence") ;
        query.setString("type",referenceDatabaseType.name()) ;
        query.setString("markerType",Marker.Type.GENE.name()) ;
        // todo: and marker_relation type = .....
//        query.setString("markerRelationshipType", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT.name()) ;
        dbLinks.addAll(query.list()) ;


        String hql2 = "select dbl " +
                "from MarkerDBLink dbl, MarkerRelationship mr" +
                " where mr.secondMarker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType = :superType "+
                " and dbl.referenceDatabase.foreignDBDataType.dataType = :type and mr.firstMarker.zdbID = dbl.marker.zdbID " ;
        // todo: and first marker type to clone somehow
//        " and :markerTypeGrroup in (mr.firstMarker.markerType.typeGroupStrings)    " ;
//        " and mr.type = :markerRelationshipType " ;
        // todo: and marker_relation type = .....
        query = session.createQuery(hql2) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        query.setParameter("superType",ForeignDBDataType.SuperType.SEQUENCE) ;
        query.setParameter("type",referenceDatabaseType) ;
//        query.setString("markerTypeGroup",Marker.TypeGroup.CLONE.name()) ;
        dbLinks.addAll(query.list()) ;

        return dbLinks ;
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
        Session session = HibernateUtil.currentSession() ;
        String hql = "select dbl " +
                "from MarkerDBLink dbl " +
                " where dbl.marker.zdbID = :markerZdbID and dbl.referenceDatabase.foreignDBDataType.superType <> :superType";
        Query query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        query.setString("superType",ForeignDBDataType.SuperType.SEQUENCE.toString()) ;


        MarkerDBLinkList dbLinks = new MarkerDBLinkList() ;
        dbLinks.addAll(query.list()) ;

        return dbLinks ;
    }

    public MarkerDBLinkList getSummaryMarkerDBLinksForMarker(Marker marker) {
        Session session = HibernateUtil.currentSession();
        String hql = "select dbl from MarkerDBLink dbl" +
                " where dbl.marker.zdbID = :markerZdbID " +
                "   and dbl.referenceDatabase.foreignDBDataType.superType = :superType" +
                "   and dbl.referenceDatabase.foreignDB.dbName <> :geo" +
                "   and dbl.referenceDatabase.foreignDB.dbName <> :zfespresso";
        Query query = session.createQuery(hql);
        query.setString("markerZdbID",marker.getZdbID());
        query.setString("superType",ForeignDBDataType.SuperType.SUMMARY_PAGE.toString());

        query.setString("geo",ForeignDB.AvailableName.GEO.toString());
        query.setString("zfespresso",ForeignDB.AvailableName.ZF_ESPRESSO.toString());

        //the app page code also excludes ArrayExpress, but that doesn't seem to be
        //in the database anymore, and it's commented out of the enum, so I'lll leave
        //it out.

        MarkerDBLinkList dblinks = new MarkerDBLinkList() ;
        dblinks.addAll(query.list());
        return dblinks;
    }

    public DBLink getDBLink(String markerZdbID, String accession, String referenceDBName) {
        Session session = HibernateUtil.currentSession() ;
        String hql = "from DBLink mdbl where mdbl.accessionNumber = :accession " +
                " and mdbl.dataZdbID = :markerZdbID " +
                " and mdbl.referenceDatabase.foreignDB.dbName = :referenceDBName" ;
        Query query =  session.createQuery(hql) ;
        query.setString("accession",accession) ;
        query.setString("markerZdbID",markerZdbID) ;
        query.setString("referenceDBName",referenceDBName) ;
        return (DBLink) query.uniqueResult() ;
    }

    public List<ReferenceDatabase> getReferenceDatabasesWithInternalBlast() {
        Session session = HibernateUtil.currentSession() ;
        String hql = "select rd from ReferenceDatabase rd join rd.primaryBlastDatabase blast " +
                " where blast.origination.type in (:externalTypes) " +
                " order by rd.foreignDB.dbName asc " ;
        Query query = session.createQuery(hql) ;
        List<Origination.Type> types = new ArrayList<Origination.Type>() ;
        types.add(Origination.Type.CURATED) ;
        types.add(Origination.Type.LOADED) ;
        query.setParameterList("externalTypes", types) ;
        return (List<ReferenceDatabase>) query.list() ;
    }

    public Map<String, List<DBLink>> getDBLinksForAccessions(Collection<String> accessionNumbers) {
        Map<String, List<DBLink>> returnMap = new HashMap<String, List<DBLink>>() ;
        if(true==CollectionUtils.isEmpty(accessionNumbers)){
            return returnMap ;
        }

        Session session = HibernateUtil.currentSession() ;
        Criteria criteria = session.createCriteria(DBLink.class) ;
        criteria.add(Restrictions.in("accessionNumber", accessionNumbers));
        List<DBLink> dbLinks = criteria.list() ;

        // cheaper to remap then to do further queries
        for(DBLink dbLink : dbLinks){
            List<DBLink> dbLinksForAccession = returnMap.get(dbLink.getAccessionNumber()) ;
            if(dbLinksForAccession==null){
                List<DBLink> dbLinkList = new ArrayList<DBLink>() ;
                dbLinkList.add(dbLink) ;
                returnMap.put(dbLink.getAccessionNumber(), dbLinkList ) ;
            }
            else{
                dbLinksForAccession.add(dbLink) ;
            }
        }

        return returnMap ;
    }


}


