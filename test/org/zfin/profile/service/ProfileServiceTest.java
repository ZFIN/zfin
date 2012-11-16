package org.zfin.profile.service;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.*;
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
    public void updatePersonWithFields() {

        HibernateUtil.createTransaction();


        try {
            Person p = profileRepository.getPerson("ZDB-PERS-960805-676");
            assertEquals("(541) 346-4607", p.getPhone());
            List<BeanFieldUpdate> beanFieldUpdateList = new ArrayList<BeanFieldUpdate>();
            BeanFieldUpdate beanFieldUpdate = new BeanFieldUpdate();
            beanFieldUpdate.setField("phone");
            beanFieldUpdate.setFieldType(String.class);
            beanFieldUpdate.setFrom("(541) 346-4607");
            beanFieldUpdate.setTo("(541) 346-1234");
            beanFieldUpdateList.add(beanFieldUpdate);
            profileService.updateProfileWithFields(p.getZdbID(), beanFieldUpdateList, p.getZdbID());
            p = profileRepository.getPerson("ZDB-PERS-960805-676");
            assertEquals("(541) 346-1234", p.getPhone());
        } catch (Exception e) {
            logger.error(e);
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @Test
    public void setCurrentPrefix() {
        String prefix;
        try {
            HibernateUtil.createTransaction();
            // only sets one that is already there
            Lab lab1 = profileRepository.getLabById("ZDB-LAB-001018-1");
            profileService.setCurrentPrefix(lab1, "zf");
            prefix = featureRepository.getCurrentPrefixForLab("ZDB-LAB-001018-1");
            assertEquals("zf", prefix);
            profileService.setCurrentPrefix(lab1, "ae");
            prefix = featureRepository.getCurrentPrefixForLab("ZDB-LAB-001018-1");
            assertEquals("ae", prefix);
        } catch (Exception e) {
            logger.error(e.fillInStackTrace().toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @Test
    public void setMembersToOrganizationAddress() {
        HibernateUtil.createTransaction();
        try {
            int returnCount = profileService.setMembersToOrganizationAddress("ZDB-LAB-000914-1");
            assertThat(returnCount, greaterThan(1));
            assertThat(returnCount, lessThan(40));
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void createLab() {
        HibernateUtil.createTransaction();
        try {
            Lab lab = new Lab();
            lab.setName("Bob Jones School of Intelligent Design");
            lab = profileService.createLab(lab);
            assertNotNull(lab);
            assertEquals("Bob Jones School of Intelligent Design", lab.getName());
            assertNotNull(lab.getZdbID());
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @Test
    public void createCompany() {
        HibernateUtil.createTransaction();
        try {
            Company company = new Company();
            company.setName("Bob Jones Company of Intelligent Design");
            company = profileService.createCompany(company);
            assertNotNull(company);
            assertEquals("Bob Jones Company of Intelligent Design", company.getName());
            assertNotNull(company.getZdbID());
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @Test
    public void createPerson() {
        HibernateUtil.createTransaction();
        try {
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
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

}

