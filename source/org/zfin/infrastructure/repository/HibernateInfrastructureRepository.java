/**
 *  Class HibernateInfrastructureRepository
 *
 */
package org.zfin.infrastructure.repository;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.zfin.ExternalNote;
import org.zfin.publication.Publication;
import org.zfin.expression.ExpressionAssay;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerType;
import org.zfin.people.Person;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

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


    //todo: add a getter here, or do some mapping to objects so that we can test the insert in a routine way
    public RecordAttribution insertRecordAttribution(String dataZdbID, String sourceZdbID) {
        Session session = HibernateUtil.currentSession();

        // need to return null if no valid publication string
        if( null==session.get(Publication.class,sourceZdbID)){
            logger.warn("try into insert record attribution with bad pub: " + sourceZdbID);
            return null ;
        }

        RecordAttribution ra = new RecordAttribution();
        ra.setDataZdbID(dataZdbID);
        ra.setSourceZdbID(sourceZdbID);
        ra.setSourceType(RecordAttribution.SourceType.STANDARD);

        session.save(ra);
        return ra ;
    }

    public PublicationAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID) {
        return insertPublicAttribution(dataZdbID,sourceZdbID,RecordAttribution.SourceType.STANDARD) ;
    }

    public PublicationAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID, RecordAttribution.SourceType sourceType) {
        Session session = HibernateUtil.currentSession();

        PublicationAttribution publicationAttribution = new PublicationAttribution();
        publicationAttribution.setDataZdbID(dataZdbID);
        publicationAttribution.setSourceZdbID(sourceZdbID);
        Publication publication = (Publication) session.get(Publication.class,sourceZdbID) ;
        publicationAttribution.setPublication(publication);
        publicationAttribution.setSourceType(sourceType);

        session.save(publicationAttribution);
        return publicationAttribution ;
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

    public List<PublicationAttribution> getPublicationAttributions(String dblinkZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PublicationAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", dblinkZdbID));
        return criteria.list();
    }


    public PublicationAttribution getStandardPublicationAttribution(String dataZdbID,String pubZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(PublicationAttribution.class) ;
        criteria.add(Restrictions.eq("dataZdbID",dataZdbID)) ;
        criteria.add(Restrictions.eq("sourceZdbID",pubZdbID)) ;
        criteria.add(Restrictions.eq("sourceType", RecordAttribution.SourceType.STANDARD.toString())) ;
        return (PublicationAttribution) criteria.uniqueResult() ;
    }
//
//    public RecordAttribution getRecordAttribution(String zdbID){
//        Session session = HibernateUtil.currentSession();
//        Criteria criteria = session.createCriteria(RecordAttribution.class);
//        criteria.add(Restrictions.eq("zdbID", zdbID));
//        return (RecordAttribution) criteria.uniqueResult() ;
//    }
//
//    public void deleteRecordAttribution(RecordAttribution recordAttribution){
//        Session session = HibernateUtil.currentSession();
//        session.delete(recordAttribution);
//    }

    public void insertUpdatesTable(String recID, String fieldName, String new_value, String comments, String submitterID, String submitterName) {
        Session session = HibernateUtil.currentSession();

        Updates up = new Updates();
        Date date = new Date();
        up.setRecID(recID);
        up.setFieldName(fieldName);
        up.setSubmitterID(submitterID);
        up.setSubmitterName(submitterName);
        up.setComments(comments);
        up.setNewValue(new_value);
        up.setWhenUpdated(date);
        session.save(up);
    }

    public void insertUpdatesTable(String recID, String fieldName, String comments, String newValue,String oldValue) {
        Session session = HibernateUtil.currentSession();

        Updates update = new Updates();
        update.setRecID(recID);
        update.setFieldName(fieldName);
//        update.setSubmitterID(submitter.getZdbID());
//        update.setSubmitterName(submitter.getFullName());
        update.setComments(comments);
        update.setNewValue(newValue);
        update.setOldValue(oldValue);
        update.setWhenUpdated(new Date() );
        session.save(update);
    }

    public void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person, String newValue, String oldValue) {
        Session session = HibernateUtil.currentSession();

        Updates up = new Updates();
        up.setRecID(marker.getZdbID());
        up.setFieldName(fieldName);
        if(person!=null){
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
     * @param marker
     * @param fieldName
     * @param comments
     * @param person
     */
    public void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person) {
        insertUpdatesTable(marker,fieldName,comments,person,marker.getAbbreviation(),"");
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
        String hql ="" +
                "delete from RecordAttribution ra where ra.dataZdbID in (:dataZdbIDs)" ;
        Query query = session.createQuery(hql) ;
        query.setParameterList("dataZdbIDs",dataZdbIDs);
        return query.executeUpdate() ;

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

    @SuppressWarnings("unchecked")
    public List<String> getDataAliasesWithAbbreviation(String zdbID) {
        Session session = HibernateUtil.currentSession();
        SQLQuery sqlQuery = session.createSQLQuery("select get_obj_abbrev(dalias_data_zdb_id) as abbreviation " +
                "from data_alias where dalias_alias_lower = :id ");
        sqlQuery.addScalar("abbreviation");
        sqlQuery.setParameter("id", zdbID);
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
                "select anatitem_zdb_id as zdb_id from anatomy_item " +
                "where anatitem_name_lower = :name ");
        sqlQuery.addScalar("zdbID");
        sqlQuery.setParameter("name", name.toLowerCase());
        return (List<String>) sqlQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionAssay> getAllAssays() {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(ExpressionAssay.class);
        crit.addOrder(Order.asc("displayOrder"));
        return (List<ExpressionAssay>) crit.list();
    }

}


