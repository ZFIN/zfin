package org.zfin.ontology.datatransfer;

import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;

/**
 * Abstract class that provides basic support for running db scripts.
 */
public class AbstractScriptWrapper {

    protected static final Options options = new Options();
    //protected static ApplicationContext context = new FileSystemXmlApplicationContext("C:\\projects\\zfin\\mainline\\server_apps\\data_transfer\\LoadOntology\\spring-configuration.xml");
    protected CronJobUtil cronJobUtil;

    protected void initAll() {
        initProperties();
        initDatabase();
    }

    protected void initProperties() {
        ZfinProperties.init();
    }

    protected void initDatabase() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    protected static void initSpringConfiguration() {
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        String name = "";
    }

    protected void startTransaction() {
        HibernateUtil.createTransaction();
    }

    public enum ScriptExecutionStatus {
        SUCCESS, ERROR, WARNING, INFO, FATAL
    }

    private static final Logger LOG = Logger.getLogger(AbstractScriptWrapper.class);
}
