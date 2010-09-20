package org.zfin.util.servlet;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.zfin.people.Person;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

/**
 * Service class that extracts various information from the servlet request.
 */
public class ServletService {

    public static final String JSESSIONID = "JSESSIONID";

    public static String getUserInfo(HttpServletRequest request) {
        String userInfo = "User: ";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            userInfo += authentication.getName();
        } else {
            userInfo += "Guest";
        }
        userInfo += " [" + getSessionID(request) + "]";

        return userInfo;
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Retrieve the session ID from the cookie (Tomcat).
     * If non is found then return null.
     *
     * @param httpRequest http servlet request
     * @return session id
     */
    public static String getSessionID(HttpServletRequest httpRequest) {
        Cookie cookie = getJSessionCookie(httpRequest);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    /**
     * Retrieve the Tomcat session cookie.
     * If none is found it returns null.
     *
     * @param httpRequest http request
     * @return cookie
     */
    public static Cookie getJSessionCookie(HttpServletRequest httpRequest) {
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies == null)
            return null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JSESSIONID)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Creates a String that contains request info:
     * Request URL: <url>
     * Query parameters: <GET parameters>
     *
     * @param httpRequest http servlet request
     * @return string
     */
    public static String getRequestInfo(HttpServletRequest httpRequest) {
        StringBuffer message = new StringBuffer(50);
        message.append("Request URL: ").append(httpRequest.getRequestURL());
        String requestQueryString = httpRequest.getQueryString();
        if (requestQueryString != null) {
            message.append("  Query parameters: ").append(URLDecoder.decode(requestQueryString));
            message.append(requestQueryString);
        }
        return message.toString();
    }


    public static RequestBean getRequestBean(HttpServletRequest request) {
        RequestBean bean = new RequestBean();
        bean.setTomcatJSessioncookie(getJSessionCookie(request));
        bean.setPerson(Person.getCurrentSecurityUser());
        bean.setQueryParameter(request.getParameterMap());
        bean.setRequest(request.getRequestURI());
        bean.setQueryRequestString(request.getQueryString());
        bean.setHttRequest(request);
        return bean;
    }
}
