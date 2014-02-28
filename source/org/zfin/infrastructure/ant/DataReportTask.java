package org.zfin.infrastructure.ant;

import org.apache.commons.io.FileUtils;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 */
public class DataReportTask extends AbstractValidateDataReportTask {

    public static final String DELIMITER = "__";
    private static final String REPORT_DIRECTORY = "server_apps/DB_maintenance/report_data";
    private Map<String, String> dataMap = new HashMap<>(5);

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
        setLoggerFile();
        HibernateUtil.currentSession().beginTransaction();
        List<String> errorMessages;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile, null, dataMap);
            List<List<List<String>>> resultList = service.getListOfResultRecords();
            if (resultList != null)
                createErrorReport(errorMessages, resultList.get(0), null, dataDirectory);
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.rollbackTransaction();
            HibernateUtil.closeSession();
        }
    }

    protected String getSqlQuery() throws IOException {
        String sql = FileUtils.readFileToString(queryFile);
        for (String value : dataMap.keySet()) {
            sql = sql.replaceAll("\\$" + value, dataMap.get(value));
        }
        return sql;
    }


    // Todo: Needs to be rafactored to be more general
    // use commons commandLine and a better way to pass in the value default
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
            values = year + "__" + month + "__" + day;
        }
        task.valueNames = values;
        String baseDir = null;
        if (args.length > 3)
            baseDir = args[3];
        task.templateName = "report-data-email.template";
        task.propertiesFile = "report-data-email.properties";
        String pathname = baseDir + "/" + REPORT_DIRECTORY;
        if (baseDir != null) {
            task.propertyFilePath = baseDir + "/" + task.propertyFilePath;
            task.init(pathname);
        }
        task.dataDirectory = new File(pathname);
        task.execute();
    }
}
