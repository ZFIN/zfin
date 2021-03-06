package org.zfin.framework;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.FactoryBean;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Class HibernateSessionCreator.  Used to handle connections without going through Tomcat explicitly.
 * <p/>
 */
public class HibernateComparisonSessionFactory implements FactoryBean {

    private static final Logger LOG = LogManager.getLogger(HibernateComparisonSessionFactory.class);

    private static String FILE_SEP = System.getProperty("file.separator");

    private boolean showSql = false;
    private SessionFactory sessionFactory;

    public HibernateComparisonSessionFactory(String db, boolean showSql) {
        LOG.info("Start Hibernate Session Creation");
        this.showSql = showSql;
        LOG.info("Database used:" + db);
        String configDirectory = ZfinPropertiesEnum.HIBERNATE_CONFIGURATION_DIRECTORY.value();
        String showSqlString = ZfinPropertiesEnum.SHOW_SQL.value();
        if (showSqlString != null && showSqlString.equals("true")) {
            this.showSql = true;
        }
        if (db == null || configDirectory == null) {
            throw new RuntimeException("Failed to instantiate the the db-name [" + db + "] and configDirectory[" + configDirectory + "]");
        }
        Configuration config = createConfiguration(db);
        File[] hbmFiles = getHibernateConfigurationFiles();
        if (hbmFiles == null)
            throw new RuntimeException("No Hibernate mapping files found!");

        LOG.debug("Hibernate Mapping files being used:");
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
        sessionFactory = config.buildSessionFactory();
    }

    public Session getSession() {
        return sessionFactory.openSession();
    }

    public void closeSession() {
        if (sessionFactory != null)
            sessionFactory.close();
    }


    private static File[] getHibernateConfigurationFiles() {
        // first in the source directory
        File hibernateConfDir = FileUtil.createFileFromStrings("source", "org", "zfin", "database");
        // if not found in the source (used for testing) then check in the classpath
        if (hibernateConfDir == null || !hibernateConfDir.exists()) {
            // a bit hacky...
            ClassLoader cl = HibernateComparisonSessionFactory.class.getClassLoader();
            String directory = cl.getResource("org/zfin/database/sysmaster.hbm.xml").toString();
            directory = directory.substring(0, directory.lastIndexOf("/"));
            directory = directory.replace("file:/", "");
            hibernateConfDir = FileUtil.createFileFromStrings(directory);
        }
        File[] hibernateConfigurationFiles = hibernateConfDir.listFiles(new HibernateFilenameFilter());
        if (hibernateConfigurationFiles == null) {
            // then search in the classpath
            URL resource = ClassLoader.getSystemResource(FileUtil.createFileFromStrings("org", "zfin", "database").getPath());

            String hibernateConfDirString = URLDecoder.decode(resource.getFile());
            LOG.info("hibernateConfDirString: " + hibernateConfDirString);
            hibernateConfigurationFiles = (new File(hibernateConfDirString)).listFiles(new HibernateFilenameFilter());
            if (hibernateConfigurationFiles == null)
                throw new NullPointerException("No configuration files found in directory" + hibernateConfDir.getAbsolutePath());
        }
        LOG.info("Hibernate Configuration files in directory: " + hibernateConfDir.getAbsolutePath());
        return hibernateConfigurationFiles;
    }

    @Override
    public Object getObject
            () throws Exception {
        return sessionFactory;
    }

    @Override
    public Class getObjectType
            () {
        return (this.sessionFactory != null) ? this.sessionFactory.getClass() : SessionFactory.class;
    }

    @Override
    public boolean isSingleton
            () {
        return true;
    }

    static class HibernateFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.endsWith(".hbm.xml");
        }
    }

    private Configuration createConfiguration(String db) {
        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", "org.zfin.database.ZfinInformixDialect");
        config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
        String informixServer = "ToBeDefined";
        String informixPort = "ToBeDefined";
        String sqlHostsHost = ZfinPropertiesEnum.SQLHOSTS_HOST.value();
        String connectionString = "jdbc:informix-sqli://" + sqlHostsHost + ":" + informixPort + "/" + db + ":INFORMIXSERVER=" + informixServer + ";DB_LOCALE=en_US.utf8";
//        System.out.println("connectionString: " + connectionString) ; 
        config.setProperty("hibernate.connection.url", connectionString);
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.connection.autocommit", "false");

        // should use the default isolation
//        config.setProperty("hibernate.connection.isolation", "1");
//        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
        config.setProperty("hibernate.show_sql", Boolean.toString(showSql));
        config.setProperty("hibernate.format_sql", "true");
//        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
        config.setProperty("hibernate.cache.provider_configuration_file_resource_path", "conf");
//        config.setProperty("hibernate.use_sql_comments", "true");
        return config;
    }


} 


