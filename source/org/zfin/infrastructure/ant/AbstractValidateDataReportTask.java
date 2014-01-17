package org.zfin.infrastructure.ant;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
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

    protected void createErrorReportFull(List<String> errorMessages, List<List<List<String>>> resultList, String reportPrefix) {
        if (CollectionUtils.isEmpty(resultList))
            return;
        if (resultList.size() > 1)
            throw new RuntimeException("More than one element");
        createErrorReport(errorMessages, resultList.get(0), reportPrefix);
    }

    protected void createErrorReport(List<String> errorMessages, List<List<String>> resultList, String reportPrefix) {
        String fileName = jobName + "." + reportPrefix + ".";
        File reportFile = new File(baseValidateDataDirectory, fileName + "report.html");
        if (reportFile.exists())
            reportFile.delete();
        File dataFile = new File(baseValidateDataDirectory, fileName + "txt");
        if (dataFile.exists())
            dataFile.delete();

        if (CollectionUtils.isEmpty(resultList))
            return;
        freemarker.template.Configuration configuration = new freemarker.template.Configuration();
        try {
            configuration.setDirectoryForTemplateLoading(baseValidateDataDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //StringWriter writer = new StringWriter();
        FileWriter writer = null;
        try {
            writer = new FileWriter(reportFile);
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> root = new HashMap<>();
            root.put("errorMessage", reportProperties.get(fileName + ERROR_MESSAGE));
            root.put("recordList", resultList);
            root.put("numberOfRecords", resultList.size());
            // header columns
            String[] headerCols = reportProperties.getProperty(fileName + HEADER_COLUMNS).split("\\|");
            root.put("headerColumns", headerCols);
            root.put("dateRun", new Date());
            if (queryFile != null)
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
