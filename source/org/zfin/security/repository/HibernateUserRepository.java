package org.zfin.security.repository;

import org.acegisecurity.annotation.Secured;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.ZfinSession;
import org.zfin.people.Person;

import java.util.Date;
import java.util.List;

/**
 * Hibernate implementation of the UserRepository.
 */
public class HibernateUserRepository implements UserRepository {

    public Person getPersonByLoginName(String username) {
        Session session = HibernateUtil.currentSession();
        String hql = " from Person person where person.user.login = :login ";
        Query query = session.createQuery(hql);
        query.setString("login", username);
        return (Person) query.uniqueResult();
    }

    @Secured({"root"})
    public void createPerson(Person tempPerson) {
        Session session = HibernateUtil.currentSession();
        session.save(tempPerson);
    }

    public void createSession(ZfinSession zfinSession) {
        Session session = HibernateUtil.currentSession();
        zfinSession.setDateCreated(new Date());
        zfinSession.setStatus("active");
        session.save(zfinSession);
    }

    public ZfinSession getSession(String sessionID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(ZfinSession.class);
        query.add(Restrictions.eq("sessionID", sessionID));
        return (ZfinSession) query.uniqueResult();
    }

    public void updateSession(ZfinSession zfinSession) {
        Session session = HibernateUtil.currentSession();
        if (session.getTransaction().wasCommitted()) {
            session.getTransaction().begin();
            session.save(zfinSession);
            session.getTransaction().commit();
        } else
            session.save(zfinSession);
    }

    public List<ZfinSession> getActiveSessions() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ZfinSession.class);
        criteria.add(Restrictions.eq("status", "active"));
        criteria.addOrder(Order.asc("dateCreated"));
        return (List<ZfinSession>) criteria.list();
    }
}
