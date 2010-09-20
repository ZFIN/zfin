package org.zfin.properties;


import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 */
public class ZfinPropertiesLoadListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    private static final Logger logger = Logger.getLogger(ZfinPropertiesLoadListener.class);

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
    public void sessionCreated(HttpSessionEvent se) { }
    public void sessionDestroyed(HttpSessionEvent se) { }
    public void attributeAdded(HttpSessionBindingEvent sbe) { }
    public void attributeRemoved(HttpSessionBindingEvent sbe) { }
    public void attributeReplaced(HttpSessionBindingEvent sbe) { }
}
