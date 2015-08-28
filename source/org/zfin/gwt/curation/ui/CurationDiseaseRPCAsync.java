package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

/**
 * RPC Async Class for the Curation module.
 */
public interface CurationDiseaseRPCAsync {

    void getHumanDiseaseList(String publicationID, AsyncCallback<List<TermDTO>> callback);

    void getHumanDiseaseModelList(String publicationID, AsyncCallback<List<DiseaseModelDTO>> callback);

    void addHumanDiseaseModel(DiseaseModelDTO diseaseModelDTO, AsyncCallback<List<DiseaseModelDTO>> callback);

    void getStrList(String publicationID, AsyncCallback<List<RelatedEntityDTO>> callback);

    void createFish(String publicationID, FishDTO newFish, AsyncCallback<List<FishDTO>> error);

    void getFishList(String publicationID, AsyncCallback<List<FishDTO>> callback);

    void deleteDiseaseModel(DiseaseModelDTO diseaseModelDTO, AsyncCallback<List<DiseaseModelDTO>> callback);

    void getGenotypeList(String publicationID, AsyncCallback<List<GenotypeDTO>> callback);

    void getFeatureList(String publicationID, AsyncCallback<List<FeatureDTO>> callback);

    void searchGenotypes(String publicationID, String featureID, String genotypeID, AsyncCallback<List<GenotypeDTO>> callback);

    void addGenotypeToPublication(String publicationID, String zdbID, AsyncCallback<GenotypeDTO> callback);

    void savePublicNote(String publicationID, ExternalNoteDTO externalNoteDTO, AsyncCallback<List<GenotypeDTO>> async);

    void createPublicNote(String publicationID, GenotypeDTO genotypeDTO, String text, AsyncCallback<List<GenotypeDTO>> async);

    void deletePublicNote(String publicationID, ExternalNoteDTO note, AsyncCallback<List<GenotypeDTO>> listZfinAsyncCallback);

    void saveCuratorNote(String publicationID, CuratorNoteDTO externalNoteDTO, AsyncCallback<List<GenotypeDTO>> zfinAsyncCallback);

    void deleteCuratorNote(String publicationID, CuratorNoteDTO note, AsyncCallback<List<GenotypeDTO>> zfinAsyncCallback);

    void createCuratorNote(String publicationID, GenotypeDTO genotypeDTO, String text, AsyncCallback<List<GenotypeDTO>> async);

    void getZygosityLists(AsyncCallback<List<ZygosityDTO>> async);

    void createGenotypeFeature(String publicationID, List<GenotypeFeatureDTO> genotypeFeatureDTOList, List<GenotypeDTO> genotypeBackgroundList, String nickname, AsyncCallback<GenotypeDTO> callBack);
}


