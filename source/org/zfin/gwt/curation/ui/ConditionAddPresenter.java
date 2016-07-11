package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import org.zfin.gwt.curation.event.ChangeConditionEvent;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.*;

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

        ExperimentRPCService.App.getInstance().getChildMap(new ZfinAsyncCallback<Map<String, Set<String>>>("Failed to load child map: ", view.errorLabel) {
            public void onSuccess(Map<String, Set<String>> childrenMap) {
                childMap = childrenMap;
                updateExperimentList();
            }
        });
    }

    // notify means: calling this form within this module, notification needed for other part,
    // or it is called from outside then no notification needed.
    public void loadExperiments(boolean notify) {
        ExperimentRPCService.App.getInstance().getExperimentList(publicationID,
                new ExperimentListCallBack(notify, "Failed to retrieve experiments: "));

    }

    public void populateData() {
        int elementIndex = 0;

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
                DeleteImage deleteImage = new DeleteImage("Delete Note " + conditionDTO.getZdbID());
                deleteImage.addClickHandler(new DeleteConditionClickHandler(conditionDTO, this));
                view.addDeleteButton(deleteImage, elementIndex);
                elementIndex++;
                lastCondition = conditionDTO;
            }
        }
    }

    private void displayControlSection(Map<TermEntry, Boolean> visibilityMap) {
        view.addZeco();
        int row = 1;
        for (TermEntry termEntry : visibilityMap.keySet()) {
            if (visibilityMap.get(termEntry))
                view.addTermEntry(termEntry, row++);
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
        ExperimentRPCService.App.getInstance().copyConditions(experimentID, copyConditionIdList,
                new ExperimentListCallBack(true, "Failed to copy conditions: "));
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
        Map<TermEntry, Boolean> visibilityMap = new HashMap<>(4);
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

        ConditionDTO conditionDTO = getConditionFromFrom();
        view.clearError();
        ExperimentRPCService.App.getInstance().createCondition(publicationID, conditionDTO,
                new ExperimentListCallBack(true, "Failed to save condition: "));
    }

    private boolean formIsValidated() {
        if (view.zecoTermEntry.getTermTextBox().hasValidateTerm())
            return true;
        if (view.aoTermEntry.getTermTextBox().hasValidateTerm() && view.aoTermEntry.isVisible())
            return true;
        if (view.goCcTermEntry.getTermTextBox().hasValidateTerm() && view.goCcTermEntry.isVisible())
            return true;
        if (view.taxonTermEntry.getTermTextBox().hasValidateTerm() && view.taxonTermEntry.isVisible())
            return true;
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

    private class DeleteConditionClickHandler implements ClickHandler {

        private ConditionDTO conditionDTO;
        private ConditionAddPresenter presenter;


        public DeleteConditionClickHandler(ConditionDTO conditionDTO, ConditionAddPresenter presenter) {
            this.conditionDTO = conditionDTO;
            this.presenter = presenter;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            String message = "Are you sure you want to delete this condition?";
            if (!Window.confirm(message))
                return;
            view.loadingImage.setVisible(true);

            ExperimentRPCService.App.getInstance().deleteCondition(conditionDTO, new FeatureEditCallBack<List<ExperimentDTO>>("Failed to remove condition: ", presenter) {
                @Override
                public void onSuccess(List<ExperimentDTO> list) {
                    dtoList.clear();
                    dtoList = list;
                    populateData();
                    view.loadingImage.setVisible(false);
                    // notify the create-experiment section
                    ChangeConditionEvent event = new ChangeConditionEvent();
                    AppUtils.EVENT_BUS.fireEvent(event);
                }
            });
        }
    }


    private class ExperimentListCallBack extends ZfinAsyncCallback<List<ExperimentDTO>> {
        private boolean notify;

        public ExperimentListCallBack(boolean notify, String errorMessage) {
            super(errorMessage, ConditionAddPresenter.this.view.errorLabel);
            view.loadingImage.setVisible(false);
            this.notify = notify;
        }

        public void onSuccess(List<ExperimentDTO> experimentList) {
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
                ChangeConditionEvent event = new ChangeConditionEvent();
                AppUtils.EVENT_BUS.fireEvent(event);
            }
        }
    }
}
