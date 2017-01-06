package org.zfin.ontology.datatransfer.service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.obo.dataadapter.AbstractParseEngine;
import org.obo.dataadapter.DefaultOBOParser;
import org.obo.dataadapter.OBOParseEngine;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.*;
import org.obo.history.SessionHistoryList;
import org.springframework.beans.factory.annotation.Autowired;
import org.zfin.database.DbSystemUtil;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.DataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.ontology.datatransfer.InvalidOBOFileException;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.util.*;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.*;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;


/**
 * Main class to start the ontology loader. Need to pass in the name of the obo file.
 * Command line options:
 * -oboFile <obo file name>
 * -log4jFilename <log4j.xml file>
 * -dbScriptFileName <*.sql file>
 * -propertyFile <path to zfin.properties location>
 * <p/>
 * This class assumes the latest obo file has already been downloaded from the corresponding site.
 * See {@link org.zfin.ontology.datatransfer.DownloadOntology}.
 */
public class LoadOntology extends AbstractValidateDataReportTask {

    @Autowired
    private ExpressionService expressionService = new ExpressionService();

    private static Logger LOG;

    static {
        options.addOption(oboFileNameOption);
        options.addOption(loadDir);
        options.addOption(log4jFileOption);
        options.addOption(dbScriptFileOption);
        options.addOption(webrootDirectory);
        options.addOption(productionModeOption);
        options.addOption(debugModeOption);
        options.addOption(DataReportTask.jobNameOpt);
    }

    private OBOSession oboSession;
    private String oboFilename;
    private String[] dbScriptFiles;

    private Ontology ontology;
    private CronJobReport report;
    private long sectionTime;
    // this attribute holds all the data that need to be imported into the database,
    // i.e. new terms, synonyms etc.. They have a key that is used (referred to) in the db script file
    // Currently, all the data need to be of type string!
    // There are about 20 different types of data set to be imported.  
    private Map<String, List<List<String>>> dataMap = new HashMap<>(20);
    private OntologyMetadata oboMetadata;
    private static final ChoiceFormat termChoice = new ChoiceFormat("0#terms| 1#term| 2#terms");

    // if true then only import if a new file is encountered
    // if false always load the obo file.
    private boolean productionMode = true;
    private boolean debugMode = true;
    private File loadDirectory;

    /**
     * Used from within the web app. No initialization needed.
     *
     * @param oboFile     obo file name
     * @param scriptFiles array of script files
     * @throws IOException file not found exception
     */
    public LoadOntology(String oboFile, String... scriptFiles) throws IOException {
        initializeLoad(oboFile, scriptFiles);
        cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
    }

    public LoadOntology(String jobName, String propertyDirectory, String baseDir, String oboFile, String... scriptFiles) throws IOException {
        super(jobName, propertyDirectory, baseDir);
        initDatabase();
        initializeLoad(oboFile, scriptFiles);
        cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
    }

