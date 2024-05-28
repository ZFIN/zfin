/**
 * Class HibernateInfrastructureRepository
 */
package org.zfin.infrastructure.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.ExternalNote;
import org.zfin.database.DbSystemUtil;
import org.zfin.database.UnloadInfo;
import org.zfin.database.presentation.Column;
import org.zfin.database.presentation.Table;
import org.zfin.expression.ExpressionAssay;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.featureflag.FeatureFlag;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerHistory;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.profile.Person;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.uniprot.persistence.UniProtRelease;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DateUtil;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;

@Repository
public class HibernateInfrastructureRepository implements InfrastructureRepository {

    private static final Logger logger = LogManager.getLogger(HibernateInfrastructureRepository.class);


    public void insertActiveData(String zdbID) {
        Session session = HibernateUtil.currentSession();
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(zdbID);
        session.save(activeData);
    }

    @Override
    public void insertActiveDataWithoutValidationIgnoreConflict(String zdbID) {
        currentSession().createSQLQuery("""
                INSERT INTO zdb_active_data(zactvd_zdb_id) VALUES (:zdbID)
                ON CONFLICT (zactvd_zdb_id)
                DO NOTHING
                """).setParameter("zdbID", zdbID)
            .executeUpdate();
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
        Query<ActiveData> query = session.createQuery("from ActiveData where zdbID = :id", ActiveData.class);
        query.setParameter("id", zdbID);
        return query.uniqueResult();
    }

    @Override
    public List<ActiveData> getAllActiveData(Set<String> zdbIDs) {
        Session session = currentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ActiveData> query = builder.createQuery(ActiveData.class);
        Root<ActiveData> root = query.from(ActiveData.class);
        query.select(root)
            .where(
                root.get("zdbID").in(zdbIDs)
            );

        return session.createQuery(query).list();
    }

    public ActiveSource getActiveSource(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query<ActiveSource> query = session.createQuery("from ActiveSource where zdbID = :ID", ActiveSource.class);
        query.setParameter("ID", zdbID);
        return query.uniqueResult();
    }


    @Override
    public List<ActiveSource> getAllActiveSource(Set<String> zdbIDs) {
        Session session = currentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ActiveSource> query = builder.createQuery(ActiveSource.class);
        Root<ActiveSource> root = query.from(ActiveSource.class);
        query.select(root)
            .where(
                root.get("zdbID").in(zdbIDs)
            );

        return session.createQuery(query).list();
    }

    //todo: add a getter here, or do some mapping to objects so that we can test the insert in a routine way

    public RecordAttribution insertRecordAttribution(String dataZdbID, String sourceZdbID) {
        if (sourceZdbID != null) {
            sourceZdbID = sourceZdbID.trim();
        }
        if (hasStandardPublicationAttribution(dataZdbID, sourceZdbID)) {
            return null;
        }

        Session session = HibernateUtil.currentSession();

        // need to return null if no valid publication string
        if (null == session.get(Publication.class, sourceZdbID)) {
            logger.warn("try into insert record attribution with bad pub: " + sourceZdbID);
            return null;
        }

        RecordAttribution ra = new RecordAttribution();
        ra.setDataZdbID(dataZdbID);
        ra.setSourceZdbID(sourceZdbID);
        ra.setSourceType(RecordAttribution.SourceType.STANDARD);

        session.save(ra);
        return ra;
    }

