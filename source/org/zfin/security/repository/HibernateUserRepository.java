package org.zfin.security.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;

import java.util.List;

/**
 * Hibernate implementation of the UserRepository.
 */
@Repository
public class HibernateUserRepository implements UserRepository {

    public Person getPersonByLoginName(String username) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select person from Person person, AccountInfo accountInfo
            where accountInfo in elements(person.accountInfoList)
             AND accountInfo.login = :login
            """;
        Query<Person> query1 = session.createQuery(hql, Person.class);
        query1.setParameter("login", username);
        List<Person> list = query1.list();
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        Person person = list.get(0);
        person.getAccountInfo();
        return person;
    }

}
