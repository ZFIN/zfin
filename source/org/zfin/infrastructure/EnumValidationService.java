package org.zfin.infrastructure;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.Criteria;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.*;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.mutant.Feature;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.orthology.Species;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.ForeignDB;
import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Service validates java Enumerations versus their database counterparts for controlled vocabulary clases.
 */
public class EnumValidationService {

    protected static String ENUM_NOT_FOUND = "Java enum not found in database: " ; 
    protected static String DATABASE_VALUE_NOT_FOUND = "Database value not mapped to java enum: " ; 

    Logger logger = Logger.getLogger(EnumValidationService.class);

    public void checkAllEnums() throws EnumValidationException {
        logger.info("Begin validation");
        Method[] methods = this.getClass().getMethods();
        int count = 0;
        for (Method method : methods) {
            if( method.isAnnotationPresent(ServiceTest.class)){
                try {
                    logger.info("running method: " + method.getName());
                    method.invoke(this,new Object[0]);
                    ++count;
                }
                catch (IllegalAccessException iae) {
                    logger.fatal("bad method exception", iae);
                    throw new EnumValidationException("failed to called EnumValidationService: ", iae);
                }
                catch (InvocationTargetException ite) {
                    logger.fatal("bad method exception", ite);
                    throw new EnumValidationException("failed to call EnumValidationService: ", ite);
                } catch (Exception e) {
                    logger.fatal("tests failed", e);
                    throw new EnumValidationException("failed to call EnumValidationService: ", e);
                }
            }
        }
        logger.info("End validation of " + count + " enumerations");
    }

    @ServiceTest
    public void validateMarkerTypes() throws EnumValidationException {
        String hql = "select mt.name from MarkerType mt" ; 
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list() ;
        checkEnumVersusDatabaseCollection(typeList,Marker.Type.values()) ;

        Criteria c = HibernateUtil.currentSession().createCriteria(MarkerType.class) ;
    }

    @ServiceTest
    public void validateMarkerTypeGroups() throws EnumValidationException {
        String hql = "select mtg.name from MarkerTypeGroup mtg";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,Marker.TypeGroup.values()) ;
    }


    /**
     * Verify that every AnatomyStatistics is contained in AnatomyStatistics.Type.
     * // todo: create anatomy_statistics_type table
     * As there is no explicit anatomy statistics table, this table must exist this way.
     *
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateAnatomyStatisticsType() throws EnumValidationException {
        Session session = HibernateUtil.currentSession();
        String hqlAll = "select count(astat.zdbID) from AnatomyStatistics astat";
        Query queryAll = session.createQuery(hqlAll);
        Integer countAll = Integer.valueOf(queryAll.uniqueResult().toString());

        String hqlWithTypes = "select count(astat.zdbID) from AnatomyStatistics astat where astat.type in  (:types) ";
        Query queryBothTypes = session.createQuery(hqlWithTypes);
        List<String> types = new ArrayList<String>();
        for (AnatomyStatistics.Type type : AnatomyStatistics.Type.values()) {
            types.add(type.name());
        }
        queryBothTypes.setParameterList("types", types);
        Integer countBothTypes = Integer.valueOf(queryBothTypes.uniqueResult().toString());
//        logger.info("count["+ countBothTypes+"]");
        logger.info("number of antatomy statistics types: " + AnatomyStatistics.Type.values().length);

        if (countAll.intValue() != countBothTypes.intValue()) {
            throw new EnumValidationException("anatomystatistics for both types are not equal - " +
                    "all[" + countAll + "]" +
                    " bothtypes[" + countBothTypes + "]");
        }
    }


    @ServiceTest
    public void validateRecordAttributionSourceType() throws EnumValidationException {
        String hql = "select * from attribution_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,RecordAttribution.SourceType.values()) ;
    }


    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateMarkerHistoryEvent() throws EnumValidationException {
        String hql = "select * from marker_history_event";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,MarkerHistory.Event.values()) ;
    }


    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateMarkerHistoryReason() throws EnumValidationException {
        String hql = "select * from marker_history_reason";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,MarkerHistory.Reason.values()) ;
    }

    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateMarkerRelationshipType() throws EnumValidationException {
        String hql = "select mreltype_name from marker_relationship_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,MarkerRelationship.Type.values()) ;
    }


    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateFeatureType() throws EnumValidationException {
        String hql = "select ftrtype_name from feature_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,Feature.Type.values()) ;
    }

    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateOrthoEvidenceCode() throws EnumValidationException {
        String hql = "select code from EvidenceCode";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,OrthoEvidence.Code.values()) ;
    }

    @ServiceTest
    public void validateSpecies() throws EnumValidationException {
        String hql = "select organism_common_name from organism";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,Species.values()) ;
    }

    @ServiceTest
    public void validateRunType() throws EnumValidationException {
        String hql = "select *  from run_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,Run.Type.values()) ;
    }

    @ServiceTest
    public void validateReferenceDatabaseType() throws EnumValidationException {
        String hql = "select fdbdt_data_type  from foreign_db_data_type group by fdbdt_data_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,ReferenceDatabase.Type.values()) ;
    }

    @ServiceTest
    public void validateReferenceDatabaseSuperType() throws EnumValidationException {
        String hql = "select fdbdt_super_type from foreign_db_data_type group by fdbdt_super_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,ReferenceDatabase.SuperType.values()) ;
    }


    /**
     * Validate that all data_alias.dalias_group values have a match in the
     * DataAlias.Group Enumeration.
     *
     * @throws EnumValidationException thrown if there is a mismatch
     */
    @ServiceTest
    public void validateDataAliasGroup() throws EnumValidationException {
        String hql = "select dag.name from DataAliasGroup dag";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList,DataAlias.Group.values()) ;
    }

    @ServiceTest
    public void validateForeignDBNames() throws EnumValidationException {
        String hql = "select fdb.dbName from ForeignDB fdb";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, ForeignDB.AvailableName.values()) ;
    }


    public void checkEnumVersusDatabaseCollection(List<String> list,Enum[] enumValues)
        throws EnumValidationException
    {
        List<String> enumList = new ArrayList<String>() ;
        for(Enum type : enumValues){
            enumList.add(type.toString()) ;
        }

        String message = getCollectionDifferenceReport(enumList, list);
        if (message != null){
            throw new EnumValidationException(message);
        }

    }


    /**
     * Retrieve the difference of two given collections.
     * The first collection is called the source collection while the
     * second collection is called target.
     *
     * @param source Source Collection
     * @param target Target Collection
     * @return report message. null if the collections are the same.
     */
    public static String getCollectionDifferenceReport(Collection<String> source, Collection<String> target) {
        if (source == null && target == null)
            return null;

        Collection<String> difference = CollectionUtils.disjunction(source, target);
        if (CollectionUtils.isEmpty(difference))
            return null;

        StringBuilder sb = new StringBuilder();
        // add unmatched source entries to report
        for (String name : difference) {
            if (source != null && source.contains(name)) {
                sb.append(ENUM_NOT_FOUND);
                sb.append(name);
                sb.append(System.getProperty("line.separator"));
            }
        }
        // add unmatched target entries to report
        for (String name : difference) {
            if (target != null && target.contains(name)) {
                sb.append(DATABASE_VALUE_NOT_FOUND);
                sb.append(name);
                sb.append(System.getProperty("line.separator"));
            }
        }

        String domain = System.getProperty("DOMAIN_NAME",null) ; 
        if(sb.length()>0){
            if(domain != null){
                sb.insert(0,domain) ; 
            }
            return sb.toString(); 
        }else{
            return null ; 
        }

    }

}
