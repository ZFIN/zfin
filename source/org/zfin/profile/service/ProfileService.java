package org.zfin.profile.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.*;

import org.zfin.profile.presentation.PersonMemberPresentation;
import org.zfin.profile.presentation.ProfileUpdateMessageBean;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.Blob;
import java.util.*;
import java.text.Collator;

/**
 */
@Service
public class ProfileService {

    public static final String SALT = "dedicated to George Streisinger";

    private Logger logger = Logger.getLogger(ProfileService.class);
    private Md5PasswordEncoder encoder = new Md5PasswordEncoder();

    @Autowired
    private BeanCompareService beanCompareService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private FeatureRepository featureRepository;

/*
    @Autowired
    private JmsTemplate jmsTemplate;
*/

    public String createUuid() {
        return UUID.randomUUID().toString();
    }

    public Person refreshSecurityPerson() {
        Person p = getCurrentSecurityUser();
        if (p.getZdbID() != null) {
            p = profileRepository.getPerson(p.getZdbID());
        }
        return p;
    }

    /**
     * This returns a Person object of the current security person.
     * If no authorized Person is found return null.
     *
     * @return Person object
     */
    public static Person getCurrentSecurityUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            Person person = new Person();
            person.setShortName("Guest");
            person.setLastName("User");
            person.setFirstName("Guest");
            return person;
        }
        Object principal = authentication.getPrincipal();
        // ToDo: Annonymous user should also be a Person object opposed to a String object
        if (principal instanceof String) {
            return null;
        }

        // for debugging.  Allows using an in-line spring authentication-manager.
        if (principal instanceof User) {
            User user =
                    (User) principal;
            Person person = new Person();
            person.setShortName(user.getUsername());
            return person;
        }
        // make sure this object
        return (Person) principal;
    }

    /**
     * This returns a Person object of the current security person.
     * If no authorized Person is found return null.
     *
     * @return Is user root?
     */
    public boolean isCurrentSecurityUserRoot() {
        Person person = getCurrentSecurityUser();
        if (person == null || person.getAccountInfo() == null) {
            return false;
        }
        return person.getAccountInfo().getRole().equals(AccountInfo.Role.ROOT.toString());
    }

