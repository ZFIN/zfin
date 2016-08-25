package org.zfin.profile.repository;

import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.profile.*;
import org.zfin.profile.presentation.*;
import org.zfin.publication.Publication;

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


    void insertLab(Lab lab);

    void addSupplier(Organization organization, Marker marker);

    void removeSupplier(Organization organization, Marker marker);

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

    List<Person> getPeopleByFullName(String fullName);

    /**
     * Delete a curator session element.
     *
     * @param session element
     */
    void deleteCuratorSession(CuratorSession session);

    /**
     * Retrieve curator session.
     *
     * @param publicationID    publication
     * @param boxDivID         div element
     * @param mutantDisplayBox attribute
     * @return curator session
     */
    CuratorSession getCuratorSession(String publicationID, String boxDivID, CuratorSession.Attribute mutantDisplayBox);

    Lab getLabById(String labZdbId);

    List<OrganizationLink> getSupplierLinksForZdbId(String zdbID);

    Company getCompanyById(String zdbID);

    List<CompanyPresentation> getCompanyForPersonId(String zdbID);

    List<LabPresentation> getLabsForPerson(String zdbID);

    List<PersonMemberPresentation> getLabMembers(String zdbID);

    List<Publication> getPublicationsForLab(String zdbID);

    List<PersonMemberPresentation> getCompanyMembers(String zdbID);

    List<Publication> getPublicationsForCompany(String zdbID);

    int removeMemberFromOrganization(String personZdbID, String organizationZdbID);

    List<PersonLookupEntry> getPersonNamesForString(String lookupString);

    int addCompanyMember(String personZdbID, String organizationZdbID, Integer position);

    int addLabMember(String personZdbID, String organizationZdbID, Integer position);

    List<OrganizationPosition> getLabPositions();

    List<OrganizationPosition> getCompanyPositions();

    int changeLabPosition(String personZdbID, String organizationZdbID, Integer positionID);

    int changeCompanyPosition(String personZdbID, String organizationZdbID, Integer positionID);

    int removeLabMember(String personZdbID, String organizationZdbID);

    int removeCompanyMember(String personZdbID, String organizationZdbID);

    Organization getOrganizationByZdbID(String orgZdbID);

    List<Lab> getLabs();

    List<Company> getCompanies();

    List<Person> getPersonByLastNameStartsWith(String lastNameStartsWith);

    List<Person> getPersonByLastNameStartsWithAndFirstNameStartsWith(String lastNameStartsWith, String firstNameStartsWith);

    List<Person> getPersonByLastNameEqualsAndFirstNameStartsWith(String lastName, String firstNameStartsWith);

    boolean isOrganizationPersonExist(String personZdbID, String organizationZdbID);

    Address getAddress(Long addressId);

    PaginationResult<Company> searchCompanies(CompanySearchBean searchBean);

    PaginationResult<Lab> searchLabs(LabSearchBean searchBean);

    PaginationResult<Person> searchPeople(PersonSearchBean searchBean);

    List<String> getSuppliedDataIds(Organization organization);

    List<String> getSourcedDataIds(Organization organization);

    List<String> getDistributionList();

    List<String> getPiDistributionList();

    List<String> getUsaDistributionList();

    List<Person> getUsersByRole(String role);

}
