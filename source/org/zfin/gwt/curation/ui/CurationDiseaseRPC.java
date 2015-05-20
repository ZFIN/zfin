package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;

import java.util.List;

/**
 * GWT class to facilitate curation of FX
 */
public interface CurationDiseaseRPC extends RemoteService {

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
