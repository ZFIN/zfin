package org.zfin.security.repository;

import org.zfin.profile.Person;

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

}
