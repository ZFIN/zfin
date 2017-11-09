package org.zfin.infrastructure.ant;

import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;

import java.io.File;

/**
 */
public class RunSQLFiles extends AbstractValidateDataReportTask {

    private final Logger LOG = Logger.getLogger(RunSQLFiles.class);

    protected boolean useDynamicQuery;
    private DatabaseService service = new DatabaseService();

    public RunSQLFiles(String jobName, String propertyFilePath, String directory) {
        super(jobName, propertyFilePath, directory);
    }

    public void initBasicInfo(String jobName, String propertyFilePath, String dataDirectoryString) {
        this.jobName = jobName;
        this.propertyFilePath = propertyFilePath;
        ZfinProperties.init(propertyFilePath);
        if (dataDirectoryString != null) {
            dataDirectory = new File(dataDirectoryString);
            if (!dataDirectory.exists()) {
                String message = "No directory found: " + dataDirectory.getAbsolutePath();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        }
    }

    public int execute() {
        LOG.info("Job Name: " + jobName);
        LOG.info("Running SQLQueryTask on instance: " + instance);
        for (File queryFile : queryFiles) {
            if (!queryFile.exists()) {
                String message = "No file found: " + queryFile.getAbsolutePath();
                LOG.error(message);
                throw new RuntimeException(message);
            }
            LOG.info("Handling file: " + queryFile.getAbsolutePath());
            runQueryFile(queryFile);
            LOG.info("Finished file: " + queryFile.getAbsolutePath());
        }
        return 0;
    }

    private void runQueryFile(File dbQueryFile) {
        clearReportDirectory();
        setLoggerFile();
        copyFileToReportDirectory(dbQueryFile);
        HibernateUtil.currentSession().beginTransaction();
        try {
            service.runDbScriptFile(dbQueryFile);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            LOG.error(e);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public static void main(String[] args) {
        String instance = args[0];
        String jobName = args[1];
        String directory = args[2];
        String propertyFilePath = args[3];
        RunSQLFiles task = new RunSQLFiles(jobName, propertyFilePath, directory);
        task.setInstance(instance);
        if (args.length > 4) {
            task.useDynamicQuery = Boolean.parseBoolean(args[4]);
        }
        task.initDatabase();
        System.exit(task.execute());
    }
}
