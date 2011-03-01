package org.zfin.datatransfer.microarray;


import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.marker.Marker;
import org.zfin.orthology.Species;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Class UpdateMicroarrayMain processes platform documentation for microarrays which we will provide links to
 * within markerview.  See case
 * <a href="http://zfinwinserver1.uoregon.edu/fogbugz/default.asp?pgx=EV&ixBug=2009&=#edit_1_2009">2009</a> in
 * FogBugz.
 */
public final class MicroarrayProcessor {

    final Logger logger = Logger.getLogger(MicroarrayProcessor.class);

    private boolean listNotFound = false;

    private ReferenceDatabase geoDatabase = null;
    //    ReferenceDatabase zfEspressoDatabase = null ;
//    ReferenceDatabase arrayExpressDatabase = null ;
    private ReferenceDatabase genBankGenomicDatabase = null;
    private ReferenceDatabase genBankRNADatabase = null;
    private ReferenceDatabase refseqRNADatabase = null;
    private ReferenceDatabase mirbaseStemLoopDatabase = null;
    private ReferenceDatabase mirbaseMatureDatabase = null;
    private SequenceRepository sequenceRepository = null;


    final String referencePubZdbID = "ZDB-PUB-071218-1";
    private Publication refPub;


    public void init() throws Exception {
        logger.debug("init");
        try {
            sequenceRepository = RepositoryFactory.getSequenceRepository();

            geoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GEO,
                    ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
            logger.debug("geoDatabase: " + geoDatabase);

            // zfEspressoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.ZF_ESPRESSO.toString(),
//                    ReferenceDatabase.Type.OTHER,ReferenceDatabase.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
            //           logger.debug("zfEspressoDatabase: " + zfEspressoDatabase) ;


//            arrayExpressDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.ARRAY_EXPRESS.toString(),
//                    ForeignDBDataType.DataType.OTHER,ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
//            logger.debug("arrayExpressDatabase: " + arrayExpressDatabase) ;


            genBankGenomicDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("genBankGenomicDatabase: " + genBankGenomicDatabase);


            genBankRNADatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.RNA, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("genBankRNADatabase: " + genBankRNADatabase);

            refseqRNADatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.REFSEQ,
                    ForeignDBDataType.DataType.RNA, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("refseqRNADatabase: " + refseqRNADatabase);

            mirbaseStemLoopDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.MIRBASE_STEM_LOOP,
                    ForeignDBDataType.DataType.RNA, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("mirbaseStemLoopDatabase: " + mirbaseStemLoopDatabase);

            mirbaseMatureDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.MIRBASE_MATURE,
                    ForeignDBDataType.DataType.RNA, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
            logger.debug("mirbaseMatureDatabase: " + mirbaseMatureDatabase);

            refPub = RepositoryFactory.getPublicationRepository().getPublication(referencePubZdbID);

        } catch (Exception e) {
            logger.error("failed to init: ", e);
            throw e;
        }
    }


    /**
     * Schedule addition of new MarkerDBLinks.
     *
     * @param newMicroarrayAccessions Accessions parsed.
     * @param genBankLinks            Genbank links to compare against.
     * @param referenceDatabases      Reference databases to add to and subtract from.
     * @return Bean of file output.
     * @throws IOException Exception thrown if unable to write file.
     */
    public MicroarrayBean processNewLinks(Set<String> newMicroarrayAccessions, Map<String, MarkerDBLink> genBankLinks, ReferenceDatabase... referenceDatabases) throws IOException {
        logger.info("start processing new links");

        MicroarrayBean microarrayBean = new MicroarrayBean();


        microarrayBean.addMessage("processNewLinks - microarray accessions to process for addition: " + newMicroarrayAccessions.size());

        logger.info("total accessions: " + newMicroarrayAccessions.size());
        microarrayBean.addMessage("total accession PARSED: " + newMicroarrayAccessions.size());

        Collection<String> accessionsInGenbank = CollectionUtils.intersection(newMicroarrayAccessions, genBankLinks.keySet());
        microarrayBean.addMessage("accessions FOUND in genbank: " + accessionsInGenbank.size());

        Map<String, Collection<MarkerDBLink>> currentMicroarrayLinks = sequenceRepository.getMarkerDBLinks(referenceDatabases);   // 0 - load microarray

        microarrayBean.addMessage("CURRENT microarray accession: " + currentMicroarrayLinks.size());


        List<String> accessionsToAdd = new ArrayList<String>(CollectionUtils.subtract(accessionsInGenbank, currentMicroarrayLinks.keySet()));
        Collections.sort(accessionsToAdd);

        microarrayBean.addMessage("accessions TO ADD that are in genbank: " + accessionsToAdd.size());

        if (CollectionUtils.isNotEmpty(accessionsToAdd)) {
            Session session = HibernateUtil.currentSession();
            CacheMode oldCacheMode = session.getCacheMode();
            session.setCacheMode(CacheMode.IGNORE);
            try {
                microarrayBean = addMicroarrayAcessions(accessionsToAdd, genBankLinks, microarrayBean, geoDatabase);
            } finally {
                session.setCacheMode(oldCacheMode);
            }
        }


        Collection<String> accessionsToRemove = CollectionUtils.subtract(currentMicroarrayLinks.keySet(), accessionsInGenbank);
        microarrayBean.addMessage("accessions TO REMOVE: " + accessionsToRemove.size());

        if (CollectionUtils.isNotEmpty(accessionsToRemove)) {
            microarrayBean = removeMicroarrayAccessions(accessionsToRemove, microarrayBean, currentMicroarrayLinks);
        }

        // based on this:
        // http://java.sun.com/docs/books/tutorial/collections/interfaces/set.html
        // using collections.removeall should be much faster
//        Collection<String> accessionsNotFound = CollectionUtils.subtract(newMicroarrayAccessions,genBankLinks.keySet()) ;
        newMicroarrayAccessions.removeAll(genBankLinks.keySet());
        microarrayBean.addMessage("accessions NOT FOUND in genbank: " + newMicroarrayAccessions.size());
        if (listNotFound) {
            microarrayBean.setNotFoundAccessions(newMicroarrayAccessions);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(File.createTempFile("microarray_report", ".txt")));
            writer.write(microarrayBean.toString());
            writer.close();
        } catch (IOException e) {
            logger.error("could not write out report", e.fillInStackTrace());
        }

