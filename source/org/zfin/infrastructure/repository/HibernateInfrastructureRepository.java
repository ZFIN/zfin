/**
 * Class HibernateInfrastructureRepository
 */
package org.zfin.infrastructure.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.springframework.stereotype.Repository;
import org.zfin.ExternalNote;
import org.zfin.database.DbSystemUtil;
import org.zfin.database.UnloadInfo;
import org.zfin.database.presentation.Column;
import org.zfin.database.presentation.Table;
import org.zfin.expression.ExpressionAssay;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerType;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.TermAlias;
import org.zfin.profile.Person;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DateUtil;
import org.zfin.util.ZfinStringUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static org.zfin.framework.HibernateUtil.currentSession;

@Repository
public class HibernateInfrastructureRepository implements InfrastructureRepository {

    private static Logger logger = Logger.getLogger(HibernateInfrastructureRepository.class);


    public void insertActiveData(String zdbID) {
        Session session = HibernateUtil.currentSession();
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(zdbID);
        session.save(activeData);
    }

    public void insertActiveSource(String zdbID) {
        Session session = HibernateUtil.currentSession();
        ActiveSource activeSource = new ActiveSource();
        activeSource.setZdbID(zdbID);
        session.save(activeSource);
    }

    public void deleteActiveData(ActiveData activeData) {
        logger.info("Deleting " + activeData.getZdbID() + " from zdb_active_data");
        HibernateUtil.currentSession().delete(activeData);
    }


    public void deleteActiveSource(ActiveSource activeSource) {
        logger.info("Deleting " + activeSource.getZdbID() + " from zdb_active_source");
        HibernateUtil.currentSession().delete(activeSource);
    }

    public void deleteActiveDataByZdbID(String zdbID) {
        ActiveData a = getActiveData(zdbID);
        if (a == null) {
            logger.error("unable to find zdbID in active data to delete [" + zdbID + "]");
            return;
        }
        deleteActiveData(a);
    }

    public void deleteActiveSourceByZdbID(String zdbID) {
        ActiveSource activeSource = getActiveSource(zdbID);
        if (activeSource == null) {
            logger.error("unable to find zdbID in active source to delete [" + zdbID + "]");
            return;
        }
        deleteActiveSource(activeSource);
    }

