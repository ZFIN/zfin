package org.zfin;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

/**
 * This class sets up a test database (Hypersonic) for unit testing.
 */
public abstract class AbstractDatabaseTest {

    private static boolean firstTime = true;

    public AbstractDatabaseTest() {
        if (firstTime)
            init();
        firstTime = false;
    }

    public void init() {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    @After
    public void closeSession() {
        HibernateUtil.rollbackTransaction();
    }

    @Before
    public void startTransaction() {
        HibernateUtil.currentSession().beginTransaction();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestConfiguration.setAuthenticatedUser();
    }



}
