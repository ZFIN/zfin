package org.zfin.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.ZfinSession;
import org.zfin.people.Person;
import org.zfin.people.User;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is needed to add a new cookie to the response object when the
 * user is successfully authenticated through the login form.
 * This is necessary here as you have to add cookies to the response obejct before the
 * sendRedirect() method is called that would not allow adding cookies to the response
 * as the response is marked as committed. Check out the catalina Reponse Class
 */
public class ZfinAuthenticationProcessingFilter extends AuthenticationProcessingFilter {

    public static final String ZFIN_LOGIN = "zfin_login";
    public static final String JSESSIONID = "JSESSIONID";

    // Used to keep track of authenticated sessions.
    // We would like to know if a session is non-authenticatied
    // to remove its session after a shorter period than for authenticated sessions.
    // See SessionMangerSerlvet.
    private static Map<String, ZfinSession> authenticatedSessions = new HashMap<String, ZfinSession>();

    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              Authentication authResult) throws IOException {

        Map params = request.getParameterMap();
        if (params.containsKey(AuthenticationProcessingFilter.ACEGI_SECURITY_FORM_USERNAME_KEY)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication.isAuthenticated()) {
                Cookie[] cookies = request.getCookies();
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(JSESSIONID)) {
                        String id = cookie.getValue();
                        String value = id.substring(0, 19);
                        Cookie zfinCookie = new Cookie(ZFIN_LOGIN, value);
                        zfinCookie.setPath(ZfinProperties.getCookiePath());
                        response.addCookie(zfinCookie);
                        String login = authResult.getName();
                        setCookieAndSession(login, id, value);
                    }
                }
            }
        }

    }

    // if no session found then the user started with the login page
    // create a new session then, otherwise update the existing one
    // with the now known user name and userID.
    // updating the cookie into the user and creating or updating the session
    // is done in a single transcation.
    private void setCookieAndSession(String login, String sessionID, String value) {

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        UserRepository ur = RepositoryFactory.getUserRepository();
        Person person = ur.getPersonByLoginName(login);
        User user = person.getUser();
        user.setCookie(value);

        ZfinSession newSession = new ZfinSession();
        newSession.setUserName(login);
        newSession.setSessionID(sessionID);
        authenticatedSessions.put(sessionID, newSession);
/*
        ZfinSession zfinS;
        zfinS = ur.getSession(sessionID);
        boolean newSession = false;
        if (zfinS == null) {
            zfinS = new ZfinSession();
            newSession = true;
        }

        zfinS.setUserID(user.getZdbID());
        zfinS.setUserName(login);
        zfinS.setSessionID(sessionID);

        if (newSession)
            ur.createSession(zfinS);
        else {
            zfinS.setDateModified(new Date());
            ur.updateSession(zfinS);
        }
*/
        tx.commit();
        HibernateUtil.closeSession();

    }

    public static Map getAuthenticatedSessions() {
        return authenticatedSessions;
    }

    public static void removeSession(String sessionID) {
        authenticatedSessions.remove(sessionID);
    }
}
