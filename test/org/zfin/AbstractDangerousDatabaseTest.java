package org.zfin;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

/**
 * This class sets up the database for unit testing.
 * It does not rollback transactions after each test, so it is dangerous to use
 */
public abstract class AbstractDangerousDatabaseTest {

    private static boolean firstTime = true;

    public AbstractDangerousDatabaseTest() {
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

    @BeforeClass
    public static void setUpBeforeClass() {
        TestConfiguration.setAuthenticatedUser();
        System.setProperty("log4j.configurationFile","./test/log4j2.xml");
    }

}
