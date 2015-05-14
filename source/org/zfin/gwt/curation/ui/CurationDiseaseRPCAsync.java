package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

/**
 * RPC Async Class for the Curation module.
 */
public interface CurationDiseaseRPCAsync {

    void saveHumanDisease(TermDTO term, String publicationID, AsyncCallback<List<TermDTO>> callback);

    void getHumanDiseaseList(String publicationID, AsyncCallback<List<TermDTO>> callback);

    void deleteHumanDisease(TermDTO term, String publicationID, AsyncCallback<List<TermDTO>> callback);

    void getHumanDiseaseModelList(String publicationID, AsyncCallback<List<DiseaseModelDTO>> callback);

    void addHumanDiseaseModel(DiseaseModelDTO diseaseModelDTO, AsyncCallback<List<DiseaseModelDTO>> callback);

    void getStrList(String publicationID, AsyncCallback<List<RelatedEntityDTO>> callback);

    void createFish(String publicationID, FishDTO newFish, AsyncCallback<List<FishDTO>> error);

    void getFishList(String publicationID, AsyncCallback<List<FishDTO>> callback);

}

