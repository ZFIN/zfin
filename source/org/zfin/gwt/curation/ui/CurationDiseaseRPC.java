package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

/**
 * GWT class to facilitate curation of FX
 */
public interface CurationDiseaseRPC extends RemoteService {

    List<GenotypeDTO> getGenotypeList(String publicationID);

    List<FeatureDTO> getFeatureList(String publicationID);

    List<GenotypeDTO> searchGenotypes(String publicationID, String featureID, String genotypeID);

    GenotypeDTO addGenotypeToPublication(String publicationID, String zdbID) throws TermNotFoundException;

    List<GenotypeDTO> savePublicNote(String publicationID, ExternalNoteDTO externalNoteDTO) throws TermNotFoundException;

    List<GenotypeDTO> createPublicNote(String publicationID, GenotypeDTO genotypeDTO, String text) throws TermNotFoundException;

    List<GenotypeDTO> deletePublicNote(String publicationID, ExternalNoteDTO note) throws TermNotFoundException;

    List<GenotypeDTO> saveCuratorNote(String publicationID, CuratorNoteDTO externalNoteDTO) throws TermNotFoundException;

    List<GenotypeDTO> deleteCuratorNote(String publicationID, CuratorNoteDTO note) throws TermNotFoundException;

    List<GenotypeDTO> createCuratorNote(String publicationID, GenotypeDTO genotypeDTO, String text) throws TermNotFoundException;

    List<ZygosityDTO> getZygosityLists();

    GenotypeDTO createGenotypeFeature(String publicationID, List<GenotypeFeatureDTO> genotypeFeatureDTOList, GenotypeDTO selectedGenotypeBackground, String nickname)
            throws TermNotFoundException;

    public static class App {
        private static final CurationDiseaseRPCAsync INSTANCE;

        static {
            INSTANCE = GWT.create(CurationDiseaseRPC.class);
            ((ServiceDefTarget) INSTANCE).setServiceEntryPoint("/ajax/curation-disease");
        }

        public static CurationDiseaseRPCAsync getInstance() {
            return INSTANCE;
        }
    }

    List<TermDTO> getHumanDiseaseList(String publicationID);

    List<DiseaseModelDTO> getHumanDiseaseModelList(String publicationID) throws TermNotFoundException;

    List<DiseaseModelDTO> addHumanDiseaseModel(DiseaseModelDTO diseaseModelDTO) throws TermNotFoundException;

    List<RelatedEntityDTO> getStrList(String publicationID);

    List<FishDTO> createFish(String publicationID, FishDTO newFish) throws TermNotFoundException;

    List<FishDTO> getFishList(String publicationID);

    List<DiseaseModelDTO> deleteDiseaseModel(DiseaseModelDTO diseaseModelDTO) throws TermNotFoundException;

}
