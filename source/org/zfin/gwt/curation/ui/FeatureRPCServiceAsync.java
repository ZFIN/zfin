package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 */
public interface FeatureRPCServiceAsync {

    void getLabsOfOriginWithPrefix(AsyncCallback<List<OrganizationDTO>> async);

    void getPrefix(String name, AsyncCallback<List<FeaturePrefixDTO>> async);

    void createFeature(FeatureDTO featureDTO, AsyncCallback<FeatureDTO> async);

    void editFeatureDTO(FeatureDTO featureDTO, AsyncCallback<FeatureDTO> async);

    void getFeature(String zdbID, AsyncCallback<FeatureDTO> async);

    void deleteFeature(String zdbID, AsyncCallback<Void> async);

    void getFeaturesForPub(String pubZdbID, AsyncCallback<List<FeatureDTO>> async);

    void getFeatureMarkerRelationshipsForPub(String publicationZdbID, AsyncCallback<List<FeatureMarkerRelationshipDTO>> featureEditCallBack);

    void deleteFeatureMarkerRelationship(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO, AsyncCallback<Void> featureEditCallBack);

    void getRelationshipTypesForFeatureType(FeatureTypeEnum ftrTypeDisplay, AsyncCallback<List<String>> featureEditCallBack);

    void getMarkersForFeatureRelationAndSource(String featureTypeName, String publicationZdbID, AsyncCallback<List<MarkerDTO>> featureEditCallBack);

    void addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO, String publicationID, AsyncCallback<List<FeatureMarkerRelationshipDTO>> featureEditCallBack);

    void addFeatureAlias(String name, String ftrZdbID, String pubZdbID, AsyncCallback<Void> async);

    void removeFeatureAlias(String name, String ftrZdbID, AsyncCallback<Void> async);

    void editPublicNote(NoteDTO noteDTO, AsyncCallback<FeatureDTO> async);

    void addCuratorNote(CuratorNoteDTO noteDTO, AsyncCallback<CuratorNoteDTO> markerEditCallBack);

    void editCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void removeCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);

    void addFeatureSequence(String name, String ftrZdbID, String pubZdbID, AsyncCallback<Void> async);

    void removeFeatureSequence(String name, String ftrZdbID, AsyncCallback<Void> async);

    void getMutagensForFeatureType(FeatureTypeEnum ftrType, AsyncCallback<List<String>> featureEditCallBack);

    void removePublicNote(NoteDTO updatedNoteDTO, AsyncCallback<Void> featureEditCallBack);

    void getCuratorInfo(AsyncCallback<PersonDTO> personDTOZfinAsyncCallback);

    void getDnaChangeList(AsyncCallback<List<MutationDetailControlledVocabularyTermDTO>> zfinAsyncCallback);

    void getDnaLocalizationChangeList(AsyncCallback<List<MutationDetailControlledVocabularyTermDTO>> zfinAsyncCallback);

    void getProteinConsequenceList(AsyncCallback<List<MutationDetailControlledVocabularyTermDTO>> zfinAsyncCallback);

    void getAminoAcidList(AsyncCallback<List<MutationDetailControlledVocabularyTermDTO>> zfinAsyncCallback);

    void getTranscriptConsequenceList(AsyncCallback<List<MutationDetailControlledVocabularyTermDTO>> zfinAsyncCallback);

    void isValidAccession(String accessionNumber, String type, AsyncCallback<String> valid);
}

