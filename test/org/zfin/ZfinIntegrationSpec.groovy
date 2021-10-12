package org.zfin

import org.hibernate.SessionFactory
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import spock.lang.Specification

@WebAppConfiguration
@ContextConfiguration(locations = "file:home/WEB-INF/spring/mvc-webapp.xml")
@RunWith(SpringJUnit4ClassRunner.class)
abstract class ZfinIntegrationSpec extends Specification {

    public def setupSpec() {
        TestConfiguration.configure()
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory()
        if (sessionFactory == null) {
            new HibernateSessionCreator(false)
        }
    }

    public def cleanupSpec() {
        HibernateUtil.closeSession()
    }

}
