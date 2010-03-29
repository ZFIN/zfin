package org.zfin.people.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.CuratorSession;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.people.*;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

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

    public int removeSource(String supplierZdbID, String dataZdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from MarkerSupplier sup where sup.org =:supplierZdbID and sup.dataZdbID=:dataZdbID");
        query.setParameter("supplierZdbID", supplierZdbID);
        query.setParameter("dataZdbID", dataZdbID);
        return query.executeUpdate();
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
        Criteria criteria = session.createCriteria(CuratorSession.class);
        criteria.add(Restrictions.eq("curator.zdbID", curatorZdbID));

        //publication field is nullable
        if (pubZdbID != null) {
            criteria.add(Restrictions.eq("publication.zdbID", pubZdbID));
        }

        criteria.add(Restrictions.eq("field", field));
        return (CuratorSession) criteria.uniqueResult();
    }

    public CuratorSession getCuratorSession(String pubID, CuratorSession.Attribute field) {
        Person curator = Person.getCurrentSecurityUser();
        if (curator == null)
            return null;

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(CuratorSession.class);
        criteria.add(Restrictions.eq("curator.zdbID", curator.getZdbID()));
        criteria.add(Restrictions.eq("publication.zdbID", pubID));
        criteria.add(Restrictions.eq("field", field.toString()));
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
        AccountInfo accountInfo = person.getAccountInfo();
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

    public void updateAccountInfo(Person currentPerson, AccountInfo newAccountInfo) {
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

    /**
     * Persist curator session info
     *
     * @param pubID       pub ID
     * @param showSection attribute name
     * @param visibility  attribute value
     */
    public void setCuratorSession(String pubID, CuratorSession.Attribute showSection, boolean visibility) {
        Session session = HibernateUtil.currentSession();
        CuratorSession curationAttribute = getCuratorSession(pubID, showSection);
        if (curationAttribute != null)
            curationAttribute.setValue(String.valueOf(visibility));
        else {
            Person curator = Person.getCurrentSecurityUser();
            // ToDo: IS this the right thing to do?
            if (curator == null)
                return;

            Publication pub = RepositoryFactory.getPublicationRepository().getPublication(pubID);
            curationAttribute = new CuratorSession();
            curationAttribute.setPublication(pub);
            curationAttribute.setCurator(curator);
            curationAttribute.setField(showSection.toString());
            curationAttribute.setValue(String.valueOf(visibility));
            session.save(curationAttribute);
        }
    }

    /**
     * Persist curation attribute.
     *
     * @param publicationID pub ID
     * @param attributeName attribute name
     * @param zdbID         zdbID
     */
    public void setCuratorSession(String publicationID, CuratorSession.Attribute attributeName, String zdbID) {
        Session session = HibernateUtil.currentSession();
        CuratorSession curationAttribute = getCuratorSession(publicationID, attributeName);
        if (curationAttribute != null)
            curationAttribute.setValue(zdbID);
        else {
            Person curator = Person.getCurrentSecurityUser();
            // ToDo: IS this the right thing to do?
            if (curator == null)
                return;

            Publication pub = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
            curationAttribute = new CuratorSession();
            curationAttribute.setPublication(pub);
            curationAttribute.setCurator(curator);
            curationAttribute.setField(attributeName.toString());
            curationAttribute.setValue(zdbID);
            session.save(curationAttribute);
        }
    }

    /**
     * Retrieve a person record by login name.
     *
     * @param login login
     * @return person
     */
    @SuppressWarnings("unchecked")
    public Person getPersonByName(String login) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Person.class);
        criteria.add(Restrictions.eq("accountInfo.login", login));
        return (Person) criteria.uniqueResult();
    }

    /**
     * Delete a curator session element.
     *
     * @param curatorSession CuratorSession
     */
    public void deleteCuratorSession(CuratorSession curatorSession) {
        Session session = HibernateUtil.currentSession();
        session.delete(curatorSession);
    }

    /**
     * Retrieve curator session.
     * @param publicationID publication
     * @param boxDivID                 div element
     * @param mutantDisplayBox                    attribute
     * @return curator session
     */
    @SuppressWarnings("unchecked")
    public CuratorSession getCuratorSession(String publicationID, String boxDivID, CuratorSession.Attribute mutantDisplayBox) {
        Session session = HibernateUtil.currentSession();
        Person curator = Person.getCurrentSecurityUser();
        String hql = "from CuratorSession where publication.zdbID = :publicationID " +
                "AND field = :fieldName AND curator = :person";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);
        query.setParameter("fieldName", boxDivID);
        query.setParameter("person", curator);
        return (CuratorSession) query.uniqueResult();
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
