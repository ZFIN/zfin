package org.zfin.infrastructure.ant;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 */
public abstract class AbstractValidateDataReportTask extends AbstractScriptWrapper {

    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String HEADER_COLUMNS = "headerColumns";
    protected static final Logger LOG = Logger.getLogger(AbstractValidateDataReportTask.class);

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

    public abstract void execute();

    protected void init(String baseDir) {
        this.baseDir = baseDir;
        init(true);
    }

    protected void init() {
        init(true);
    }

    protected void init(boolean initDatabase) {
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
        reportProperties = new Properties();
        try {
            reportProperties.load(new FileInputStream(new File(dataDirectory, propertiesFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, String reportPrefix) {
        createErrorReport(errorMessages, resultList, reportPrefix, dataDirectory);
    }

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, String reportPrefix, File directory) {
        String fileName = jobName;
        if (StringUtils.isNotEmpty(reportPrefix))
            fileName += "." + reportPrefix;

        File reportFile = new File(directory, fileName + ".report.html");
        if (reportFile.exists())
            if (!reportFile.delete())
                LOG.error("Could not delete report file: " + reportFile.getAbsolutePath());
        File dataFile = new File(directory, fileName + ".txt");
        if (dataFile.exists())
            if (!dataFile.delete())
                LOG.error("Could not delete data file: " + dataFile.getAbsolutePath());

        if (CollectionUtils.isEmpty(resultList) || CollectionUtils.isEmpty(resultList))
            return;
        freemarker.template.Configuration configuration = new freemarker.template.Configuration();
        try {
            configuration.setDirectoryForTemplateLoading(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileWriter writer;
        try {
            writer = new FileWriter(reportFile);
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> root = new HashMap<>();
            String errorMessage = (String) reportProperties.get(fileName + "." + ERROR_MESSAGE);
            String columnHeader = (String) reportProperties.get(fileName + "." + HEADER_COLUMNS);
            if (StringUtils.isEmpty(errorMessage))
                throw new RuntimeException("No value for key " + fileName + " found in file " + directory + "/" + templateName);
            if (StringUtils.isEmpty(columnHeader))
                throw new RuntimeException("No value for key " + fileName + " found in file " + directory + "/" + templateName);
            root.put("errorMessage", errorMessage);
            root.put("recordList", resultList);
            root.put("numberOfRecords", resultList.size());
            // header columns
            String[] headerCols = columnHeader.split("\\|");
            root.put("headerColumns", headerCols);
            root.put("dateRun", new Date());
            if (queryFile != null)
                root.put("sqlQuery", getSqlQuery());
            template.process(root, writer);
            writer.flush();
            // export data
            StringBuilder lines = new StringBuilder();
            for (List<String> record : resultList) {
                for (String element : record) {
                    lines.append(element);
                    lines.append(",");
                }
                lines.append("\n");
            }
            FileUtils.writeStringToFile(dataFile, lines.toString());
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

    protected String getSqlQuery() throws IOException {
        return FileUtils.readFileToString(queryFile);
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
