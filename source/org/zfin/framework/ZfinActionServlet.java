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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
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
        // make web root dir available to the application
        webRoot = getServletContext().getRealPath("/");
        try {
            initProperties();
        } catch (TomcatStartupException e) {
            throw new RuntimeException("TomcatStartupException caught, Stopping server",e) ;
        }
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
    private void initProperties() throws TomcatStartupException{
        String instance = System.getenv("INSTANCE") ;
        if(instance==null){
            throw new TomcatStartupException("INSTANCE not defined in environment") ;
        }
        String propertiesFileString = webRoot + "/WEB-INF/properties/" + instance+".properties" ;
        File propertiesFile = new File(propertiesFileString) ;
        if(false==propertiesFile.exists()){
            throw new TomcatStartupException("Property file: " +propertiesFile.getAbsolutePath() +
                    " not found for INSTANCE: "+ instance) ;
        }
        ZfinProperties.init(propertiesFileString);
        ZfinProperties.validateProperties() ;
        checkDeployedInstance() ;
    }

    private void checkDeployedInstance() throws TomcatStartupException{
        File file = new File(webRoot+"/WEB-INF/INSTANCE") ;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file)) ;
            String instance = reader.readLine() ;
            reader.close();
            reader = null ;
            if(false==instance.equals(ZfinPropertiesEnum.INSTANCE.value())){
                throw new TomcatStartupException(
                        "Deployed instance["+instance+"] does not match " +
                                "loaded instance from environment : " + ZfinPropertiesEnum.INSTANCE.value() +
                                " current environment["+System.getenv("INSTANCE")+"]")  ;
            }
        } catch (Exception e) {
            throw new TomcatStartupException("INSTANCE not deployed properly due to error trying to load file: "+ file,e)  ;
        }
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
