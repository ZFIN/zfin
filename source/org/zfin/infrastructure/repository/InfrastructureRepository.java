/**
 *  Class InfrastructureRepository
 */
package org.zfin.infrastructure.repository;

import org.hibernate.Session;
import org.zfin.ExternalNote;
import org.zfin.database.UnloadInfo;
import org.zfin.database.presentation.Column;
import org.zfin.database.presentation.Table;
import org.zfin.expression.ExpressionAssay;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerType;
import org.zfin.mutant.GenotypeExternalNote;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.publication.Publication;
import org.zfin.util.DatabaseJdbcStatement;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface InfrastructureRepository {

    void insertActiveData(String zdbID);

    void insertActiveSource(String zdbID);

    ActiveData getActiveData(String zdbID);

    public ActiveSource getActiveSource(String zdbID);

    void deleteActiveData(ActiveData activeData);

    void deleteActiveSource(ActiveSource activeSource);

    void deleteActiveDataByZdbID(String zdbID);

    void deleteActiveSourceByZdbID(String zdbID);

    int deleteActiveDataByZdbID(List<String> zdbID);

    int deleteRecordAttributionsForData(String dataZdbID);

    int deleteRecordAttribution(String dataZdbID, String sourceZdbId);

    void removeRecordAttributionForType(String zdbID, String datazdbID);

    int getGoRecordAttributions(String dataZdbID, String sourceZdbId);

    RecordAttribution getRecordAttribution(String dataZdbID,
                                           String sourceZdbId,
                                           RecordAttribution.SourceType sourceType);

    List<RecordAttribution> getRecAttribforFtrType(String dataZdbID);

    List<RecordAttribution> getRecordAttributionsForType(String dataZdbID, RecordAttribution.SourceType sourceType);


    List<RecordAttribution> getRecordAttributions(ActiveData data);

    /**
     * Retrieve a single record attribution record.
     * Since this table has a composite key you need to provide three parameters:
     * 1) Data ZDB ID
     * 2) Source ZDB ID
     * 3) Source type
     *
     * @param data   Active data id
     * @param source Active data id
     * @param type   source type
     * @return Record Attribution
     */
    RecordAttribution getRecordAttribution(ActiveData data, ActiveSource source, RecordAttribution.SourceType type);

    /**
     * Retrieve a publication attribution by PK.
     * Since this table has a composite key you need to provide three parameters:
     * 1) Data ZDB ID
     * 2) Source ZDB ID
     * 3) Source type
     *
     * @param attribution Publication Attribution
     * @return Publication Attribution
     */
    PublicationAttribution getPublicationAttribution(PublicationAttribution attribution);

    // TODO: RecordAttribution has a composite primary key, so not needed just yet

    RecordAttribution insertRecordAttribution(String dataZdbID, String sourceZdbID);

    PublicationAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID);

    PublicationAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID, RecordAttribution.SourceType sourceType);

    void insertUpdatesTable(String recID, String comments, String submitterZdbID,Date updateDate);

    void insertUpdatesTable(String recID, String fieldName, String comments);

    void insertUpdatesTable(String recID, String fieldName, String new_value, String comments);

    void insertUpdatesTable(String recID, String fieldName, String oldValue, String newValue, String comments);

    void insertUpdatesTable(EntityZdbID entity, String fieldName, String comments, String newValue, String oldValue);

    void insertUpdatesTable(EntityZdbID entity, String fieldName, String comments);

    void insertUpdatesTable(String recID, BeanFieldUpdate beanFieldUpdate);


