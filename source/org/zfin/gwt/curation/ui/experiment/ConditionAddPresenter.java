package org.zfin.gwt.curation.ui.experiment;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.REST;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.LookupRPCService;

import java.util.*;

import static org.zfin.gwt.curation.ui.CurationEntryPoint.experimentService;

public class ConditionAddPresenter implements HandlesError {

    private ConditionAddView view;

    private String publicationID;

    private List<ExperimentDTO> dtoList;
    private List<CheckBox> copyConditionsCheckBoxList = new ArrayList<>();
    private Map<String, Set<String>> childMap;


    public ConditionAddPresenter(ConditionAddView view, String publicationID) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {
        loadChildMap();
    }


    public void updateExperimentList() {
        loadExperiments(false);
        setVisibility(null);
    }

    private void loadChildMap() {

        AppUtils.fireAjaxCall(ExperimentModule.getModuleInfo(), AjaxCallEventType.GET_CHILD_MAP_START);
        REST.withCallback(new ZfinAsynchronousCallback<Map<String, Set<String>>>("Failed to load child map: ", view.errorLabel,
                ExperimentModule.getModuleInfo(), AjaxCallEventType.GET_CHILD_MAP_STOP) {
            public void onSuccess(Method method, Map<String, Set<String>> childrenMap) {
                super.onFinish();
                childMap = childrenMap;
                updateExperimentList();
            }
        })
                .call(experimentService)
                .getZecoChildMap();
    }

    // notify means: calling this form within this module, notification needed for other part,
    // or it is called from outside then no notification needed.
    public void loadExperiments(boolean notify) {
        AppUtils.fireAjaxCall(ExperimentModule.getModuleInfo(), AjaxCallEventType.GET_EXPERIMENT_LIST_START);
        try {
            REST.withCallback(new ExperimentListCallBack(notify, "Failed to retrieve experiments: ", null,
                    ExperimentModule.getModuleInfo(), AjaxCallEventType.GET_EXPERIMENT_LIST_STOP))
                    .call(experimentService)
                    .getExperiments(publicationID);
        } catch (ValidationException e) {
            e.printStackTrace();
        }

    }

