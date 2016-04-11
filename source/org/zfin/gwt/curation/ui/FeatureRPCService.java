package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.ValidationException;

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

    List<OrganizationDTO> getLabsOfOriginWithPrefix();

    List<FeaturePrefixDTO> getPrefix(String name);

    FeatureDTO createFeature(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException;

    FeatureDTO editFeatureDTO(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException;

    FeatureDTO getFeature(String featureZdbID);

    void deleteFeature(String zdbID);

    List<FeatureMarkerRelationshipDTO> getFeaturesMarkerRelationshipsForPub(String publicationZdbID);

    void addFeatureAlias(String name, String ftrZdbID, String pubZdbID);

    void removeFeatureAlias(String name, String ftrZdbID);

    void addFeatureSequence(String name, String ftrZdbID, String pubZdbID) throws ValidationException;

    void removeFeatureSequence(String name, String ftrZdbID);

    void deleteFeatureMarkerRelationship(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO);

    List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum ftrTypeDisplay);

    List<MarkerDTO> getMarkersForFeatureRelationAndSource(String featureTypeName, String publicationZdbID);

    void addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO);

    // note stuff
    FeatureDTO editPublicNote(NoteDTO noteDTO);

    CuratorNoteDTO addCuratorNote(CuratorNoteDTO noteDTO);

    void editCuratorNote(NoteDTO noteDTO);

    void removeCuratorNote(NoteDTO noteDTO);

    List<String> getMutagensForFeatureType(FeatureTypeEnum ftrType);

    void removePublicNote(NoteDTO updatedNoteDTO);

    PersonDTO getCuratorInfo();

}
