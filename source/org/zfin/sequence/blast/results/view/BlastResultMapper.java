package org.zfin.sequence.blast.results.view;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.marker.*;
import org.zfin.marker.Transcript;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.BlastDatabaseException;
import org.zfin.sequence.blast.BlastService;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.WebHostDatabaseStatisticsCache;
import org.zfin.sequence.blast.presentation.BlastPresentationService;
import org.zfin.sequence.blast.presentation.DatabaseNameComparator;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.*;
import org.zfin.gbrowse.GBrowseService;

import java.util.*;

/**
 * This class maps the jaxb generated classes to database backed presentation BlastResultBean object viewable on the blast results pages.
 * <p/>
 * Every blast hit has a related accession that refers to one or more dblinks.
 * We only use one dblink as the "hit" dblink.
 * That dblink relates directly to a transcript or a clone through its recID.
 * The "hit" dblink may have either an associated transcript, clone, or have no association and the dblink must not be derived from a supporting sequence (ie, genbank for an OTTDART will always be a supporting sequence).
 * In the case where there is no associated clone or transcript, the directly related gene provides the "gene" link for the blast hit.  Otherwise, the gene is retrieved from the "hit" dblink transcript or clone by either the "gene produces transcript" or "gene encodes small segment" relationships.
 * <p/>
 * The process should then be:
 * - get the hit accession ID
 * - get DBLinks for that accession ID, REMOVING SUPPORTING SEQUENCES
 * - if we have a clone ID, set that as the hitMarker and get the gene from that
 * - else if we have the transcript ID, set that as the hitMarker and get the gene from that
 * - else, if the only dblink is for gene, then their is no hitMarker, and we use the direct dblink (hopefully just one) as the correct gene
 * - if no dblink for gene then we should see if there is a hit on a genomic clone
 * - in this case there could be 0...* genes associated with the clone hit
 * - if no dblink still we should assume that there is no gene, then check for the accession to display
 */
public class BlastResultMapper {

    private static final Logger logger = Logger.getLogger(BlastResultMapper.class);
    private static DatabaseNameComparator databaseNameComparator = new DatabaseNameComparator();

