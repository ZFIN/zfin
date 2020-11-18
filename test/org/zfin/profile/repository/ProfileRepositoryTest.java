package org.zfin.profile.repository;

import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.profile.*;
import org.zfin.profile.presentation.*;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.UserDetailServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Class PeopleRepositoryTest.
 */

public class ProfileRepositoryTest extends AbstractDatabaseTest {
    private static final String REAL_PERSON_1_ZDB_ID = "ZDB-PERS-960805-676"; //Monte;
    private static final String REAL_PERSON_2_ZDB_ID = "ZDB-PERS-970321-3"; //George S.

    private static final ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();


    @After
    public void closeSession() {
        super.closeSession();
        // make sure to close the session to be able to re-create the entities
        HibernateUtil.closeSession();
    }

    @Test
    public void createAndUpdateCuratorSession() {

        Person person = profileRepository.getPerson(REAL_PERSON_1_ZDB_ID);
        Person person2 = profileRepository.getPerson(REAL_PERSON_2_ZDB_ID);
        Publication pub = person.getPublications().iterator().next();

        String field = "This is my field";
        String value = "This is my value";

        profileRepository.createCuratorSession(person.getZdbID(), pub.getZdbID(), field, value);
        profileRepository.createCuratorSession(person2.getZdbID(), pub.getZdbID(), field, value);

        CuratorSession databaseCS = profileRepository.getCuratorSession(person.getZdbID(), pub.getZdbID(), field);

        assertNotNull("curator session created successfully", databaseCS);
        assertNotNull("curator session created with PK id", databaseCS.getID());
        assertEquals("curator session value is correct", databaseCS.getValue(), value);


    }

    @Test
    public void createAndUpdateCuratorSessionWithNoPublication() {

        Person person = profileRepository.getPerson(REAL_PERSON_1_ZDB_ID);
        String field = "This is my field";
        String value = "This is my value";

        profileRepository.createCuratorSession(REAL_PERSON_1_ZDB_ID, null, field, value);

        CuratorSession databaseCS = profileRepository.getCuratorSession(person.getZdbID(), null, field);

        assertNotNull("curator session created successfully", databaseCS);
        assertNotNull("curator session created with PK id", databaseCS.getID());
        assertEquals("curator session value is correct", databaseCS.getValue(), value);

    }

    /**
     * Test that creation of a new person object including a user object
     * creates a single PK for both of them. User is a value object and is
     * tied to the Person object: one-to-one relationhip.
     */
    @Test
    public void createPersonWithAccountInfo() {
        Person person = getTestPerson();
        HibernateUtil.currentSession().save(person);

        String personID = person.getZdbID();
        assertTrue("PK created", personID != null && personID.startsWith("ZDB-PERS"));

    }

    /**
     * Test that creation of a new person object without a user object works without
     * creating a user object in the database.
     */
    @Test
    public void createPersonOnly() {
        Person person = getTestPerson();
        person.setAccountInfo(null);
        HibernateUtil.currentSession().save(person);

        String personID = person.getZdbID();
        assertTrue("PK created", personID != null && personID.startsWith("ZDB-PERS"));
        assertNull("No user object created", person.getAccountInfo());
    }

    @Test
    public void retrievePersonAndAccountInfo() {
        // monte
        String zdbID = "ZDB-PERS-960805-676";
        Person person = profileRepository.getPerson(zdbID);
        assertNotNull(person);
        assertNotNull(person.getAccountInfo());
    }

    private Person getTestPerson() {
        Person person = new Person();
        person.setShortName("Test Person");
        person.setEmail("Email Address Test");
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLogin("newUser");
        accountInfo.setRole("root");
        accountInfo.setName("Test Person");
        accountInfo.setLoginDate(new Date());
        accountInfo.setAccountCreationDate(new Date());
        person.setAccountInfo(accountInfo);
        return person;
    }

    @Test
    public void getMatchingOrganizations() {
        String name = "zeb";
        List<Organization> orgs = profileRepository.getOrganizationsByName(name);
        assertNotNull(orgs);

    }


