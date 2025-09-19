/**
 * Class InfrastructureRepository
 */
package org.zfin.infrastructure.repository;

import org.hibernate.Session;
import org.zfin.ExternalNote;
import org.zfin.database.UnloadInfo;
import org.zfin.database.presentation.Column;
import org.zfin.database.presentation.Table;
import org.zfin.expression.ExpressionAssay;
import org.zfin.framework.featureflag.FeatureFlag;
import org.zfin.framework.featureflag.PersonalFeatureFlag;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerHistory;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.profile.Person;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.publication.Publication;
import org.zfin.uniprot.persistence.UniProtRelease;
import org.zfin.util.DatabaseJdbcStatement;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface InfrastructureRepository {

    void insertActiveData(String zdbID);

    void insertActiveDataWithoutValidationIgnoreConflict(String zdbID);

    void insertActiveSource(String zdbID);

    ActiveData getActiveData(String zdbID);

    List<ActiveData> getAllActiveData(Set<String> zdbID);

    public ActiveSource getActiveSource(String zdbID);

    void deleteActiveData(ActiveData activeData);

    void deleteActiveSource(ActiveSource activeSource);

    void deleteActiveDataByZdbID(String zdbID);

    void deleteActiveSourceByZdbID(String zdbID);

    int deleteActiveDataByZdbID(List<String> zdbID);

    int deleteRecordAttributionsForData(String dataZdbID);

    int deleteRecordAttribution(String dataZdbID, String sourceZdbId);

    void removeRecordAttributionForType(String zdbID, String datazdbID);

    void removeRecordAttributionForTranscript(String zdbID, String datazdbID);

    int getGoRecordAttributions(String dataZdbID, String sourceZdbId);

    RecordAttribution getRecordAttribution(String dataZdbID,
                                           String sourceZdbId,
                                           RecordAttribution.SourceType sourceType);

    List<RecordAttribution> getRecordAttributionsForType(String dataZdbID, RecordAttribution.SourceType sourceType);
    List<RecordAttribution> getRecordAttributionsForPublicationAndType(String pubZdbID, RecordAttribution.SourceType sourceType);


    List<RecordAttribution> getRecordAttributions(ActiveData data);

    List<RecordAttribution> getRecordAttributions(String ActiveDataZdbID);

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
    PublicationAttribution getPublicationAttribution(Publication publication, String dataZdbID);

    PublicationAttribution getPublicationAttributionByID(long publicationAttributionID);

    // TODO: RecordAttribution has a composite primary key, so not needed just yet

    List<ActiveSource> getAllActiveSource(Set<String> zdbIDs);

    RecordAttribution insertRecordAttribution(String dataZdbID, String sourceZdbID);

    RecordAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID);

    void insertPublicAttribution(Genotype genotype, String sourceZdbID);

    PublicationAttribution insertStandardPubAttribution(String dataZdbID, Publication publication);

    RecordAttribution insertPublicAttribution(String dataZdbID, String sourceZdbID, RecordAttribution.SourceType sourceType);

    List<Updates> getUpdates(String zdbID);

    void insertUpdatesTable(String recID, String fieldName, String comments);

    void insertUpdatesTable(String recID, String fieldName, String new_value, String comments);

    void insertUpdatesTable(String recID, String fieldName, String oldValue, String newValue, String comments);

    void insertUpdatesTable(EntityZdbID entity, String fieldName, String comments, String newValue, String oldValue);

    void insertUpdatesTable(EntityZdbID entity, String fieldName, String comments);

    void insertUpdatesTableWithoutPerson(String recID, String fieldName, String oldValue, String newValue, String comments);

    void insertUpdatesTable(String recID, BeanFieldUpdate beanFieldUpdate);

    void insertUpdatesTable(String recID, List<BeanFieldUpdate> beanFieldUpdates);

    void insertUpdatesTable(EntityZdbID entity, BeanFieldUpdate beanFieldUpdate, String comment);


