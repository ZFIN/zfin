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

    void getFeatureList(String publicationID, AsyncCallback<List<RelatedEntityDTO>> callback);

    void searchGenotypes(String publicationID, String featureID, String genotypeID, AsyncCallback<List<GenotypeDTO>> callback);

    void addGenotypeToPublication(String publicationID, String zdbID, AsyncCallback<List<GenotypeDTO>> callback);
}


