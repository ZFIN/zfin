package org.zfin.properties;


import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Class is used to set the context so that later ZfinProperties.init can find the proper location
 * of the properties files.
 *
 * TODO: Use this class to load in the property file.
 *
 */
public class ZfinPropertiesLoadListener implements ServletContextListener {

    private static final Logger logger = LogManager.getLogger(ZfinPropertiesLoadListener.class);

    private static String webRoot = null ;
    private static ServletContext servletContext = null ;

    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
        webRoot = servletContext.getRealPath(".") ;
    }

    public static String getWebRoot() {
        return webRoot;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public void contextDestroyed(ServletContextEvent sce) { }
}
