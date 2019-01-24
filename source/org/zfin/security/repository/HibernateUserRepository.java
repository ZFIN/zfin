package org.zfin.security.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
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
        String hql = "select person from Person person left join fetch person.labs " +
                "where person.accountInfo.login = :login ";
        Query<Person> query = session.createQuery(hql, Person.class);
        query.setParameter("login", username);
        return query.getSingleResult();
    }

}
