package org.zfin.infrastructure.ant;

import freemarker.template.SimpleDate;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.zfin.database.DatabaseService;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 */
public class CreateValidateDataReportTask {

    private final Logger LOG = Logger.getLogger(CreateValidateDataReportTask.class);

    private String instance;
    private String baseDir;
    private String jobName;
    private String propertyFilePath;
    private File baseValidateDataDirectory;
    private File queryFile;
    private String templateName = "validate-data-email-report.template";
    private String propertiesFile = "validate-data-email-report.properties";
    private Properties reportProperties;

    public void execute() {
        System.out.println("Job Name: " + jobName);
        System.out.println("Validate Data Directory: " + baseDir);
        System.out.println("Property Path: " + propertyFilePath);

        LOG.info("Running SQLQueryTask on instance: " + instance);
        baseValidateDataDirectory = new File(baseDir);
        if (!baseValidateDataDirectory.exists()) {
            String message = "No directory found: " + baseValidateDataDirectory.getAbsolutePath();
            LOG.error(message);
            throw new RuntimeException(message);
        }
        queryFile = new File(baseValidateDataDirectory, jobName + ".sql");
        if (!queryFile.exists()) {
            String message = "No file found: " + queryFile.getAbsolutePath();
            LOG.info(message);
            throw new RuntimeException(message);
        }
        LOG.info("Handling file : " + queryFile.getAbsolutePath());
        ZfinProperties.init(propertyFilePath);
        initDatabase();
        reportProperties = new Properties();
        try {
            reportProperties.load(new FileInputStream(new File(baseValidateDataDirectory, propertiesFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        runQueryFile(queryFile);
    }

    private void runQueryFile(File dbQueryFile) {
        DatabaseService service = new DatabaseService();
        HibernateUtil.currentSession().beginTransaction();
        List<String> errorMessages = null;
        try {
            errorMessages = service.runDbScriptFile(dbQueryFile);
            List<List<List<String>>> resultList = service.getListOfResultRecords();
            createErrorReport(errorMessages, resultList);
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.rollbackTransaction();
            HibernateUtil.closeSession();
        }
    }

    private void createErrorReport(List<String> errorMessages, List<List<List<String>>> resultList) {
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
            writer = new FileWriter(new File(baseValidateDataDirectory, jobName + ".report.html"));
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> root = new HashMap<>();
            root.put("errorMessage", reportProperties.get(jobName + ".errorMessage"));
            root.put("recordList", resultList.get(0));
            root.put("numberOfRecords", resultList.get(0).size());
            // header columns
            String[] headerCols = reportProperties.getProperty(jobName + ".headerColumns").split("\\|");
            root.put("headerColumns", headerCols);
            root.put("dateRun", new Date());
            root.put("sqlQuery", FileUtils.readFileToString(queryFile));
            template.process(root, writer);
            writer.flush();
            // export data
            StringBuilder lines = new StringBuilder();
            for (List<String> record : resultList.get(0)) {
                for (String element : record) {
                    lines.append(element);
                    lines.append(",");
                }
                lines.deleteCharAt(lines.length() - 1);
                lines.append("\n");
            }
            FileUtils.writeStringToFile(new File(baseValidateDataDirectory, jobName + ".txt"), lines.toString());
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

        config.setProperty("hibernate.cache.provider_configuration_file_resource_path", "conf");
        config.setProperty("hibernate.cache.use_second_level_cache", "false");
        config.setProperty("hibernate.cache.use_query_cache", "false");

        return config;
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

    public static void main(String[] args) {
        String instance = args[0];
        String jobName = args[1];
        String directory = args[2];
        String propertyFilePath = args[3];
        CreateValidateDataReportTask task = new CreateValidateDataReportTask();
        task.setInstance(instance);
        task.setJobName(jobName);
        task.setBaseDir(directory);
        task.setPropertyFilePath(propertyFilePath);
        task.execute();
    }
}
