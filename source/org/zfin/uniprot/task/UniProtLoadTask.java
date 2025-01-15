package org.zfin.uniprot.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.biojava.bio.BioException;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniprot.*;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;
import org.zfin.uniprot.handlers.*;
import org.zfin.uniprot.persistence.UniProtRelease;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.uniprot.UniProtFilterTask.readAllZebrafishEntriesFromSourceIntoMap;
import static org.zfin.uniprot.UniProtTools.getArgOrEnvironmentVar;
import static org.zfin.util.FileUtil.writeToFileOrZip;

/**
 * The UniProtLoadTask class loads UniProt data from a given dat file,
 * processes it using a pipeline, and then generates an output report.
 */
@Log4j2
@Getter
@Setter
public class UniProtLoadTask extends AbstractScriptWrapper {
    private static final String LOAD_REPORT_TEMPLATE_HTML = "/home/uniprot/load-report.html";
    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";
    private static final String UNIPROT_LOAD_OUTPUT_FILENAME_TEMPLATE = "/tmp/uniprot_load_%s.%s";
    private String inputFileName;
    private final String outputJsonName;
    private final String outputReportName;
    private final boolean commitChanges;
    private final String contextOutputFile;
    private final String contextInputFile;
    private UniProtLoadContext context;

    private UniProtRelease release;


    /**
     * Run the UniProtLoadTask. This will load the UniProt data from the given release file that's been downloaded
     * from UniProt. It will then process the data using a pipeline of handlers. Finally, it will generate a report
     * and a JSON file.
     *
     * It can be called from the command line with the following positional arguments:
     *  1. The input file name (if omitted or "null", the default is the latest release file from UniProt that we have in our DB table "uniprot_release")
     *  2. The output JSON file name (if omitted or "null", the default is /tmp/uniprot_load_<timestamp>.json)
     *  3. The output report file name (if omitted or "null", the default is /tmp/uniprot_load_<timestamp>.html)
     *  4. Whether to commit the changes to the database -> false = dry run (if omitted or "null", the default is false),
     *  5. The output file name to record this load's "context" (aka the current state of the DB). If omitted or "null", the default is to not record it.
     *
     *  The arguments can also be passed as environment variables:
     *  1. UNIPROT_INPUT_FILE
     *  2. UNIPROT_OUTPUT_JSON_FILE
     *  3. UNIPROT_OUTPUT_REPORT_FILE
     *  4. UNIPROT_COMMIT_CHANGES
     *  5. UNIPROT_CONTEXT_FILE
     *
     * @param args Command line arguments
     *
     */
    public static void main(String[] args) throws Exception {

        Date startTime = new Date();
        String inputFileName = getArgOrEnvironmentVar(args, 0, "UNIPROT_INPUT_FILE", "");
        String commitChanges = getArgOrEnvironmentVar(args, 1, "UNIPROT_COMMIT_CHANGES", "false");
        String outputJsonName = getArgOrEnvironmentVar(args, 2, "UNIPROT_OUTPUT_JSON_FILE", calculateDefaultOutputFileName(startTime, "json"));
        String outputReportName = getArgOrEnvironmentVar(args, 3, "UNIPROT_OUTPUT_REPORT_FILE", calculateDefaultOutputFileName(startTime, "report.html"));
        String contextOutputFile = getArgOrEnvironmentVar(args, 4, "UNIPROT_CONTEXT_FILE", "");
        String contextInputFile = getArgOrEnvironmentVar(args, 5, "UNIPROT_CONTEXT_INPUT_FILE", "");

        UniProtLoadTask task = new UniProtLoadTask(inputFileName, outputJsonName, outputReportName, "true".equals(commitChanges), contextOutputFile, contextInputFile);
        task.runTask();
    }

    private static Optional<UniProtRelease> getLatestUnprocessedUniProtRelease() {
        return Optional.ofNullable(getInfrastructureRepository().getLatestUnprocessedUniProtRelease());
    }

    public UniProtLoadTask(String inputFileName, String outputJsonName, String outputReportName, boolean commitChanges, String contextOutputFile, String contextInputFile) {
        this.inputFileName = inputFileName;
        this.outputJsonName = outputJsonName;
        this.outputReportName = outputReportName;
        this.commitChanges = commitChanges;
        this.contextOutputFile = contextOutputFile;
        this.contextInputFile = contextInputFile;
    }

