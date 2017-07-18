package org.zfin.profile.service;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 */
public class ProfileServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(ProfileServiceTest.class);

    private ProfileService profileService = new ProfileService();
    private ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
    private FeatureRepository featureRepository = RepositoryFactory.getFeatureRepository();


    @After
    public void closeSession() {
        super.closeSession();
        // make sure to close the session to be able to re-create the entities
        HibernateUtil.closeSession();
    }

    public ProfileServiceTest() {
        // will come in automatically with Spring
        profileService.setBeanCompareService(new BeanCompareService());
        profileService.setProfileRepository(profileRepository);
        profileService.setFeatureRepository(featureRepository);
    }

    /**
     * Simulates Monte updating his own record.
     */
    @Test
    public void updatePersonWithFields() throws Exception {

        Person p = profileRepository.getPerson("ZDB-PERS-960805-676");
        assertEquals("(541) 346-4607", p.getPhone());
        List<BeanFieldUpdate> beanFieldUpdateList = new ArrayList<>();
        BeanFieldUpdate beanFieldUpdate = new BeanFieldUpdate();
        beanFieldUpdate.setField("phone");
        beanFieldUpdate.setFrom("(541) 346-4607");
        beanFieldUpdate.setTo("(541) 346-1234");
        beanFieldUpdateList.add(beanFieldUpdate);
        profileService.updateProfileWithFields(p.getZdbID(), beanFieldUpdateList, p.getZdbID());
        p = profileRepository.getPerson("ZDB-PERS-960805-676");
        assertEquals("(541) 346-1234", p.getPhone());
    }

    @Test
    public void updatePersonAddressWithUtf8Characters() throws Exception {

            Person p = profileRepository.getPerson("ZDB-PERS-960805-676");
            String oldAddress = p.getAddress();
            //This address has nasty high utf8 characters
            String newAddress = "Max Planck Institute of Neurobiology\n" +
                    "Dept. Genes - Circuits – Behavior (Group Baier) \n" +
                    "Am Klopferspitz 18 • D-82152 Martinsried\n" +
                    "phone +49 89 8578 3263 • fax +49 89 8578 3240";

            List<BeanFieldUpdate> beanFieldUpdateList = new ArrayList<>();
            BeanFieldUpdate beanFieldUpdate = new BeanFieldUpdate();
            beanFieldUpdate.setField("address");
            beanFieldUpdate.setFrom(oldAddress);
            beanFieldUpdate.setTo(newAddress);

            beanFieldUpdateList.add(beanFieldUpdate);
            profileService.updateProfileWithFields(p.getZdbID(), beanFieldUpdateList, p.getZdbID());
            HibernateUtil.currentSession().flush();
    }


    @Test
    public void setCurrentPrefix() {
        String prefix;
        // only sets one that is already there
        Lab lab1 = profileRepository.getLabById("ZDB-LAB-001018-1");
        profileService.setCurrentPrefix(lab1, "zf");
        prefix = featureRepository.getCurrentPrefixForLab("ZDB-LAB-001018-1");
        assertEquals("zf", prefix);
        profileService.setCurrentPrefix(lab1, "ae");
        prefix = featureRepository.getCurrentPrefixForLab("ZDB-LAB-001018-1");
        assertEquals("ae", prefix);
    }


    @Test
    public void setMembersToOrganizationAddress() {
        int returnCount = profileService.setMembersToOrganizationAddress("ZDB-LAB-000914-1");
        assertThat(returnCount, greaterThan(1));
        assertThat(returnCount, lessThan(40));
    }

    @Test
    public void createLab() {
        Lab lab = new Lab();
        lab.setName("Bob Jones School of Intelligent Design");
        lab = profileService.createLab(lab);
        assertNotNull(lab);
        assertEquals("Bob Jones School of Intelligent Design", lab.getName());
        assertNotNull(lab.getZdbID());
    }


    @Test
    public void createCompany() {
        Company company = new Company();
        company.setName("Bob Jones Company of Intelligent Design");
        company = profileService.createCompany(company);
        assertNotNull(company);
        assertEquals("Bob Jones Company of Intelligent Design", company.getName());
        assertNotNull(company.getZdbID());
    }


    @Test
    public void createPerson() {
        Person person = new Person();
        person.setPutativeLoginName("bobjones");
        person.setPass1("password");
        person.setPass2("password");
        person.setFirstName("Bob");
        person.setLastName("Jones");
        person = profileService.createPerson(person);
        assertNotNull(person);
        assertEquals("Jones-B.", person.getShortName());
        assertNotNull(person.getZdbID());
        assertNotNull(person.getAccountInfo());
        assertNotNull(person.getAccountInfo().getZdbID());
    }

}