//    void deleteRecordAttribution(RecordAttribution recordAttribution);

    //  RecordAttribution getRecordAttribution(String zdbID);

    DataNote getDataNoteByID(String zdbID);

    MarkerAlias getMarkerAliasByID(String zdbID);

    DataAlias getDataAliasByID(String zdbID);

    void insertUpdatesTable(String recId, Person submitter, String fieldName, String oldValue, String newValue, String comments);

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

    FeatureFlag getFeatureFlag(String name);

    PersonalFeatureFlag getPersonalFeatureFlag(Person person, String flagName);

    void setPersonalFeatureFlag(Person person, String flagName, boolean enabled);

    void setFeatureFlag(String name, boolean enabled);

    /**
     * retrieve an external note by zdb ID (PK)
     *
     * @param zdbID String
     * @return external note
     */
    ExternalNote getExternalNoteByID(String zdbID);

    ExternalNote updateExternalNote(ExternalNote note, String text);

    ExternalNote updateExternalNote(ExternalNote note, String text, Publication publication);

    void deleteExternalNote(ExternalNote note);

    /**
     * Retrieves standard PublicationAttributions.
     *
     * @param dataZdbID
     * @param pubZdbID
     * @return
     */
    /**
     * Retrieve the replaced zdbID for a given zdbID.
     *
     * @param oldZdbID zdb ID
     * @return Replacement object
     */
    ReplacementZdbID getReplacementZdbId(String oldZdbID);

    /**
     * Bulk retrieve the replaced zdbID for a given zdbID.
     * @param oldZdbIDs list of ZDB IDs
     * @return all matching replaced objects
     */
    List<ReplacementZdbID> getAllReplacementZdbIds(List<String> oldZdbIDs);

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
     * Retrieves all Assays.
     *
     * @return list of assays.
     */
    List<ExpressionAssay> getAllAssays();

    List<PublicationAttribution> getPublicationAttributions(String dataZdbID);

    List<PublicationAttribution> getPublicationAttributions(String dataZdbID, RecordAttribution.SourceType type);

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
     * Fetch a Data Alias Group entity for a given name
     *
     * @param name alias group object
     * @return DataAliasGroup entity
     */
    DataAliasGroup getDataAliasGroupByName(String name);

    ControlledVocab getCVZdbIDByTerm(String cvTermName);

    int getDataAliasesAttributions(String zdbID, String pubZdbID);

    int getOrthologRecordAttributions(String zdbID, String pubZdbID);

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
     * Return a set of data from a native SELECT statement.
     *
     * @param statement jdbc query
     * @return list of strings
     */
    List<List<String>> executeNativeQuery(DatabaseJdbcStatement statement);

    /**
     * Used to execute a dynamic query, i.e. a query with a sub query.
     *
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

    List<String> getPublicationAttributionZdbIdsForType(String microarray_pub, Marker.Type markerType);

    int removeAttributionsNotFound(Collection<String> attributionsToRemove, String microarrayPub);

    int addAttributionsNotFound(Collection<String> attributionsToAdd, String microarrayPub);

    List<String> getPublicationAttributionsForPub(String microarrayPub);

    boolean hasStandardPublicationAttribution(String zdbID, String microarrayPub);

    boolean hasStandardPublicationAttributionForRelatedMarkers(String zdbID, String microarrayPub);

    List<String> retrieveMetaData(String table);

    /**
     * Retrieve the meta data for all columns of a given table.
     *
     * @param table table
     * @return list of column objects
     */
    List<Column> retrieveColumnMetaData(Table table);

    /**
     * execute SQL query for each provided data row individually (for debugging purposes).
     *
     * @param statement
     * @param data
     */
    void executeJdbcStatementOneByOne(DatabaseJdbcStatement statement, List<List<String>> data);

    List<List<String>> executeNativeQuery(DatabaseJdbcStatement statement, Session session);

    /**
     * Retrieve the date when the database was loaded from. For dev sites it's the date of the production database that
     * was used for loading.
     *
     * @return UnloadInfo of the production database.
     */
    UnloadInfo getUnloadInfo();

    int getGenotypeExpressionExperimentRecordAttributions(String zdbID, String pubZdbID);

    /**
     * Generic deletion of ActiveData and ActiveSource
     *
     * @param zdbID
     */
    void deleteActiveEntity(String zdbID);

    List<Publication> getTermReferences(GenericTerm term, String orderBy);

    void saveDataNote(DataNote note, Publication publication);

    void insertPublicAttribution(Genotype genotype, Publication publication);

    void insertRecordAttribution(Fish fish, Publication publication);

    long getDistinctPublicationsByData(String entityID);

    boolean isTermNameForControlledVocabExists(String cvTermName);

    boolean isForeignSpeciesForControlledVocabExists(String cvForeignSpecies);

    boolean isNameDefForControlledVocabExists(String cvNameDefinition);

    ControlledVocab getControlledVocabByNameAndSpecies(String termName, String foreignSpecies);

    ControlledVocab getControlledVocabByID(String zdbID);

    void insertMutationDetailAttribution(String dataZdbID, String publicationID);

    void deleteMutationDetailAttribution(String zdbID, String publicationZdbID);

    EntityZdbID getEntityByID(Class<? extends EntityZdbID> entity, String zdbID);

    void insertMarkerHistory(MarkerHistory history);

    void setDisableUpdatesFlag(boolean readonlyMode);

    String getWithdrawnZdbID(String oldZdbID);

    List<AnnualStats> getAnnualStats();

    List<ControlledVocab> getControlledVocabsForSpeciesByConstruct(Marker construct);

    void deletePubProcessingInfo(String zdbID);

    UniProtRelease getUniProtReleaseByDate(Date date);
    UniProtRelease getLatestUnprocessedUniProtRelease();

    UniProtRelease getLatestProcessedUniProtRelease();

    List<UniProtRelease> getAllUniProtReleases();

    UniProtRelease getUniProtReleaseByID(Long id);

    void insertUniProtRelease(UniProtRelease release);

    void updateUniProtRelease(UniProtRelease release);

    void upsertUniProtRelease(UniProtRelease release);
}




