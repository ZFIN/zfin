/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository ;

import org.zfin.sequence.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.orthology.Species;
import org.zfin.publication.Publication;
import org.zfin.database.ZdbIdGenerator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinProperties;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.apache.log4j.Logger;

import java.util.*;

public class HibernateSequenceRepository implements SequenceRepository {

    Logger logger = Logger.getLogger(HibernateSequenceRepository.class) ;

    public ReferenceDatabase getReferenceDatabaseByAlternateKey(ForeignDB foreignDB,
                                                             ReferenceDatabase.Type type,
                                                             ReferenceDatabase.SuperType superType,
                                                             Species organism) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ReferenceDatabase.class);
        criteria.add(Restrictions.eq("foreignDB", foreignDB));
        criteria.add(Restrictions.eq("type",type.toString()));
        criteria.add(Restrictions.eq("superType",superType.toString()));
        criteria.add(Restrictions.eq("organism",organism.toString()));
        return (ReferenceDatabase) criteria.uniqueResult();

    }

    public ForeignDB getForeignDBByName(String dbName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ForeignDB.class);
        criteria.add(Restrictions.eq("dbName", dbName));
        return (ForeignDB) criteria.uniqueResult();

    }

    public ReferenceDatabase getReferenceDatabase(String foreignDBName, ReferenceDatabase.Type type, ReferenceDatabase.SuperType superType, Species organism) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ReferenceDatabase.class);
        criteria.add(Restrictions.eq("foreignDB.dbName", foreignDBName));
        criteria.add(Restrictions.eq("type",type.toString()));
        criteria.add(Restrictions.eq("superType",superType.toString()));
        criteria.add(Restrictions.eq("organism",organism.toString()));
        return (ReferenceDatabase) criteria.uniqueResult();
    }

    public Accession getAccessionByAlternateKey(String number, ReferenceDatabase referenceDatabase) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Accession.class);
        criteria.add(Restrictions.eq("number",number));
        criteria.add(Restrictions.eq("referenceDatabase",referenceDatabase));
        return (Accession) criteria.uniqueResult();
    }


    public Map<String, MarkerDBLink> getUniqueMarkerDBLinks(ReferenceDatabase... referenceDatabases) throws Exception {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(MarkerDBLink.class);
        criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
        criteria.addOrder(Order.asc("accessionNumber"));
        List<MarkerDBLink> dbLinks = criteria.list() ;

        HashMap<String,MarkerDBLink> returnMap = new HashMap<String,MarkerDBLink>() ;

        // todo: this should be logged somewhere else possible and not be tied directly to microarray
        Logger microArrayErrorLog = Logger.getLogger(ZfinProperties.MICROARRAY_ERROR) ;
        for(MarkerDBLink markerDBLink : dbLinks ) {
            if(false==returnMap.containsKey( markerDBLink.getAccessionNumber())){
                returnMap.put(markerDBLink.getAccessionNumber(),markerDBLink) ;
            }
            else
            {
                if(markerDBLink.getMarker().isInTypeGroup(Marker.TypeGroup.CDNA_AND_EST)){
                    microArrayErrorLog.warn("CDNA/EST accession references more than 1 link:" + markerDBLink.getAccessionNumber());
                }
                else{ // if is in genedom or otherwise, we don't really care
                    logger.debug("Accession references >1 links: "+ markerDBLink.getAccessionNumber());
                }
            }

        }

        return returnMap ;
    }

     public Map<String, Set<MarkerDBLink>> getMarkerDBLinks(ReferenceDatabase... referenceDatabases) throws Exception {
         Session session = HibernateUtil.currentSession();

         Criteria criteria = session.createCriteria(MarkerDBLink.class);
         criteria.add(Restrictions.in("referenceDatabase",referenceDatabases));
         criteria.addOrder(Order.asc("accessionNumber"));
         List<MarkerDBLink> dbLinks = criteria.list() ;

         HashMap<String,Set<MarkerDBLink>> returnMap = new HashMap<String,Set<MarkerDBLink>>() ;
         for(MarkerDBLink markerDBLink : dbLinks ) {
             Set<MarkerDBLink> dbLinkSet = returnMap.get(markerDBLink.getAccessionNumber()) ;
             if(dbLinkSet == null){
                 Set<MarkerDBLink> newSet = new HashSet<MarkerDBLink>() ;
                 newSet.add(markerDBLink) ;
                 returnMap.put(markerDBLink.getAccessionNumber(),newSet) ;
             }
             else
             {
                 dbLinkSet.add(markerDBLink) ;
             }

         }

         return returnMap ;
     }


    /**
     * Saves a collection of MarkerDBLinks.
     * The try catch block is within the loop because I want to report on failures directly.  
     * @param dbLinksToAdd
     * @param attributionPub
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
     * @param dbLinksToRemove
     */
     public void removeDBLinks(Set<MarkerDBLink> dbLinksToRemove) {

        Session session = HibernateUtil.currentSession() ;
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository() ;
        List<String> dbLinkZdbIdsToDelete = new ArrayList<String>() ;
        logger.info("dbLinksToRemove.size: " + dbLinksToRemove.size()) ; 
        session.flush();  // without this, it fails


        
        for(MarkerDBLink markerDBLink: dbLinksToRemove){
            dbLinkZdbIdsToDelete.add(markerDBLink.getZdbID()) ;
            session.delete(markerDBLink) ;
        }

        if(dbLinkZdbIdsToDelete.size() > 0){
            ir.deleteRecordAttributionByDataZdbID(dbLinkZdbIdsToDelete);
//            session.flush();  // without this, it fails
//
//            logger.info("accessionNumbers to find in genBank: " + accessionNumbers.size()) ; 
            ir.deleteActiveDataByZdbID(dbLinkZdbIdsToDelete);
            dbLinksToRemove.clear();
        }



    }



}


