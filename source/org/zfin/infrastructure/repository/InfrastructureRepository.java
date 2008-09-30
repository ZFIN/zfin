/**
 *  Class InfrastructureRepository
 */
package org.zfin.infrastructure.repository;

import org.zfin.ExternalNote;
import org.zfin.infrastructure.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerType;
import org.zfin.people.Person;

import java.util.List;

public interface InfrastructureRepository {

    void insertActiveData(String zdbID);

    void insertActiveSource(String zdbID);

    ActiveData getActiveData(String zdbID);

    void deleteActiveData(ActiveData activeData);

    void deleteActiveDataByZdbID(String zdbID);

    int deleteActiveDataByZdbID(List<String> zdbID);

    RecordAttribution getRecordAttribution(String dataZdbID,
                                           String sourceZdbId,
                                           RecordAttribution.SourceType sourceType);

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
    void insertRecordAttribution(String dataZdbID, String sourceZdbID);

    void insertUpdatesTable(String recID, String fieldName, String new_value, String comments, String submitterID, String submitterName);

    void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person,String newValue,String oldValue);
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
     * @param string search string
     * @return list of AllNamesFastSearch
     */
    List<AllNamesFastSearch> getAllNameMarkerMatches(String string);

    /**
     * Retrieves Markers that match a given string (contains) for a specified marker type.
     * @param string search string
     * @param type marker type
     * @return list of AllNamesFastSearch
     */
    List<AllMarkerNamesFastSearch> getAllNameMarkerMatches(String string, MarkerType type);
}




