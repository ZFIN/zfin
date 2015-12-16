package org.zfin.framework;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.proxy.HibernateProxy;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.repository.RepositoryFactory;
import org.zfin.repository.SessionCreator;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for web applications to make a database session object
 * available via the framework of Hibernate.
 * <p/>
 * To obtain a new db connection call currentSession() and to close the
 * session call closeSession().
 * The first time the currentSession() method is called a new ZfinSession object
 * is created (or obtained from the connection pool) and put into a ThreadLocal
 * variable. At any later time during the execution of this thread this session
 * object is retrieved by a currentSession() call. This allows to create transactions
 * easily without the need to pass around the session object.
 */
public class HibernateUtil {

    private static Logger log = Logger.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory;

    private static final ThreadLocal<Session> localSession = new ThreadLocal<Session>();

    /**
     * Method that forces Hibernate to initialize.
     * If this method is called it initializes all static variables.
     */
    public static void init() {
        try {
            if (sessionFactory != null)
                throw new RuntimeException("SessionFactory already instantiated");
            // Create the SessionFactory
            Configuration configuration = new AnnotationConfiguration();
            configuration.setInterceptor(new StringCleanInterceptor());
            sessionFactory = configuration.configure().buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Initialize the session with a given session factory.
     *
     * @param factory passed in
     */
    public static void init(SessionFactory factory) {
        if (sessionFactory != null)
            throw new RuntimeException("SessionFactory already instantiated");
        sessionFactory = factory;
    }

    public static Transaction createTransaction() {
        return currentSession().beginTransaction();
    }

    public static void rollbackTransaction() {
        try {
            Transaction t = currentSession().getTransaction();
            t.rollback();
        } catch (HibernateException e) {
            log.error(e);
        }
    }

    public static void flushAndCommitCurrentSession() {
        currentSession().flush();
        currentSession().getTransaction().commit();
    }

    /**
     * Call this method to obtain a database session (transaction) object.
     * Within a single thread this method returns always the same object.
     *
     * @return current {@link org.hibernate.Session}
     * @throws org.hibernate.HibernateException
     *
     */
    public static Session currentSession() {
        // if no session factory is created yet we may be in hosted mode. Then
        // initialize
        if (sessionFactory == null) {
            SessionCreator.instantiateDBForHostedMode();
        }

        Session s = localSession.get();
        if (s != null)
            return s;
        // create a new session
        s = sessionFactory.openSession();
        localSession.set(s);
        s.enableFilter("noSecondaryAliasesForAO").setParameter("group", DataAliasGroup.Group.SECONDARY_ID.toString());
        return s;
    }

    /**
     * This method closes the session (transaction).
     *
     * @throws HibernateException
     */
    public static void closeSession() {
        Session s = localSession.get();
        localSession.set(null);
        if (s != null) {
            s.close();
        }
    }

    /**
     * Inquire if the session factory has already been initialized.
     *
     * @return boolean
     */
    public static boolean hasSessionFactoryDefined() {
        return sessionFactory != null;
    }

    /**
     * Do not use this to make any changes to the factory class.
     *
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void setSessionFactory(SessionFactory newSessionFactory) {
        sessionFactory = newSessionFactory;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            return null;
        }
        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
                    .getImplementation();
        }
        return entity;
    }

}
