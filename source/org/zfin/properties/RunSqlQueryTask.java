package org.zfin.properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

import java.io.File;
import java.util.List;

/**
 */
public class RunSqlQueryTask {

    private final Logger LOG = Logger.getLogger(RunSqlQueryTask.class);

    private String instance;
    private String sqlFileName;
    private String baseDir;

    public void execute() {
        //System.setProperty("java.io.tmpdir", "test/temp");
        System.out.println("java.io.tmpdir: " + System.getProperty("java.io.tmpdir"));

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
        LOG.info("Handling file : " + dbQueryFile.getAbsolutePath());
        ZfinProperties.init();
        initDatabase();
        runQueryFile(dbQueryFile);
    }

    private void runQueryFile(File dbQueryFile) {
        DatabaseService service = new DatabaseService();
        HibernateUtil.currentSession().beginTransaction();
        List<String> errorMessages = null;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile);
            HibernateUtil.flushAndCommitCurrentSessionWithLowPdq();
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

    private void initDatabase() {
        LOG.info("Start Hibernate Session Creation");
        LOG.info("Database used:" + instance);
        if (instance == null) {
            throw new RuntimeException("Failed to instantiate the the db-name [" + instance + "] ");
        }

        LOG.info("Initial Hibernate session defined: " + HibernateUtil.hasSessionFactoryDefined());
        if (HibernateUtil.hasSessionFactoryDefined())
            return;
        Configuration config = createConfiguration(instance);
        File[] hbmFiles = HibernateSessionCreator.getHibernateConfigurationFiles();
        if (hbmFiles == null)
            throw new RuntimeException("No Hibernate mapping files found!");

        LOG.info("Hibernate Mapping files being used:");
        for (File file : hbmFiles) {
            LOG.info(file.getAbsolutePath());
        }

        // first add filter.hbm.xml bug in Hibernate!!
        for (File configurationFile : hbmFiles) {
            if (configurationFile.getName().startsWith("filters.")) {
                config.addFile(configurationFile);
                break;
            }
        }
        // now add the others
        for (File configurationFile : hbmFiles) {
            if (!configurationFile.getName().startsWith("filter.")) {
                config.addFile(configurationFile);
            }
        }
        HibernateUtil.init(config.buildSessionFactory());
        LOG.info("Hibernate session defined: " + HibernateUtil.hasSessionFactoryDefined());
    }

    private Configuration createConfiguration(String db) {
        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", "org.zfin.database.ZfinInformixDialect");
        config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
        String informixServer = ZfinPropertiesEnum.INFORMIX_SERVER.value();
        String informixPort = ZfinPropertiesEnum.INFORMIX_PORT.value();
        String sqlHostsHost = ZfinPropertiesEnum.SQLHOSTS_HOST.value();
        String connectionString = "jdbc:informix-sqli://" + sqlHostsHost + ":" + informixPort + "/" + db + ":INFORMIXSERVER=" + informixServer;
        connectionString += ";IFX_LOCK_MODE_WAIT=9;defaultIsolationLevel=1";
        LOG.info("connectionString: " + connectionString);
        config.setProperty("hibernate.connection.url", connectionString);
        config.setProperty("hibernate.connection.username", "zfinner");
        config.setProperty("hibernate.connection.password", "Rtwm4ts");
        config.setProperty("hibernate.connection.autocommit", "false");

        if (LOG.getLevel() != null && LOG.getLevel().equals(Level.DEBUG)) {
            config.setProperty("hibernate.show_sql", "true");
            config.setProperty("hibernate.format_sql", "true");
        }
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.connection.autocommit", "false");

        config.setProperty("hibernate.cache.provider_class", "net.sf.ehcache.hibernate.EhCacheProvider");
        config.setProperty("hibernate.cache.provider_configuration_file_resource_path", "conf");
        config.setProperty("hibernate.cache.use_second_level_cache", "false");
        config.setProperty("hibernate.cache.use_query_cache", "true");

        return config;
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
        RunSqlQueryTask task = new RunSqlQueryTask();
        task.setBaseDir(baseDir);
        task.setInstance(instance);
        task.setSqlFileName(dbQueryFile);
        task.execute();
    }
}
