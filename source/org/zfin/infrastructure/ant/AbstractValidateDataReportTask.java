package org.zfin.infrastructure.ant;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinProperties;

import java.io.*;
import java.util.*;

/**
 */
public abstract class AbstractValidateDataReportTask extends AbstractScriptWrapper {

    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String HEADER_COLUMNS = "headerColumns";
    protected static final Logger LOG = Logger.getLogger(AbstractValidateDataReportTask.class);
    public static final String TEMPLATE = ".template";

    protected String instance;
    protected String baseDir;
    protected String jobName;
    protected String propertyFilePath = "home/WEB-INF/zfin.properties";
    protected File dataDirectory;
    protected File queryFile;
    protected String templateName = "validate-data-email-report.template";
    protected String propertiesFile = "validate-data-email-report.properties";
    protected Properties reportProperties;
    protected DatabaseService service = new DatabaseService();
    // contains key-value pairs that are used in the report generation.
    protected Map<String, String> dataMap = new HashMap<>(5);

    public abstract void execute();

    protected void init(String baseDir) {
        this.baseDir = baseDir;
        init(true);
    }

    protected void init() {
        init(true);
    }

    protected void init(boolean initDatabase) {
        clearReportDirectory();
        ZfinProperties.init(propertyFilePath);
        if (initDatabase)
            new HibernateSessionCreator(false);

        if (baseDir != null) {
            dataDirectory = new File(baseDir);
            if (!dataDirectory.exists()) {
                String message = "No directory found: " + dataDirectory.getAbsolutePath();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        }
        setReportProperties();
    }

    protected void setReportProperties() {
        reportProperties = new Properties();
        try {
            reportProperties.load(new FileInputStream(new File(dataDirectory, propertiesFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String reportPrefix;

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList) {
        createErrorReport(errorMessages, resultList, dataDirectory);
    }

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, File directory) {
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, baseDir, directory.getAbsolutePath());
        createErrorReport(errorMessages, resultList, reportConfiguration);
    }

    //protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, String reportPrefix, File directory, boolean useReportTemplate) {
    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, ReportConfiguration reportConfiguration) {

        Configuration configuration = new Configuration();
        try {
            configuration.setDirectoryForTemplateLoading(reportConfiguration.getTemplateDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOG.info("Template File being used: " + reportConfiguration.getTemplateDirectory() + "/" + reportConfiguration.getTemplateFileName());
        FileWriter writer;
        try {
            writer = new FileWriter(reportConfiguration.getReportFile());
            Template template = configuration.getTemplate(reportConfiguration.getTemplateFileName());
            Map<String, Object> root = new HashMap<>();


            if (reportConfiguration.isUseDefaultReportTemplate()) {
                String errorMessage = (String) reportProperties.get(reportConfiguration.getReportName() + "." + ERROR_MESSAGE);
                String columnHeader = (String) reportProperties.get(reportConfiguration.getReportName() + "." + HEADER_COLUMNS);
                if (StringUtils.isEmpty(errorMessage)) {
                    LOG.debug("No value for key " + reportConfiguration.getReportName() + " found in file " + reportConfiguration.getTemplateDirectory() + "/" + propertiesFile);
                    reportConfiguration.getReportFile().delete();
                    return;
                }
                if (StringUtils.isEmpty(columnHeader)) {
                    LOG.debug("No value for key " + reportConfiguration.getReportName() + " found in file " + reportConfiguration.getTemplateDirectory() + "/" + propertiesFile);
                    reportConfiguration.getReportFile().delete();
                    return;
                }
                root.put("errorMessage", errorMessage);
                // header columns
                String[] headerCols = columnHeader.split("\\|");
                root.put("headerColumns", headerCols);
            }
            if (CollectionUtils.isEmpty(resultList) || CollectionUtils.isEmpty(resultList)) {
                root.put("recordList", new ArrayList<List<String>>());
                root.put("numberOfRecords", 0);
            } else {
                root.put("recordList", resultList);
                root.put("numberOfRecords", resultList.size());
            }
            // add custom variables
            addCustomVariables(root);
            root.put("dateRun", new Date());
//            root.putAll();
            if (queryFile != null)
                root.put("sqlQuery", getSqlQuery());
            template.process(root, writer);
            writer.flush();
            // export data
            StringBuilder lines = new StringBuilder();
            if (CollectionUtils.isNotEmpty(resultList)) {
                for (List<String> record : resultList) {
                    for (String element : record) {
                        lines.append(element);
                        lines.append(",");
                    }
                    lines.append("\n");
                }
            }
            FileUtils.writeStringToFile(reportConfiguration.getPlainReportFile(), lines.toString());
        } catch (IOException e) {
            LOG.error(e);
            throw new RuntimeException("Error finding template file.", e);
        } catch (TemplateException e) {
            LOG.error(e);
            throw new RuntimeException("Error while creating email body", e);
        }

        if (CollectionUtils.isEmpty(errorMessages))
            return;

        int index = 1;
        for (String error : errorMessages) {
            System.out.println("\r" + index++ + ". ");
            System.out.println(error);
        }
        throw new RuntimeException(" Errors in unit test:" + errorMessages.size());
    }

    // Override this in your sub class...
    protected void addCustomVariables(Map<String, Object> map) {

    }

    protected void createReportFile(File directory, List<List<String>> resultList, List<String> errorMessages) {
        File[] templateFileList = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(TEMPLATE) && name.startsWith(jobName);
            }
        });
        if (templateFileList != null && templateFileList.length > 0) {
            for (File templateFile : templateFileList) {
                ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, templateFile, jobName, false);
                createErrorReport(errorMessages, resultList, reportConfiguration);
            }
        } else{
            ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, directory, jobName, true);
            createErrorReport(errorMessages, resultList, reportConfiguration);
        }
    }

    private String getReportFileFromTemplateName(String templateName) {
        if (StringUtils.isEmpty(templateName))
            return null;
        if (!templateName.endsWith(TEMPLATE))
            throw new RuntimeException("template file name needs to end with " + TEMPLATE);
        // strip off .template ending
        // and job name prefix
        return templateName.replace(TEMPLATE, "").replace(jobName + ".", "");
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
        File logFile = new File(dataDirectory, jobName + ".log");
        if (logFile.exists()) {
            if (!logFile.delete())
                LOG.error("Could not delete lgo file " + logFile.getAbsolutePath());
        }
        service.setLoggerFile(logFile);
    }

    protected void setReportLoggerFile() {
        service.setLoggerLevelInfo();
        File logFile = FileUtils.getFile(dataDirectory, jobName, jobName + ".log");
        if (logFile.exists()) {
            if (!logFile.delete())
                LOG.error("Could not delete lgo file " + logFile.getAbsolutePath());
        }
        service.setLoggerFile(logFile);
    }

    /**
     * clear out existing report directory named after the job.
     */
    protected void clearReportDirectory() {
        File reportDirectory = new File(dataDirectory, jobName);
        reportDirectory.delete();
        reportDirectory.mkdir();
    }


    /*
        protected void createErrorReport(List<String> errorMessages, List<List<List<String>>> resultList) {
            createErrorReport(errorMessages, resultList, null);
        }

    */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;

    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setPropertyFilePath(String propertyFilePath) {
        this.propertyFilePath = propertyFilePath;
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
