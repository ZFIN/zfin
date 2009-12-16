package org.zfin.people.repository;

import org.zfin.marker.Marker;
import org.zfin.people.*;

import java.util.List;

/**
 * Repository interface that defines the methods how to access profile data.
 */
public interface ProfileRepository {

    /**
     * Lookup a Person by its primary key.
     *
     * @param zdbID
     */
    Person getPerson(String zdbID);

    /**
     * Lookup a Person by given attribtes. If more than one objects are
     * found a runtime exception is thrown.
     *
     * @param person
     */
    Person getPerson(Person person);

    /**
     * Retrieve an organization by name
     *
     * @param name name
     * @return Organization
     */
    Organization getOrganizationByName(String name);

    /**
     * Save a person object and all its non-transient attributes, such as
     * Labs and publications. This method checks if those composed attributes are
     * already persisted. If so then it does not try to insert them again.
     *
     * @param person Person
     */
    void insertPerson(Person person);

    /**
     * Save a new Lab object. If it exists a runtime exception is thrown.
     *
     * @param lab Lab
     */
    void insertLab(Lab lab);

    void addSupplier(Organization organization, Marker marker);

    /**
     * @param curatorZdbID person for whom the session value is being retrieved
     * @param pubZdbID     pub that the session value is associated with [can be null!]
     * @param field        field name
     * @return CuratorSession object from the database, if there is one
     */
    CuratorSession getCuratorSession(String curatorZdbID, String pubZdbID, String field);

    /**
     * Retrieve a Curator Session by pub and field name.
     * This requires to have a security login, i.e. a Person record.
     *
     * @param pubZdbID pub that the session value is associated with [can be null!]
     * @param field    field name
     * @return CuratorSession object from the database, if there is one
     */
    CuratorSession getCuratorSession(String pubZdbID, CuratorSession.Attribute field);

    /**
     * @param curatorZdbID person for whom the session value is being saved for
     * @param pubZdbID     associated pub, can be null.
     * @param field        field name
     * @param value        value being saved
     * @return the newly created CuratorSession object
     */
    CuratorSession createCuratorSession(String curatorZdbID, String pubZdbID, String field, String value);

    List<Organization> getOrganizationsByName(String name);

    /**
     * Retrieve a source for a given marker and organizatioin
     *
     * @param marker       Marker
     * @param organization Organziation @return Source
     * @return Source
     */
    MarkerSupplier getSpecificSupplier(Marker marker, Organization organization);

    int removeSource(String supplierZdbID, String dataZdbID);

    /**
     * Retrieve an organization by ID
     *
     * @param zdbID zdbID (PK)
     * @return Organization
     */
    Organization getOrganizationByID(String zdbID);

    /**
     * Delete a supplier fomr the records. Does not delete the lab or company.
     *
     * @param supplier Supplier
     */
    void deleteSupplier(MarkerSupplier supplier);


    /**
     * Delete account Info of person, but not the person record.
     *
     * @param person Person
     */
    void deleteAccountInfo(Person person);

    /**
     * Retrieve a User for a given login.
     *
     * @param login login string
     * @return boolean
     */
    boolean userExists(String login);

    /**
     * Update a users account info and record the changes.
     *
     * @param currentPerson  person info on which updates are applied
     * @param newAccountInfo new account info
     */
    void updateAccountInfo(Person currentPerson, AccountInfo newAccountInfo);

    /**
     * Persist section visibility
     *
     * @param pubID       pub ID
     * @param showSection attribute name
     * @param visibility  attribute value
     */
    void setCuratorSession(String pubID, CuratorSession.Attribute showSection, boolean visibility);

    /**
     * Persist experiment section visibility
     *
     * @param publicationID pub ID
     * @param attributeName attribute name
     * @param zdbID         zdbID
     */
    void setCuratorSession(String publicationID, CuratorSession.Attribute attributeName, String zdbID);

    /**
     * Retrieve a person record by login name.
     *
     * @param login login
     * @return person
     */
    Person getPersonByName(String login);

    /**
     * Delete a curator session element.
     *
     * @param session element
     */
    void deleteCuratorSession(CuratorSession session);
}
