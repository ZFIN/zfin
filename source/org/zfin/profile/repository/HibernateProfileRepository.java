package org.zfin.profile.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.springframework.stereotype.Repository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.profile.*;
import org.zfin.profile.presentation.*;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Persistence storage of profile data.
 */
@Repository
public class HibernateProfileRepository implements ProfileRepository {

    private Logger logger = Logger.getLogger(HibernateProfileRepository.class);

    private ProfileService profileService = new ProfileService();

    private OrganizationLookupTransformer organizationLookupEntryTransformer = new OrganizationLookupTransformer();

    public Person getPerson(String zdbID) {
        return (Person) HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", zdbID))
                .uniqueResult();
    }

    public Organization getOrganizationByName(String name) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct o  from  Organization o" +
                "     where o.name = :name";
        Query query = session.createQuery(hql);
        query.setParameter("name", name);
        return ((Organization) query.uniqueResult());
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

        InfrastructureService.insertUpdate(marker, "Added supplier " + organization.getName());
    }

    public void removeSupplier(Organization organization, Marker marker) {
        Session session = HibernateUtil.currentSession();
        MarkerSupplier supplier = getSpecificSupplier(marker, organization);
        if (supplier != null) {
            session.delete(supplier);
            InfrastructureService.insertUpdate(marker, "Removed supplier " + organization.getName());
        }
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
        Person curator = profileService.getCurrentSecurityUser();
        if (curator == null) {
            return null;
        }

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
        if (pubZdbID != null) {
            cs.setPublication(publicationRepository.getPublication(pubZdbID));
        }
        cs.setField(field);
        cs.setValue(value);

        session.save(cs);
        return cs;
    }

    public void deleteAccountInfo(Person person) {
        AccountInfo accountInfo = person.getAccountInfo();
        if (accountInfo == null) {
            return;
        }

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
        if (currentAccountInfo == null) {
            return;
        }

        if (newAccountInfo == null) {
            return;
        }

        Session session = HibernateUtil.currentSession();
        String newName = newAccountInfo.getName();
        Person submittingPerson = profileService.getCurrentSecurityUser();
        if (StringUtils.isNotEmpty(newName) && !newName.equals(currentAccountInfo.getName())) {
            Updates update = new Updates();
            update.setFieldName("name");
            update.setNewValue(newName);
            update.setOldValue(currentAccountInfo.getName());
            update.setRecID(currentPerson.getZdbID());
            update.setSubmitter(submittingPerson);
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
            update.setSubmitter(submittingPerson);
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
            update.setSubmitter(submittingPerson);
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
            update.setSubmitter(submittingPerson);
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
        if (curationAttribute != null) {
            curationAttribute.setValue(String.valueOf(visibility));
        } else {
            Person curator = profileService.getCurrentSecurityUser();
            // ToDo: IS this the right thing to do?
            if (curator == null) {
                return;
            }

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
        if (curationAttribute != null) {
            curationAttribute.setValue(zdbID);
        } else {
            Person curator = profileService.getCurrentSecurityUser();
            // ToDo: IS this the right thing to do?
            if (curator == null) {
                return;
            }

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

    /*
    * Gets person records by fullName, "Westerfield, Monte"
    *
    * Since it requires an equals match, it will only return
    * more than one person for non-unique full names, but
    * the interface needs to handle that in some way.
    *
    * */
    public List<Person> getPeopleByFullName(String fullName) {
        List<Person> people = new ArrayList<Person>();
        Session session = HibernateUtil.currentSession();

        people.addAll((List<Person>) session.createCriteria(Person.class)
                .add(Restrictions.eq("fullName", fullName))
                .list());
        return people;
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
     *
     * @param publicationID    publication
     * @param boxDivID         div element
     * @param mutantDisplayBox attribute
     * @return curator session
     */
    @SuppressWarnings("unchecked")
    public CuratorSession getCuratorSession(String publicationID, String boxDivID, CuratorSession.Attribute mutantDisplayBox) {
        Session session = HibernateUtil.currentSession();
        Person curator = profileService.getCurrentSecurityUser();
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
        Criteria labCriteria = HibernateUtil.currentSession().createCriteria(Lab.class);
        labCriteria.add(Restrictions.ilike("name", "%" + name + "%"));

        List<Organization> labs = (List<Organization>) labCriteria.list();

        Criteria companyCriteria = HibernateUtil.currentSession().createCriteria(Company.class);
        companyCriteria.add(Restrictions.ilike("name", "%" + name + "%"));

        List<Organization> companies = (List<Organization>) companyCriteria.list();
        labs.addAll(companies);
        Collections.sort(labs);
        return labs;
    }


    @Override
    public Lab getLabById(String labZdbId) {
        return (Lab) HibernateUtil.currentSession().get(Lab.class, labZdbId);
    }

    @Override
    public List<OrganizationLink> getSupplierLinksForZdbId(String zdbID) {
        String sql = "" +
                "SELECT id.idsup_supplier_zdb_id," +
                "       su.srcurl_url," +
                "       su.srcurl_display_text," +
                "       id.idsup_acc_num," +
                "       comp.NAME AS cname," +
                "       l.NAME    AS lname " +
                "FROM   int_data_supplier id" +
                "       LEFT OUTER JOIN source_url su" +
                "                    ON id.idsup_supplier_zdb_id = su.srcurl_source_zdb_id" +
                "                       AND su.srcurl_purpose = 'order'" +
                "       LEFT OUTER JOIN company comp" +
                "                    ON comp.zdb_id = id.idsup_supplier_zdb_id" +
                "       LEFT OUTER JOIN lab l" +
                "                    ON l.zdb_id = id.idsup_supplier_zdb_id " +
                "WHERE  id.idsup_data_zdb_id =  :OID  " ;

        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("OID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        OrganizationLink organinzationLink = new OrganizationLink();
                        organinzationLink.setSupplierZdbId(tuple[0].toString());
                        if (tuple[1] != null) {
                            organinzationLink.setSourceUrl(tuple[1].toString());
                        }
                        if (tuple[2] != null) {
                            organinzationLink.setUrlDisplayText(tuple[2].toString());
                        }
                        if (tuple[3] != null) {
                            organinzationLink.setAccessionNumber(tuple[3].toString());
                        }
                        if (tuple[4] != null) {
                            organinzationLink.setCompanyName(tuple[4].toString());
                        }
                        if (tuple[5] != null) {
                            organinzationLink.setLabName(tuple[5].toString());
                        }

                        return organinzationLink;
                    }
                })
                .list()
                ;
    }


    @Override
    public Company getCompanyById(String zdbID) {
        return (Company) HibernateUtil.currentSession().get(Company.class, zdbID);
    }

    @Override
    public List<CompanyPresentation> getCompanyForPersonId(String zdbID) {
        String sql = " select b.name, " +
                "      b.zdb_id, " +
                "      a.position_id,  " +
                "      c.compos_order  " +
                "    from int_person_company a, " +
                "      company b,  " +
                "      company_position c  " +
                "    where source_id= :zdbID  " +
                "    and target_id=b.zdb_id  " +
                "    and a.position_id=c.compos_pk_id  " +
                "    order by c.compos_order desc;";
        return (List<CompanyPresentation>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("zdbID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public CompanyPresentation transformTuple(Object[] tuple, String[] aliases) {
                        CompanyPresentation companyPresentation = new CompanyPresentation();
                        companyPresentation.setName(tuple[0].toString());
                        companyPresentation.setZdbID(tuple[1].toString());
                        companyPresentation.setPosition(Integer.parseInt(tuple[2].toString()));
                        companyPresentation.setOrder(tuple[3].toString());
                        companyPresentation.setShowPosition(false);
                        return companyPresentation;
                    }
                })
                .list();
    }

    @Override
    public List<LabPresentation> getLabsForPerson(String zdbID) {
        String sql = " select b.name, " +
                "      b.zdb_id, " +
                "      a.position_id, " +
                "      c.labpos_order  " +
                "        from int_person_lab a, " +
                "      lab b,  " +
                "      lab_position c  " +
                "        where source_id= :zdbID  " +
                "        and target_id=b.zdb_id  " +
                "        and a.position_id=c.labpos_pk_id " +
                "        order by c.labpos_order desc, b.name asc; " +
                " ";
        return (List<LabPresentation>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setParameter("zdbID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public LabPresentation transformTuple(Object[] tuple, String[] aliases) {
                        LabPresentation labPresentation = new LabPresentation();
                        labPresentation.setName(tuple[0].toString());
                        labPresentation.setZdbID(tuple[1].toString());
                        if (tuple[2] != null) {
                            labPresentation.setPosition(Integer.parseInt(tuple[2].toString()));
                        }
                        labPresentation.setOrder(tuple[3].toString());
                        return labPresentation;
                    }
                })
                .list();
    }

    @Override
    public List<PersonMemberPresentation> getLabMembers(final String zdbID) {
        String sql = " select b.last_name || ', ' ||b.first_name , b.zdb_id, a.position_id, c.labpos_order, c.labpos_position " +
                " from int_person_lab a, person b, lab_position c " +
                " where source_id=b.zdb_id and target_id=:zdbID " +
                " and a.position_id = c.labpos_pk_id " +
                " order by c.labpos_order,b.last_name, b.first_name ";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("zdbID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public PersonMemberPresentation transformTuple(Object[] tuple, String[] aliases) {
                        PersonMemberPresentation personMemberPresentation = new PersonMemberPresentation();
                        personMemberPresentation.setName(tuple[0].toString());
                        personMemberPresentation.setPersonZdbID(tuple[1].toString());
                        personMemberPresentation.setPosition(Integer.parseInt(tuple[2].toString()));
                        personMemberPresentation.setOrder(Integer.parseInt(tuple[3].toString()));
                        personMemberPresentation.setPositionString(tuple[4].toString());
                        personMemberPresentation.setOrganizationZdbID(zdbID);
                        return personMemberPresentation;
                    }
                })
                .list();
    }

    @Override
    public List<Publication> getPublicationsForLab(String zdbID) {
        String hql = " select distinct pub , pub.publicationDate, lower(pub.authors) " +
                " from Person pers " +
                " join pers.publications pub " +
                " join pers.labs l " +
                " join fetch pub.journal " +
                "  where l.zdbID = :zdbID " +
                "  order by pub.publicationDate desc, lower(pub.authors) " +
                " ";

        return HibernateUtil.currentSession().createQuery(hql)
                .setString("zdbID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Publication transformTuple(Object[] tuple, String[] aliases) {
                        return (Publication) tuple[0];
                    }
                })
                .list();
    }

    @Override
    public List<PersonMemberPresentation> getCompanyMembers(final String zdbID) {
        String sql = "  select " +
                "        b.last_name || ', ' ||b.first_name , " +
                "        b.zdb_id, " +
                "        a.position_id, " +
                "        c.compos_order,   " +
                "        c.compos_position   " +
                "    from " +
                "        int_person_company a, " +
                "        person b, " +
                "        company_position c   " +
                "    where " +
                "        source_id=b.zdb_id  " +
                "        and target_id= :zdbID  " +
                "        and a.position_id = c.compos_pk_id " +
                "    order by " +
                "        c.compos_position, " +
                "        b.last_name , b.first_name ";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("zdbID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public PersonMemberPresentation transformTuple(Object[] tuple, String[] aliases) {
                        PersonMemberPresentation personMemberPresentation = new PersonMemberPresentation();
                        personMemberPresentation.setName(tuple[0].toString());
                        personMemberPresentation.setPersonZdbID(tuple[1].toString());
                        personMemberPresentation.setPosition(Integer.parseInt(tuple[2].toString()));
                        personMemberPresentation.setOrder(Integer.parseInt(tuple[3].toString()));
                        personMemberPresentation.setPositionString(tuple[4].toString());
                        personMemberPresentation.setOrganizationZdbID(zdbID);
                        return personMemberPresentation;
                    }
                })
                .list();
    }

    @Override
    public List<Publication> getPublicationsForCompany(String zdbID) {
        String hql = " select distinct pub , pub.publicationDate, lower(pub.authors) from Person pers join pers.publications pub join pers.companies c " +
                "  where c.zdbID = :zdbID " +
                "  order by pub.publicationDate desc, lower(pub.authors) " +
                " ";

        return HibernateUtil.currentSession().createQuery(hql)
                .setString("zdbID", zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Publication transformTuple(Object[] tuple, String[] aliases) {
                        return (Publication) tuple[0];
                    }
                })
                .list();
    }


    @Override
    public int removeMemberFromOrganization(String personZdbID, String organizationZdbID) {
        String sql;
        if (organizationZdbID.startsWith("ZDB-LAB")) {
            sql = " delete from int_person_lab  " +
                    " where source_id = :personZdbID and target_id = :organizationZdbID ";
        } else if (organizationZdbID.startsWith("ZDB-COMPANY")) {
            sql = " delete from int_person_company  " +
                    " where source_id = :personZdbID and target_id = :organizationZdbID ";
        } else {
            logger.error("Not a valid organization to remove member from: " + organizationZdbID);
            return 0;
        }
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("personZdbID", personZdbID.toUpperCase())
                .setString("organizationZdbID", organizationZdbID.toUpperCase())
                .executeUpdate();
    }

    @Override
    public List<PersonLookupEntry> getPersonNamesForString(String lookupString) {
        String hql = " select p FROM Person p " +
                "where " +
                "lower(p.fullName) like :lookupString " +
                "or lower(p.firstName) like :lookupString order by p.fullName asc, p.zdbID desc  ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("lookupString", lookupString.toLowerCase() + "%")
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        Person p = (Person) tuple[0];
                        PersonLookupEntry personMemberPresentation = new PersonLookupEntry();
                        personMemberPresentation.setId(p.getZdbID());
                        personMemberPresentation.setLabel(p.getFullName());
                        personMemberPresentation.setValue(p.getFullName());
                        return personMemberPresentation;
                    }
                })
                .list()
                ;
    }

    @Override
    public int addCompanyMember(String personZdbID, String organizationZdbID, Integer position) {
        String sql = "insert into int_person_company (source_id,target_id,position_id) " +
                " values (:personZdbID,:companyZdbID,:positionID)  ";
        logger.debug("personZdbID: " + personZdbID);
        logger.debug("organizationZdbID: " + organizationZdbID);
        logger.debug("positionID: " + position);
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("personZdbID", personZdbID)
                .setString("companyZdbID", organizationZdbID)
                .setInteger("positionID", position)
                .executeUpdate();
    }

    @Override
    public int addLabMember(String personZdbID, String organizationZdbID, Integer positionID) {
        String sql = "insert into int_person_lab (source_id,target_id,position_id) " +
                " values (:personZdbID,:labZdbID,:positionID)  ";
        logger.debug("person: " + personZdbID);
        logger.debug("lab: " + organizationZdbID);
        logger.debug("positionID: " + positionID);
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("personZdbID", personZdbID)
                .setString("labZdbID", organizationZdbID)
                .setInteger("positionID", positionID)
                .executeUpdate();
    }

    public int changeLabPosition(String personZdbID, String organizationZdbID, Integer positionID) {
        String sql = "update int_person_lab " +
                " set source_id = :personZdbID, target_id = :labZdbID, position_id = :positionID " +
                " where source_id = :personZdbID and target_id = :labZdbID";
        logger.debug("person: " + personZdbID);
        logger.debug("lab: " + organizationZdbID);
        logger.debug("positionID: " + positionID);
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("personZdbID", personZdbID)
                .setString("labZdbID", organizationZdbID)
                .setInteger("positionID", positionID)
                .executeUpdate();
    }

    @Override
    public List<OrganizationPosition> getLabPositions() {
        String sql = "select labpos_position,labpos_pk_id,labpos_order from lab_position order by labpos_order ";
        return HibernateUtil.currentSession()
                .createSQLQuery(sql)
                .setResultTransformer(new BasicTransformerAdapter() {
                                          @Override
                                          public Object transformTuple(Object[] tuple, String[] aliases) {
                                              OrganizationPosition organizationPosition = new OrganizationPosition();
                                              organizationPosition.setName(tuple[0].toString());
                                              organizationPosition.setId(Integer.parseInt(tuple[1].toString()));
                                              return organizationPosition;

                                          }
                                      }
                )
                .list();
    }

    @Override
    public List<OrganizationPosition> getCompanyPositions() {
        String sql = "select compos_position,compos_pk_id,compos_order from company_position order by compos_order ";
        return HibernateUtil.currentSession()
                .createSQLQuery(sql)
                .setResultTransformer(new BasicTransformerAdapter() {
                                          @Override
                                          public Object transformTuple(Object[] tuple, String[] aliases) {
                                              OrganizationPosition organizationPosition = new OrganizationPosition();
                                              organizationPosition.setName(tuple[0].toString());
                                              organizationPosition.setId(Integer.parseInt(tuple[1].toString()));
                                              return organizationPosition;

                                          }
                                      }
                ).list();
    }

    public int changeCompanyPosition(String personZdbID, String organizationZdbID, Integer positionID) {
        String sql = "update int_person_company " +
                " set source_id = :personZdbID, target_id = :companyZdbID, position_id = :positionID " +
                " where source_id = :personZdbID and target_id = :companyZdbID";
        logger.debug("person: " + personZdbID);
        logger.debug("company: " + organizationZdbID);
        logger.debug("positionID: " + positionID);
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("personZdbID", personZdbID)
                .setString("companyZdbID", organizationZdbID)
                .setInteger("positionID", positionID)
                .executeUpdate();
    }

    @Override
    public int removeLabMember(String personZdbID, String organizationZdbID) {
        String sql = "delete from int_person_lab where source_id = :personZdbID and target_id = :organizationID ";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("personZdbID", personZdbID)
                .setString("organizationID", organizationZdbID)
                .executeUpdate();
    }

    @Override
    public int removeCompanyMember(String personZdbID, String organizationZdbID) {
        String sql = "delete from int_person_company where source_id = :personZdbID and target_id = :organizationID ";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("personZdbID", personZdbID)
                .setString("organizationID", organizationZdbID)
                .executeUpdate();
    }

    @Override
    public Organization getOrganizationByZdbID(String orgZdbID) {
        return (Organization) HibernateUtil.currentSession().createCriteria(Organization.class)
                .add(Restrictions.eq("zdbID", orgZdbID))
                .uniqueResult();
    }

    @Override
    public List<Lab> getLabs() {
        return HibernateUtil.currentSession().createCriteria(Lab.class).addOrder(Order.asc("name")).list();
    }

    @Override
    public List<Company> getCompanies() {
        return HibernateUtil.currentSession().createCriteria(Company.class).addOrder(Order.asc("name")).list();
    }


    @Override
    public PaginationResult<Company> searchCompanies(CompanySearchBean searchBean) {

        Criteria criteria = HibernateUtil.currentSession()
                .createCriteria(Company.class).addOrder(Order.asc("name"));
        if (StringUtils.isNotEmpty(searchBean.getName())) {
            criteria = addTokenizedLikeRestriction("name", searchBean.getName(), criteria);
        }
        if (StringUtils.isNotEmpty(searchBean.getAddress())) {
            criteria = addTokenizedLikeRestriction("address", searchBean.getAddress(), criteria);
        }

        if (StringUtils.isNotEmpty(searchBean.getContains())) {
            String containsType = searchBean.getContainsType();
            if (containsType.equals("bio")) {
                criteria = addTokenizedLikeRestriction("bio", searchBean.getContains(), criteria);
            } else if (containsType.equals("email")) {
                criteria.add(Restrictions.ilike("email", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("url")) {
                criteria.add(Restrictions.ilike("url", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("fax")) {
                criteria.add(Restrictions.ilike("fax", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("phone")) {
                criteria.add(Restrictions.ilike("phone", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("zdb_id")) {
                criteria.add(Restrictions.ilike("zdbID", "%" + searchBean.getContains() + "%"));
            }
        }

//        return criteria.list();
        PaginationResult<Company> paginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(
                searchBean.getFirstRecordOnPage() - 1, searchBean.getLastRecordOnPage(), criteria.scroll());
        paginationResult.setStart(searchBean.getFirstRecord());

        return paginationResult;
    }

    @Override
    public PaginationResult<Lab> searchLabs(LabSearchBean searchBean) {

        Criteria criteria = HibernateUtil.currentSession()
                .createCriteria(Lab.class).addOrder(Order.asc("name"));
        if (StringUtils.isNotEmpty(searchBean.getName())) {
            criteria = addTokenizedLikeRestriction("name", searchBean.getName(), criteria);
        }
        if (StringUtils.isNotEmpty(searchBean.getAddress())) {
            criteria = addTokenizedLikeRestriction("address", searchBean.getAddress(), criteria);
        }

        if (StringUtils.isNotEmpty(searchBean.getContains())) {
            String containsType = searchBean.getContainsType();
            if (containsType.equals("bio")) {

                criteria = addTokenizedLikeRestriction("bio", searchBean.getContains(), criteria);

            } else if (containsType.equals("email")) {
                criteria.add(Restrictions.ilike("email", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("url")) {
                criteria.add(Restrictions.ilike("url", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("fax")) {
                criteria.add(Restrictions.ilike("fax", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("phone")) {
                criteria.add(Restrictions.ilike("phone", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("zdb_id")) {
                criteria.add(Restrictions.ilike("zdbID", "%" + searchBean.getContains() + "%"));
            }
        }

        PaginationResult<Lab> paginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(
                searchBean.getFirstRecordOnPage() - 1, searchBean.getLastRecordOnPage(), criteria.scroll());
        paginationResult.setStart(searchBean.getFirstRecord());

        return paginationResult;
    }

    @Override
    public List<Person> getPersonByLastNameStartsWith(String lastNameStartsWith) {
        if (lastNameStartsWith == null) {
            return HibernateUtil.currentSession().createCriteria(Person.class)
                    .add(Restrictions.isNull("lastName"))
                    .addOrder(Order.asc("zdbID"))
                    .list();
        }
        return HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.ilike("lastName", lastNameStartsWith + "%"))
                .addOrder(Order.asc("lastName"))
                .addOrder(Order.asc("firstName"))
//                .addOrder(Order.asc("middleNameOrInitial"))
                .list();
    }

    @Override
    public List<Person> getPersonByLastNameStartsWithAndFirstNameStartsWith(String lastNameStartsWith, String firstNameStartsWith) {

        return HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.ilike("lastName", lastNameStartsWith + "%"))
                .add(Restrictions.ilike("firstName", firstNameStartsWith + "%"))
                .addOrder(Order.asc("lastName"))
                .addOrder(Order.asc("firstName"))
                .list();
    }

    @Override
    public List<Person> getPersonByLastNameEqualsAndFirstNameStartsWith(String lastName, String firstNameStartsWith) {

        return HibernateUtil.currentSession().createCriteria(Person.class)
                .add(Restrictions.eq("lastName", lastName))
                .add(Restrictions.ilike("firstName", firstNameStartsWith + "%"))
                .addOrder(Order.asc("lastName"))
                .addOrder(Order.asc("firstName"))
                .list();
    }


    @Override
    public boolean isOrganizationPersonExist(String personZdbID, String organizationZdbID) {
        return HibernateUtil.currentSession().createSQLQuery("select ipc.source_id, ipc.target_id from int_person_company ipc " +
                "where ipc.source_id=:personZdbID and ipc.target_id= :organizationZdbID " +
                "union  " +
                "select ipl.source_id, ipl.target_id from int_person_lab ipl " +
                "where ipl.source_id=:personZdbID  and ipl.target_id= :organizationZdbID  ")
                .setString("personZdbID", personZdbID)
                .setString("organizationZdbID", organizationZdbID)
                .list()
                .size() > 0
                ;
    }

    @Override
    public Address getAddress(Long addressId) {
        return (Address) HibernateUtil.currentSession().get(Address.class, addressId);
    }

    @Override
    public PaginationResult<Person> searchPeople(PersonSearchBean searchBean) {

        Criteria criteria = HibernateUtil.currentSession()
                .createCriteria(Person.class)
                .addOrder(Order.asc("lastName"))
                .addOrder(Order.asc("firstName"));
        if (StringUtils.isNotEmpty(searchBean.getName())) {
            criteria = addTokenizedLikeRestriction("fullName", searchBean.getName(), criteria);
        }
        if (StringUtils.isNotEmpty(searchBean.getAddress())) {
            criteria = addTokenizedLikeRestriction("address", searchBean.getAddress(), criteria);
        }

        if (StringUtils.isNotEmpty(searchBean.getContains())) {
            String containsType = searchBean.getContainsType();
            if (containsType.equals("bio")) {
                criteria = addTokenizedLikeRestriction("personalBio", searchBean.getContains(), criteria);
            } else if (containsType.equals("email")) {
                criteria = addTokenizedLikeRestriction("email", searchBean.getContains(), criteria);
            } else if (containsType.equals("url")) {
                criteria.add(Restrictions.ilike("url", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("fax")) {
                criteria.add(Restrictions.ilike("fax", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("phone")) {
                criteria.add(Restrictions.ilike("phone", "%" + searchBean.getContains() + "%"));
            } else if (containsType.equals("zdb_id")) {
                criteria.add(Restrictions.ilike("zdbID", "%" + searchBean.getContains() + "%"));
            }
        }
//        return criteria.list();
        PaginationResult<Person> paginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(
                searchBean.getFirstRecordOnPage() - 1, searchBean.getLastRecordOnPage(), criteria.scroll());
        paginationResult.setStart(searchBean.getFirstRecord());

        return paginationResult;
    }


    /**
     * Doing multi-word searching is something that's useful in all of these searches
     * <p>
     * Returning the criteria doesn't really do anything, but it feels like it's a little
     * more immediately obvious what's going on if I do it that way.
     *
     * @param fieldName
     * @param queryString
     * @param criteria
     * @return
     */
    private Criteria addTokenizedLikeRestriction(String fieldName, String queryString, Criteria criteria) {
        if (fieldName != null && queryString != null && criteria != null) {
            for (String queryTerm : Arrays.asList(queryString.split(" "))) {
                criteria.add(Restrictions.ilike(fieldName, "%" + queryTerm + "%"));
            }
        }

        return criteria;
    }

    public List<String> getSuppliedDataIds(Organization organization) {
        return HibernateUtil.currentSession().createSQLQuery("select idsup_data_zdb_id from int_data_supplier " +
                "where idsup_supplier_zdb_id = :supplierId order by idsup_data_zdb_id ")
                .setString("supplierId", organization.getZdbID())
                .list();
    }

    public List<String> getSourcedDataIds(Organization organization) {
        return HibernateUtil.currentSession().createSQLQuery("select ids_data_zdb_id from int_data_source " +
                "where ids_source_zdb_id = :sourId and ids_data_zdb_id[5,8] not in (:exclusion1,:exclusion2) order by ids_data_zdb_id ")
                .setString("sourId", organization.getZdbID())
                .setString("exclusion1", "XPAT")
                .setString("exclusion2", "GENO")
                .list();
    }

    public List<String> getDistributionList() {
        return HibernateUtil.currentSession()
                .createSQLQuery("select distinct email from person " +
                        "where on_dist_list='t' " +
                        "and email is not null " +
                        "and email != '';")
                .list();
    }

    public List<String> getPiDistributionList() {
        return HibernateUtil.currentSession()
                .createSQLQuery("select distinct email from person, int_person_lab, lab_position " +
                        "where zdb_id = source_id " +
                        "and position_id = labpos_pk_id " +
                        "and labpos_position in ('PI/Director', 'Co-PI/Senior Scientist') " +
                        "and email is not null " +
                        "and email != '' " +
                        "and on_dist_list='t'")
                .list();
    }

    public List<String> getUsaDistributionList() {
        return HibernateUtil.currentSession()
                .createSQLQuery("select distinct email from person " +
                        "where on_dist_list='t' " +
                        "and upper(address) like '%USA%' " +
                        "and email is not null " +
                        "and email != '';")
                .list();
    }

    public List<Person> getCurators() {
        return HibernateUtil.currentSession()
                .createCriteria(Person.class)
                .add(Restrictions.eq("accountInfo.curator", true))
                .list();
    }

    public boolean personHasSnapshot(Person person) {
        return HibernateUtil.currentSession()
                .createCriteria(Person.class)
                .add(Restrictions.eq("zdbID", person.getZdbID()))
                .add(Restrictions.isNotNull("snapshot"))
                .uniqueResult() != null;
    }
}