    public void runTask() throws IOException, BioException, SQLException {
        initialize();
        log.info("Starting UniProtLoadTask for file " + inputFileName + " with output files " + outputJsonName + " and " + outputReportName + ".");
        log.info("Commit changes: " + commitChanges + ".");

        try (BufferedReader inputFileReader = new BufferedReader(new java.io.FileReader(inputFileName))) {
            Map<String, RichSequenceAdapter> entries = readUniProtEntries(inputFileReader);
            Set<UniProtLoadAction> actions = executePipeline(entries);
            loadChangesIfNotDryRun(actions);
            List<UniProtLoadSummaryItemDTO> summary = calculateSummary(actions);
            writeOutputReportFile(actions, summary, entries);
        }
    }

    private List<UniProtLoadSummaryItemDTO> calculateSummary(Set<UniProtLoadAction> actions) {
        int preExistingUniprotLinksCount = context.getUniprotDbLinks().size();
        int newUniprotLinksCount = actions.stream().filter(a -> a.getType().equals(UniProtLoadAction.Type.LOAD)).toList().size();
        int deletedUniprotLinksCount = actions.stream().filter(a -> a.getType().equals(UniProtLoadAction.Type.DELETE)).toList().size();
        int netIncrease = newUniprotLinksCount - deletedUniprotLinksCount;
        int postExistingUniprotLinks = preExistingUniprotLinksCount + netIncrease;
        UniProtLoadSummaryItemDTO row1 = new UniProtLoadSummaryItemDTO("db_link records", Long.valueOf(preExistingUniprotLinksCount), Long.valueOf(postExistingUniprotLinks));

        int preGenesWithDBLinksCount = context.getUniprotDbLinksByGeneID().size();
        int genesLosingAllUniprotAccessions = actions.stream().filter(a -> a.getType().equals(UniProtLoadAction.Type.INFO) && a.getSubType().equals(UniProtLoadAction.SubType.GENE_LOST_ALL_UNIPROTS)).toList().size();
        int genesGainFirstUniprotAccession = actions.stream().filter(a -> a.getType().equals(UniProtLoadAction.Type.INFO) && a.getSubType().equals(UniProtLoadAction.SubType.GENE_GAINS_FIRST_UNIPROT)).toList().size();
        int postGenesWithDBLinksCount = preGenesWithDBLinksCount - genesLosingAllUniprotAccessions + genesGainFirstUniprotAccession;
        UniProtLoadSummaryItemDTO row2 = new UniProtLoadSummaryItemDTO("genes with uniprot links (see INFO for details)", Long.valueOf(preGenesWithDBLinksCount), Long.valueOf(postGenesWithDBLinksCount));
        return List.of(row1, row2);
    }

    private void loadChangesIfNotDryRun(Set<UniProtLoadAction> actions) {
        if (commitChanges) {
            log.info("Loading changes into database.");
            loadChanges(actions);
        } else {
            log.info("Dry run, not loading changes into database.");
        }
    }

    private void loadChanges(Set<UniProtLoadAction> actions) {
        UniProtLoadService.processActions(actions, release);
    }

    public void initialize() {
        initAll();
        setInputFileName();
        calculateContext();
        writeContext();
    }