    public int deleteActiveDataByZdbID(List<String> zdbIDs) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from ActiveData ad where ad.zdbID in (:zdbIDs)");
        query.setParameterList("zdbIDs", zdbIDs);
        return query.executeUpdate();
    }

    public int deleteRecordAttributionsForData(String dataZdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:dataZdbID");
        query.setParameter("dataZdbID", dataZdbID);
        return query.executeUpdate();
    }

    public int deleteRecordAttribution(String dataZdbID, String sourceZdbId) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:dataZdbID and ra.sourceZdbID = :sourceZdbID");
        query.setParameter("dataZdbID", dataZdbID);
        query.setParameter("sourceZdbID", sourceZdbId);
        return query.executeUpdate();
    }

    public ActiveData getActiveData(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ActiveData.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (ActiveData) criteria.uniqueResult();
    }

    public ActiveSource getActiveSource(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ActiveSource.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (ActiveSource) criteria.uniqueResult();
    }

    //todo: add a getter here, or do some mapping to objects so that we can test the insert in a routine way

    public void insertRecordAttribution(String dataZdbID, String sourceZdbID) {
        if (hasStandardPublicationAttribution(dataZdbID, sourceZdbID)) {
            return;
        }

        Session session = HibernateUtil.currentSession();

        // need to return null if no valid publication string
        if (null == session.get(Publication.class, sourceZdbID)) {
            logger.warn("try into insert record attribution with bad pub: " + sourceZdbID);
            return;
        }

        RecordAttribution ra = new RecordAttribution();
        ra.setDataZdbID(dataZdbID);
        ra.setSourceZdbID(sourceZdbID);
        ra.setSourceType(RecordAttribution.SourceType.STANDARD);

        session.save(ra);
    }

    public RecordAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID) {
        return insertPublicAttribution(dataZdbID, sourceZdbID, RecordAttribution.SourceType.STANDARD);
    }

    @Override
    public void insertPublicAttribution(Genotype genotype, String sourceZdbID) {
        Publication publication = (Publication) HibernateUtil.currentSession().get(Publication.class, sourceZdbID);
        insertPublicAttribution(genotype, publication);
    }

    @Override
    public void insertStandardPubAttribution(String dataZdbID, Publication publication) {
        PublicationAttribution publicationAttribution = new PublicationAttribution();
        publicationAttribution.setDataZdbID(dataZdbID);
        publicationAttribution.setPublication(publication);
        publicationAttribution.setSourceType(RecordAttribution.SourceType.STANDARD);
        if (!existAttribution(publicationAttribution)) {
            HibernateUtil.currentSession().save(publicationAttribution);
        }
    }

    private boolean existAttribution(PublicationAttribution attribution) {
        return getRecordAttribution(attribution.getDataZdbID(), attribution.getSourceZdbID(), attribution.getSourceType()) != null;
    }

    public RecordAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();

        RecordAttribution recordAttribution = new RecordAttribution();
        recordAttribution.setDataZdbID(dataZdbID);
        recordAttribution.setSourceZdbID(sourceZdbID);
        Publication publication = (Publication) session.get(Publication.class, sourceZdbID);
        recordAttribution.setSourceZdbID(publication.getZdbID());
        recordAttribution.setSourceType(sourceType);

        RecordAttribution result = (RecordAttribution) session.createCriteria(RecordAttribution.class).add(Example.create(recordAttribution)).uniqueResult();
        if (result == null) {
            session.save(recordAttribution);
        }
        return recordAttribution;
    }

    //retrieve a dataNote by its zdb_id

    public DataNote getDataNoteByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DataNote.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (DataNote) criteria.uniqueResult();
    }

    public MarkerAlias getMarkerAliasByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(MarkerAlias.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (MarkerAlias) criteria.uniqueResult();
    }

    public DataAlias getDataAliasByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DataAlias.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (DataAlias) criteria.uniqueResult();
    }

    public RecordAttribution getRecordAttribution(String dataZdbID, String sourceZdbId, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("sourceZdbID", sourceZdbId));
        if (sourceType != null) {
            criteria.add(Restrictions.eq("sourceType", sourceType.toString()));
        }
        // if not specified, load the default inserted type
        else {
            criteria.add(Restrictions.eq("sourceType", RecordAttribution.SourceType.STANDARD.toString()));
        }
        return (RecordAttribution) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<RecordAttribution> getRecordAttributionsForType(String dataZdbID, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("sourceType", sourceType.toString()));
        return criteria.list();
    }


    @SuppressWarnings("unchecked")
    public List<RecordAttribution> getRecordAttributions(ActiveData data) {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", data.getZdbID()));

        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public RecordAttribution getRecordAttribution(ActiveData data, ActiveSource source, RecordAttribution.SourceType type) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", data.getZdbID()));
        criteria.add(Restrictions.eq("sourceZdbID", source.getZdbID()));
        criteria.add(Restrictions.eq("sourceType", type.toString()));

        return (RecordAttribution) criteria.uniqueResult();
    }

    public PublicationAttribution getPublicationAttribution(PublicationAttribution attribution) {
        Session session = HibernateUtil.currentSession();
        String hql = "from PublicationAttribution " +
                "where publication = :publication AND" +
                "      dataZdbID = :dataID ";
        Query query = session.createQuery(hql);
        query.setParameter("publication", attribution.getPublication());
        query.setParameter("dataID", attribution.getDataZdbID());

        return (PublicationAttribution) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<PublicationAttribution> getPublicationAttributions(String dataZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PublicationAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<PublicationAttribution> getPublicationAttributions(String dataZdbID, RecordAttribution.SourceType type) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PublicationAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("sourceType", type.toString()));
        return criteria.list();
    }

    /**
     * Retrieves all data alias groups
     *
     * @return list of data alias groups
     */
    @SuppressWarnings("unchecked")
    public List<DataAliasGroup> getAllDataAliasGroups() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DataAliasGroup.class);
        return (List<DataAliasGroup>) criteria.list();
    }

    /**
     * Retrieve terms by name (contains) and ontology
     *
     * @param termName   term name (contains)
     * @param ontologies Ontology
     * @return list of GenericTerm
     * exception: If no ontology provided a NullPointerException is thrown.
     */
    @SuppressWarnings("unchecked")
    public List<GenericTerm> getTermsByName(String termName, List<Ontology> ontologies) {
        if (ontologies == null || ontologies.isEmpty()) {
            throw new NullPointerException("No Ontology provided");
        }
        List<String> ontologyNameStrings = new ArrayList<>(2);
        for (Ontology ontology : ontologies) {
            ontologyNameStrings.add(ontology.getOntologyName());
        }


        String hql = "select distinct term from GenericTerm term  " +
                "where lower(term.termName) like :name " +
                " AND term.obsolete = :obsolete ";
        if (ontologies.size() == 1) {
            hql += " AND term.ontology = :ontology ";
        } else {
            hql += " AND term.ontology in (:ontology) ";
        }
        hql += " order by term.termName";

        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        query.setString("name", "%" + termName.toLowerCase() + "%");
        if (ontologies.size() == 1) {
            query.setParameter("ontology", ontologies.get(0));
        } else {
            query.setParameterList("ontology", ontologyNameStrings);
        }
        query.setBoolean("obsolete", false);
        List<GenericTerm> list = (List<GenericTerm>) query.list();

        hql = "select alias.term from TermAlias alias " +
                "where  alias.aliasLowerCase like :name " +
                " AND alias.term.obsolete = :obsolete ";
        if (ontologies.size() == 1) {
            hql += " AND alias.term.ontology = :ontology ";
        } else {
            hql += " AND alias.term.ontology in  (:ontology) ";
        }
        hql += " order by alias.term.termName";
        Query queryTwo = session.createQuery(hql);
        queryTwo.setString("name", "%" + termName.toLowerCase() + "%");
        if (ontologies.size() == 1) {
            queryTwo.setParameter("ontology", ontologies.get(0));
        } else {
            queryTwo.setParameterList("ontology", ontologies);
        }
        queryTwo.setBoolean("obsolete", false);
        List<GenericTerm> synonyms = (List<GenericTerm>) queryTwo.list();
        list.addAll(synonyms);
        Set<GenericTerm> distinctSet = new HashSet<>(list);
        List<GenericTerm> distinctList = new ArrayList<>(distinctSet);
        return distinctList;
    }

    /**
     * Retrieve terms by synonym match.
     *
     * @param queryString synonym name
     * @param ontology    name
     * @return list of terms
     */
    @SuppressWarnings("unchecked")
    public List<GenericTerm> getTermsBySynonymName(String queryString, Ontology ontology) {
        if (ontology == null) {
            throw new NullPointerException("No Ontology provided");
        }

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(TermAlias.class);
        criteria.add(Restrictions.like("alias", queryString, MatchMode.ANYWHERE));
        Criteria termCriteria = criteria.createCriteria("term");
        termCriteria.add(Restrictions.eq("ontology", ontology));
        termCriteria.add(Restrictions.eq("obsolete", false));
        return (List<GenericTerm>) criteria.list();
    }

    /**
     * Retrieve a single term by name and ontology. If more than one term is found
     * an exception is thrown.
     *
     * @param termName name
     * @param ontology Ontology
     * @return Term
     */
    @SuppressWarnings("unchecked")
    public GenericTerm getTermByName(String termName, Ontology ontology) {
        if (ontology == null) {
            throw new NullPointerException("No Ontology provided");
        }

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.eq("termName", termName));
        criteria.add(Restrictions.eq("obsolete", false));
        criteria.add(Restrictions.eq("ontology", ontology));
        criteria.add(Restrictions.eq("secondary", false));
        GenericTerm term = (GenericTerm) criteria.uniqueResult();
        if (term == null) {
            return null;
        }
        return term;
    }

    /**
     * Retrieve a single term by name and a list of ontologies. Checks for all ontologies and picks the first one.
     * Hopefully, there term is only found in a single ontology. Match has to be exact.
     *
     * @param termName   name
     * @param ontologies list of ontologies
     * @return Term
     */
    public GenericTerm getTermByName(String termName, List<Ontology> ontologies) {
        if (ontologies == null) {
            return null;
        }
        for (Ontology ontology : ontologies) {
            GenericTerm term = getTermByName(termName, ontology);
            if (term != null) {
                return term;
            }
        }
        return null;
    }

    /**
     * Retrieve Term by ZDB ID from the gDAG table.
     * If the ID is from the GOTERM table retrieve the corresponding term ID first.
     *
     * @param termID term id
     * @return Generic Term
     */
    @SuppressWarnings("unchecked")
    public GenericTerm getTermByID(String termID) {
        if (StringUtils.isEmpty(termID)) {
            return null;
        }

        Session session = HibernateUtil.currentSession();
        GenericTerm term = null;
        term = (GenericTerm) session.get(GenericTerm.class, termID);
        if (term == null) {
            return null;
        }
        return term;
    }


    /**
     * Retrieve Root of given ontology.
     *
     * @param ontologyName ontology name
     * @return Term
     */
    public GenericTerm getRootTerm(String ontologyName) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(GenericTerm.class);
        Ontology ontology = Ontology.getOntology(ontologyName);
        crit.add(Restrictions.eq("ontology", ontology));
        crit.add(Restrictions.eq("root", true));
        return (GenericTerm) crit.uniqueResult();
    }

    /**
     * Fetch a Data Alias Group entity for a given name
     *
     * @param name alias group object
     * @return DataAliasGroup entity
     */
    public DataAliasGroup getDataAliasGroupByName(String name) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(DataAliasGroup.class);
        query.add(Restrictions.eq("name", name));
        return (DataAliasGroup) query.uniqueResult();
    }

    public ControlledVocab getCVZdbIDByTerm(String cvTermName) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(ControlledVocab.class);
        query.add(Restrictions.eq("cvTermName", cvTermName));
        return (ControlledVocab) query.uniqueResult();
    }


    public PublicationAttribution getStandardPublicationAttribution(String dataZdbID, String pubZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PublicationAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("sourceZdbID", pubZdbID));
        criteria.add(Restrictions.eq("sourceType", RecordAttribution.SourceType.STANDARD.toString()));
        return (PublicationAttribution) criteria.uniqueResult();
    }

    @Override
    public List<Updates> getUpdates(String zdbID) {
        return HibernateUtil.currentSession()
                .createCriteria(Updates.class)
                .add(Restrictions.eq("recID", zdbID))
                .addOrder(Order.desc("whenUpdated"))
                .list();
    }

    @Override
    public void insertUpdatesTable(String recID, String fieldName, String newValue, String comments) {
        Person person = ProfileService.getCurrentSecurityUser();
        insertUpdatesTable(recID, person, fieldName, null, newValue, comments);
    }


    @Override
    public void insertUpdatesTable(String recID, BeanFieldUpdate beanFieldUpdate) {
        insertUpdatesTable(recID, beanFieldUpdate, null);
    }

    @Override
    public void insertUpdatesTable(EntityZdbID entity, BeanFieldUpdate beanFieldUpdate, String comment) {
        insertUpdatesTable(entity.getZdbID(), beanFieldUpdate, comment);
    }

    @Override
    public void insertUpdatesTable(String recID, String fieldName, String comments) {
        insertUpdatesTable(recID, fieldName, null, null, comments);
    }

    @Override
    public void insertUpdatesTable(String recID, String fieldName, String oldValue, String newValue, String comments) {
        Person person = ProfileService.getCurrentSecurityUser();
        insertUpdatesTable(recID, person, fieldName, oldValue, newValue, comments);
    }

    @Override
    public void insertUpdatesTable(EntityZdbID entity, String fieldName, String comments, String newValue, String oldValue) {
        Person person = ProfileService.getCurrentSecurityUser();
        insertUpdatesTable(entity.getZdbID(), person, fieldName, oldValue, newValue, comments);
    }

    @Override
    public void insertUpdatesTable(EntityZdbID entity, String fieldName, String comments) {
        insertUpdatesTable(entity, fieldName, comments, entity.getAbbreviation(), null);
    }

    private void insertUpdatesTable(String recID, BeanFieldUpdate beanFieldUpdate, String comments) {
        String from = (beanFieldUpdate.getFrom() == null) ? null : beanFieldUpdate.getFrom().toString();
        String to = (beanFieldUpdate.getTo() == null) ? null : beanFieldUpdate.getTo().toString();

        insertUpdatesTable(recID, beanFieldUpdate.getField(), from, to, comments);
    }

    private void insertUpdatesTable(String recId, Person submitter, String fieldName, String oldValue, String newValue, String comments) {
        Date when = new Date();
        if (submitter == null) {
            insertUpdatesTable(recId, null, null, fieldName, oldValue, newValue, comments, when);
        } else {
            insertUpdatesTable(recId, submitter, submitter.getFullName(), fieldName, oldValue, newValue, comments, when);
        }
    }

    private void insertUpdatesTable(String recID, Person submitter, String submitterName, String fieldName,
                                    String oldValue, String newValue, String comments, Date when) {
        Session session = HibernateUtil.currentSession();

        Updates updates = new Updates();
        updates.setRecID(recID);
        updates.setSubmitter(submitter);
        updates.setSubmitterName(submitterName);
        updates.setFieldName(fieldName);
        updates.setOldValue(oldValue);
        updates.setNewValue(newValue);
        updates.setComments(comments);
        updates.setWhenUpdated(when);
        session.save(updates);
    }

    public int deleteRecordAttributionForPub(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.sourceZdbID=:zdbID");
        query.setParameter("zdbID", zdbID);
        return query.executeUpdate();
    }


    @SuppressWarnings("unchecked")
    public int deleteRecordAttributionByDataZdbIDs(List<String> dataZdbIDs) {
        for (String zdbID : dataZdbIDs) {
            logger.debug("zdbID: " + zdbID);
        }


        Session session = HibernateUtil.currentSession();
        String hql = "" +
                "delete from RecordAttribution ra where ra.dataZdbID in (:dataZdbIDs)";
        Query query = session.createQuery(hql);
        query.setParameterList("dataZdbIDs", dataZdbIDs);
        return query.executeUpdate();

//        Criteria criteria = session.createCriteria(RecordAttribution.class);
//        criteria.add(Restrictions.in("dataZdbID", dataZdbIDs));
//        List<RecordAttribution> recordAttributions = criteria.list();
//
//        for (RecordAttribution recordAttribution : recordAttributions) {
//            logger.info("deleting recordAttribution: " + recordAttribution);
//            session.delete(recordAttribution);
//            logger.info("DELETED recordAttribution: " + recordAttribution);
//        }
//        session.flush();
//        return recordAttributions.size();

//        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID in (:dataZdbIDs)");
//        query.setParameterList("dataZdbIDs",dataZdbIDs) ;
//        int deletedRecords = query.executeUpdate() ;
//        return deletedRecords ;
    }


    public int removeRecordAttributionForPub(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:zdbID");
        query.setParameter("zdbID", zdbID);
        return query.executeUpdate();
    }

    public int removeRecordAttributionForData(String dataZdbID, String pubZdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:datazdbID and ra.sourceZdbID=:zdbID");
        query.setParameter("zdbID", pubZdbID);
        query.setParameter("datazdbID", dataZdbID);
        return query.executeUpdate();
    }

    public void removeRecordAttributionForType(String zdbID, String datazdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:datazdbID and ra.sourceZdbID=:zdbID and ra.sourceType=:sourceType");
        query.setParameter("zdbID", zdbID);
        query.setParameter("datazdbID", datazdbID);
        query.setParameter("sourceType", RecordAttribution.SourceType.FEATURE_TYPE.toString());
        query.executeUpdate();
        currentSession().flush();
    }

    public void removeRecordAttributionForTranscript(String zdbID, String datazdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:datazdbID and ra.sourceZdbID=:zdbID and ra.sourceType=:sourceType");
        query.setParameter("zdbID", zdbID);
        query.setParameter("datazdbID", datazdbID);
        query.setParameter("sourceType", RecordAttribution.SourceType.STANDARD.toString());
        query.executeUpdate();
        currentSession().flush();
    }

    /**
     * Retrieve the Updates flag that indicates if the db is disabled for updates.
     *
     * @return zdbFlag
     */
    public ZdbFlag getUpdatesFlag() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ZdbFlag.class);
        criteria.add(Restrictions.eq("type", ZdbFlag.Type.DISABLE_UPDATES));
        return (ZdbFlag) criteria.uniqueResult();
    }

    public ExternalNote getExternalNoteByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (ExternalNote) session.get(ExternalNote.class, zdbID);
    }

    @SuppressWarnings("unchecked")
    public List<AllNamesFastSearch> getAllNameMarkerMatches(String string) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AllMarkerNamesFastSearch.class);
        criteria.add(Restrictions.like("nameLowerCase", "%" + string + "%"));
        return (List<AllNamesFastSearch>) criteria.list();

    }

    @SuppressWarnings("unchecked")
    public List<AllMarkerNamesFastSearch> getAllNameMarkerMatches(String string, MarkerType type) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AllMarkerNamesFastSearch.class);
        criteria.add(Restrictions.like("nameLowerCase", "%" + string + "%"));
        Criteria marker = criteria.createCriteria("marker");
        marker.add(Restrictions.eq("markerType", type));
        return (List<AllMarkerNamesFastSearch>) marker.list();
    }


    // Todo: ReplacementZdbID is a composite key (why?) and thus this
    // could retrieve more than one record. If so then it throws an exception,
    // meaning the id was replaced more than once and then we would not know which one to use.

    public ReplacementZdbID getReplacementZdbId(String oldZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(ReplacementZdbID.class);
        query.add(Restrictions.eq("oldZdbID", oldZdbID));
        return (ReplacementZdbID) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<DataAlias> getDataAliases(String aliasLowerName) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(DataAlias.class);
        crit.add(Restrictions.eq("aliasLowerCase", aliasLowerName));
        return (List<DataAlias>) crit.list();
    }

    /**
     * Retrieve a list of names of entities that match a given alias name.
     *
     * @param aliasLowerName alias
     * @return list of strings
     */
    @SuppressWarnings("unchecked")
    public List<String> getDataAliasesWithAbbreviation(String aliasLowerName) {
        Session session = HibernateUtil.currentSession();
        SQLQuery sqlQuery = session.createSQLQuery("select distinct get_obj_abbrev(dalias_data_zdb_id) as abbreviation " +
                "from data_alias, alias_group " +
                "where dalias_alias_lower = :aliasLowerName and dalias_group_id = aliasgrp_pk_id " +
                "                and aliasgrp_name != :aliasGroup ");
        sqlQuery.addScalar("abbreviation");
        sqlQuery.setParameter("aliasLowerName", aliasLowerName);
        sqlQuery.setParameter("aliasGroup", DataAliasGroup.Group.SEQUENCE_SIMILARITY.toString());
        return (List<String>) sqlQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<String> getBestNameMatch(String name) {
        Session session = HibernateUtil.currentSession();
        SQLQuery sqlQuery = session.createSQLQuery("select allmapnm_zdb_id as zdbID from all_map_names " +
                "where allmapnm_name_lower = :name and " +
                "allmapnm_precedence in ('Current symbol', 'Current name', 'Genotype name') " +
                "UNION " +
                "select term_zdb_id as zdb_id from term " +
                "where lower(term_name) = :name and term_ontology = :ontology");
        sqlQuery.addScalar("zdbID");
        sqlQuery.setParameter("name", name.toLowerCase());
        sqlQuery.setParameter("ontology", Ontology.ANATOMY.getOntologyName());
        return (List<String>) sqlQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionAssay> getAllAssays() {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(ExpressionAssay.class);
        crit.addOrder(Order.asc("displayOrder"));
        return (List<ExpressionAssay>) crit.list();
    }


    public int getDataAliasesAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(" " +
                        " select count(*) from record_attribution, data_alias" +
                        "      where recattrib_data_zdb_id = dalias_zdb_id" +
                        "      and dalias_data_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID" +
                        " ")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    public int getOrthologRecordAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(
                        "select count(*) From ortholog as ortho, " +
                                "              ortholog_evidence as oe " +
                                "where ortho.ortho_zebrafish_gene_zdb_id = :zdbID " +
                                "and   ortho.ortho_zdb_id = oe.oev_ortho_zdb_id " +
                                "and   oe.oev_pub_zdb_id =  :pubZdbID")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    public int getMarkerFeatureRelationshipAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(" " +
                        " select count(*) from record_attribution, feature_marker_relationship " +
                        "      where recattrib_data_zdb_id = fmrel_ftr_zdb_id" +
                        "      and fmrel_mrkr_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID" +
                        " ")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }


    public int getMarkerGenotypeFeatureRelationshipAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(" " +
                        " select count(*) from record_attribution, feature_marker_relationship, genotype_feature " +
                        "      where recattrib_data_zdb_id = genofeat_geno_zdb_id" +
                        "      and fmrel_ftr_zdb_id = genofeat_feature_zdb_id " +
                        "      and fmrel_mrkr_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID" +
                        " ")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    public int getFeatureGenotypeAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(" " +
                        " select count(*) from record_attribution, genotype_feature " +
                        "      where recattrib_data_zdb_id = genofeat_geno_zdb_id" +
                        "      and genofeat_feature_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID" +
                        " ")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    @Override
    public int getGoRecordAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "select count(*)" +
                        " from record_attribution, marker_go_term_evidence " +
                        "      where recattrib_data_zdb_id = mrkrgoev_zdb_id " +
                        "      and mrkrgoev_mrkr_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID  " +
                        "")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    public int getDBLinkAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "select count(*)" +
                        " from record_attribution, db_link " +
                        "      where recattrib_data_zdb_id = dblink_zdb_id " +
                        "      and dblink_linked_recid = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID  " +
                        "")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    public int getDBLinkAssociatedToGeneAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "select count(*)" +
                        " from record_attribution, db_link, marker_relationship " +
                        "      where recattrib_data_zdb_id = dblink_zdb_id " +
                        "      and dblink_linked_recid = mrel_mrkr_2_zdb_id" +
                        "      and mrel_mrkr_1_zdb_id = :zdbID" +
                        "     and mrel_type = 'gene encodes small segment' " +
                        "      and recattrib_source_zdb_id = :pubZdbID  " +
                        "")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    /**
     * Unused.
     * Retrieve # of related markers (in the first position) that are attributed to to this pub.
     *
     * @param zdbID
     * @param pubZdbID
     * @return
     */
    public int getFirstMarkerRelationshipAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "select count(*)" +
                        " from record_attribution, marker_relationship " +
                        "      where recattrib_data_zdb_id = mrel_mrkr_1_zdb_id " +
                        "      and mrel_mrkr_2_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID  " +
                        "")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    /**
     * Retrieve # of related markers (in the second position) that are attributed to to this pub.
     *
     * @param zdbID
     * @param pubZdbID
     * @return
     */
    public int getSecondMarkerRelationshipAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "select count(*)" +
                        " from record_attribution, marker_relationship " +
                        "      where recattrib_data_zdb_id = mrel_mrkr_2_zdb_id " +
                        "      and mrel_mrkr_1_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID  " +
                        "")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    public int getExpressionExperimentMarkerAttributions(Marker m, String pubZdbID) {
        if (m.isInTypeGroup(Marker.TypeGroup.ATB)) {
            return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                            "select count(*)" +
                            " from record_attribution ra , expression_experiment ee " +
                            " where ra.recattrib_data_zdb_id = ee.xpatex_zdb_id " +
                            " and ee.xpatex_atb_zdb_id = :zdbID " +
                            " and ee.xpatex_source_zdb_id = :pubZdbID " +
                            "")
                            .setString("zdbID", m.getZdbID())
                            .setString("pubZdbID", pubZdbID)
                            .uniqueResult().toString()
            );
        }
        // assume its a gene
        else if (m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                            "select count(*)" +
                            " from record_attribution ra , expression_experiment ee " +
                            " where ra.recattrib_data_zdb_id = ee.xpatex_zdb_id " +
                            " and ee.xpatex_gene_zdb_id = :zdbID " +
                            " and ee.xpatex_source_zdb_id = :pubZdbID " +
                            "")
                            .setString("zdbID", m.getZdbID())
                            .setString("pubZdbID", pubZdbID)
                            .uniqueResult().toString()
            );
        } else if (m.isInTypeGroup(Marker.TypeGroup.CDNA_AND_EST)) {
            return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                            "select count(*)" +
                            " from record_attribution ra , expression_experiment ee " +
                            " where ra.recattrib_data_zdb_id = ee.xpatex_zdb_id " +
                            " and ee.xpatex_probe_feature_zdb_id = :zdbID " +
                            " and ee.xpatex_source_zdb_id = :pubZdbID " +
                            "")
                            .setString("zdbID", m.getZdbID())
                            .setString("pubZdbID", pubZdbID)
                            .uniqueResult().toString()
            );
        } else {
            return 0;
        }
    }

    public int getSequenceTargetingReagentEnvironmentAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "  select count(*)  " +
                        " from record_attribution ra, fish_str str " +
                        " where ra.recattrib_data_zdb_id = str.fishstr_fish_zdb_id " +
                        " and str.fishstr_str_zdb_id = :zdbID " +
                        " and  ra.recattrib_source_zdb_id = :pubZdbID " +
                        "")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }


    public int getGenotypeExperimentRecordAttributions(String genotypeID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "  select count(*)  " +
                        " from record_attribution ra, fish_experiment ge, fish fish  " +
                        " where ra.recattrib_data_zdb_id = ge.genox_exp_zdb_id " +
                        " and ge.genox_fish_zdb_id = fish_zdb_id " +
                        " and fish.fish_genotype_zdb_id = :genotypeID " +
                        " and  ra.recattrib_source_zdb_id = :pubZdbID " +
                        "")
                        .setString("genotypeID", genotypeID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    /**
     * Number of phenotype experiments a genotype is being used.
     *
     * @param genotypeID    genotype
     * @param publicationID publication
     * @return number of references
     */
    public int getGenotypePhenotypeRecordAttributions(String genotypeID, String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select phenox from PhenotypeExperiment phenox where " +
                " phenox.fishExperiment.fish.genotype.id = :genotypeID and " +
                " phenox.figure.publication.id = :publicationID ";
        Query query = session.createQuery(hql);
        query.setString("genotypeID", genotypeID);
        query.setString("publicationID", publicationID);
        List list = query.list();

        return list == null ? 0 : list.size();
    }

    @SuppressWarnings("unchecked")
    public String getReplacedZdbID(String oldZdbID) {
        List<ReplacementZdbID> replacedAccessionList =
                (List<ReplacementZdbID>) HibernateUtil.currentSession()
                        .createCriteria(ReplacementZdbID.class)
                        .add(Restrictions.eq("oldZdbID", oldZdbID))
                        .list();
        if (replacedAccessionList != null && replacedAccessionList.size() == 1) {
            return replacedAccessionList.get(0).getReplacementZdbID();
        } else if (replacedAccessionList == null) {
            logger.warn("Replacement list is null for zdbID: " + oldZdbID);
        } else if (replacedAccessionList.size() > 1) {
            logger.error("Replacement list has non-unique replacements: " + replacedAccessionList.size() + " for zdbID: " + oldZdbID);
        }
        return null;
    }

    @Override
    public List<ReplacementZdbID> getReplacedZdbIDsByType(ActiveData.Type type) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ReplacementZdbID.class);
        criteria.add(Restrictions.like("oldZdbID", "ZDB-" + type.toString() + "-%"));
        return (List<ReplacementZdbID>) criteria.list();
    }

    public String getNewZdbID(String wdoldZdbID) {
        List<WithdrawnZdbID> replacedAccessionList =
                (List<WithdrawnZdbID>) HibernateUtil.currentSession()
                        .createCriteria(WithdrawnZdbID.class)
                        .add(Restrictions.eq("wdoldZdbID", wdoldZdbID))
                        .list();
        if (replacedAccessionList != null && replacedAccessionList.size() == 1) {
            return replacedAccessionList.get(0).getWdnewZdbID();
        } else if (replacedAccessionList == null) {
            logger.warn("Replacement list is null for zdbID: " + wdoldZdbID);
        } else if (replacedAccessionList.size() > 1) {
            logger.error("Replacement list has non-unique replacements: " + replacedAccessionList.size() + " for zdbID: " + wdoldZdbID);
        }
        return null;
    }

    /**
     * Execute a sql statement through straight JDBC call.
     *
     * @param jdbcStatement query
     * @return number from sql query: # of updated records, inserted records, deleted records.
     */
    @Override
    public int executeJdbcStatement(final DatabaseJdbcStatement jdbcStatement) {
        final Session session = currentSession();
        return session.doReturningWork(new ReturningWork<Integer>(){
            @Override
            public Integer execute(Connection connection) throws SQLException {
                Statement statement = null;
                int affectedRows = 0;
                try {
                    statement = connection.createStatement();
                    statement.execute(jdbcStatement.getQuery());
                    affectedRows = statement.getUpdateCount();
                    logger.info("Number of updated rows: " + affectedRows);
                    session.flush();
                } catch (SQLException exception) {
                    logger.error("could not execute statement in file '" + jdbcStatement.getScriptFile() + "' " +
                            "and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                            jdbcStatement.getHumanReadableQueryString(), exception);
                    logger.error(DbSystemUtil.getLockInfo());
                    throw new RuntimeException(exception);
                } finally {
                    try {
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException e) {
                        logger.error("could not close statement '" + jdbcStatement.getScriptFile() + "' " +
                                "                    and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                                jdbcStatement.getHumanReadableQueryString(), e);
                    }
                }
                return affectedRows;
            }
        });
    }

    @Override
    public void executeJdbcStatement(DatabaseJdbcStatement statement, List<List<String>> data) {
        executeJdbcStatement(statement, data, 1000);
    }

    /**
     * Execute a sql statement through straight JDBC call and inserting given string data.
     *
     * @param jdbcStatement query
     * @param data          string data
     */
    @Override
    public void executeJdbcStatement(final DatabaseJdbcStatement jdbcStatement, final List<List<String>> data, final int batchSize) {
        long start = System.currentTimeMillis();
        final Session session = currentSession();
        session.doWork(connection -> {
            PreparedStatement statement = null;
            ResultSet rs = null;
            int accumulatedBatchCounter = 0;
            int currentBatchSize = 0;
            try {
                Statement st = connection.createStatement();
                rs = st.executeQuery("select * from " + jdbcStatement.getTableName());
                ResultSetMetaData rsMetaData = rs.getMetaData();
                statement = connection.prepareStatement(jdbcStatement.getQuery());
                for (List<String> individualRow : data) {
                    int index = 1;
                    for (String column : individualRow) {
                        String columnType = rsMetaData.getColumnClassName(index);
                        if (columnType.equals("java.lang.Boolean")) {
                            boolean columnTypeBol = column.equals("t");
                            statement.setBoolean(index++, columnTypeBol);
                        } else if (columnType.equals("java.lang.Long")) {
                            long number = Long.valueOf(column);
                            statement.setLong(index++, number);
                        } else if (columnType.equals("java.lang.Integer")) {
                            int number = Integer.valueOf(column);
                            statement.setInt(index++, number);
                        } else {
                            statement.setString(index++, column);
                        }
                    }
                    statement.addBatch();
                    currentBatchSize++;
                    accumulatedBatchCounter++;
                    // execute batch if batch size is reached or if no more records are found.
                    if (currentBatchSize == batchSize || accumulatedBatchCounter == data.size()) {
                        statement.executeBatch();
                        logger.info("Batch inserted records up to #: " + accumulatedBatchCounter);
                        // reset the index that keeps track of the current batch size
                        currentBatchSize = 0;
                    }
                    session.flush();
                    session.clear();
                }
            } catch (SQLException exception) {
                logger.error("could not execute statement in file '" + jdbcStatement.getScriptFile() + "' " +
                        "and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                        jdbcStatement.getHumanReadableQueryString(), exception);
                for (int index = 0; index < batchSize; index++) {
                    int index1 = accumulatedBatchCounter - batchSize + index;
                    logger.error("Record number " + index1 + ", record: " + data.get(index1));
                }
                logger.error(DbSystemUtil.getLockInfo());
                throw new RuntimeException(exception);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    logger.error("could not close statement '" + jdbcStatement.getScriptFile() + "' " +
                            "                    and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                            jdbcStatement.getQuery(), e);
                }
                // find out the individual line that caused the problem.
/*
        if (error)
            runBatchJDBCStatementIndividually(jdbcStatement, data, accumulatedBatchCounter - batchSize, accumulatedBatchCounter);
*/
            }
        });
        long end = System.currentTimeMillis();
        logger.debug("Insertion:  " + jdbcStatement.getHumanReadableQueryString());
        logger.info("Finished Insertion of " + data.size() + " records in " + DateUtil.getTimeDuration(start, end));
        session.flush();
    }

    public void executeJdbcQuery(final String query) {
        long start = System.currentTimeMillis();
        final Session session = currentSession();
        session.doWork(new Work(){
            @Override
            public void execute(Connection connection) throws SQLException {
                ResultSet rs = null;
                try {
                    Statement st = connection.createStatement();
                    int returnNumber = st.executeUpdate(query);
                    session.flush();
                    session.clear();
                } catch (SQLException exception) {
                    logger.error("could not execute  '" + query + "' ", exception);
                    throw new RuntimeException(exception);
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (SQLException e) {
                        // ignore
                    }
                }
            }
        });
        long end = System.currentTimeMillis();
        logger.debug("Query:  " + query);
        logger.info("Query took " + DateUtil.getTimeDuration(start, end));
        session.flush();
    }

    private void runBatchJDBCStatementIndividually(final DatabaseJdbcStatement jdbcStatement, final List<List<String>> data, final int start, final int end) {
        final Session session = currentSession();
        session.doWork(new Work(){
            @Override
            public void execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                ResultSet rs = null;
                int index = start;
                try {
                    Statement st = connection.createStatement();
                    rs = st.executeQuery("select * from " + jdbcStatement.getTableName());
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    statement = connection.prepareStatement(jdbcStatement.getQuery());
                    for (; index < end; index++) {
                        List<String> row = data.get(index);
                        for (String column : row) {
                            String columnType = rsMetaData.getColumnClassName(index);
                            if (columnType.equals("java.lang.Boolean")) {
                                boolean columnTypeBol = column.equals("t");
                                statement.setBoolean(index++, columnTypeBol);
                            } else {
                                statement.setString(index++, column);
                            }
                        }
                        statement.execute();
                    }
                } catch (SQLException exception) {
                    logger.error("Record number " + index + ", record: " + data.get(index));
                    logger.error(DbSystemUtil.getLockInfo());
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException e) {
                        logger.error("could not close statement '" + jdbcStatement.getScriptFile() + "' " +
                                "                    and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                                jdbcStatement.getQuery(), e);
                    }
                }
            }
        });
    }

    /**
     * Return a set of data from a native SELECT statement.
     *
     * @param statement jdbc query
     * @return list of strings
     */
    @Override
    public List<List<String>> executeNativeQuery(DatabaseJdbcStatement statement) {
        if (statement.isSubquery()) {
            return executeNativeQuery(statement.getSubQuery());
        } else {
            return executeNativeQuery(statement.getQuery());
        }
    }

    public List<List<String>> executeNativeQuery(final String queryString) {
        final Session session = HibernateUtil.currentSession();
        return session.doReturningWork(connection -> {
            Statement stmt = null;
            List<List<String>> data = new ArrayList<>();
            try {
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(queryString);
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int index = 1; index <= rs.getMetaData().getColumnCount(); index++) {
                        String value = rs.getString(index);
                        if (value == null) {
                            value = "";
                        }
                        row.add(value);
                    }
                    data.add(row);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return data;
        });

    }

    @Override
    public List<List<String>> executeNativeDynamicQuery(DatabaseJdbcStatement statement) {
/*
        if (!statement.isDynamicQuery())
            throw new RuntimeException("Not a dynamic query");
*/
        List<List<String>> firstQueryResultList = executeNativeQuery(statement);
        if (firstQueryResultList == null) {
            return null;
        }
        DatabaseJdbcStatement subQuery = statement.getSubQueryStatement();
        if (subQuery == null) {
            return firstQueryResultList;
        }
        List<List<String>> returnResultList = new ArrayList<>();
        for (List<String> resultRecord : firstQueryResultList) {
            subQuery.bindVariables(resultRecord);
            List<List<String>> c = executeNativeQuery(subQuery);
            if ((!subQuery.isExistsSubquery() && CollectionUtils.isEmpty(c)) ||
                    (subQuery.isExistsSubquery() && CollectionUtils.isNotEmpty(c))) {
                if (subQuery.isListSubquery()) {
                    returnResultList.addAll(c);
                } else {
                    returnResultList.add(resultRecord);
                }
            }
        }
        return returnResultList;
    }

    /**
     * Return a set of data from a native SELECT statement.
     *
     * @param statement jdbc query
     * @return list of strings
     */
    @Override
    public List<List<String>> executeNativeQuery(DatabaseJdbcStatement statement, Session session) {
        SQLQuery query = session.createSQLQuery(statement.getQuery());
        List objects = query.list();
        if (objects == null) {
            return null;
        }
        if (objects.size() == 0) {
            return null;
        }

        List<List<String>> data = new ArrayList<>(objects.size());
        if (objects.get(0) instanceof Object[]) {
            List<Object[]> entities = (List<Object[]>) objects;
            for (Object[] row : entities) {
                List<String> singleRow = new ArrayList<>(row.length);
                for (Object o : row) {
                    if (o != null) {
                        singleRow.add(o.toString());
                    } else {
                        singleRow.add("");
                    }
                }
                data.add(singleRow);
            }
        } else if (objects.get(0) instanceof BigDecimal) {
            List<BigDecimal> entities = (List<BigDecimal>) objects;
            for (BigDecimal row : entities) {
                List<String> singleRow = new ArrayList<>(1);
                singleRow.add(row.toString());
                data.add(singleRow);
            }
        } else {
            List<String> entities = (List<String>) objects;
            for (String row : entities) {
                List<String> singleRow = new ArrayList<>(1);
                singleRow.add(row);
                data.add(singleRow);
            }
        }
        return data;
    }

    /**
     * Retrieve the date when the database was loaded from. For dev sites it's the date of the production database that
     * was used for loading.
     *
     * @return unload date of the production database.
     */
    @Override
    public UnloadInfo getUnloadInfo() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(UnloadInfo.class);
        criteria.addOrder(Order.desc("date"));
        criteria.setFirstResult(0);
        criteria.setMaxResults(1);
        return (UnloadInfo) criteria.uniqueResult();
    }

    /**
     * Retrieve all term ids.
     * If firstNIds > 0 return only the first N.
     * If firstNIds < 0 return null
     *
     * @param clazz     Entity to be retrieved
     * @param idName    unique id
     * @param firstNIds number of records
     * @return list of ids
     */
    @Override
    public List<String> getAllEntities(Class clazz, String idName, int firstNIds) {
        if (firstNIds < 0) return null;

        if (HibernateUtil.currentSession().getSessionFactory().getClassMetadata(clazz) == null) {
            throw new NullPointerException("No Entity of type " + clazz.getName() + " found in Hibernate mapping file.");
        }

        String hql = "select distinct " + idName + " from " + clazz.getSimpleName() + " order by " + idName;
        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (firstNIds > 0) {
            query.setMaxResults(firstNIds);
        }
        return query.list();
    }

    @Override
    public List<String> getPublicationAttributionZdbIdsForType(String microarrayPubZdbID, Marker.Type markerType) {
        String hql = " select distinct pa.dataZdbID from Marker m join m.publications pa " +
                " where m.markerType.name = :type " +
                " and pa.publication.zdbID = :pubZdbId  " +
                " ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("type", markerType.name())
                .setString("pubZdbId", microarrayPubZdbID)
                .list();
    }

    /**
     * select *
     * from
     * record_attribution ra
     * join marker m on m.mrkr_zdb_id=ra.recattrib_data_zdb_id
     * where
     * ra.recattrib_source_zdb_id='ZDB-PUB-071218-1'
     * and
     * not exists (
     * select * from geo_marker gm
     * where gm.gm_zdb_id=ra.recattrib_data_zdb_id
     * );
     *
     * @param deletedAttributions
     * @param microarrayPub
     * @return
     */
    @Override
    public int removeAttributionsNotFound(Collection<String> deletedAttributions, String microarrayPub) {
        int batchSize = 20;
        int count = 0;

        for (String attributionToRemove : deletedAttributions) {
            count += removeRecordAttributionForData(attributionToRemove, microarrayPub);
            if (count % batchSize == 0) {
                HibernateUtil.currentSession().flush();
            }
        }


        return count;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * select *
     * from geo_marker gm
     * where
     * not exists (
     * select 't' from record_attribution ra
     * where ra.recattrib_data_zdb_id=gm.gm_zdb_id
     * and
     * ra.recattrib_source_zdb_id='ZDB-PUB-071218-1'
     * );
     *
     * @param addAttributions
     * @param microarrayPub
     * @return
     */
    @Override
    public int addAttributionsNotFound(Collection<String> addAttributions, String microarrayPub) {
        int batchSize = 20;
        int count = 0;

        for (String attributionToRemove : addAttributions) {
            insertPublicAttribution(attributionToRemove, microarrayPub);
            ++count;
            if (count % batchSize == 0) {
                HibernateUtil.currentSession().flush();
            }
        }
        return count;
    }

    @Override
    public List<String> getPublicationAttributionsForPub(String microarrayPub) {
        String hql = " " +
                "  select p.dataZdbID from Marker m join m.publications p where p.sourceZdbID = :pubZdbIDs " +
                " ";
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("pubZdbIDs", microarrayPub)
                .list();
    }


    @Override
    public boolean hasStandardPublicationAttribution(String zdbID, String microarrayPub) {
        String sql = " " +
                " select count(*) from record_attribution ra " +
                "where ra.recattrib_data_zdb_id= :zdbID " +
                "and ra.recattrib_source_zdb_id= :pubZdbID " +
                "and ra.recattrib_source_type='standard' " +
                " ";
        int numPubs = Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("zdbID", zdbID)
                .setString("pubZdbID", microarrayPub)
                .uniqueResult().toString());

        return numPubs > 0;
    }

    @Override
    public boolean hasStandardPublicationAttributionForRelatedMarkers(String zdbID, String microarrayPub) {
        String sql = " " +
                " select count(*) from (" +
                "select ra.recattrib_data_zdb_id " +
                "from record_attribution ra " +
                "where ra.recattrib_data_zdb_id= :zdbID " +
                "and ra.recattrib_source_zdb_id= :pubZdbID " +
                "and ra.recattrib_source_type='standard' " +
                "union all " +
                "select mr.mrel_mrkr_1_zdb_id " +
                " from record_attribution ra  " +
                " join marker_relationship mr on mr.mrel_mrkr_2_zdb_id=ra.recattrib_data_zdb_id " +
                " where mr.mrel_mrkr_1_zdb_id=:zdbID " +
                " and ra.recattrib_source_zdb_id=:pubZdbID " +
                " and mr.mrel_type in ('gene encodes small segment','gene contains small segment')" +
                " and ra.recattrib_source_type='standard' " +
                ") as subquery" +
                " ";
        int numPubs = Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("zdbID", zdbID)
                .setString("pubZdbID", microarrayPub)
                .uniqueResult().toString());

        return numPubs > 0;
    }

    public List<String> retrieveMetaData(final String table) {
        Session session = currentSession();
        return session.doReturningWork(new ReturningWork<List<String>>(){
            @Override
            public List<String> execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                ResultSet rs = null;
                List<String> columnNames = null;
                try {
                    Statement st = connection.createStatement();
                    rs = st.executeQuery("select * from " + table);
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    columnNames = new ArrayList<>(columnCount);
                    int index = 1;
                    while (index <= columnCount) {
                        columnNames.add(rsMetaData.getColumnName(index++));
                    }
                } catch (SQLException exception) {
                    logger.error(DbSystemUtil.getLockInfo());
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException e) {
                        logger.error("could not close statement", e);
                    }
                }
                return columnNames;
            }
        });
    }

    /**
     * Retrieve the meta data for all columns of a given table.
     *
     * @param table table
     * @return list of column objects
     */
    public List<Column> retrieveColumnMetaData(final Table table) {
        final String tableName = table.getTableName();
        Session session = currentSession();
        return session.doReturningWork(new ReturningWork<List<Column>>(){
            @Override
            public List<Column> execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                ResultSet rs = null;
                List<Column> columns = new ArrayList<>(5);
                try {
                    Statement st = connection.createStatement();
                    rs = st.executeQuery("select * from " + tableName);
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    int index = 1;
                    while (index <= columnCount) {
                        Column column = new Column(rsMetaData.getColumnName(index), table);
                        column.setColumnType(rsMetaData.getColumnTypeName(index));
                        column.setColumnLength(rsMetaData.getColumnDisplaySize(index));
                        column.setIsNullable(rsMetaData.isNullable(index));
                        columns.add(column);
                        index++;
                    }
                } catch (SQLException exception) {
                    logger.error(DbSystemUtil.getLockInfo());
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException e) {
                        logger.error("could not close statement", e);
                    }
                }
                return columns;
            }
        });
    }


    /**
     * execute SQL query for each provided data row individually (for debugging purposes).
     *
     * @param statement
     * @param data
     */
    @Override
    public void executeJdbcStatementOneByOne(DatabaseJdbcStatement statement, List<List<String>> data) {
        executeJdbcStatement(statement, data, 1);
    }

    public int getGenotypeExpressionExperimentRecordAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                        "  select count(*)  " +
                        " from record_attribution ra, fish_experiment ge, expression_experiment ee, fish f " +
                        " where ra.recattrib_data_zdb_id = ee.xpatex_zdb_id " +
                        " and ee.xpatex_genox_zdb_id = ge.genox_zdb_id " +
                        " and ge.genox_fish_zdb_id = :zdbID " +
                        " and  ra.recattrib_source_zdb_id = :pubZdbID " +
                        "")
                        .setString("zdbID", zdbID)
                        .setString("pubZdbID", pubZdbID)
                        .uniqueResult().toString()
        );
    }

    @Override
    public void deleteActiveEntity(String zdbID) {
        if (ActiveData.validateActiveData(zdbID)) {
            deleteActiveDataByZdbID(zdbID);
        } else {
            deleteActiveSourceByZdbID(zdbID);
        }
    }

    @Override
    public List<Publication> getTermReferences(GenericTerm term, String orderBy) {
        String hql = "select distinct model.publication, model.publication.publicationDate, " +
                "model.publication.authors  from DiseaseAnnotation as model where " +
                "model.disease = :term";

        if (orderBy == null || orderBy.equalsIgnoreCase("date")) {
            hql += "     order by model.publication.publicationDate desc";
        } else if (orderBy.equalsIgnoreCase("author")) {
            hql += "     order by model.publication.authors";
        }
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);

        List<Object[]> objectList = (List<Object[]>) query.list();
        List<Publication> pubList = new ArrayList<>(objectList.size());
        for (Object[] o : objectList) {
            pubList.add((Publication) o[0]);
        }
        return pubList;
    }

    @Override
    public void saveDataNote(DataNote note, Publication publication) {
        note.setDate(new Date());
        note.setCurator(ProfileService.getCurrentSecurityUser());
        HibernateUtil.currentSession().save(note);
    }

    @Override
    public void insertPublicAttribution(Genotype genotype, Publication publication) {
        insertStandardPubAttribution(genotype.getZdbID(), publication);
        for (GenotypeFeature gFeature : genotype.getGenotypeFeatures()) {
            Feature feature = gFeature.getFeature();
            insertStandardPubAttribution(feature.getZdbID(), publication);
            if (feature.getAllelicGene() != null) {
                insertStandardPubAttribution(feature.getAllelicGene().getZdbID(), publication);
            }
        }

    }

    @Override
    public void insertRecordAttribution(Fish fish, Publication publication) {
        insertStandardPubAttribution(fish.getZdbID(), publication);
        insertStandardPubAttribution(fish.getGenotype().getZdbID(), publication);
    }

    @Override
    public long getDistinctPublicationsByData(String entityID) {
        String hql = "select distinct pubAtt.publication from PublicationAttribution as pubAtt " +
                "where pubAtt.dataZdbID = :genotypeID AND " +
                "pubAtt.sourceType = :type ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("genotypeID", entityID);
        query.setParameter("type", RecordAttribution.SourceType.STANDARD);
        List<Publication> list = query.list();
        return list.size();

    }

    @Override
    public boolean isTermNameForControlledVocabExists(String cvTermName) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ControlledVocab.class);
        criteria.add(Restrictions.eq("cvTermName", cvTermName));

        ControlledVocab controlledVocab = (ControlledVocab) criteria.uniqueResult();
        return controlledVocab != null;
    }

    @Override
    public boolean isForeignSpeciesForControlledVocabExists(String cvForeignSpecies) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ControlledVocab.class);
        criteria.add(Restrictions.eq("cvForeignSpecies", cvForeignSpecies));

        ControlledVocab controlledVocab = (ControlledVocab) criteria.uniqueResult();
        return controlledVocab != null;
    }

    @Override
    public boolean isNameDefForControlledVocabExists(String cvNameDefinition) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ControlledVocab.class);
        criteria.add(Restrictions.eq("cvNameDefinition", cvNameDefinition));

        ControlledVocab controlledVocab = (ControlledVocab) criteria.uniqueResult();
        return controlledVocab != null;
    }

    @Override
    public ControlledVocab getControlledVocabByNameAndSpecies(String termName, String foreignSpecies) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ControlledVocab.class);
        criteria.add(Restrictions.eq("cvTermName", termName));
        criteria.add(Restrictions.eq("cvForeignSpecies", foreignSpecies));

        return (ControlledVocab) criteria.uniqueResult();
    }

    @Override
    public ControlledVocab getControlledVocabByID(String zdbID) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ControlledVocab.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));

        return (ControlledVocab) criteria.uniqueResult();
    }

    @Override
    public void insertMutationDetailAttribution(String dataZdbID, String publicationID) {
        Session session = HibernateUtil.currentSession();

        RecordAttribution recordAttribution = new RecordAttribution();
        recordAttribution.setDataZdbID(dataZdbID);
        recordAttribution.setSourceZdbID(publicationID);
        recordAttribution.setSourceType(RecordAttribution.SourceType.STANDARD);

        Criteria criteriaExisting = session.createCriteria(RecordAttribution.class);
        criteriaExisting.add(Example.create(recordAttribution));
        RecordAttribution thisPubResult = (RecordAttribution) criteriaExisting.uniqueResult();
        // done if record already exists
        if (thisPubResult != null) {
            return;
        }

        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("sourceType", RecordAttribution.SourceType.STANDARD.toString()));
        RecordAttribution result = (RecordAttribution) criteria.uniqueResult();

        // remove previous attribution if different from current pub
        if (result != null && !result.getSourceZdbID().equals(publicationID)) {
            session.delete(result);
        }
        session.save(recordAttribution);
    }

    @Override
    public void deleteMutationDetailAttribution(String zdbID, String publicationID) {
        Session session = HibernateUtil.currentSession();

        RecordAttribution recordAttribution = new RecordAttribution();
        recordAttribution.setDataZdbID(zdbID);
        recordAttribution.setSourceZdbID(publicationID);
        recordAttribution.setSourceType(RecordAttribution.SourceType.STANDARD);

        Criteria criteriaExisting = session.createCriteria(RecordAttribution.class);
        criteriaExisting.add(Example.create(recordAttribution));
        RecordAttribution thisPubResult = (RecordAttribution) criteriaExisting.uniqueResult();
        if (thisPubResult != null) {
            HibernateUtil.currentSession().delete(thisPubResult);
        }
    }

    @Override
    public EntityZdbID getEntityByID(Class<? extends EntityZdbID> entity, String zdbID) {
        return (EntityZdbID) HibernateUtil.currentSession().get(entity, zdbID);
    }

    @Override
    public void insertMarkerHistory(MarkerHistory history) {
        Session session = HibernateUtil.currentSession();
        history.setDate(new Date());
        session.save(history);
    }

    @Override
    public void setDisableUpdatesFlag(boolean readonlyMode) {
        Boolean.valueOf(HibernateUtil.currentSession().createSQLQuery("select " +
                "        zflag_is_on  " +
                "    from  zdb_flag " +
                "    where zflag_name='disable updates'").uniqueResult().toString());
    }
}


