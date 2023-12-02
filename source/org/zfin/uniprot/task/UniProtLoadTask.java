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
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.UniProtLoadPipeline;
import org.zfin.uniprot.UniProtLoadService;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.handlers.*;
import org.zfin.uniprot.persistence.UniProtRelease;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.uniprot.UniProtFilterTask.readAllZebrafishEntriesFromSourceIntoMap;
import static org.zfin.uniprot.UniProtTools.getArgOrEnvironmentVar;

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
        String outputJsonName = getArgOrEnvironmentVar(args, 1, "UNIPROT_OUTPUT_JSON_FILE", calculateDefaultOutputFileName(startTime, "json"));
        String outputReportName = getArgOrEnvironmentVar(args, 2, "UNIPROT_OUTPUT_REPORT_FILE", calculateDefaultOutputFileName(startTime, "report.html"));
        String commitChanges = getArgOrEnvironmentVar(args, 3, "UNIPROT_COMMIT_CHANGES", "false");
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
            writeOutputReportFile(actions);
            loadChangesIfNotDryRun(actions);
        }
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
                objectMapper.writeValue(new File(contextOutputFile), context);
            } catch (IOException e) {
                log.error("Error writing context file " + contextOutputFile + ": " + e.getMessage(), e);
            }
        }
    }

    private void setInputFileName() {
        Optional<UniProtRelease> releaseOptional = getLatestUnprocessedUniProtRelease();
        if (inputFileName.isEmpty() && releaseOptional.isPresent()) {
            inputFileName = releaseOptional.get().getLocalFile().getAbsolutePath();
            release = releaseOptional.get();
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
        pipeline.addHandler(new ReportWouldBeLostHandler());
        pipeline.addHandler(new MatchOnRefSeqIgnoreExistingHandler());
        pipeline.addHandler(new RemoveIgnoreActionsHandler());
        pipeline.addHandler(new ReportLegacyProblemFilesHandler());
        pipeline.addHandler(new FlagPotentialIssuesHandler());

        Set<UniProtLoadAction> actions = pipeline.execute();
        return actions;
    }

    private void writeOutputReportFile(Set<UniProtLoadAction> actions) {
        String reportFile = this.outputReportName;
        String jsonFile = this.outputJsonName;

        log.info("Creating report file: " + reportFile);
        try {
            String jsonContents = actionsToJson(actions);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + LOAD_REPORT_TEMPLATE_HTML;
            String templateContents = FileUtils.readFileToString(new File(template), "UTF-8");
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonContents);
            FileUtils.writeStringToFile(new File(reportFile), filledTemplate, "UTF-8");
            FileUtils.writeStringToFile(new File(jsonFile), jsonContents, "UTF-8");
            log.info("Finished creating report file: " + reportFile + " and json file: " + jsonFile);
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ") from template\n" + e.getMessage(), e);
        }
    }

    private String actionsToJson(Set<UniProtLoadAction> actions) throws JsonProcessingException {
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

}
