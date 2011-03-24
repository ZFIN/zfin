package org.zfin.database;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.zfin.framework.GBrowseHibernateUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.SysmasterHibernateUtil;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.people.Person;
import org.zfin.util.ZfinSMTPAppender;
import org.zfin.util.log4j.Log4jService;
import org.zfin.util.servlet.RequestBean;
import org.zfin.util.servlet.ServletService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;

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
    private final static String NEWLINE = System.getProperty("line.separator");

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    // ToDo: At this moment this filter only closes the session.
    // This could also include the transaction definition but needs some work
    // to handle exceptions: how does this filter know where to redirect if
    // a transaction fails. Maybe back to the previous page?

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
            // this probably should go into its own filter as it is not much related to hibernate session handling.
            if (Logger.getLogger("org.zfin.gwt").isDebugEnabled()) {
                String gwtRequestString = (String) request.getAttribute(ZfinRemoteServiceServlet.GWT_REQUEST_STRING);
                if (gwtRequestString != null) {
                    String debugMessage = getDebugMessage(gwtRequestString);
                    LOG.debug("Posted Data for GWT request: " + System.getProperty("line.separator") + debugMessage);
                }
            }
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
            String gwtRequestString = (String) req.getAttribute(ZfinRemoteServiceServlet.GWT_REQUEST_STRING);
            if (StringUtils.isNotEmpty(gwtRequestString)) {
                message.append(getDebugMessage(gwtRequestString));
            }
            LOG.error(message, e);
            LOG.error(DbSystemUtil.getLockInfo());
        } finally {
            // ensure that the Hibernate session is closed, meaning, the threadLocal object is detached from
            // the current threadLocal
            HibernateUtil.closeSession();
            GBrowseHibernateUtil.closeSession();
            SysmasterHibernateUtil.closeSession();
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

    private String getDebugMessage(String contents) {
        Table output = new Table(2);
        if (Person.getCurrentSecurityUser() != null) {
            output.addCell("User Name");
            output.addCell(Person.getCurrentSecurityUser().getName());
        }
        output.addCell("GWT Data");
        output.addCell("");
        String[] values = contents.split("\\|");
        int index = 1;
        for (String val : values) {
            output.addCell("" + index++ + " ", new CellStyle(CellStyle.HorizontalAlign.right));
            output.addCell(val);
        }
        return output.render();
    }

    public void destroy() {
    }
}
