package org.zfin.properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.File;
import java.util.List;

/**
 */
public class RunSqlQueryTask extends AbstractScriptWrapper {

    private static final Logger LOG = Logger.getLogger(RunSqlQueryTask.class);

    private String instance;
    private String sqlFileName;
    private String baseDir;
    private String propertyPath;

    public void execute() {

        LOG.info("Running SQLQueryTask on instance: " + instance);
        File baseDirectory = new File(baseDir);
        if (!baseDirectory.exists()) {
            String message = "No directory found: " + baseDirectory.getAbsolutePath();
            LOG.error(message);
            throw new RuntimeException(message);
        }
        File dbQueryFile = new File(baseDirectory, sqlFileName);
        if (!dbQueryFile.exists()) {
            String message = "No file found: " + dbQueryFile.getAbsolutePath();
            LOG.info(message);
            throw new RuntimeException(message);
        }
        LOG.info("Running : " + dbQueryFile.getAbsolutePath());
        if (propertyPath == null)
            initAll();
        else {
            File file = new File(propertyPath);
            System.out.println("File: " + file.getAbsolutePath());
            System.out.println("exists: " + file.exists());
            ZfinProperties.init(propertyPath);
            initDatabase();
        }
        runQueryFile(dbQueryFile);
    }

    private void runQueryFile(File dbQueryFile) {
        DatabaseService service = new DatabaseService();
        HibernateUtil.currentSession().beginTransaction();
        List<String> errorMessages = null;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
        createErrorReport(errorMessages);
    }

    private void createErrorReport(List<String> errorMessages) {
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

    public void setSqlFileName(String sqlFileName) {
        this.sqlFileName = sqlFileName;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;

    }

    public static void main(String[] args) {
        String instance = args[0];
        String dbQueryFile = args[1];
        String baseDir = args[2];
        System.out.println("Instance: " + instance);
        System.out.println("SQL file: " + dbQueryFile);
        System.out.println("baseDir: " + baseDir);
        RunSqlQueryTask task = new RunSqlQueryTask();
        task.setBaseDir(baseDir);
        task.setInstance(instance);
        task.setSqlFileName(dbQueryFile);
        if (args.length > 3) {
            String homeDir = args[3];
            String propertyFileName = AbstractValidateDataReportTask.getPropertyFileFromWebroot(homeDir);
            task.propertyPath = propertyFileName;
            System.out.println("homeDir: " + propertyFileName);

        }
        initLog4J();
        setLoggerToInfoLevel(LOG);
        task.execute();
        System.exit(0);
    }
}
