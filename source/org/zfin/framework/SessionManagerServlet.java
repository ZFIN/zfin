package org.zfin.framework;

import org.apache.catalina.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.security.ZfinAuthenticationProcessingFilter;
import org.zfin.security.repository.UserRepository;
import org.zfin.repository.RepositoryFactory;
import org.hibernate.HibernateException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import java.util.Map;

/**
 * Background Servlet that should not be called directly.
 * It is only loaded upon server startup and then kicks off
 * a separate thread that checks the sessions.
 * sessions that are no associated to logins should be invlidated after
 * a short period of time while login sessions (mostly curators) should
 * last for a longer period.
 */
public class SessionManagerServlet extends HttpServlet implements ContainerServlet {

    private static final Logger LOG = Logger.getLogger(SessionManagerServlet.class);

    public static final String PROCESS_DELAY_TIME = "process delay time";
    public static final String GUEST_SESSION_TIMEOUT_TIME = "guest session timeout";

    public static final long MINUTE = 60 * 1000L;
    public static long processDelayTime = 20 * MINUTE;
    public static long guestSessionTimeout = 30 * MINUTE;

    protected Wrapper wrapper = null;
    protected Host host = null;
    protected Context context = null;
    /**
     * The associated deployer ObjectName.
     */
    protected ObjectName oname = null;

    /**
     * Return the Catalina Wrapper with which we are associated.
     */
    public Wrapper getWrapper() {

        return (this.wrapper);

    }


    /**
     * Set the Catalina Wrapper with which we are associated.
     *
     * @param wrapper The new wrapper
     */
    public void setWrapper(Wrapper wrapper) {

        this.wrapper = wrapper;
        if (wrapper == null) {
            context = null;
            host = null;
            oname = null;
        } else {
            context = (Context) wrapper.getParent();
            host = (Host) context.getParent();
            Engine engine = (Engine) host.getParent();
            try {
                oname = new ObjectName(engine.getName()
                        + ":type=Deployer,host=" + host.getName());
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Initialize this servlet.
     */
    public void init() throws ServletException {

        // Ensure that our ContainerServlet properties have been set
        if ((wrapper == null) || (context == null))
            throw new UnavailableException(" No wrapper or context available");

        // Verify that we were not accessed using the invoker servlet
        String servletName = getServletConfig().getServletName();
        if (servletName == null)
            servletName = "";
        if (servletName.startsWith("org.apache.catalina.INVOKER."))
            throw new UnavailableException
                    ("Called through Invoker Servlet");

        String processTime = getInitParameter(PROCESS_DELAY_TIME);
        if (!StringUtils.isEmpty(processTime))
            processDelayTime = Long.valueOf(processTime) * MINUTE;

        String guestSession = getInitParameter(GUEST_SESSION_TIMEOUT_TIME);
        if (!StringUtils.isEmpty(guestSession))
            guestSessionTimeout = Long.valueOf(guestSession) * MINUTE;

        SessionManagerThread thread = new SessionManagerThread();
        thread.start();
        LOG.info("Session Manager Thread started: ");
        restoreAuthenticatedSessions();
    }

    private void restoreAuthenticatedSessions() {
        Session[] sessions = wrapper.getManager().findSessions();
        if (sessions == null)
            return;

        for (Session session : sessions) {
        ZfinAuthenticationProcessingFilter.addAuthenticatedSession(session.getId());
            try {
                org.hibernate.Session hSession = HibernateUtil.currentSession();
                try {
                    hSession.beginTransaction();
                    UserRepository userRep = RepositoryFactory.getUserRepository();
                    userRep.restoreAPGCookie(session.getId());
                    hSession.getTransaction().commit();
                } catch (HibernateException e) {
                    try {
                        hSession.getTransaction().rollback();
                    } catch (HibernateException he) {
                        LOG.error(he);
                    }
                    LOG.error(e);
                }

            } catch (Exception e) {
                LOG.error("Error during session checks.", e);
            }
        }
        LOG.debug("Sessions found: " + sessions.length);
    }

    /**
     * Thread that does the session cleanup.
     */
    private class SessionManagerThread extends Thread {

        /**
         * Check the sesessions every 20 minutes and clean the idle sessions.
         */


        public SessionManagerThread() {
            super("ZFIN Session Manager Thread");
            setDaemon(true);
        }

        public void run() {
            boolean runForever = true;
            while (runForever) {
                try {
                    Thread.sleep(processDelayTime);
                } catch (InterruptedException e) {
                    LOG.error("Error while sleeping", e);
                }
                Session[] sessions = wrapper.getManager().findSessions();
                if (sessions == null)
                    continue;
                for (Session session : sessions) {
                    try {
                        String sessionID = session.getId();
                        Map authenticatedSessions = ZfinAuthenticationProcessingFilter.getAuthenticatedSessions();
                        // check session times only for non-authenticated sessions
                        // authentication sessions are not touched as they are handled by the Tomcat container.
                        if (!authenticatedSessions.containsKey(sessionID)) {
                            LOG.debug("Session: " + session.getId());
                            long lastAccessedTime = session.getLastAccessedTime();
                            long now = System.currentTimeMillis();
                            long idleTime = (now - lastAccessedTime);
                            if (idleTime > guestSessionTimeout) {
                                // invalidate session
                                session.expire();
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error during session checks.", e);
                    }
                }
                LOG.debug("Sessions found: " + sessions.length);
            }
        }
    }

    /**
     * Remove all non-authenticated uses sessions since they
     * cause an annoying warning in clickstream.
     */
    public void destroy() {
        LOG.info("Servlet is decomissioned.");
        Session[] sessions = wrapper.getManager().findSessions();
        if (sessions == null)
            return;
        for (Session session : sessions) {
            try {
                String sessionID = session.getId();
                Map authenticatedSessions = ZfinAuthenticationProcessingFilter.getAuthenticatedSessions();
                // check session times only for non-authenticated sessions
                // authentication sessions are not touched as they are handled by the Tomcat container.
                if (!authenticatedSessions.containsKey(sessionID)) {
                    LOG.debug("Session: " + session.getId());
                    session.expire();
                    continue;
                }
                // backup authenticated sessions in APG land
                org.hibernate.Session hSession = HibernateUtil.currentSession();
                try {
                    hSession.beginTransaction();
                    UserRepository userRep = RepositoryFactory.getUserRepository();
                    userRep.backupAPGCookie(session.getId());
                    hSession.getTransaction().commit();
                } catch (HibernateException e) {
                    try {
                        hSession.getTransaction().rollback();
                    } catch (HibernateException he) {
                        LOG.error(he);
                    }
                    LOG.error(e);
                }

            } catch (Exception e) {
                LOG.error("Error during session checks.", e);
            }
        }
        LOG.debug("Sessions found: " + sessions.length);
    }

}

