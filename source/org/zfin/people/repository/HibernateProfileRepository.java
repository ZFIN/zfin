package org.zfin.people.repository;

import org.zfin.people.*;
import org.zfin.framework.HibernateUtil;
import static org.zfin.framework.HibernateUtil.currentSession;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.marker.Marker;
import org.zfin.infrastructure.Updates;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Collections;
import java.util.Date;

/**
 * Persistence storage of profile data.
 */
public class HibernateProfileRepository implements ProfileRepository {

    public Person getPerson(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Person person = (Person) session.get(Person.class, zdbID);
        return person;
    }

    public MarkerSupplier getSupplier(String zdbID) {
        Session session = HibernateUtil.currentSession();
        MarkerSupplier supplier = (MarkerSupplier) session.get(MarkerSupplier.class, zdbID);
        return supplier;
    }


    public Organization getOrganizationByID(String zdbID) {
        Session session = currentSession();
        return (Organization) session.get(Organization.class, zdbID);
    }

    public void deleteSupplier(MarkerSupplier supplier) {
        Session session = currentSession();
        session.delete(supplier);


    }

    public Organization getOrganizationByName(String name) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Organization.class);
        criteria.add(Restrictions.eq("name", name));
        return (Organization) criteria.uniqueResult();
    }

    public Person getPerson(Person pers) {
        return null;
    }

    public void insertPerson(Person person) {
        Session session = HibernateUtil.currentSession();
        session.save(person);
    }

    public void addSupplier(Organization organization, Marker marker) {
        Session session = HibernateUtil.currentSession();
        MarkerSupplier supplier = new MarkerSupplier();
        supplier.setOrganization(organization);
        supplier.setMarker(marker);
        session.save(supplier);

    }

    public void insertLab(Lab lab) {
        Session session = HibernateUtil.currentSession();
        session.save(lab);
    }

    public MarkerSupplier getSpecificSupplier(Marker marker, Organization organization) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(MarkerSupplier.class);
        criteria.add(Restrictions.eq("marker", marker));
        criteria.add(Restrictions.eq("organization", organization));
        return (MarkerSupplier) criteria.uniqueResult();
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
        return (CuratorSession) criteria.uniqueResult();
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

    public void deleteAccountInfo(Person person) {
        AccountInfo accountInfo  = person.getAccountInfo();
        if (accountInfo == null)
            return;

        Session session = HibernateUtil.currentSession();
        AccountInfo info = (AccountInfo) session.get(AccountInfo.class, person.getZdbID());
        session.delete(info);
    }

    public boolean userExists(String login) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(Person.class);
        crit.add(Restrictions.eq("accountInfo.login", login));
        Person person = (Person) crit.uniqueResult();
        return person != null;
    }

    public void updateAccountInfo(Person currentPerson, AccountInfo newAccountInfo){
        AccountInfo currentAccountInfo = currentPerson.getAccountInfo();
        if (currentAccountInfo == null)
            return;

        if (newAccountInfo == null)
            return;

        Session session = HibernateUtil.currentSession();
        String newName = newAccountInfo.getName();
        Person submittingPerson = Person.getCurrentSecurityUser();
        if (StringUtils.isNotEmpty(newName) && !newName.equals(currentAccountInfo.getName())) {
            Updates update = new Updates();
            update.setFieldName("name");
            update.setNewValue(newName);
            update.setOldValue(currentAccountInfo.getName());
            update.setRecID(currentPerson.getZdbID());
            update.setSubmitterID(submittingPerson.getZdbID());
            update.setSubmitterName(submittingPerson.getUsername());
            update.setWhenUpdated(new Date());
            session.save(update);
            currentAccountInfo.setName(newName);
        }
        // since the password has to be typed in every time there is no concept
        // of changing a password. So we mark it as changed...
        {
            Updates update = new Updates();
            update.setFieldName("password");
            update.setRecID(currentPerson.getZdbID());
            update.setSubmitterID(submittingPerson.getZdbID());
            update.setSubmitterName(submittingPerson.getUsername());
            update.setWhenUpdated(new Date());
            session.save(update);
        }
        String role = newAccountInfo.getRole();
        if (StringUtils.isNotEmpty(role) && !role.equals(currentAccountInfo.getRole())) {
            Updates update = new Updates();
            update.setFieldName("role");
            update.setRecID(currentPerson.getZdbID());
            update.setNewValue(role);
            update.setOldValue(currentAccountInfo.getRole());
            update.setSubmitterID(submittingPerson.getZdbID());
            update.setSubmitterName(submittingPerson.getUsername());
            update.setWhenUpdated(new Date());
            currentAccountInfo.setRole(role);
            session.save(update);
        }
        String login = newAccountInfo.getLogin();
        if (StringUtils.isNotEmpty(login) && !login.equals(currentAccountInfo.getLogin())) {
            Updates update = new Updates();
            update.setFieldName("login");
            update.setRecID(currentPerson.getZdbID());
            update.setNewValue(login);
            update.setOldValue(currentAccountInfo.getLogin());
            update.setSubmitterID(submittingPerson.getZdbID());
            update.setSubmitterName(submittingPerson.getUsername());
            update.setWhenUpdated(new Date());
            currentAccountInfo.setLogin(login);
            session.save(update);
        }
        session.update(currentPerson);
    }

    @SuppressWarnings("unchecked")
    public List<Organization> getOrganizationsByName(String name) {
        Session session = HibernateUtil.currentSession();
        Criteria labCriteria = session.createCriteria(Lab.class);
        labCriteria.add(Restrictions.ilike("name", "%" + name + "%"));

        List<Organization> labs = (List<Organization>) labCriteria.list();

        Criteria companyCriteria = session.createCriteria(Company.class);
        companyCriteria.add(Restrictions.ilike("name", "%" + name + "%"));

        List<Organization> companies = (List<Organization>) companyCriteria.list();
        labs.addAll(companies);
        Collections.sort(labs);
        return labs;
    }
}
