package org.zfin.ontology.datatransfer;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateSysmasterSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.SysmasterHibernateUtil;
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

    protected void initAll(String propertyDirectory) {
        ZfinProperties.init(propertyDirectory);
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
        SessionFactory sessionSysmasterFactory = SysmasterHibernateUtil.getSessionFactory();
        if (sessionSysmasterFactory == null) {
            new HibernateSysmasterSessionCreator();
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

    public static CommandLine parseArguments(String[] args, String usageHelpMessage) {
        CommandLine commandLine = null;
        try {
            // parse the command line arguments
            CommandLineParser parser = new GnuParser();
            commandLine = parser.parse(options, args);
        } catch (ParseException exp) {
            LOG.error("Parsing failed.  Reason: " + exp.getMessage());
            System.exit(-1);
        }
        if (commandLine == null || commandLine.getOptions().length == 0) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(usageHelpMessage, options);
            System.exit(-1);
        }
        return commandLine;
    }

    public static void initializeLogger(String log4jFilename) {
        DOMConfigurator.configure(log4jFilename);
    }

    private static final Logger LOG = Logger.getLogger(AbstractScriptWrapper.class);
}
