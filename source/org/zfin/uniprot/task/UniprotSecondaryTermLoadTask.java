package org.zfin.uniprot.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.biojava.bio.BioException;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.InterProProteinDTO;
import org.zfin.uniprot.interpro.EntryListTranslator;
import org.zfin.uniprot.secondary.*;
import org.zfin.uniprot.persistence.UniProtRelease;
import org.zfin.uniprot.secondary.handlers.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.datatransfer.service.DownloadService.downloadFileViaWget;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.sequence.ForeignDB.AvailableName.*;
import static org.zfin.uniprot.UniProtFilterTask.readAllZebrafishEntriesFromSourceIntoRecords;
import static org.zfin.uniprot.UniProtTools.getArgOrEnvironmentVar;

@Log4j2
@Getter
@Setter
public class UniprotSecondaryTermLoadTask extends AbstractScriptWrapper {

    private static final int ACTION_SIZE_ERROR_THRESHOLD = 20_000;
    private static final String LOAD_REPORT_TEMPLATE_HTML = "/home/uniprot/secondary-load-report.html";
    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";
    private final String domainFile;
    private final String contextInputFile;

    private enum LoadTaskMode {
        REPORT,
        LOAD,
        REPORT_AND_LOAD
    }
    private final LoadTaskMode mode;
    private final String actionsFileName;
    private String inputFileName;
    private final String outputJsonName;
    private final String outputReportName;
    private final String contextOutputFile;
    private final String ipToGoTranslationFile;
    private final String ecToGoTranslationFile;
    private final String upToGoTranslationFile;
    private UniProtRelease release;
    private List<SecondaryTerm2GoTerm> ipToGoRecords;
    private List<SecondaryTerm2GoTerm> ecToGoRecords;
    private List<SecondaryTerm2GoTerm> upToGoRecords;
    private List<InterProProteinDTO> downloadedInterproDomainRecords;

    private SecondaryTermLoadPipeline pipeline;

    public static void main(String[] args) throws Exception {

        String inputFileName = "";
        String ipToGoTranslationFile = "";
        String ecToGoTranslationFile = "";
        String upToGoTranslationFile = "";
        String domainFile = "";
        String outputJsonName = "";
        String actionsFileName = "";
        String contextInputFile = "";

        //mode can be one of the following (REPORT is default):
        // 1. "REPORT" - generate actions from the input file and write them to a file (no load)
        // 2. "LOAD" - load actions from a file into the database
        // 3. "REPORT_AND_LOAD" - generate actions from the input file, write them to a file, and load them into the database
        String modeArg = getArgOrEnvironmentVar(args, 0, "UNIPROT_LOAD_MODE", "");
        LoadTaskMode mode = null;
        try {
            mode = LoadTaskMode.valueOf(modeArg.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.debug("Invalid mode or no mode provided, defaulting to REPORT: " + modeArg);
        }

        //if no "mode" is provided, check the environment variable UNIPROT_COMMIT_CHANGES from jenkins
        if (mode == null) {
            String commitChangesEnvironmentVar = System.getenv("UNIPROT_COMMIT_CHANGES");
            if (StringUtils.isNotEmpty(commitChangesEnvironmentVar) && commitChangesEnvironmentVar.equalsIgnoreCase("true")) {
                mode = LoadTaskMode.REPORT_AND_LOAD;
            } else {
                mode = LoadTaskMode.REPORT;
            }
        }

        if (mode.equals(LoadTaskMode.REPORT) || mode.equals(LoadTaskMode.REPORT_AND_LOAD)) {
            inputFileName = getArgOrEnvironmentVar(args, 1, "UNIPROT_INPUT_FILE", "");
            ipToGoTranslationFile = getArgOrEnvironmentVar(args, 2, "IP2GO_FILE", "");
            ecToGoTranslationFile = getArgOrEnvironmentVar(args, 3, "EC2GO_FILE", "");
            upToGoTranslationFile = getArgOrEnvironmentVar(args, 4, "UP2GO_FILE", "");
            domainFile = getArgOrEnvironmentVar(args, 4, "DOMAIN_FILE", "");
            outputJsonName = getArgOrEnvironmentVar(args, 5, "UNIPROT_OUTPUT_FILE", defaultOutputFileName(inputFileName));
            contextInputFile = getArgOrEnvironmentVar(args, 6, "CONTEXT_INPUT_FILE", "");
        } else if (mode.equals(LoadTaskMode.LOAD)) {
            actionsFileName = getArgOrEnvironmentVar(args, 1, "UNIPROT_ACTIONS_FILE", "");
        } else {
            printUsage();
            System.exit(1);
        }

        UniprotSecondaryTermLoadTask task = new UniprotSecondaryTermLoadTask(mode, inputFileName, outputJsonName, ipToGoTranslationFile, ecToGoTranslationFile, upToGoTranslationFile, domainFile, contextInputFile, actionsFileName);
        task.runTask();
    }

    private static String defaultOutputFileName(String inputFileName) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
        return inputFileName + ".actions." + timestamp + ".json";
    }

