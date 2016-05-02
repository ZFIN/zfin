package org.zfin.infrastructure.ant;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinProperties;
import org.zfin.util.ReportGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 */
public abstract class AbstractValidateDataReportTask extends AbstractScriptWrapper {

    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String HEADER_COLUMNS = "headerColumns";
    protected static final Logger LOG = Logger.getLogger(AbstractValidateDataReportTask.class);
    public static final String TEMPLATE = ".template";

    protected String instance;
    protected String jobName;
    protected String propertyFilePath = "home/WEB-INF/zfin.properties";
    protected File dataDirectory;
    protected File queryFile;
    protected String propertiesFile = "report.properties";
    protected Properties reportProperties;
    protected DatabaseService service = new DatabaseService();
    // contains key-value pairs that are used in the report generation.
    protected Map<String, String> dataMap = new HashMap<>(5);

    public abstract int execute();

    protected AbstractValidateDataReportTask() {
    }

    protected AbstractValidateDataReportTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super();
        initBasicInfo(jobName, propertyFilePath, dataDirectoryString);
    }

    /**
     * Use if you cannot instantiate class through the non-default constructor, e.g. Autowire
     *
     * @param jobName
     * @param propertyFilePath
     * @param dataDirectoryString
     */
    public void initBasicInfo(String jobName, String propertyFilePath, String dataDirectoryString) {
        this.jobName = jobName;
        this.propertyFilePath = propertyFilePath;
        ZfinProperties.init(propertyFilePath);
        if (dataDirectoryString != null) {
            dataDirectory = new File(dataDirectoryString);
            if (!dataDirectory.exists()) {
                String message = "No directory found: " + dataDirectory.getAbsolutePath();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        }
        setReportProperties();
    }

    protected void initDatabase() {
        new HibernateSessionCreator(false);

    }

    public void initDatabase(boolean autocommit) {
        new HibernateSessionCreator(false, autocommit);
    }

    protected void setReportProperties() {
        reportProperties = new Properties();
        try {
            reportProperties.load(new FileInputStream(new File(dataDirectory, propertiesFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPropertyFileFromWebroot(String webrootDirectory) {
        if (webrootDirectory == null) {
            return null;
        }

        Path path = Paths.get(webrootDirectory, "WEB-INF", "zfin.properties");
        return path.toString();
    }

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList) {
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, jobName, true);
        createErrorReport(errorMessages, resultList, reportConfiguration);
    }

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, ReportConfiguration reportConfiguration) {

        if (CollectionUtils.isEmpty(resultList)) {
            File noUpdate = new File(reportConfiguration.getReportFile().getParent(), "no-update.txt");
            try {
                noUpdate.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ReportGenerator report = new ReportGenerator();
        report.setReportTitle("Report for " + jobName);
        report.includeTimestamp();

        if (reportConfiguration.isUseDefaultReportTemplate()) {
            String errorMessage = (String) reportProperties.get(reportConfiguration.getReportName() + "." + ERROR_MESSAGE);
            String columnHeader = (String) reportProperties.get(reportConfiguration.getReportName() + "." + HEADER_COLUMNS);
            if (StringUtils.isEmpty(errorMessage)) {
                LOG.warn("No value for key `" + reportConfiguration.getReportName() + "` found in file " + reportConfiguration.getTemplateDirectory() + "/" + propertiesFile);
                reportConfiguration.getReportFile().delete();
                return;
            }
            if (StringUtils.isEmpty(columnHeader)) {
                LOG.warn("No value for key `" + reportConfiguration.getReportName() + "` found in file " + reportConfiguration.getTemplateDirectory() + "/" + propertiesFile);
                reportConfiguration.getReportFile().delete();
                return;
            }
            if (resultList != null) {
                report.addIntroParagraph(resultList.size() + " " + errorMessage);
                report.addDataTable(Arrays.asList(columnHeader.split("\\|")), resultList);
            } else {
                report.addIntroParagraph("0 " + errorMessage);
            }
        }
        if (queryFile != null) {
            try {
                report.addCodeSnippet(getSqlQuery());
            } catch (IOException e) {
                LOG.error("Unable to load SQL query for report", e);
            }
        }
        report.writeFiles(reportConfiguration.getReportDirectory(), reportConfiguration.getReportName());

        if (CollectionUtils.isEmpty(errorMessages)) {
            return;
        }

        int index = 1;
        for (String error : errorMessages) {
            System.out.println("\r" + index++ + ". ");
            System.out.println(error);
        }
        throw new RuntimeException(" Errors in unit test:" + errorMessages.size());
    }

    protected void generateReports(List<String> errorMessages, Map<String, List<List<String>>> resultMap) {
        if (MapUtils.isNotEmpty(resultMap)) {
            if (resultMap.size() > 1) {
                for (String report : resultMap.keySet()) {
                    String reportName = jobName + "." + report;
                    ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
                    createErrorReport(null, resultMap.get(report), reportConfiguration);
                }
            } else {
                String key = resultMap.keySet().iterator().next();
                List<List<String>> result = resultMap.get(key);
                ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, jobName, true);
                createErrorReport(errorMessages, result, reportConfiguration);
            }
        } else {
            createErrorReport(errorMessages, null);
        }
    }

    // Override this in your sub class...
    protected void addCustomVariables(Map<String, Object> map) {

    }

    protected String getSqlQuery() throws IOException {
        String sql = FileUtils.readFileToString(queryFile);
        if (MapUtils.isNotEmpty(dataMap)) {
            for (String value : dataMap.keySet()) {
                sql = sql.replaceAll("\\$" + value, dataMap.get(value));
            }
        }
        return sql;
    }

    protected void copyFileToReportDirectory(File file) {
        try {
            FileUtils.copyFileToDirectory(file, new File(dataDirectory, jobName));
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    protected void setLoggerFile() {
        service.setLoggerLevelInfo();
        File logFile = FileUtils.getFile(dataDirectory, jobName, jobName + ".log");
        if (logFile.exists()) {
            if (!logFile.delete()) {
                LOG.error("Could not delete lgo file " + logFile.getAbsolutePath());
            }
        }
        service.setLoggerFile(logFile);
    }

    /**
     * clear out existing report directory named after the job.
     */
    protected void clearReportDirectory() {
        File reportDirectory = FileUtils.getFile(dataDirectory, jobName);
        try {
            FileUtils.deleteDirectory(reportDirectory);
        } catch (IOException e) {
            LOG.error(e);
        }

        if (!reportDirectory.mkdir()) {
            LOG.error("could not create Directory: " + reportDirectory.getAbsolutePath());
        }
    }


    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getJobName() {
        return jobName;
    }

    public static List<List<String>> getStringifiedList(List<String> updatedPages) {
        List<List<String>> returnList = new ArrayList<>(updatedPages.size());
        for (String value : updatedPages) {
            List<String> list = new ArrayList<>(1);
            list.add(value);
            returnList.add(list);
        }
        return returnList;
    }

}
