package org.zfin.database;

import org.zfin.framework.HibernateUtil;
import org.hibernate.Session;

import javax.servlet.*;
import java.io.IOException;

/**
 * This servlet filter creates a Hibernate session for each incoming request
 * and closes the session when the controller and the view renderer is finished.
 * It basically wraps a controller within a single session and allows the renderer
 * to use the same session, needed lor lazy initializations (if you closed the
 * session in the controller the renderer could not retrieve data from the db
 * that was not already loaded). See Open Session in View pattern (OSIV).
 */
public class HibernateSessionRequestFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    // ToDo: At this moment this filter only closes the session.
    // This could also include the transaction definition but needs some work
    // to handle exceptions: how does this filter know where to redirect if
    // a transaction fails. Maybe back to the previous page?
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
        HibernateUtil.closeSession();
    }

    public void destroy() {
    }
}
