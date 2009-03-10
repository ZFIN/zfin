package org.zfin.security.repository;

import org.acegisecurity.annotation.Secured;
import org.zfin.framework.ZfinSession;
import org.zfin.people.Person;

import java.util.List;

/**
 * Repository that is used for Person-related persistence logic.
 */
public interface UserRepository {

    /**
     * Retrieve a person record from the login name.
     *
     * @param username login name
     * @return Person
     */
    Person getPersonByLoginName(String username);

    /**
     * Create a Person record.
     * There are rules around creating certain access level accounts,
     * i.e. only a thread run with a certain level can create a new person
     * with the same level or under.
     *
     * @param person Person
     */
    @Secured({"root"})
    public void createPerson(Person person);

    /**
     * Create a new session obejct to allow tracking of a user.
     *
     * @param zfinS session
     */
    void createSession(ZfinSession zfinS);

    /**
     * Retrieve a Zfin session object from the session ID
     *
     * @param sessionID session id
     * @return session
     */
    ZfinSession getSession(String sessionID);

    /**
     * Update the session object
     *
     * @param zfinSession session
     */
    void updateSession(ZfinSession zfinSession);

    /**
     * Retrieve all web sessions that are still active.
     * The app server has a parameter that sets the maximum time for
     * a session to expire.
     *
     * @return session
     */
    List<ZfinSession> getActiveSessions();

    /**
     * Backup the APG cookie that is associated to an authenticated user.
     *
     * @param sessionID Tomcat session
     */
    void backupAPGCookie(String sessionID);

    /**
     * Restore the APG cookie after Tomcat is started if it is a valid
     * authenticated session in Tomcat.
     *
     * @param sessionID Tomcat session
     */
    void restoreAPGCookie(String sessionID);
}