    private static Optional<UniProtRelease> getLatestUnprocessedUniProtRelease() {
        List<UniProtRelease> releases = getInfrastructureRepository().getAllUniProtReleases();
        if (releases.isEmpty()) {
            return Optional.empty();
        }

        //get the latest release that's been processed by the primary load logic, but not by the secondary load logic
        return releases
                .stream()
                .filter(release -> release.getSecondaryLoadDate() == null)
                .filter(release -> release.getProcessedDate() != null)
                .findFirst();
    }

    public UniprotSecondaryTermLoadTask(LoadTaskMode mode, String inputFileName, String outputJsonName, String ipToGoTranslationFile, String ecToGoTranslationFile, String upToGoTranslationFile, String domainFile, String contextInputFile, String actionsFileName) {
        this.mode = mode;
        this.inputFileName = inputFileName;
        this.outputJsonName = outputJsonName;
        this.outputReportName = outputJsonName + ".report.html";
        this.contextOutputFile = outputJsonName + ".context.json";

        this.ipToGoTranslationFile = ipToGoTranslationFile;
        this.ecToGoTranslationFile = ecToGoTranslationFile;
        this.upToGoTranslationFile = upToGoTranslationFile;
        this.domainFile = domainFile;
        this.contextInputFile = contextInputFile;
        this.actionsFileName = actionsFileName;
    }

    public void runTask() throws IOException, BioException, SQLException {
        initialize();
        log.debug("Starting UniProtSecondaryTermLoadTask for file " + inputFileName + ".");

        if (mode.equals(LoadTaskMode.REPORT) || mode.equals(LoadTaskMode.REPORT_AND_LOAD)) {
            try (BufferedReader inputFileReader = new BufferedReader(new java.io.FileReader(inputFileName))) {
                loadTranslationFiles();
                UniprotReleaseRecords entries = readUniProtEntries(inputFileReader);
                executePipeline(entries);
                log.debug("Finished executing pipeline: " + pipeline.getActions().size() + " actions created.");
                writeActionsToFile(pipeline.getActions());
                writeOutputReportFile(pipeline.getActions());

                String overrideEnv = System.getenv("ACTION_SIZE_ERROR_THRESHOLD");
                long overrideThreshold = overrideEnv == null ? ACTION_SIZE_ERROR_THRESHOLD : Long.parseLong(overrideEnv);
                if (pipeline.getActions().size() > overrideThreshold) {
                    log.error("Too many actions created: " + pipeline.getActions().size() + " actions created.");
                    log.error("Threshold set to: " + ACTION_SIZE_ERROR_THRESHOLD);
                    log.error("Override threshold with environment variable: ACTION_SIZE_ERROR_THRESHOLD");
                    log.error("Exiting script in case this is due to an error.");
                    System.exit(1);
                }

                if (mode.equals(LoadTaskMode.REPORT_AND_LOAD)) {
                    pipeline.setRelease(release);
                    pipeline.processActions();
                }
            }
        } else if (mode.equals(LoadTaskMode.LOAD)) {
            List<SecondaryTermLoadAction> actions = readActionsFile();
            log.debug("Finished reading actions file: " + actions.size() + " actions read.");
            pipeline = new SecondaryTermLoadPipeline();
            pipeline.setActions(actions);
            pipeline.setRelease(release);
            pipeline.processActions();
        } else {
            System.out.println("Invalid mode: " + mode);
            printUsage();
            System.exit(1);
        }

    }

