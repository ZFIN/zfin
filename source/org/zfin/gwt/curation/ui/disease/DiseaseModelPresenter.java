package org.zfin.gwt.curation.ui.disease;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.REST;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.curation.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.ui.ZfinAsynchronousCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.zfin.gwt.curation.ui.CurationEntryPoint.curationService;
import static org.zfin.gwt.curation.ui.CurationEntryPoint.expressionService;

/**
 * Table of associated genotypes
 */
public class DiseaseModelPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationRPCService = CurationExperimentRPC.App.getInstance();
    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();
    private DiseaseModelView view;
    private String publicationID;
    private List<FishDTO> fishList = new ArrayList<>();
    private List<ExperimentDTO> environmentList = new ArrayList<>();
    private List<TermDTO> diseaseList = new ArrayList<>();
    private List<DiseaseAnnotationDTO> diseaseModelList = new ArrayList<>();
    private boolean processing = false;

    public DiseaseModelPresenter(DiseaseModelView view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
    }

    protected void addModelEvent() {
        if (processing)
            return;
        processing = true;
        DiseaseAnnotationDTO disease = getDiseaseModel();


        if (disease == null) {
            processing = false;
            return;
        }

        if(disease.getFish() == null || disease.getFish().getZdbID() == null || disease.getFish().getZdbID().isEmpty()) {
            setError("No fish selected, saving annotation but not submitting to alliance.");
        }

        AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.ADD_HUMAN_DISEASE_ANNOTATIONS_START);
        diseaseRpcService.addHumanDiseaseAnnotation(disease,
                new RetrieveDiseaseModelListCallBack(disease, "Could not add a new disease model", view.getErrorLabel(),
                        AjaxCallEventType.ADD_HUMAN_DISEASE_ANNOTATIONS_STOP, true));
        view.getLoadingImage().setVisible(true);
    }

    private DiseaseAnnotationDTO getDiseaseModel() {
        DiseaseAnnotationDTO dto = new DiseaseAnnotationDTO();
        int selectedIndexFish = view.getFishSelectionBox().getSelectedIndex();


        int selectedIndexEnv = view.getEnvironmentSelectionBox().getSelectedIndex();


        if ((selectedIndexFish == 0 && selectedIndexEnv > 0) ||
                (selectedIndexFish > 0 && selectedIndexEnv == 0)) {
            setError("You need to select both a Fish and an Environment or none at all.");
            return null;
        }
        if (selectedIndexEnv > 0 && selectedIndexFish > 0) {

            FishDTO fish = fishList.get(selectedIndexFish-1);
            

            ExperimentDTO environment = environmentList.get(selectedIndexEnv-1);

            if (fish.isWildtype() && environment.isStandard()) {
                setError("You cannot use a wildtype fish with Standard or Generic Control environment");
                return null;
            }
            dto.setFish(fish);
            dto.setEnvironment(environment);
        }
        int selectedIndexDis = view.getDiseaseSelectionBox().getSelectedIndex();
        if (selectedIndexDis == -1) {
            setError("No Disease available. Please add a new disease below.");
            return null;
        }
        dto.setDisease(diseaseList.get(selectedIndexDis));
        String itemText = view.getEvidenceCodeSelectionBox().getItemText(view.getEvidenceCodeSelectionBox().getSelectedIndex());
        if (itemText.equals("TAS")) {
            TermDTO eco = new TermDTO();
            eco.setZdbID("ZDB-TERM-170419-250");
            dto.setEvidenceCode(eco);
        }
        if (itemText.equals("IC")) {
            TermDTO eco = new TermDTO();
            eco.setZdbID("ZDB-TERM-170419-251");
            dto.setEvidenceCode(eco);
        }
        PublicationDTO pubDto = new PublicationDTO();
        pubDto.setZdbID(publicationID);
        dto.setPublication(pubDto);
        return dto;

    }

    @Override
    public void go() {
        retrieveAllRecords();
    }

    private void retrieveAllRecords() {
        // human disease model list
        AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_HUMAN_DISEASE_LIST_START);
        diseaseRpcService.getHumanDiseaseModelList(publicationID,
                new RetrieveDiseaseModelListCallBack(null, view.getErrorLabel(), AjaxCallEventType.GET_HUMAN_DISEASE_LIST_STOP, false));

        // environment list
        retrieveEnvironmentList();

        retrieveFishList();
    }

    public void retrieveEnvironmentList() {
        String message = "Error while reading the environment";
        AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_ENVIRONMENT_LIST_START);
        REST.withCallback(new RetrieveEnvironmentListCallBack(message, view.getErrorLabel()))
                .call(expressionService).getExperiments(publicationID);
    }

    private void updateDiseaseList(List<DiseaseAnnotationDTO> diseaseModelList) {
        if (diseaseModelList == null)
            return;

        for (DiseaseAnnotationDTO annotationDTO : diseaseModelList) {
            if (!diseaseList.contains(annotationDTO.getDisease()))
                diseaseList.add(annotationDTO.getDisease());
        }
        reCreateDiseaseListBox();
    }

    private void reCreateDiseaseListBox() {
        Collections.sort(diseaseList);
        view.getDiseaseSelectionBox().clear();
        for (TermDTO disease : diseaseList)
            view.getDiseaseSelectionBox().addItem(disease.getTermName(), disease.getZdbID());
    }

    public void setError(String message) {
        view.getErrorLabel().setText(message);
    }

    public void clearErrorMessages() {
        view.getErrorLabel().setError("");
    }

    public void addDiseaseToSelectionBox(TermDTO disease) {
        AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_TERM_INFO_START);
        lookupRPC.getTermInfo(OntologyDTO.DISEASE_ONTOLOGY, disease.getOboID(), new DiseaseInfoCallBack("Could not retrieve Term Info", view.getErrorLabel()));
    }

    public void updateConditions() {
        String message = "getting wildtype";
        AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_BACKGROUND_GENOTYPES_START);
        curationRPCService.getBackgroundGenotypes(publicationID, new RetrieveBackgroundNewGenoCallback(message, view.getErrorLabel()));
    }

    public void retrieveFishList() {
        // fish list
        String message = "Error while reading Fish";
        AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_FISH_LIST_START);
        FishServiceGWT.callServer(publicationID, new RetrieveFishListCallBack(message, view.getErrorLabel()));
    }

    class RetrieveEnvironmentListCallBack extends ZfinAsynchronousCallback<List<ExperimentDTO>> {

        public RetrieveEnvironmentListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel,
                    HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_ENVIRONMENT_LIST_STOP);
        }

        @Override
        public void onSuccess(Method method, List<ExperimentDTO> list) {
            super.onFinish();
            view.getEnvironmentSelectionBox().clear();
            environmentList = list;
            view.getEnvironmentSelectionBox().addItem("None");
            for (ExperimentDTO dto : list) {
                view.getEnvironmentSelectionBox().addItem(dto.getName(), dto.getZdbID());
            }
            view.resetUI();
        }
    }

    class RetrieveBackgroundNewGenoCallback extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveBackgroundNewGenoCallback(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel,
                    HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_BACKGROUND_GENOTYPES_STOP);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            super.onFinish();
            List<String> genos = new ArrayList<>();
            String termSel = view.getFishSelectionBox().getItemText(view.getFishSelectionBox().getSelectedIndex());

            for (GenotypeDTO dto : list) {
                genos.add(dto.getName());
            }
            genos.add("WT");
            if (view.getEnvironmentSelectionBox().getSelectedValue().equals("ZDB-EXP-041102-1") || view.getEnvironmentSelectionBox().getSelectedValue().equals("ZDB-EXP-070511-5")) {
                int selInd = view.getEnvironmentSelectionBox().getSelectedIndex();
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(selInd).setAttribute("disabled", "disabled");
            } else {
                int selInd = view.getEnvironmentSelectionBox().getSelectedIndex();
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(selInd).removeAttribute("disabled");
            }
            if (genos.contains(termSel)) {

                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(1).setAttribute("disabled", "disabled");
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(2).setAttribute("disabled", "disabled");
                //view.getEnvironmentSelectionBox().setEnabled(false);
            } else {
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(1).removeAttribute("disabled");
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(2).removeAttribute("disabled");
            }
        }
    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel,
                    HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_FISH_LIST_STOP);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            super.onFinish();
            if (list == null)
                return;
            if (list.size() > 0) {

                Collections.sort(list, new Comparator<FishDTO>() {
                    @Override
                    public int compare(FishDTO o1, FishDTO o2) {
                        return o1.compareToWildtypeFirst(o2);
                    }
                });
            }
           view.getFishSelectionBox().clear();
            fishList = list;
            view.getFishSelectionBox().addItem("None");
            for (FishDTO dto : list) {
                view.getFishSelectionBox().addItem(dto.getHandle(), dto.getZdbID());
            }
            view.resetUI();
        }
    }

    class RetrieveDiseaseModelListCallBack extends ZfinAsyncCallback<List<DiseaseAnnotationDTO>> {

        private boolean createDeleteModel;
        private DiseaseAnnotationDTO diseaseAnnotation;

        public RetrieveDiseaseModelListCallBack(String errorMessage, ErrorHandler errorLabel, AjaxCallEventType event, Boolean createDeleteModel) {
            super(errorMessage, errorLabel, view.loadingImage,
                    HumanDiseaseModule.getModuleInfo(), event);
            this.createDeleteModel = createDeleteModel;
        }

        public RetrieveDiseaseModelListCallBack(DiseaseAnnotationDTO diseaseAnnotation, String errorMessage, ErrorHandler errorLabel, AjaxCallEventType event, Boolean createDeleteModel) {
            super(errorMessage, errorLabel, view.loadingImage,
                    HumanDiseaseModule.getModuleInfo(), event);
            this.diseaseAnnotation = diseaseAnnotation;
            this.createDeleteModel = createDeleteModel;
        }

        @Override
        public void onSuccess(List<DiseaseAnnotationDTO> modelDTOs) {
            super.onFinish();
            if (modelDTOs == null) {
                diseaseModelList.clear();
            } else {
                diseaseModelList = modelDTOs;
            }
            if (modelDTOs != null && modelDTOs.size() > 0) {
                int index = 0;
                for (DiseaseAnnotationDTO disease : modelDTOs) {
                    if (disease.getDamoDTO() != null) {
                        for (DiseaseAnnotationModelDTO dto : disease.getDamoDTO()) {
                            populateSingleRowDiseaseTable(index++, disease, dto);
                        }
                    } else {
                        populateSingleRowDiseaseTable(index++, disease, null);
                    }
                }
            } else {
                view.removeAllDataRows();
            }
            view.endTableUpdate();
            updateDiseaseList(modelDTOs);
            view.getLoadingImage().setVisible(false);
            processing = false;
            if (createDeleteModel) {
                String id = "";
                if(diseaseAnnotation != null)
                    id = diseaseAnnotation.toString();
                AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.CD_DISEASE_MODEL, id));
            }
        }

        private void populateSingleRowDiseaseTable(int index, DiseaseAnnotationDTO disease, DiseaseAnnotationModelDTO dto) {
            if (dto != null) {
                view.addFish(dto.getFish(), index);
                view.addEnvironment(dto.getEnvironment(), index);
                view.addIsModelOf(true, index);
                view.addDeleteButtonFishModel(dto, index, new HumanDiseaseAnnotationModelDeleteClickListener(dto));
            } else {
                view.addFish(null, index);
                view.addEnvironment(null, index);
                view.addIsModelOf(false, index);
                view.addDeleteButtonDisease(disease, index, new HumanDiseaseModelDeleteClickListener(disease));
            }
            view.addDisease(disease.getDisease(), index, new PopulateTermEntryClickListener(disease.getDisease()));
            view.addEvidence(disease.getEvidenceCode().getTermName(), index);
        }

        @Override
        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
            processing = false;
        }
    }

    private class HumanDiseaseModelDeleteClickListener implements ClickHandler {

        private DiseaseAnnotationDTO diseaseAnnotationDTO;

        public HumanDiseaseModelDeleteClickListener(DiseaseAnnotationDTO diseaseAnnotationDTO) {
            this.diseaseAnnotationDTO = diseaseAnnotationDTO;
        }

        public void onClick(ClickEvent event) {
            AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.DELETE_DISEASE_MODEL_START);
            diseaseRpcService.deleteDiseaseModel(diseaseAnnotationDTO,
                    new RetrieveDiseaseModelListCallBack("Could not delete Disease model", view.getErrorLabel(),
                            AjaxCallEventType.DELETE_DISEASE_MODEL_STOP, true));
        }
    }

    private class HumanDiseaseAnnotationModelDeleteClickListener implements ClickHandler {

        private DiseaseAnnotationModelDTO diseaseAnnotationModelDTO;

        public HumanDiseaseAnnotationModelDeleteClickListener(DiseaseAnnotationModelDTO diseaseAnnotationModelDTO) {
            this.diseaseAnnotationModelDTO = diseaseAnnotationModelDTO;
        }

        public void onClick(ClickEvent event) {
            AppUtils.fireAjaxCall(HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.DELETE_DISEASE_ANNOTATION_MODEL_START);
            diseaseRpcService.deleteDiseaseAnnotationModel(diseaseAnnotationModelDTO,
                    new RetrieveDiseaseModelListCallBack("Could not delete Disease model", view.getErrorLabel(), AjaxCallEventType.DELETE_DISEASE_ANNOTATION_MODEL_STOP, true));
        }
    }

    private class PopulateTermEntryClickListener implements ClickHandler {

        private TermDTO termDTO;

        public PopulateTermEntryClickListener(TermDTO termDTO) {
            this.termDTO = termDTO;
        }

        public void onClick(ClickEvent event) {
            AppUtils.EVENT_BUS.fireEvent(new ClickTermEvent(termDTO));
        }
    }


    private class DiseaseInfoCallBack extends ZfinAsyncCallback<TermDTO> {

        public DiseaseInfoCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel,
                    HumanDiseaseModule.getModuleInfo(), AjaxCallEventType.GET_TERM_INFO_STOP);
        }

        @Override
        public void onSuccess(TermDTO disease) {
            super.onFinish();
            if (disease.isObsolete()) {
                errorHandler.setError("Cannot use obsolete term");
            } else {
                if (diseaseList.contains(disease))
                    return;
                diseaseList.add(disease);
                reCreateDiseaseListBox();
            }
        }
    }
}
