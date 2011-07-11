package org.zfin.ontology.datatransfer.service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.collections.CollectionUtils;
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
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DateUtil;
import org.zfin.util.DbScriptFileParser;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.util.*;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.*;


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
public class LoadOntology extends AbstractScriptWrapper {

    @Autowired
    private ExpressionService expressionService ;

    private static Logger LOG;

    static {
        options.addOption(oboFileNameOption);
        options.addOption(log4jFileOption);
        options.addOption(dbScriptFileOption);
        options.addOption(webrootDirectory);
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
    private Map<String, List<List<String>>> dataMap = new HashMap<String, List<List<String>>>(20);
    private OntologyMetadata oboMetadata;
    private ChoiceFormat termChoice = new ChoiceFormat("0#terms| 1#term| 2#terms");

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

    public LoadOntology(String oboFile, String propertyDirectory, String... scriptFiles) throws IOException {
        initializeLoad(oboFile, scriptFiles);
        if (propertyDirectory == null)
            initAll();
        else
            initAll(propertyDirectory + "/WEB-INF/zfin.properties");
        if (propertyDirectory == null)
            throw new RuntimeException("No property file found.");
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue(propertyDirectory);
        cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
    }

    private void initializeLoad(String oboFile, String[] scriptFiles) throws IOException {
        oboFilename = oboFile;
        this.dbScriptFiles = scriptFiles;
        if (!FileUtil.checkFileExists(oboFilename))
            throw new IOException("No OBO file <" + oboFile + "> found. You may have to download the obo file first.");
        if (dbScriptFiles != null) {
            for (String dbScriptFile : dbScriptFiles)
                if (!FileUtil.checkFileExists(dbScriptFile))
                    throw new IOException("No DB script file <" + dbScriptFile + "> found!");
        }
    }

    public static void main(String[] arguments) {
        LOG = Logger.getLogger(LoadOntology.class);
        LOG.info("Start Ontology Loader class: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
        initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        String oboFile = commandLine.getOptionValue(oboFileNameOption.getOpt());
        String dbScriptFilesNames = commandLine.getOptionValue(dbScriptFileOption.getOpt());
        String propertyFileName = commandLine.getOptionValue(webrootDirectory.getOpt());
        String[] dbScriptFiles = dbScriptFilesNames.split(",");
        LOG.info("Loading obo file: " + oboFile);

        LoadOntology loader = null;
        try {
            loader = new LoadOntology(oboFile, propertyFileName, dbScriptFiles);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.exit(-1);
        }
        LOG.info("Property: " + ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL.value());
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        if (loader.initialize(oboFile, cronJobUtil))
            loader.runOntologyUpdateProcess();
    }

    public void runOntologyUpdateProcess() {
        //ontologyLoader.openAllFiles();
        if (processOboFile()) {
            try {
                HibernateUtil.createTransactionWithLowPDQ();
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
                HibernateUtil.flushAndCommitCurrentSessionWithLowPdq();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                LOG.error(e);
                report.error(e);
            } finally {
                HibernateUtil.closeSession();
            }
            postLoadProcess();
        }
        LOG.info("Total Execution Time: " + DateUtil.getTimeDuration(sectionTime));
        //ontologyLoader.closeAllFiles();
    }

    public boolean initialize(String fileName, CronJobUtil cronJobUtil) {
        return initialize(new CronJobReport("Load Ontology: " + fileName, cronJobUtil));
    }

    public boolean initialize(CronJobReport cronJobReport) {
        sectionTime = System.currentTimeMillis();
        this.report = cronJobReport;
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
        TermPresentation.domain = "http://" + ZfinPropertiesEnum.DOMAIN_NAME;
        List<PhenotypeStatement> phenotypes = RepositoryFactory.getMutantRepository().getPhenotypesOnObsoletedTerms();
        if (phenotypes != null && phenotypes.size() > 0) {
            LOG.warn("Pato annotations found that use obsoleted terms");
            List<List<String>> rows = new ArrayList<List<String>>(phenotypes.size());
            for (PhenotypeStatement pheno : phenotypes) {
                List<String> row = new ArrayList<String>();
                row.add(pheno.getPhenotypeExperiment().getFigure().getPublication().getZdbID());
                row.add(pheno.getPhenotypeExperiment().getFigure().getPublication().getTitle());
                row.add(pheno.getDisplayName());
                Set<GenericTerm> obsoletedTerms = PhenotypeService.getObsoleteTerm(pheno);
                StringBuilder hyperlink = new StringBuilder();
                StringBuilder replaceLinks = new StringBuilder();
                StringBuilder considerLinks = new StringBuilder();
                for (GenericTerm obsoletedTerm : obsoletedTerms){
                    hyperlink.append(TermPresentation.getLink(obsoletedTerm, true));
                    replaceLinks.append(getListOfHyperlinksOfReplacedByTerms(obsoletedTerm));
                    considerLinks.append(getListOfHyperlinksOfConsiderTerms(obsoletedTerm))  ;
                }
                row.add(hyperlink.toString());
                row.add(replaceLinks.toString());
                row.add(considerLinks.toString());
                rows.add(row);
            }
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " annotations with obsoleted PATO terms");
            cronReport.warning("Found phenotypes with obsoleted terms.");
            cronJobUtil.addObjectToTemplateMap("domain", ZfinPropertiesEnum.DOMAIN_NAME.value());
            cronJobUtil.emailReport("ontology-loader-obsolete-terms-used.ftl", cronReport);
        }
        // check if any secondary IDs are used in any expression annotation:
        List<ExpressionResult> obsoletedTermsUsedExpression = RepositoryFactory.getExpressionRepository().getExpressionOnObsoletedTerms();
        if (obsoletedTermsUsedExpression != null && obsoletedTermsUsedExpression.size() > 0) {
            LOG.warn("Expression annotations found that use obsoleted term ids");
            StringBuffer buffer = new StringBuffer();
            String messageHeader = "Expression annotations found that use obsoleted term ids";
            buffer.append(messageHeader);
            report.addMessageToSection(messageHeader, "Post-Processing");
            List<List<String>> rows = new ArrayList<List<String>>(obsoletedTermsUsedExpression.size());
            for (ExpressionResult expressionResult : obsoletedTermsUsedExpression) {
                List<String> row = new ArrayList<String>();
                row.add(expressionResult.getExpressionExperiment().getPublication().getZdbID());
                row.add(expressionResult.getExpressionExperiment().getPublication().getTitle());
                row.add(expressionResult.getEntity().getDisplayName());
                Set<GenericTerm> obsoletedTerms = expressionService.getObsoleteTerm(expressionResult);
                StringBuilder hyperlink = new StringBuilder();
                StringBuilder replaceLinks = new StringBuilder();
                StringBuilder considerLinks = new StringBuilder();
                for (GenericTerm obsoletedTerm : obsoletedTerms){
                    hyperlink.append(TermPresentation.getLink(obsoletedTerm, true));
                    replaceLinks.append(getListOfHyperlinksOfReplacedByTerms(obsoletedTerm));
                    considerLinks.append(getListOfHyperlinksOfConsiderTerms(obsoletedTerm))  ;
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
            cronJobUtil.emailReport("ontology-loader-obsolete-terms-used.ftl", cronReport);
        }
        // check if any secondary IDs are used in any annotation:
        List<PhenotypeStatement> secondaryTermsUsed = RepositoryFactory.getMutantRepository().getPhenotypesOnSecondaryTerms();
        if (secondaryTermsUsed != null && secondaryTermsUsed.size() > 0) {
            LOG.warn("Pato annotations found that use secondary term ids");
            StringBuffer buffer = new StringBuffer();
            String messageHeader = "Pato annotations found that use secondary term ids";
            buffer.append(messageHeader);
            report.addMessageToSection(messageHeader, "Post-Processing");
            List<List<String>> rows = new ArrayList<List<String>>(secondaryTermsUsed.size());
            for (PhenotypeStatement pheno : secondaryTermsUsed) {
                List<String> row = new ArrayList<String>();
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
            List<List<String>> rows = new ArrayList<List<String>>(secondaryTermsUsedExpression.size());
            for (ExpressionResult expressionResult : secondaryTermsUsedExpression) {
                List<String> row = new ArrayList<String>();
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
        if (dataMap.get(UnloadFile.TERMS_MISSING_OBO_ID.getValue()) != null) {
            List<List<String>> rows = dataMap.get(UnloadFile.TERMS_MISSING_OBO_ID.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " missing OBO ID");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-terms-missing-obo-id.ftl", cronReport);
        }
        // missing terms with OBO id report.
        if (dataMap.get(UnloadFile.TERMS_UN_OBSOLETED_ID.getValue()) != null) {
            List<List<String>> rows = dataMap.get(UnloadFile.TERMS_UN_OBSOLETED_ID.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " terms are un-obsoleted");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-terms-un-obsoleted.ftl", cronReport);
        }
        // new terms report.
        if (dataMap.get(UnloadFile.NEW_TERMS.getValue()) != null) {
            List<List<String>> rows = dataMap.get(UnloadFile.NEW_TERMS.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " new " + termChoice.format(rows.size()) + " found");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-new-terms.ftl", cronReport);
        }
        // updated terms report.
        if (dataMap.get(UnloadFile.UPDATED_TERMS.getValue()) != null) {
            List<List<String>> rows = dataMap.get(UnloadFile.UPDATED_TERMS.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " updated term names");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-updated-terms.ftl", cronReport);
        }
        // updated term definitions report.
        if (dataMap.get(UnloadFile.MODIFIED_TERM_DEFINITIONS.getValue()) != null) {
            List<List<String>> rows = dataMap.get(UnloadFile.MODIFIED_TERM_DEFINITIONS.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " updated term definitions");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-updated-terms.ftl", cronReport);
        }
        // updated term comments report.
        if (dataMap.get(UnloadFile.MODIFIED_TERM_COMMENTS.getValue()) != null) {
            List<List<String>> rows = dataMap.get(UnloadFile.MODIFIED_TERM_COMMENTS.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " updated term comments");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-updated-terms.ftl", cronReport);
        }
        // updated terms report.
        if (dataMap.get(UnloadFile.SEC_UNLOAD_REPORT.getValue()) != null) {
            List<List<String>> rows = dataMap.get(UnloadFile.SEC_UNLOAD_REPORT.getValue());
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " merged " + termChoice.format(rows.size()) + " updated");
            cronReport.info();
            cronJobUtil.emailReport("ontology-loader-secondary-terms.ftl", cronReport);
        }
        updatePhenotypesReport();
        updateExpressionReport();
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
        if (data != null) {
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

    private void runDbScriptFile(String dbScriptFile) {
        File file = new File(dbScriptFile);
        if (!file.exists()) {
            LOG.error("Could not find script file " + file.getAbsolutePath());
            return;
        }
        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        InfrastructureRepository infrastructureRep = RepositoryFactory.getInfrastructureRepository();
        if (!LOG.isDebugEnabled())
            LOG.info("No Debugging enabled: To see more debug data enable the logger to leg level debug.");
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
                statement.updateInsertStatement(data.get(0).size());
                infrastructureRep.executeJdbcStatement(statement, data);
                LOG.info(data.size() + " records inserted");
            } else if (statement.isDebug()) {
                List<List<String>> dataReturn = null;
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
            } else if (statement.isUnloadStatement() || statement.isReadOnlyStatement()) {
                List<List<String>> dataReturn = null;
                dataReturn = infrastructureRep.executeNativeQuery(statement);
                if (dataReturn == null) {
                    LOG.info("  Debug data: No records found.");
                } else if (statement.getDataKey() == null) {
                    LOG.info("  Data: " + dataReturn.size() + " records.");
                } else if (statement.getDataKey().toUpperCase().equals(DatabaseJdbcStatement.DEBUG)) {
                    LOG.info("  Debug data: " + dataReturn.size() + " records.");
                    for (List<String> row : dataReturn)
                        LOG.info("  " + row);
                } else
                    dataMap.put(statement.getDataKey(), dataReturn);
            } else if (statement.isEcho()) {
                LOG.info(statement.getQuery());
            } else {
                infrastructureRep.executeJdbcStatement(statement);
            }
            DbSystemUtil.logLockInfo();
        }
    }

    private boolean processOboFile() {
        if (!newerVersionFound()) {
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
                    for (Link parentTerm : term.getParents()) {

                        appendFormattedRecord(UnloadFile.TERM_RELATIONSHIPS, parentTerm.getParent().getID(), term.getID(), parentTerm.getType().getName());
                    }
                    if (term.getReplacedBy() != null) {
                        for (org.obo.datamodel.ObsoletableObject replacedByID : term.getReplacedBy())
                            appendFormattedRecord(UnloadFile.TERM_REPLACED, replacedByID.getID(), term.getID(), "replaced_by");
                    }
                    if (term.getSynonyms() != null) {
                        for (Synonym synonym : term.getSynonyms())
                            appendSynonym(UnloadFile.TERM_SYNONYMS, term.getID(), synonym.getScope(), synonym.getText());
                    }
                    if (term.getConsiderReplacements() != null) {
                        for (org.obo.datamodel.ObsoletableObject considerObject : term.getConsiderReplacements())
                            appendFormattedRecord(UnloadFile.TERM_CONSIDER, term.getID(), considerObject.getID(), "consider");
                    }
                    if (term.getDbxrefs() != null) {
                        for (Dbxref dbxref : term.getDbxrefs()) {
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

    private void appendSynonym(UnloadFile unloadFile, String id, int scope, String text) {
        String type = getSynonymDescriptor(scope);
        appendFormattedRecord(unloadFile, id, text, type, "[]", "synonym");
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
        appendFormattedRecord(UnloadFile.TERM_PARSED, term.getID(),
                term.getName(), term.getNamespace().getID(), term.getDefinition(), term.getComment(), obsolete);
    }

    private void createOboSession() throws OBOParseException, IOException {
        DefaultOBOParser oboParser = new DefaultOBOParser();
        AbstractParseEngine engine = new OBOParseEngine(oboParser);
        // GOBOParseEngine can parse several files at once
        // and create one munged-together ontology,
        // so we need to provide a Collection to the setPaths() method
        Collection<String> paths = new LinkedList<String>();
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
        MODIFIED_TERM_NAMES("modified_term_names.unl"),
        MODIFIED_TERM_DEFINITIONS("modified_term_definitions.unl"),
        MODIFIED_TERM_COMMENTS("modified_term_comments.unl"),
        SUBSETDEFS_HEADER("subsetdefs_header.unl");
        private String value;

        UnloadFile(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private Map<UnloadFile, FileWriter> fileWriters = new HashMap<UnloadFile, FileWriter>(UnloadFile.values().length);

    private void appendFormattedRecord(UnloadFile unloadFile, String... record) {
        //appendRecord(unloadFile, InformixUtil.getUnloadRecord(record));
        List<List<String>> data = dataMap.get(unloadFile.getValue());
        if (data == null) {
            data = new ArrayList<List<String>>();
        }
        List<String> individualRecord = new ArrayList<String>(record.length);
        individualRecord.addAll(Arrays.asList(record));
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

    public CronJobReport getReport() {
        return report;
    }
}
