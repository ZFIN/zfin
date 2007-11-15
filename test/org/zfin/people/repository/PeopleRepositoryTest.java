package org.zfin.people.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.Person;
import org.zfin.people.User;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;

import java.util.Date;

/**
 * Class PeopleRepositoryTest.
 */

public class PeopleRepositoryTest {

    private static UserRepository userRepository = RepositoryFactory.getUserRepository();

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
    /**
     * Test that creation of a new person object including a user object
     * creates a single PK for both of them. User is a value object and is
     * tied to the Person object: one-to-one relationhip.
     */
    public void createPersonWithUser() {
        Person person = getTestPerson();

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(person);

            String personID = person.getZdbID();
            assertTrue("PK created", personID != null && personID.startsWith("ZDB-PERS"));
            String userID = person.getUser().getZdbID();
            assertEquals("Perseon and User have the same primary key", personID, userID);

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
    public void createPersonOnly(){
        Person person = getTestPerson();
        person.setUser(null);
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(person);

            String personID = person.getZdbID();
            assertTrue("PK created", personID != null && personID.startsWith("ZDB-PERS"));
            assertTrue("No user object created", person.getUser() == null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tx.rollback();
        }
    }

    private Person getTestPerson() {
        Person person = new Person();
        person.setName("Test Person");
        person.setEmail("Email Address Test");
        User user = new User();
        user.setLogin("newUser");
        user.setRole("root");
        user.setName("Test Person");
        user.setLoginDate(new Date());
        user.setAccountCreationDate(new Date());
        person.setUser(user);
        user.setPerson(person);
        return person;
    }
}
