package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.MutationDetailControlledVocabularyTermDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.DuplicateEntryException;
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
    List<FeatureDTO> getZebrashareFeaturesForPub(String pubID);

    List<OrganizationDTO> getLabsOfOriginWithPrefix();

    List<FeaturePrefixDTO> getPrefix(String name);

    String getNextZFLineNum();

    FeatureDTO createFeature(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException;

    FeatureDTO editFeatureDTO(FeatureDTO featureDTO) throws DuplicateEntryException, ValidationException;

    FeatureDTO getFeature(String featureZdbID);

    void deleteFeature(String zdbID);

    List<FeatureMarkerRelationshipDTO> getFeatureMarkerRelationshipsForPub(String publicationZdbID);

    void addFeatureAlias(String name, String ftrZdbID, String pubZdbID);

    void removeFeatureAlias(String name, String ftrZdbID);

    void addFeatureSequence(String name, String ftrZdbID, String pubZdbID) throws ValidationException;

    void removeFeatureSequence(String name, String ftrZdbID);

    void deleteFeatureMarkerRelationship(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO);

    List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum ftrTypeDisplay);

    List<MarkerDTO> getMarkersForFeatureRelationAndSource(String featureTypeName, String publicationZdbID);

    List<FeatureMarkerRelationshipDTO> addFeatureMarkerRelationShip(FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO, String publicationID) throws ValidationException;

    // note stuff
    FeatureDTO editPublicNote(NoteDTO noteDTO);

    CuratorNoteDTO addCuratorNote(CuratorNoteDTO noteDTO);

    void editCuratorNote(NoteDTO noteDTO);

    void removeCuratorNote(NoteDTO noteDTO);

    List<String> getMutagensForFeatureType(FeatureTypeEnum ftrType);

    void removePublicNote(NoteDTO updatedNoteDTO);

    PersonDTO getCuratorInfo();

    List<MutationDetailControlledVocabularyTermDTO> getDnaChangeList();

    List<MutationDetailControlledVocabularyTermDTO> getDnaLocalizationChangeList();

    List<MutationDetailControlledVocabularyTermDTO> getProteinConsequenceList();

    List<MutationDetailControlledVocabularyTermDTO> getTranscriptConsequenceList();

    List<MutationDetailControlledVocabularyTermDTO> getAminoAcidList();


    String isValidAccession(String accessionNumber, String type);


}
