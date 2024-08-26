package org.zfin.sequence.load;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.TranscriptDBLink;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.sequence.load.LoadAction.SubType.*;

@Log4j2
public class EnsemblTranscriptLengthUpdate extends EnsemblTranscriptBase {

    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";
    public static final String HTTPS_WWW_ENSEMBL_ORG_DANIO_RERIO_GENE_SUMMARY_G = "https://www.ensembl.org/Danio_rerio/Gene/Summary?g=";

    public static void main(String[] args) throws IOException {
        AbstractScriptWrapper wrapper = new AbstractScriptWrapper();
        wrapper.initAll();

        EnsemblTranscriptLengthUpdate loader = new EnsemblTranscriptLengthUpdate();
        loader.init();
    }

    private Map<String, List<RichSequence>> allEnsemblProvidedGeneMap;

    Map<Marker, List<TranscriptDBLink>> geneEnsdartMap;

    public void init() throws IOException {
        downloadFile(cdnaFileName);
        loadSequenceMapFromDownloadFile();
        System.exit(0);
    }

    private void loadSequenceMapFromDownloadFile() {

        EnsemblLoadSummaryItemDTO dto = new EnsemblLoadSummaryItemDTO();

        // <ensdargID, List<RichSequence>>
        Map<String, List<RichSequence>> geneTranscriptMap = getGeneTranscriptMap(cdnaFileName);
        dto.getCounts().put("ensemblGeneCount", (long) geneTranscriptMap.size());
        dto.getCounts().put("zfinEnsemblGeneCount", (long) getMarkerDbLinks().size());
        Set<RichSequence> transcriptSet = new HashSet<>(geneTranscriptMap.values().stream().flatMap(Collection::stream).toList());
        dto.getCounts().put("ensemblTranscriptCount", (long) transcriptSet.size());

        Set<LoadAction> actions = new HashSet<>();
        List<String> zfinGeneAccessionIDs = getMarkerDbLinks().stream().map(DBLink::getAccessionNumber).distinct().toList();
        List<String> ensemblGeneAccessionIDs = geneTranscriptMap.keySet().stream().map(EnsemblTranscriptBase::getUnversionedAccession).toList();
        CollectionUtils.removeAll(ensemblGeneAccessionIDs, zfinGeneAccessionIDs).forEach(missingID -> {
            LoadLink missingTranscript = new LoadLink(missingID, "https://www.ensembl.org/Danio_rerio/Gene/Summary?g=" + missingID);
            LoadAction missingAction = new LoadAction(LoadAction.Type.INFO, ENSDARG_MISSING, missingID, "", "This ENSDARG currently is not loaded into ZFIN", 0, new TreeSet<>());
            missingAction.addLink(missingTranscript);
            actions.add(missingAction);
        });
        Map<String, List<MarkerDBLink>> map = getMarkerDbLinks().stream().collect(Collectors.groupingBy(DBLink::getAccessionNumber));
        CollectionUtils.removeAll(zfinGeneAccessionIDs, ensemblGeneAccessionIDs).forEach(missingID -> {
            List<String> zdbIDs = map.get(missingID).stream().map(markerDBLink -> markerDBLink.getMarker().getZdbID()).toList();
            String idConcat = missingID + " - " + StringUtils.join(zdbIDs, ",");
            LoadAction obsoletedAction = new LoadAction(LoadAction.Type.INFO, ZFIN_OBSOLETE, missingID, idConcat, "This ENSDARG ID is not found at ENSEMBL", 0, new TreeSet<>());
            zdbIDs.forEach(zdbID -> {
                LoadLink link = new LoadLink(idConcat, "https://zfin.org/" + zdbID);
                obsoletedAction.addLink(link);
            });
            actions.add(obsoletedAction);
        });


        Map<String, List<RichSequence>> sortedGeneTranscriptMap = geneTranscriptMap.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // remove version number from accession ID
        allEnsemblProvidedGeneMap = new LinkedHashMap<>();
        sortedGeneTranscriptMap.forEach((versionedAccession, richSequences) -> allEnsemblProvidedGeneMap.put(getUnversionedAccession(versionedAccession), richSequences));

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
            LoadAction missingAction = new LoadAction(LoadAction.Type.INFO, ENSDART_MISSING, missingID, "", "This ENSDART currently is not loaded into ZFIN", 0, new TreeSet<>());
            missingAction.addLink(missingTranscript);
            actions.add(missingAction);
        });

        Map<String, List<TranscriptDBLink>> tmap = ensemblTranscripts.stream().collect(Collectors.groupingBy(DBLink::getAccessionNumber));
        CollectionUtils.removeAll(zfinEnsemblTranscriptIDs, ensemblTranscriptIDs).forEach(missingID -> {
            List<String> zdbIDs = tmap.get(missingID).stream().map(transcriptDBLink -> transcriptDBLink.getTranscript().getZdbID()).toList();
            String idConcat = missingID + " - " + StringUtils.join(zdbIDs, ",");
            LoadAction obsoletedAction = new LoadAction(LoadAction.Type.INFO, ZFIN_TRANSCRIPT_OBSOLETE, missingID, idConcat, "This ENSDART ID is not found at ENSEMBL", 0, new TreeSet<>());
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
            System.out.println("Adjusted Tx: " + modifiedTx);

        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            System.out.println(e.getMessage());
        }

        dto.setDescription("Loading Ensembl Transcripts into ZFIN");
        LoadActionsContainer container = new LoadActionsContainer(dto, actions);
        writeOutputReportFile(actions, dto);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(container);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println(json);
        System.exit(0);
    }

    private static void createUpdateNullLengthActions(Set<LoadAction> actions, TranscriptDBLink link) {
        UpdateLengthLoadAction updateAction = new UpdateLengthLoadAction(LoadAction.Type.UPDATE, UPDATE_LENGTH_NULL, new TreeSet<>(), link);
        actions.add(updateAction);
    }

    private static void createUpdateNotNullLengthActions(Set<LoadAction> actions, TranscriptDBLink link, int oldValue) {
        UpdateLengthLoadAction updateAction = new UpdateLengthLoadAction(LoadAction.Type.UPDATE, UPDATE_LENGTH_NON_NULL, new TreeSet<>(), link, oldValue);
        actions.add(updateAction);
    }


    private String actionsToJson(LoadActionsContainer actions) throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(actions);
    }

    private void writeOutputReportFile(Set<LoadAction> actions, EnsemblLoadSummaryItemDTO summary) {
        String reportFile = "ensembl-transcript-load-report.html";
        String jsonFile = "ensemb-transcript-output.json";

        log.info("Creating report file: " + reportFile);
        try {
            LoadActionsContainer actionsContainer = LoadActionsContainer.builder()
                .actions(actions)
                .summary(summary)
                .build();
            String jsonContents = actionsToJson(actionsContainer);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + "/ensembl-transcript-report-template.html";
            String templateContents = FileUtils.readFileToString(new File(template), "UTF-8");
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonContents);
            FileUtils.writeStringToFile(new File(reportFile), filledTemplate, "UTF-8");
            FileUtils.writeStringToFile(new File(jsonFile), jsonContents, "UTF-8");
            log.info("Finished creating report file: " + reportFile + " and json file: " + jsonFile);
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ") from template\n" + e.getMessage(), e);
        }
    }

}

