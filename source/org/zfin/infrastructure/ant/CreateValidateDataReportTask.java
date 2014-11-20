package org.zfin.infrastructure.ant;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 */
public class CreateValidateDataReportTask extends AbstractValidateDataReportTask {

    private final Logger LOG = Logger.getLogger(CreateValidateDataReportTask.class);

    protected boolean useDynamicQuery;
    private DatabaseService service = new DatabaseService();

    public int execute() {
        LOG.info("Job Name: " + jobName);
        LOG.info("Running SQLQueryTask on instance: " + instance);
        if (useDynamicQuery)
            queryFile = new File(dataDirectory, jobName + ".sqlj");
        else
            queryFile = new File(dataDirectory, jobName + ".sql");
        if (!queryFile.exists()) {
            String message = "No file found: " + queryFile.getAbsolutePath();
            LOG.error(message);
            throw new RuntimeException(message);
        }
        LOG.info("Handling file : " + queryFile.getAbsolutePath());
        return runQueryFile(queryFile);
    }

    private int runQueryFile(File dbQueryFile) {
        clearReportDirectory();
        setLoggerFile();
        copyFileToReportDirectory(dbQueryFile);
        HibernateUtil.currentSession().beginTransaction();
        Map<String, List<List<String>>> resultMap;
        List<String> errorMessages;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile);
            resultMap = service.getResultMap();

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
                    createErrorReport(null, result, reportConfiguration);
                }
            } else {
                createErrorReport(errorMessages, null);
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.rollbackTransaction();
            HibernateUtil.closeSession();
        }
        boolean errorRecordsExist = isErrorRecord(resultMap);
        return errorRecordsExist ? 1 : 0;
    }

    private boolean isErrorRecord(Map<String, List<List<String>>> resultMap) {
        if (MapUtils.isEmpty(resultMap))
            return false;
        for (String key : resultMap.keySet())
            if (CollectionUtils.isNotEmpty(resultMap.get(key)))
                return true;
        return false;
    }

    public static void main(String[] args) {
        String instance = args[0];
        String jobName = args[1];
        String directory = args[2];
        String propertyFilePath = args[3];
        CreateValidateDataReportTask task = new CreateValidateDataReportTask();
        task.setInstance(instance);
        task.setJobName(jobName);
        task.setPropertyFilePath(propertyFilePath);
        if (args.length > 4)
            task.useDynamicQuery = Boolean.parseBoolean(args[4]);
        task.init(directory);
        System.exit(task.execute());
    }
}
