package org.zfin.framework;

import org.hibernate.cfg.Configuration;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Class HibernateSessionCreator.  Used to handle connections without going through Tomcat explicitly.
 * <p/>
 */
public class HibernateSessionCreator {

    private static String FILE_SEP = System.getProperty("file.separator");

    private boolean showSql = false;

    public HibernateSessionCreator() {
        this(false);
    }
    public HibernateSessionCreator(boolean showSql) {
        this.showSql = showSql;
        String db = ZfinPropertiesEnum.DB_NAME.value() ;
        String configDirectory = ZfinPropertiesEnum.HIBERNATE_CONFIGURATION_DIRECTORY.value() ;
        String showSqlString = ZfinPropertiesEnum.SHOW_SQL.value() ;
        if (showSqlString != null && showSqlString.equals("true")) {
            this.showSql = true;
        }
        if (db == null || configDirectory == null) {
            throw new RuntimeException("Failed to instantiate the the db-name [" + db + "] and configDirectory[" + configDirectory + "]");
        }
        Configuration config = createConfiguration(db);
        File[] hbmFiles = getHibernateConfigurationFiles();
        if (hbmFiles != null) {
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
        }
    }

    private static File[] getHibernateConfigurationFiles() {
        File hibernateConfDir = FileUtil.createFileFromStrings("source", "org", "zfin");
        return hibernateConfDir.listFiles(new HibernateFilenameFilter());
    }

    static class HibernateFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.endsWith(".hbm.xml");
        }
    }

    private Configuration createConfiguration(String db) {
        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.InformixDialect");
        config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
        String informixServer = ZfinPropertiesEnum.INFORMIX_SERVER.value() ;
        String informixPort = ZfinPropertiesEnum.INFORMIX_PORT.value() ;
        String sqlHostsHost = ZfinPropertiesEnum.SQLHOSTS_HOST.value() ;
        String connectionString = "jdbc:informix-sqli://" + sqlHostsHost + ":" + informixPort + "/" + db + ":INFORMIXSERVER=" + informixServer;
//        System.out.println("connectionString: " + connectionString) ; 
        config.setProperty("hibernate.connection.url", connectionString);
        config.setProperty("hibernate.connection.username", "zfinner");
        config.setProperty("hibernate.connection.password", "Rtwm4ts");
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.connection.autocommit", "false");
        config.setProperty("hibernate.connection.isolation", "1");
        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
        config.setProperty("hibernate.show_sql", Boolean.toString(showSql));
        config.setProperty("hibernate.format_sql", "true");
        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
        config.setProperty("hibernate.cache.provider_class", "net.sf.ehcache.hibernate.EhCacheProvider");
        config.setProperty("hibernate.cache.provider_configuration_file_resource_path", "conf");
        config.setProperty("hibernate.cache.use_second_level_cache", "true");
        config.setProperty("hibernate.cache.use_query_cache", "true");
//        config.setProperty("hibernate.use_sql_comments", "true");
        return config;
    }


} 


