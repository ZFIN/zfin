/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository;

import org.apache.commons.collections.map.MultiValueMap;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.orthology.Species;
import org.zfin.publication.Publication;
import org.zfin.sequence.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SequenceRepository {

    ForeignDB getForeignDBByName(ForeignDB.AvailableName dbName);

    ReferenceDatabase getReferenceDatabase(ForeignDB.AvailableName foreignDBName,
                                           ForeignDBDataType.DataType type,
                                           ForeignDBDataType.SuperType superType,
                                           Species organism);

    ReferenceDatabase getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName foreignDBName,
                                                            ForeignDBDataType.DataType type);

    Accession getAccessionByAlternateKey(String number, ReferenceDatabase... referenceDatabase);

    List<Accession> getAccessionsByNumber(String number);

    Map<String, MarkerDBLink> getUniqueMarkerDBLinks(ReferenceDatabase... referenceDatabases);

    MultiValueMap getMarkerDBLinks(ReferenceDatabase... referenceDatabases);

    List<DBLink> getDBLinksForAccession(String accessionString);

    List<DBLink> getDBLinksForAccession(String accessionString, boolean include, ReferenceDatabase... referenceDatabases);

    List<MarkerDBLink> getMarkerDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases);

    List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases);

    List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, Transcript transcript);

    List<TranscriptDBLink> getTranscriptDBLinksForTranscript(Transcript transcript, ReferenceDatabase... referenceDatabases);

    DBLink getDBLinkByID(String zdbID);

    DBLink getDBLinkByAlternateKey(String accessionString, String dataZdbID, ReferenceDatabase referenceDatabases);

    List<MarkerDBLink> getDBLinksForMarker(Marker marker, ReferenceDatabase... referenceDatabases);

    List<MarkerDBLink> getDBLinksForMarkerExcludingReferenceDatabases(Marker marker, ForeignDBDataType.DataType refType,
                                                                      ReferenceDatabase... referenceDatabases);

    void addDBLinks(Collection<MarkerDBLink> dbLinksToAdd, Publication attributionPub, int commitChunk);

    int removeDBLinks(Set<DBLink> dbLinksToRemove);

    int removeAccessionByNumber(String accessionNumber);

    MarkerDBLinkList getAllSequencesForMarkerAndType(Marker marker, ForeignDBDataType.DataType referenceDatabaseType);
//    TreeSet<MarkerDBLink> getSequenceDBLinksForMarker(Marker marker) ;

    //    TreeSet<TranscriptDBLink> getSequenceDBLinksForTranscript(Transcript transcript) ;
    List<DBLink> getDBLinks(String accession, ReferenceDatabase... referenceDatabase);


    MarkerDBLinkList getNonSequenceMarkerDBLinksForMarker(Marker marker);

    MarkerDBLinkList getSummaryMarkerDBLinksForMarker(Marker marker);

    DBLink getDBLink(String markerZdbID, String accession, String referenceDBName);

    List<ReferenceDatabase> getReferenceDatabasesWithInternalBlast();

    Map<String, List<DBLink>> getDBLinksForAccessions(Collection<String> accessionNumbers);


}



