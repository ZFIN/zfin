package org.zfin.uniprot;

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
import java.util.*;

import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.handlers.*;

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
    private final BufferedReader inputFileReader;
    private final String outputJsonName;
    private final String outputReportName;
    private UniProtLoadContext context;


    public static void main(String[] args) throws Exception {
        String inputFileName = getArgOrEnvironmentVar(args, 0, "UNIPROT_INPUT_FILE");
        String outputJsonName = getArgOrEnvironmentVar(args, 1, "UNIPROT_OUTPUT_JSON_FILE", calculateDefaultOutputFileName("json"));
        String outputReportName = getArgOrEnvironmentVar(args, 2, "UNIPROT_OUTPUT_REPORT_FILE", calculateDefaultOutputFileName("report.html"));

        try (BufferedReader inputFileReader = new BufferedReader(new java.io.FileReader(inputFileName))) {
            UniProtLoadTask task = new UniProtLoadTask(inputFileReader, outputJsonName, outputReportName);
            log.debug("Starting UniProtLoadTask for file " + inputFileName + " with output files " + outputJsonName + " and " + outputReportName + ".");
            task.runTask();
        }
    }

    public UniProtLoadTask(BufferedReader bufferedReader, String outputJsonName, String outputReportName) {
        this.inputFileReader = bufferedReader;
        this.outputJsonName = outputJsonName;
        this.outputReportName = outputReportName;
    }

    public void runTask() throws IOException, BioException, SQLException {
        initialize();
        Map<String, RichSequenceAdapter> entries = readUniProtEntries();
        Set<UniProtLoadAction> actions = executePipeline(entries);
        writeOutputReportFile(actions);
    }

    private void initialize() {
        initAll();
        calculateContext();
    }

    private Map<String, RichSequenceAdapter> readUniProtEntries() throws BioException, IOException {
        Map<String, RichSequenceAdapter> entries = readAllZebrafishEntriesFromSourceIntoMap(inputFileReader);
        log.debug("Finished reading file: " + entries.size() + " entries read.");
        return entries;
    }

    private Set<UniProtLoadAction> executePipeline(Map<String, RichSequenceAdapter> entries) {
        // data entry pipeline
        UniProtLoadPipeline pipeline = new UniProtLoadPipeline();
        pipeline.setContext(context);
        pipeline.setUniProtRecords(entries);
        pipeline.addHandler(new RemoveVersionHandler());
        pipeline.addHandler(new IgnoreSpecificAccessionsHandler());
        pipeline.addHandler(new ReportWouldBeLostHandler());
        pipeline.addHandler(new IgnoreAccessionsAlreadyInDatabaseHandler());
        pipeline.addHandler(new MatchOnRefSeqHandler());

        Set<UniProtLoadAction> actions = pipeline.execute();
        return actions;
    }

    private void writeOutputReportFile(Set<UniProtLoadAction> actions) {
        String reportFile = this.outputReportName;
        String jsonFile = this.outputJsonName;

        log.debug("Creating report file: " + reportFile);
        try {
            String jsonContents = actionsToJson(actions);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + LOAD_REPORT_TEMPLATE_HTML;
            String templateContents = FileUtils.readFileToString(new File(template), "UTF-8");
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonContents);
            FileUtils.writeStringToFile(new File(reportFile), filledTemplate, "UTF-8");
            FileUtils.writeStringToFile(new File(jsonFile), jsonContents, "UTF-8");
            log.debug("Finished creating report file: " + reportFile + " and json file: " + jsonFile);
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ") from template\n" + e.getMessage(), e);
        }
    }

    private String actionsToJson(Set<UniProtLoadAction> actions) throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(actions);
    }


    private void calculateContext() {
        context = UniProtLoadContext.createFromDBConnection();
    }

    private static String calculateDefaultOutputFileName(String extension) {
        return String.format(UNIPROT_LOAD_OUTPUT_FILENAME_TEMPLATE, System.currentTimeMillis(), extension);
    }

}
