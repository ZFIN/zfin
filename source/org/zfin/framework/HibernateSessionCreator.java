package org.zfin.framework;

import org.hibernate.cfg.Configuration;

/**
 * Class HibernateSessionCreator.  Used to handle connections without going through Tomcat explicitly.
 * <p/>
 * Java environment parameters:
 * -DDBNAME=<mutant_db> -DCONFIGURATION_DIRECTORY=<directory of hbm-files> -DINFORMIXSERVER=<informix server, devel is wanda> -DSQLHOSTS_HOST=<informix host machine> -DINFORMIXPORT=<database listening port> -DshowSQL=true
 */
public class HibernateSessionCreator {

    private static String FILE_SEP = System.getProperty("file.separator");

    private static final String INPUT_DBNAME = "DBNAME";
    private static final String INPUT_CONFIGURATION_DIRECTORY = "CONFIGURATION_DIRECTORY";
    private static final String INPUT_INFORMIX_SERVER = "INFORMIX_SERVER";
    private static final String INPUT_INFORMIX_PORT = "INFORMIX_PORT";
    private static final String INPUT_SQLHOSTS_HOST = "SQLHOSTS_HOST";
    private static final String SHOW_SQL = "showSQL";

    private boolean showSql = false;

    public HibernateSessionCreator(String... hbmFiles) {
        this(false, hbmFiles);
    }

    public HibernateSessionCreator(boolean showSql, String... hbmFiles) {
        this.showSql = showSql;
        String db = System.getProperty(INPUT_DBNAME);
        String configDirectory = System.getProperty(INPUT_CONFIGURATION_DIRECTORY);
        String showSqlString = System.getProperty(SHOW_SQL);
        if (showSqlString != null && showSqlString.equals("true")){
            this.showSql = true;
        }
        if (db == null || configDirectory == null) {
            throw new RuntimeException("Failed to instantiate the the db-name [" + db + "] and configDirectory[" + configDirectory + "]");
        }
        Configuration config = createConfiguration(db);
        if (hbmFiles != null) {
            for (String confFile : hbmFiles) {
                config.addFile(configDirectory + FILE_SEP + confFile);
            }
        }
        HibernateUtil.init(config.buildSessionFactory());
    }

    public void initHibernate() {

    }

    private Configuration createConfiguration(String db) {
        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.InformixDialect");
        config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
        String informixServer = System.getProperty(INPUT_INFORMIX_SERVER);
        String informixPort = System.getProperty(INPUT_INFORMIX_PORT);
        String sqlHostsHost = System.getProperty(INPUT_SQLHOSTS_HOST);
        String connectionString = "jdbc:informix-sqli://" + sqlHostsHost + ":" + informixPort + "/" + db + ":INFORMIXSERVER=" + informixServer;
//        System.out.println("connectionString: " + connectionString) ; 
        config.setProperty("hibernate.connection.url", connectionString);
        config.setProperty("hibernate.connection.username", "zfinner");
        config.setProperty("hibernate.connection.password", "Rtwm4ts");
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.connection.autocommit", "false");
        config.setProperty("hibernate.connection.isolation", "1");
        config.setProperty("hibernate.show_sql", Boolean.toString(showSql));
//        config.setProperty("hibernate.format_sql", "true");
//        config.setProperty("hibernate.use_sql_comments", "true");
        return config;
    }


} 


