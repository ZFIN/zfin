package org.zfin

import org.hibernate.SessionFactory
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import spock.lang.Specification


abstract class ZfinIntegrationSpec extends Specification {

    def setupSpec() {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    def cleanupSpec() {
        HibernateUtil.closeSession();
    }



}
