package org.zfin.infrastructure.ant;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.properties.ZfinProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 */
public abstract class AbstractValidateDataReportTask {

    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String HEADER_COLUMNS = "headerColumns";
    private final Logger LOG = Logger.getLogger(AbstractValidateDataReportTask.class);

    protected String instance;
    protected String baseDir;
    protected String jobName;
    protected String propertyFilePath;
    protected File baseValidateDataDirectory;
    protected File queryFile;
    protected String templateName = "validate-data-email-report.template";
    protected String propertiesFile = "validate-data-email-report.properties";
    protected Properties reportProperties;

    public abstract void execute();

    protected void init() {
        ZfinProperties.init(propertyFilePath);
        new HibernateSessionCreator(false);
        baseValidateDataDirectory = new File(baseDir);
        if (!baseValidateDataDirectory.exists()) {
            String message = "No directory found: " + baseValidateDataDirectory.getAbsolutePath();
            LOG.error(message);
            throw new RuntimeException(message);
        }
        reportProperties = new Properties();
        try {
            reportProperties.load(new FileInputStream(new File(baseValidateDataDirectory, propertiesFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, String reportPrefix) {
        String fileName = jobName;
        if (StringUtils.isNotEmpty(reportPrefix))
            fileName += "." + reportPrefix;

        File reportFile = new File(baseValidateDataDirectory, fileName + ".report.html");
        if (reportFile.exists())
            if (!reportFile.delete())
                LOG.error("Could not delete report file: " + reportFile.getAbsolutePath());
        File dataFile = new File(baseValidateDataDirectory, fileName + ".txt");
        if (dataFile.exists())
            if (!dataFile.delete())
                LOG.error("Could not delete data file: " + dataFile.getAbsolutePath());

        if (CollectionUtils.isEmpty(resultList) || CollectionUtils.isEmpty(resultList.get(0)))
            return;
        freemarker.template.Configuration configuration = new freemarker.template.Configuration();
        try {
            configuration.setDirectoryForTemplateLoading(baseValidateDataDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileWriter writer;
        try {
            writer = new FileWriter(reportFile);
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> root = new HashMap<>();
            root.put("errorMessage", reportProperties.get(jobName + "." + ERROR_MESSAGE));
            root.put("recordList", resultList.get(0));
            root.put("numberOfRecords", resultList.get(0).size());
            // header columns
            String[] headerCols = reportProperties.getProperty(jobName + "." + HEADER_COLUMNS).split("\\|");
            root.put("headerColumns", headerCols);
            root.put("dateRun", new Date());
            root.put("sqlQuery", FileUtils.readFileToString(queryFile));
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

}
