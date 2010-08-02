package org.zfin;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;

/**
 * This initializes the database.
 */
public class AbstractDbUnitTest {

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }


}
