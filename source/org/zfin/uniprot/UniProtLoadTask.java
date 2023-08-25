package org.zfin.uniprot;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * This class is used to perform a load of uniprot dat file.
 *
 */
@Log4j2
public class UniProtLoadTask extends AbstractScriptWrapper {
    private BufferedReader inputFileReader;
    private UniProtLoadContext context;

    public static void main(String[] args) throws Exception {
        String inputFileName = getArgOrEnvironmentVar(args, 0, "UNIPROT_INPUT_FILE");

        BufferedReader inputFileReader = new BufferedReader(new java.io.FileReader(inputFileName));

        UniProtLoadTask task = new UniProtLoadTask(inputFileReader);
        task.runTask();
    }

    public UniProtLoadTask(BufferedReader bufferedReader) {
        this.inputFileReader = bufferedReader;
    }

    public void runTask() throws IOException, BioException, SQLException {
        initAll();

        // calculate the current context
        calculateContext();

        System.out.println("Starting to read file: " );
        Map<String, RichSequenceAdapter> entries = readAllZebrafishEntriesFromSourceIntoMap(inputFileReader);
        System.out.println("Finished reading file: " + entries.size() + " entries read.");


        // data entry pipeline
        UniProtLoadPipeline pipeline = new UniProtLoadPipeline();
        pipeline.setContext(context);
        pipeline.setUniProtRecords(entries);
        pipeline.addHandler(new RemoveVersionHandler());
        pipeline.addHandler(new IgnoreSpecificAccessionsHandler());

        //these two work together to get list of genes that would lose accessions
        pipeline.addHandler(new MatchOnRefSeqHandler());
        pipeline.addHandler(new ReportLostUniProtsHandler());

        //reset everything back to the beginning
        pipeline.addHandler(new ResetLoadActionsHandler());
        pipeline.addHandler(new IgnoreAccessionsAlreadyInDatabaseHandler());
        pipeline.addHandler(new MatchOnRefSeqHandler());
        List<UniProtLoadAction> actions = pipeline.execute();

        //do something with the actions
        writeActions(actions);
        writeOutputReportFile(actions);
    }

    private void writeActions(List<UniProtLoadAction> actions) {
        String tempFileName = "/tmp/uniprot_load_report_" + System.currentTimeMillis() + ".json";
        System.out.println("report tempfile: " + tempFileName);
        try {
            FileUtils.writeStringToFile(new File(tempFileName), actionsToJson(actions));
        } catch (IOException e) {
            log.error("Error writing report file: " + tempFileName, e);
        }
    }

    private String actionsToJson(List<UniProtLoadAction> actions) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(actions);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeOutputReportFile(List<UniProtLoadAction> actions) {
        String reportfile = "/tmp/uniprot_load_" + System.currentTimeMillis() + ".report.html";

        System.out.println("Creating report file: " + reportfile);
        try {
            String jsonContents = actionsToJson(actions);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + "/home/uniprot/load-report.html";
            String templateContents = FileUtils.readFileToString(new File(template));
            String filledTemplate = templateContents.replace("JSON_GOES_HERE", jsonContents);
            FileUtils.writeStringToFile(new File(reportfile), filledTemplate);
        } catch (IOException e) {
            System.err.println("Error creating report (" + reportfile + ") from template\n" + e.getMessage());
        }
    }


    private void calculateContext() {
        context = UniProtLoadContext.createFromDBConnection();

        //create temp file
        try {
            String tempFileName = "/tmp/uniprot_load_" + System.currentTimeMillis() + ".tmp";
            System.out.println("tempFileName: " + tempFileName);
            File tempFile = new File(tempFileName);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(tempFile, context);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
