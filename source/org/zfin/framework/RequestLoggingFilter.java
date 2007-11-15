package org.zfin.framework;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;

/**
 * A servlet filter to log servlet requests.
 * You need to configure your web.xml, the deployment descriptor,
 * to include the filter.
 */
public class RequestLoggingFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("filterConfig: " + filterConfig.getFilterName());
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        Principal principal = request.getUserPrincipal();
        String userName = request.getRemoteUser();
        boolean isAdmin = request.isUserInRole("admin");


        if (userName != null)
            System.out.println("userName: " + userName);
        System.out.println("Role is Admin: " + isAdmin);
        if (principal != null)
            System.out.println("Principal: " + principal.getName());

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {

    }
}
