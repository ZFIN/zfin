package org.zfin.framework;

import org.hibernate.stat.Statistics;
import org.springframework.web.servlet.DispatcherServlet;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.infrastructure.EnumValidationException;
import org.zfin.infrastructure.EnumValidationService;
import org.zfin.ontology.RelationshipDisplayNames;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.blast.WebHostDatabaseStatisticsCache;
import org.zfin.uniquery.categories.SiteSearchCategories;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;

/**
 * <p>
 * Master Servlet that controls each request and response.
 * 1) Initialize Hibernate
 * 2) Initialize ZFIN properties
 * 3) Runs db dictionary consistency tests
 * <p/>
 * ToDo: the different initializations could be done in a better way by registering
 * plug-in classes whose init() methods all get called. Inherit from
 * PlugIn class.
 */
public class ZfinActionServlet extends DispatcherServlet {

    private String webRoot;

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
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue(webRoot);
        ZfinPropertiesEnum.INDEXER_DIRECTORY.setValue(getServletContext().getInitParameter("quicksearch-index-directory"));
        initCategories();
        // Added this to the application context to make it easier to use global values.
        // ToDo: Should add all global parameters into application context and have it added
        // to the right context. There might be parameters that should only apply on a session scope...
        config.getServletContext().setAttribute("webdriverURL", ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value());
        initDatabase();
        startupTests();
        if(Boolean.valueOf(ZfinPropertiesEnum.BLAST_CACHE_AT_STARTUP.value())){
            initBlast();
        }
        initRelationshipDisplayNames();
    }

    private void initRelationshipDisplayNames() {
        new RelationshipDisplayNames();
    }

    private void initBlast() {
        Thread t = new Thread() {
            @Override
            public void run() {
                WebHostDatabaseStatisticsCache.getInstance().cacheAll();
                HibernateUtil.closeSession();
                GBrowseHibernateUtil.closeSession();
                SysmasterHibernateUtil.closeSession();
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
        // initialize Hibernate
        HibernateUtil.init();
        GBrowseHibernateUtil.init();
        SysmasterHibernateUtil.init();
        Statistics stats = HibernateUtil.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
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
