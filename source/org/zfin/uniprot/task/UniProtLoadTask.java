package org.zfin.uniprot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.biojava.bio.BioException;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.report.LegacyReportAdapter;
import org.zfin.report.ReportWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private static final String UNIPROT_LOAD_OUTPUT_FILENAME_TEMPLATE = "/tmp/uniprot_load_%s.%s";
    private String inputFileName;
    private final String outputReportName;
    private final boolean commitChanges;
    private final boolean rerunLatestProcessed;
    private final String contextOutputFile;
    private final String contextInputFile;
    private UniProtLoadContext context;

    private UniProtRelease release;


    /**
     * Run the UniProtLoadTask. This will load the UniProt data from the given release file that's been downloaded
     * from UniProt. It will then process the data using a pipeline of handlers. Finally, it will generate an HTML
     * report through the unified viewer (source/org/zfin/report/report-template.html).
     *
     * It can be called from the command line with the following positional arguments:
     *  1. The input file name (if omitted or "null", the default is the latest release file from UniProt that we have in our DB table "uniprot_release")
     *  2. Whether to commit the changes to the database -> false = dry run (if omitted or "null", the default is false),
     *  3. The output report file name (if omitted or "null", the default is /tmp/uniprot_load_&lt;timestamp&gt;.report.html)
     *  4. The output file name to record this load's "context" (aka the current state of the DB). If omitted or "null", the default is to not record it.
     *  5. The context input file (read instead of querying the DB). If omitted or "null", the default is to query the DB.
     *  6. Whether to re-run the load against the latest processed release file when there is no unprocessed release (if omitted, default false).
     *     Used by the monthly auto-run job to re-integrate DB-side changes against the same UniProt release file when no new release is available.
     *
     *  The arguments can also be passed as environment variables:
     *  1. UNIPROT_INPUT_FILE
     *  2. UNIPROT_COMMIT_CHANGES
     *  3. UNIPROT_OUTPUT_REPORT_FILE
     *  4. UNIPROT_CONTEXT_FILE
     *  5. UNIPROT_CONTEXT_INPUT_FILE
     *  6. UNIPROT_RERUN_LATEST_PROCESSED
     *
     * @param args Command line arguments
     *
     */
    public static void main(String[] args) throws Exception {

        log.info("UniProtLoadTask starting...");

        Date startTime = new Date();
        String inputFileName = getArgOrEnvironmentVar(args, 0, "UNIPROT_INPUT_FILE", "");
        String commitChanges = getArgOrEnvironmentVar(args, 1, "UNIPROT_COMMIT_CHANGES", "false");
        String outputReportName = getArgOrEnvironmentVar(args, 2, "UNIPROT_OUTPUT_REPORT_FILE", calculateDefaultOutputFileName(startTime, "report.html"));
        String contextOutputFile = getArgOrEnvironmentVar(args, 3, "UNIPROT_CONTEXT_FILE", "");
        String contextInputFile = getArgOrEnvironmentVar(args, 4, "UNIPROT_CONTEXT_INPUT_FILE", "");
        String rerunLatestProcessed = getArgOrEnvironmentVar(args, 5, "UNIPROT_RERUN_LATEST_PROCESSED", "false");

        UniProtLoadTask task = new UniProtLoadTask(inputFileName, outputReportName, "true".equals(commitChanges), contextOutputFile, contextInputFile, "true".equals(rerunLatestProcessed));
        task.runTask();
    }

    private static Optional<UniProtRelease> getLatestUnprocessedUniProtRelease() {
        return Optional.ofNullable(getInfrastructureRepository().getLatestUnprocessedUniProtRelease());
    }

    public UniProtLoadTask(String inputFileName, String outputReportName, boolean commitChanges, String contextOutputFile, String contextInputFile) {
        this(inputFileName, outputReportName, commitChanges, contextOutputFile, contextInputFile, false);
    }

    public UniProtLoadTask(String inputFileName, String outputReportName, boolean commitChanges, String contextOutputFile, String contextInputFile, boolean rerunLatestProcessed) {
        this.inputFileName = inputFileName;
        this.outputReportName = outputReportName;
        this.commitChanges = commitChanges;
        this.contextOutputFile = contextOutputFile;
        this.contextInputFile = contextInputFile;
        this.rerunLatestProcessed = rerunLatestProcessed;
    }

    public void runTask() throws IOException, BioException, SQLException {
        initialize();
        log.info("Starting UniProtLoadTask for file " + inputFileName + " with report file " + outputReportName + ".");
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
            log.info("Dry run, not loading changes into database. (set UNIPROT_COMMIT_CHANGES environment variable to 'true' to load changes).");
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
        if (!inputFileName.isEmpty()) {
            return;
        }

        // rerunLatestProcessed is authoritative: a synchronizing re-run (e.g. the scheduled
        // monthly catch-up load) must run against the most recent ALREADY-PROCESSED release and
        // ignore any pending new release. Pulling in a new UniProt release is a deliberate,
        // manual action (run without UNIPROT_RERUN_LATEST_PROCESSED). This is checked before the
        // unprocessed-release branch so a pending release never silently gets auto-loaded here.
        if (rerunLatestProcessed) {
            UniProtRelease latestProcessed = getInfrastructureRepository().getLatestProcessedUniProtRelease();
            if (latestProcessed == null) {
                throw new RuntimeException("No input file specified, and no processed UniProt release to re-run against.");
            }
            log.info("Re-running against latest processed UniProt release: " + latestProcessed.getLocalFile().getAbsolutePath() + ".");
            inputFileName = latestProcessed.getLocalFile().getAbsolutePath();
            release = latestProcessed;
            if (!new File(inputFileName).exists() || new File(inputFileName).length() == 0) {
                throw new RuntimeException("No such file (or empty): " + inputFileName);
            }
            return;
        }

        Optional<UniProtRelease> latestUnprocessedUniProtRelease = getLatestUnprocessedUniProtRelease();
        if (latestUnprocessedUniProtRelease.isPresent()) {

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
        } else {
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
        log.info("Creating report file: " + reportFile);
        try {
            UniProtLoadActionsContainer container = UniProtLoadActionsContainer.builder()
                    .actions(actions)
                    .summary(summary)
                    .uniprotDatFile(buildDatFileMap(actions, uniprotRecords))
                    .build();
            String releaseID = release != null ? release.getReleaseNumber() : null;
            var zfinReport = new UniProtReportAdapter().adapt("UniProt Diff Load", releaseID, container);
            var report = new LegacyReportAdapter().adapt(zfinReport);
            String html = new ReportWriter().render(report);
            writeToFileOrZip(new File(reportFile), html, "UTF-8");
            log.info("Finished creating report file: " + reportFile);
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ")\n" + e.getMessage(), e);
        }
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
            log.debug("Creating context from DB connection.");
            context = UniProtLoadContext.createFromDBConnection();
            log.debug("Context initialized");
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
