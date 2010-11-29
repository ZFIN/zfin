package org.zfin.security.repository;

import org.springframework.security.access.annotation.Secured;
import org.zfin.people.Person;

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
    Person restoreAPGCookie(String sessionID);
}
