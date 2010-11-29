package org.zfin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.zfin.framework.GBrowseHibernateUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;
import org.zfin.util.servlet.ServletService;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * This class is needed to add a new cookie to the response object when the
 * user is successfully authenticated through the login form.
 * This is necessary here as you have to add cookies to the response object before the
 * sendRedirect() method is called that would not allow adding cookies to the response
 * as the response is marked as committed. Check out the catalina Response Class
 */
public class ApgAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler{

    public static final String APG_ZFIN_LOGIN = "zfin_login";
    public static final int MAX_LENGTH_OF_APG_COOKIE = 19;

    private SessionRegistry sessionRegistry ;

    // Used to keep track of authenticated sessions.
    // We would like to know if a session is non-authenticated
    // to remove its session after a shorter period than for authenticated sessions.
    // See SessionManagerServlet.
//    private static Map<String, ZfinSession> authenticatedSessions = new HashMap<String, ZfinSession>();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication != null && authentication.isAuthenticated()) {
            Cookie cookie = ServletService.getJSessionCookie(request);
            if (cookie != null) {
                String id = cookie.getValue();
                String value = convertTomcatCookieToApgCookie(id);
                Cookie zfinCookie = new Cookie(APG_ZFIN_LOGIN, value);
                zfinCookie.setPath(ZfinProperties.getCookiePath());
                zfinCookie.setSecure(false);
                zfinCookie.setMaxAge(Integer.valueOf(ZfinPropertiesEnum.VALID_SESSION_TIMEOUT_SECONDS.value()));
                response.addCookie(zfinCookie);
//                cookie.setSecure(false);
                String login = authentication.getName();
                setCookieAndSession(login, id, value);
            }
        }
        super.onAuthenticationSuccess(request,response,authentication);
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

        UserRepository ur = RepositoryFactory.getUserRepository();
        Person person = ur.getPersonByLoginName(login);

        HibernateUtil.createTransaction();
        AccountInfo accountInfo = person.getAccountInfo();
        accountInfo.setCookie(value);
        accountInfo.setPreviousLoginDate(new Date());
        if(accountInfo.getZdbID()==null){
            accountInfo.setZdbID(person.getZdbID()) ;
        }
        HibernateUtil.currentSession().update(accountInfo);


//        ZfinSession newSession = new ZfinSession();
//        newSession.setUserName(login);
//        newSession.setSessionID(sessionID);
//        authenticatedSessions.put(sessionID, newSession);
        HibernateUtil.flushAndCommitCurrentSession();
        GBrowseHibernateUtil.closeSession();

        sessionRegistry.registerNewSession(sessionID,person);

    }

    public void setSessionRegistry(SessionRegistry sessionRegistry ) {
        this.sessionRegistry = sessionRegistry ;
    }
}
