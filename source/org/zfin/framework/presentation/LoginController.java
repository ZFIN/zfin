package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple controller that serves the developers home page.
 */
@Controller
public class LoginController {
    public static final String LOGOUT = "logout";
    public static final String ACCESS_DENIED = "access-denied";

    @RequestMapping("/login")
    protected String login(HttpServletRequest request) throws Exception {

        // Check if we came through /action/login
        // this is a jumper page that can be accessed insecurely and thus creates a no-ssl session if not already created.
        // it then redirects to the secure login page.

        String accessDenied = request.getParameter(ACCESS_DENIED);
        String queryString = request.getQueryString();
        // Sometimes a query string is not recognized as a parameter
        // that's why we check both cases.
        if (accessDenied != null || (queryString != null && queryString.startsWith(ACCESS_DENIED)))
            return "access-denied";
        return "redirect:/action/login-redirect";
    }

    @RequestMapping("/login-redirect")
    protected String loginRedirect(HttpServletRequest request) throws Exception {

        // Check if we came through /action/login
        // this is a jumper page that can be accessed insecurely and thus creates a no-ssl session if not already created.
        // it then redirects to the secure login page.
        String accessDenied = request.getParameter(ACCESS_DENIED);
        String queryString = request.getQueryString();
        // Sometimes a query string is not recognized as a parameter
        // that's why we check both cases.
        if (accessDenied != null || (queryString != null && queryString.startsWith(ACCESS_DENIED)))
            return "access-denied";
        return "login";
    }

}
