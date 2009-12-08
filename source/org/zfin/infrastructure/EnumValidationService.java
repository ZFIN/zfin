package org.zfin.infrastructure;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Closure;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.*;
import org.zfin.mutant.Feature;
import org.zfin.mutant.Genotype;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.Origination;
import org.zfin.marker.Transcript;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Service validates java Enumerations versus their database counterparts for controlled vocabulary clases.
 */
public class EnumValidationService {

    protected static String ENUM_NOT_FOUND_IN_DATABASE = "Java enum not found in database: ";
    protected static String DATABASE_VALUE_NOT_FOUND_IN_JAVA = "Database value not mapped to java enum: ";
    protected static String DOMAIN = "Domain: ";
    protected static String DOMAIN_NAME = "DOMAIN_NAME";

    private static final Logger logger = Logger.getLogger(EnumValidationService.class);

    private StringBuilder report;

    public void checkAllEnums() throws EnumValidationException {
        logger.info("Begin validation");
        Method[] methods = this.getClass().getMethods();
        int count = 0;
        for (Method method : methods) {
            if (method.isAnnotationPresent(ServiceTest.class)) {
                try {
                    logger.info("running method: " + method.getName());
                    method.invoke(this, new Object[0]);
                    ++count;
                }
                catch (IllegalAccessException iae) {
                    if (iae.getCause() instanceof EnumValidationException) {
                        throw new EnumValidationException("test failed" + method.getName() + "\n"
                                + iae.getCause().getMessage(), iae);
                    }
                    logger.fatal("bad method exception", iae);
                    throw new EnumValidationException("exception from EnumValidationService on method " + method.getName(), iae);
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
        String hql = "select mt.name from MarkerType mt";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, Marker.Type.values());

        Criteria c = HibernateUtil.currentSession().createCriteria(MarkerType.class);
    }

    @ServiceTest
    public void validateMarkerTypeGroups() throws EnumValidationException {
        String hql = "select mtg.name from MarkerTypeGroup mtg";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, Marker.TypeGroup.values());
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
        Number countAll = ((Number) queryAll.uniqueResult());

        String hqlWithTypes = "select count(astat.zdbID) from AnatomyStatistics astat where astat.type in  (:types) ";
        Query queryBothTypes = session.createQuery(hqlWithTypes);
        List<String> types = new ArrayList<String>();
        for (AnatomyStatistics.Type type : AnatomyStatistics.Type.values()) {
            types.add(type.name());
        }
        queryBothTypes.setParameterList("types", types);
        Number countBothTypes = (Number) queryBothTypes.uniqueResult();
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
        String hql = "select attype_type from attribution_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, RecordAttribution.SourceType.values());
    }


    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateMarkerHistoryEvent() throws EnumValidationException {
        String hql = "select * from marker_history_event";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, MarkerHistory.Event.values());
    }


    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateMarkerHistoryReason() throws EnumValidationException {
        String hql = "select * from marker_history_reason";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, MarkerHistory.Reason.values());
    }

    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateMarkerRelationshipType() throws EnumValidationException {
        String hql = "select mreltype_name from marker_relationship_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, MarkerRelationship.Type.values());
    }


    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateFeatureType() throws EnumValidationException {
        String hql = "select ftrtype_name from feature_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, Feature.Type.values());
    }

    /**
     * @throws EnumValidationException
     */
    @ServiceTest
    public void validateOrthoEvidenceCode() throws EnumValidationException {
        String hql = "select code from EvidenceCode";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, OrthoEvidence.Code.values());
    }

    @ServiceTest
    public void validateRunType() throws EnumValidationException {
        String hql = "select *  from run_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, Run.Type.values());
    }

    @ServiceTest
    public void validateReferenceDatabaseType() throws EnumValidationException {
        String hql = "select fdbdt_data_type  from foreign_db_data_type group by fdbdt_data_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, ForeignDBDataType.DataType.values());
    }

    @ServiceTest
    public void validateReferenceDatabaseSuperType() throws EnumValidationException {
        String hql = "select fdbdt_super_type from foreign_db_data_type group by fdbdt_super_type";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, ForeignDBDataType.SuperType.values());
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
        checkEnumVersusDatabaseCollection(typeList, DataAliasGroup.Group.values());
    }

    @ServiceTest
    public void validateForeignDBNames() throws EnumValidationException {
        String hql = "select fdb.dbName from ForeignDB fdb";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, ForeignDB.AvailableName.values());
    }

    @ServiceTest
    public void validateCloneProblemType() throws EnumValidationException {
        String hql = "select cpt_type from clone_problem_type cpt";
        List<String> typeList = HibernateUtil.currentSession().createSQLQuery(hql).list();
        checkEnumVersusDatabaseCollection(typeList, Clone.ProblemType.values());
    }

    @ServiceTest
    public void validateTranscriptType() throws EnumValidationException {
        String sql = "select tt.type from TranscriptType tt";
        List<TranscriptType> typeList = HibernateUtil.currentSession().createQuery(sql).list();
        checkEnumVersusDatabaseCollection(typeList, TranscriptType.Type.values()) ;
    }

    @ServiceTest
    public void validateBlastDatabaseType() throws EnumValidationException {
        String sql = "select bd.type from Database bd group by bd.type ";
        List<Database.Type> typeList = HibernateUtil.currentSession().createQuery(sql).list();
        checkEnumVersusDatabaseCollection(typeList, Database.Type.values()) ;
    }

    @ServiceTest
    public void validateBlastDatabaseEnumeration() throws EnumValidationException {
        String sql = "select bd.abbrev from Database bd";
        List<Database.AvailableAbbrev> typeList = HibernateUtil.currentSession().createQuery(sql).list();
        checkEnumVersusDatabaseCollection(typeList, Database.AvailableAbbrev.values()) ;
    }

    @ServiceTest
    public void validateTranscriptStatus() throws EnumValidationException {
        String sql = "select ts.status from TranscriptStatus  ts";
        List<TranscriptStatus> typeList = HibernateUtil.currentSession().createQuery(sql).list();
        checkEnumVersusDatabaseCollection(typeList, TranscriptStatus.Status.values(),true) ;
    }

    @ServiceTest
    public void validateDisplayGroups() throws EnumValidationException {
        String sql = "select dg.groupName from DisplayGroup dg ";
        List<String> typeList = HibernateUtil.currentSession().createQuery(sql).list();
        checkEnumVersusDatabaseCollection(typeList, DisplayGroup.GroupName.values(),true) ;
    }

    @ServiceTest
    public void validateGenotypeWildtypeEnum() throws EnumValidationException {
        String hql = "select g.handle from Genotype g where g.wildtype = :isWildtype";
        List<String> typeList = HibernateUtil.currentSession().createQuery(hql).setBoolean("isWildtype",true).list();
        checkEnumValuesPresentInDatabase(typeList, Genotype.Wildtype.values());
    }

    @ServiceTest
    public void validateBlastOriginationTypes() throws EnumValidationException {
        String sql = "select o.type from Origination o";
        List<String> typeList = HibernateUtil.currentSession().createQuery(sql).list();
        checkEnumVersusDatabaseCollection(typeList, Origination.Type.values() ,true) ;
    }

    public <T extends Object> void checkEnumValuesPresentInDatabase(List<T> databaseList, Enum[] enumValues) {
        List<String> enumList = new ArrayList<String>();
        for (Enum wt : enumValues) {
            enumList.add(wt.toString());
        }
        for (String enumString : enumList) {
            if (!databaseList.contains(enumString)) {
                String message = "Wildtype " + enumString + " is present in the code but not in the database.";
                String reason = System.getProperty("line.separator") + "*******************************************************************************";
                reason += System.getProperty("line.separator");
                reason += "ENUMERATION VALIDATION REPORT: " + System.getProperty("line.separator") + message;
                reason += System.getProperty("line.separator") + "*******************************************************************************";
                logger.warn(reason);
                report.append(message);
            }
        }

    }


    public <T extends Object> void checkEnumVersusDatabaseCollection(List<T> list,Enum[] enumValues) throws EnumValidationException{
        checkEnumVersusDatabaseCollection(list,enumValues,false);
    }

    public <T extends Object> void checkEnumVersusDatabaseCollection(List<T> databaseList,Enum[] enumValues,boolean allowNullEnumValue)
            throws EnumValidationException
    {
        List<String> enumList = new ArrayList<String>() ;
        Enum enumType = null;
        for(Enum type : enumValues){
            if(type.toString() != null || allowNullEnumValue==false){
                enumList.add(type.toString()) ;
            enumType = type;
        }
        }

        List<String> databaseStringList = new ArrayList<String>() ;
        for(T type: databaseList){
            databaseStringList.add(type.toString());
        }

        String message = getCollectionDifferenceReport(enumList, databaseStringList, enumType.getClass());
        if (message != null) {
            String reason = System.getProperty("line.separator") + "*******************************************************************************";
            reason += System.getProperty("line.separator");
            reason += "ENUMERATION VALIDATION REPORT: " + System.getProperty("line.separator") + message;
            reason += System.getProperty("line.separator") + "*******************************************************************************";
            logger.warn(reason);
            if (report == null)
                report = new StringBuilder();
            report.append(message);
        }
    }


    /**
     * Retrieve the difference of two given collections.
     * The first collection is called the source collection while the
     * second collection is called target.
     *
     * @param enumList     Source Collection
     * @param databaseList Target Collection
     * @return report message. null if the collections are the same.
     */
    public static String getCollectionDifferenceReport(Collection<String> enumList, Collection<String> databaseList, Class clazz) {
        if (enumList == null && databaseList == null)
            return null;

        Collection<String> differences = CollectionUtils.disjunction(enumList, databaseList);
        if (differences == null){
            return null;
        }

        StringBuilder sb = new StringBuilder();
        // add unmatched enumList entries to report
        for (String name : differences) {
            if (enumList != null && enumList.contains(name)) {
                sb.append(ENUM_NOT_FOUND_IN_DATABASE);
                sb.append(name);
                sb.append(System.getProperty("line.separator"));
                sb.append("You may wish to add " + name + " to the database. The enumeration");
                sb.append(System.getProperty("line.separator"));
            }
        }
        // add unmatched databaseList entries to report
        for (String name : differences) {
            if (databaseList != null && databaseList.contains(name)) {
                sb.append(DATABASE_VALUE_NOT_FOUND_IN_JAVA);
                sb.append(name);
                sb.append(System.getProperty("line.separator"));
                sb.append("You may wish to add " + name + " to the class: " + clazz.getName());
                sb.append(System.getProperty("line.separator"));
            }
        }

        String domain = System.getenv(DOMAIN_NAME);
        if (sb.length() > 0) {
            if (domain != null) {
                sb.insert(0, DOMAIN + domain + "\n");
            }
            return sb.toString();
        } else {
            return null;
        }

    }

    public String getReport() {
        if (report == null)
            return null;
        return report.toString();
    }
}
