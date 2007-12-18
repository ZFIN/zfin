/**
 *  Class InfrastructureRepository
 */
package org.zfin.infrastructure.repository;

import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.marker.Marker;
import org.zfin.people.Person;

import java.util.List;

public interface InfrastructureRepository {

    public void insertActiveData(String zdbID);

    public void insertActiveSource(String zdbID);

    public ActiveData getActiveData(String zdbID);

    public void deleteActiveData(ActiveData activeData);

    public void deleteActiveDataByZdbID(String zdbID);

    public RecordAttribution getRecordAttribution(String dataZdbID,
                                                  String sourceZdbId,
                                                  RecordAttribution.SourceType sourceType);

    public List<RecordAttribution> getRecordAttributions(ActiveData data);

    // TODO: RecordAttribution has a composite primary key, so not needed just yet
    public void insertRecordAttribution(String dataZdbID, String sourceZdbID);

    public void insertUpdatesTable(String recID, String fieldName, String new_value, String comments, String submitterID, String submitterName);

    public void insertUpdatesTable(Marker marker, String fieldName, String comments, Person person);

    /**
     * Retrieve a dataNote by its zdb id
     *
     * @param zdbID zdb ID
     * @return DataNote
     */
    public DataNote getDataNoteByID(String zdbID);

    /**
     * Retrieve the Updates flag that indicates if the db is disabled for updates.
     *
     * @return zdbFlag
     */
    ZdbFlag getUpdatesFlag();
}


