package org.zfin.people.repository;

import org.zfin.people.Person;
import org.zfin.people.Lab;
import org.zfin.people.User;
import org.zfin.framework.HibernateUtil;
import org.hibernate.Session;

/**
 * Persistence storage of profile data.
 */
public class HibernateProfileRepository implements ProfileRepository {

    public Person getPerson(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Person person = (Person) session.get(Person.class, zdbID);
        return person;
    }

    public Person getPerson(Person pers) {
        return null;
    }

    public void insertPerson(Person person) {
        Session session = HibernateUtil.currentSession();
        session.getTransaction().begin();
        session.save(person);
        session.getTransaction().commit();
    }

    public void insertLab(Lab lab) {
        Session session = HibernateUtil.currentSession();
        session.getTransaction().begin();
        session.save(lab);
        session.getTransaction().commit();
    }

    public User getUser(String zdbID) {
        Session session = HibernateUtil.currentSession();
        User user = (User) session.get(User.class, zdbID);
        return user;
    }
}
