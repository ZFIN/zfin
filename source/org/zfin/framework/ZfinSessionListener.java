package org.zfin.framework;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.*;
import java.sql.Timestamp;

/**
 * This listener is called whenever a session object is created or removed and
 * also whenever an attribute in the session is added, modified or removed.
 * This filter is configured in the deployment descriptor file web.xml.
 */
public class ZfinSessionListener implements HttpSessionListener, HttpSessionAttributeListener, ServletContextListener {

    private static final Log LOG = LogFactory.getLog(ZfinSessionListener.class);

    public void attributeAdded(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        StringBuilder builder = new StringBuilder("ZfinSession modifed [SessionID = ");
        builder.append(session.getId());
        builder.append("]: Added Attribute: [");
        builder.append(event.getName());
        builder.append(",");
        builder.append(event.getValue());
        builder.append("]");
        ZfinStaticLogger.write(builder.toString());
    }

    public void attributeRemoved(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        StringBuilder builder = new StringBuilder("ZfinSession modifed [SessionID = ");
        builder.append(session.getId());
        builder.append("]: Removeds Attribute: [");
        builder.append(event.getName());
        builder.append(",");
        builder.append(event.getValue());
        builder.append("]");
        ZfinStaticLogger.write(builder.toString());
    }

    public void attributeReplaced(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        StringBuilder builder = new StringBuilder("ZfinSession modifed [SessionID = ");
        builder.append(session.getId());
        builder.append("]: Modified Attribute: [");
        builder.append(event.getName());
        builder.append(",");
        builder.append(event.getValue());
        builder.append("]");
        ZfinStaticLogger.write(builder.toString());
    }

    /**
     * This method is called when a new session object is created.
     *
     * @param event
     */
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        StringBuilder builder = new StringBuilder("ZfinSession created: SessionID = ");
        builder.append(session.getId());
        ZfinStaticLogger.write(builder.toString());
        // ToDO: commented out for now until the session table gets recreated in the db.
        //createSession(session);
    }

    /**
     * This method is called when an existing session is removed due to session timeout or
     * invlidation of the session (logout, invalidating the session manually, ...).
     *
     * @param event
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        StringBuilder builder = new StringBuilder("ZfinSession removed: SessionID = ");
        builder.append(session.getId());
        ZfinStaticLogger.write(builder.toString());
        // ToDO: commented out for now until the session table gets recreated in the db.
        //logRemoveSession(session);
    }

    // todo: catch exception form hibernate
    private void createSession(HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = null;
        if (authentication != null)
            name = authentication.getName();
        String sessionID = session.getId();
        ZfinSession zfinS = new ZfinSession();
        zfinS.setSessionID(sessionID);
        zfinS.setStatus("active");
        zfinS.setDateCreated(new Timestamp(System.currentTimeMillis()));
        zfinS.setUserName(name);
        Session sess = HibernateUtil.currentSession();
        Transaction tx = sess.beginTransaction();
        UserRepository ur = RepositoryFactory.getUserRepository();
        ur.createSession(zfinS);
        tx.commit();
        HibernateUtil.closeSession();
        GBrowseHibernateUtil.closeSession();
    }

    // todo: catch exception form hibernate
    private void logRemoveSession(HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = null;
        if (authentication != null)
            name = authentication.getName();
        String sessionID = session.getId();

        UserRepository ur = RepositoryFactory.getUserRepository();
        Session sess = HibernateUtil.currentSession();
        Transaction tx = sess.beginTransaction();
        ZfinSession zfinSession = ur.getSession(sessionID);
        zfinSession.setStatus("inactive");
        zfinSession.setDateModified(new Timestamp(System.currentTimeMillis()));
        ur.updateSession(zfinSession);
        tx.commit();
        HibernateUtil.closeSession();
        GBrowseHibernateUtil.closeSession();

    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
    }

    /**
     * @param servletContextEvent
     */
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // DO we need to mark all sessions as inactive because the context is removed,
        // i.e. the servlet is shutdown? What is the server serializes the sessions?
        LOG.info("Context destroyed");
    }
}
