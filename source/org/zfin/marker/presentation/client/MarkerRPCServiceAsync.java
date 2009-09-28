package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.marker.presentation.dto.*;

import java.util.List;

/**
 */
public interface MarkerRPCServiceAsync {

    // publication access method
    void getPublicationAbstract(String zdbID, AsyncCallback<PublicationAbstractDTO> async);

    // note methods
    void addCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void editCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void removeCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void editPublicNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    // self-attribution
    void addMarkerAttribution(String zdbID, String pubZdbID, AsyncCallback<Void> async);

    void removeMarkerAttribution(String zdbID, String pubZdbID, AsyncCallback<Void> async);

    /**
     * @param zdbID ZdbID of Marker to return attributions for.
     * @return Returns accession numbers as strings.
     */
    void getMarkerAttributions(String zdbID, AsyncCallback<List<String>> async);

    // alias attribution list
    void addDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO, AsyncCallback<RelatedEntityDTO> async);

    void addDataAliasAttribution(RelatedEntityDTO relatedEntityDTO, AsyncCallback<RelatedEntityDTO> async);

    void removeDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO, AsyncCallback<Void> async);

    void removeDataAliasAttribution(RelatedEntityDTO relatedEntityDTO, AsyncCallback<Void> async);

    void addInternalProteinSequence(String markerZdbID, String sequence, String pubZdbID, String referenceZdbID, AsyncCallback<DBLinkDTO> async);

    void addInternalNucleotideSequence(String markerZdbID, String sequence, String pubZdbID, String referenceZdbID, AsyncCallback<DBLinkDTO> async);

    /**
     * @return List of dblinks
     */
    void getMarkerDBLinksForAccession(String accession, AsyncCallback<List<DBLinkDTO>> async);

    // MarkerDBLink methods
    void getDBLink(String zdbID, AsyncCallback<DBLinkDTO> async);

    void addDBLink(DBLinkDTO dbLinkDTO, List<ReferenceDatabaseDTO> referenceDatabaseDTOs, AsyncCallback<DBLinkDTO> async);

    void addSequenceAttribution(SequenceDTO dbLinkDTO, AsyncCallback<SequenceDTO> async);

    void addDBLinkAttribution(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void removeDBLink(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void removeDBLinkAttribution(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void updateDBLinkLength(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    // marker update
    void updateMarkerName(MarkerDTO markerDTO, AsyncCallback<Void> async);

    void getMarkerForName(String name, AsyncCallback<MarkerDTO> async);

    void getGeneForZdbID(String zdbID, AsyncCallback<MarkerDTO> async);

    // supplier methods
    /**
     * @return List of supplier names
     */
    void getAllSupplierNames(AsyncCallback<List<String>> async);

    void addMarkerSupplier(String name, String markerZdbID, AsyncCallback<Void> async);

    void removeMarkerSupplier(String name, String markerZdbID, AsyncCallback<Void> async);

    void getWebDriverPath(AsyncCallback<String> async);

    // general methods
    void addRelatedMarker(MarkerDTO markerDTO, AsyncCallback<MarkerDTO> async);

    void addRelatedMarkerAttribution(MarkerDTO markerDTO, AsyncCallback<MarkerDTO> async);

    void removeRelatedMarker(MarkerDTO markerDTO, AsyncCallback<Void> async);

    void removeRelatedMarkerAttribution(MarkerDTO markerDTO, AsyncCallback<Void> async);

    void validateDBLink(DBLinkDTO dbLinkDTO, AsyncCallback<String> async);
}
