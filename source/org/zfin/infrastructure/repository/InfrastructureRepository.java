/**
 *  Class InfrastructureRepository
 */
package org.zfin.infrastructure.repository ; 

import org.zfin.infrastructure.*;
import org.zfin.people.Person;
import org.zfin.marker.Marker;
import org.zfin.sequence.Accession;

import java.util.List;
import java.util.Set;

public interface InfrastructureRepository{

    public void insertActiveData(String zdbID);

    public void insertActiveSource(String zdbID);

    public ActiveData getActiveData(String zdbID);
    public void deleteActiveData(ActiveData activeData);
    public void deleteActiveDataByZdbID(String zdbID);
    public int deleteActiveDataByZdbID(List<String> zdbID);
    public RecordAttribution getRecordAttribution(String dataZdbID,
                                                  String sourceZdbId,
                                                  RecordAttribution.SourceType sourceType);
    public List<RecordAttribution> getRecordAttributions(ActiveData data);
    // TODO: RecordAttribution has a composite primary key, so not needed just yet
    public void insertRecordAttribution(String dataZdbID, String sourceZdbID);
    public void insertUpdatesTable(String recID, String fieldName, String new_value,String comments, String submitterID, String submitterName);
    public void insertUpdatesTable(Marker marker, String fieldName,String comments, Person person);
//    public void deleteRecordAttribution(RecordAttribution recordAttribution);
//    public RecordAttribution getRecordAttribution(String zdbID);
    public DataNote getDataNoteByID(String zdbID);
    public int deleteRecordAttributionForPub(String zdbID);
    public int deleteRecordAttributionByDataZdbID(List<String> dataZdbIDs);
    public int removeRecordAttributionForPub(String zdbID);

    /**
     * Retrieve the Updates flag that indicates if the db is disabled for updates.
     *
     * @return zdbFlag
     */
    ZdbFlag getUpdatesFlag();

}




