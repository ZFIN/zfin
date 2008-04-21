package org.zfin.people.repository;

import org.zfin.people.Person;
import org.zfin.people.Lab;
import org.zfin.people.User;
import org.zfin.people.CuratorSession;
import org.zfin.framework.HibernateUtil;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Example;

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

 
    public CuratorSession getCuratorSession(String curatorZdbID, String pubZdbID, String field) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(org.zfin.people.CuratorSession.class);

        criteria.add(Restrictions.eq("curator.zdbID", curatorZdbID));

        //publication field is nullable
        if (pubZdbID != null) {
            criteria.add(Restrictions.eq("publication.zdbID", pubZdbID));
        }

        criteria.add(Restrictions.eq("field", field));
        return (CuratorSession)criteria.uniqueResult();
    }
    public CuratorSession getCuratorSession(CuratorSession curatorSession) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CuratorSession createCuratorSession(String curatorZdbID, String pubZdbID, String field, String value) {
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Session session = HibernateUtil.currentSession();

        CuratorSession cs = new CuratorSession();

        cs.setCurator(getPerson(curatorZdbID));
        if (pubZdbID != null)
             cs.setPublication(publicationRepository.getPublication(pubZdbID));
        cs.setField(field);
        cs.setValue(value);

        session.save(cs);
        return cs;
    }
}