    private void initializeLoad(String oboFile, String[] scriptFiles) throws IOException {
        oboFilename = oboFile;
        this.dbScriptFiles = scriptFiles;
        if (!FileUtil.checkFileExists(oboFilename))
            throw new IOException("No OBO file <" + oboFile + "> found. You may have to download the obo file first.");
        if (dbScriptFiles != null) {
            for (String dbScriptFile : dbScriptFiles) {
                File file = new File(dbScriptFile);
                loadDirectory = new File(FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath()));
                if (!FileUtil.checkFileExists(dbScriptFile))
                    throw new IOException("No DB script file <" + dbScriptFile + "> found!");
            }
        }
    }

    public static void main(String[] arguments) {
        LOG = Logger.getLogger(LoadOntology.class);
        LOG.setLevel(Level.INFO);
        LOG.info("Start Ontology Loader class: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
        initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        String oboFile = commandLine.getOptionValue(oboFileNameOption.getOpt());
        String dbScriptFilesNames = commandLine.getOptionValue(dbScriptFileOption.getOpt());
        String webrootDir = commandLine.getOptionValue(webrootDirectory.getOpt());
        String jobName = commandLine.getOptionValue(DataReportTask.jobNameOpt.getOpt());
        String loadingDir = commandLine.getOptionValue(loadDir.getOpt());
        String[] dbScriptFiles = dbScriptFilesNames.split(",");
        LOG.info("Loading obo file: " + oboFile);
        String propertyFileName = getPropertyFileFromWebroot(webrootDir);

        LoadOntology loader = null;
        try {
            loader = new LoadOntology(jobName, propertyFileName, loadingDir, oboFile, dbScriptFiles);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.exit(-1);
        }
        loader.jobName = jobName;
        LOG.info("Job Name: " + loader.jobName);
        String optionValue = commandLine.getOptionValue(productionModeOption.getOpt());
        if (StringUtils.isNotEmpty(optionValue))
            loader.productionMode = Boolean.parseBoolean(optionValue);
        String debugOptionValue = commandLine.getOptionValue(debugModeOption.getOpt());
        if (StringUtils.isNotEmpty(debugOptionValue))
            loader.debugMode = Boolean.parseBoolean(debugOptionValue);
        LOG.info("Property: " + ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL.value());
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        if (loader.initialize(oboFile, cronJobUtil)) {
            loader.runOntologyUpdateProcess();
        }
    }

    public void runOntologyUpdateProcess() {
        openTraceFile();
        if (processOboFile()) {
            try {
                HibernateUtil.createTransaction();
                LOG.info("Duration of Loader: " + DateUtil.getTimeDuration(sectionTime));
                report.addMessageToSection("Finished Processing Obo file", "Parse Obo File");
                sectionTime = System.currentTimeMillis();
                int index = 1;
                for (String dbScriptFile : dbScriptFiles) {
                    LOG.info("Running Db script: " + dbScriptFile);
                    String sectionName = "Script execution " + index++;
                    StringBuilder startMessage = new StringBuilder("Start Script Execution: " + dbScriptFile + "\n");
                    long startTimeLong = System.currentTimeMillis();
                    startMessage.append("Start Time: " + new Date() + "\n");
                    report.addMessageToSection(startMessage.toString(), sectionName);
                    runDbScriptFile(dbScriptFile);
                    LOG.info("Duration of Script Execution: " + DateUtil.getTimeDuration(sectionTime));
                    report.addMessageToSection("Finish Time: " + new Date() + "\n", sectionName);
                    report.addMessageToSection("Duration: " + DateUtil.getTimeDuration(startTimeLong), sectionName);
                }
                runValidationChecks();
                HibernateUtil.flushAndCommitCurrentSession();
                LOG.info("Committed load...");
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                LOG.error(e);
                report.error(e);
                throw new RuntimeException(e);
            } finally {
                try {
                    // set handleCurationEvent status file so the new ontology can be loaded into memory
                    File dir = ZfinProperties.getOntologyReloadStatusDirectory().toFile();
                    if (!dir.exists())
                        dir.mkdir();
                    File statusFile = new File(dir, ontology.getOntologyName());
                    if (statusFile.exists())
                        statusFile.delete();
                    statusFile.createNewFile();
                } catch (IOException e) {
                    LOG.error(e);
                }
                HibernateUtil.closeSession();
            }
            // needed to allow the generate absolute hyperlinks in emails
            TermPresentation.domain = "http://" + ZfinPropertiesEnum.DOMAIN_NAME;
            postLoadProcess();
            // need to reverse it. A bit of a hack!                                                                                                                                                    zx
            TermPresentation.domain = null;
        } else {
            // create "no update" report
            ReportGenerator rg = new ReportGenerator();
            rg.setReportTitle("Report for " + jobName);
            rg.includeTimestamp();
            rg.addIntroParagraph("No update found for " + ontology.getOntologyName() + " load. Current version saved " +
                    "by " + oboMetadata.getSavedBy() + " on " + oboMetadata.getDate() + ".");
            rg.writeFiles(new File(loadDirectory, jobName), "statistics");
        }
        LOG.info("Total Execution Time: " + DateUtil.getTimeDuration(sectionTime));
        closeTraceFile();
    }

    private void runValidationChecks() {
        boolean failure = false;
        // check stage ranges for terms
        List<GenericTermRelationship> invalidStartStages = getOntologyRepository().getTermsWithInvalidStartStageRange();
        if (CollectionUtils.isNotEmpty(invalidStartStages)) {
            String title = "Terms with start stages that are before the start stages of their parent term";
            createStageReport(invalidStartStages, title, "Start Stage", "Start Stage", "terms with invalid start stages");
            failure = true;
        }
        List<GenericTermRelationship> invalidEndStages = getOntologyRepository().getTermsWithInvalidEndStageRange();
        if (CollectionUtils.isNotEmpty(invalidEndStages)) {
            String title = "Terms with end stages that are after the end stages of their parent term";
            createEndStageReport(invalidEndStages, title, "End Stage", "End Stage", "terms with invalid end stages (!= develops from)");
            failure = true;
        }
        invalidEndStages = getOntologyRepository().getTermsWithInvalidStartEndStageRangeForDevelopsFrom();
        if (CollectionUtils.isNotEmpty(invalidEndStages)) {
            String title = "Terms with start stages that are after the end stages of their parent terms (develops from)";
            createStageReport(invalidEndStages, title, "End Stage", "Start Stage", "terms with invalid end stages (= develops from)");
            failure = true;
        }
        if (failure)
            throw new RuntimeException("Error while running stage definition validation checks");
    }

    private void createEndStageReport(List<GenericTermRelationship> invalidStages, String title, String parentStage, String childStage, String subject) {
        LOG.warn(title);
        List<List<String>> rows = new ArrayList<>(invalidStages.size());
        for (GenericTermRelationship pheno : invalidStages) {
            List<String> row = new ArrayList<>();
            row.add(pheno.getTermOne().getTermName());
            row.add(pheno.getTermTwo().getTermName());
            row.add(pheno.getTermOne().getEnd().getName());
            row.add(pheno.getTermTwo().getEnd().getName());
            rows.add(row);
        }
        emailStageReport(title, parentStage, childStage, subject, rows);
    }

    private void createStageReport(List<GenericTermRelationship> invalidStages, String title, String parentStage, String childStage, String subject) {
        LOG.warn(title);
        List<List<String>> rows = new ArrayList<>(invalidStages.size());
        for (GenericTermRelationship pheno : invalidStages) {
            List<String> row = new ArrayList<>();
            row.add(pheno.getTermOne().getTermName());
            row.add(pheno.getTermTwo().getTermName());
            row.add(pheno.getTermOne().getStart().getName());
            row.add(pheno.getTermTwo().getStart().getName());
            rows.add(row);
        }
        emailStageReport(title, parentStage, childStage, subject, rows);
    }

    private void emailStageReport(String title, String parentStage, String childStage, String subject, List<List<String>> rows) {
        CronJobReport cronReport = new CronJobReport(report.getJobName());
        cronReport.setRows(rows);
        cronReport.appendToSubject(" - " + rows.size() + subject);
        cronReport.warning(title);
        cronJobUtil.addObjectToTemplateMap("domain", ZfinPropertiesEnum.DOMAIN_NAME.value());
        cronReport.addHeaderInfo(title);
        cronReport.addHeaderInfo(parentStage);
        cronReport.addHeaderInfo(childStage);
        cronJobUtil.emailReport("ontology-loader-invalid-stage-defintions.ftl", cronReport);
    }

    public boolean initialize(String fileName, CronJobUtil cronJobUtil) {
        return initialize(new CronJobReport("Load Ontology: " + fileName, cronJobUtil));
    }

    public boolean initialize(CronJobReport cronJobReport) {
        sectionTime = System.currentTimeMillis();
        this.report = cronJobReport;
        this.propertiesFile = "report.properties";
        this.dataDirectory = loadDirectory;
        clearReportDirectory();
        setReportProperties();

        try {
            createOboSession();
        } catch (OBOParseException e) {
            LOG.error("Error while parsing Obo file", e);
            report.error("Error while parsing Obo file");
            return false;
        } catch (IOException e) {
            LOG.error("No obo file found", e);
            report.error("Error while parsing Obo file");
            return false;
        }
        return true;
    }

    private void postLoadProcess() {
        // report annotations on obsoleted terms
        List<PhenotypeStatement> phenotypes = getMutantRepository().getPhenotypesOnObsoletedTerms(ontology);
        if (phenotypes != null && phenotypes.size() > 0) {
            LOG.warn("Pato annotations found that use obsoleted terms");
            List<List<String>> rows = new ArrayList<>(phenotypes.size());
            for (PhenotypeStatement pheno : phenotypes) {
                List<String> row = new ArrayList<>();
                row.add(pheno.getPhenotypeExperiment().getFigure().getPublication().getZdbID());
                row.add(pheno.getPhenotypeExperiment().getFigure().getPublication().getTitle());
                row.add(pheno.getDisplayName());
                Set<GenericTerm> obsoletedTerms = PhenotypeService.getObsoleteTerm(pheno);
                StringBuilder hyperlink = new StringBuilder();
                StringBuilder replaceLinks = new StringBuilder();
                StringBuilder considerLinks = new StringBuilder();
                for (GenericTerm obsoletedTerm : obsoletedTerms) {
                    hyperlink.append(TermPresentation.getLink(obsoletedTerm, true));
                    replaceLinks.append(getListOfHyperlinksOfReplacedByTerms(obsoletedTerm));
                    considerLinks.append(getListOfHyperlinksOfConsiderTerms(obsoletedTerm));
                }
                row.add(hyperlink.toString());
                row.add(replaceLinks.toString());
                row.add(considerLinks.toString());
                rows.add(row);
            }
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " annotations with obsoleted terms");
            cronReport.warning("Found phenotypes with obsoleted terms.");
            cronJobUtil.addObjectToTemplateMap("domain", ZfinPropertiesEnum.DOMAIN_NAME.value());
            cronJobUtil.emailReport("ontology-loader-obsolete-terms-used.ftl", cronReport);
        }
        // check if any secondary IDs are used in any expression annotation:
        List<ExpressionResult2> obsoletedTermsUsedExpression = RepositoryFactory.getExpressionRepository().getExpressionOnObsoletedTerms();
        if (obsoletedTermsUsedExpression != null && obsoletedTermsUsedExpression.size() > 0) {
            LOG.warn("Expression annotations found that use obsoleted term ids");
            StringBuffer buffer = new StringBuffer();
            String messageHeader = "Expression annotations found that use obsoleted term ids";
            buffer.append(messageHeader);
            report.addMessageToSection(messageHeader, "Post-Processing");
            List<List<String>> rows = new ArrayList<>(obsoletedTermsUsedExpression.size());
            for (ExpressionResult2 expressionResult : obsoletedTermsUsedExpression) {
                List<String> row = new ArrayList<>();
                row.add(expressionResult.getExpressionFigureStage().getExpressionExperiment().getPublication().getZdbID());
                row.add(expressionResult.getExpressionFigureStage().getExpressionExperiment().getPublication().getTitle());
                row.add(expressionResult.getEntity().getDisplayName());
                Set<GenericTerm> obsoletedTerms = expressionService.getObsoleteTerm(expressionResult);
                StringBuilder hyperlink = new StringBuilder();
                StringBuilder replaceLinks = new StringBuilder();
                StringBuilder considerLinks = new StringBuilder();
                for (GenericTerm obsoletedTerm : obsoletedTerms) {
                    hyperlink.append(TermPresentation.getLink(obsoletedTerm, true));
                    replaceLinks.append(getListOfHyperlinksOfReplacedByTerms(obsoletedTerm));
                    considerLinks.append(getListOfHyperlinksOfConsiderTerms(obsoletedTerm));
                }
                row.add(hyperlink.toString());
                row.add(replaceLinks.toString());
                row.add(considerLinks.toString());
                rows.add(row);
            }
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " expression annotations with obsolete terms");
            cronReport.warning("Found expressions with obsolete terms.");
            cronJobUtil.addObjectToTemplateMap("domain", ZfinPropertiesEnum.DOMAIN_NAME.value());
            cronJobUtil.emailReport("ontology-loader-obsolete-terms-used.ftl", cronReport);
        }
        // check if any secondary IDs are used in any annotation:
        List<PhenotypeStatement> secondaryTermsUsed = getMutantRepository().getPhenotypesOnSecondaryTerms();
        if (secondaryTermsUsed != null && secondaryTermsUsed.size() > 0) {
            LOG.warn("Pato annotations found that use secondary term ids");
            StringBuffer buffer = new StringBuffer();
            String messageHeader = "Pato annotations found that use secondary term ids";
            buffer.append(messageHeader);
            report.addMessageToSection(messageHeader, "Post-Processing");
            List<List<String>> rows = new ArrayList<>(secondaryTermsUsed.size());
            for (PhenotypeStatement pheno : secondaryTermsUsed) {
                List<String> row = new ArrayList<>();
                row.add(pheno.getPhenotypeExperiment().getFigure().getPublication().getZdbID());
                row.add(pheno.getPhenotypeExperiment().getFigure().getPublication().getTitle());
                row.add(pheno.getDisplayName());
                GenericTerm secondaryTerm = null;
                if (pheno.getEntity().getSuperterm().isSecondary()) {
                    secondaryTerm = pheno.getEntity().getSuperterm();
                } else if (pheno.getEntity().getSubterm() != null && pheno.getEntity().getSubterm().isSecondary()) {
                    secondaryTerm = pheno.getEntity().getSubterm();
                } else if (pheno.getQuality() != null && pheno.getQuality().isSecondary()) {
                    secondaryTerm = pheno.getQuality();
                } else if (pheno.getRelatedEntity() != null) {
                    PostComposedEntity entity = pheno.getRelatedEntity();
                    if (entity.getSuperterm() != null && entity.getSuperterm().isSecondary())
                        secondaryTerm = entity.getSuperterm();
                    if (entity.getSubterm() != null && entity.getSubterm().isSecondary())
                        secondaryTerm = entity.getSubterm();
                }
                row.add(secondaryTerm.getTermName());
                row.add(secondaryTerm.getOboID());
                rows.add(row);
            }
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " annotations with secondary terms");
            cronReport.warning("Found phenotypes with secondary terms.");
            cronJobUtil.emailReport("ontology-loader-secondary-terms-used.ftl", cronReport);
        }
        // check if any secondary IDs are used in any expression annotation:
        List<ExpressionResult> secondaryTermsUsedExpression = RepositoryFactory.getExpressionRepository().getExpressionOnSecondaryTerms();
        if (secondaryTermsUsedExpression != null && secondaryTermsUsedExpression.size() > 0) {
            LOG.warn("Expression annotations found that use secondary term ids");
            StringBuffer buffer = new StringBuffer();
            String messageHeader = "Expression annotations found that use secondary term ids";
            buffer.append(messageHeader);
            report.addMessageToSection(messageHeader, "Post-Processing");
            List<List<String>> rows = new ArrayList<>(secondaryTermsUsedExpression.size());
            for (ExpressionResult expressionResult : secondaryTermsUsedExpression) {
                List<String> row = new ArrayList<>();
                row.add(expressionResult.getExpressionExperiment().getPublication().getZdbID());
                row.add(expressionResult.getExpressionExperiment().getPublication().getTitle());
                row.add(expressionResult.getEntity().getDisplayName());
                GenericTerm secondaryTerm = null;
                if (expressionResult.getEntity().getSuperterm().isSecondary()) {
                    secondaryTerm = expressionResult.getEntity().getSuperterm();
                } else if (expressionResult.getEntity().getSubterm() != null && expressionResult.getEntity().getSubterm().isSecondary()) {
                    secondaryTerm = expressionResult.getEntity().getSubterm();
                }
                row.add(secondaryTerm.getTermName());
                row.add(secondaryTerm.getOboID());
                rows.add(row);
            }
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " expression annotations with secondary terms");
            cronReport.warning("Found expressions with secondary terms.");
            cronJobUtil.emailReport("ontology-loader-secondary-terms-used.ftl", cronReport);
        }
        // missing terms with OBO id report.
        if (dataMapHasValues(UnloadFile.TERMS_MISSING_OBO_ID)) {
            List<List<String>> rows = dataMap.get(UnloadFile.TERMS_MISSING_OBO_ID.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " missing OBO ID");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-terms-missing-obo-id.ftl", cronReport);
        }
        // missing terms with OBO id report.
        if (dataMapHasValues(UnloadFile.TERMS_UN_OBSOLETED_ID)) {
            List<List<String>> rows = dataMap.get(UnloadFile.TERMS_UN_OBSOLETED_ID.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " terms are un-obsoleted");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-terms-un-obsoleted.ftl", cronReport);
        }
        // updated terms report.
        if (dataMapHasValues(UnloadFile.SEC_UNLOAD_REPORT)) {
            List<List<String>> rows = dataMap.get(UnloadFile.SEC_UNLOAD_REPORT.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " merged " + termChoice.format(rows.size()) + " updated");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-secondary-terms-used.ftl", cronReport);
        }
        createAllReportFiles();
        //unloadData();
        updatePhenotypesReport();
        updateExpressionReport();
    }

    private void createAllReportFiles() {
        if (CollectionUtils.isEmpty(dataMap.keySet())) {
            return;
        }

        // check every key from the data map to see if there is a report defined.
        for (String key : dataMap.keySet()) {
            if (dataMapHasValues(key)) {
                ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, loadDirectory, key, true);
                createErrorReport(null, dataMap.get(key), reportConfiguration);
            }
        }
        ReportGenerator stats = new ReportGenerator();
        stats.setReportTitle("Report for " + jobName);
        stats.includeTimestamp();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("New Terms", getDataSize("new_terms"));
        summary.put("Updated Term Names", getDataSize("updated_term_names"));
        summary.put("New Definitions", getDataSize("new_term_definitions"));
        summary.put("Updated Term Definitions", getDataSize("updated_definitions"));
        summary.put("Updated Term Comments", getDataSize("updated_term_comments"));
        summary.put("New Aliases", getDataSize("new_aliases"));
        summary.put("Removed Aliases", getDataSize("removed_aliases"));
        summary.put("New Relationships", getDataSize("new_relationships"));
        summary.put("Removed Relationships", getDataSize("removed_relationships"));
        summary.put("Obsoleted Terms", getDataSize("obsoleted_terms"));
        summary.put("New DBXrefs", getDataSize("new_xrefs"));
        summary.put("Removed DBXrefs", getDataSize("removed_xrefs"));
        summary.put("Updated Merged Terms", getDataSize("zeco_updates"));
        stats.addSummaryTable("Statistics", summary);
        stats.writeFiles(new File(loadDirectory, jobName), "statistics");
    }

    @Override
    protected void addCustomVariables(Map<String, Object> map) {
        for (String key : dataMap.keySet()) {
            if (dataMapHasValues(key)) {
                if (key != null && key.endsWith("_count")) {
                    map.put(key + "s", getSingleValue(dataMap.get(key)));
                }
            }
        }
        map.putAll(dataMap);
    }

    private Object getSingleValue(List<List<String>> lists) {
        if (lists == null)
            return null;
        for (List<String> list : lists) {
            if (list != null && list.size() == 1)
                return list.get(0);
        }
        return null;
    }

    private int getDataSize(String key) {
        List<List<String>> data = dataMap.get(key);
        return data == null ? 0 : data.size();
    }

    private void unloadData() {
        openAllFiles();
        writeToFiles();
        closeAllFiles();
    }

    private void writeToFiles() {
        for (String key : dataMap.keySet()) {
            if (!key.endsWith("txt"))
                continue;
            if (dataMapHasValues(key)) {
                List<List<String>> list = dataMap.get(key);
                //writeToFile(key, list);
            }
        }
    }

    private boolean dataMapHasValues(UnloadFile unloadFile) {
        if (unloadFile == null)
            return false;
        String value = unloadFile.getValue();
        if (StringUtils.isEmpty(value))
            return false;
        List<List<String>> list = dataMap.get(unloadFile.getValue());
        return dataMapHasValues(list);
    }

    private boolean dataMapHasValues(String unloadFileName) {
        if (unloadFileName == null)
            return false;
        if (StringUtils.isEmpty(unloadFileName))
            return false;
        List<List<String>> list = dataMap.get(unloadFileName);
        return dataMapHasValues(list);
    }

    private boolean dataMapHasValues(List<List<String>> list) {
        return list != null && CollectionUtils.isNotEmpty(list);
    }

    private List<GenericTermRelationship> createRelationshipList(List<List<String>> relationships) {
        List<GenericTermRelationship> relationshipList = new ArrayList<>();
        if (relationships != null) {
            for (List<String> listID : relationships) {
                GenericTermRelationship relationship = new GenericTermRelationship();
                relationship.setZdbId(listID.get(0));
                relationship.setType(listID.get(3));
                relationship.setTermOne(getOntologyRepository().getTermByZdbID(listID.get(1)));
                relationship.setTermTwo(getOntologyRepository().getTermByZdbID(listID.get(2)));
                relationshipList.add(relationship);
            }
        }
        return relationshipList;
    }

    private StringBuilder getListOfHyperlinksOfConsiderTerms(GenericTerm obsoletedTerm) {
        List<ConsiderTerm> considerTerms = RepositoryFactory.getOntologyRepository().getConsiderTerms(obsoletedTerm);
        StringBuilder considerBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(considerTerms)) {
            for (ConsiderTerm term : considerTerms) {
                String hyperlink = TermPresentation.getLink(term.getConsiderTerm(), true);
                considerBuilder.append(hyperlink);
                considerBuilder.append(", ");
            }
            considerBuilder.deleteCharAt(considerBuilder.length() - 1);
            considerBuilder.deleteCharAt(considerBuilder.length() - 1);
        }
        return considerBuilder;
    }

    private StringBuilder getListOfHyperlinksOfReplacedByTerms(GenericTerm obsoletedTerm) {
        List<ReplacementTerm> replacedByTerms = RepositoryFactory.getOntologyRepository().getReplacedByTerms(obsoletedTerm);
        StringBuilder builder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(replacedByTerms)) {
            LOG.info("creating replaced by Term list");
            for (ReplacementTerm term : replacedByTerms) {
                String hyperlink = TermPresentation.getLink(term.getReplacementTerm(), true);
                builder.append(hyperlink);
                builder.append(", ");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder;
    }

    private void updateExpressionReport() {
        // secondary terms replaced report.
        replacedTerms(dataMap.get(UnloadFile.EXPRESSION_SUPERTERM_UPDATES.getValue()), "Super term", "Expression");
        replacedTerms(dataMap.get(UnloadFile.EXPRESSION_SUBTERM_UPDATES.getValue()), "Sub term", "Expression");
    }

    private void updatePhenotypesReport() {
        // secondary terms replaced report.
        replacedTerms(dataMap.get(UnloadFile.PHENOTYPE_SUPERTERM_UPDATES.getValue()), "Entity Super term", "Phenotype");
        replacedTerms(dataMap.get(UnloadFile.PHENOTYPE_SUBTERM_UPDATES.getValue()), "Entity Sub term", "Phenotype");
        replacedTerms(dataMap.get(UnloadFile.PHENOTYPE_SUPERTERM_UPDATES.getValue()), "Related Entity Super term", "Phenotype");
        replacedTerms(dataMap.get(UnloadFile.PHENOTYPE_SUBTERM_UPDATES.getValue()), "Related Entity Sub term", "Phenotype");
        replacedTerms(dataMap.get(UnloadFile.PHENOTYPE_QUALITY_UPDATES.getValue()), "Quality term", "Phenotype");
    }

    private void replacedTerms(List<List<String>> data, String termType, String expressionPhenotype) {
        if (dataMapHasValues(data)) {
            String message = expressionPhenotype + " Superterms replaced: " + data.size();
            LOG.info(message);
            report.addMessageToSection(message, termType + " replaced by merged term:");
            report.setRows(data);
            report.appendToSubject(expressionPhenotype + ": Replaced " + data.size() + " records " +
                    " for " + ontology.getOntologyName());
            report.setDataSectionTitle(message);
            cronJobUtil.emailReport("ontology-loader-report-replaced-terms.ftl", report);
        }
    }

    private InfrastructureRepository infrastructureRep = RepositoryFactory.getInfrastructureRepository();

    private void runDbScriptFile(String dbScriptFile) {
        File file = new File(dbScriptFile);
        if (!file.exists()) {
            LOG.error("Could not find script file " + file.getAbsolutePath());
            return;
        }
        writeToTraceFile("Executing script file: " + dbScriptFile);

        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        if (!LOG.isDebugEnabled())
            LOG.info("No Debugging enabled: To see more debug data enable the logger to log level debug.");
        for (DatabaseJdbcStatement statement : queries) {
            LOG.info("Statement " + statement.getLocationInfo() + ": " + statement.getHumanReadableQueryString());
            if (statement.isInformixWorkStatement())
                continue;
            if (statement.isLoadStatement()) {
                List<List<String>> data = dataMap.get(statement.getDataKey());
                if (data == null) {
                    LOG.info("No data found for key: " + statement.getDataKey());
                    continue;
                }
                DatabaseJdbcStatement modifiedStatement = statement.completeInsertStatement(data.get(0).size());
                infrastructureRep.executeJdbcStatement(modifiedStatement, data);
                runDebugStatement(statement);
                LOG.info(data.size() + " records inserted");
            } else if (statement.isSingleLoadStatement()) {
                List<List<String>> data = dataMap.get(statement.getDataKey());
                if (data == null) {
                    LOG.info("No data found for key: " + statement.getDataKey());
                    continue;
                }
                statement.completeInsertStatement(data.get(0).size());
                infrastructureRep.executeJdbcStatementOneByOne(statement, data);
                LOG.info(data.size() + " records inserted");
            } else if (statement.isDebug()) {
                List<List<String>> dataReturn;
                if (LOG.isDebugEnabled()) {
                    dataReturn = infrastructureRep.executeNativeQuery(statement);
                    if (dataReturn == null)
                        LOG.debug("  Debug data: No records found.");
                    else {
                        LOG.debug("  Debug data: " + dataReturn.size() + " records.");
                        for (List<String> row : dataReturn)
                            LOG.debug("  " + row);
                    }
                }
            } else if (statement.isSelectStatement()) {
                List<List<String>> dataReturn;
                dataReturn = infrastructureRep.executeNativeQuery(statement);
                writeToTraceFile(statement, dataReturn);
                if (dataReturn == null) {
                    LOG.info("  Debug data: No records found.");
                } else if (statement.getDataKey() != null && statement.getDataKey().toUpperCase().equals(DatabaseJdbcStatement.DEBUG)) {
                    LOG.info("  Debug data: " + dataReturn.size() + " records.");
                    for (List<String> row : dataReturn)
                        LOG.info("  " + row);
                } else {
                    List<List<String>> existingData = dataMap.get(statement.getDataKey());
                    if (existingData != null)
                        addDataToList(existingData, dataReturn);
                    else
                        dataMap.put(statement.getDataKey(), dataReturn);
                }
            } else if (statement.isComment()) {
                writeToTraceFile(statement.getComment());
            } else if (statement.isEcho()) {
                LOG.info(statement.getQuery());
                writeToTraceFile(statement.getComment());
            } else {
                if (statement.isInsertStatement() || statement.isDeleteStatement()) {
                    runDebugStatement(statement);
                }
                int affectedRows = infrastructureRep.executeJdbcStatement(statement);
                if (statement.isDeleteStatement()) {
                    writeToTraceFile("Deleted Rows: " + affectedRows);
                    writeToTraceFile("After Delete Statement: ");
                    runDebugStatementAfterDelete(statement);
                }
            }
            writeToTraceFile("**************************************************************************************");
            writeToTraceFile("");
            DbSystemUtil.logLockInfo();
        }
    }

    private void addDataToList(List<List<String>> existingData, List<List<String>> newData) {
        if (existingData == null || newData == null)
            return;
        for (List<String> elements : newData)
            existingData.add(elements);
    }

    private void runDebugStatement(DatabaseJdbcStatement statement) {
        if (debugMode == false)
            return;
        try {
            DatabaseJdbcStatement debugStatement = statement.getDebugStatement();
            List<List<String>> dataReturn = infrastructureRep.executeNativeQuery(debugStatement);
            writeToTraceFile(debugStatement, dataReturn);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void runDebugStatementAfterDelete(DatabaseJdbcStatement statement) {
        if (debugMode == false)
            return;
        try {
            DatabaseJdbcStatement debugStatement = statement.getDebugDeleteStatement();
            List<List<String>> dataReturn = infrastructureRep.executeNativeQuery(debugStatement);
            writeToTraceFile(debugStatement, dataReturn, false);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void writeToTraceFile(DatabaseJdbcStatement statement, List<List<String>> rows) {
        writeToTraceFile(statement, rows, true);
    }

    private void writeToTraceFile(DatabaseJdbcStatement statement, List<List<String>> rows, boolean showLocation) {
        if (statement.isDebugStatement()) {
            int numberOfRows = 0;
            if (CollectionUtils.isNotEmpty(rows))
                numberOfRows = rows.size();
            if (showLocation) {
                writeToTraceFile(statement.getLocationInfo() + " " + FileUtil.getFileNameFromPath(statement.getScriptFile()));
                writeToTraceFile(statement.getComment());
                writeToTraceFile("(" + numberOfRows + ")");
                if (statement.getParentStatement() != null)
                    writeToTraceFileIndendented(statement.getParentStatement().getHumanReadableQueryString());
            } else {
                writeToTraceFileIndendented(statement.getHumanReadableQueryString());
                writeToTraceFileIndendented(" No. of Records: " + numberOfRows);
            }
            if (numberOfRows == 0)
                writeToTraceFileIndendented("NONE");
            else {
                writeToTraceFile(rows);
            }
        }
    }

    /*
        private void writeToFile(String fileName, List<List<String>> rows) {
            if (CollectionUtils.isEmpty(rows))
                return;
            for (List<String> row : rows) {
                StringBuilder builder = new StringBuilder();
                for (String column : row) {
                    builder.append(column);
                    builder.append(",");
                }
                builder.append("\n");
                appendRecord(fileName, builder.toString());
            }
        }

    */
    private void writeToTraceFile(List<List<String>> rows) {
        if (CollectionUtils.isEmpty(rows))
            return;
        int maxNumber = 10;
        int index = 0;
        for (List<String> row : rows) {
            if (index >= maxNumber) {
                writeToFile("... " + rows.size() + " total records");
                break;
            }
            StringBuilder builder = new StringBuilder();
            for (String column : row) {
                builder.append(column);
                builder.append("|");
            }
            builder.append("\n");
            writeToFile(builder.toString());
            index++;
        }
        writeToFile("\n");
    }

    private void writeToTraceFile(String comment) {
        if (StringUtils.isEmpty(comment))
            return;
        writeToFile(comment);
        writeToFile("\n");
    }

    private void writeToTraceFileIndendented(String comment) {
        if (StringUtils.isEmpty(comment))
            return;
        comment = comment.replace("\n", "\n          ");
        writeToFile(comment);
        writeToFile("\n");
    }

    private void writeToFile(String string) {
        try {
            traceFileWriter.write(string);
            traceFileWriter.flush();
        } catch (IOException e) {
            LOG.error("Could not write to trace file: ");
        }
    }

    private boolean processOboFile() {
        if (!newerVersionFound() && productionMode) {
            String message = ontology.getOntologyName() + " ontology in the database is up-to-date. \n";
            message += oboMetadata.toString();
            LOG.info(message);
            report.addMessageToSection(message, "Check file version");
            return false;
        }
        parseOboFile();
        return true;
    }

    private void parseOboFile() {
        int numberOfTerms = 0;
        for (IdentifiedObject obj : oboSession.getObjects()) {
            if (obj instanceof OBOClass) {
                OBOClass term = (OBOClass) obj;
                if (!term.getID().startsWith("obo:")) {
                    numberOfTerms++;
                    pushToParsedTerms(term);
                    if (term.isObsolete()) {
                        appendFormattedRecord(UnloadFile.TERM_OBSOLETE, term.getID());
                    }
                    if (term.getSecondaryIDs() != null) {
                        for (String secondaryID : term.getSecondaryIDs())
                            appendFormattedRecord(UnloadFile.TERM_SECONDARY, term.getID(), secondaryID);
                    }
                    RelationshipsValidator validator = new RelationshipsValidator(term.getID());
                    for (Link parentTerm : term.getParents()) {
                        String type = parentTerm.getType().getName();
                        String parentTermID = parentTerm.getParent().getID();
                        validator.addParentRelationship(parentTermID, type);
                        if (!validator.isValidParentRelationshipUnit())
                            throw new RuntimeException(validator.getErrorMessage());
                        appendFormattedRecord(UnloadFile.TERM_RELATIONSHIPS, parentTermID, term.getID(), type);
                    }
                    if (term.getReplacedBy() != null) {
                        for (org.obo.datamodel.ObsoletableObject replacedByID : term.getReplacedBy())
                            appendFormattedRecord(UnloadFile.TERM_REPLACED, replacedByID.getID(), term.getID(), "replaced_by");
                    }
                    if (term.getSynonyms() != null) {
                        Set<String> synonymKeySet = new HashSet<>(5);
                        for (Synonym synonym : term.getSynonyms()) {
                            boolean isChebiSynonym = false;
                            if (ontology.equals(Ontology.CHEBI)) {
                                if (synonym.getXrefs() != null) {
                                    for (Dbxref ref : synonym.getXrefs()) {
                                        if (ref.getDatabase().toLowerCase().startsWith("chebi") )
                                            isChebiSynonym = true;
                                    }
                                }
                            }
                            // don't create synonyms for entries dbxrefs of type chebi:! they have huge strings....
                            if(isChebiSynonym)
                                continue;
                            // Make sure not to create distinct synonyms per these three keys.
                            String synonymText =synonym.getText().trim().replaceAll(" +"," ");
                            if(synonymText.length() > 255)
                                synonymText = synonymText.substring(0,255);
                            String synonymKey = term.getID() + ":" + synonymText;
                            if (synonymKeySet.contains(synonymKey))
                                continue;
                            synonymKeySet.add(synonymKey);
                            if (synonym.getSynonymType() != null)
                                appendSynonym(UnloadFile.TERM_SYNONYMS, term.getID(), synonym.getScope(), synonymText, synonym.getSynonymType().getName());
                            else
                                appendSynonym(UnloadFile.TERM_SYNONYMS, term.getID(), synonym.getScope(), synonymText, "");
                        }
                    }
                    if (term.getConsiderReplacements() != null) {
                        for (org.obo.datamodel.ObsoletableObject considerObject : term.getConsiderReplacements())
                            appendFormattedRecord(UnloadFile.TERM_CONSIDER, term.getID(), considerObject.getID(), "consider");
                       }
                    if (term.getDbxrefs() != null) {
                        for (Dbxref dbxref : term.getDbxrefs()) {
                            //  appendFormattedRecord(UnloadFile.TERM_XREF, term.getID(), dbxref.getDatabase(), dbxref.getDatabaseID(), "xref");
                            appendFormattedRecord(UnloadFile.TERM_XREF, term.getID(), dbxref.getDatabase(), dbxref.getDatabaseID(), "xref");
                        }
                    }
                    if (term.getSubsets() != null) {
                        for (TermSubset subset : term.getSubsets()) {
                            appendFormattedRecord(UnloadFile.TERM_SUBSET, term.getID(), subset.getName(), "subset");
                        }
                    }
                }
            }
        }
        if (oboSession.getSubsets() != null) {
            for (TermSubset term : oboSession.getSubsets())
                appendFormattedRecord(UnloadFile.SUBSETDEFS_HEADER, oboSession.getDefaultNamespace().getID(), term.getName(), term.getDesc(), "subsetdefs");
        }
        if (oboSession.getSynonymTypes() != null) {
            for (SynonymType synonymType : oboSession.getSynonymTypes()) {
                appendFormattedRecord(UnloadFile.SYNTYPEDEFS_HEADER, oboSession.getDefaultNamespace().getID(), synonymType.getID(), synonymType.getName(), getSynonymDescriptor(synonymType.getScope()), "syntypedefs");
            }
        }
        String message = "Number of Terms in obo file: " + numberOfTerms;
        LOG.info(message);
        report.addMessageToSection(message, "Header");
    }

    @Override
    public int execute() {
        return 0;
    }

    class RelationshipsValidator {

        private String childId;
        // <parentID, relationshipType>
        private Map<String, String> parentRelationshipMap = new HashMap<>(4);
        // relationship types
        private List<String> relationshipTypeList = new ArrayList<>(4);

        RelationshipsValidator(String childId) {
            this.childId = childId;
        }

        protected void addParentRelationship(String termID, String type) {
            parentRelationshipMap.put(termID, type);
            relationshipTypeList.add(type);
        }

        protected boolean isValidParentRelationshipUnit() {
            if (hasMoreThanOneType("start stage"))
                return false;
            if (hasMoreThanOneType("end stage"))
                return false;
            return true;
        }

        private boolean hasMoreThanOneType(String relationshipType) {
            boolean hasType = false;
            for (String type : relationshipTypeList) {
                if (type.equals(relationshipType)) {
                    if (hasType)
                        return true;
                    hasType = true;
                }
            }
            return false;
        }

        protected String getErrorMessage() {
            if (hasMoreThanOneType("start stage"))
                return "Term " + childId + " has more than one start stage:";
            if (hasMoreThanOneType("end stage"))
                return "Term " + childId + " has more than one end stage:";
            return "";
        }
    }


    private void appendSynonym(UnloadFile unloadFile, String id, int scope, String text, String synonymType) {
        String scopeString = getSynonymDescriptor(scope);
        appendFormattedRecord(unloadFile, id, text, scopeString, "[]", "synonym", synonymType);
    }

    private String getSynonymDescriptor(int scope) {
        String type = null;
        switch (scope) {
            case 0:
                type = "RELATED";
                break;
            case 1:
                type = "EXACT";
                break;
            case 2:
                type = "NARROW";
                break;
            case 3:
                type = "BROAD";
                break;
            default:
                type = "UNKNOWN SYNONYM";
        }
        return type;
    }

    private void appendRelationship(UnloadFile unloadFile, String idOne, String idTwo, String relationshipName) {
        int indexOfArrow = idOne.indexOf("->");
        String convertFirstId = idOne.substring(indexOfArrow + 2);
        appendFormattedRecord(unloadFile, convertFirstId, idTwo, relationshipName);
    }

    private void pushToParsedTerms(OBOClass term) {
        String obsolete = "";
        if (term.isObsolete())
            obsolete = "t";
        if (StringUtils.isEmpty(term.getName())) {
            throw new InvalidOBOFileException("Term with id " + term.getID() + " has no name attribute");
        }
        appendFormattedRecord(UnloadFile.TERM_PARSED, term.getID(),
                term.getName(), term.getNamespace().getID(), term.getDefinition(), term.getComment(), obsolete);
        if (term.getDefDbxrefs() != null) {
            for (Dbxref xref : term.getDefDbxrefs()) {
                // remove non-printable characters
                String databaseID = xref.getDatabaseID().replaceAll("\\p{C}", "");
                // replace the en dash '\u2013', –, with a regular hyphen
                databaseID = databaseID.replace("\u2013", "-");
                // replace the em dash '\u2014', –, with a regular hyphen
                databaseID = databaseID.replace("\u2014", "-");
                appendFormattedRecord(UnloadFile.TERM_REFERENCES, term.getID(), xref.getDatabase(), databaseID);
            }
        }
    }

    private void createOboSession() throws OBOParseException, IOException {
        DefaultOBOParser oboParser = new DefaultOBOParser();
        AbstractParseEngine engine = new OBOParseEngine(oboParser);
        // GOBOParseEngine can parse several files at once
        // and create one munged-together ontology,
        // so we need to provide a Collection to the setPaths() method
        Collection<String> paths = new LinkedList<>();
        paths.add(oboFilename);
        engine.setPaths(paths);
        engine.parse();
        oboSession = oboParser.getSession();
    }

    /**
     * Check if the current obo file is a newer version compared to the version in the database.
     *
     * @return false or true
     */
    private boolean newerVersionFound() {
        String ontologyName = oboSession.getDefaultNamespace().getID();
        ontology = Ontology.getOntology(ontologyName);
        if (ontology == null)

            throw new RuntimeException();

        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        OntologyMetadata dbMetadata = ontologyRepository.getOntologyMetadata(ontologyName);
        oboMetadata = getMetadataFromOboFile();
        if (dbMetadata == null) {
            int maxOrderNumber = ontologyRepository.getMaxOntologyOrderNumber();
            oboMetadata.setOrder(maxOrderNumber + 1);
            HibernateUtil.currentSession().save(oboMetadata);
            HibernateUtil.currentSession().flush();
            LOG.info("New Ontology: " + oboMetadata.toString());
            return true;
        }
        boolean sameVersion = dbMetadata.equals(oboMetadata);
        if (!sameVersion) {
            LOG.info("New ontology found");
            LOG.info("New Version    : " + oboMetadata.toString());
            LOG.info("Current Version: " + dbMetadata.toString());
            // update version number on all namespaces found in this obo file.
            updateMetadata(dbMetadata, oboMetadata);
            HibernateUtil.currentSession().flush();
        } else {
            LOG.info("Current Version: " + dbMetadata.toString());
        }
        appendFormattedRecord(UnloadFile.ONTOLOGY_HEADER, oboMetadata.getOboVersion(), oboMetadata.getDataVersion(),
                oboMetadata.getDate(), oboMetadata.getSavedBy(), oboMetadata.getGeneratedBy(),
                oboMetadata.getDefaultNamespace(), oboMetadata.getRemark());
        return !sameVersion;
    }

    private void updateMetadata(OntologyMetadata dbMetadata, OntologyMetadata newMetadata) {
        dbMetadata.setDataVersion(newMetadata.getDataVersion());
        dbMetadata.setDate(newMetadata.getDate());
        dbMetadata.setDefaultNamespace(newMetadata.getDefaultNamespace());
        dbMetadata.setGeneratedBy(newMetadata.getGeneratedBy());
        dbMetadata.setOboVersion(newMetadata.getOboVersion());
        dbMetadata.setRemark(newMetadata.getRemark());
        dbMetadata.setSavedBy(newMetadata.getSavedBy());
    }

    private OntologyMetadata getMetadataFromOboFile() {
        OntologyMetadata metadata = new OntologyMetadata();
        SessionHistoryList header = oboSession.getCurrentHistory();
        metadata.setDataVersion(header.getVersion());
        metadata.setSavedBy(header.getUser());
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        metadata.setDate(df.format(header.getDate()));
        metadata.setDefaultNamespace(oboSession.getDefaultNamespace().getID());
        metadata.setName(oboSession.getDefaultNamespace().getID());
        String revision = getRevisionFromComment(header.getComment());
        metadata.setDataVersion(revision);
        return metadata;
    }

    /**
     * Check for revision from gene_ontology file which is
     * part of the comment field
     * e.g.: cvs version: $Revision: 1.1416 $
     *
     * @param comment string
     * @return revision number
     */
    public static String getRevisionFromComment(String comment) {
        if (comment == null)
            return null;
        String revName = "$Revision: ";
        int startOfRevision = comment.indexOf(revName);
        if (startOfRevision == -1)
            return null;
        String revision = comment.substring(startOfRevision + revName.length());
        int endOfRevision = revision.indexOf("$");
        return revision.substring(0, endOfRevision).trim();
    }

    public void setLogger(Logger log) {
        LOG = log;
    }

    enum UnloadFile {
        REMOVED_RELATIONSHIPS_1("deleted_relationships_1.unl"),
        REMOVED_RELATIONSHIPS_2("deleted_relationships_2.unl"),
        TERM_PARSED("term_parsed.unl"),
        TERM_CONSIDER("term_consider.unl"),
        TERM_RELATIONSHIPS("term_relationships.unl"),
        TERM_REPLACED("term_replaced.unl"),
        TERM_SECONDARY("term_secondary.unl"),
        SECONDARY_TERMS_USED("secondary_terms_used.unl"),
        TERM_OBSOLETE("term_obsolete.unl"),
        TERM_SYNONYMS("term_synonyms.unl"),
        TERM_SUBSET("term_subset.unl"),
        TERM_XREF("term_xref.unl"),
        ONTOLOGY_HEADER("ontology_header.unl"),
        TERMS_MISSING_OBO_ID("terms_missing_obo_id.txt"),
        TERMS_UN_OBSOLETED_ID("terms_un_obsoleted.txt"),
        NEW_TERMS("new_terms.unl"),
        UPDATED_TERMS("updated_terms.unl"),
        TERM_REFERENCES("term_references.unl"),
        EXPRESSION_SUPERTERM_UPDATES("expression-superterm-updates.unl"),
        EXPRESSION_SUBTERM_UPDATES("expression-subterm-updates.unl"),
        PHENOTYPE_SUPERTERM_UPDATES("phenotype-superterm-updates.unl"),
        PHENOTYPE_SUBTERM_UPDATES("phenotype-subterm-updates.unl"),
        PHENOTYPE_RELATED_ENITTY_SUPERTERM_UPDATES("phenotype-related-entity-superterm-updates.unl"),
        PHENOTYPE_RELATED_ENITTY_SUBTERM_UPDATES("phenotype-related-enityt-subterm-updates.unl"),
        PHENOTYPE_QUALITY_UPDATES("phenotype-quality-updates.unl"),
        SYNTYPEDEFS_HEADER("syntypedefs_header.unl"),
        SEC_UNLOAD_REPORT("sec_unload_report.unl"),
        SEC_UNLOAD("sec_unload.unl"),
        SUBSETDEFS_HEADER("subsetdefs_header.unl");
        private String value;

        UnloadFile(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    private Map<UnloadFile, FileWriter> fileWriters = new HashMap<>(UnloadFile.values().length);
    private FileWriter traceFileWriter;

    private void appendFormattedRecord(UnloadFile unloadFile, String... record) {
        //appendRecord(unloadFile, InformixUtil.getUnloadRecord(record));
        List<List<String>> data = dataMap.get(unloadFile.getValue());
        if (data == null) {
            data = new ArrayList<>();
        }

        List<String> individualRecord;
        individualRecord = new ArrayList<>(record.length);
        individualRecord.addAll(Arrays.asList(record));
        data.add(individualRecord);
        dataMap.put(unloadFile.getValue(), data);
    }

    private void appendSpecialFormattedRecord(UnloadFile unloadFile, String firstString, String secondString, String thir) {
        //appendRecord(unloadFile, InformixUtil.getUnloadRecord(record));
        List<List<String>> data = dataMap.get(unloadFile.getValue());
        if (data == null) {
            data = new ArrayList<>();
        }
        List<String> individualRecord = new ArrayList<>(2);
        individualRecord.add(firstString);
        individualRecord.add(secondString);
        data.add(individualRecord);
        dataMap.put(unloadFile.getValue(), data);
    }

    private void appendRecord(UnloadFile unloadFile, String record) {
        try {
            fileWriters.get(unloadFile).write(record);
        } catch (IOException e) {
            LOG.error("Could not write to file " + unloadFile.getValue());
        }
    }

    private void openAllFiles() {
        for (UnloadFile unloadFile : UnloadFile.values()) {
            File file = new File(unloadFile.getValue());
            try {
                FileWriter fileWriter = new FileWriter(file);
                fileWriters.put(unloadFile, fileWriter);
            } catch (IOException e) {
                LOG.error("Could not open file " + file.getAbsolutePath());
            }
        }
    }

    private void openTraceFile() {
        String traceFileName = "trace-" + FileUtil.getFileNameFromPath(oboFilename) + ".log";
        File file = new File(loadDirectory, "logs");
        file = new File(file, traceFileName);
        try {
            LOG.info("Opening trace file: " + file.getAbsolutePath());
            traceFileWriter = new FileWriter(file);
        } catch (IOException e) {
            LOG.error("Could not open file " + file.getAbsolutePath());
        }
    }

    private void closeAllFiles() {
        if (fileWriters == null)
            return;

        for (UnloadFile unloadFile : fileWriters.keySet()) {
            try {
                fileWriters.get(unloadFile).close();
            } catch (IOException e) {
                LOG.error("Could not close file " + unloadFile.getValue());
            }
        }
    }

    private void closeTraceFile() {
        if (traceFileWriter == null)
            return;

        try {
            traceFileWriter.close();
        } catch (IOException e) {
            LOG.error("Could not close file " + traceFileWriter.toString());
        }
    }

    public CronJobReport getReport() {
        return report;
    }
}
