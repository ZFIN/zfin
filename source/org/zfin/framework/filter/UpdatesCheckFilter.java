package org.zfin.framework.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.zfin.database.DatabaseLock;
import org.zfin.database.DbSystemUtil;
import org.zfin.database.TableLock;
import org.zfin.database.repository.SysmasterRepository;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Filter that checks if the database is in Update mode and thus does not allow
 * login sessions. Needs to be run for each servlet request.
 */
public class UpdatesCheckFilter implements Filter {

    private static Logger logger = Logger.getLogger(UpdatesCheckFilter.class);
    private static boolean systemUpdatesDisabled = false;
    private static final String REDIRECT_URL = "/action/login";
    private List<LogoutHandler> logoutHandlers;
    private LogoutSuccessHandler logoutSuccessHandler;
    private static final List<String> readOnlyUrls = new ArrayList<>();
    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    static {
        readOnlyUrls.add("/ontology/");
        readOnlyUrls.add("/anatomy/");
        readOnlyUrls.add("/marker/transcript-view/");
        readOnlyUrls.add("/ajax/anatomylookup");
        readOnlyUrls.add("/wiki/summary");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Start Updates Check Filter");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String url = request.getRequestURI();
        boolean readOnlyUrl = isReadOnlyUrl(url);
        if (!readOnlyUrl) {
            systemUpdatesDisabled = infrastructureRepository.getDisableUpdatesFlag();
        }

        // redirect if user is logged in and database is locked
        if (systemUpdatesDisabled && !url.equals(REDIRECT_URL)) {
            Person person = ProfileService.getCurrentSecurityUser();
            if (person != null) {
                logoutUser(request, response);
                logger.info("System is currently being updated. No login session are allowed.");
                return;
            }
        }
        List<TableLock> locks = null;
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            HttpServletRequest req = (HttpServletRequest) request;
            StringBuffer message = new StringBuffer("Unhandled Exception");
            String gwtRequestString = (String) req.getAttribute(ZfinRemoteServiceServlet.GWT_REQUEST_STRING);
            if (StringUtils.isNotEmpty(gwtRequestString)) {
                message.append(getDebugMessage(gwtRequestString));
            }
            logger.error(message, e);
            List<DatabaseLock> dbLocks = SysmasterRepository.getLocks();
            locks = DbSystemUtil.getLockSummary(dbLocks);
        } finally {
            // ensure that the Hibernate session is closed, meaning, the threadLocal object is detached from
            // the current threadLocal
            HibernateUtil.closeSession();
            GBrowseHibernateUtil.closeSession();
            SysmasterHibernateUtil.closeSession();
            callSmtpAppender((HttpServletRequest) request, locks);
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


    private boolean isReadOnlyUrl(String url) {
        for (String value : readOnlyUrls) {
            if (url.contains(value))
                return true;
        }
        if (!(url.contains("action/") || url.contains("ajax/")))
            return true;
        if (url.equals("/action/"))
            return true;
        return false;
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

    public static boolean getSystemUpdatesFlag() {
        return systemUpdatesDisabled;
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