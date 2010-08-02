package org.zfin.repository;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.MockAuthenticationManager;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.log4j.Logger;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;

/**
 * This class instantiates exactly one session per VM.
 */
public class SessionCreator {

    private static Logger logger = Logger.getLogger(SessionCreator.class);

    private static final String LOGIN = "login";
    private static boolean isInstantiated = false;

    /**
     * Only creates session if in hosted mode.
     *
     * @return boolean Instantiation success
     */
    public static synchronized boolean instantiateDBForHostedMode() {
        String gwtArgs = System.getProperty("gwt.args");
        if (gwtArgs != null && gwtArgs.indexOf("hosted") >= 0 && isInstantiated == false) {
            logger.warn("running in hosted mode");
            isInstantiated = createSession();
            String login = System.getProperty(LOGIN);
            if (login != null && login.length() > 0) {
                ProfileRepository pr = RepositoryFactory.getProfileRepository();
                Person person = pr.getPersonByName(login);
                SecurityContext security = new SecurityContextImpl();
                AuthenticationManager manager = new MockAuthenticationManager(true);
                Authentication authentication = new UsernamePasswordAuthenticationToken(person, null);
                manager.authenticate(authentication);
                security.setAuthentication(authentication);
                SecurityContextHolder.setContext(security);
            }

        }

        return isInstantiated;
    }

    public static boolean createSession() {
        try {
            new HibernateSessionCreator();
            return true;
        }
        catch (Exception e) {
            logger.error("session creation excpetion", e);
            return false;
        }
    }

}