    public RecordAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID) {
        return insertPublicAttribution(dataZdbID, sourceZdbID, RecordAttribution.SourceType.STANDARD);
    }

    @Override
    public void insertPublicAttribution(Genotype genotype, String sourceZdbID) {
        Publication publication = HibernateUtil.currentSession().get(Publication.class, sourceZdbID);
        insertPublicAttribution(genotype, publication);
    }

    @Override
    public PublicationAttribution insertStandardPubAttribution(String dataZdbID, Publication publication) {
        PublicationAttribution publicationAttribution = new PublicationAttribution();
        publicationAttribution.setDataZdbID(dataZdbID);
        publicationAttribution.setPublication(publication);
        publicationAttribution.setSourceType(RecordAttribution.SourceType.STANDARD);
        if (!existAttribution(publicationAttribution)) {
            HibernateUtil.currentSession().save(publicationAttribution);
        }
        return publicationAttribution;
    }

    private boolean existAttribution(PublicationAttribution attribution) {
        return getRecordAttribution(attribution.getDataZdbID(), attribution.getSourceZdbID(), attribution.getSourceType()) != null;
    }

    public RecordAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();

        RecordAttribution recordAttribution = new RecordAttribution();
        recordAttribution.setDataZdbID(dataZdbID);
        recordAttribution.setSourceZdbID(sourceZdbID);
        Publication publication = session.get(Publication.class, sourceZdbID);
        recordAttribution.setSourceZdbID(publication.getZdbID());
        recordAttribution.setSourceType(sourceType);

        RecordAttribution result = getRecordAttribution(dataZdbID, sourceZdbID, sourceType);
        if (result == null) {
            session.save(recordAttribution);
            result = recordAttribution;
        }
        return result;
    }

    //retrieve a dataNote by its zdb_id

    public DataNote getDataNoteByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query<DataNote> criteria = session.createQuery("from DataNote where zdbID = :ID", DataNote.class);
        criteria.setParameter("ID", zdbID);
        return criteria.uniqueResult();
    }

    public MarkerAlias getMarkerAliasByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query<MarkerAlias> query = session.createQuery("from MarkerAlias where zdbID = :ID", MarkerAlias.class);
        query.setParameter("ID", zdbID);
        return query.uniqueResult();
    }

    public DataAlias getDataAliasByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query<DataAlias> query = session.createQuery("from DataAlias where zdbID = :ID", DataAlias.class);
        query.setParameter("ID", zdbID);
        return query.uniqueResult();
    }

    public RecordAttribution getRecordAttribution(String dataZdbID, String sourceZdbId, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();
        String hql = """ 
                    from RecordAttribution
                    where dataZdbID = :dataZdbID
                    AND sourceZdbID = :sourceZdbID
                    AND sourceType = :sourceType
            """;
        Query<RecordAttribution> query = session.createQuery(hql, RecordAttribution.class);

        query.setParameter("dataZdbID", dataZdbID);
        query.setParameter("sourceZdbID", sourceZdbId);
        // if not specified, load the default inserted type
        query.setParameter("sourceType", Objects.requireNonNullElse(sourceType, RecordAttribution.SourceType.STANDARD).toString());
        return query.uniqueResult();
    }

    public List<RecordAttribution> getRecordAttributionsForPublicationAndType(String sourceZdbID, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();
        String hql = "FROM RecordAttribution WHERE sourceZdbID = :sourceZdbID AND sourceType = :sourceType";
        Query<RecordAttribution> query = session.createQuery(hql, RecordAttribution.class);
        query.setParameter("sourceZdbID", sourceZdbID);
        query.setParameter("sourceType", sourceType.toString());
        return query.list();
    }

    public List<RecordAttribution> getRecordAttributionsForType(String dataZdbID, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();
        Query<RecordAttribution> query = session.createQuery("""
            from RecordAttribution where dataZdbID = :dataZdbID
            AND sourceType = :sourceType
            """, RecordAttribution.class);
        query.setParameter("dataZdbID", dataZdbID);
        query.setParameter("sourceType", sourceType.toString());
        return query.list();
    }


    public List<RecordAttribution> getRecordAttributions(ActiveData data) {
        return getRecordAttributions(data.getZdbID());
    }

    public List<RecordAttribution> getRecordAttributions(String activeDataZdbID) {
        Session session = HibernateUtil.currentSession();
        Query<RecordAttribution> query = session.createQuery("from RecordAttribution where dataZdbID = :ID", RecordAttribution.class);
        query.setParameter("ID", activeDataZdbID);
        return query.list();
    }

    public PublicationAttribution getPublicationAttributionByID(long publicationAttributionID) {
        Session session = HibernateUtil.currentSession();
        return session.get(PublicationAttribution.class, publicationAttributionID);
    }

    public PublicationAttribution getPublicationAttribution(PublicationAttribution attribution) {
        return getPublicationAttribution(attribution.getPublication(), attribution.getDataZdbID());
    }

    public PublicationAttribution getPublicationAttribution(Publication publication, String dataZdbID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from PublicationAttribution " +
                     "where publication = :publication AND" +
                     "      dataZdbID = :dataID ";
        Query<PublicationAttribution> query = session.createQuery(hql, PublicationAttribution.class);
        query.setParameter("publication", publication);
        query.setParameter("dataID", dataZdbID);

        return query.uniqueResult();
    }

    public List<PublicationAttribution> getPublicationAttributions(String dataZdbID) {
        Session session = HibernateUtil.currentSession();
        Query<PublicationAttribution> query = session.createQuery("from PublicationAttribution where dataZdbID = :dataZdbID", PublicationAttribution.class);
        query.setParameter("dataZdbID", dataZdbID);
        return query.list();
    }

    public List<PublicationAttribution> getPublicationAttributions(String dataZdbID, RecordAttribution.SourceType type) {
        Session session = HibernateUtil.currentSession();
        Query<PublicationAttribution> query = session.createQuery("from PublicationAttribution " +
                                                                  "where dataZdbID = :dataZdbID " +
                                                                  "AND sourceType= :sourceType", PublicationAttribution.class);
        query.setParameter("dataZdbID", dataZdbID);
        query.setParameter("sourceType", type.toString());
        return query.list();
    }

    /**
     * Retrieves all data alias groups
     *
     * @return list of data alias groups
     */
    @SuppressWarnings("unchecked")
    public List<DataAliasGroup> getAllDataAliasGroups() {
        Session session = HibernateUtil.currentSession();
        return session.createQuery("from DataAliasGroup").list();
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
        query.setParameter("name", "%" + termName.toLowerCase() + "%");
        if (ontologies.size() == 1) {
            query.setParameter("ontology", ontologies.get(0));
        } else {
            query.setParameterList("ontology", ontologyNameStrings);
        }
        query.setParameter("obsolete", false);
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
        queryTwo.setParameter("name", "%" + termName.toLowerCase() + "%");
        if (ontologies.size() == 1) {
            queryTwo.setParameter("ontology", ontologies.get(0));
        } else {
            queryTwo.setParameterList("ontology", ontologies);
        }
        queryTwo.setParameter("obsolete", false);
        List<GenericTerm> synonyms = (List<GenericTerm>) queryTwo.list();
        list.addAll(synonyms);
        Set<GenericTerm> distinctSet = new HashSet<>(list);
        return new ArrayList<>(distinctSet);
    }

    /**
     * Fetch a Data Alias Group entity for a given name
     *
     * @param name alias group object
     * @return DataAliasGroup entity
     */
    public DataAliasGroup getDataAliasGroupByName(String name) {
        Session session = HibernateUtil.currentSession();
        Query<DataAliasGroup> query = session.createQuery("from DataAliasGroup where name =:name", DataAliasGroup.class);
        query.setParameter("name", name);
        return query.uniqueResult();
    }

    public ControlledVocab getCVZdbIDByTerm(String cvTermName) {
        Session session = HibernateUtil.currentSession();
        Query<ControlledVocab> query = session.createQuery("from ControlledVocab where cvTermName =:cvTermName", ControlledVocab.class);
        query.setParameter("cvTermName", cvTermName);
        return query.uniqueResult();
    }


    @Override
    public List<Updates> getUpdates(String zdbID) {
        return HibernateUtil.currentSession()
            .createQuery("from Updates where recID = :recID order by whenUpdated desc", Updates.class)
            .setParameter("recID", zdbID)
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
    public void insertUpdatesTable(String recID, List<BeanFieldUpdate> beanFieldUpdates) {
        for (BeanFieldUpdate update : beanFieldUpdates) {
            insertUpdatesTable(recID, update);
        }
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
        Query<ZdbFlag> query = session.createQuery("from ZdbFlag WHERE type = :type", ZdbFlag.class);
        query.setParameter("type", ZdbFlag.Type.DISABLE_UPDATES);
        return query.uniqueResult();
    }

    public FeatureFlag getFeatureFlag(String name) {
        Session session = HibernateUtil.currentSession();
        Query<FeatureFlag> query = session.createQuery("from FeatureFlag WHERE name = :name", FeatureFlag.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    public void setFeatureFlag(String name, boolean enabled) {
        FeatureFlag flag;
        Session session = HibernateUtil.currentSession();
        try {
            flag = getFeatureFlag(name);
        } catch (NoResultException e) {
            flag = new FeatureFlag();
            flag.setName(name);
        }
        flag.setEnabledForGlobalScope(enabled);
        flag.setLastModified(new Date());
        session.save(flag);
    }

    public ExternalNote getExternalNoteByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(ExternalNote.class, zdbID);
    }

    public ExternalNote updateExternalNote(ExternalNote note, String text) {
        //TODO: Is this method ever called? It looks like it would generate a NPE
        return updateExternalNote(note, text, null);
    }

    public ExternalNote updateExternalNote(ExternalNote note, String text, Publication publication) {
        String oldText = note.getNote();
        Session session = HibernateUtil.currentSession();
        note.setNote(text);
        if (publication != null) {
            note.setPublication(publication);
        }
        session.save(note);
        insertUpdatesTable(note.getExternalDataZdbID(), "external note", oldText, text, "for " + publication.getZdbID());
        return note;
    }

    public void deleteExternalNote(ExternalNote note) {
        Session session = HibernateUtil.currentSession();
        insertUpdatesTable(note.getExternalDataZdbID(), "external note", "removed note for " + note.getPublication().getZdbID());
        session.delete(note);
    }

    // Todo: ReplacementZdbID is a composite key (why?) and thus this
    // could retrieve more than one record. If so then it throws an exception,
    // meaning the id was replaced more than once and then we would not know which one to use.

    public ReplacementZdbID getReplacementZdbId(String oldZdbID) {
        Session session = HibernateUtil.currentSession();
        Query<ReplacementZdbID> query = session.createQuery("from ReplacementZdbID WHERE oldZdbID = :oldZdbID", ReplacementZdbID.class);
        query.setParameter("oldZdbID", oldZdbID);
        return query.uniqueResult();
    }

    @Override
    public List<ReplacementZdbID> getAllReplacementZdbIds(List<String> oldZdbIDs) {
        Session session = currentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ReplacementZdbID> query = builder.createQuery(ReplacementZdbID.class);
        Root<ReplacementZdbID> root = query.from(ReplacementZdbID.class);
        query.select(root)
            .where(
                root.get("oldZdbID").in(oldZdbIDs)
            );

        return session.createQuery(query).list();
    }

    public List<DataAlias> getDataAliases(String aliasLowerName) {
        Session session = HibernateUtil.currentSession();
        Query<DataAlias> query = session.createQuery("from DataAlias WHERE aliasLowerCase = :aliasLowerCase", DataAlias.class);
        query.setParameter("aliasLowerCase", aliasLowerName);
        return query.list();
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
        NativeQuery sqlQuery = session.createNativeQuery("select distinct get_obj_abbrev(dalias_data_zdb_id) as abbreviation " +
                                                         "from data_alias, alias_group " +
                                                         "where dalias_alias_lower = :aliasLowerName and dalias_group_id = aliasgrp_pk_id " +
                                                         "                and aliasgrp_name != :aliasGroup ");
        sqlQuery.addScalar("abbreviation");
        sqlQuery.setParameter("aliasLowerName", aliasLowerName);
        sqlQuery.setParameter("aliasGroup", DataAliasGroup.Group.SEQUENCE_SIMILARITY.toString());
        return (List<String>) sqlQuery.list();
    }

    public List<ExpressionAssay> getAllAssays() {
        Session session = HibernateUtil.currentSession();
        Query<ExpressionAssay> query = session.createQuery("from ExpressionAssay order by displayOrder", ExpressionAssay.class);
        return query.list();
    }


    public int getDataAliasesAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(
            HibernateUtil.currentSession().createSQLQuery(" " +
                                                          " select count(*) from record_attribution, data_alias" +
                                                          "      where recattrib_data_zdb_id = dalias_zdb_id" +
                                                          "      and dalias_data_zdb_id = :zdbID" +
                                                          "      and recattrib_source_zdb_id = :pubZdbID" +
                                                          " ")
                .setParameter("zdbID", zdbID)
                .setParameter("pubZdbID", pubZdbID)
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
                .setParameter("zdbID", zdbID)
                .setParameter("pubZdbID", pubZdbID)
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
                .setParameter("zdbID", zdbID)
                .setParameter("pubZdbID", pubZdbID)
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
                .setParameter("zdbID", zdbID)
                .setParameter("pubZdbID", pubZdbID)
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
                .setParameter("zdbID", zdbID)
                .setParameter("pubZdbID", pubZdbID)
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
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", pubZdbID)
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
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", pubZdbID)
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
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", pubZdbID)
            .uniqueResult().toString()
        );
    }

    /**
     * Retrieve # of related markers (in the first position) that are attributed to to this pub.
     */
    public int getFirstMarkerRelationshipAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(HibernateUtil.currentSession().createSQLQuery(" " +
                                                                              "select count(*)" +
                                                                              " from record_attribution, marker_relationship " +
                                                                              "      where recattrib_data_zdb_id = mrel_mrkr_1_zdb_id " +
                                                                              "      and mrel_mrkr_2_zdb_id = :zdbID" +
                                                                              "      and recattrib_source_zdb_id = :pubZdbID  " +
                                                                              "")
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", pubZdbID)
            .uniqueResult().toString()
        );
    }

    public int getSecondMarkerRelationshipAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(HibernateUtil.currentSession().createSQLQuery(" " +
                                                                              "select count(*)" +
                                                                              " from record_attribution, marker_relationship " +
                                                                              "      where recattrib_data_zdb_id = mrel_mrkr_2_zdb_id " +
                                                                              "      and mrel_mrkr_1_zdb_id = :zdbID" +
                                                                              "      and recattrib_source_zdb_id = :pubZdbID  " +
                                                                              "")
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", pubZdbID)
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
                .setParameter("zdbID", m.getZdbID())
                .setParameter("pubZdbID", pubZdbID)
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
                .setParameter("zdbID", m.getZdbID())
                .setParameter("pubZdbID", pubZdbID)
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
                .setParameter("zdbID", m.getZdbID())
                .setParameter("pubZdbID", pubZdbID)
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
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", pubZdbID)
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
            .setParameter("genotypeID", genotypeID)
            .setParameter("pubZdbID", pubZdbID)
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
        Query<PhenotypeExperiment> query = session.createQuery(hql, PhenotypeExperiment.class);
        query.setParameter("genotypeID", genotypeID);
        query.setParameter("publicationID", publicationID);
        List<PhenotypeExperiment> list = query.list();
        return list == null ? 0 : list.size();
    }

    public String getReplacedZdbID(String oldZdbID) {
        List<ReplacementZdbID> replacedAccessionList =
            HibernateUtil.currentSession()
                .createQuery("from ReplacementZdbID where oldZdbID = :oldZdbID", ReplacementZdbID.class)
                .setParameter("oldZdbID", oldZdbID)
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

        Query<ReplacementZdbID> query = session.createQuery("from ReplacementZdbID WHERE oldZdbID like  :oldZdbID", ReplacementZdbID.class);
        query.setParameter("oldZdbID", "ZDB-" + type.toString() + "-%");
        return query.list();
    }

    public String getNewZdbID(String wdoldZdbID) {
        List<WithdrawnZdbID> replacedAccessionList =
            HibernateUtil.currentSession()
                .createQuery("from WithdrawnZdbID where wdoldZdbID = :wdoldZdbID", WithdrawnZdbID.class)
                .setParameter("wdoldZdbID", wdoldZdbID)
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
        return session.doReturningWork(connection -> {
            Statement statement = null;
            int affectedRows;
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
                            long number = Long.parseLong(column);
                            statement.setLong(index++, number);
                        } else if (columnType.equals("java.lang.Integer")) {
                            int number = Integer.parseInt(column);
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
        NativeQuery query = session.createSQLQuery(statement.getQuery());
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
        Query<UnloadInfo> query = session.createQuery("from UnloadInfo order by date desc", UnloadInfo.class);
        query.setFirstResult(0);
        query.setMaxResults(1);
        return query.uniqueResult();
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
            .setParameter("type", markerType.name())
            .setParameter("pubZdbId", microarrayPubZdbID)
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
            .setParameter("pubZdbIDs", microarrayPub)
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
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", microarrayPub)
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
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", microarrayPub)
            .uniqueResult().toString());

        return numPubs > 0;
    }

    public List<String> retrieveMetaData(final String table) {
        Session session = currentSession();
        return session.doReturningWork(connection -> {
            ResultSet rs = null;
            Statement st = null;
            List<String> columnNames = null;
            try {
                st = connection.createStatement();
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
                    if (st != null) {
                        st.close();
                    }
                } catch (SQLException e) {
                    logger.error("could not close statement", e);
                }
            }
            return columnNames;
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
        return session.doReturningWork(connection -> {
            Statement st = null;
            ResultSet rs = null;
            List<Column> columns = new ArrayList<>(5);
            try {
                st = connection.createStatement();
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
                    if (st != null) {
                        st.close();
                    }
                } catch (SQLException e) {
                    logger.error("could not close statement", e);
                }
            }
            return columns;
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
            .setParameter("zdbID", zdbID)
            .setParameter("pubZdbID", pubZdbID)
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
        Query<Publication> query = HibernateUtil.currentSession().createQuery(hql, Publication.class);
        query.setParameter("genotypeID", entityID);
        query.setParameter("type", RecordAttribution.SourceType.STANDARD);
        List<Publication> list = query.list();
        return list.size();

    }

    @Override
    public boolean isTermNameForControlledVocabExists(String cvTermName) {
        Session session = HibernateUtil.currentSession();

        Query<ControlledVocab> query = session.createQuery("from ControlledVocab where cvTermName = :cvTermName", ControlledVocab.class);
        query.setParameter("cvTermName", cvTermName);

        ControlledVocab controlledVocab = query.uniqueResult();
        return controlledVocab != null;
    }

    @Override
    public boolean isForeignSpeciesForControlledVocabExists(String cvForeignSpecies) {
        Session session = HibernateUtil.currentSession();

        Query<ControlledVocab> query = session.createQuery("from ControlledVocab where cvForeignSpecies = :cvForeignSpecies", ControlledVocab.class);
        query.setParameter("cvForeignSpecies", cvForeignSpecies);

        ControlledVocab controlledVocab = query.uniqueResult();
        return controlledVocab != null;
    }

    @Override
    public boolean isNameDefForControlledVocabExists(String cvNameDefinition) {
        Session session = HibernateUtil.currentSession();

        Query<ControlledVocab> query = session.createQuery("from ControlledVocab where cvNameDefinition = :cvNameDefinition", ControlledVocab.class);
        query.setParameter("cvNameDefinition", cvNameDefinition);
        ControlledVocab controlledVocab = query.uniqueResult();
        return controlledVocab != null;
    }

    @Override
    public ControlledVocab getControlledVocabByNameAndSpecies(String termName, String foreignSpecies) {
        Session session = HibernateUtil.currentSession();

        Query<ControlledVocab> query = session.createQuery("from ControlledVocab " +
                                                           "where cvTermName = :cvTermName" +
                                                           "AND cvForeignSpecies = :cvForeignSpecies", ControlledVocab.class);
        query.setParameter("cvTermName", termName);
        query.setParameter("cvForeignSpecies", foreignSpecies);

        return query.uniqueResult();
    }

    @Override
    public ControlledVocab getControlledVocabByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(ControlledVocab.class, zdbID);
    }

    @Override
    public void insertMutationDetailAttribution(String dataZdbID, String publicationID) {
        Session session = HibernateUtil.currentSession();

        RecordAttribution recordAttribution = new RecordAttribution();
        recordAttribution.setDataZdbID(dataZdbID);
        recordAttribution.setSourceZdbID(publicationID);
        recordAttribution.setSourceType(RecordAttribution.SourceType.STANDARD);

        RecordAttribution thisPubResult = getRecordAttribution(dataZdbID, publicationID, RecordAttribution.SourceType.STANDARD);
        // done if record already exists
        if (thisPubResult != null) {
            return;
        }

        List<RecordAttribution> resultList = getRecordAttributionsForType(dataZdbID, RecordAttribution.SourceType.STANDARD);
        if (resultList == null || resultList.size() > 1)
            return;
        RecordAttribution result = resultList.stream().findFirst().orElse(null);

        // remove previous attribution if different from current pub
        if (result != null && !result.getSourceZdbID().equals(publicationID)) {
            session.delete(result);
        }
        session.save(recordAttribution);
    }

    @Override
    public void deleteMutationDetailAttribution(String zdbID, String publicationID) {
        RecordAttribution thisPubResult = getRecordAttribution(zdbID, publicationID, RecordAttribution.SourceType.STANDARD);
        if (thisPubResult != null) {
            HibernateUtil.currentSession().delete(thisPubResult);
        }
    }

    @Override
    public EntityZdbID getEntityByID(Class<? extends EntityZdbID> entity, String zdbID) {
        return HibernateUtil.currentSession().get(entity, zdbID);
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

    @Override
    public String getWithdrawnZdbID(String oldZdbID) {
        List<WithdrawnZdbID> withdraws =
            HibernateUtil.currentSession()
                .createQuery("from WithdrawnZdbID where wdoldZdbID = :wdoldZdbID", WithdrawnZdbID.class)
                .setParameter("wdoldZdbID", oldZdbID)
                .list();
        if (withdraws != null && withdraws.size() == 1) {
            return withdraws.get(0).getWdnewZdbID();
        } else if (withdraws == null) {
            logger.warn("Withdrawn list is null for zdbID: " + oldZdbID);
        } else if (withdraws.size() > 1) {
            logger.error("Replacement list has non-unique replacements: " + withdraws.size() + " for zdbID: " + oldZdbID);
        }
        return null;
    }

    @Override
    public List<AnnualStats> getAnnualStats() {
        Session session = HibernateUtil.currentSession();
        return session.createQuery("from AnnualStats ", AnnualStats.class).list();
    }

    @Override
    public List<ControlledVocab> getControlledVocabsForSpeciesByConstruct(Marker construct) {
        Session session = currentSession();
        String hql = "SELECT DISTINCT cv FROM ControlledVocab as cv, " +
                     " ConstructComponent AS cc " +
                     " WHERE cv.zdbID = cc.componentZdbID " +
                     "   AND cv.cvForeignSpecies is not null " +
                     "   AND cc.constructZdbID = :constructId ";

        Query<ControlledVocab> query = session.createQuery(hql, ControlledVocab.class);
        query.setParameter("constructId", construct.getZdbID());

        return query.list();
    }

    @Override
    public void deletePubProcessingInfo(String zdbID) {

        String sql = "delete from PublicationProcessingChecklistEntry where " +
                     " publication.zdbID = :ID ";
        final Query query = HibernateUtil.currentSession().createQuery(sql);
        query.setParameter("ID", zdbID);
        query.executeUpdate();

    }

    @Override
    public UniProtRelease getUniProtReleaseByDate(Date date) {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaQuery<UniProtRelease> query = criteriaBuilder.createQuery(UniProtRelease.class);
        Root<UniProtRelease> uniProtRelease = query.from(UniProtRelease.class);
        query.where(criteriaBuilder.equal(uniProtRelease.get("date"), date));

        return session.createQuery(query).getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public UniProtRelease getLatestUnprocessedUniProtRelease() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaQuery<UniProtRelease> query = criteriaBuilder.createQuery(UniProtRelease.class);
        Root<UniProtRelease> uniProtRelease = query.from(UniProtRelease.class);
        query.where(criteriaBuilder.isNull(uniProtRelease.get("processedDate")));
        query.orderBy(criteriaBuilder.desc(uniProtRelease.get("date")));

        return session.createQuery(query).getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public List<UniProtRelease> getAllUniProtReleases() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaQuery<UniProtRelease> query = criteriaBuilder.createQuery(UniProtRelease.class);
        Root<UniProtRelease> uniProtRelease = query.from(UniProtRelease.class);
        query.orderBy(criteriaBuilder.desc(uniProtRelease.get("date")));

        return session.createQuery(query).list();
    }

    @Override
    public UniProtRelease getUniProtReleaseByID(Long id) {
        return currentSession().get(UniProtRelease.class, id);
    }

    @Override
    public void insertUniProtRelease(UniProtRelease release) {
        currentSession().save(release);
    }

    @Override
    public void updateUniProtRelease(UniProtRelease release) {
        currentSession().update(release);
    }

    @Override
    public void upsertUniProtRelease(UniProtRelease release) {
        currentSession().saveOrUpdate(release);
    }

}


