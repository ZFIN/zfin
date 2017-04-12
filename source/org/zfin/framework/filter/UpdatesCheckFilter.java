package org.zfin.framework.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.zfin.database.TableLock;
import org.zfin.framework.GBrowseHibernateUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.SysmasterHibernateUtil;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.ZfinSMTPAppender;
import org.zfin.util.log4j.Log4jService;
import org.zfin.util.servlet.RequestBean;
import org.zfin.util.servlet.ServletService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Filter that checks if the database is in Update mode and thus does not allow
 * login sessions. Needs to be run for each servlet request.
 */
public class UpdatesCheckFilter implements Filter {

    private static Logger logger = Logger.getLogger(UpdatesCheckFilter.class);
    private static Boolean readOnlyMode = null;
    private static final String REDIRECT_URL = "/action/login";
    private List<LogoutHandler> logoutHandlers;
    private LogoutSuccessHandler logoutSuccessHandler;

    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    // This is never called as it is part of the Spring FilterChainProxy line
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Start Updates Check Filter");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (readOnlyMode == null)
            readOnlyMode = infrastructureRepository.getUpdatesFlag().isSystemUpdateDisabled();

        String url = request.getRequestURI();

        // redirect if user is logged in and database is locked
        if (readOnlyMode && !url.equals(REDIRECT_URL)) {
            Person person = ProfileService.getCurrentSecurityUser();
            if (person != null && person.getAccountInfo() != null) {
                if (!person.getAccountInfo().isAdmin()) {
                    response.sendRedirect(REDIRECT_URL);
                    logoutUser(request, response);
                    logger.info("System is currently being updated. No login sessions are allowed.");
                    return;
                }
            }
        }
        List<TableLock> locks = null;
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            StringBuffer message = new StringBuffer("Unhandled Exception");
            String gwtRequestString = (String) request.getAttribute(ZfinRemoteServiceServlet.GWT_REQUEST_STRING);
            if (StringUtils.isNotEmpty(gwtRequestString)) {
                message.append(getDebugMessage(gwtRequestString));
            }
            logger.error(message, e);
        } finally {
            // ensure that the Hibernate session is closed, meaning, the threadLocal object is detached from
            // the current threadLocal
            HibernateUtil.closeSession();
            GBrowseHibernateUtil.closeSession();
            SysmasterHibernateUtil.closeSession();
//            callSmtpAppender((HttpServletRequest) request, locks);
        }
    }

    private void callSmtpAppender(HttpServletRequest request, List<TableLock> locks) {
        ZfinSMTPAppender smtpAppender = Log4jService.getSmtpAppender();
        if (smtpAppender != null) {
            RequestBean bean = ServletService.getRequestBean(request);
            bean.setLocks(locks);
            smtpAppender.sendEmailOfEvents(bean);
        }
    }

    private String getDebugMessage(String contents) {
        Table output = new Table(2);
        if (ProfileService.getCurrentSecurityUser() != null) {
            output.addCell("User Name");
            output.addCell(ProfileService.getCurrentSecurityUser().getShortName());
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


    private void logoutUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // logout user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.error("No authentication object in context");
            return;
        }

        logger.debug("logging out " + authentication);


        if (logoutHandlers != null) {
            for (LogoutHandler logoutHandler : logoutHandlers) {
                logoutHandler.logout(request, response, authentication);
            }
        }

        if (logoutSuccessHandler != null) {
            logoutSuccessHandler.onLogoutSuccess(request, response, authentication);
        }
    }

    public static Boolean getReadOnlyMode() {
        return readOnlyMode;
    }

    public static void setReadOnlyMode(Boolean readOnlyMode) {
        UpdatesCheckFilter.readOnlyMode = readOnlyMode;
    }

    public void setLogoutSuccessHandler(LogoutSuccessHandler logoutSuccessHandler) {
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    public void setLogoutHandlers(List<LogoutHandler> logoutHandlers) {
        this.logoutHandlers = logoutHandlers;
    }


    public void destroy() {
        logoutHandlers = null;
        logoutSuccessHandler = null;
    }
}