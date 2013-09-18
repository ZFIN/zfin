package org.zfin.framework;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.zfin.properties.ZfinPropertiesEnum;

/**
 * Based on HibernateUtil, but greatly simplified since our GBrowse
 * database needs aren't on the same scale.
 */

public class GBrowseHibernateUtil {

    private static String FILE_SEP = System.getProperty("file.separator");


    public static Logger log = Logger.getLogger(GBrowseHibernateUtil.class);

    public static SessionFactory gbrowseSessionFactory;

    //It would be sensible to name this localSession, but because it is threadlocal,
    //it will conflict with the HibernateUtil localSession variable.
    private static final ThreadLocal<Session> localGBrowseSession = new ThreadLocal<Session>();

    public static void init() {
        try {
            if (gbrowseSessionFactory != null) {
                throw new RuntimeException("GBrowse SessionFactory already instantiated.");
            }

            Configuration config = new Configuration().configure("hibernate-gbrowse.cfg.xml");

            config.setProperty("connection.datasource", "java:comp/env/jdbc/gbrowse");
            //config.setProperty("dialect", "org.hibernate.dialect.MySQLDialect");
            config.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");

            gbrowseSessionFactory = config.buildSessionFactory();

        } catch (Throwable ex) {
            log.error("GBrowse SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }


    public static void initForTest() {
        try {
            if (gbrowseSessionFactory != null) {
                throw new RuntimeException("GBrowse SessionFactory already instantiated.");
            }

            Configuration config = new Configuration();


            String mysqlServer = ZfinPropertiesEnum.GBROWSE_DB_HOST.value();
            String mysqlPort = "3306"; //the default, we're unlikely to stray from it

            String mysqlDB = ZfinPropertiesEnum.GBROWSE_DB.value();
            String connectionString = "jdbc:mysql://" + mysqlServer + ":" + mysqlPort + "/" + mysqlDB;
            log.debug(connectionString);
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            config.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");

            config.setProperty("hibernate.connection.url", connectionString);

            config.setProperty("hibernate.connection.username", "nobody");
            config.setProperty("hibernate.connection.pool_size", "1");

            config.setProperty("hibernate.show_sql", "false");
            config.setProperty("hibernate.format_sql", "true");
            config.addFile(ZfinPropertiesEnum.HIBERNATE_CONFIGURATION_DIRECTORY+ FILE_SEP + "gbrowse.hbm.xml");


            gbrowseSessionFactory = config.buildSessionFactory();

        } catch (Throwable ex) {
            log.error("GBrowse SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }


    public static Session currentSession() {
        Session s = localGBrowseSession.get();
        if (s == null) {
            if(gbrowseSessionFactory == null)
                initForTest();
            s = gbrowseSessionFactory.openSession();
            localGBrowseSession.set(s);
        }
        return s;
    }

    public static void closeSession() {
        Session s = localGBrowseSession.get();
        localGBrowseSession.set(null);
        if (s != null) {
            s.close();
        }
    }

    public static boolean hasSessionFactoryDefined() {
        return gbrowseSessionFactory != null;
    }

    public static SessionFactory getSessionFactory() {
        return gbrowseSessionFactory;
    }

    public static void setSesssionFactory(SessionFactory newSessionFactory) {
        gbrowseSessionFactory = newSessionFactory;
    }


}
