package org.zfin.datatransfer ;


import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;

import java.util.*;

import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.orthology.Species;
import org.zfin.publication.Publication;
import org.zfin.marker.Marker;
import org.hibernate.Session;

/** Class UpdateMicroArrayMain processes platform documentation for microarrays which we will provide links to
 *  within markerview.  See case
 *  <a href="http://zfinwinserver1.uoregon.edu/fogbugz/default.asp?pgx=EV&ixBug=2009&=#edit_1_2009">2009</a> in
 *  FogBugz.
 *
 */
public final class UpdateMicroArrayMain {

    final Logger logger = Logger.getLogger( UpdateMicroArrayMain.class ) ;
    final Logger notFoundLogger  = Logger.getLogger(ZfinProperties.MICROARRAY_NOT_FOUND);
    final Logger errorLogger = Logger.getLogger(ZfinProperties.MICROARRAY_ERROR);
    final Logger infoLogger  = Logger.getLogger(ZfinProperties.MICROARRAY_INFO);
    ReferenceDatabase geoDatabase = null ;
    ReferenceDatabase zfEspressoDatabase = null ;
//    ReferenceDatabase arrayExpressDatabase = null ;

    Map<String,MarkerDBLink> genBankLinks = null ; 
    Map<String,MarkerDBLink> microarrayLinks = null ; 


    ReferenceDatabase genBankGenomicDatabase  = null ;
    ReferenceDatabase genBankRNADatabase  = null ;
    SequenceRepository sequenceRepository = null ;


    public static String[] confFiles() {
        return new String[]{
                "filters.hbm.xml",
                "antibody.hbm.xml",
                "reno.hbm.xml",
                "anatomy.hbm.xml",
                "people.hbm.xml",
                "general.hbm.xml",
                "blast.hbm.xml",
                "marker.hbm.xml",
                "expression.hbm.xml",
                "sequence.hbm.xml",
                "publication.hbm.xml",
                "orthology.hbm.xml",
                "mutant.hbm.xml",
                "infrastructure.hbm.xml",
                "mapping.hbm.xml"
        };
    }


    final String referencePubZdbID = "ZDB-PUB-071218-1" ;
    Publication refPub ;

    

    void loadGenBankDBs() throws Exception{
        genBankLinks = sequenceRepository.getUniqueMarkerDBLinks( genBankGenomicDatabase, genBankRNADatabase) ;   // 1 - load genbank
    }


    void init() throws Exception{
        logger.debug("init" ) ;
        try{
            if(HibernateUtil.hasSessionFactoryDefined()==false){
                new HibernateSessionCreator(false, confFiles()) ;
            }
            sequenceRepository = RepositoryFactory.getSequenceRepository() ;


            geoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GEO,
                    ForeignDBDataType.DataType.OTHER,ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
            logger.debug("geoDatabase: " + geoDatabase) ;

            // zfEspressoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.ZF_ESPRESSO.toString(),
//                    ReferenceDatabase.Type.OTHER,ReferenceDatabase.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
 //           logger.debug("zfEspressoDatabase: " + zfEspressoDatabase) ;


//            arrayExpressDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.ARRAY_EXPRESS.toString(),
//                    ForeignDBDataType.DataType.OTHER,ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
//            logger.debug("arrayExpressDatabase: " + arrayExpressDatabase) ;


            genBankGenomicDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC,ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("genBankGenomicDatabase: " + genBankGenomicDatabase) ;


            genBankRNADatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.RNA,ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("genBankRNADatabase: " + genBankRNADatabase) ;

            refPub = RepositoryFactory.getPublicationRepository().getPublication(referencePubZdbID) ;

        }
        catch(Exception e){
            logger.error("failed to init: ",e);
            throw e ;
        }
    }


    /**
     * If not in genbank then delete.
     *
     * 3 
     */
    void cleanupOldLinks(Collection<String> newAccessions,ReferenceDatabase... referenceDatabases) throws Exception{
        Map<String,Set<MarkerDBLink>> currentMicroArrayLinkSets = sequenceRepository.getMarkerDBLinks( referenceDatabases) ;
        Set<DBLink> dbLinksToRemove =new HashSet<DBLink>() ;
        Session session = HibernateUtil.currentSession()  ;
        int numDeleted = 0 ; 
//        genBankLinks
        for(String accession : currentMicroArrayLinkSets.keySet()){
            if(false==genBankLinks.containsKey(accession)
                 ||
               false==newAccessions.contains(accession)
                    ){
                for(MarkerDBLink dbLinkToRemove:currentMicroArrayLinkSets.get(accession) ){
                    infoLogger.warn("removing dblink: " + accession) ; 
                    dbLinksToRemove.add(dbLinkToRemove) ; 
                    ++numDeleted ; 
                }
            }            
        }
        sequenceRepository.removeDBLinks(dbLinksToRemove) ; 
        infoLogger.info("number of links cleaned out: " + numDeleted) ; 
    }





