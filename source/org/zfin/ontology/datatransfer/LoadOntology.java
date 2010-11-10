package org.zfin.ontology.datatransfer;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.obo.dataadapter.AbstractParseEngine;
import org.obo.dataadapter.DefaultOBOParser;
import org.obo.dataadapter.OBOParseEngine;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.*;
import org.obo.history.SessionHistoryList;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.Phenotype;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.OntologyMetadata;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DateUtil;
import org.zfin.util.DbScriptFileParser;

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
 * <p/>
 * This class assumes the latest obo file has already been downloaded from the corresponding site.
 * See {@link DownloadOntology}.
 */
public class LoadOntology extends AbstractScriptWrapper {

    private static Logger LOG;

    static {
        options.addOption(oboFileNameOption);
        options.addOption(log4jFileOption);
        options.addOption(dbScriptFileOption);
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

    public LoadOntology(String oboFile, String... scriptFiles) throws IOException {
        oboFilename = oboFile;
        this.dbScriptFiles = scriptFiles;
        if (!checkFileExists(oboFilename))
            throw new IOException("No OBO file <" + oboFile + "> found. You may have to download the obo file first.");
        if (dbScriptFiles != null) {
            for (String dbScriptFile : dbScriptFiles)
                if (!checkFileExists(dbScriptFile))
                    throw new IOException("No DB script file <" + dbScriptFile + "> found!");
        }
        cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
    }

    public static void main(String[] arguments) {
        LOG = Logger.getLogger(LoadOntology.class);
        LOG.info("Start Ontology Loader class: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments);
        initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        String oboFile = commandLine.getOptionValue(oboFileNameOption.getOpt());
        String dbScriptFilesNames = commandLine.getOptionValue(dbScriptFileOption.getOpt());
        String[] dbScriptFiles = dbScriptFilesNames.split(",");
        LOG.info("Loading obo file: " + oboFile);

        LoadOntology loader = null;
        try {
            loader = new LoadOntology(oboFile, dbScriptFiles);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.exit(-1);
        }
        loader.initAll();
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        if (loader.initialize(oboFile, cronJobUtil))
            loader.runOntologyUpdateProcess();
    }

    public void runOntologyUpdateProcess() {
        //ontologyLoader.openAllFiles();
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
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                LOG.error(e);
                report.error(e);
            }finally {
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
        List<Phenotype> phenotypes = RepositoryFactory.getMutantRepository().getPhenotypesOnObsoletedTerms();
        if (phenotypes != null && phenotypes.size() > 0) {
            LOG.warn("Pato annotations found that use obsoleted terms");
            List<List<String>> rows = new ArrayList<List<String>>(phenotypes.size());
            for (Phenotype pheno : phenotypes) {
                List<String> row = new ArrayList<String>();
                row.add(pheno.getPublication().getZdbID());
                row.add(pheno.getPublication().getTitle());
                row.add(pheno.getSuperterm().getTermName());
                row.add(pheno.getTerm().getTermName());
                row.add(pheno.getTerm().getID());
                rows.add(row);
            }
            CronJobReport cronReport = new CronJobReport(report.getJobName());
            cronReport.setRows(rows);
            cronReport.appendToSubject(" - " + rows.size() + " annotations with obsoleted PATO terms");
            cronReport.warning("Found phenotypes with obsoleted terms.");
            cronJobUtil.emailReport("ontology-loader-obsolete-terms-used.ftl", cronReport);
        }
        // check if any secondary IDs are used in any annotation:
        List<List<String>> secondaryTermsUsed = dataMap.get(UnloadFile.SECONDARY_TERMS_USED.getValue());
        if (secondaryTermsUsed != null && secondaryTermsUsed.size() > 0) {
            LOG.warn("Pato annotations found that use secondary term ids");
            StringBuffer buffer = new StringBuffer();
            String messageHeader = "Pato annotations found that use secondary term ids";
            buffer.append(messageHeader);
            report.addMessageToSection(messageHeader, "Post-Processing");
            for (List<String> row : secondaryTermsUsed) {
                StringBuilder builder = new StringBuilder();
                for (String column : row) {
                    builder.append(column);
                    builder.append("/t");
                }
                LOG.warn(builder.toString());
                report.addMessageToSection(builder.toString(), "Post-Processing");
                buffer.append(builder.toString());
                buffer.append("\r");
            }
            cronJobUtil.emailReport("Ontology Loader: " + ontology.getOntologyName(), buffer.toString(), ScriptExecutionStatus.INFO);
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
            cronReport.appendToSubject(" - " + rows.size() + " " + termChoice.format(rows.size()) + " updated");
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

    private void updateExpressionReport() {
        // secondary terms replaced report.
        replacedTerms(dataMap.get(UnloadFile.EXPRESSION_SUBTERM_UPDATES.getValue()), "Super term", "Expression");
        replacedTerms(dataMap.get(UnloadFile.EXPRESSION_SUBTERM_UPDATES.getValue()), "Sub term", "Expression");
    }

    private void updatePhenotypesReport() {
        // secondary terms replaced report.
        replacedTerms(dataMap.get(UnloadFile.PHENOTYPE_SUPERTERM_UPDATES.getValue()), "Super term", "Phenotype");
        replacedTerms(dataMap.get(UnloadFile.PHENOTYPE_SUBTERM_UPDATES.getValue()), "Sub term", "Phenotype");
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

    private static void initializeLogger(String log4jFilename) {
        DOMConfigurator.configure(log4jFilename);
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
/*
        Ontology ontology = Ontology.getOntology(oboSession.getDefaultNamespace().getID());
        Set<Term> terms = OntologyManager.getInstance().getAllTerms(ontology);
        LOG.info("Number of Terms in database: " + terms.size());
*/
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
            for (Namespace namespace : oboSession.getNamespaces()) {
                dbMetadata = ontologyRepository.getOntologyMetadata(namespace.getID());
                if (dbMetadata == null) {
                    dbMetadata = new OntologyMetadata();
                    dbMetadata.setName(namespace.getID());
                    dbMetadata.setDefaultNamespace(oboSession.getDefaultNamespace().getID());
                    updateMetadata(dbMetadata, oboMetadata);
                    ontologyRepository.saveNewDbMetaData(dbMetadata);
                } else
                    updateMetadata(dbMetadata, oboMetadata);
            }
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

    private boolean checkFileExists(String fileName) {
        File file = new File(fileName);
        if (file.exists())
            return true;
        LOG.error("File not found: " + file.getAbsolutePath());
        return false;
    }

    private static CommandLine parseArguments(String[] args) {
        CommandLine commandLine = null;
        try {
            // parse the command line arguments
            CommandLineParser parser = new GnuParser();
            commandLine = parser.parse(options, args);
        }
        catch (ParseException exp) {
            LOG.error("Parsing failed.  Reason: " + exp.getMessage());
            System.exit(-1);
        }
        if (commandLine == null || commandLine.getOptions().length == 0) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("load ontology", options);
            System.exit(-1);
        }
        return commandLine;
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
        NEW_TERMS("new_terms.unl"),
        UPDATED_TERMS("updated_terms.unl"),
        EXPRESSION_SUPERTERM_UPDATES("expression-superterm-updates.unl"),
        EXPRESSION_SUBTERM_UPDATES("expression-subterm-updates.unl"),
        PHENOTYPE_SUPERTERM_UPDATES("phenotype-superterm-updates.unl"),
        PHENOTYPE_SUBTERM_UPDATES("phenotype-subterm-updates.unl"),
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

    // TODO: Will be used when we do the update directly from Java instead of unloading and loading all term infrastructure
    // objects.
    private OntologyManager ontologyManager;

    private void loadOntologyManager() {
        try {
//            ontologyManager = OntologyManager.getInstance(Ontology.QUALITY);
            ontologyManager = OntologyManager.getEmptyInstance();
            ontologyManager.deserializeRelationships();
            ontologyManager.deserializeOntology(Ontology.QUALITY);
        } catch (Exception e) {
            LOG.error("failed to load from file: " + ontologyManager, e);
        }
    }

    public CronJobReport getReport() {
        return report;
    }
}
