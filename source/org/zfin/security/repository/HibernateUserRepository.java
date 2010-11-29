package org.zfin.security.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.security.access.annotation.Secured;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.Person;
import org.zfin.security.ApgCookieHandler;

/**
 * Hibernate implementation of the UserRepository.
 */
public class HibernateUserRepository implements UserRepository {

    public Person getPersonByLoginName(String username) {
        Session session = HibernateUtil.currentSession();
        String hql = " from Person person where person.accountInfo.login = :login ";
        Query query = session.createQuery(hql);
        query.setString("login", username);
        return (Person) query.uniqueResult();
    }

    @Secured({"root"})
    public void createPerson(Person tempPerson) {
        Session session = HibernateUtil.currentSession();
        session.save(tempPerson);
    }

    public void backupAPGCookie(String sessionID) {
        Session session = HibernateUtil.currentSession();
        String hql = " from Person person where person.accountInfo.cookie = :cookie ";
        Query query = session.createQuery(hql);
        query.setString("cookie", ApgCookieHandler.convertTomcatCookieToApgCookie(sessionID));
        Person person = (Person) query.uniqueResult();
        if(person != null){
            person.getAccountInfo().setAuthenticatedCookie(person.getAccountInfo().getCookie());
            // have to set the cookie to a unique value (not null), so I chose to use the person id.
            person.getAccountInfo().setCookie(person.getZdbID());
        }
    }

    public Person restoreAPGCookie(String sessionID) {
        Session session = HibernateUtil.currentSession();
        String hql = " from Person person where person.accountInfo.authenticatedCookie = :cookie ";
        Query query = session.createQuery(hql);
        query.setString("cookie", ApgCookieHandler.convertTomcatCookieToApgCookie(sessionID));
        Person person = (Person) query.uniqueResult();
        if(person != null){
            person.getAccountInfo().setCookie(person.getAccountInfo().getAuthenticatedCookie());
            return person ;
        }
        return null ;
    }
}
