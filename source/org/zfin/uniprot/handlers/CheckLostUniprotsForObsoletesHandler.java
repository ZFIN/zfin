package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.datatransfer.webservice.NCBIRefSeqFetch;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This handler goes over all the actions that were created to delete uniprot records from our database.
 * For each of those deletes, it cross-references data available from NCBI to see if the UniProt record
 * contains any RefSeq records that have been obsoleted and replaced with a new RefSeq.  If that's the case,
 * and if ZFIN has the newer RefSeq, these delete actions should be flagged as expected to be re-added later.
 *
 * Optional environment variables:
 *   NCBI_FETCH_CACHE_INPUT_FILE=/path/to/somefile.json:
 *   If set, input cache will be used instead of fetching from NCBI api
 *
 * NCBI_FETCH_CACHE_OUTPUT_FILE=/path/to/some-other-file.json
 *   If set, the output file will be written after fetching from NCBI (for use in future runs as input file)
 *
 */
@Log4j2
public class CheckLostUniprotsForObsoletesHandler implements UniProtLoadHandler {

    private File cacheFile = null;
    private Map<String, NCBIRefSeqFetch.NCBIRefSeqData> ncbiRefSeqDataMap;

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        initialize();
        log.debug("Check Lost Uniprot for obsoletes");

        List<UniProtLoadAction> deletes = actions.stream().filter(a -> UniProtLoadAction.Type.DELETE.equals(a.getType())).toList();
        initializeNcbiRefseqData(uniProtRecords, deletes);

        //go through each of the deletes and check if they have refseqs that are replacements for obsoleted ones
        for(UniProtLoadAction action : deletes) {
            checkAndUpdateAction(action, uniProtRecords, context);
        }
    }

    private void initialize() {
        String inputFile = System.getenv("NCBI_FETCH_CACHE_INPUT_FILE");
        if (inputFile != null) {
            File tempCacheFile = new File(inputFile);
            if (tempCacheFile.exists()) {
                cacheFile = tempCacheFile;
            }
        }
    }

    private void checkAndUpdateAction(UniProtLoadAction action, Map<String, RichSequenceAdapter> uniProtRecords, UniProtLoadContext context) {
        RichSequenceAdapter uniprotData = uniProtRecords.get(action.getAccession());
        if (uniprotData == null) {
            log.debug("No uniprot record for accession " + action.getAccession());
            return;
        }
        Set<String> refseqs = uniprotData.getRefSeqsWithoutVersion();
        for (String refseq : refseqs) {
            NCBIRefSeqFetch.NCBIRefSeqData ncbiData = ncbiRefSeqDataMap.get(refseq);
            if (ncbiData == null) {
                log.debug("RefSeq data null: " + refseq);
                continue;
            }
            String replacement = ncbiData.replacedby();
            if (replacement != null) {
                StringBuilder sb = new StringBuilder();
                List<DBLinkSlimDTO> foundReplacementInZfin = context.getRefseqDbLinks().get(replacement);
                List<DBLinkSlimDTO> foundOldRefSeqInZfin = context.getRefseqDbLinks().get(refseq);

                sb.append("Found replacement for " + refseq + ": " + replacement + "\n");
                if (foundReplacementInZfin != null) {
                    sb.append("The replacement exists in ZFIN: " + replacement + " -> " + foundReplacementInZfin.stream().map(DBLinkSlimDTO::getDataZdbID).collect(Collectors.joining(", ")));
                }
                if (foundOldRefSeqInZfin != null) {
                    sb.append("The old refseq exists in ZFIN: " + refseq + " -> " + foundOldRefSeqInZfin.stream().map(DBLinkSlimDTO::getDataZdbID).collect(Collectors.joining(", ")));
                }
                String details = sb.toString();
                action.addTag(UniProtLoadAction.CategoryTag.REPLACED_REFSEQ);
                action.addDetails(details);

                //default behavior is to not delete a uniprot/gene dblink if the refseq mismatch is due to the refseq getting a replacement
                //we expect the difference to reconcile itself over time
                if ("true".equals(System.getenv("UNIPROT_DELETE_REPLACED_REFSEQ_FLAG"))) {
                    log.info("Deleting UniProt accession despite replaced refseq: " + action.getGeneZdbID() + "/" + action.getAccession() + "/" + refseq);
                } else {
                    log.info("Changing action type from DELETE to WARNING due to replaced refseq: " + action.getGeneZdbID() + "/" + action.getAccession());
                    action.setType(UniProtLoadAction.Type.WARNING);
                    action.addDetails("\nTo DELETE this uniprot accession, re-run uniprot load with flag 'UNIPROT_DELETE_REPLACED_REFSEQ_FLAG' set to 'true'.");
                }

                log.debug(details);
            }
            if ("suppressed".equals(ncbiData.status())) {
                log.debug("suppressed refseq: " + refseq);
                action.addTag(UniProtLoadAction.CategoryTag.SUPPRESSED_REFSEQ);
            }
        }
    }

    private void initializeNcbiRefseqData(Map<String, RichSequenceAdapter> uniProtRecords, List<UniProtLoadAction> deletes) {
        //let's get the list of all lost uniprot accessions that exist in the dat file
        List<String> losingAccessions = deletes.stream().map(UniProtLoadAction::getAccession).toList();
        Set<String> accessionsExistingInUniprotFile = uniProtRecords.keySet();
        Collection<String> overlap = CollectionUtils.intersection(losingAccessions, accessionsExistingInUniprotFile);

        Map<String, Set<String>> refseqsInUniprotByAccession = uniProtRecords
                .entrySet()
                .stream()
                .filter(e -> overlap.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getRefSeqsWithoutVersion()));

        List<String> refseqs = refseqsInUniprotByAccession.values().stream().flatMap(Collection::stream).toList();

        NCBIRefSeqFetch fetcher = new NCBIRefSeqFetch();
        if (cacheFile != null) {
            fetcher.setCacheFile(cacheFile);
        }
        Map<String, NCBIRefSeqFetch.NCBIRefSeqData> finalResults = fetcher.fetchRefSeqsByID(refseqs);
        this.ncbiRefSeqDataMap = finalResults;
        this.writeCache(finalResults);
    }

    private void writeCache(Map<String, NCBIRefSeqFetch.NCBIRefSeqData> ncbiRefSeqData) {
        String outputFile = System.getenv("NCBI_FETCH_CACHE_OUTPUT_FILE");
        if (outputFile != null) {
            File tempCacheFile = new File(outputFile);
            try {
                NCBIRefSeqFetch.writeCache(tempCacheFile, ncbiRefSeqData);
            } catch (Exception e) {
                log.error("Failed to write cache to file " + tempCacheFile.getAbsolutePath());
                log.error(e);
            }
        }
    }

}
