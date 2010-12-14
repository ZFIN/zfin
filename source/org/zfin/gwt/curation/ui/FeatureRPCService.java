package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.DuplicateEntryException;

import java.util.List;

/**
 */
public interface FeatureRPCService extends RemoteService {

    /**
     * Utility/Convenience class.
     */
    public static class App {
        private static FeatureRPCServiceAsync ourInstance = null;

        public static synchronized FeatureRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (FeatureRPCServiceAsync) GWT.create(FeatureRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/featureservice");
            }
            return ourInstance;
        }
    }


    List<FeatureDTO> getFeaturesForPub(String pubZdbId);
    List<LabDTO> getLabsOfOriginWithPrefix();
    List<FeaturePrefixDTO> getPrefix(String name);
    FeatureDTO createFeature(FeatureDTO featureDTO) throws DuplicateEntryException;
    FeatureDTO editFeatureDTO(FeatureDTO featureDTO) throws DuplicateEntryException;
    FeatureDTO getFeature(String featureZdbID);
    void deleteFeature(String zdbID);
    List<FeatureMarkerRelationshipDTO> getFeaturesMarkerRelationshipsForPub(String publicationZdbID);
    void addFeatureAlias(String name, String ftrZdbID);
    void removeFeatureAlias(String name, String ftrZdbID);
    void deleteFeatureMarkerRelationship(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO);
    List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum ftrTypeDisplay);
    List<MarkerDTO> getMarkersForFeatureRelationAndSource(String featureTypeName, String publicationZdbID);
    void addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO);

    // note stuff
    void editPublicNote(NoteDTO noteDTO);
    NoteDTO addCuratorNote(NoteDTO noteDTO);
    void editCuratorNote(NoteDTO noteDTO);
    void removeCuratorNote(NoteDTO noteDTO);

}
