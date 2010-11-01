package org.zfin.ontology.datatransfer;

import org.zfin.util.DateUtil;

import java.util.*;

/**
 * Cron Job report contains all the important information for a given job.
 */
public class CronJobReport {

    private String jobName;
    private Map<String, StringBuffer> messageMap = new LinkedHashMap<String, StringBuffer>(5);
    private long startTime;
    private long finishTime;
    private AbstractScriptWrapper.ScriptExecutionStatus status = AbstractScriptWrapper.ScriptExecutionStatus.SUCCESS;
    private List<String> errorMessages = new ArrayList<String>(2);
    private List rows;
    private String dataSectionTitle;
    private CronJobUtil cronJobUtil;

    public CronJobReport(String jobName) {
        this.jobName = jobName;
    }

    public CronJobReport(String jobName, CronJobUtil cronJobUtil) {
        this.jobName = jobName;
        this.cronJobUtil = cronJobUtil;
    }

    public void addMessageToSection(String message, String sectionName) {
        StringBuffer messageBody = messageMap.get(sectionName);
        if (messageBody == null) {
            messageBody = new StringBuffer();
            messageMap.put(sectionName, messageBody);
        } else
            messageBody.append(System.getProperty("line.separator"));
        messageBody.append(message);
    }

    public String getFullReport() {
        if (messageMap.size() == 0)
            return "";

        StringBuilder builder = new StringBuilder();
        for (String sectionName : messageMap.keySet()) {
            builder.append(sectionName);
            builder.append(messageMap.get(sectionName));
        }
        return builder.toString();
    }

    public String getSummaryReport() {
        StringBuilder builder = new StringBuilder();
        builder.append("Summary Report: ");
        builder.append("Cron job Name: ");
        builder.append(jobName);
        builder.append("Curation: ");
        builder.append(DateUtil.getTimeDuration(startTime));
        builder.append("Start time: ");
        builder.append(new Date(startTime));
        builder.append("Finish time: ");
        builder.append(new Date(finishTime));

        for (String sectionName : messageMap.keySet()) {
            builder.append(sectionName);
            builder.append(messageMap.get(sectionName));
        }
        return builder.toString();
    }

    public void success() {
        status = AbstractScriptWrapper.ScriptExecutionStatus.SUCCESS;
    }

    public void error(String message) {
        errorMessages.add(message);
        status = AbstractScriptWrapper.ScriptExecutionStatus.ERROR;
    }

    public void warning(String message) {
        errorMessages.add(message);
        status = AbstractScriptWrapper.ScriptExecutionStatus.WARNING;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void finish() {
        finishTime = System.currentTimeMillis();
    }

    public AbstractScriptWrapper.ScriptExecutionStatus getStatus() {
        return status;
    }

    public String getJobName() {
        return jobName;
    }

    public String getStartDate() {
        return (new Date(startTime)).toString();
    }

    public String getEndDate() {
        return (new Date(finishTime)).toString();
    }

    public String getDuration() {
        return DateUtil.getTimeDuration(startTime, finishTime);
    }

    public Map<String, StringBuffer> getMessageMap() {
        return messageMap;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public void appendToSubject(String message) {
        jobName += " " + message;
    }

    public void setDataSectionTitle(String message) {
        dataSectionTitle = message;
    }

    public String getDataSectionTitle() {
        return dataSectionTitle;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void error(Throwable throwable) {
        StringBuilder errorString = new StringBuilder();
        if (throwable.getCause() != null) {
            errorString.append(throwable.getCause());
        }

        StackTraceElement[] elements = throwable.getStackTrace();
        if (throwable.getMessage() != null)
            errorString.append(throwable.getMessage() + "\n");
        errorString.append(throwable.toString() + "\n");
        for (StackTraceElement element : elements) {
            errorString.append(element + "\n");
        }
        errorMessages.add(errorString.toString());
        status = AbstractScriptWrapper.ScriptExecutionStatus.ERROR;

    }

    public void info() {
        status = AbstractScriptWrapper.ScriptExecutionStatus.INFO;
    }
}
