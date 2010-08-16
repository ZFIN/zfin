package org.zfin.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.GBrowseHibernateUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.ZfinSession;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;
import org.zfin.util.servlet.ServletService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is needed to add a new cookie to the response object when the
 * user is successfully authenticated through the login form.
 * This is necessary here as you have to add cookies to the response object before the
 * sendRedirect() method is called that would not allow adding cookies to the response
 * as the response is marked as committed. Check out the catalina Response Class
 */
public class ZfinAuthenticationProcessingFilter extends AuthenticationProcessingFilter {

    public static final String APG_ZFIN_LOGIN = "zfin_login";

    // Used to keep track of authenticated sessions.
    // We would like to know if a session is non-authenticated
    // to remove its session after a shorter period than for authenticated sessions.
    // See SessionManagerServlet.
    private static Map<String, ZfinSession> authenticatedSessions = new HashMap<String, ZfinSession>();
    public static final int MAX_LENGTH_OF_APG_COOKIE = 19;

    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              Authentication authenticationResult) throws IOException {

        if (authenticationResult != null && authenticationResult.isAuthenticated()) {
            Cookie cookie = ServletService.getJSessionCookie(request);
            if (cookie != null) {
                String id = cookie.getValue();
                String value = convertTomcatCookieToApgCookie(id);
                Cookie zfinCookie = new Cookie(APG_ZFIN_LOGIN, value);
                zfinCookie.setPath(ZfinProperties.getCookiePath());
                response.addCookie(zfinCookie);
                String login = authenticationResult.getName();
                setCookieAndSession(login, id, value);
            }
        }
    }

    /**
     * Pass in a Tomcat cookie and return an apg cookie.
     * APG cookies are shorter than Tomcat cookies and typically
     * are truncated.
     *
     * @param id Tomcat cookie
     * @return corresponding APG cookie
     */
    public static String convertTomcatCookieToApgCookie(String id) {
        return id.substring(0, MAX_LENGTH_OF_APG_COOKIE);
    }

    // if no session found then the user started with the login page
    // create a new session then, otherwise update the existing one
    // with the now known user name and userID.
    // updating the cookie into the user and creating or updating the session
    // is done in a single transaction.

    private void setCookieAndSession(String login, String sessionID, String value) {

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        UserRepository ur = RepositoryFactory.getUserRepository();
        Person person = ur.getPersonByLoginName(login);
        AccountInfo accountInfo = person.getAccountInfo();
        accountInfo.setCookie(value);
        accountInfo.setPreviousLoginDate(new Date());

        ZfinSession newSession = new ZfinSession();
        newSession.setUserName(login);
        newSession.setSessionID(sessionID);
        authenticatedSessions.put(sessionID, newSession);
        tx.commit();
        HibernateUtil.closeSession();
        GBrowseHibernateUtil.closeSession();


    }

    public static Map getAuthenticatedSessions() {
        return authenticatedSessions;
    }

    public static void addAuthenticatedSession(String sessionID) {
        ZfinSession newSession = new ZfinSession();
        newSession.setSessionID(sessionID);
        authenticatedSessions.put(sessionID, newSession);
    }

    public static void removeSession(String sessionID) {
        authenticatedSessions.remove(sessionID);
    }

    protected String determineTargetUrl(HttpServletRequest request) {

        String targetUrl = super.determineTargetUrl(request);

        // if the login redirect came through an ajax call redirect to a separate page
        // Todo: find a better place to store these strings
        if (targetUrl.indexOf("/ajax/") > -1)
            targetUrl = "/javascript/ajax-lib/ajax-redirect.html";
        return targetUrl;
    }

}