    @Test
    public void getOrganizationToWork() {
        Organization organization1 = HibernateUtil.currentSession().get(Organization.class, "ZDB-LAB-001018-2");
        assertNotNull(organization1);
    }

    @Test
    public void getSuppliersLinkForMarker() {
        // test lab
        List<OrganizationLink> organizationLinks;
        organizationLinks = profileRepository.getSupplierLinksForZdbId("ZDB-CDNA-050503-2");
        assertEquals(1, organizationLinks.size());
        String linkText1 = "<a href=\"/action/profile/view/ZDB-LAB-991005-53\">Zebrafish International Resource Center (ZIRC)</a>";
        linkText1 += " ";
        linkText1 += "<span style=\"font-size: small;\">(<a href=\"http://zebrafish.org?OID=ZDB-CDNA-050503-2\">order this</a>)</span>";
        assertEquals(linkText1, organizationLinks.iterator().next().getLinkWithAttributionAndOrderThis());

        // test has source url
        organizationLinks = profileRepository.getSupplierLinksForZdbId("ZDB-GENO-960809-7");
        assertEquals(1, organizationLinks.size());
        String linkText2 = "<a href=\"/action/profile/view/ZDB-LAB-991005-53\">Zebrafish International Resource Center (ZIRC)</a> <span style=\"font-size: small;\">(<a href=\"http://zebrafish.org?OID=ZDB-GENO-960809-7\">order this</a>)</span>";
        assertEquals(linkText2, organizationLinks.iterator().next().getLinkWithAttributionAndOrderThis());
    }

    @Test
    public void getCompany() {
        Company company = profileRepository.getCompanyById("ZDB-COMPANY-081009-1");
        assertNotNull(company);
    }

    @Test
    public void getPersonFields() {
        Person person = profileRepository.getPerson("ZDB-PERS-960805-676");
        assertTrue(person.getPersonalBio().contains("Princeton University"));
        assertTrue(person.getNonZfinPublications().contains("Westerfield"));
        assertNotNull(person);
    }

    @Test
    public void getCompanyLinks() {
        List<CompanyPresentation> companyPresentations = profileRepository.getCompanyForPersonId("ZDB-PERS-000502-1");
        assertThat(companyPresentations.size(), greaterThan(0));
    }

    @Test
    public void getLabLinks() {
        List<LabPresentation> labs = profileRepository.getLabsForPerson("ZDB-PERS-960805-676");
        assertEquals(3, labs.size()); // currently just 3 labs!
        String link = labs.get(0).getLink();
        assertNotNull(link);
    }

    @Test
    public void getLabMembers() {
        List<PersonMemberPresentation> personMemberPresentations = profileRepository.getLabMembers("ZDB-LAB-000914-1");
        assertThat(personMemberPresentations.size(), greaterThan(10));
        assertThat(personMemberPresentations.size(), lessThan(100));
    }

    @Test
    public void getPublicationsForLab() {
        List<Publication> publications = profileRepository.getPublicationsForLab("ZDB-LAB-000914-1");
        assertThat(publications.size(), greaterThan(10));
        assertThat(publications.size(), lessThan(300));
    }

    @Test
    public void getCompanyMembers() {
        List<PersonMemberPresentation> personMemberPresentations = profileRepository.getCompanyMembers("ZDB-COMPANY-000713-1");
        assertThat(personMemberPresentations.size(), greaterThan(5));
        assertThat(personMemberPresentations.size(), lessThan(30));
    }

    @Test
    public void getPublicationsForCompany() {
        List<Publication> publications = profileRepository.getPublicationsForCompany("ZDB-COMPANY-100928-1");
        assertThat(publications.size(), greaterThan(1));
        assertThat(publications.size(), lessThan(10));
    }

    @Test
    public void deleteFromOrganization() {

        int result;
        result = profileRepository.removeMemberFromOrganization("ZDB-PERS-960805-676", "ZDB-LAB-000914-1");
        assertEquals(1, result);
        result = profileRepository.removeMemberFromOrganization("ZDB-PERS-010126-3", "ZDB-COMPANY-020115-1");
        assertEquals(1, result);
    }

