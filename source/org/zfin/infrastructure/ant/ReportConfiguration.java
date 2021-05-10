package org.zfin.infrastructure.ant;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.gwt.root.util.StringUtils;

import java.io.File;

/**
 */
public class ReportConfiguration {

    public static final String TEMPLATE = "template";
    private String templateFileName;
    private String defaultTemplateFileName = "report.html.template";
    private String reportFileName = "report.html";
    private String reportFileNamePlain = "report.txt";
    private String reportProperties = "report.properties";
    private File reportFile;
    private File plainReportFile;
    private File reportDirectory;
    private File templateFile;
    private File templateDirectory;
    private String jobName;
    private String reportName;
    private boolean useDefaultReportTemplate = true;

    protected static final Logger LOG = LogManager.getLogger(ReportConfiguration.class);


    public ReportConfiguration(String jobName, File templateDirectory, File reportDirectory, String reportName) {
        checkNotNull(jobName, "No job name provided:");
        this.jobName = jobName;
        checkNotNull(templateDirectory, "No template directory provided:");
        this.templateDirectory = templateDirectory;
        checkNotNull(reportDirectory, "No report directory provided:");
        this.reportDirectory = reportDirectory;
        this.reportName = reportName;
        init();
    }

    public ReportConfiguration(String jobName,
                               File templateDirectory,
                               File reportDirectory,
                               String reportName,
                               boolean useDefaultReportTemplate) {
        checkNotNull(jobName, "No job name provided:");
        this.jobName = jobName;
        checkNotNull(templateDirectory, "No template directory provided:");
        this.templateDirectory = templateDirectory;
        checkNotNull(reportDirectory, "No report directory provided:");
        this.reportDirectory = reportDirectory;
        this.reportName = reportName;
        this.useDefaultReportTemplate = useDefaultReportTemplate;
        init();
    }

    /**
     * Assumes that the report directory is the template directory/<jobName>
     *
     * @param jobName
     * @param templateDirectory
     * @param reportName
     * @param useDefaultReportTemplate
     */
    public ReportConfiguration(String jobName,
                               File templateDirectory,
                               String reportName,
                               boolean useDefaultReportTemplate) {
        checkNotNull(jobName, "No job name provided:");
        this.jobName = jobName;
        checkNotNull(templateDirectory, "No template directory provided:");
        if (templateDirectory.isDirectory())
            this.templateDirectory = templateDirectory;
        else {
            this.templateDirectory = templateDirectory.getParentFile();
            templateFileName = templateDirectory.getName();
        }
        this.reportDirectory = new File(this.templateDirectory, jobName);
        this.reportName = reportName;
        this.useDefaultReportTemplate = useDefaultReportTemplate;
        init();
    }

    public ReportConfiguration(String jobName, File reportDirectory, String reportName) {
        this(jobName, reportDirectory, reportName, true);
    }

    private void init() {
        initReportFile();
        initTemplateFile();
    }

    private void initTemplateFile() {
        // is the template file already been set do nothing in this method
        if (templateFileName != null)
            return;
        if (reportName != null && !useDefaultReportTemplate) {
            templateFileName = reportName + ".html.template";
        }
        if (useDefaultReportTemplate)
            templateFileName = defaultTemplateFileName;
        templateFile = FileUtils.getFile(templateDirectory, templateFileName);
/*
        if (!templateFile.exists())
            LOG.error("Template file not found: " + templateFile.getAbsolutePath());
*/
    }

    private void initReportFile() {
        if (reportName != null && templateFileName == null) {
            reportFileName = reportName + ".html";
            reportFileNamePlain = reportName + ".txt";
        }
        if (templateFileName != null) {
            int indexOfTemplate = templateFileName.indexOf(TEMPLATE);
            if (indexOfTemplate > -1)
                reportFileName = templateFileName.substring(0, indexOfTemplate - 1);
        }
        reportFile = FileUtils.getFile(reportDirectory, reportFileName);
        plainReportFile = FileUtils.getFile(reportDirectory, reportFileNamePlain);
        if (reportFile.exists())
            if (!reportFile.delete())
                LOG.error("Could not delete report file: " + reportFile.getAbsolutePath());
        if (plainReportFile.exists())
            if (!plainReportFile.delete())
                LOG.error("Could not delete report file: " + plainReportFile.getAbsolutePath());
        File dataFile = FileUtils.getFile(reportDirectory, jobName, reportFileNamePlain);
        if (dataFile.exists())
            if (!dataFile.delete())
                LOG.error("Could not delete data file: " + dataFile.getAbsolutePath());
    }

    public boolean isUseDefaultReportTemplate() {
        return useDefaultReportTemplate;
    }

    private void checkNotNull(String reportDirectory, String message) {
        if (StringUtils.isEmptyTrim(reportDirectory)) {
            throw new NullPointerException(message);
        }
    }

    private void checkNotNull(File reportDirectory, String message) {
        if (reportDirectory == null) {
            throw new NullPointerException(message);
        }
    }

    public File getTemplateDirectory() {
        return templateDirectory;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public File getReportFile() {
        return reportFile;
    }

    public File getPlainReportFile() {
        return plainReportFile;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public String getReportName() {
        return reportName == null ? jobName : reportName;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public File getReportDirectory() {
        return reportDirectory;
    }

}