        logger.info("finished processing new links: " + newMicroarrayAccessions.size());

        return microarrayBean;
    }

    public MicroarrayBean removeMicroarrayAccessions(Collection<String> accessionsToRemove, MicroarrayBean microarrayBean, Map<String, Collection<MarkerDBLink>> currentMicroarrayLinks) throws IOException {
        Set<DBLink> dbLinksToRemove = new HashSet<DBLink>();
        int numDeleted = 0;
        for (String accession : accessionsToRemove) {
            Collection<MarkerDBLink> dbLinksToRemoveForAccession = currentMicroarrayLinks.get(accession);
            logger.info("removing all GEO dblinks for accession: " + accession + " zdb-ID: " + dbLinksToRemoveForAccession.size());
            dbLinksToRemove.addAll(dbLinksToRemoveForAccession);
            numDeleted += dbLinksToRemoveForAccession.size();
        }

        // add to report
        int i = 0;
        for (DBLink dbLink : dbLinksToRemove) {
            microarrayBean.addMessage("accession " + (++i) + "/" + dbLinksToRemove.size() + " TO REMOVE: " + dbLink.getAccessionNumber());
        }
        sequenceRepository.removeDBLinks(dbLinksToRemove);
        microarrayBean.addMessage("accessions actually REMOVED: " + numDeleted);
        return microarrayBean;
    }

    public MicroarrayBean addMicroarrayAcessions(Collection<String> accessionsToAdd, Map<String, MarkerDBLink> genBankLinks, MicroarrayBean microarrayBean, ReferenceDatabase... referenceDatabases) throws IOException {
        int microarrayAccessionsAdded = 0;
        int cacheSize = 20;
        Set<MarkerDBLink> dbLinksToAdd = new HashSet<MarkerDBLink>();
        try {
            for (String newMicroarrayAccession : accessionsToAdd) {
                MarkerDBLink genBankLink = genBankLinks.get(newMicroarrayAccession);

                Marker marker = genBankLink.getMarker();
                if (marker.isInTypeGroup(Marker.TypeGroup.CDNA_AND_EST)
                        || marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)
                        ) {
                    for (ReferenceDatabase referenceDatabase : referenceDatabases) {
                        MarkerDBLink newLink = new MarkerDBLink();
                        newLink.setAccessionNumber(newMicroarrayAccession);
                        newLink.setMarker(genBankLink.getMarker());
                        newLink.setReferenceDatabase(referenceDatabase);
                        newLink.setLength(genBankLink.getLength());
                        ++microarrayAccessionsAdded;
                        dbLinksToAdd.add(newLink);
                        microarrayBean.addMessage("adding accession[" + newMicroarrayAccession + "] " +
                                " for referenceDB[" + referenceDatabase.getForeignDB().getDbName() +
                                "]");
                    }
                }
                if (dbLinksToAdd.size() >= cacheSize) {
                    sequenceRepository.addDBLinks(dbLinksToAdd, refPub, 20);
                }
            }
            sequenceRepository.addDBLinks(dbLinksToAdd, refPub, 20);

        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
        }

        microarrayBean.addMessage("number of links actually added: " + microarrayAccessionsAdded);

        return microarrayBean;
    }

    public Set<String> getGEOAccessions() {
        // Process chipsets for GEO only
        Set<String> newGEOAccessions = new TreeSet<String>();

        // will have to be done as part of a blast, just reporting sequence
        // see fogbugz 5116 for more details on these
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL5783") );
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL5746") ) ;
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7132",5) ) ;
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7133",5) ) ;


        // also may need to fix for multiple species
        // see fogbugz 5115 for more details on these
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL6683",4) ) ;

        // multiple in column, comma-separated only want dre value, must allow multiple sequences to be parsed per line
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL8021",1) ) ;
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL6432",3) ) ;

        // same as above, but pipe separated
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL6741",2) ) ;

        // same as above, but underscore separated
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL8131",1,"dre-miR") ) ;

//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL3766",6) ) ; // NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL3767",6) ) ; // NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7125",5) ) ; // NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7126",5) ) ; // NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7127",5) ) ; // NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7128",5) ) ;// NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7129",5) ) ;// NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7130",5) ) ;// NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7131",5) ) ;// NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL6600",8) ) ; // NC_*
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL6601",8) ) ; // NC_*

        // compugen section
        SoftParser defaultSoftParser = new DefaultGeoSoftParser();

        // from fogbugz 5003 excel spreadsheet
        // MWG Biotech
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL530", 7, null,
                new String[]{"DRFRAMEFINDER", "empty", "control"}));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL531", 7, null,
                new String[]{"DRFRAMEFINDER", "empty", "control"}));

        // affy
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL1319", 2,
                new String[]{"Danio rerio"}, new String[]{"Control"}));

        // agilent
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL6457", 8, null, new String[]{"ENSDART", "XM_", "XR_"}));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL6563", 4, null, new String[]{"ENSDART", "XM_", "XR_"}));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7244", 5));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7302", 5));

        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL4375", 11));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL4481", 2, null, new String[]{"XM_"}));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL3548", 5));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL5692", 2));

        // Chou, academia sinica
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL5182", 2));

        // medical college of wisonsin
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7194", 7));

        // cincinnati children's hospital
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7338", 2));

        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL4518", 2));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL1743", 2));

        // Webb, Hemholtz Zentrem Muenchen
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7801", 3));

        // Bannister CSIRO
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7472", 3, new String[]{"dre|"}));

        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL5675", 5));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL3365", 5));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL4603", 9));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL4609", 4));

        // Garnett, Berkeley
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7343", 4));

        // Zakrewiski, U of Leiden
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7735", 3));

        // no data yet
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL5724",3) );

        // Miller, U of O
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL7556", 2));

        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL5720", 2));
        // no data yet
