package org.zfin.uniquery.repository;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;

public class HibernateQuicksearchRepositoryTest  {

    private static QuicksearchRepository quickRep = RepositoryFactory.getQuicksearchRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

}