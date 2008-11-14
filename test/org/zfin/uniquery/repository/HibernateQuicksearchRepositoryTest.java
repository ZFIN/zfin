package org.zfin.uniquery.repository;

import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.TestConfiguration;
import org.zfin.infrastructure.ReplacementZdbID;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

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