//            newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL4014",2) );
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL3721", 2));
        newGEOAccessions.addAll(defaultSoftParser.parseUniqueNumbers("GPL2715", 2));
        logger.info("finished parsing microarray downloads: " + newGEOAccessions.size());
        return newGEOAccessions;
    }

    public MicroarrayBean run() {
        HibernateUtil.createTransaction();

        MicroarrayBean microarrayBean = null;

        try {
            microarrayBean = new MicroarrayBean();
            Set<String> newGEOAccessions = getGEOAccessions();

            Map<String, MarkerDBLink> genBankLinks = sequenceRepository.getUniqueMarkerDBLinks(getGenbankReferenceDatabases());   // 1 - load genbank

            microarrayBean = processNewLinks(newGEOAccessions, genBankLinks, geoDatabase); // 2

            // to process others
//            microarrayLinks = sequenceRepository.getMarkerDBLinks(null, zfEspressoDatabase ,arrayExpressDatabase) ;   // 0 - load microarray
//            processNewLinks( newOtherAccessions , microarrayLinks,zfEspressoDatabase,arrayExpressDatabase) ;  // 2

//            HibernateUtil.rollbackTransaction();
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error("failed to do microarray update", e);
            try {
                microarrayBean.addMessage("Failed to to microarray update\n" + e.fillInStackTrace().toString());
            } catch (IOException e1) {
                logger.error(e.fillInStackTrace());
            }
            HibernateUtil.rollbackTransaction();
        } finally {
            HibernateUtil.closeSession();
        }
        return microarrayBean;
    }

    public ReferenceDatabase[] getGenbankReferenceDatabases() {
        ReferenceDatabase[] referenceDatabases = new ReferenceDatabase[5];
        referenceDatabases[0] = genBankGenomicDatabase;
        referenceDatabases[1] = genBankRNADatabase;
        referenceDatabases[2] = mirbaseMatureDatabase;
        referenceDatabases[3] = mirbaseStemLoopDatabase;
        referenceDatabases[4] = refseqRNADatabase;
        return referenceDatabases;
    }

    public boolean isListNotFound() {
        return listNotFound;
    }

    public void setListNotFound(boolean listNotFound) {
        this.listNotFound = listNotFound;
    }

    public static void main(String args[]) {

        MicroarrayProcessor processor = new MicroarrayProcessor();
        try {
            new HibernateSessionCreator(false);
            processor.init();
            MicroarrayBean microarrayBean = processor.run();
            (new IntegratedJavaMailSender()).sendMail("microarray updates for: " + (new Date()).toString()
                    , microarrayBean.toString()
                    , ZfinProperties.splitValues(ZfinPropertiesEnum.VALIDATION_EMAIL_OTHER));
        } catch (Exception e) {
            // the error should already be logged
            e.printStackTrace();
        }

    }

} 


