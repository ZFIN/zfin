package org.zfin.framework;

import org.springframework.web.servlet.DispatcherServlet;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.infrastructure.EnumValidationException;
import org.zfin.infrastructure.EnumValidationService;
import org.zfin.properties.ZfinProperties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;

/**
 * Master Servlet that controls each request and resonse.
 * 1) Initialize Hibernate
 * 2) Initialize ZFIN properties
 * 3) Runs db dictionary consistency tests
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
        initProperties();
        initDatabase();
        try {
            startupTests();
        }
        catch (RuntimeException rte) {
            logger.error("error in enumeration validation.", rte);
            Throwable rootCause = rte; // set a default
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            StackTraceElement[] elements = rootCause.getStackTrace();
            String errorString = rootCause.getMessage() + "\n";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            logger.error("notification sent: " + (new IntegratedJavaMailSender()).sendMail("Enumeration Mapping Failure", "Enumeration mapping failure." +
                    "\n" + errorString, ZfinProperties.getValidationOtherEmailAddresses()));
        }
    }

    public void startupTests() {
        EnumValidationService service = new EnumValidationService();
        try {
            service.checkAllEnums();
        }
        catch (EnumValidationException eve) {
            throw new RuntimeException("EnumValidationException caught", eve);
        }
        if (service.getReport() != null)
            throw new RuntimeException(service.getReport());
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

    // ToDo: create utilities method that takes an array of dir's and creates
    // a valid file name.
    private String getConcatenatedDir(String dir1, String dir2) {
        File file1 = new File(dir1);
        File file2 = new File(file1, dir2);
        return file2.getAbsolutePath();
    }


}