    /**
     * Schedule addition of new MarkerDBLinks.
     * @param currentMicroArrayLinkSets
     * @param newMicroArrayAccessions
     */
    public void processNewLinks(Set<String> newMicroArrayAccessions,Map<String,Set<MarkerDBLink>> currentMicroArrayLinkSets,ReferenceDatabase... referenceDatabases){

        infoLogger.info("processNewLinks - microarray accessions to process for addition: " + newMicroArrayAccessions.size()   ) ;
        Set<MarkerDBLink> dbLinksToAdd = new HashSet<MarkerDBLink>() ;
        int numAdded = 0 ; 
        for(String newMicroArrayAccession : newMicroArrayAccessions){
            MarkerDBLink genBankLink = genBankLinks.get(newMicroArrayAccession) ; 
            Set<MarkerDBLink> currentMicroArrayLinkSet = currentMicroArrayLinkSets.get(newMicroArrayAccession) ;
//            if(genBankLink != null && currentMicroArrayLinkSet != null){
//               // check for update
//               // very unlikely, mostly encoding will change
//               if(genBankLink.getMarker()!=currentMicroArrayLinkSet.getMarker()){
//                   currentMicroArrayLinkSet.setMarker(genBankLink.getMarker());
//                   infoLogger.info("updating marker for accession["+newMicroArrayAccession+"] "+
//                           " for referenceDB["+referenceDatabase.getForeignDB().getDbName()) ;
//               }
//            }
//            else
            if(genBankLink != null && currentMicroArrayLinkSet == null){
                // add link for each referenceDatabase

                Marker marker = genBankLink.getMarker() ;
                if(marker.isInTypeGroup(Marker.TypeGroup.CDNA_AND_EST)
                        || marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)
                        )
                {
                    for(ReferenceDatabase referenceDatabase: referenceDatabases){
                        MarkerDBLink newLink = new MarkerDBLink() ;
                        newLink.setAccessionNumber(newMicroArrayAccession);
                        newLink.setMarker(genBankLink.getMarker());
                        newLink.setReferenceDatabase(referenceDatabase);
                        newLink.setLength(genBankLink.getLength());
                        ++numAdded ;
                        dbLinksToAdd.add( newLink ) ;
                        infoLogger.info("adding accession["+newMicroArrayAccession+"] "+
                                " for referenceDB["+referenceDatabase.getForeignDB().getDbName() +
                                "]") ;
                    }
                }
            }
            else
            if(genBankLink == null ){
               // note that accession not found for new accession
               notFoundLogger.info(newMicroArrayAccession);
            }
        }
        sequenceRepository.addDBLinks(dbLinksToAdd,refPub, 30000) ; 
        infoLogger.info("number of links added: " + numAdded) ; 


    }




    void run() {
        Session session = HibernateUtil.currentSession() ;
        session.beginTransaction() ;

        try{
            loadGenBankDBs() ;     // 1


            // Process 2715 and 1319 chipsets for GEO only
            Set<String> newGEOAccessions = new HashSet<String>() ;
            newGEOAccessions.addAll(   (new SoftParser2715()).parseUniqueNumbers() );
            newGEOAccessions.addAll(   (new SoftParser1319()).parseUniqueNumbers() );
            Map<String,Set<MarkerDBLink>> microarrayLinks = sequenceRepository.getMarkerDBLinks(geoDatabase ) ;   // 0 - load microarray
            processNewLinks(newGEOAccessions,microarrayLinks,geoDatabase) ; // 2
            cleanupOldLinks(newGEOAccessions,geoDatabase) ; // 3

                                                                                                                                                        

            // Process the 1319 chipset for all.  
            // Set<String> newOtherAccessions = new HashSet<String>() ;
            //newOtherAccessions.addAll(   (new SoftParser1319()).parseUniqueNumbers() );
//            //okay if it only returns one
            //microarrayLinks.clear();
            //microarrayLinks = sequenceRepository.getMarkerDBLinks(null, zfEspressoDatabase ) ;   // 0 - load microarray
           //processNewLinks( newOtherAccessions , microarrayLinks,zfEspressoDatabase) ;  // 2
            // cleanupOldLinks(newOtherAccessions,zfEspressoDatabase) ; // 3

//            microarrayLinks = sequenceRepository.getMarkerDBLinks(null, zfEspressoDatabase ,arrayExpressDatabase) ;   // 0 - load microarray
//            processNewLinks( newOtherAccessions , microarrayLinks,zfEspressoDatabase,arrayExpressDatabase) ;  // 2
//            cleanupOldLinks(newOtherAccessions,zfEspressoDatabase,arrayExpressDatabase) ; // 3



            session.getTransaction().commit() ;
        }
        catch(Exception e){
            errorLogger.fatal("failed to add dblinks",e);
            session.getTransaction().rollback();
        }


    }

    public static void main(String args[]){

        UpdateMicroArrayMain main = new UpdateMicroArrayMain() ;
        try{
            main.init() ;
            main.run() ;
        }
        catch(Exception e){
            // the error should already be logged
            e.printStackTrace() ;
        }

    }

} 


