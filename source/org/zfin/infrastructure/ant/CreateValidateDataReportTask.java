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

    public CreateValidateDataReportTask(String jobName, String propertyFilePath, String directory) {
        super(jobName, propertyFilePath, directory);
    }

    public int execute() {
        LOG.info("Job Name: " + jobName);
        LOG.info("Running SQLQueryTask on instance: " + instance);
        if (useDynamicQuery) {
            queryFile = new File(dataDirectory, jobName + ".sqlj");
        } else {
            queryFile = new File(dataDirectory, jobName + ".sql");
        }
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
            generateReports(errorMessages, resultMap);
            if (!resultMap.values().stream().allMatch(List::isEmpty)) {
                LOG.warn("Validation Errors found");
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            LOG.error(e);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
        return 0;
    }

    private boolean isErrorRecord(Map<String, List<List<String>>> resultMap) {
        if (MapUtils.isEmpty(resultMap)) {
            return false;
        }
        for (String key : resultMap.keySet()) {
            if (CollectionUtils.isNotEmpty(resultMap.get(key))) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String instance = args[0];
        String jobName = args[1];
        String directory = args[2];
        String propertyFilePath = args[3];
        CreateValidateDataReportTask task = new CreateValidateDataReportTask(jobName, propertyFilePath, directory);
        task.setInstance(instance);
        if (args.length > 4) {
            task.useDynamicQuery = Boolean.parseBoolean(args[4]);
        }
        task.initDatabase();
        System.exit(task.execute());
    }
}
