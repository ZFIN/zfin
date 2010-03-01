/**
 *  Class InfrastructureRepository
 */
package org.zfin.infrastructure.repository;

import org.zfin.ExternalNote;
import org.zfin.expression.ExpressionAssay;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerType;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.TermRelationship;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

public interface InfrastructureRepository {

    void insertActiveData(String zdbID);

    void insertActiveSource(String zdbID);

    ActiveData getActiveData(String zdbID);

    void deleteActiveData(ActiveData activeData);

    void deleteActiveDataByZdbID(String zdbID);

    int deleteActiveDataByZdbID(List<String> zdbID);

    int deleteRecordAttributionsForData(String dataZdbID);

    int deleteRecordAttribution(String dataZdbID, String sourceZdbId);


    RecordAttribution getRecordAttribution(String dataZdbID,
                                           String sourceZdbId,
                                           RecordAttribution.SourceType sourceType);

    List<RecordAttribution> getRecAttribforFtrType(String dataZdbID);


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

    void insertUpdatesTable(String recID, String fieldName, String new_value, String comments, Person person );

    void insertUpdatesTable(String recID, String fieldName, String new_value, String comments, String submitterID, String submitterName);

    void insertUpdatesTable(String recID, String fieldName, String oldValue, String newValue, String comments);

    void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person, String newValue, String oldValue);

    void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person);
//    void deleteRecordAttribution(RecordAttribution recordAttribution);

    //  RecordAttribution getRecordAttribution(String zdbID);

    DataNote getDataNoteByID(String zdbID);

    MarkerAlias getMarkerAliasByID(String zdbID);

    int deleteRecordAttributionForPub(String zdbID);

    public int removeRecordAttributionForData(String zdbID, String datazdbID);

    int deleteRecordAttributionByDataZdbID(List<String> dataZdbIDs);

    int removeRecordAttributionForPub(String zdbID);

    /**
     * Retrieve the Updates flag that indicates if the db is disabled for updates.
     *
     * @return zdbFlag
     */
    ZdbFlag getUpdatesFlag();

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
     * @param zdbID zdb ID
     * @return List of data aliases
     */
    List<DataAlias> getDataAliases(String zdbID);

    /**
     * Retrieve all data aliases for a given zdbID. This method
     * also retrieves abbreviation info for an alias
     *
     * @param zdbID zdb ID
     * @return List of data aliases
     */
    List<String> getDataAliasesWithAbbreviation(String zdbID);

    /**
     * Retrieve anatomy terms (zdbIDs) by token.
     *
     * @param name string
     * @return list of strings
     */
    List<String> getAnatomyTokens(String name);

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
     * @param termName term name (contains)
     * @param ontology Ontology
     * @return list of GenericTerm
     */
    List<GenericTerm> getTermsByName(String termName, Ontology ontology);

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
    GenericTerm getTermByID(String termID);

    /**
     * Retrieve Term by OBO ID.
     *
     * @param termID term id
     * @return Generic Term
     */
    GenericTerm getTermByOboID(String termID);

    /**
     * Retrieve all related Terms.
     *
     * @param genericTerm term
     * @return list of relationships
     */
    List<TermRelationship> getTermRelationships(GenericTerm genericTerm);

    /**
     * Fetch a Data Alias Group entity for a given name
     *
     * @param name alias group object
     * @return DataAliasGroup entity
     */
    DataAliasGroup getDataAliasGroupByName(String name);
}