    private void writeContext() {
        if (contextOutputFile != null && !contextOutputFile.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                log.info("Writing context file: " + contextOutputFile + ".");
                writeToFileOrZip(new File(contextOutputFile), objectMapper.writeValueAsString(context), "UTF-8");
            } catch (IOException e) {
                log.error("Error writing context file " + contextOutputFile + ": " + e.getMessage(), e);
            }
        }
    }

    private void setInputFileName() {
        Optional<UniProtRelease> latestUnprocessedUniProtRelease = getLatestUnprocessedUniProtRelease();
        if (inputFileName.isEmpty() && latestUnprocessedUniProtRelease.isPresent()) {

            //if the latest unprocessed release is older than a processed release, throw an exception
            UniProtRelease latestProcessed = getInfrastructureRepository().getLatestProcessedUniProtRelease();
            if (latestProcessed != null && latestProcessed.getDownloadDate().after(latestUnprocessedUniProtRelease.get().getDownloadDate())) {
                throw new RuntimeException("The latest unprocessed UniProt release (from " + latestUnprocessedUniProtRelease.get().getDownloadDate() + ") is older than the latest processed UniProt release (from " + latestProcessed.getDownloadDate() + ").");
            }

            log.info("No input file specified, using latest unprocessed UniProt release: " + latestUnprocessedUniProtRelease.get().getLocalFile().getAbsolutePath() + ".");
            inputFileName = latestUnprocessedUniProtRelease.get().getLocalFile().getAbsolutePath();
            release = latestUnprocessedUniProtRelease.get();
            if (!new File(inputFileName).exists() || new File(inputFileName).length() == 0) {
                throw new RuntimeException("No such file (or empty): " + inputFileName);
            }
        } else if (inputFileName.isEmpty()) {
            throw new RuntimeException("No input file specified and no unprocessed UniProt release found.");
        }
    }

    public Map<String, RichSequenceAdapter> readUniProtEntries(BufferedReader inputFileReader) throws BioException, IOException {
        Map<String, RichSequenceAdapter> entries = readAllZebrafishEntriesFromSourceIntoMap(inputFileReader);
        log.info("Finished reading file: " + entries.size() + " entries read.");
        return entries;
    }

    public Set<UniProtLoadAction> executePipeline(Map<String, RichSequenceAdapter> entries) {
        // data entry pipeline
        UniProtLoadPipeline pipeline = new UniProtLoadPipeline();
        pipeline.setContext(context);
        pipeline.setUniProtRecords(entries);
        pipeline.addHandler(new RemoveVersionHandler());
        pipeline.addHandler(new IgnoreSpecificAccessionsHandler());
        pipeline.addHandler(new DeleteAccessionsHandler());
        pipeline.addHandler(new MatchOnRefSeqIgnoreExistingHandler());
        pipeline.addHandler(new RemoveIgnoreActionsHandler());
        pipeline.addHandler(new ReportLegacyProblemFilesHandler());
        pipeline.addHandler(new FlagPotentialIssuesHandler());
        pipeline.addHandler(new CheckLostUniprotsForObsoletesHandler());

        return pipeline.execute();
    }

    private void writeOutputReportFile(Set<UniProtLoadAction> actions, List<UniProtLoadSummaryItemDTO> summary, Map<String, RichSequenceAdapter> uniprotRecords) {
        String reportFile = this.outputReportName;
        String jsonFile = this.outputJsonName;

        log.info("Creating report file: " + reportFile);
        try {
            UniProtLoadActionsContainer actionsContainer = UniProtLoadActionsContainer.builder()
                    .actions(actions)
                    .summary(summary)
                    .uniprotDatFile(buildDatFileMap(actions, uniprotRecords))
                    .build();
            String jsonContents = actionsToJson(actionsContainer);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + LOAD_REPORT_TEMPLATE_HTML;
            String templateContents = FileUtils.readFileToString(new File(template), "UTF-8");
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonContents);
            writeToFileOrZip(new File(reportFile), filledTemplate, "UTF-8");
            writeToFileOrZip(new File(jsonFile), jsonContents, "UTF-8");
            log.info("Finished creating report file: " + reportFile + " and json file: " + jsonFile);
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ") from template\n" + e.getMessage(), e);
        }
    }

    private String actionsToJson(UniProtLoadActionsContainer actions) throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(actions);
    }

    private void calculateContext() {
        if (contextInputFile != null && !contextInputFile.isEmpty() && new File(contextInputFile).exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                log.info("Reading context file: " + contextInputFile + ".");
                context = objectMapper.readValue(new File(contextInputFile), UniProtLoadContext.class);
            } catch (IOException e) {
                log.error("Error reading context file " + contextInputFile + ": " + e.getMessage(), e);
            }
        } else {
            context = UniProtLoadContext.createFromDBConnection();
        }
    }

    private static String calculateDefaultOutputFileName(Date startTime, String extension) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(startTime);
        return String.format(UNIPROT_LOAD_OUTPUT_FILENAME_TEMPLATE, timestamp, extension);
    }

    private Map<String, String> buildDatFileMap(Set<UniProtLoadAction> actions, Map<String, RichSequenceAdapter> uniprotRecords) {
        //only include the entries from the dat file that are actually being acted upon
        Map<String, String> datFileMap = new HashMap<>();
        actions.stream().map(UniProtLoadAction::getAccession).forEach(accession -> {
            RichSequenceAdapter adapter = uniprotRecords.get(accession);
            if (adapter != null) {
                datFileMap.put(accession, adapter.toUniProtFormat());
            }
        });
        return datFileMap;
    }


}
