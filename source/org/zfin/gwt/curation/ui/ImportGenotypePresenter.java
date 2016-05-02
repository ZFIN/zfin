package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Table of genotypes that are associated to different publications.
 */
public class ImportGenotypePresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRpcService = CurationExperimentRPC.App.getInstance();
    private ImportGenotype view;
    private String publicationID;
    private List<GenotypeDTO> genotypeDTOList = new ArrayList<>();

    public ImportGenotypePresenter(ImportGenotype view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
    }

    protected void searchForGenotypes() {
        view.getErrorLabel().clearAllErrors();
        String featureID = getSelectedFeatureID();
        String genotypeID = getSelectedGenotypeID();
        if (featureID == null && genotypeID == null)
            return;
        diseaseRpcService.searchGenotypes(publicationID, featureID, genotypeID, new RetrieveExistingGenotypeListCallBack("error", view.getErrorLabel()));
        view.getLoadingImage().setVisible(true);
    }

    private String getSelectedFeatureID() {
        String id = view.getFeatureListBox().getSelectedValue();
        if (id.trim().equals(""))
            return null;
        return id;
    }

    private String getSelectedGenotypeID() {
        String id = view.getBackgroundListBox().getSelectedValue();
        if (id.trim().equals(""))
            return null;
        return id;
    }


    @Override
    public void go() {
        retrieveInitialEntities();
    }

    public void retrieveInitialEntities() {
        // get wildtype background list
        RetrieveRelatedEntityDTOListCallBack<GenotypeDTO> retrieveBackgroundCallback = new RetrieveRelatedEntityDTOListCallBack<>(view.getBackgroundListBox(), "Background List", view.getErrorLabel());
        retrieveBackgroundCallback.setLeaveFirstEntryBlank(true);
        curationExperimentRpcService.getBackgroundGenotypes(publicationID, retrieveBackgroundCallback);
        updateFeatureList();
    }

    public void updateFeatureList() {
        // get Feature List
        RetrieveRelatedEntityDTOListCallBack<FeatureDTO> featureListCallBack = new RetrieveRelatedEntityDTOListCallBack<>(view.getFeatureListBox(), "Feature List", view.getErrorLabel());
        featureListCallBack.setLeaveFirstEntryBlank(true);
        diseaseRpcService.getFeatureList(publicationID, featureListCallBack);
    }

    class RetrieveExistingGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveExistingGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, view.getLoadingImage());
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            view.resetGUI();
            genotypeDTOList = list;
            populateDataTable();
            view.getLoadingImage().setVisible(false);
            if (list == null || list.size() == 0)
                view.setError("No more genotypes found");
        }
    }

    private void populateDataTable() {
        if (genotypeDTOList != null && genotypeDTOList.size() > 0) {
            int elementIndex = 0;
            for (GenotypeDTO dto : genotypeDTOList) {
                view.addGenotype(dto, elementIndex);
                view.addCheckBox(elementIndex, new CheckGenotypeClickHandler(dto));
                elementIndex++;
            }
        } else {
            view.removeAllDataRows();
        }
        view.createLastTableRow();
    }

    class ImportGenotypeCallBack extends ZfinAsyncCallback<GenotypeDTO> {

        public ImportGenotypeCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, view.getLoadingImage());
        }

        @Override
        public void onSuccess(GenotypeDTO genotypeDTO) {
            view.resetGUI();
            if (genotypeDTO != null) {
                view.setMessage("Imported genotype " + genotypeDTO.getHandle());
                genotypeDTOList.remove(genotypeDTO);
                populateDataTable();
            }
            view.getLoadingImage().setVisible(false);
            AppUtils.EVENT_BUS.fireEvent(new ImportGenotypeEvent());
        }
    }

    class CheckGenotypeClickHandler implements ClickHandler {
        private GenotypeDTO genotypeDTO;

        public CheckGenotypeClickHandler(GenotypeDTO genotypeDTO) {
            this.genotypeDTO = genotypeDTO;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            view.getLoadingImage().setVisible(true);
            diseaseRpcService.addGenotypeToPublication(publicationID, genotypeDTO.getZdbID(),
                    new ImportGenotypeCallBack("Import Genotype", view.getErrorLabel()));
        }
    }
}
