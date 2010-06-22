package org.zfin.framework;

import org.hibernate.stat.Statistics;
import org.springframework.web.servlet.DispatcherServlet;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.infrastructure.EnumValidationException;
import org.zfin.infrastructure.EnumValidationService;
import org.zfin.ontology.OntologyManager;
import org.zfin.properties.ZfinProperties;
import org.zfin.sequence.blast.WebHostDatabaseStatisticsCache;
import org.zfin.uniquery.categories.SiteSearchCategories;

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
    private static final String SITE_SEARCH_CATEGORIES_FILE = "site-search-category-file";

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
        ZfinProperties.setIndexDirectory(getServletContext().getInitParameter("quicksearch-index-directory"));
        initProperties();
        initCategories();
        // Added this to the application context to make it easier to use global values.
        // ToDo: Should add all global parameters into application context and have it added
        // to the right context. There might be parameters that should only apply on a session scope...
        config.getServletContext().setAttribute("webdriverURL", ZfinProperties.getWebDriver());
        initDatabase();
        startupTests();
        initOntologies() ;
        initBlast();
    }

    private void initOntologies() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    OntologyManager.getInstance(OntologyManager.LoadingMode.SERIALIZED_FILE)  ;
                } catch (Exception e) {
                    logger.error("Failed to load ontologies from serial file, loading from database",e);
                }
                
                try {
                    OntologyManager.getInstance().reLoadOntologies();
//                    OntologyManager.getInstance(OntologyManager.LoadingMode.DATABASE).serializeOntologies();  ;
                } catch (Exception e1) {
                    logger.fatal("Failed to load ontologies from the database",e1);
                }
            }
        };
        t.start();
    }

    private void initBlast() {
        Thread t = new Thread() {
            @Override
            public void run() {
                WebHostDatabaseStatisticsCache.getInstance().cacheAll();
                HibernateUtil.closeSession();
                GBrowseHibernateUtil.closeSession();
            }
        };
        t.start();
    }

    public void startupTests() {
        EnumValidationService service = new EnumValidationService();
        try {
            service.checkAllEnums();
            if (service.getReport() != null) {
                throw new EnumValidationException(service.getReport());
            }
        }
        catch (EnumValidationException eve) {
            logger.error("Error in enumeration validation.", eve);
            Throwable rootCause = eve; // set a default
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
        GBrowseHibernateUtil.init();
        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
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

    /**
     * Initialize the Zfin Site search categories by reading the property file and
     * making the parameters available.
     */
    private void initCategories() {
        String file = getInitParameter(SITE_SEARCH_CATEGORIES_FILE);
        String dirRel = getInitParameter(PROPERTY_FILE_DIR_PARAM);
        String dir = getConcatenatedDir(webRoot, dirRel);
        dir = getConcatenatedDir(dir, "conf");
        SiteSearchCategories.init(dir, file);
        // Check if any type strings collide with stop words.
        SiteSearchCategories.getAllSearchCategories();
    }

    // ToDo: create utilities method that takes an array of dir's and creates
    // a valid file name.

    private String getConcatenatedDir(String dir1, String dir2) {
        File file1 = new File(dir1);
        File file2 = new File(file1, dir2);
        return file2.getAbsolutePath();
    }


}
