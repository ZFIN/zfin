package org.zfin.framework.filter;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;

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
public class UpdatesCheckFilter implements Filter{

    private static Logger logger = Logger.getLogger(UpdatesCheckFilter.class);
    private static boolean systemUpdatesDisabled =false;
    private static final String REDIRECT_URL = "/action/login";
    private List<LogoutHandler> logoutHandlers;
    private LogoutSuccessHandler logoutSuccessHandler;
    private static final List<String> readOnlyUrls = new ArrayList<String>();
    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository() ;

    static {
        readOnlyUrls.add("/ontology/");
        readOnlyUrls.add("/anatomy/");
        readOnlyUrls.add("/marker/transcript-view/");
        readOnlyUrls.add("/ajax/anatomylookup");
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
        Person person = Person.getCurrentSecurityUser();

        // redirect if user is logged in and database is locked
        if (systemUpdatesDisabled && !url.equals(REDIRECT_URL) && person != null) {
            logoutUser(request, response);
//            response.sendRedirect(response.encodeRedirectURL(REDIRECT_URL));
            logger.info("System is currently being updated. No login session are allowed.");
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

    private void logoutUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // logout user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null){
            logger.error("No authentication object in context");
            return;
        }

        logger.debug("logging out "+ authentication );


        if(logoutHandlers!=null){
            for(LogoutHandler logoutHandler : logoutHandlers){
                logoutHandler.logout(request,response,authentication);
            }
        }

        if(logoutSuccessHandler!=null){
            logoutSuccessHandler.onLogoutSuccess(request,response,authentication);
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
        logoutHandlers = null ;
        logoutSuccessHandler = null ;
    }
}