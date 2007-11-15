package org.zfin.framework.presentation;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple controller that serves the developers home page.
 */
public class LoginController implements Controller {
    public static final String LOGOUT = "j_acegi_logout";
    public static final String ACCESS_DENIED = "access-denied";

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String accessDenied = request.getParameter(ACCESS_DENIED);
        String queryString = request.getQueryString();
        // Sometimes a query string is not recognized as a parameter
        // that's why we check both cases.
        if (accessDenied != null || (queryString != null && queryString.startsWith(ACCESS_DENIED)) )
            return new ModelAndView("access-denied");
        return new ModelAndView("login");
    }

}