    @Test
    public void getPersonNamesForString() {
        assertThat(profileRepository.getPersonNamesForString("West").size(), greaterThan(3));
        assertThat(profileRepository.getPersonNamesForString("West").size(), lessThan(100));
    }

    @Test
    public void addLabMember() {
        int result = profileRepository.addLabMember("ZDB-PERS-000329-1", "ZDB-LAB-000914-1", 7);
        assertEquals(1, result);
    }

    @Test
    public void addCompanyMember() {
        int result = profileRepository.addCompanyMember("ZDB-PERS-000329-1", "ZDB-COMPANY-001017-1", 2);
        assertEquals(1, result);
    }


    @Test
    public void getLabPositions() {
        int numPositions = profileRepository.getLabPositions().size();
        assertThat(numPositions, greaterThan(9));
        assertThat(numPositions, lessThan(11));
    }

    @Test
    public void getCompanyPositions() {
        int numPositions = profileRepository.getCompanyPositions().size();
        assertThat(numPositions, greaterThan(6));
        assertThat(numPositions, lessThan(8));
    }

    @Test
    public void removeLabMember() {
        int result;
        result = profileRepository.removeLabMember("ZDB-PERS-000329-1", "ZDB-LAB-000914-1");
        assertEquals(0, result);
        result = profileRepository.addLabMember("ZDB-PERS-000329-1", "ZDB-LAB-000914-1", 4);
        assertEquals(1, result);
        result = profileRepository.removeLabMember("ZDB-PERS-000329-1", "ZDB-LAB-000914-1");
        assertEquals(1, result);
    }

    @Test
    public void removeCompanyMember() {
        int result;
        result = profileRepository.removeCompanyMember("ZDB-PERS-000329-1", "ZDB-COMPANY-001017-1");
        assertEquals(0, result);
        result = profileRepository.addCompanyMember("ZDB-PERS-000329-1", "ZDB-COMPANY-001017-1", 3);
        assertEquals(1, result);
        result = profileRepository.removeCompanyMember("ZDB-PERS-000329-1", "ZDB-COMPANY-001017-1");
        assertEquals(1, result);
    }

    @Test
    public void checkPasswords() throws ParseException {
        List<Person> list = HibernateUtil.currentSession().createQuery("from Person", Person.class).getResultList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date when = sdf.parse("2017-3-31");

        //System.out.println("date1 : " + sdf.format(when));
        for (Person person : list) {
            AccountInfo info = person.getAccountInfo();
            if (info != null) {
                String saltedLogin = new Md5PasswordEncoder().encodePassword(info.getLogin(), "dedicated to George Streisinger");
                if (info.getPassword() != null && info.getPassword().equals(saltedLogin) && info.getPreviousLoginDate() != null
                        && info.getPreviousLoginDate().after(when)) {
                    //System.out.println(index++ +": "+person.getZdbID()+": "+info.getPreviousLoginDate().toString());
                    //System.out.println(index++ +": "+person.getEmail());
                    //System.out.println(person.getFullName());
                }

            }

        }
    }

    @Test
    public void getOrganizationByZdbID() {
        Organization organizationByZdbID = profileRepository.getOrganizationByZdbID("ZDB-LAB-980205-5");
        assertNotNull(organizationByZdbID);
    }

    @Test
    public void getPersonByLastNameStartsWith() {
        List<Person> personList;
        personList = profileRepository.getPersonByLastNameStartsWith("d");
        assertThat(personList.size(), greaterThan(4));
        assertThat(personList.size(), lessThan(5000));

        personList = profileRepository.getPersonByLastNameStartsWith(null);
        assertThat(personList.size(), equalTo(0));
//        assertThat(personList.size(),lessThan(5000));
    }

    @Test
    public void isOrganizationPersonExist() {
        assertTrue(profileRepository.isOrganizationPersonExist("ZDB-PERS-960805-676", "ZDB-LAB-000914-1"));
        assertFalse(profileRepository.isOrganizationPersonExist("ZDB-PERS-040204-2", "ZDB-LAB-000914-1"));
    }

