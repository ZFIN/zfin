package org.zfin.database;

import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.GBrowseHibernateUtil;
import org.zfin.util.log4j.Log4jService;
import org.zfin.util.servlet.RequestBean;
import org.zfin.util.servlet.ServletService;
import org.zfin.util.ZfinSMTPAppender;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;

/**
 * This servlet filter creates a Hibernate session for each incoming request
 * and closes the session when the controller and the view renderer is finished.
 * It basically wraps a controller within a single session and allows the renderer
 * to use the same session, needed lor lazy initializations (if you closed the
 * session in the controller the renderer could not retrieve data from the db
 * that was not already loaded). See Open Session in View pattern (OSIV).
 */
public class HibernateSessionRequestFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(HibernateSessionRequestFilter.class);
    private final static String NEWLINE = System.getProperty("line.separator") ;

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    // ToDo: At this moment this filter only closes the session.
    // This could also include the transaction definition but needs some work
    // to handle exceptions: how does this filter know where to redirect if
    // a transaction fails. Maybe back to the previous page?

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            HttpServletRequest req = (HttpServletRequest) request;
            StringBuffer message = new StringBuffer("Unhandled Exception in Servlet Filter found: ");
            message.append(NEWLINE);
            message.append("Request URL: ").append(req.getRequestURL());
            message.append(NEWLINE);
            String requestQueryString = req.getQueryString();
            if (requestQueryString != null) {
                message.append("Query parameters: ").append(URLDecoder.decode(requestQueryString));
            }
            LOG.error(message, e);
        } finally {
            // ensure that the Hibernate session is closed, meaning, the threadLocal object is detached from
            // the current threadLocal
            HibernateUtil.closeSession();
            GBrowseHibernateUtil.closeSession();
            callSmtpAppender((HttpServletRequest) request);
        }
    }

    private void callSmtpAppender(HttpServletRequest request) {
        ZfinSMTPAppender smtpAppender = Log4jService.getSmtpAppender();
        if (smtpAppender != null) {
            RequestBean bean = ServletService.getRequestBean(request);
            smtpAppender.sendEmailOfEvents(bean);
        }
    }

    public void destroy() {
    }
}
