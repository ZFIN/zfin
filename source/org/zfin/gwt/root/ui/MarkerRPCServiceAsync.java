package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 */
public interface MarkerRPCServiceAsync {

    // note methods

    void addCuratorNote(NoteDTO noteDTO, AsyncCallback<NoteDTO> async);

    void editCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void removeCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void editPublicNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    // self-attribution

    void addAttribution(String zdbID, String pubZdbID, AsyncCallback<Void> async);

    void removeAttribution(String zdbID, String pubZdbID, AsyncCallback<String> async);

    // alias attribution list

    void addDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO, AsyncCallback<RelatedEntityDTO> async);

    void addDataAliasAttribution(RelatedEntityDTO relatedEntityDTO, AsyncCallback<RelatedEntityDTO> async);

    void removeDataAliasRelatedEntity(RelatedEntityDTO relatedEntityDTO, AsyncCallback<Void> async);

    void removeDataAliasAttribution(RelatedEntityDTO relatedEntityDTO, AsyncCallback<Void> async);

    void addInternalProteinSequence(String markerZdbID, String sequence, String pubZdbID, String referenceZdbID, AsyncCallback<DBLinkDTO> async);

    void addInternalNucleotideSequence(String markerZdbID, String sequence, String pubZdbID, String referenceZdbID, AsyncCallback<DBLinkDTO> async);

    void addDBLink(DBLinkDTO dbLinkDTO, List<ReferenceDatabaseDTO> referenceDatabaseDTOs, AsyncCallback<DBLinkDTO> async);

    void addSequenceAttribution(SequenceDTO dbLinkDTO, AsyncCallback<SequenceDTO> async);

    void addDBLinkAttribution(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void removeDBLink(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void removeDBLinkAttribution(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    void updateDBLinkLength(DBLinkDTO dbLinkDTO, AsyncCallback<DBLinkDTO> async);

    // marker update

    void getMarkerForName(String name, AsyncCallback<MarkerDTO> async);

    void getGeneOnlyForZdbID(String zdbID, AsyncCallback<MarkerDTO> async);

    void getGeneForZdbID(String zdbID, AsyncCallback<MarkerDTO> async);

    // supplier methods

    /**
     * Returns list of supplier names
     *
     * @param async Callback list.
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

    void addExternalNote(NoteDTO noteDTO, AsyncCallback<NoteDTO> async);

    void editExternalNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void removeExternalNote(NoteDTO noteDTO, AsyncCallback<Void> asyncCallback);

    void updateMarkerHeaders(MarkerDTO markerDTO, AsyncCallback<Void> async);

    void getGeneDBLinkAddReferenceDatabases(String markerZdbID, AsyncCallback<List<ReferenceDatabaseDTO>> asyncCallback);

    void addAttributionForMarkerName(String value, String pubZdbID, AsyncCallback<Void> markerEditCallBack);

    void addAttributionForFeatureName(String value, String publicationZdbID, AsyncCallback<Void> featureEditCallBack);
}
