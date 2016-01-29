package org.zfin.security.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;

/**
 * Hibernate implementation of the UserRepository.
 */
@Repository
public class HibernateUserRepository implements UserRepository {

    public Person getPersonByLoginName(String username) {
        Session session = HibernateUtil.currentSession();
        String hql = " from Person person where person.accountInfo.login = :login ";
        Query query = session.createQuery(hql);
        query.setString("login", username);
        return (Person) query.uniqueResult();
    }

}
