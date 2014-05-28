package org.zfin.infrastructure.ant;

import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.util.Calendar;
import java.util.List;

/**
 */
public class DataReportTask extends AbstractValidateDataReportTask {

    public static final String DELIMITER = "__";
    private static final String REPORT_DIRECTORY = "server_apps/DB_maintenance/report_data";

    private String variableNames;
    private String valueNames;

    public void execute() {
        LOG.info("Job Name: " + jobName);
        String[] variables = variableNames.split(DELIMITER);
        String[] values = valueNames.split(DELIMITER);
        if (variables.length != values.length)
            throw new RuntimeException("The number of variables need to match the number of values");
        for (int index = 0; index < values.length; index++) {
            dataMap.put(variables[index], values[index]);
        }
        queryFile = new File(dataDirectory, jobName + ".sql");
        if (!queryFile.exists()) {
            String message = "No file found: " + queryFile.getAbsolutePath();
            LOG.error(message);
            throw new RuntimeException(message);
        }
        LOG.info("Handling file : " + queryFile.getAbsolutePath());
        runQueryFile(queryFile);
    }

    private void runQueryFile(File dbQueryFile) {
        setReportLoggerFile();
        copyFileToReportDirectory(dbQueryFile);
        HibernateUtil.currentSession().beginTransaction();
        List<String> errorMessages;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile, null, dataMap);
            List<List<List<String>>> resultList = service.getListOfResultRecords();
            if (resultList != null)
                createReportFile(dataDirectory, resultList.get(0), errorMessages);
            else
                createErrorReport(errorMessages, null, dataDirectory);
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.rollbackTransaction();
            HibernateUtil.closeSession();
        }
    }

    public static void main(String[] args) {
        String jobName = args[0];
        DataReportTask task = new DataReportTask();
        task.setJobName(jobName);
        task.variableNames = args[1];
        String values = args[2];
        if (values.equals("")) {
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH) + 1;
            int day = now.get(Calendar.DAY_OF_MONTH);
            values = year + "__" + getPaddedNumber(month) + "__" + getPaddedNumber(day);
        }
        task.valueNames = values;
        String baseDir = null;
        if (args.length > 3)
            baseDir = args[3];
        task.propertiesFile = "report-data-email.properties";
        task.templateName = "report-data-email.template";
        String pathname = baseDir + "/" + REPORT_DIRECTORY;
        if (baseDir != null) {
            task.propertyFilePath = baseDir + "/" + task.propertyFilePath;
            task.init(pathname);
        }
        task.dataDirectory = new File(pathname);
        task.execute();
    }

    private static String getPaddedNumber(int month) {
        if (month > 9)
            return String.valueOf(month);
        else
            return "0" + String.valueOf(month);
    }

}
