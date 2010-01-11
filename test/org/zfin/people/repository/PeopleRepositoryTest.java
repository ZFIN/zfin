package org.zfin.people.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.CuratorSession;
import org.zfin.people.AccountInfo;
import org.zfin.people.Organization;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Class PeopleRepositoryTest.
 */

public class PeopleRepositoryTest {
    private static String REAL_PERSON_1_ZDB_ID = "ZDB-PERS-960805-676"; //Monte;
    private static String REAL_PERSON_2_ZDB_ID = "ZDB-PERS-970321-3"; //George S.

    private static UserRepository userRepository = RepositoryFactory.getUserRepository();
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }


    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    @Test
    public void createAndUpdateCuratorSession() {

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Person person = profileRepository.getPerson(REAL_PERSON_1_ZDB_ID);
            Person person2 = profileRepository.getPerson(REAL_PERSON_2_ZDB_ID);
            Publication pub = person.getPublications().iterator().next();

            String field = "This is my field";
            String value = "This is my value";
            String value2 = "This is my other value";

            CuratorSession cs1 = profileRepository.createCuratorSession(person.getZdbID(), pub.getZdbID(), field, value);
            CuratorSession cs2 = profileRepository.createCuratorSession(person2.getZdbID(), pub.getZdbID(), field, value);

            CuratorSession databaseCS = profileRepository.getCuratorSession(person.getZdbID(), pub.getZdbID(), field);

            assertNotNull("curator session created successfully", databaseCS);
            assertNotNull("curator session created with PK id", databaseCS.getID());
            assertEquals("curator session value is correct", databaseCS.getValue(), value);

        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }

    }

    @Test
    public void createAndUpdateCuratorSessionWithNoPublication() {

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Person person = profileRepository.getPerson(REAL_PERSON_1_ZDB_ID);
            String field = "This is my field";
            String value = "This is my value";

            profileRepository.createCuratorSession(REAL_PERSON_1_ZDB_ID, null, field, value);

            CuratorSession databaseCS = profileRepository.getCuratorSession(person.getZdbID(), null, field);

            assertNotNull("curator session created successfully", databaseCS);
            assertNotNull("curator session created with PK id", databaseCS.getID());
            assertEquals("curator session value is correct", databaseCS.getValue(), value);

        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }

    }

    @Test
    /**
     * Test that creation of a new person object including a user object
     * creates a single PK for both of them. User is a value object and is
     * tied to the Person object: one-to-one relationhip.
     */
    public void createPersonWithAccountInfo() {
        Person person = getTestPerson();

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(person);

            String personID = person.getZdbID();
            assertTrue("PK created", personID != null && personID.startsWith("ZDB-PERS"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tx.rollback();
        }
    }

    /**
     * Test that creation of a new person object without a user object works without
     * creating a user object in the database.
     */
    @Test
    public void createPersonOnly() {
        Person person = getTestPerson();
        person.setAccountInfo(null);
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(person);

            String personID = person.getZdbID();
            assertTrue("PK created", personID != null && personID.startsWith("ZDB-PERS"));
            assertTrue("No user object created", person.getAccountInfo() == null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void retrievePersonAndAccountInfo() {
        // monte
        String zdbID = "ZDB-PERS-960805-676";
        Person person = profileRepository.getPerson(zdbID);
        assertTrue(person != null);
        assertTrue(person.getAccountInfo() != null);
    }

    private Person getTestPerson() {
        Person person = new Person();
        person.setName("Test Person");
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
        ProfileRepository pr = RepositoryFactory.getProfileRepository();
        List<Organization> orgs = pr.getOrganizationsByName(name);
        assertTrue(orgs != null);

    }
}
