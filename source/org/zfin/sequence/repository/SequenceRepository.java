/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository;

import org.apache.commons.collections.map.MultiValueMap;
import org.zfin.Species;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.RelatedMarkerDBLinkDisplay;
import org.zfin.publication.Publication;
import org.zfin.sequence.*;
import org.zfin.sequence.presentation.AccessionPresentation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SequenceRepository {

    ForeignDB getForeignDBByName(ForeignDB.AvailableName dbName);

    ReferenceDatabase getReferenceDatabaseByID(String referenceDatabaseID);

    ReferenceDatabase getReferenceDatabase(ForeignDB.AvailableName foreignDBName,
                                           ForeignDBDataType.DataType type,
                                           ForeignDBDataType.SuperType superType,
                                           Species.Type organism);

    ReferenceDatabase getZebrafishSequenceReferenceDatabase(ForeignDB.AvailableName foreignDBName,
                                                            ForeignDBDataType.DataType type);

    Accession getAccessionByAlternateKey(String number, ReferenceDatabase... referenceDatabase);

    List<Accession> getAccessionsByNumber(String number);

    Map<String, MarkerDBLink> getUniqueMarkerDBLinks(ReferenceDatabase... referenceDatabases);

    MultiValueMap getMarkerDBLinks(ReferenceDatabase... referenceDatabases);

    Set<String> getAccessions(ReferenceDatabase... referenceDatabases);

    List<DBLink> getDBLinksForAccession(String accessionString);

    List<DBLink> getDBLinksForAccession(String accessionString, boolean include, ReferenceDatabase... referenceDatabases);

    List<MarkerDBLink> getMarkerDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases);

    List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, ReferenceDatabase... referenceDatabases);

    List<TranscriptDBLink> getTranscriptDBLinksForAccession(String accessionString, Transcript transcript);

    List<TranscriptDBLink> getTranscriptDBLinksForTranscript(Transcript transcript, ReferenceDatabase... referenceDatabases);

    DBLink getDBLinkByID(String zdbID);

    DBLink getDBLinkByAlternateKey(String accessionString, String dataZdbID, ReferenceDatabase referenceDatabases);
    DBLink getDBLinkByData(String dataZdbID,ReferenceDatabase referenceDatabase);
    FeatureDBLink getFeatureDBLinkByAlternateKey(String accessionString, String dataZdbID, ReferenceDatabase referenceDatabases);

    MarkerDBLink getSingleDBLinkForMarker (Marker marker, ReferenceDatabase referenceDatabase);

    List<MarkerDBLink> getDBLinksForMarker(Marker marker, ReferenceDatabase... referenceDatabases);

    List<MarkerDBLink> getDBLinksForMarkerExcludingReferenceDatabases(Marker marker, ForeignDBDataType.DataType refType,
                                                                      ReferenceDatabase... referenceDatabases);

    void addDBLinks(Collection<MarkerDBLink> dbLinksToAdd, Publication attributionPub, int commitChunk);

    int removeDBLinks(Collection<DBLink> dbLinksToRemove);

    int removeAccessionByNumber(String accessionNumber);

    MarkerDBLinkList getAllSequencesForMarkerAndType(Marker marker, ForeignDBDataType.DataType referenceDatabaseType);

    List<DBLink> getDBLinks(String accession, ReferenceDatabase... referenceDatabase);

    List<String> getGenbankSequenceDBLinks();

    MarkerDBLinkList getNonSequenceMarkerDBLinksForMarker(Marker marker);

    List<DBLink> getSummaryMarkerDBLinksForMarker(Marker marker);
    DBLink getAtlasDBLink(String markerZdbID, String referenceDBName);
    DBLink getDBLink(String markerZdbID, String accession, String referenceDBName);
    DBLink getDBLink(String featureZDbID,String accession);

    List<ReferenceDatabase> getReferenceDatabasesWithInternalBlast();

    Map<String, List<DBLink>> getDBLinksForAccessions(Collection<String> accessionNumbers);

    List<String> getGenbankCdnaDBLinks();

    Set<String> getGenbankXpatCdnaDBLinks();

    List<ReferenceDatabase> getSequenceReferenceDatabases(ForeignDB.AvailableName genbank, ForeignDBDataType.DataType genomic);

    /**
     * Retrieves all marker ids with sequence information (accession numbers)
     * @param firstNIds number of sequences to be returned
     * @return list of markers
     */
    List<String> getAllNSequences(int firstNIds);

    List<DBLink> getDBLinksForMarker(String zdbID, ForeignDBDataType.SuperType protein);


    int getNumberDBLinks(Marker marker);

    List<DBLink> getDBLinksForMarkerAndDisplayGroup(Marker marker, DisplayGroup.GroupName groupName);

    List<TranscriptDBLink> getTranscriptDBLinksForMarkerAndDisplayGroup(Transcript transcript, DisplayGroup.GroupName groupName);

    List<RelatedMarkerDBLinkDisplay> getDBLinksForFirstRelatedMarker(Marker marker, DisplayGroup.GroupName groupName, MarkerRelationship.Type... markerRelationshipTypes);

    List<RelatedMarkerDBLinkDisplay> getDBLinksForSecondRelatedMarker(Marker marker,  DisplayGroup.GroupName groupName, MarkerRelationship.Type...  markerRelationshipTypes);

    Collection<String> getDBLinkAccessionsForMarker(Marker marker, ForeignDBDataType.DataType dataType);

    Collection<String> getDBLinkAccessionsForEncodedMarkers(Marker marker, ForeignDBDataType.DataType dataType);

    Map<String,String> getGeoAccessionCandidates();

    List<MarkerDBLink> getWeakReferenceDBLinks(Marker m,MarkerRelationship.Type type1, MarkerRelationship.Type type2);

    /**
     * Retrieve a list of all accessions for a given database.
     * @param name foreign database
     * @return list of DBLink records.
     */
    List<DBLink> getDBLinks(ForeignDB.AvailableName name);

    /**
     * Retrieve the first numberOfRecords of all accessions for a given database.
     * @param name foreign database
     * @param numberOfRecords numberOfRecords
     * @return list of DBLink records.
     */
    List<DBLink> getDBLinks(ForeignDB.AvailableName name, int numberOfRecords);

    List<AccessionPresentation> getAccessionPresentation(ForeignDB.AvailableName name, Marker marker);

    List<DBLink> getDBLinksForAccession(Accession accesion);
    List<MarkerDBLink> getBlastableDBlinksForAccession(Accession accession);

    List<ReferenceDatabase> getReferenceDatabases(List<ForeignDB.AvailableName> availableNames, List<ForeignDBDataType.DataType> dataTypes, ForeignDBDataType.SuperType superType, Species.Type species);
}