    public static BlastResultBean createBlastResultBean(BlastOutput blastOutput) {

        if (blastOutput == null) {
            return null;
        }

        // todo: caching
        // 1. get all hit accession IDS
        // 2. do a query for all markers, etc.  associated with those DBLink accessions (grab as much as possible)
        //      -> may have to do 2, one for transcripts, one for clones . . . .
        //      -> cache into a HashMap of N HitViewBean's (probably use what I already have) keyed by accessionID
        // 3. get the M<N genes associated with each HitViewBean (add them to a new HashTable (put does not do a copy, but maintains a reference))?
        // 4. for those M genes, requery to get the figures, go, phenotype, etc. thereby reducing the # of queries (2-4*N?) or just 1
        //      -> plug the results of the queries back into the items
        // 5. now run through the hits from the XML again, as below and set new values for the keyed accession


        BlastResultBean blastResultBean = new BlastResultBean();

        // set up xmlBlastBean
        XMLBlastBean inputBlastBean = new XMLBlastBean();
        blastResultBean.setXmlBlastBean(inputBlastBean);

        inputBlastBean.setProgram(blastOutput.getBlastOutputProgram().getContent());
        blastResultBean.setQueryLength(Integer.parseInt(blastOutput.getBlastOutputQueryLen().getContent()));
        String queryDefLine = "";
        if (StringUtils.isNotEmpty(blastOutput.getBlastOutputQueryID().getContent())) {
            queryDefLine += blastOutput.getBlastOutputQueryID().getContent();
        }

        if (StringUtils.isNotEmpty(blastOutput.getBlastOutputQueryDef().getContent())) {
            queryDefLine += " ";
            queryDefLine += blastOutput.getBlastOutputQueryDef().getContent();
        }
        blastResultBean.setDefLine(queryDefLine);

        Parameters blastParameters = blastOutput.getBlastOutputParam().getParameters();
        if (blastParameters.getParametersFilter() != null) {
            String filterString = blastParameters.getParametersFilter().getContent();
            blastResultBean.setFilter(filterString);
            inputBlastBean.setDust(filterString.contains(BlastService.FILTER_DUST));
            inputBlastBean.setSeg(filterString.contains(BlastService.FILTER_SEG));
            inputBlastBean.setXnu(filterString.contains(BlastService.FILTER_XNU));
        }

        inputBlastBean.setExpectValue(Double.parseDouble(blastParameters.getParametersExpect().getContent()));

        if (blastParameters.getParametersMatrix() != null) {
            inputBlastBean.setMatrix(blastParameters.getParametersMatrix().getContent());
        }


        if (blastOutput.getZFINParameters().getWordLength() != null) {
            inputBlastBean.setWordLength(Integer.parseInt(blastOutput.getZFINParameters().getWordLength().getContent()));
        }
        if (blastOutput.getZFINParameters().getSubSequenceFrom() != null) {
            inputBlastBean.setQueryFrom(Integer.parseInt(blastOutput.getZFINParameters().getSubSequenceFrom().getContent()));
        }

        if (blastOutput.getZFINParameters().getSubSequenceTo() != null) {
            inputBlastBean.setQueryTo(Integer.parseInt(blastOutput.getZFINParameters().getSubSequenceTo().getContent()));
        }

        if (blastOutput.getZFINParameters().getQueryType() != null) {
            inputBlastBean.setQueryType(blastOutput.getZFINParameters().getQueryType().getContent());
        }

        if (blastOutput.getZFINParameters().getDataLibrary() != null) {
            inputBlastBean.setDataLibraryString(blastOutput.getZFINParameters().getDataLibrary().getContent());
        }

        if (blastOutput.getZFINParameters().getSequenceFASTA() != null) {
            inputBlastBean.setQuerySequence(blastOutput.getZFINParameters().getSequenceFASTA().getContent());
        }

        if (blastOutput.getZFINParameters().getSequenceID() != null) {
            inputBlastBean.setSequenceID(blastOutput.getZFINParameters().getSequenceID().getContent());
        }

        if (blastOutput.getZFINParameters().getSequenceFile() != null) {
            inputBlastBean.setSequenceFile(blastOutput.getZFINParameters().getSequenceFile().getContent());
        }

        if (blastOutput.getZFINParameters().getPolyAFilter() != null) {
            inputBlastBean.setPoly_a(Integer.parseInt(blastOutput.getZFINParameters().getPolyAFilter().getContent()) > 0);
        }


        List<String> otherTickets = new ArrayList<String>();
        if (blastOutput.getZFINParameters().getOtherTickets() != null) {
            List<Ticket> tickets = (List<Ticket>) blastOutput.getZFINParameters().getOtherTickets().getTicket();
            if (CollectionUtils.isNotEmpty(tickets)) {
                for (Ticket ticket : tickets) {
                    otherTickets.add(ticket.getContent());
                }
            }
        }
        blastResultBean.setTickets(otherTickets);

        // because we no longer have the original request, we need to see what the common public database request is
        // if not root
        Set<Database> databases = new HashSet<Database>();
//        StringTokenizer databaseStringTokens  = new StringTokenizer(blastOutput.getBlastOutputDb().getContent()," ") ;
        boolean isRoot = Person.isCurrentSecurityUserRoot();

        logger.debug("is root: " + isRoot);
        int numberOfSequences = 0;
        if (blastOutput.getZFINParameters().getTargetDatabases() != null) {
            List<TargetDatabase> otherDatabasesFromQuery = (List<TargetDatabase>) blastOutput.getZFINParameters().getTargetDatabases().getTargetDatabase();
            if (CollectionUtils.isNotEmpty(otherDatabasesFromQuery)) {
                logger.debug("number of databii: " + otherDatabasesFromQuery.size());
                for (TargetDatabase targetDatabase : otherDatabasesFromQuery) {
//        while(databaseStringTokens.hasMoreTokens()){
                    String databaseString = targetDatabase.getContent();
                    String databaseAbbrevString = databaseString.substring(databaseString.lastIndexOf("/") + 1);
                    logger.debug("get database abbrev sring: " + databaseAbbrevString);
                    Database database = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.getType(databaseAbbrevString.trim()));
                    logger.debug("get database: " + database.getAbbrev());
                    try {
                        int numSequences = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database).getNumSequences();
                        if (numSequences > 0) {
                            numberOfSequences += numSequences;
                        }
                    } catch (BlastDatabaseException e) {
                        logger.error("failed to get number of sequences for: " + database);
                    }

                    // show all of the databases if we are root
                    if (database.isPublicDatabase() || isRoot) {
                        databases.add(database);
                    } else {
                        Database publicParentDatabase = BlastPresentationService.getFirstPublicParentDatabase(database);
                        if (publicParentDatabase != null) {
                            databases.add(publicParentDatabase);
                        }
                    }
                }
            }
        }
        blastResultBean.setNumberOfSequences(numberOfSequences);

        // otherwise, we show multiple, I guess
        blastResultBean.setDatabases(BlastPresentationService.createPresentationBeansWithRemoteSize(databases));
        Collections.sort(blastResultBean.getDatabases(), databaseNameComparator);


        // set the query length
        blastResultBean.setQueryLength(Integer.parseInt(blastOutput.getBlastOutputQueryLen().getContent()));


        // process hits
        List<Hit> hits = ((List<Iteration>) blastOutput.getBlastOutputIterations().getIteration()).get(0).getIterationHits().getHit();
        Map<String, HitViewBean> hitViewBeans = getBeanMapForHits(hits);

        Map<String, List<DBLink>> dbLinkMap = RepositoryFactory.getSequenceRepository().getDBLinksForAccessions(hitViewBeans.keySet());


        List<HitViewBean> hitViewBeansSorted = new ArrayList(hitViewBeans.values());
        Collections.sort(hitViewBeansSorted, new HitViewBeanComparator());


        Iterator<HitViewBean> hitViewBeanIterator = hitViewBeansSorted.iterator();
        HitViewBean hitViewBean;
        hitLoop:
        while (hitViewBeanIterator.hasNext()) {
            hitViewBean = hitViewBeanIterator.next();
            Marker hitMarker = null;
            String hitAccessionID = hitViewBean.getAccessionNumber();

//          get DBLinks for that accession ID, REMOVING SUPPORTING SEQUENCES
//            List<DBLink> dbLinks = RepositoryFactory.getSequenceRepository().getDBLinksForAccession(hitAccessionID ) ;
            List<DBLink> dbLinks = dbLinkMap.get(hitAccessionID);
            Set<Marker> genes = new HashSet<Marker>();
            if (CollectionUtils.isNotEmpty(dbLinks)) {
//            if we have a clone ID, set that as the hitMarker and get the gene from that
//            else if we have the transcript ID, set that as the hitMarker and get the gene from that
//            else, if the only dblink is for gene, then their is no hitMarker, and we use the direct dblink (hopefully just one) as the correct gene

                DBLink cloneDBLink = null;
                DBLink transcriptDBLink = null;
                DBLink geneDBLink = null;
                for (DBLink dbLink : dbLinks) {
                    if (dbLink.getReferenceDatabase().getForeignDBDataType().getSuperType() == ForeignDBDataType.SuperType.SEQUENCE) {
                        // if we do transcript
                        if (dbLink.getDataZdbID().startsWith("ZDB-TSCRIPT-")) {
                            if (ForeignDB.AvailableName.GENBANK != dbLink.getReferenceDatabase().getForeignDB().getDbName()) {
                                transcriptDBLink = dbLink;
                            }
                        } else if (dbLink.getDataZdbID().startsWith("ZDB-GENE-")) {
                            geneDBLink = dbLink;
                        } else if (dbLink.getDataZdbID().startsWith("ZDB-CDNA-")
                                ||
                                dbLink.getDataZdbID().startsWith("ZDB-EST-")
                                ) {
                            // needs to be a small segment clone
                            // not all clones are mapped to the clone table, so we will just assume its a marker
                            cloneDBLink = dbLink;
                        }
                    }
                }

                // if there are no hits in sequences, then assume we have a genomic clone
                if (geneDBLink == null && cloneDBLink == null && transcriptDBLink == null) {
                    for (DBLink dbLink : dbLinks) {
                        if(false==dbLink.getDataZdbID().startsWith("ZDB-ORTHO-")){
                            logger.debug("geneDBLink is null so setting cloneDBLink for a genomic: " + dbLink);
                            Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(dbLink.getDataZdbID());
                            // if a genomic clone then we handle appropriately
                            if (marker.isInTypeGroup(Marker.TypeGroup.CLONE)) {
                                cloneDBLink = dbLink;
                                // not all clones are mapped to the clone table, so we will just assume its a marker
                                hitMarker = RepositoryFactory.getMarkerRepository().getMarkerByID(cloneDBLink.getDataZdbID());
                                hitViewBean.setHitDBLink(cloneDBLink);
                                genes = MarkerService.getRelatedMarker(hitMarker, MarkerRelationship.Type.CLONE_CONTAINS_GENE);
                            }
                        }
                    }

                    // if there are no hits, then go ahead and use the genbank transcript hit
                    if (cloneDBLink == null) {
                        for (DBLink dbLink : dbLinks) {
                            if (dbLink.getDataZdbID().startsWith("ZDB-TSCRIPT-")) {
                                transcriptDBLink = dbLink;
                            }
                        }

                    }
                }


                // assign genes and hitMarker based on dblinks
                if (cloneDBLink != null) {
                    // not all clones are mapped to the clone table, so we will just assume its a marker
                    hitMarker = RepositoryFactory.getMarkerRepository().getMarkerByID(cloneDBLink.getDataZdbID());
                    genes = MarkerService.getRelatedMarker(hitMarker, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
                    hitViewBean.setHitDBLink(cloneDBLink);
                } else if (transcriptDBLink != null && false==TranscriptService.isSupportingSequence((TranscriptDBLink) transcriptDBLink)) {
                    hitMarker = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(transcriptDBLink.getDataZdbID());
                    genes = MarkerService.getRelatedMarker(hitMarker, MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
                    hitViewBean.setHitDBLink(transcriptDBLink);
                    TranscriptStatus transcriptStatus = ((Transcript) hitMarker).getStatus();
                    hitViewBean.setWithdrawn( transcriptStatus==null ? false : transcriptStatus.getStatus()==TranscriptStatus.Status.WITHDRAWN_BY_SANGER);
                } else if (geneDBLink != null) {
                    genes.add(RepositoryFactory.getMarkerRepository().getMarkerByID(geneDBLink.getDataZdbID()));
                    hitViewBean.setHitDBLink(geneDBLink);
                }

            }
            // well, then its a morpholino
            else if (hitAccessionID.startsWith("ZDB-MRPHLNO")) {
                hitMarker = RepositoryFactory.getMarkerRepository().getMarkerByID(hitAccessionID);
                if(hitMarker!=null){
                    genes = MarkerService.getRelatedMarker(hitMarker, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
                    hitViewBean.setMarkerIsHit(true);
                }
                else{
                    logger.error("blast DB has a bad morpholino: "+ hitAccessionID);
                }
            }
            // if there is no hit dblink, then use an accession
            else {
                List<Accession> accessions = RepositoryFactory.getSequenceRepository().getAccessionsByNumber(hitAccessionID);
                if (accessions.size() == 0 && hitAccessionID.startsWith("ZFIN")) {
                    logger.warn("removing unmapped ZFIN accession: " + hitAccessionID);
                    hitViewBeanIterator.remove();
                    continue hitLoop;
                } else if (accessions.size() > 0) {
                    if (accessions.size() != 1) {
                        logger.debug("accessions[" + hitAccessionID + "]does not equal 1: " + accessions.size());
                    }
                    hitViewBean.setZfinAccession(accessions.get(0));
                }
            }


            // start debugging
            if (dbLinks != null && dbLinks.size() > 1) {
                logger.debug("dblink hits is not 1: " + dbLinks.size());
                for (DBLink dbLink : dbLinks) {
                    logger.debug("dblink: " + dbLink);
                }
            } else if (dbLinks == null) {
                logger.debug("dblink is null " + hitAccessionID);
            }
            // end start debugging

            hitViewBean.setGenes(genes);

            if (genes.size() == 1) {
                GeneDataMap.getInstance().calculateExpressionForGene(genes.iterator().next(), hitViewBean);
            }

            hitViewBean.setHitMarker(hitMarker);
            //only show gbrowse images in a very special case for now, basically just for OTTDARTS
            if (hitMarker != null
                    && hitMarker.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)
                    && hitViewBean.getHitDBLink() != null
                    && hitViewBean.getHitDBLink().getReferenceDatabase().getForeignDB().getDbName().equals(ForeignDB.AvailableName.VEGA_TRANS)) {
                //todo: eventually handle multiple genes, just incase...
                Transcript transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(hitMarker.getZdbID());
                Marker gene = TranscriptService.getRelatedGenes(transcript).iterator().next().getMarker();

                //get gbrowse images
                try {
                    logger.debug("attempting to get GBrowseImage list for blast hit");
                    hitViewBean.setGbrowseImages(GBrowseService.getGBrowseTranscriptImages(gene, transcript));
                } catch (Exception e) {
                    logger.error("Couldn't get GBrowse Feature " + e.getMessage());
                }
            }

        }

        blastResultBean.setHits(hitViewBeansSorted);

        return blastResultBean;
    }



    public static Map<String, HitViewBean> getBeanMapForHits(List<Hit> hits) {
        Map<String, HitViewBean> hitViewBeans = new HashMap<String, HitViewBean>();

        // first, we process all of the hits to get accessions
        Iterator<Hit> iter = hits.iterator();
        while (iter.hasNext()) {
            Hit hit = iter.next();
            HitViewBean hitViewBean = new HitViewBean();

            hitViewBean.setId(hit.getHitId().getContent());
            // - get the hit accession ID
            String hitAccession = hit.getHitAccession().getContent();
            // now that we have the accessionID set, we can write the contains method
            if (false == hitViewBeans.containsKey(hitAccession)) {

                if (hitAccession.contains(".")) {
                    StringTokenizer tokenizer = new StringTokenizer(hitAccession, ".");
                    hitAccession = tokenizer.nextToken();
                    hitViewBean.setVersion(Integer.parseInt(tokenizer.nextToken()));
                }
                hitViewBean.setAccessionNumber(hitAccession);

                // set scores
                hitViewBean.setHitLength(Integer.parseInt(hit.getHitLen().getContent()));
                hitViewBean.setHitNumber(Integer.parseInt(hit.getHitNum().getContent()));
                hitViewBean.setNValue(hit.getHitHsps().getHsp().size());
                hitViewBean.setDefinition(hit.getHitDef().getContent());


                // handle the detailed alignments here
                List<Hsp> hsps = hit.getHitHsps().getHsp();
                List<HighScoringPair> highScoringPairs = new ArrayList<HighScoringPair>();
                double lowEValue = Double.MAX_VALUE;
                int highScore = Integer.MIN_VALUE;
                for (Hsp hsp : hsps) {
                    HighScoringPair highScoringPair = new HighScoringPair();
                    highScoringPair.setHspNumber(Integer.parseInt(hsp.getHspNum().getContent()));
                    highScoringPair.setQueryStrand(hsp.getHspQseq().getContent());
                    if(hsp.getHspMidline()!=null){
                        highScoringPair.setMidlineStrand(hsp.getHspMidline().getContent());
                    }
                    highScoringPair.setHitStrand(hsp.getHspHseq().getContent());
                    highScoringPair.setQueryFrom(Integer.parseInt(hsp.getHspQueryFrom().getContent()));
                    highScoringPair.setQueryTo(Integer.parseInt(hsp.getHspQueryTo().getContent()));
                    highScoringPair.setHitFrom(Integer.parseInt(hsp.getHspHitFrom().getContent()));
                    highScoringPair.setHitTo(Integer.parseInt(hsp.getHspHitTo().getContent()));
                    highScoringPair.setBitScore(Float.parseFloat(hsp.getHspBitScore().getContent()));
                    highScoringPair.setScore(Integer.parseInt(hsp.getHspScore().getContent()));
                    highScoringPair.setEValue(Float.parseFloat(hsp.getHspEvalue().getContent()));
                    if(hsp.getHspAlignLen()!=null){
                        highScoringPair.setAlignmentLength(Integer.parseInt(hsp.getHspAlignLen().getContent()));
                    }
                    highScoringPair.setIdentity(Integer.parseInt(hsp.getHspIdentity().getContent()));
                    if(hsp.getHspPositive()!=null){
                        highScoringPair.setPositive(Integer.parseInt(hsp.getHspPositive().getContent()));
                    }

                    // set hits as those with max hsp score
                    if (highScoringPair.getScore() > highScore) {
                        highScore = highScoringPair.getScore();
                        lowEValue = highScoringPair.getEValue();
                    }

                    highScoringPairs.add(highScoringPair);
                }
                // set hits as those with max score
                hitViewBean.setEValue(lowEValue);
                hitViewBean.setScore(highScore);

                hitViewBean.setHighScoringPairs(highScoringPairs);

                hitViewBeans.put(hitAccession, hitViewBean);
            }
        }
        return hitViewBeans;
    }

}
