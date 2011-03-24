package org.zfin.framework;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Based on HibernateUtil, but greatly simplified since our sysmaster
 * database needs aren't on the same scale.
 */

public class SysmasterHibernateUtil {

    public static Logger log = Logger.getLogger(SysmasterHibernateUtil.class);
    public static SessionFactory sessionFactory;

    /**
     * Initialize the session with a given session factory.
     *
     * @param factory passed in
     */
    public static void init(SessionFactory factory) {
        if (sessionFactory != null)
            throw new RuntimeException("SessionSysmasterFactory already instantiated");
        sessionFactory = factory;
    }

    public static void init() {
        try {
            if (sessionFactory != null) {
                log.warn("Sysmaster SessionFactory already instantiated.");
                return;
            }
            Configuration config = new Configuration().configure("hibernate-sysmaster.cfg.xml");
            config.setProperty("connection.datasource", "java:comp/env/jdbc/sysmaster");
            config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
            sessionFactory = config.buildSessionFactory();

        } catch (Throwable ex) {
            log.error("Sysmaster SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static void closeSession() {
	if(sessionFactory != null)
	    sessionFactory.close();
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
