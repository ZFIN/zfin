package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 */
public interface FeatureRPCServiceAsync {

    void getLabsOfOriginWithPrefix(AsyncCallback<List<LabDTO>> async);
    void getPrefix(String name, AsyncCallback<List<FeaturePrefixDTO>> async);
    void createFeature(FeatureDTO featureDTO, AsyncCallback<FeatureDTO> async);
    void editFeatureDTO(FeatureDTO featureDTO, AsyncCallback<FeatureDTO> async);
    void getFeature(String zdbID, AsyncCallback<FeatureDTO> async);
    void deleteFeature(String zdbID, AsyncCallback<Void> async);
    void getFeaturesForPub(String pubZdbID, AsyncCallback<List<FeatureDTO>> async);
    void getFeaturesMarkerRelationshipsForPub(String publicationZdbID, AsyncCallback<List<FeatureMarkerRelationshipDTO>> featureEditCallBack);
    void deleteFeatureMarkerRelationship(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO, AsyncCallback<Void> featureEditCallBack);
    void getRelationshipTypesForFeatureType(FeatureTypeEnum ftrTypeDisplay, AsyncCallback<List<String>> featureEditCallBack);
    void getMarkersForFeatureRelationAndSource(String featureTypeName, String publicationZdbID, AsyncCallback<List<MarkerDTO>> featureEditCallBack);
    void addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO, AsyncCallback<Void> featureEditCallBack);
    void addFeatureAlias(String name, String ftrZdbID,String pubZdbID ,AsyncCallback<Void> async);
    void removeFeatureAlias(String name, String ftrZdbID, AsyncCallback<Void> async);
    void editPublicNote(NoteDTO noteDTO, AsyncCallback<Void> async);
    void addCuratorNote(NoteDTO noteDTO, AsyncCallback<NoteDTO> markerEditCallBack);
    void editCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);
    void removeCuratorNote(NoteDTO noteDTO, AsyncCallback<Void> async);
    void addFeatureSequence(String name, String ftrZdbID,String pubZdbID ,AsyncCallback<Void> async);
    void removeFeatureSequence(String name, String ftrZdbID, AsyncCallback<Void> async);
}

