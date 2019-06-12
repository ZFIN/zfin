package org.zfin.ontology.datatransfer.service;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.*;


/**
 * Class to validate ontologies, in particular for non-ASCII characters.
 * Command line options:
 * -termStageUpdateFile <obo file name>
 * -log4jFilename <log4j.xml file>
 * -propertyFile <path to zfin.properties location>
 * <p/>
 * This class assumes the latest obo file has already been downloaded from the corresponding site.
 * See {@link org.zfin.ontology.datatransfer.DownloadOntology}.
 */
public class OntologyTermFigureStageUpdate extends AbstractScriptWrapper {

    private static Logger LOG;

    static {
        options.addOption(termStageUpdateFileOption);
        options.addOption(log4jFileOption);
        options.addOption(webrootDirectory);
    }

    private CronJobReport report;

    public OntologyTermFigureStageUpdate(String oboFile, String propertyDirectory) throws IOException {
        if (propertyDirectory == null)
            initProperties();
        else
            ZfinProperties.init(propertyDirectory + "/WEB-INF/zfin.properties");
        initDatabase();
        if (propertyDirectory == null)
            throw new RuntimeException("No property file found.");
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue(propertyDirectory);
        cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
    }

    private static String updateFile;

    public static void main(String[] arguments) {
        LOG = LogManager.getLogger(OntologyTermFigureStageUpdate.class);
        LOG.info("Start Term-Stage Update: " + (new Date()).toString());
        CommandLine commandLine = parseArguments(arguments, "load <>");
//        initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        updateFile = commandLine.getOptionValue(termStageUpdateFileOption.getOpt());
        String propertyFileName = commandLine.getOptionValue(webrootDirectory.getOpt());
        LOG.info("Loading obo file: " + updateFile);
        OntologyTermFigureStageUpdate loader = null;
        try {
            loader = new OntologyTermFigureStageUpdate(updateFile, propertyFileName);
            loader.load(updateFile);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.exit(-1);
        }
        LOG.info("Property: " + ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL.value());
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        if (loader.initialize(new CronJobReport("Ontology Update Term-Fiure-stage info: " + updateFile, cronJobUtil)))
            loader.runOntologyUpdateProcess();

    }

    public void runOntologyUpdateProcess() {
        try {
            HibernateUtil.createTransaction();
            LOG.info("Duration of Loader: " + DateUtil.getTimeDuration(sectionTime));
            report.addMessageToSection("Finished Processing Obo file", "Parse Obo File");
            sectionTime = System.currentTimeMillis();
            StringBuilder startMessage = new StringBuilder("Start Script Execution: " + updateFile + "\n");
            long startTimeLong = System.currentTimeMillis();
            startMessage.append("Start Time: " + new Date() + "\n");
            // find expression-result objects
            List<ExpressionResultSplitStatement> statementList = new ArrayList<ExpressionResultSplitStatement>(splitStatementList.size());
            for (TermStageSplitStatement statement : splitStatementList) {
                ExpressionResultSplitStatement splitStatement = ExpressionService.splitExpressionAnnotations(statement);
                if (splitStatement != null)
                    statementList.add(splitStatement);
            }
            // create report
            generateReport(statementList);
            LOG.info("Duration of Script Execution: " + DateUtil.getTimeDuration(sectionTime));
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
            report.error(e);
        } finally {
            HibernateUtil.closeSession();
        }
        // needed to allow the generate absolute hyperlinks in emails
        TermPresentation.domain = "http://" + ZfinPropertiesEnum.DOMAIN_NAME;
        //postLoadProcess();
        // need to reverse it. A bit of a hack!
        TermPresentation.domain = null;
        LOG.info("Total Execution Time: " + DateUtil.getTimeDuration(sectionTime));
    }

    private void generateReport(List<ExpressionResultSplitStatement> statementList) {
        List<List<String>> rows = new ArrayList<List<String>>();
        for (ExpressionResultSplitStatement statement : statementList) {
            rows.add(getStringsPerRow(statement.getOriginalExpressionResult()));
            for (ExpressionResult splitStatement : statement.getExpressionResultList())
                rows.add(getStringsPerRow(splitStatement));

        }
        CronJobReport cronReport = new CronJobReport(report.getJobName());
        cronReport.setRows(rows);
        cronReport.appendToSubject(" - " + rows.size() + " new expression Results" + rows.size() + " found");
        cronReport.info();
        cronJobUtil.emailReport("term-stage-split-event.ftl", cronReport);

    }

    private List<String> getStringsPerRow(ExpressionResult result) {
        List<String> row = new ArrayList<String>(10);
//TODO        row.add(result.getZdbID());
        row.add(result.getSuperTerm().getOboID());
        row.add(result.getSuperTerm().getTermName());
        row.add(result.getStartStage().getAbbreviation());
        row.add(result.getEndStage().getAbbreviation());
        row.add(result.getExpressionExperiment().getPublication().getShortAuthorList());
        row.add(result.getExpressionExperiment().getPublication().getZdbID());
        StringBuilder builder = new StringBuilder();
        for (Figure figure : result.getFigures())
            builder.append(figure.getLabel() + ",");
        row.add(builder.deleteCharAt(builder.length() - 1).toString());
        return row;
    }

    private List<TermStageSplitStatement> splitStatementList;

    private void load(String fileName) {
        File file = new File(fileName);
        if (!file.exists())
            throw new RuntimeException(file.getAbsolutePath());
        TermStageUpdateFileParser parser = new TermStageUpdateFileParser(file);
        splitStatementList = parser.parseFile();
        findObjects(splitStatementList);
    }

    private List<TermStageSplitStatement> getSplitStatementList() {
        return splitStatementList;
    }

    /**
     * Tries to find the full entities for each one by example.
     *
     * @param splitStatementList list of split statements
     */
    private void findObjects(List<TermStageSplitStatement> splitStatementList) {
        if (splitStatementList == null)
            return;
        for (TermStageSplitStatement termStageSplitStatement : splitStatementList) {
            for (TermFigureStageRange stageRange : termStageSplitStatement.getTermFigureStageRangeList()) {
                ExpressionService.populateEntities(stageRange);
                String errorMessage = ExpressionService.populateEntities(termStageSplitStatement.getOriginalTermFigureStageRange());
                if (errorMessage != null)
                    termStageSplitStatement.setErrorMessage(errorMessage);
            }
        }
    }

    private long sectionTime;

    public boolean initialize(CronJobReport cronJobReport) {
        sectionTime = System.currentTimeMillis();
        this.report = cronJobReport;
        return true;
    }


    public void setLogger(Logger log) {
        LOG = log;
    }

}
