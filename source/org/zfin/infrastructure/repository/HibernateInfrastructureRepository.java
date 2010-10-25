/**
 *  Class HibernateInfrastructureRepository
 *
 */
package org.zfin.infrastructure.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.ExternalNote;
import org.zfin.expression.ExpressionAssay;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.ZfinJSPFunctions;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerType;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.TermAlias;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.ReplacedAccessionNumber;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DateUtil;

import java.sql.*;
import java.util.*;
import java.util.Date;

import static org.zfin.framework.HibernateUtil.currentSession;

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
        Session session = HibernateUtil.currentSession();
        session.delete(activeData);
    }

    public void deleteActiveDataByZdbID(String zdbID) {
        ActiveData a = getActiveData(zdbID);
        deleteActiveData(a);
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

    public RecordAttribution insertRecordAttribution(String dataZdbID, String sourceZdbID) {
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

    public PublicationAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID) {
        return insertPublicAttribution(dataZdbID, sourceZdbID, RecordAttribution.SourceType.STANDARD);
    }

    public PublicationAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();

        PublicationAttribution publicationAttribution = new PublicationAttribution();
        publicationAttribution.setDataZdbID(dataZdbID);
        publicationAttribution.setSourceZdbID(sourceZdbID);
        Publication publication = (Publication) session.get(Publication.class, sourceZdbID);
        publicationAttribution.setPublication(publication);
        publicationAttribution.setSourceType(sourceType);

        session.save(publicationAttribution);
        return publicationAttribution;
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

    public List<RecordAttribution> getRecAttribforFtrType(String dataZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("sourceType", RecordAttribution.SourceType.FEATURE_TYPE.toString()));

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
        return (PublicationAttribution) session.get(PublicationAttribution.class, attribution);
    }

    @SuppressWarnings("unchecked")
    public List<PublicationAttribution> getPublicationAttributions(String dblinkZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PublicationAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dblinkZdbID));
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
     *         exception: If no ontology provided a NullPointerException is thrown.
     */
    @SuppressWarnings("unchecked")
    public List<GenericTerm> getTermsByName(String termName, List<Ontology> ontologies) {
        if (ontologies == null || ontologies.isEmpty())
            throw new NullPointerException("No Ontology provided");
        List<String> ontologyNameStrings = new ArrayList<String>(2);
        for (Ontology ontology : ontologies) {
            ontologyNameStrings.add(ontology.getOntologyName());
        }


        String hql = "select distinct term from GenericTerm term  " +
                "where lower(term.termName) like :name " +
                " AND term.obsolete = :obsolete ";
        if (ontologies.size() == 1)
            hql += " AND term.ontology = :ontology ";
        else
            hql += " AND term.ontology in (:ontology) ";
        hql += " order by term.termName";

        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        query.setString("name", "%" + termName.toLowerCase() + "%");
        if (ontologies.size() == 1)
            query.setParameter("ontology", ontologies.get(0));
        else
            query.setParameterList("ontology", ontologyNameStrings);
        query.setBoolean("obsolete", false);
        List<GenericTerm> list = (List<GenericTerm>) query.list();

        hql = "select term from GenericTerm term, TermAlias alias " +
                "where alias member of term.aliases " +
                " AND alias.aliasLowerCase like :name " +
                " AND term.obsolete = :obsolete ";
        if (ontologies.size() == 1)
            hql += " AND term.ontology = :ontology ";
        else
            hql += " AND term.ontology in  (:ontology) ";
        hql += " order by term.termName";
        Query queryTwo = session.createQuery(hql);
        queryTwo.setString("name", "%" + termName.toLowerCase() + "%");
        if (ontologies.size() == 1)
            queryTwo.setParameter("ontology", ontologies.get(0));
        else
            queryTwo.setParameterList("ontology", ontologies);
        queryTwo.setBoolean("obsolete", false);
        List<GenericTerm> synonyms = (List<GenericTerm>) queryTwo.list();
        list.addAll(synonyms);
        Set<GenericTerm> distinctSet = new HashSet<GenericTerm>(list);
        List<GenericTerm> distinctList = new ArrayList<GenericTerm>(distinctSet);
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
        if (ontology == null)
            throw new NullPointerException("No Ontology provided");

        Session session = HibernateUtil.currentSession();
        String hql = "from TermAlias alias where " +
                "          alias ";
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
        if (ontology == null)
            throw new NullPointerException("No Ontology provided");

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.eq("termName", termName));
        criteria.add(Restrictions.eq("obsolete", false));
        criteria.add(Restrictions.eq("ontology", ontology));
        criteria.add(Restrictions.eq("secondary", false));
        GenericTerm term = (GenericTerm) criteria.uniqueResult();
        if (term == null)
            return null;
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
        if (ontologies == null)
            return null;
        for (Ontology ontology : ontologies) {
            GenericTerm term = getTermByName(termName, ontology);
            if (term != null)
                return term;
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
        if (StringUtils.isEmpty(termID))
            return null;

        Session session = HibernateUtil.currentSession();
        GenericTerm term = null;
        term = (GenericTerm) session.get(GenericTerm.class, termID);
        if (term == null)
            return null;
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


    public PublicationAttribution getStandardPublicationAttribution(String dataZdbID, String pubZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PublicationAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dataZdbID));
        criteria.add(Restrictions.eq("sourceZdbID", pubZdbID));
        criteria.add(Restrictions.eq("sourceType", RecordAttribution.SourceType.STANDARD.toString()));
        return (PublicationAttribution) criteria.uniqueResult();
    }

    public void insertUpdatesTable(String recID, String fieldName, String new_value, String comments) {
        Session session = HibernateUtil.currentSession();

        Updates up = new Updates();
        Date date = new Date();
        up.setRecID(recID);
        up.setFieldName(fieldName);

        Person person = Person.getCurrentSecurityUser();
        if (person != null) {
            up.setSubmitterID(person.getZdbID());
            up.setSubmitterName(person.getFullName());
        }

        up.setComments(comments);
        up.setNewValue(new_value);
        up.setWhenUpdated(date);
        session.save(up);
    }

    @Override
    public void insertUpdatesTable(String recID, String fieldName, String oldValue, String newValue, String comments) {
        Session session = HibernateUtil.currentSession();

        Updates update = new Updates();
        update.setRecID(recID);
        update.setFieldName(fieldName);
        Person person = Person.getCurrentSecurityUser();
        if (person != null) {
            update.setSubmitterID(person.getZdbID());
            update.setSubmitterName(person.getFullName());
        }
        update.setComments(comments);
        update.setNewValue(newValue);
        update.setOldValue(oldValue);
        update.setWhenUpdated(new Date());
        session.save(update);
    }


    public void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person, String newValue, String oldValue) {
        Session session = HibernateUtil.currentSession();

        Updates up = new Updates();
        up.setRecID(marker.getZdbID());
        up.setFieldName(fieldName);
        if (person != null) {
            up.setSubmitterID(person.getZdbID());
            up.setSubmitterName(person.getUsername());
        }
        up.setComments(comments);
        up.setNewValue(newValue);
        up.setOldValue(oldValue);
        up.setWhenUpdated(new Date());
        session.save(up);
        session.flush();
    }

    /**
     * todo: how is the "old value set"
     *
     * @param marker
     * @param fieldName
     * @param comments
     * @param person
     */
    public void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person) {
        insertUpdatesTable(marker, fieldName, comments, person, marker.getAbbreviation(), "");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int deleteRecordAttributionForPub(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:zdbID");
        query.setParameter("zdbID", zdbID);
        return query.executeUpdate();
    }


    @SuppressWarnings("unchecked")
    public int deleteRecordAttributionByDataZdbID(List<String> dataZdbIDs) {
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

    public int removeRecordAttributionForData(String zdbID, String datazdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:datazdbID and ra.sourceZdbID=:zdbID");
        query.setParameter("zdbID", zdbID);
        query.setParameter("datazdbID", datazdbID);
        return query.executeUpdate();
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
    // meaning the id was replaced more than once and then we would not know whic one to use.

    public ReplacementZdbID getReplacementZdbId(String oldZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(ReplacementZdbID.class);
        query.add(Restrictions.eq("oldZdbID", oldZdbID));
        return (ReplacementZdbID) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<DataAlias> getDataAliases(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(DataAlias.class);
        crit.add(Restrictions.eq("aliasLowerCase", zdbID));
        return (List<DataAlias>) crit.list();
    }

    /**
     * Retrieve a list of names of entities that match a given alias name.
     *
     * @param aliasName alias
     * @return list of strings
     */
    @SuppressWarnings("unchecked")
    public List<String> getDataAliasesWithAbbreviation(String aliasName) {
        Session session = HibernateUtil.currentSession();
        SQLQuery sqlQuery = session.createSQLQuery("select distinct get_obj_abbrev(dalias_data_zdb_id) as abbreviation " +
                "from data_alias, alias_group " +
                "where dalias_alias_lower = :id and dalias_group_id = aliasgrp_pk_id " +
                "                and aliasgrp_name != :aliasGroup ");
        sqlQuery.addScalar("abbreviation");
        sqlQuery.setParameter("id", aliasName);
        sqlQuery.setParameter("aliasGroup", DataAliasGroup.Group.SEQUENCE_SIMILARITY.toString());
        return (List<String>) sqlQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<String> getAnatomyTokens(String name) {
        Session session = HibernateUtil.currentSession();
        SQLQuery sqlQuery = session.createSQLQuery("select anattok_anatitem_zdb_id as zdbID " +
                "from all_anatomy_tokens where anattok_token_lower =  :token ");
        sqlQuery.addScalar("zdbID");
        sqlQuery.setParameter("token", name.toLowerCase());
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

    public int getOrthologueRecordAttributions(String zdbID, String pubZdbID) {
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(" " +
                        " select count(*) from record_attribution, orthologue_evidence_display " +
                        "      where recattrib_data_zdb_id = oevdisp_zdb_id" +
                        "      and oevdisp_gene_zdb_id = :zdbID" +
                        "      and recattrib_source_zdb_id = :pubZdbID" +
                        " ")
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


    public int getMorpholinoRelatedMarkerAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                "select count(*)" +
                " from record_attribution, marker_relationship " +
                "      where recattrib_data_zdb_id = mrel_zdb_id " +
                "      and mrel_mrkr_1_zdb_id = :zdbID" +
                "      and recattrib_source_zdb_id = :pubZdbID  " +
                "       and mrel_mrkr_1_zdb_id[1,8] = 'ZDB-MRPH' " +
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

    public int getMorpholinoEnvironmentAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                "  select count(*)  " +
                " from record_attribution ra, experiment_condition ec " +
                " where ra.recattrib_data_zdb_id = ec.expcond_exp_zdb_id " +
                " and ec.expcond_mrkr_zdb_id = :zdbID " +
                " and  ra.recattrib_source_zdb_id = :pubZdbID " +
                "")
                .setString("zdbID", zdbID)
                .setString("pubZdbID", pubZdbID)
                .uniqueResult().toString()
        );
    }


    public int getGenotypeExperimentRecordAttributions(String zdbID, String pubZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                "  select count(*)  " +
                " from record_attribution ra, genotype_experiment ge " +
                " where ra.recattrib_data_zdb_id = ge.genox_exp_zdb_id " +
                " and ge.genox_geno_zdb_id = :zdbID " +
                " and  ra.recattrib_source_zdb_id = :pubZdbID " +
                "")
                .setString("zdbID", zdbID)
                .setString("pubZdbID", pubZdbID)
                .uniqueResult().toString()
        );
    }

    public int getGenotypePhenotypeRecordAttributions(String zdbID, String pubZdbID) {
//select * from record_attribution ra, atomic_phenotype ph, genotype_experiment ge
//where
//ra.recattrib_data_zdb_id=ph.apato_zdb_id
//and
//ph.apato_genox_zdb_id=ge.genox_zdb_id
//and
//ge.genox_geno_zdb_id = 'ZDB-GENO-000405-1'
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery(" " +
                "  select count(*)  " +
                " from record_attribution ra, atomic_phenotype ph, genotype_experiment ge " +
                " where ra.recattrib_data_zdb_id = ph.apato_zdb_id " +
                " and ph.apato_genox_zdb_id = ge.genox_zdb_id " +
                " and ge.genox_geno_zdb_id = :zdbID " +
                " and  ra.recattrib_source_zdb_id = :pubZdbID " +
                "")
                .setString("zdbID", zdbID)
                .setString("pubZdbID", pubZdbID)
                .uniqueResult().toString()
        );
    }

    @SuppressWarnings("unchecked")
    public String getReplacedZdbID(String oldZdbID){
        List<ReplacementZdbID> replacedAccessionList =
                (List<ReplacementZdbID>) HibernateUtil.currentSession()
                        .createCriteria(ReplacementZdbID.class)
                        .add(Restrictions.eq("oldZdbID",oldZdbID))
                        .list();
        if(replacedAccessionList !=null && replacedAccessionList.size()==1){
            return replacedAccessionList.get(0).getReplacementZdbID() ;
        }
        else
        if(replacedAccessionList ==null){
            logger.warn("Replacement list is null for zdbID: "+ oldZdbID);
        }
        else
        if(replacedAccessionList.size() >1){
            logger.error("Replacement list has non-unique replacements: "+ replacedAccessionList.size() + " for zdbID: "+oldZdbID);
        }
        return null ;
    }

    /**
     * Execute a sql statement through straight JDBC call.
     *
     * @param jdbcStatement query
     * @return number from sql query: # of updated records, inserted records, deleted records.
     */
    @Override
    public int executeJdbcStatement(DatabaseJdbcStatement jdbcStatement) {
        Session session = currentSession();
        Connection connection = session.connection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(jdbcStatement.getQuery());
            session.flush();
        }
        catch (SQLException exception) {
            logger.error("could not execute statement in file '" + jdbcStatement.getScriptFile() + "' " +
                    "and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                    jdbcStatement.getHumanReadableQueryString(), exception);
            throw new RuntimeException(exception);
        }
        finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException e) {
                logger.error("could not close statement '" + jdbcStatement.getScriptFile() + "' " +
                        "                    and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                        jdbcStatement.getHumanReadableQueryString(), e);
            }
        }
        return 1;
    }

    /**
     * Execute a sql statement through straight JDBC call and inserting given string data.
     *
     * @param jdbcStatement query
     * @param data          string data
     */
    @Override
    public void executeJdbcStatement(DatabaseJdbcStatement jdbcStatement, List<List<String>> data) {
        long start = System.currentTimeMillis();
        Session session = currentSession();
        Connection connection = session.connection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        int batchSize = 50;
        int accumulatedBatchCounter = 0;
        int currentBatchSize = 0;
        boolean error = false;
        try {
            Statement st = connection.createStatement();
            rs = st.executeQuery("select * from " + jdbcStatement.getTableName());
            ResultSetMetaData rsMetaData = rs.getMetaData();
            statement = connection.prepareStatement(jdbcStatement.getQuery());
            boolean lastRwoExecuted = false;
            for (List<String> individualRow : data) {
                int index = 1;
                for (String column : individualRow) {
                    String columnType = rsMetaData.getColumnClassName(index);
                    if (columnType.equals("java.lang.Boolean")) {
                        boolean columnTypeBol = column.equals("t");
                        statement.setBoolean(index++, columnTypeBol);
                    } else
                        statement.setString(index++, column);
                }
                statement.addBatch();
                currentBatchSize++;
                accumulatedBatchCounter++;
                // execute batch if batch size is reached or if no more records are found.
                if (currentBatchSize == batchSize || accumulatedBatchCounter == data.size()) {
                    statement.executeBatch();
                    logger.debug("Batch inserted records up to #: " + accumulatedBatchCounter);
                    // reset the index that keeps track of the current batch size
                    currentBatchSize = 0;
                }
                session.flush();
            }
        }
        catch (SQLException exception) {
            logger.error("could not execute statement in file '" + jdbcStatement.getScriptFile() + "' " +
                    "and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                    jdbcStatement.getHumanReadableQueryString(), exception);
            for (int index = 0; index < batchSize; index++) {
                int index1 = accumulatedBatchCounter - batchSize + index;
                logger.error("Record number " + index1 + ", record: " + data.get(index1));
            }
            error = true;
            throw new RuntimeException(exception);
        }
        finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
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
        long end = System.currentTimeMillis();
        logger.debug("Insertion:  " + jdbcStatement.getHumanReadableQueryString());
        logger.info("Finished Insertion of " + data.size() + " records in " + DateUtil.getTimeDuration(start, end));
        session.flush();
    }

    private void runBatchJDBCStatementIndividually(DatabaseJdbcStatement jdbcStatement, List<List<String>> data, int start, int end) {
        Session session = currentSession();
        Connection connection = session.connection();
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
                    } else
                        statement.setString(index++, column);
                }
                statement.execute();
            }
        }
        catch (SQLException exception) {
            logger.error("Record number " + index + ", record: " + data.get(index));
        }
        finally {
            try {
                if (rs != null)
                    rs.close();
                if (statement != null)
                    statement.close();
            } catch (SQLException e) {
                logger.error("could not close statement '" + jdbcStatement.getScriptFile() + "' " +
                        "                    and line [" + jdbcStatement.getStartLine() + "," + jdbcStatement.getEndLine() + "]: " +
                        jdbcStatement.getQuery(), e);
            }
        }
    }

    /**
     * Return a set of data from a native SELECT statement.
     *
     * @param statement jdbc query
     * @return list of strings
     */
    @Override
    public List<List<String>> executeNativeQuery(DatabaseJdbcStatement statement) {
        Session session = HibernateUtil.currentSession();
        SQLQuery query = session.createSQLQuery(statement.getQuery());
        List objects = query.list();
        if (objects == null)
            return null;
        if (objects.size() == 0)
            return null;

        List<List<String>> data = new ArrayList<List<String>>(objects.size());
        if (objects.get(0) instanceof Object[]) {
            List<Object[]> entities = (List<Object[]>) objects;
            for (Object[] row : entities) {
                List<String> singleRow = new ArrayList<String>(row.length);
                for (Object o : row) {
                    if (o != null)
                        singleRow.add(o.toString());
                    else
                        singleRow.add("");
                }
                data.add(singleRow);
            }
        } else if (objects.get(0) instanceof String) {
            List<String> entities = (List<String>) objects;
            for (String row : entities) {
                List<String> singleRow = new ArrayList<String>(1);
                singleRow.add(row);
                data.add(singleRow);
            }
        }
        return data;
    }
}