//    void deleteRecordAttribution(RecordAttribution recordAttribution);

    //  RecordAttribution getRecordAttribution(String zdbID);

    DataNote getDataNoteByID(String zdbID);

    MarkerAlias getMarkerAliasByID(String zdbID);

    DataAlias getDataAliasByID(String zdbID);

    int deleteRecordAttributionForPub(String zdbID);

    int removeRecordAttributionForData(String datazdbID, String pubZdbID);

    int deleteRecordAttributionByDataZdbIDs(List<String> dataZdbIDs);

    int removeRecordAttributionForPub(String zdbID);

    /**
     * Retrieve the Updates flag that indicates if the db is disabled for updates.
     *
     * @return zdbFlag
     */
    ZdbFlag getUpdatesFlag();

    boolean getDisableUpdatesFlag();

    /**
     * retrive an external note by zdb ID (PK)
     *
     * @param zdbID String
     * @return external note
     */
    ExternalNote getExternalNoteByID(String zdbID);

    /**
     * Retrieves Markers that match a given string (contains).
     *
     * @param string search string
     * @return list of AllNamesFastSearch
     */
    List<AllNamesFastSearch> getAllNameMarkerMatches(String string);

    /**
     * Retrieves Markers that match a given string (contains) for a specified marker type.
     *
     * @param string search string
     * @param type   marker type
     * @return list of AllNamesFastSearch
     */
    List<AllMarkerNamesFastSearch> getAllNameMarkerMatches(String string, MarkerType type);


    /**
     * Retrieves standard PublicationAttributions.
     *
     * @param dataZdbID
     * @param pubZdbID
     * @return
     */
    PublicationAttribution getStandardPublicationAttribution(String dataZdbID, String pubZdbID);

    /**
     * Retrieve the replaced zdbID for a given zdbID.
     *
     * @param oldZdbID zdb ID
     * @return Replacement object
     */
    ReplacementZdbID getReplacementZdbId(String oldZdbID);

    /**
     * Retrieve all data aliases for a given zdbID.
     *
     * @param aliasLowerName Lower-case alias name.
     * @return List of data aliases
     */
    List<DataAlias> getDataAliases(String aliasLowerName);

    /**
     * Retrieve all data aliases for a given zdbID. This method
     * also retrieves abbreviation info for an alias
     *
     * @param aliasLowerName Lower-case alias name.
     * @return List of data aliases
     */
    List<String> getDataAliasesWithAbbreviation(String aliasLowerName);

    /**
     * Retrieve zdbIDs from entities that best match a given name
     *
     * @param name string
     * @return list of strings
     */
    List<String> getBestNameMatch(String name);

    /**
     * Retrieves all Assays.
     *
     * @return list of assays.
     */
    List<ExpressionAssay> getAllAssays();

    List<PublicationAttribution> getPublicationAttributions(String dblinkZdbID);

    /**
     * Retrieves all data alias groups
     *
     * @return list of data alias groups
     */
    List<DataAliasGroup> getAllDataAliasGroups();

    /**
     * Retrieve terms by name (contains) and ontology.
     * No obsolete terms are included.
     *
     * @param termName   term name (contains)
     * @param ontologies Ontology
     * @return list of GenericTerm
     */
    List<GenericTerm> getTermsByName(String termName, List<Ontology> ontologies);

    /**
     * Retrieve terms by synonym match.
     *
     * @param queryString synonym name
     * @param ontology    name
     * @return list of terms
     */
    List<GenericTerm> getTermsBySynonymName(String queryString, Ontology ontology);

    /**
     * Retrieve a single term by name and ontology. If more than one term is found
     * an exception is thrown.
     *
     * @param termName name
     * @param ontology Ontology
     * @return Term
     */
    GenericTerm getTermByName(String termName, Ontology ontology);

    /**
     * Retrieve Term by ZDB ID.
     *
     * @param termID term id
     * @return Generic Term
     */
    public GenericTerm getTermByID(String termID);

    /**
     * Retrieve a single term by name and a list of ontologies. Checks for all ontologies and picks the first one.
     * Hopefully, there term is only found in a single ontology. Match has to be exact.
     *
     * @param termName   name
     * @param ontologies Ontology
     * @return Term
     */
    GenericTerm getTermByName(String termName, List<Ontology> ontologies);

    /**
     * Fetch a Data Alias Group entity for a given name
     *
     * @param name alias group object
     * @return DataAliasGroup entity
     */
    DataAliasGroup getDataAliasGroupByName(String name);
    ControlledVocab getCVZdbIDByTerm(String cvTermName);

    /**
     * Retrieve Root of given ontology.
     *
     * @param ontologyName ontology name
     * @return Term
     */
    GenericTerm getRootTerm(String ontologyName);

    int getDataAliasesAttributions(String zdbID, String pubZdbID);

    int getOrthologueRecordAttributions(String zdbID, String pubZdbID);

    int getMarkerFeatureRelationshipAttributions(String zdbID, String pubZdbID);

    int getMarkerGenotypeFeatureRelationshipAttributions(String zdbID, String pubZdbID);

    int getFeatureGenotypeAttributions(String zdbID, String pubZdbID);

    int getDBLinkAttributions(String zdbID, String pubZdbID);

    int getDBLinkAssociatedToGeneAttributions(String zdbID, String pubZdbID);

    int getFirstMarkerRelationshipAttributions(String zdbID, String pubZdbID);

    int getSecondMarkerRelationshipAttributions(String zdbID, String pubZdbID);

    int getExpressionExperimentMarkerAttributions(Marker marker, String pubZdbID);

    int getSequenceTargetingReagentEnvironmentAttributions(String zdbID, String pubZdbID);

    int getGenotypeExperimentRecordAttributions(String zdbID, String pubZdbID);

    /**
     * Number of phenotype experiments a genotype is being used.
     *
     * @param genotypeID    genotype
     * @param publicationID publication
     * @return number of references
     */
    int getGenotypePhenotypeRecordAttributions(String genotypeID, String publicationID);

    String getReplacedZdbID(String oldZdbID);

    List<ReplacementZdbID> getReplacedZdbIDsByType(ActiveData.Type type);

    String getNewZdbID(String withdrawnZdbID);

    /**
     * Execute a sql statement through straight JDBC call.
     *
     * @param statement query
     * @return number from sql query: # of updated records, inserted records, deleted records.
     */
    int executeJdbcStatement(DatabaseJdbcStatement statement);

    /**
     * Execute a sql statement through straight JDBC call and inserting given string data.
     *
     * @param statement query
     * @param data      string data
     */
    void executeJdbcStatement(DatabaseJdbcStatement statement, List<List<String>> data);

    /**
     * Execute a sql statement through straight JDBC call and inserting given string data.
     *
     * @param statement query
     * @param data      string data
     * @param batchSize size of of individual batches.
     */
    void executeJdbcStatement(DatabaseJdbcStatement statement, List<List<String>> data, int batchSize);

    /**
     * Execute a query with a native JDBC call
     *
     * @param query query string
     */
    void executeJdbcQuery(String query);

    /**
     * Return a set of data from a native SELECT statement.
     *
     * @param statement jdbc query
     * @return list of strings
     */
    List<List<String>> executeNativeQuery(DatabaseJdbcStatement statement);

    /**
     * Used to execute a dynamic query, i.e. a query with a sub query.
     * @param statement
     * @return
     */
    List<List<String>> executeNativeDynamicQuery(DatabaseJdbcStatement statement);

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
    List<String> getAllEntities(Class clazz, String idName, int firstNIds);

    List<String> getExternalOrthologyNoteStrings(String zdbID);

    List<ExternalNote> getExternalNotes(String zdbID);

    List<String> getPublicationAttributionZdbIdsForType(String microarray_pub, Marker.Type markerType);

    int removeAttributionsNotFound(Collection<String> attributionsToRemove, String microarrayPub);

    int addAttributionsNotFound(Collection<String> attributionsToAdd, String microarrayPub);

    List<String> getPublicationAttributionsForPub(String microarrayPub);

    boolean hasStandardPublicationAttribution(String zdbID, String microarrayPub);

    boolean hasStandardPublicationAttributionForRelatedMarkers(String zdbID, String microarrayPub);

    List<String> retrieveMetaData(String table);

    /**
     * Retrieve the meta data for all columns of a given table.
     * @param table table
     * @return list of column objects
     */
    List<Column> retrieveColumnMetaData(Table table);

    /**
     * execute SQL query for each provided data row individually (for debugging purposes).
     * @param statement
     * @param data
     */
    void executeJdbcStatementOneByOne(DatabaseJdbcStatement statement, List<List<String>> data);

    List<List<String>> executeNativeQuery(DatabaseJdbcStatement statement, Session session);

    /**
     * Retrieve the date when the database was loaded from. For dev sites it's the date of the production database that
     * was used for loading.
     * @return UnloadInfo of the production database.
     */
    UnloadInfo getUnloadInfo();

    int getGenotypeExpressionExperimentRecordAttributions(String zdbID, String pubZdbID);

    /**
     * Generic deletion of ActiveData and ActiveSource
     * @param zdbID
     */
    void deleteActiveEntity(String zdbID);

    List<Publication> getTermReferences(GenericTerm term, String orderBy);

    void saveExternalNote(GenotypeExternalNote note, Publication publication);

    void saveDataNote(DataNote note, Publication publication);
}