    public void populateData() {
        int elementIndex = 0;
String onlyCondition="";
        copyConditionsCheckBoxList.clear();
        if (dtoList.isEmpty()) {
            view.emptyDataTable();
            return;
        }
        ConditionDTO lastCondition = null;

        for (ExperimentDTO dto : dtoList) {
            if (dto.conditionDTOList == null)
                continue;

            for (ConditionDTO conditionDTO : dto.getConditionDTOList()) {
                view.addCondition(dto, conditionDTO, lastCondition, elementIndex);
                final CheckBox checkBox = new CheckBox();
                checkBox.setTitle(conditionDTO.getZdbID());
                checkBox.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        if (clickEvent.getSource() instanceof CheckBox) {
                            CheckBox checkBox = (CheckBox) clickEvent.getSource();
                            enableCopyControls(checkBox.getValue());
                        }
                    }
                });
                copyConditionsCheckBoxList.add(checkBox);
                view.addCopyCheckBox(checkBox, elementIndex);
                DeleteImage deleteImage = new DeleteImage("Delete Experiment Condition " + conditionDTO.getZdbID());
                lastCondition = conditionDTO;
                if (dto.isUsed()&&dto.conditionDTOList.size()==1&&dto.conditionDTOList.contains(lastCondition)){
                 
                     onlyCondition="true";
                }
                else{
                     onlyCondition="false";
                }
                deleteImage.addClickHandler(new DeleteConditionClickHandler(conditionDTO, this,onlyCondition));
                view.addDeleteButton(deleteImage, elementIndex,dto,lastCondition);
                elementIndex++;
//                lastCondition = conditionDTO;
            }
        }
    }

    private void displayControlSection(Map<TermEntry, Boolean> visibilityMap) {
        view.addZeco();
        int row = 1;
        for (TermEntry termEntry : visibilityMap.keySet()) {
            if (visibilityMap.get(termEntry)) {
                view.addTermEntry(termEntry, row++);
                termEntry.setVisible(true);
            } else {
                termEntry.setVisible(false);
            }
        }
        view.addControls(row);
    }


    protected void copyConditions() {
        List<String> copyConditionIdList = new ArrayList<>(copyConditionsCheckBoxList.size());
        for (CheckBox checkBox : copyConditionsCheckBoxList) {
            if (checkBox.getValue())
                copyConditionIdList.add(checkBox.getTitle());
        }
        String experimentID = view.experimentCopyToSelectionList.getSelected();
        AppUtils.fireAjaxCall(ExperimentModule.getModuleInfo(), AjaxCallEventType.COPY_CONDITION_START);
        try {
            REST.withCallback(new ExperimentListCallBack(true, "Failed to copy conditions: ", null,
                    ExperimentModule.getModuleInfo(), AjaxCallEventType.COPY_CONDITION_STOP))
                    .call(experimentService)
                    .copyConditions(experimentID, copyConditionIdList);
        } catch (ValidationException | TermNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void enableCopyControls(boolean enable) {
        if (enable)
            view.addCopyControlsPanel();
        else
            setVisibility(null);
    }

    @Override
    public void setError(String message) {
        view.errorLabel.setError(message);
    }

    @Override
    public void clearError() {
        view.errorLabel.setError("");
    }

    @Override
    public void fireEventSuccess() {

    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {

    }

    static Map<String, List<Boolean>> ontologyDependencyMap;

    public static List<Boolean> getVisibilityMatrixOfDependentOntologies(String zecoTermID) {
        if (ontologyDependencyMap == null) {
            ontologyDependencyMap = new HashMap<>();
            ontologyDependencyMap.put("", Arrays.asList(false, false, false, false));
            ontologyDependencyMap.put("ZECO:0000111", Arrays.asList(true, false, false, false));
            ontologyDependencyMap.put("ZECO:0000239", Arrays.asList(true, false, false, false));
            ontologyDependencyMap.put("ZECO:0000143", Arrays.asList(false, true, false, false));
            ontologyDependencyMap.put("ZECO:0000229", Arrays.asList(false, true, true, false));
            ontologyDependencyMap.put("ZECO:0000176", Arrays.asList(false, true, true, false));
            ontologyDependencyMap.put("ZECO:0000105", Arrays.asList(false, false, false, true));
        }
        if (ontologyDependencyMap.get(zecoTermID) == null)
            zecoTermID = "";
        return ontologyDependencyMap.get(zecoTermID);
    }

    private List<TermEntry> getListOfTermEntries() {
        return Arrays.asList(view.chebiTermEntry, view.aoTermEntry, view.goCcTermEntry, view.taxonTermEntry);
    }

    public void onTermSelectEvent(SelectAutoCompleteEvent event) {
        OntologyDTO ontology = event.getOntology();
        if (ontology.equals(OntologyDTO.ZECO)) {
            setVisibility(event.getTermID());
        }
        handleDirty();
    }

    private void setVisibility(String termID) {
        String zecoRootTermID = "";
        if (termID != null) {
            zecoRootTermID = getRoot(termID);
        }
        List<Boolean> visibilityVector = getVisibilityMatrixOfDependentOntologies(zecoRootTermID);
        Map<TermEntry, Boolean> visibilityMap = new HashMap<>(8);
        int index = 0;
        for (TermEntry termEntry : getListOfTermEntries()) {
            visibilityMap.put(termEntry, visibilityVector.get(index++));
        }
        displayControlSection(visibilityMap);
    }

    private String getRoot(String termID) {
        if (termID == null)
            return null;
        for (String rootTermID : childMap.keySet()) {
            Set<String> termSet = childMap.get(rootTermID);
            if (termSet.contains(termID))
                return rootTermID;
        }
        return null;
    }

    private void handleDirty() {
        view.createExperimentConditionButton.setEnabled(true);
    }


    public void resetGUI() {
        view.zecoTermEntry.reset();
        view.aoTermEntry.reset();
        view.goCcTermEntry.reset();
        view.taxonTermEntry.reset();
        view.chebiTermEntry.reset();
        view.clearError();
        setVisibility("");
        view.createExperimentConditionButton.setEnabled(false);
    }

    public void createCondition() {
        if (!formIsValidated()) {
            setError("The Term entry fields are not all validated...");
            view.loadingImage.setVisible(false);
            return;
        }
        String message = validatePostCompositions();
        if (message != null) {
            setError(message);
            view.loadingImage.setVisible(false);
            return;
        }

        ConditionDTO conditionDTO = getConditionFromFrom();
        view.clearError();
        AppUtils.fireAjaxCall(ExperimentModule.getModuleInfo(), AjaxCallEventType.CREATE_CONDITION_START);
        try {
            REST.withCallback(new ExperimentListCallBack(true, "Failed to save condition: ", conditionDTO,
                    ExperimentModule.getModuleInfo(), AjaxCallEventType.CREATE_CONDITION_STOP))
                    .call(experimentService)
                    .createCondition(publicationID, conditionDTO);
        } catch (ValidationException | TermNotFoundException e) {
            // ignore as this is an asynchronous call.
        }
    }

    private String validatePostCompositions() {

        if (!view.aoTermEntry.getTermTextBox().hasValidNonNullTerm() && view.aoTermEntry.isVisible())
            return "Zeco term requires an AO term ";
        if (!view.chebiTermEntry.getTermTextBox().hasValidNonNullTerm() && view.chebiTermEntry.isVisible())
            return "Zeco term requires a Chebi term ";
        if (!view.taxonTermEntry.getTermTextBox().hasValidNonNullTerm() && view.taxonTermEntry.isVisible())
            if (view.zecoTermEntry.getTermText().equals("bacterial treatment") || view.zecoTermEntry.getTermText().equals("viral treatment") || view.zecoTermEntry.getTermText().equals("fungal treatment") ||
                    view.zecoTermEntry.getTermText().equals("bacterial treatment by exposure to environment") || view.zecoTermEntry.getTermText().equals("viral treatment by exposure to environment") || view.zecoTermEntry.getTermText().equals("fungal treatment by exposure to environment") ||
                    view.zecoTermEntry.getTermText().equals("bacterial treatment by injection") || view.zecoTermEntry.getTermText().equals("viral treatment by injection") || view.zecoTermEntry.getTermText().equals("fungal treatment by injection")) {

                return "Zeco term requires a taxonomy term ";
            }

        return null;
    }

    private boolean formIsValidated() {
        if (view.zecoTermEntry.getTermTextBox().hasValidateTerm())
            return true;
        if (view.aoTermEntry.getTermTextBox().hasValidateTerm() && view.aoTermEntry.isVisible())
            return true;
        if (view.goCcTermEntry.getTermTextBox().hasValidateTerm() && view.goCcTermEntry.isVisible())
            return true;
       /* if (view.taxonTermEntry.getTermTextBox().hasValidateTerm() && view.taxonTermEntry.isVisible())
            return true;*/
        if (view.chebiTermEntry.getTermTextBox().hasValidateTerm() && view.chebiTermEntry.isVisible())
            return true;
        return false;
    }

    private ConditionDTO getConditionFromFrom() {
        ConditionDTO dto = new ConditionDTO();
        dto.setEnvironmentZdbID(view.experimentSelectionList.getSelected());
        dto.setZecoTerm(view.zecoTermEntry.getTermTextBox().getSelectedTerm());
        dto.setAoTerm(view.aoTermEntry.getTermTextBox().getSelectedTerm());
        dto.setGoCCTerm(view.goCcTermEntry.getTermTextBox().getSelectedTerm());
        dto.setTaxonTerm(view.taxonTermEntry.getTermTextBox().getSelectedTerm());
        dto.setChebiTerm(view.chebiTermEntry.getTermTextBox().getSelectedTerm());
        return dto;
    }

    public void updateTermInfoBox(String termName, String ontologyName) {
        if (termName != null && !termName.startsWith(ItemSuggestCallback.END_ELLIPSIS)) {
            OntologyDTO ontology = OntologyDTO.getOntologyByName(ontologyName);
            LookupRPCService.App.getInstance().getTermByName(ontology, termName, new TermInfoCallBack(view.termInfoBox, termName));
        }

    }

    private class DeleteConditionClickHandler implements ClickHandler {

        private ConditionDTO conditionDTO;
        private ConditionAddPresenter presenter;
        private String onlyCond;
        private String message="";


        public DeleteConditionClickHandler(ConditionDTO conditionDTO, ConditionAddPresenter presenter,String onlyCond) {
            this.conditionDTO = conditionDTO;
            this.presenter = presenter;
            this.onlyCond=onlyCond;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {

if (onlyCond=="true"){
     message="Last condition for this experiment. Are you sure you want to delete this condition";
}
else {
    message = "Are you sure you want to delete this condition?";
}
            if (!Window.confirm(message))
                return;
            view.loadingImage.setVisible(true);

            AppUtils.fireAjaxCall(ExperimentModule.getModuleInfo(), AjaxCallEventType.DELETE_CONDITION_START);
            try {
                REST.withCallback(new ZfinAsynchronousCallback<List<ExperimentDTO>>("Failed to remove condition: ", view.errorLabel,
                        ExperimentModule.getModuleInfo(), AjaxCallEventType.DELETE_CONDITION_STOP) {
                    @Override
                    public void onSuccess(Method method, List<ExperimentDTO> list) {
                        super.onFinish();
                        dtoList.clear();
                        dtoList = list;
                        populateData();
                        view.loadingImage.setVisible(false);
                        // notify the create-experiment section
                        AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.REMOVE_EXPERIMENT_CONDITION, conditionDTO.getName()));
                    }
                })
                        .call(experimentService)
                        .deleteCondition(publicationID, conditionDTO);
            } catch (ValidationException | TermNotFoundException e) {
                // ignore as this call is asynchronous
            }
        }
    }


    private class ExperimentListCallBack extends ZfinAsynchronousCallback<List<ExperimentDTO>> {
        private boolean notify;
        private ConditionDTO conditionDTO;

        public ExperimentListCallBack(boolean notify, String errorMessage, ConditionDTO conditionDTO, ZfinModule module, AjaxCallEventType eventType) {
            super(errorMessage, ConditionAddPresenter.this.view.errorLabel,
                    module, eventType);
            view.loadingImage.setVisible(false);
            this.notify = notify;
            this.conditionDTO = conditionDTO;
        }

        public void onSuccess(Method method, List<ExperimentDTO> experimentList) {
            super.onFinish();
            dtoList = experimentList;
            view.experimentSelectionList.clear();
            view.experimentCopyToSelectionList.clear();
            for (ExperimentDTO dto : experimentList) {
                view.experimentSelectionList.addItem(dto.getName(), dto.getZdbID());
                view.experimentCopyToSelectionList.addItem(dto.getName(), dto.getZdbID());
            }
            populateData();
            resetGUI();
            view.loadingImage.setVisible(false);
            // notify the create-experiment section
            if (notify) {
                AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.CREATE_EXPERIMENT_CONDITION, conditionDTO.getName()));
            }
        }

        @Override
        public void onFailure(Method method, Throwable throwable) {
            super.onFailureBase(method, throwable);
            errorHandler.setError(method.getResponse().getText());
        }
    }
}
