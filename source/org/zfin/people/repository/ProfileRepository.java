package org.zfin.people.repository;

import org.zfin.people.Person;
import org.zfin.people.Lab;
import org.zfin.people.User;

/**
 * Repository interface that defines the methods how to access profile data.
 */
public interface ProfileRepository {

    /**
     * Lookup a Person by its primary key.
     * @param zdbID
     */
    Person getPerson(String zdbID);

    /**
     * Lookup a Person by given attribtes. If more than one objects are
     * found a runtime exception is thrown.
     * @param person
     */
    Person getPerson(Person person);

    /**
     * Save a person object and all its non-transient attributes, such as
     * Labs and publications. This method checks if those composed attributes are
     * already persisted. If so then it does not try to insert them again.
     * @param person
     */
    void insertPerson(Person person);

    /**
     * Save a new Lab object. If it exists a runtime exception is thrown.
     * @param lab
     */
    void insertLab(Lab lab);

    /**
     * Retrieve User (person with login account) information.
     * @param zdbID
     */
    User getUser(String zdbID);

    
}
