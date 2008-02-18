/**
 *  Class HibernateInfrastructureRepository
 *
 */
package org.zfin.infrastructure.repository;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.people.Person;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Date;
import java.util.List;

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

    public ActiveData getActiveData(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ActiveData.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (ActiveData) criteria.uniqueResult();
    }


    //todo: add a getter here, or do some mapping to objects so that we can test the insert in a routine way
    public void insertRecordAttribution(String dataZdbID, String sourceZdbID) {
        Session session = HibernateUtil.currentSession();

        RecordAttribution ra = new RecordAttribution();
        ra.setDataZdbID(dataZdbID);
        ra.setSourceZdbID(sourceZdbID);
        ra.setSourceType(RecordAttribution.SourceType.STANDARD.toString());

        session.save(ra);
//        session.flush();
    }

    //retrieve a dataNote by its zdb_id
    public DataNote getDataNoteByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DataNote.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (DataNote) criteria.uniqueResult();
    }

    //retrieve a set of dataNotes by its data_zdb_id
    public DataNote getDataNoteByDataID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DataNote.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (DataNote) criteria.uniqueResult();
    }

    //    public RecordAttribution getRecordAttribution(String zdbID);

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


    public List<RecordAttribution> getRecordAttributions(ActiveData data) {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.eq("dataZdbID", data.getZdbID()));

        return criteria.list();
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

    public void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person) {
        Session session = HibernateUtil.currentSession();

        Updates up = new Updates();
        up.setRecID(marker.getZdbID());
        up.setFieldName(fieldName);
        up.setSubmitterID(person.getZdbID());
        up.setSubmitterName(person.getUsername());
        up.setComments(comments);
        up.setNewValue(marker.getAbbreviation());
        up.setWhenUpdated(new Date());
        session.save(up);
        String newline = System.getProperty("line.separator");
        Connection connection = session.connection();
        StringBuilder sb = new StringBuilder("CONNECTION: ");
        sb.append(newline);
        sb.append("hashcode: ");
        sb.append(connection.hashCode());
        sb.append(newline);
        sb.append(connection);
        sb.append(newline);
        try {
            sb.append(connection.getAutoCommit());
            sb.append(newline);
            SQLWarning warnings = connection.getWarnings();
            if (warnings != null)
                sb.append(warnings.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info(sb);
        session.flush();
    }

    public int deleteRecordAttributionForPub(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("delete from RecordAttribution ra where ra.dataZdbID=:zdbID");
        query.setParameter("zdbID", zdbID);
        return query.executeUpdate();
    }


    public int deleteRecordAttributionByDataZdbID(List<String> dataZdbIDs) {
        for (String zdbID : dataZdbIDs) {
            logger.debug("zdbID: " + zdbID);
        }
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RecordAttribution.class);
        criteria.add(Restrictions.in("dataZdbID", dataZdbIDs));
        List<RecordAttribution> recordAttributions = criteria.list();

        for (RecordAttribution recordAttribution : recordAttributions) {
            logger.info("deleting recordAttribution: " + recordAttribution);
            session.delete(recordAttribution);
            logger.info("DELETED recordAttribution: " + recordAttribution);
        }
        session.flush();
        return recordAttributions.size();

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

} 


