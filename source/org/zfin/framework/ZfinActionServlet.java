package org.zfin.framework;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.web.servlet.DispatcherServlet;
import org.zfin.infrastructure.EnumValidationException;
import org.zfin.infrastructure.EnumValidationService;
import org.zfin.properties.ZfinProperties;
import org.zfin.util.FileUtil;
import org.zfin.framework.mail.MailXMailSender;
import org.zfin.framework.mail.MailSender;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import static org.junit.Assert.fail;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

/**
 * Master Servlet that controls each request and resonse.
 * 1) Initialize log4j
 * 2) Initialize Hibernate
 * 3) Initialize ZFIN properties
 * <p/>
 * //ToDo: the different initializations could be done in a better way by registering
 * plug-in classes whose init() methods all get called. Inherit from
 * PlugIn class.
 */
public class ZfinActionServlet extends DispatcherServlet {

    public static final String PROPERTY_FILE_NAME = "zfin-properties.xml";
    private String webRoot;

    private static final String PROPERTY_FILE_NAME_PARAM = "property-file";
    private static final String PROPERTY_FILE_DIR_PARAM = "property-file-directory";

    private static final String LOG4J_FILE = "log4j-init-file";
    private static final String LOG4J_DIR = "log4j-init-file-directory";

    /**
     * This method is called the first time this servlet is instantiated.
     *
     * @param config ServletConfig
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        webRoot = getServletContext().getRealPath("/");
        // make web root dir available to the application
        ZfinProperties.setWebRootDirectory(webRoot);
        try {
            initProperties();
//        initLog4J();
            initDatabase();
            startupTests();
        }
        catch (RuntimeException rte) {
            logger.error("error in enumeration validation.", rte);
            Throwable rootCause = rte; // set a default
            while(rootCause.getCause()!=null){
                rootCause = rootCause.getCause() ;
            }
            StackTraceElement[] elements = rootCause.getStackTrace();
            String errorString = rootCause.getMessage() + "\n";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            logger.error("notification sent: "+ (new IntegratedJavaMailSender()).sendMail("Enumeration Mapping Failure","Enumeration mapping failure." +
                    "\n"+errorString, ZfinProperties.getValidationOtherEmailAddresses()));
        }
    }

    public void startupTests() {
        try {
            (new EnumValidationService()).checkAllEnums();
        }
        catch (EnumValidationException eve) {
            throw new RuntimeException("EnumValidationException caught", eve);
        }
    }

    /**
     * Add a file appender with a configurable file location.
     */
    private void initLog4J() {
        String log4jFileName = getLog4JFile();
        // if the log4j-init-file is set do not initialize log4j.
        if (log4jFileName != null) {
            DOMConfigurator.configure(log4jFileName);
        }
        // temporarily commented out. the catalina.out contains all info you need.
        // This extra log file was meant to be for cases where all developer tomcats
        // wrote into the same catalina.out file.
        // addRootAppender();
        Logger log = Logger.getLogger(ZfinActionServlet.class);
        log.info("Initialization of ZfinActionServlet");

    }

    private void addRootAppender() {
        Logger rootLogger = Logger.getRootLogger();

        String logFileName = ZfinProperties.getLogFileName();
        //TODO: make this access more generic
        // independent of the servlet container.
        String absoluteFilePath = getFullLogFileName(logFileName);
        RollingFileAppender appender = null;
        try {
            String logFilePattern = ZfinProperties.getLogFilePattern();
            appender = new RollingFileAppender(new PatternLayout(logFilePattern), absoluteFilePath);
            appender.setMaximumFileSize(ZfinProperties.getLogFileSize());
            appender.setAppend(true);
            appender.setMaxBackupIndex(ZfinProperties.getMaxlogFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
        rootLogger.addAppender(appender);
    }

    private String getLog4JFile() {
        String file = getInitParameter(LOG4J_FILE);
        String dir = getInitParameter(LOG4J_DIR);
        File log4jFile = FileUtil.createFile(webRoot, dir, file);
        String log4jFileName = log4jFile.getAbsolutePath();
        if (!log4jFile.exists())
            System.out.println("Cannot find log4j file: " + log4jFileName);
        return log4jFileName;
    }

    private String getFullLogFileName(String logFileName) {
        return FileUtil.createAbsolutePath(FileUtil.getCatalinaLogDirectory(), logFileName);
    }


    private void initDatabase() {
/*
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");

            DataSource ds = (DataSource)
                    envCtx.lookup("jdbc/zfin");

            Connection conn = ds.getConnection();
            conn.close();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
*/
        // initialize Hibernate
        HibernateUtil.init();
    }

    /**
     * Initialize the Zfin Properties by reading the property file and
     * making the parameters available.
     */
    private void initProperties() {
        String file = getInitParameter(PROPERTY_FILE_NAME_PARAM);
        String dirRel = getInitParameter(PROPERTY_FILE_DIR_PARAM);
        String dir = getConcatenatedDir(webRoot, dirRel);
        ZfinProperties.init(dir, file);
    }

    // ToDo: create utilities method that takesa an array of dir's and creates
    // a valid file name.
    private String getConcatenatedDir(String dir1, String dir2) {
        File file1 = new File(dir1);
        File file2 = new File(file1, dir2);
        return file2.getAbsolutePath();
    }


}
