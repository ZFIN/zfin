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

    List<GenotypeDTO> addGenotypeToPublication(String publicationID, String zdbID) throws TermNotFoundException;

    GenotypeDTO savePublicNote(String publicationID, GenotypeDTO genotypeDTO) throws TermNotFoundException;

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
