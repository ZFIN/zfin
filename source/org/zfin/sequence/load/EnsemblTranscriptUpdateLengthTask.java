package org.zfin.sequence.load;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.TranscriptDBLink;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.sequence.load.LoadAction.SubType.*;

@Log4j2
public class EnsemblTranscriptUpdateLengthTask extends EnsemblTranscriptBase {

    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";
    public static final String HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G = "https://www.ensembl.org/Danio_rerio/Gene/Summary?g=";

    public static final String REPORT_HOME_DIRECTORY = "/home/ensembl/";

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        System.out.println("Start Length Update...");
        EnsemblTranscriptUpdateLengthTask loader = new EnsemblTranscriptUpdateLengthTask();
        loader.init();
        System.out.println("Finish Length Update");

        System.out.println("Start Load...");
        EnsemblTranscriptFastaReadProcess loadTranscripts = new EnsemblTranscriptFastaReadProcess();
        loadTranscripts.init();
        System.out.println("Finish Load");
        loader.actions.addAll(loadTranscripts.actions);
        loader.writeOutputReportFile();
    }

    Map<Marker, List<TranscriptDBLink>> geneEnsdartMap;

    public void init() throws IOException {
        super.init();
        loadSequenceMapFromDownloadFile();
    }

    private void loadSequenceMapFromDownloadFile() {
        dto = getEnsemblLoadSummaryItemDTO();
        List<String> zfinGeneAccessionIDs = getMarkerDbLinks().stream().map(DBLink::getAccessionNumber).distinct().toList();
        List<String> ensemblGeneAccessionIDs = geneTranscriptMap.keySet().stream().map(EnsemblTranscriptBase::getUnversionedAccession).toList();
        CollectionUtils.removeAll(ensemblGeneAccessionIDs, zfinGeneAccessionIDs).forEach(missingID -> {
            LoadLink missingTranscript = new LoadLink(missingID, "https://www.ensembl.org/Danio_rerio/Gene/Summary?g=" + missingID);
            LoadAction missingAction = new LoadAction(LoadAction.Type.INFO, ENSDARG_MISSING, missingID, "", "This ENSDARG currently is not loaded into ZFIN", 0, new HashMap<>());
            missingAction.addLink(missingTranscript);
            actions.add(missingAction);
        });
        Map<String, List<MarkerDBLink>> map = getMarkerDbLinks().stream().collect(Collectors.groupingBy(DBLink::getAccessionNumber));
        CollectionUtils.removeAll(zfinGeneAccessionIDs, ensemblGeneAccessionIDs).forEach(missingID -> {
            List<String> zdbIDs = map.get(missingID).stream().map(markerDBLink -> markerDBLink.getMarker().getZdbID()).toList();
            String idConcat = missingID + " - " + StringUtils.join(zdbIDs, ",");
            LoadAction obsoletedAction = new LoadAction(LoadAction.Type.INFO, ZFIN_ENSDARG_NOT_TRANSCRIPT_FILE, missingID, idConcat, "This ENSDARG ID is not found in the ENSEMBL transcript file", 0, new HashMap<>());
            zdbIDs.forEach(zdbID -> {
                LoadLink link = new LoadLink(idConcat, "https://zfin.org/" + zdbID);
                obsoletedAction.addLink(link);
            });
            if (zdbIDs.size() > 1) {
                obsoletedAction.setType(LoadAction.Type.WARNING);
                obsoletedAction.setSubType(ZFIN_ENSDARG_NOT_TRANSCRIPT_FILE_MULTIPLE);
            }
            actions.add(obsoletedAction);
        });


        Map<String, List<RichSequence>> sortedGeneTranscriptMap = geneTranscriptMap.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // remove version number from accession ID
        geneEnsdartMap = getSequenceRepository().getAllRelevantEnsemblTranscripts();
        List<TranscriptDBLink> ensemblTranscripts = geneEnsdartMap.values().stream().flatMap(Collection::stream).toList();
        List<RichSequence> richSequences = sortedGeneTranscriptMap.values().stream().flatMap(Collection::stream).toList();
        int txDifference = richSequences.size() - ensemblTranscripts.size();
        dto.getCounts().put("zfinEnsemblTranscriptCount", (long) ensemblTranscripts.size());

        Map<String, TranscriptDBLink> transcriptMap = ensemblTranscripts.stream().collect(Collectors.toMap(DBLink::getAccessionNumber, o -> o,
            (db1, db2) -> db1
        ));
        System.out.println("Total Number of Ensembl transcripts in FASTA file: " + richSequences.size());
        System.out.println("Total Number of Ensembl transcripts in ZFIN: " + ensemblTranscripts.size());

        List<String> ensemblTranscriptIDs = richSequences.stream().map(richSequence -> getUnversionedAccession(richSequence.getAccession())).toList();
        List<String> zfinEnsemblTranscriptIDs = ensemblTranscripts.stream().map(DBLink::getAccessionNumber).toList();
        CollectionUtils.removeAll(ensemblTranscriptIDs, zfinEnsemblTranscriptIDs).forEach(missingID -> {
            LoadLink missingTranscript = new LoadLink(missingID, HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G + missingID);
            LoadAction missingAction = new LoadAction(LoadAction.Type.INFO, ENSDART_MISSING, missingID, "", "This ENSDART currently is not loaded into ZFIN", 0, new HashMap<>());
            missingAction.addLink(missingTranscript);
            actions.add(missingAction);
        });

        Map<String, List<TranscriptDBLink>> tmap = ensemblTranscripts.stream().collect(Collectors.groupingBy(DBLink::getAccessionNumber));
        CollectionUtils.removeAll(zfinEnsemblTranscriptIDs, ensemblTranscriptIDs).forEach(missingID -> {
            List<String> zdbIDs = tmap.get(missingID).stream().map(transcriptDBLink -> transcriptDBLink.getTranscript().getZdbID()).toList();
            String idConcat = StringUtils.join(zdbIDs, ",");
            LoadAction obsoletedAction = new LoadAction(LoadAction.Type.INFO, ZFIN_ENSDART_NOT_TRANSCRIPT_FILE, missingID, idConcat, "This ENSDART ID is not found at ENSEMBL", 0, new HashMap<>());
            if (zdbIDs.size() > 1) {
                obsoletedAction.setType(LoadAction.Type.WARNING);
            }
            zdbIDs.forEach(zdbID -> {
                LoadLink link = new LoadLink(idConcat, "https://zfin.org/" + zdbID);
                obsoletedAction.addLink(link);
            });
            actions.add(obsoletedAction);
        });
        dto.getCounts().put("Number of Ensembl Transcripts not in ZFIN", (long) actions.size());
        System.out.println("Difference:" + txDifference + " [" + 100 * txDifference / richSequences.size() + "%]");

        LoadAction newAction = new LoadAction();
        newAction.setType(LoadAction.Type.LOAD);

        HibernateUtil.createTransaction();
        try {
            List<TranscriptDBLink> modifiedTx = new ArrayList<>();
            AtomicInteger numberOfNonNullLengthAltered = new AtomicInteger();
            AtomicInteger numberOfNullLengthAdded = new AtomicInteger();
            richSequences.forEach(richSequence -> {
                String cleanedID = getString(richSequence);
                int length = richSequence.length();
                TranscriptDBLink link = transcriptMap.get(cleanedID);
                if (link == null) {
                    return;
                }
                if (link.getLength() == null) {
                    numberOfNullLengthAdded.incrementAndGet();
                    link.setLength(length);
                    createUpdateNullLengthActions(actions, link);
                }
                if (link.getLength() != length) {
                    numberOfNonNullLengthAltered.incrementAndGet();
                    modifiedTx.add(link);
                    int oldLength = link.getLength();
                    link.setLength(length);
                    createUpdateNotNullLengthActions(actions, link, oldLength);
                }
            });
            HibernateUtil.flushAndCommitCurrentSession();
            System.out.println("Number of ZFIN transcripts in ZFIN with no length: " + numberOfNullLengthAdded);
            System.out.println("Number of ZFIN transcripts in ZFIN with length adjusted: " + numberOfNonNullLengthAltered);

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            System.out.println(e.getMessage());
        }

        dto.setDescription("Loading Ensembl Transcripts into ZFIN");
    }

    private static void createUpdateNullLengthActions(Set<LoadAction> actions, TranscriptDBLink link) {
        UpdateLengthLoadAction updateAction = new UpdateLengthLoadAction(LoadAction.Type.UPDATE, UPDATE_LENGTH_NULL, new HashMap<>(), link);
        actions.add(updateAction);
    }

    private static void createUpdateNotNullLengthActions(Set<LoadAction> actions, TranscriptDBLink link, int oldValue) {
        UpdateLengthLoadAction updateAction = new UpdateLengthLoadAction(LoadAction.Type.UPDATE, UPDATE_LENGTH_NON_NULL, new HashMap<>(), link, oldValue);
        actions.add(updateAction);
    }


}