//    private BeanComparator beanComparator = new BeanComparator() ;

    /**
     * Copies attributes from the new onto the old, noting what is updated.
     * Updated fields are fullName, name, address, phone, fax, email, url, emailList, bio, nonZfinPubs, deceased.
     *
     * @param oldPerson
     * @param newPerson
     * @return
     */
    public List<BeanFieldUpdate> comparePersonFields(Person oldPerson, Person newPerson) throws Exception {
        List<BeanFieldUpdate> fieldUpdateList = new ArrayList<BeanFieldUpdate>();
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("firstName", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("lastName", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("fullName", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("shortName", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("email", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("address", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("country", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("fax", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("phone", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("url", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("orcidID", oldPerson, newPerson));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("emailList", oldPerson, newPerson, false, true));

        if (getCurrentSecurityUser() != null   // it's a logged-in user
                && isCurrentSecurityUserRoot()) {  //  the user logged in as root
            CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("deceased", oldPerson, newPerson, false, true));
        }


        return fieldUpdateList;
    }

    public List<BeanFieldUpdate> compareBiographyField(Person oldPerson, Person newPerson) throws Exception {
        List<BeanFieldUpdate> fieldUpdateList = new ArrayList<BeanFieldUpdate>();
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("personalBio", oldPerson, newPerson));
        logger.debug("compareBiographyField fields: " + fieldUpdateList.toString());
        return fieldUpdateList;
    }

    public List<BeanFieldUpdate> comparePublicationsField(Person oldPerson, Person newPerson) throws Exception {
        List<BeanFieldUpdate> fieldUpdateList = new ArrayList<BeanFieldUpdate>();
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("nonZfinPublications", oldPerson, newPerson));
        return fieldUpdateList;
    }


    public List<BeanFieldUpdate> compareAccountInfoFields(AccountInfo oldAccountInfo, AccountInfo newAccountInfo) throws Exception {
        List<BeanFieldUpdate> fieldUpdateList = new ArrayList<BeanFieldUpdate>();
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("login", oldAccountInfo, newAccountInfo));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("role", oldAccountInfo, newAccountInfo));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("password", oldAccountInfo, newAccountInfo));
        return fieldUpdateList;
    }

    public void updateProfileWithFields(String profileZdbID, List<BeanFieldUpdate> fields, String securityPersonZdbID)
            throws Exception {

        if (profileZdbID.startsWith("ZDB-PERS")) {
            Person person = profileRepository.getPerson(profileZdbID);
            updatePersonWithFields(person, fields, securityPersonZdbID);
        } else if (profileZdbID.startsWith("ZDB-LAB")) {
            Lab lab = profileRepository.getLabById(profileZdbID);
            updateLabWithFields(lab, fields, securityPersonZdbID);
        } else if (profileZdbID.startsWith("ZDB-COMPANY")) {
            Company company = profileRepository.getCompanyById(profileZdbID);
            updateCompanyWithFields(company, fields, securityPersonZdbID);
        }

    }

    public void updatePersonWithFields(Person person, List<BeanFieldUpdate> fields, String securityPersonZdbID)
            throws Exception {

        beanCompareService.applyUpdates(person, fields);

        HibernateUtil.currentSession().update(person);
        for (BeanFieldUpdate beanFieldUpdate : fields) {
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(person.getZdbID(), beanFieldUpdate);
        }

        if (person.getZdbID().equals(securityPersonZdbID)) {
            refreshSecurityPerson();
        }
    }

    public void updateLabWithFields(Lab lab, List<BeanFieldUpdate> fields, String securityPersonZdbID)
            throws Exception {
        beanCompareService.applyUpdates(lab, fields);

        // handle Prefixes
        for (BeanFieldUpdate beanFieldUpdate : fields) {
            if (beanFieldUpdate.getField().equals("prefix")) {
                setCurrentPrefix(lab, beanFieldUpdate.getTo().toString());
            }
        }

        // handle Person

        HibernateUtil.currentSession().update(lab);
        for (BeanFieldUpdate beanFieldUpdate : fields) {
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(lab.getZdbID(), beanFieldUpdate);
        }
    }

    protected void setCurrentPrefix(Organization organization, String prefix) {
        // get new prefix id from prefix unless null
        if (prefix.startsWith("-")) {
            featureRepository.setNoLabPrefix(organization.getZdbID());
            return;
        }

        FeaturePrefix featurePrefix = featureRepository.getFeaturePrefixByPrefix(prefix);

        // set all current designations to false
        // if the current designation exists for that lab
        String returnedPrefix = featureRepository.setCurrentPrefix(organization.getZdbID(), featurePrefix.getPrefixString());
        if (returnedPrefix == null) {
            featureRepository.insertOrganizationPrefix(organization, featurePrefix);
        }

    }

    public void updateCompanyWithFields(Company company, List<BeanFieldUpdate> fields, String securityPersonZdbID)
            throws Exception {
        beanCompareService.applyUpdates(company, fields);
        HibernateUtil.currentSession().update(company);

        for (BeanFieldUpdate beanFieldUpdate : fields) {
            if (beanFieldUpdate.getField().equals("prefix")) {
                setCurrentPrefix(company, beanFieldUpdate.getTo().toString());
            }
        }

        for (BeanFieldUpdate beanFieldUpdate : fields) {
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(company.getZdbID(), beanFieldUpdate);
        }
    }

    public void setBeanCompareService(BeanCompareService beanCompareService) {
        this.beanCompareService = beanCompareService;
    }

    public void setProfileRepository(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public void setFeatureRepository(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    public void updateImage(String zdbID, String securityPersonZdbID, Blob snapshot) throws Exception {

        if (snapshot != null)
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(zdbID, "snapsnot", "updating snapshot " + snapshot.length() + " bytes");
        else
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(zdbID, "snapshot", "deleting snapshot");

        if (zdbID.startsWith("ZDB-PERS")) {
            Person person = profileRepository.getPerson(zdbID);
            person.setSnapshot(snapshot);
            HibernateUtil.currentSession().update(person);

            if (zdbID.equals(securityPersonZdbID)) {
                refreshSecurityPerson();
            }
        } else if (zdbID.startsWith("ZDB-COMPANY")) {
            Company company = profileRepository.getCompanyById(zdbID);
            company.setSnapshot(snapshot);
            HibernateUtil.currentSession().update(company);
        } else if (zdbID.startsWith("ZDB-LAB")) {
            Lab lab = profileRepository.getLabById(zdbID);
            lab.setSnapshot(snapshot);
            HibernateUtil.currentSession().update(lab);
        }
    }


    public void deleteImage(String zdbID, String securityPersonZdbID) throws Exception {
        updateImage(zdbID, securityPersonZdbID, null);
    }

    public Collection<BeanFieldUpdate> compareLabFields(Lab oldLab, Lab newLab) throws Exception {
        List<BeanFieldUpdate> fieldUpdateList = new ArrayList<BeanFieldUpdate>();
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("name", oldLab, newLab));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("phone", oldLab, newLab));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("fax", oldLab, newLab));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("email", oldLab, newLab));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("url", oldLab, newLab));
        BeanFieldUpdate beanFieldUpdate = beanCompareService.compareBeanField("address", oldLab, newLab);
        if (beanFieldUpdate != null) {
            beanFieldUpdate.setNullToTrueNull();
        }
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanFieldUpdate);
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("bio", oldLab, newLab));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("contactPerson", oldLab, newLab));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("prefix", oldLab, newLab));

        return fieldUpdateList;
    }

    public String isEditableBySecurityPerson(Person person) {
        if (isCurrentSecurityUserRoot()) {
            return getCurrentSecurityUser().getZdbID();
        }
        if (person != null) {
            return isEditableBySecurityPerson(person.getZdbID());
        } else {
            throw new RuntimeException("No user to check security status");
        }
    }

    public String isEditableBySecurityPerson(String zdbID) {
        // if you do not have a security person, you should not be edting, so NPE is okay.
        String securityPersonZdbID = getCurrentSecurityUser().getZdbID();
        // handle security stuff, here:
        if (false == isCurrentSecurityUserRoot()) {
            if (false == securityPersonZdbID.equals(zdbID)) {
                throw new RuntimeException("Submitter [" + securityPersonZdbID +
                        "] may only edit their own record not [" + zdbID + "]");
            }
        }
        return securityPersonZdbID;
    }

    public Collection<BeanFieldUpdate> compareCompanyFields(Company oldCompany, Company newCompany) throws Exception {
        List<BeanFieldUpdate> fieldUpdateList = new ArrayList<BeanFieldUpdate>();
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("name", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("phone", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("fax", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("email", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("url", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("address", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("bio", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("contactPerson", oldCompany, newCompany));
        CollectionUtils.addIgnoreNull(fieldUpdateList, beanCompareService.compareBeanField("prefix", oldCompany, newCompany));
        return fieldUpdateList;
    }

    public boolean addPersonToOrganization(PersonMemberPresentation personMemberPresentation) {
        String personZdbID = personMemberPresentation.getZdbID();
        String organizationZdbID = personMemberPresentation.getOrganizationZdbID();
        Integer position = personMemberPresentation.getPosition();
        if (profileRepository.isOrganizationPersonExist(personZdbID, organizationZdbID)) {
            logger.error("Person[" + personZdbID + "] already belongs to organization[" + organizationZdbID + "]");
            return false;
        }
        if (organizationZdbID.startsWith("ZDB-LAB")) {
            return profileRepository.addLabMember(personZdbID, organizationZdbID, position) == 1;
        } else if (organizationZdbID.startsWith("ZDB-COMPANY")) {
            return profileRepository.addCompanyMember(personZdbID, organizationZdbID, position) == 1;
        }

        logger.error("failed trying to add a person to something that was not a lab or company: " + organizationZdbID);
        return false;
    }

    public boolean changeOrganizationPosition(PersonMemberPresentation personMemberPresentation) {
        String personZdbID = personMemberPresentation.getZdbID();
        String organizationZdbID = personMemberPresentation.getOrganizationZdbID();
        Integer position = personMemberPresentation.getPosition();
        String positionString = personMemberPresentation.getPositionString();

        if (organizationZdbID.startsWith("ZDB-LAB")) {
            return profileRepository.changeLabPosition(personZdbID, organizationZdbID, position) == 1;
        } else if (organizationZdbID.startsWith("ZDB-COMPANY")) {
            return profileRepository.changeCompanyPosition(personZdbID, organizationZdbID, position) == 1;
        }

        logger.error("failed trying to change position for " + organizationZdbID);
        return false;
    }


    public boolean removePersonFromOrganization(PersonMemberPresentation personMemberPresentation) {
        String personZdbID = personMemberPresentation.getZdbID();
        String organizationZdbID = personMemberPresentation.getOrganizationZdbID();
        if (organizationZdbID.startsWith("ZDB-LAB")) {
            return profileRepository.removeLabMember(personZdbID, organizationZdbID) == 1;
        } else if (organizationZdbID.startsWith("ZDB-COMPANY")) {
            return profileRepository.removeCompanyMember(personZdbID, organizationZdbID) == 1;
        }

        logger.error("failed trying to remove a person to something that was not a lab or company: " + organizationZdbID);
        return false;
    }
    /**
     * using this method to get a list of Contries for use in Person edit
     */
    public static List<String> getCountries(final Locale inLocale) {
        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<String>(countryCodes.length);
        for (String countryCode : countryCodes) {
            Locale obj = new Locale("", countryCode);
            countries.add(obj.getDisplayCountry(inLocale));

        }
        Collections.sort(countries);
        return countries;
    }

        /**
     * If there is not an address for this person then insert the join record.
     * If there is one, then update the join record.
     * If there are multiple . . . create an error log and update both records.
     *
     * @param address     address string to use for update.
     * @param personZdbId Id of the person to update.
     * @return Number of addresses updated.
     */
    public int setAddressForPerson(String address, String personZdbId) {

        Person p = profileRepository.getPerson(personZdbId);
        if (p == null) {
            logger.error("Invalid person ID: " + personZdbId);
            return -1;
        }

        p.setAddress(address);
        HibernateUtil.currentSession().update(p);
        return 1;
    }


    public int setMembersToOrganizationAddress(String organizationZdbID) {
        Organization organization = profileRepository.getOrganizationByZdbID(organizationZdbID);

        String address = organization.getAddress();
        int count = 0;

        if (organization.getCompany()) {
            for (PersonMemberPresentation personMemberPresentation : profileRepository.getCompanyMembers(organization.getZdbID())) {
                count += setAddressForPerson(address, personMemberPresentation.getZdbID());
            }
        } else {
            for (PersonMemberPresentation personMemberPresentation : profileRepository.getLabMembers(organization.getZdbID())) {
                count += setAddressForPerson(address, personMemberPresentation.getZdbID());
            }
        }
        return count;
    }

    public Lab createLab(Lab lab) {
        Person contactPerson = getCurrentSecurityUser();
        if (contactPerson == null) {
            throw new RuntimeException("Must be logged in to create a user.");
        }
        lab.setUrl(processUrl(lab.getUrl()));
        HibernateUtil.currentSession().save(lab);
        return lab;
    }

    public Company createCompany(Company company) {
        Person contactPerson = getCurrentSecurityUser();
        if (contactPerson == null) {
            throw new RuntimeException("Must be logged in to create a user.");
        }
        company.setUrl(processUrl(company.getUrl()));
        HibernateUtil.currentSession().save(company);
        return company;
    }


    public Person createPerson(Person person) {
        person.generateNameVariations();

        AccountInfo accountInfo = new AccountInfo();
        String login = person.getPutativeLoginName();
        if (StringUtils.isEmpty(login)) {
            login = person.getEmail();
        }
        accountInfo.setName(person.getFirstName() + " " + person.getLastName());
        accountInfo.setLogin(login);
        accountInfo.setRole(AccountInfo.Role.SUBMIT.toString());
        accountInfo.setPassword(encodePassword(person.getPass1()));
        accountInfo.setCookie(Math.random() + "-" + login);

        person.setDeceased(false);
        person.setAccountInfo(accountInfo);
        person.setUrl(processUrl(person.getUrl()));

        HibernateUtil.currentSession().save(person);
        HibernateUtil.currentSession().flush();
        accountInfo.setZdbID(person.getZdbID());
        HibernateUtil.currentSession().update(accountInfo);


        return person;
    }

    /*
     * Create a person, associate them with an organization and give
     * them the address of the organization.
     */
    public Person createPerson(Person person, PersonMemberPresentation pmp) {
        person = createPerson(person);
        pmp.setPersonZdbID(person.getZdbID());
        addPersonToOrganization(pmp);
        Organization organization = profileRepository.getOrganizationByZdbID(pmp.getOrganizationZdbID());
        setPersonAddressToOrganizationAddress(person, organization);
        return person;
    }

    public int setPersonAddressToOrganizationAddress(Person person, Organization organization) {

        String address = organization.getAddress();
        if (address == null) {
            logger.error("no address for organization: " + organization.getZdbID());
            return 0;
        }
        int returnValue = setAddressForPerson(address, person.getZdbID());
        final PersonMemberPresentation personMemberPresentation = new PersonMemberPresentation();
        personMemberPresentation.setAddressToExisting(address, person.getZdbID());
        HibernateUtil.currentSession().flush();
        return 1;
    }


    public void updateAccountInfoWithFields(String personZdbID, List<BeanFieldUpdate> fields, String securityPersonZdbId)
            throws Exception {
        Person person = profileRepository.getPerson(personZdbID);
        AccountInfo accountInfo = person.getAccountInfo();

        // so #@%#@%#@% stupid.  You have to do this explicitly, though even though they are joined.
        accountInfo.setZdbID(person.getZdbID());
        beanCompareService.applyUpdates(accountInfo, fields);
        HibernateUtil.currentSession().update(accountInfo);

        for (BeanFieldUpdate beanFieldUpdate : fields) {
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(accountInfo.getZdbID(), beanFieldUpdate);
        }
    }

    public String encodePassword(String password) {
        return encoder.encodePassword(password, ProfileService.SALT);
    }

    public int findMembersEquals(int value, List<PersonMemberPresentation> labMembers) {
        int count = 0;

        for (PersonMemberPresentation personMemberPresentation : labMembers) {
            if (personMemberPresentation.getPosition() == value) {
                ++count;
            }
        }

        return count;
    }


    public String getLabPositionString(Integer positionId) {
        List<OrganizationPosition> positions;
        String positionTitle = null;
        positions = profileRepository.getLabPositions();

        for (OrganizationPosition op : positions) {
            if (op.getId().equals(positionId))
                positionTitle = op.getName();
        }
        return positionTitle;
    }


    public String getCompanyPositionString(Integer positionId) {
        List<OrganizationPosition> positions;
        String positionTitle = null;
        positions = profileRepository.getCompanyPositions();

        for (OrganizationPosition op : positions) {
            if (op.getId().equals(positionId))
                positionTitle = op.getName();
        }
        return positionTitle;
    }


    public String handleInfoUpdate(
            Errors errors
            /*, ActiveMQTopic profileTopic*/
            , final String zdbID
            , final List<BeanFieldUpdate> fields
            , final String securityPersonZdbId) {
        try {
            HibernateUtil.createTransaction();
            updateProfileWithFields(zdbID, fields, securityPersonZdbId);

            final ProfileUpdateMessageBean profileUpdateMessageBean = new ProfileUpdateMessageBean();
            profileUpdateMessageBean.setZdbIdToEdit(zdbID);
            profileUpdateMessageBean.setFields(fields);
            profileUpdateMessageBean.setSecurityPersonZdbID(securityPersonZdbId);

            //this used to be in a jms callback
            //handlePersonUpdateMessage(profileUpdateMessageBean);

            HibernateUtil.currentSession().flush();
            HibernateUtil.currentSession().getTransaction().commit();


            return "profile/profile-edit.page";
        } catch (ConstraintViolationException cve) {
            logger.error("Constraint violation when updating lab", cve);
            HibernateUtil.rollbackTransaction();

            for (ConstraintViolation constraintViolation : cve.getConstraintViolations()) {
                errors.rejectValue(constraintViolation.getPropertyPath().toString(), ""
                        , "Invalid formatting of field [" + constraintViolation.getPropertyPath() + "].  "
//                        + "] value["+constraintViolation.getInvalidValue()+"].  "
//                        + constraintViolation.getMessageTemplate());
                        + constraintViolation.getMessage());
            }
            return "profile/profile-edit.page";
        } catch (Exception e) {
            logger.error("Failed to update", e);
            HibernateUtil.rollbackTransaction();
            errors.reject("", "There was a problem updated the user record");
            return "profile/profile-edit.page";
        }

    }


    protected void handlePersonUpdateMessage(ProfileUpdateMessageBean bean) throws Exception {
        switch (bean.getProfileType()) {
            case PERSON:
                updateProfileWithFields(
                        bean.getZdbIdToEdit()
                        , bean.getFields()
                        , bean.getSecurityPersonZdbID());
                break;
            case ACCOUNT_INFO:
                updateAccountInfoWithFields(
                        bean.getZdbIdToEdit()
                        , bean.getFields()
                        , bean.getSecurityPersonZdbID());
                break;
            default:
                throw new RuntimeException("Bad profile update");
        }
        logger.debug("updated: " + bean.getSecurityPersonZdbID());
    }

    protected void setEmptyFieldToNull(BeanFieldUpdate field) {
        if (field.getFrom() == null
                && field.getTo() instanceof String
                && StringUtils.equals((String) field.getTo(), ""))
            field.setTo(null);
    }

    public String processUrl(String url) {

        logger.debug("processing url, before: " + url);
        String newUrl;

        //if the url is set and doesn't start with http://, magically make it happen, just like browsers do
        if (!StringUtils.isEmpty(url) && !url.startsWith("http://"))
            newUrl = "http://" + url;
        else
            newUrl = url;

        logger.debug("processing url, after: " + newUrl);

        return newUrl;
    }

    public static boolean isRootUser() {
        if (getCurrentSecurityUser() == null)
            return false;
        return getCurrentSecurityUser().getAccountInfo().getRoot();
    }
}
