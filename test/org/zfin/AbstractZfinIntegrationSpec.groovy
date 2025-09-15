package org.zfin

import org.hibernate.SessionFactory
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil

/*
* Basic setup & cleanup for tests that need to use the database
* */
abstract class AbstractZfinIntegrationSpec extends AbstractZfinSpec {

    public void setupSpec() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    public void cleanupSpec() {
        HibernateUtil.closeSession();
    }



}
