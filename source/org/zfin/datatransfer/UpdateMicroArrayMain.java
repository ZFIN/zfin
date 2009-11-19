package org.zfin.datatransfer ;


import org.apache.log4j.Logger;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.*;

import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;
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
//    final Logger notFoundLogger  = Logger.getLogger(ZfinProperties.MICROARRAY_NOT_FOUND);
//    final Logger errorLogger = Logger.getLogger(ZfinProperties.MICROARRAY_ERROR);
//    final Logger infoLogger  = Logger.getLogger(ZfinProperties.MICROARRAY_INFO);
    ReferenceDatabase geoDatabase = null ;
    ReferenceDatabase zfEspressoDatabase = null ;
//    ReferenceDatabase arrayExpressDatabase = null ;

    Map<String,MarkerDBLink> genBankLinks = null ;
    Map<String,MarkerDBLink> microarrayLinks = null ;


    ReferenceDatabase genBankGenomicDatabase  = null ;
    ReferenceDatabase genBankRNADatabase  = null ;
    ReferenceDatabase refseqRNADatabase  = null ;
    ReferenceDatabase mirbaseStemLoopDatabase  = null ;
    ReferenceDatabase mirbaseMatureDatabase  = null ;
    SequenceRepository sequenceRepository = null ;

    MicroArrayBean microArrayBean = null ;


    final String referencePubZdbID = "ZDB-PUB-071218-1" ;
    Publication refPub ;



    void loadGenBankDBs() throws Exception{
        genBankLinks = sequenceRepository.getUniqueMarkerDBLinks( genBankGenomicDatabase, genBankRNADatabase, refseqRNADatabase, mirbaseStemLoopDatabase, mirbaseMatureDatabase) ;   // 1 - load genbank
    }


    void init() throws Exception{
        logger.debug("init" ) ;
        microArrayBean = new MicroArrayBean();
        try{
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

            refseqRNADatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.REFSEQ,
                    ForeignDBDataType.DataType.RNA,ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("refseqRNADatabase: " + refseqRNADatabase) ;

            mirbaseStemLoopDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.MIRBASE_STEM_LOOP,
                    ForeignDBDataType.DataType.RNA,ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("mirbaseStemLoopDatabase: " + mirbaseStemLoopDatabase) ;

            mirbaseMatureDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.MIRBASE_MATURE,
                    ForeignDBDataType.DataType.RNA,ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("mirbaseMatureDatabase: " + mirbaseMatureDatabase) ;

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
        MultiValueMap currentMicroArrayLinkSets = sequenceRepository.getMarkerDBLinks( referenceDatabases) ;
        Set<DBLink> dbLinksToRemove =new HashSet<DBLink>() ;
        int numDeleted = 0 ;
//        genBankLinks
        Set<String> currentMicroArrayAccessions = (Set<String>) currentMicroArrayLinkSets.keySet() ;
        for(String accession : currentMicroArrayAccessions ){
            if(false==genBankLinks.containsKey(accession)
                    ||
                    false==newAccessions.contains(accession)
                    ){
                    Collection<DBLink> dbLinksToRemoveForAccession  = currentMicroArrayLinkSets.getCollection(accession) ;
                    logger.warn("removing all dblink: " + accession + " zdb-ID: "+ dbLinksToRemoveForAccession.size()) ;
                    dbLinksToRemove.addAll(dbLinksToRemoveForAccession) ;
                    numDeleted += dbLinksToRemoveForAccession.size() ;
            }
        }
        sequenceRepository.removeDBLinks(dbLinksToRemove) ;
        microArrayBean.addMessage("number of links cleaned out: " + numDeleted) ;
    }





    /**
     * Schedule addition of new MarkerDBLinks.
     * @param currentMicroArrayLinkSets
     * @param newMicroArrayAccessions
     */
    public void processNewLinks(Set<String> newMicroArrayAccessions,Map<String,Collection<MarkerDBLink>> currentMicroArrayLinkSets,ReferenceDatabase... referenceDatabases){

        microArrayBean.addMessage("processNewLinks - microarray accessions to process for addition: " + newMicroArrayAccessions.size()   ); 
        Set<MarkerDBLink> dbLinksToAdd = new HashSet<MarkerDBLink>() ;
        int numAdded = 0 ;
        for(String newMicroArrayAccession : newMicroArrayAccessions){
            MarkerDBLink genBankLink = genBankLinks.get(newMicroArrayAccession) ;
            Collection<MarkerDBLink> currentMicroArrayLinkSet = currentMicroArrayLinkSets.get(newMicroArrayAccession) ;
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
                        microArrayBean.addMessage("adding accession["+newMicroArrayAccession+"] "+
                                " for referenceDB["+referenceDatabase.getForeignDB().getDbName() +
                                "]");
                    }
                }
            }
            else
            if(genBankLink == null ){
                // note that accession not found for new accession
                logger.info("not found in genbank: " + newMicroArrayAccession);
                microArrayBean.addNotFound(newMicroArrayAccession) ;
            }
        }
        sequenceRepository.addDBLinks(dbLinksToAdd,refPub, 30000) ;
        microArrayBean.addMessage("number of links added: " + numAdded); ;


    }




    void run() {
        Session session = HibernateUtil.currentSession() ;
        session.beginTransaction() ;

        try{
            logger.debug("start loading genbank");
            loadGenBankDBs() ;     // 1
            logger.debug("end loading genbank");


            // Process 2715 and 1319 chipsets for GEO only
            Set<String> newGEOAccessions = new HashSet<String>() ;
            logger.debug("start parsing 2715");
            newGEOAccessions.addAll(   (new GPLSoftParser2715()).parseUniqueNumbers() );
            logger.debug("finished parsing 2715");
            logger.debug("start parsing 1319");
            newGEOAccessions.addAll(   (new GPLSoftParser1319()).parseUniqueNumbers() );
            logger.debug("finished parsing 1319");
            logger.debug("start get current microarray links");
            Map<String,Collection<MarkerDBLink>> microarrayLinks = sequenceRepository.getMarkerDBLinks(geoDatabase ) ;   // 0 - load microarray
            logger.debug("finished getting current microarray links");
            logger.debug("start processing new links");
            processNewLinks(newGEOAccessions,microarrayLinks,geoDatabase) ; // 2
            logger.debug("finished processing new links");
            logger.debug("start cleaning up old links");
            cleanupOldLinks(newGEOAccessions,geoDatabase) ; // 3
            logger.debug("finish cleaning up old links");



            // Process the 1319 chipset for all.  
            // Set<String> newOtherAccessions = new HashSet<String>() ;
            //newOtherAccessions.addAll(   (new GPLSoftParser1319()).parseUniqueNumbers() );
//            //okay if it only returns one
            //microarrayLinks.clear();
            //microarrayLinks = sequenceRepository.getMarkerDBLinks(null, zfEspressoDatabase ) ;   // 0 - load microarray
            //processNewLinks( newOtherAccessions , microarrayLinks,zfEspressoDatabase) ;  // 2
            // cleanupOldLinks(newOtherAccessions,zfEspressoDatabase) ; // 3

//            microarrayLinks = sequenceRepository.getMarkerDBLinks(null, zfEspressoDatabase ,arrayExpressDatabase) ;   // 0 - load microarray
//            processNewLinks( newOtherAccessions , microarrayLinks,zfEspressoDatabase,arrayExpressDatabase) ;  // 2
//            cleanupOldLinks(newOtherAccessions,zfEspressoDatabase,arrayExpressDatabase) ; // 3



            session.getTransaction().commit() ;
//            session.getTransaction().rollback(); ;
        }
        catch(Exception e){
            logger.error("failed to add dblinks",e);
            session.getTransaction().rollback();
        }
    }

    public MicroArrayBean getMicroArrayBean() {
        return microArrayBean;
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


