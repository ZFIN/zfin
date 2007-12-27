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
//    ReferenceDatabase zfEspressoDatabase = null ;

    Map<String,MarkerDBLink> genBankLinks = null ; 
    Map<String,MarkerDBLink> microarrayLinks = null ; 


    ReferenceDatabase genBankGenomicDatabase  = null ;
    ReferenceDatabase genBankCDNADatabase  = null ;
    SequenceRepository sequenceRepository = null ;
    final String[] confFiles = {
            "sequence.hbm.xml",
            "marker.hbm.xml",
            "mapping.hbm.xml",
            "expression.hbm.xml",
            "anatomy.hbm.xml",
            "publication.hbm.xml",
            "orthology.hbm.xml",
            "mutant.hbm.xml",
            "people.hbm.xml",
            "infrastructure.hbm.xml",
    };



    //    final String referencePubZdbID ="ZDB-PUB-071101-1" ;
    String referencePubZdbID = "ZDB-PUB-071218-1" ;
    Publication refPub ;

    

    void loadGenBankDBs() throws Exception{
        genBankLinks = sequenceRepository.getUniqueMarkerDBLinks( genBankGenomicDatabase, genBankCDNADatabase) ;   // 1 - load genbank
    }


    void init() throws Exception{
        logger.debug("init" ) ;
        try{
            if(HibernateUtil.hasSessionFactoryDefined()==false){
                new HibernateSessionCreator(false, confFiles) ;
            }
            sequenceRepository = RepositoryFactory.getSequenceRepository() ;
            geoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GEO.toString(),
                    ReferenceDatabase.Type.OTHER,ReferenceDatabase.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
            logger.debug("geoDatabase: " + geoDatabase) ;

//            zfEspressoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.ZF_ESPRESSO.toString(),
//                    ReferenceDatabase.Type.OTHER,ReferenceDatabase.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
//            logger.debug("zfEspressoDatabase: " + zfEspressoDatabase) ;

            ForeignDB geneBankForeignDB = sequenceRepository.getForeignDBByName("GenBank");
            genBankGenomicDatabase = sequenceRepository.getReferenceDatabaseByAlternateKey(geneBankForeignDB,
                    ReferenceDatabase.Type.GENOMIC,ReferenceDatabase.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("genBankGenomicDatabase: " + genBankGenomicDatabase) ;

            genBankCDNADatabase = sequenceRepository.getReferenceDatabaseByAlternateKey(geneBankForeignDB,
                    ReferenceDatabase.Type.CDNA,ReferenceDatabase.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("genBankCDNADatabase: " + genBankCDNADatabase) ;

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
        Set<MarkerDBLink> dbLinksToRemove =new HashSet<MarkerDBLink>() ;
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



            // Process the 1319 chipset for all.  
//            Set<String> newOtherAccessions = new HashSet<String>() ;
//            newOtherAccessions.addAll(   (new SoftParser1319()).parseUniqueNumbers() );
//            //okay if it only returns one
//            microarrayLinks.clear();
//            microarrayLinks = sequenceRepository.getMarkerDBLinks(null, geoDatabase, zfEspressoDatabase ) ;   // 0 - load microarray
//            processNewLinks( newOtherAccessions , microarrayLinks,geoDatabase, zfEspressoDatabase) ;  // 2
//
//
//            newOtherAccessions.addAll(newGEOAccessions) ;
            cleanupOldLinks(newGEOAccessions,geoDatabase) ; // 3

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


