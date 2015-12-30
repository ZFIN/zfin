package org.zfin.framework;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.InvalidMappingException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.expression.ExpressionFigureStage;
import org.zfin.expression.ExpressionPhenotypeTerm;
import org.zfin.expression.ExpressionResult2;
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
public class HibernateSessionCreator {

    public static final Logger LOG = RootLogger.getLogger(HibernateSessionCreator.class);

    private boolean showSql = false;
    private boolean autocommit = false;

    public HibernateSessionCreator() {
        this(false);
    }

    public HibernateSessionCreator(boolean showSql) {
        this(showSql, false);
    }

    public HibernateSessionCreator(boolean showSql, boolean autocommit) {
        this.autocommit = autocommit;
        LOG.info("Start Hibernate Session Creation");
        this.showSql = showSql;
        String db = ZfinPropertiesEnum.DB_NAME.value();
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
            LOG.debug(file.getAbsolutePath());
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
            if (!configurationFile.getName().startsWith("filters.")) {
                LOG.debug("Loading Hibernate mapping file: " + configurationFile.getAbsolutePath());
                try {
                    config.addFile(configurationFile);
                } catch (InvalidMappingException e) {
                    LOG.error("Error Loading Hibernate mapping file: " + configurationFile.getAbsolutePath());
                }
            }
        }
        HibernateUtil.init(config.addAnnotatedClass(ExpressionExperiment2.class)
                .addAnnotatedClass(ExpressionFigureStage.class)
                .addAnnotatedClass(ExpressionResult2.class)
                .addAnnotatedClass(ExpressionPhenotypeTerm.class).buildSessionFactory());
    }

    public static File[] getHibernateConfigurationFiles() {
        // first in the source directory
        File hibernateConfDir = FileUtil.createFileFromStrings("source", "org", "zfin");
        // if not found in the source (used for testing) then check in the classpath
        if (hibernateConfDir == null || !hibernateConfDir.exists()) {
            // a bit hacky...
            ClassLoader cl = HibernateSessionCreator.class.getClassLoader();
            String directory = cl.getResource("org/zfin/filters.hbm.xml").toString();
            directory = directory.substring(0, directory.lastIndexOf("/"));
            directory = directory.replace("file:/", "/");
            hibernateConfDir = FileUtil.createFileFromStrings(directory);
        }
        File[] hibernateConfigurationFiles = hibernateConfDir.listFiles(new HibernateFilenameFilter());
        if (hibernateConfigurationFiles == null) {
            // then search in the classpath
            URL resource = ClassLoader.getSystemResource(FileUtil.createFileFromStrings("org", "zfin").getPath());

            String hibernateConfDirString = URLDecoder.decode(resource.getFile());
            LOG.debug("hibernateConfDirString: " + hibernateConfDirString);
            hibernateConfigurationFiles = (new File(hibernateConfDirString)).listFiles(new HibernateFilenameFilter());
            if (hibernateConfigurationFiles == null)
                throw new NullPointerException("No configuration files found in directory" + hibernateConfDir.getAbsolutePath());
        }
        LOG.info("Hibernate Configuration files in directory: " + hibernateConfDir.getAbsolutePath());
        return hibernateConfigurationFiles;
    }

    static class HibernateFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.endsWith(".hbm.xml");
        }
    }

    private Configuration createConfiguration(String db) {
        Configuration config = new AnnotationConfiguration();
        config.setInterceptor(new StringCleanInterceptor());
        config.setProperty("hibernate.dialect", "org.zfin.database.ZfinInformixDialect");
        config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
        String informixServer = ZfinPropertiesEnum.INFORMIX_SERVER.value();
        String informixPort = ZfinPropertiesEnum.INFORMIX_PORT.value();
        String sqlHostsHost = ZfinPropertiesEnum.SQLHOSTS_HOST.value();
        String connectionString = "jdbc:informix-sqli://" + sqlHostsHost + ":" + informixPort + "/" + db + ":INFORMIXSERVER=" + informixServer;
        connectionString += ";IFX_LOCK_MODE_WAIT=7;defaultIsolationLevel=1";

//        System.out.println("connectionString: " + connectionString) ; 
        config.setProperty("hibernate.connection.url", connectionString);
        config.setProperty("hibernate.connection.username", "zfinner");
        config.setProperty("hibernate.connection.password", "Rtwm4ts");
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.connection.autocommit", String.valueOf(autocommit));

        // should use the default isolation
//        config.setProperty("hibernate.connection.isolation", "1");
//        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
        config.setProperty("hibernate.show_sql", Boolean.toString(showSql));
        config.setProperty("hibernate.format_sql", "true");
//        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
//        config.setProperty("hibernate.cache.provider_class", "net.sf.ehcache.hibernate.EhCacheProvider");
        config.setProperty("hibernate.cache.provider_configuration_file_resource_path", "conf");
        config.setProperty("hibernate.cache.use_second_level_cache", "false");
        //config.setProperty("hibernate.cache.use_query_cache", "true");
//        config.setProperty("hibernate.use_sql_comments", "true");
        return config;
    }


} 


