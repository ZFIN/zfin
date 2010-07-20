package org.zfin.framework.filter;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.log4j.Logger;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.ZfinSecurityContextLogoutHandler;

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

    private static Logger LOG = Logger.getLogger(UpdatesCheckFilter.class);
    private static ZdbFlag systemUpdates;
    private static final String REDIRECT_URL = "/action/login";
    private ZfinSecurityContextLogoutHandler securityHandler;
    private static final List<String> readOnlyUrls = new ArrayList<String>();

    static {
        readOnlyUrls.add("/ontology/");
        readOnlyUrls.add("/anatomy/");
        readOnlyUrls.add("/ajax/anatomylookup");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Start Updates Check Filter");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String url = request.getRequestURI();
        boolean readOnlyUrl = isReadOnlyUrl(url);
        if (!readOnlyUrl) {
            InfrastructureRepository infrastructure = RepositoryFactory.getInfrastructureRepository();
            systemUpdates = infrastructure.getUpdatesFlag();
        }
        Person person = Person.getCurrentSecurityUser();

        // redirect if user is logged in and database is locked
        if (systemUpdates != null && systemUpdates.isSystemUpdateDisabled() && !url.equals(REDIRECT_URL) && person != null) {
            logoutUser(request, response);
            response.sendRedirect(response.encodeRedirectURL(REDIRECT_URL));
            LOG.info("System is currently being updated. No login session are allowed.");
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isReadOnlyUrl(String url) {
        for (String value : readOnlyUrls) {
            if (url.contains(value))
                return true;
        }
        return false;
    }

    private void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // logout user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        securityHandler.logout(request, response, authentication);
    }

    public static ZdbFlag getSystemUpdatesFlag() {
        return systemUpdates;
    }

    public ZfinSecurityContextLogoutHandler getSecurityHandler() {
        return securityHandler;
    }

    public void setSecurityHandler(ZfinSecurityContextLogoutHandler securityHandler) {
        this.securityHandler = securityHandler;
    }

    public void destroy() {
    }
}