    @Test
    public void companySearch() {
        CompanySearchBean companySearchBean = new CompanySearchBean();
        companySearchBean.setMaxDisplayRecords(100);
        PaginationResult<Company> companyList;
        companySearchBean.setName("Aquatic");
        companyList = profileRepository.searchCompanies(companySearchBean);
        int size1 = companyList.getTotalCount();
        assertThat(size1, greaterThan(2));
        assertThat(size1, lessThan(50));

        companySearchBean.setName("AQUATIC");
        companyList = profileRepository.searchCompanies(companySearchBean);
        assertThat(size1, equalTo(companyList.getTotalCount()));

        companySearchBean.setAddress("DE");
        companyList = profileRepository.searchCompanies(companySearchBean);
        size1 = companyList.getTotalCount();
        assertThat(size1, greaterThan(0));
        assertThat(size1, lessThan(50));

        companySearchBean.setAddress("de");
        companyList = profileRepository.searchCompanies(companySearchBean);
        assertThat(size1, equalTo(companyList.getTotalCount()));

        companySearchBean = new CompanySearchBean();
        companySearchBean.setContainsType("url");
        companySearchBean.setContains("fish");
        companySearchBean.setMaxDisplayRecords(100);
        companyList = profileRepository.searchCompanies(companySearchBean);
        assertThat(companyList.getTotalCount(), greaterThan(0));
        assertThat(companyList.getTotalCount(), lessThan(50));


    }

    @Test
    public void labSearch() {
        LabSearchBean companySearchBean = new LabSearchBean();
        companySearchBean.setMaxDisplayRecords(100);
        PaginationResult<Lab> companyList;
        companySearchBean.setName("Wester");
        companyList = profileRepository.searchLabs(companySearchBean);
        int size1 = companyList.getPopulatedResults().size();
        assertThat(size1, greaterThan(1));
        assertThat(size1, lessThan(20));
        assertThat(companyList.getTotalCount(), equalTo(size1));

        companySearchBean.setName("wester");
        companyList = profileRepository.searchLabs(companySearchBean);
        assertThat(size1, equalTo(companyList.getPopulatedResults().size()));

        companySearchBean.setName(null);
        companySearchBean.setAddress("Oregon");
        companyList = profileRepository.searchLabs(companySearchBean);
        size1 = companyList.getPopulatedResults().size();
        assertThat(size1, greaterThan(0));
        assertThat(size1, lessThan(50));
        assertThat(companyList.getTotalCount(), equalTo(size1));

        companySearchBean.setAddress("oregon");
        companyList = profileRepository.searchLabs(companySearchBean);
        assertThat(size1, equalTo(companyList.getPopulatedResults().size()));

        companySearchBean = new LabSearchBean();
        companySearchBean.setMaxDisplayRecords(100);
        companySearchBean.setContainsType("url");
        companySearchBean.setContains("fish");
        companyList = profileRepository.searchLabs(companySearchBean);
        assertThat(companyList.getPopulatedResults().size(), greaterThan(0));
        assertThat(companyList.getPopulatedResults().size(), lessThan(50));
        assertThat(companyList.getTotalCount(), equalTo(companyList.getPopulatedResults().size()));
        companySearchBean.setMaxDisplayRecords(5);
        companyList = profileRepository.searchLabs(companySearchBean);
        assertThat(companyList.getPopulatedResults().size(), equalTo(5));
        assertThat(companyList.getTotalCount(), greaterThan(companyList.getPopulatedResults().size()));
    }


    @Test
    public void personSearch() {
        PersonSearchBean personSearchBean = new PersonSearchBean();
        personSearchBean.setMaxDisplayRecords(100);
        personSearchBean.setName("Dunn");
        PaginationResult<Person> personList;
        personList = profileRepository.searchPeople(personSearchBean);
        assertThat(personList.getTotalCount(), greaterThan(0));
        assertThat(personList.getTotalCount(), lessThan(20));

    }

    @Test
    public void findPersonByLoginName() {
        UserDetailsService service = new UserDetailServiceImpl();
        Person person = (Person) service.loadUserByUsername("cmpich");
        assertNotNull(person);
    }

    @Test
    public void findStudentAccounts() {
        List<Person> students = profileRepository.getStudents();
        assertNotNull(students);
        assertThat(students.size(), greaterThan(0));
    }

}
