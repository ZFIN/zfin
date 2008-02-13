package org.zfin.framework.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple controller that serves the developers home page.
 */
public class LoginController implements Controller {
    public static final String LOGOUT = "j_acegi_logout";
    public static final String ACCESS_DENIED = "access-denied";

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Check if we came through /action/login
        // this is a jumper page that can be accessed insecurely and thus creates a no-ssl session if not already created.
        // it then redirects to the secure login page.
        String url = request.getRequestURI();
        if (url.endsWith("/action/login")) {
            response.sendRedirect("/action/login-redirect");
        }

        String accessDenied = request.getParameter(ACCESS_DENIED);
        String queryString = request.getQueryString();
        // Sometimes a query string is not recognized as a parameter
        // that's why we check both cases.
        if (accessDenied != null || (queryString != null && queryString.startsWith(ACCESS_DENIED)))
            return new ModelAndView("access-denied");
        return new ModelAndView("login");
    }

}