    private List<SecondaryTermLoadAction> readActionsFile() {
        String jsonFile = this.actionsFileName;
        if (StringUtils.isEmpty(jsonFile)) {
            log.error("No actions file specified for mode: " + getMode() +  ".");
            System.exit(6);
        }
        log.debug("Reading JSON file: " + jsonFile);
        try {
            SecondaryTermLoadActionsContainer actionsContainer =
                    new ObjectMapper().readValue(new File(jsonFile), SecondaryTermLoadActionsContainer.class);
            if (actionsContainer.getReleaseID() != null) {
                this.release = getInfrastructureRepository().getUniProtReleaseByID(actionsContainer.getReleaseID());
            }
            return actionsContainer.getActions();
        } catch (IOException e) {
            log.error("Failed to read JSON file: " + jsonFile, e);
            return null;
        }
    }

    private void writeOutputReportFile(List<SecondaryTermLoadAction> actions) {
        String reportFile = this.outputReportName;

        log.debug("Creating report file: " + reportFile);
        try {
            String jsonContents = actionsToJsonString(actions);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + LOAD_REPORT_TEMPLATE_HTML;
            String templateContents = FileUtils.readFileToString(new File(template), "UTF-8");
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonContents);
            FileUtils.writeStringToFile(new File(reportFile), filledTemplate, "UTF-8");
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ") from template\n" + e.getMessage(), e);
        }
    }

    private void writeActionsToFile(List<SecondaryTermLoadAction> actions) {
        SecondaryTermLoadActionsContainer actionsContainer = new SecondaryTermLoadActionsContainer(
                this.release == null ? null : this.release.getUpr_id(),
                new Date(),
                actions);

        try {
            (new ObjectMapper()).writeValue(new File(this.outputJsonName), actionsContainer);
        } catch (IOException e) {
            log.error("Failed to write JSON file: " + this.outputJsonName, e);
            //do nothing
        }
    }

    private String actionsToJsonString(List<SecondaryTermLoadAction> actions) throws JsonProcessingException {
        SecondaryTermLoadActionsContainer actionsContainer = new SecondaryTermLoadActionsContainer(
                this.release == null ? null : this.release.getUpr_id(),
                new Date(),
                actions);

        return (new ObjectMapper()).writeValueAsString(actionsContainer);
    }

    private void writeContext(SecondaryLoadContext context) {
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

    private void executePipeline(UniprotReleaseRecords entries) {
        setupPipeline(entries);
        calculatePipelineActions();
    }

    private void setupPipeline(UniprotReleaseRecords entries) {
        this.pipeline = new SecondaryTermLoadPipeline();
        pipeline.setUniprotRecords(entries);

        SecondaryLoadContext context;
        if (StringUtils.isEmpty(contextInputFile)) {
            context = SecondaryLoadContext.createFromDBConnection();
            writeContext(context);
        } else {
            try {
                log.debug("Reading context file: " + contextInputFile + ".");
                context = SecondaryLoadContext.createFromContextFile(contextInputFile);
            } catch (IOException e) {
                throw new RuntimeException("Error reading context file: " + contextInputFile + ": " + e.getMessage(), e);
            }
        }

        pipeline.setContext(context);
    }

    private void calculatePipelineActions() {
        pipeline.addHandler(new RemoveFromLostUniProtsActionCreator(INTERPRO), RemoveFromLostUniProtsActionProcessor.class);
        pipeline.addHandler(new AddNewDBLinksFromUniProtsActionCreator(INTERPRO), AddNewDBLinksFromUniProtsActionProcessor.class);

        pipeline.addHandler(new RemoveFromLostUniProtsActionCreator(EC), RemoveFromLostUniProtsActionProcessor.class);
        pipeline.addHandler(new AddNewDBLinksFromUniProtsActionCreator(EC), AddNewDBLinksFromUniProtsActionProcessor.class);

        pipeline.addHandler(new RemoveFromLostUniProtsActionCreator(PFAM), RemoveFromLostUniProtsActionProcessor.class);
        pipeline.addHandler(new AddNewDBLinksFromUniProtsActionCreator(PFAM), AddNewDBLinksFromUniProtsActionProcessor.class);

        pipeline.addHandler(new RemoveFromLostUniProtsActionCreator(PROSITE), RemoveFromLostUniProtsActionProcessor.class);
        pipeline.addHandler(new AddNewDBLinksFromUniProtsActionCreator(PROSITE), AddNewDBLinksFromUniProtsActionProcessor.class);

        pipeline.addHandler(new MarkerGoTermEvidenceActionCreator(INTERPRO, ipToGoRecords), MarkerGoTermEvidenceActionProcessor.class);
        pipeline.addHandler(new MarkerGoTermEvidenceActionCreator(EC, ecToGoRecords), MarkerGoTermEvidenceActionProcessor.class);

        pipeline.addHandler(new AddNewSpKeywordTermToGoActionCreator(UNIPROTKB, upToGoRecords), AddNewSpKeywordTermToGoActionProcessor.class);
        pipeline.addHandler(new RemoveSpKeywordTermToGoActionCreator(UNIPROTKB, upToGoRecords), RemoveSpKeywordTermToGoActionProcessor.class);

        pipeline.addHandler(new InterproDomainActionCreator(downloadedInterproDomainRecords), InterproDomainActionProcessor.class);
        pipeline.addHandler(new InterproProteinActionCreator(), InterproProteinActionProcessor.class);
        pipeline.addHandler(new InterproMarkerToProteinActionCreator(), InterproMarkerToProteinActionProcessor.class);
        pipeline.addHandler(new ProteinToInterproActionCreator(), ProteinToInterproActionProcessor.class);
        pipeline.addHandler(new PDBActionCreator(), PDBActionProcessor.class);

        pipeline.createActions();
    }

    public void initialize() {
        initAll();
        setInputFileName();
    }

    private void loadTranslationFiles() {
        try {
            //If the 2go files are not provided, download them
            String upToGo = upToGoTranslationFile;
            if (StringUtils.isEmpty(upToGo)) {
                File downloadedFile1 = File.createTempFile("upkw2go", ".dat");
                upToGo = downloadedFile1.getAbsolutePath();
                String url1 = ZfinPropertiesEnum.UNIPROT_KW2GO_FILE_URL.value();
                downloadFileViaWget(url1, downloadedFile1.toPath(), 10_000, log);
            }
            log.debug("Loading " + upToGo);
            upToGoRecords = SecondaryTerm2GoTermTranslator.convertTranslationFileToUnloadFile(upToGo, SecondaryTerm2GoTermTranslator.SecondaryTermType.UniProtKB);
            log.debug("Loaded " + upToGoRecords.size() + " UP to GO records.");

            String ipToGo = ipToGoTranslationFile;
            if (StringUtils.isEmpty(ipToGo)) {
                File downloadedFile2 = File.createTempFile("ip2go", ".dat");
                ipToGo = downloadedFile2.getAbsolutePath();
                String url2 = ZfinPropertiesEnum.UNIPROT_IP2GO_FILE_URL.value();
                downloadFileViaWget(url2, downloadedFile2.toPath(), 10_000, log);
            }
            log.debug("Loading " + ipToGo);
            ipToGoRecords = SecondaryTerm2GoTermTranslator.convertTranslationFileToUnloadFile(ipToGo, SecondaryTerm2GoTermTranslator.SecondaryTermType.InterPro);
            log.debug("Loaded " + ipToGoRecords.size() + " InterPro to GO records.");

            String ecToGo = ecToGoTranslationFile;
            if (StringUtils.isEmpty(ecToGo)) {
                File downloadedFile3 = File.createTempFile("ec2go", ".dat");
                ecToGo = downloadedFile3.getAbsolutePath();
                String url3 = ZfinPropertiesEnum.UNIPROT_EC2GO_FILE_URL.value();
                downloadFileViaWget(url3, downloadedFile3.toPath(), 10_000, log);
            }
            log.debug("Loading " + ecToGo);
            ecToGoRecords = SecondaryTerm2GoTermTranslator.convertTranslationFileToUnloadFile(ecToGo, SecondaryTerm2GoTermTranslator.SecondaryTermType.EC);
            log.debug("Loaded " + ecToGoRecords.size() + " EC to GO records.");

            String domainFilename = this.domainFile;
            if (StringUtils.isEmpty(domainFilename)) {
                log.debug("Downloading entry.list for domain info");
                File downloadedFile4 = File.createTempFile("entry", ".list");
                //            String url4 = ZfinPropertiesEnum.UNIPROT_ENTRY_LIST_FILE_URL.value();
                String url4 = "https://ftp.ebi.ac.uk/pub/databases/interpro/current_release/entry.list";
                downloadFileViaWget(url4, downloadedFile4.toPath(), 10_000, log);
                log.debug("Loading " + downloadedFile4.getAbsolutePath());
                domainFilename = downloadedFile4.getAbsolutePath();
            }
            List<InterProProteinDTO> entryList = EntryListTranslator.parseFile(new File(domainFilename));
            log.debug("Loaded " + entryList.size() + " entries.");
            this.downloadedInterproDomainRecords = entryList;

        } catch (IOException e) {
            log.error("Failed to load translation file: ", e);
            throw new RuntimeException(e);
        }
    }

    private void setInputFileName() {
        Optional<UniProtRelease> releaseOptional = getLatestUnprocessedUniProtRelease();

        //only need an input file if we are generating a report of actions, otherwise, we are loading directly from the actions
        if (mode.equals(LoadTaskMode.REPORT) || mode.equals(LoadTaskMode.REPORT_AND_LOAD)) {
            if (inputFileName.isEmpty() && releaseOptional.isPresent()) {
                log.debug("Loading from latest UniProt release: " + releaseOptional.get().getPath() + "(md5:" + releaseOptional.get().getMd5() + ")" );
                inputFileName = releaseOptional.get().getLocalFile().getAbsolutePath();
                release = releaseOptional.get();
            } else if (inputFileName.isEmpty()) {
                throw new RuntimeException("No input file specified and no unprocessed UniProt release found.");
            }
        }
    }

    public UniprotReleaseRecords readUniProtEntries(BufferedReader inputFileReader) throws BioException, IOException {
        UniprotReleaseRecords entries = readAllZebrafishEntriesFromSourceIntoRecords(inputFileReader);
        log.debug("Finished reading file: " + entries.size() + " entries read.");

        return entries;
    }

    public static void printUsage() {
        System.out.println("Usage: uniprotSecondaryTermLoadTask <mode> [more args]");
        System.out.println("  mode: 'LOAD' or 'REPORT' or 'REPORT_AND_LOAD'");
        System.out.println("  if mode is 'LOAD', more args = <actions file>");
        System.out.println("  if mode is 'REPORT_AND_LOAD' or 'REPORT', more args = <input file> <up to go translation file> <ip to go translation file> <ec to go translation file> <output file>");
        System.out.println("  instead of arguments, you can use environment variables: UNIPROT_LOAD_MODE, INPUT_FILE, UP_TO_GO_TRANSLATION_FILE, IP_TO_GO_TRANSLATION_FILE, EC_TO_GO_TRANSLATION_FILE, OUTPUT_FILE");
        System.out.println("  or, for load mode, env vars: UNIPROT_LOAD_MODE, UNIPROT_ACTIONS_FILE");
        System.out.println("  the actions file is generated by the report mode");
    }

}
