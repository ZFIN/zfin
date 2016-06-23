package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.*;

public class ConditionAddPresenter implements HandlesError {

    private ConditionAddView view;

    private String publicationID;

    private List<EnvironmentDTO> dtoList;
    private List<CheckBox> copyConditionsCheckBoxList = new ArrayList<>();

    public ConditionAddPresenter(ConditionAddView view, String publicationID) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {
        loadExperiments();
        setVisibility("");
    }

    public void loadExperiments() {
        ExperimentRPCService.App.getInstance().getExperimentList(publicationID, new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to retrieve experiments: ", view.errorLabel) {
            public void onSuccess(List<EnvironmentDTO> experimentList) {
                dtoList = experimentList;
                for (EnvironmentDTO dto : experimentList) {
                    view.experimentSelectionList.addItem(dto.getName(), dto.getZdbID());
                    view.experimentCopyToSelectionList.addItem(dto.getName(), dto.getZdbID());
                }
                populateData();
            }
        });

    }

    public void populateData() {
        int elementIndex = 0;

        copyConditionsCheckBoxList.clear();
        if (dtoList.isEmpty()) {
            view.emptyDataTable();
            return;
        }
        ConditionDTO lastCondition = null;

        for (EnvironmentDTO dto : dtoList) {
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
        ExperimentRPCService.App.getInstance().copyConditions(experimentID, copyConditionIdList, new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to copy conditions: ", view.errorLabel) {
            public void onSuccess(List<EnvironmentDTO> experimentList) {
                dtoList = experimentList;
                for (EnvironmentDTO dto : experimentList) {
                    view.experimentSelectionList.addItem(dto.getName(), dto.getZdbID());
                    view.experimentCopyToSelectionList.addItem(dto.getName(), dto.getZdbID());
                }
                populateData();
                resetGUI();
            }
        });
    }

    private void enableCopyControls(boolean enable) {
        if (enable)
            view.addCopyControlsPanel();
        else
            setVisibility("");
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
        if (termID == null)
            termID = "";
        List<Boolean> visibilityVector = getVisibilityMatrixOfDependentOntologies(termID);
        Map<TermEntry, Boolean> visibilityMap = new HashMap<>(4);
        int index = 0;
        for (TermEntry termEntry : getListOfTermEntries()) {
            visibilityMap.put(termEntry, visibilityVector.get(index++));
        }
        displayControlSection(visibilityMap);
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
            return;
        }

        ConditionDTO conditionDTO = getConditionFromFrom();
        view.clearError();
        ExperimentRPCService.App.getInstance().createCondition(publicationID, conditionDTO, new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to save a condition: ", view.errorLabel) {
            public void onSuccess(List<EnvironmentDTO> experimentList) {
                dtoList = experimentList;
                for (EnvironmentDTO dto : experimentList) {
                    resetGUI();
                    view.experimentSelectionList.addItem(dto.getName(), dto.getZdbID());
                }
                populateData();
            }
        });
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

            ExperimentRPCService.App.getInstance().deleteCondition(conditionDTO, new FeatureEditCallBack<List<EnvironmentDTO>>("Failed to remove condition: ", presenter) {
                @Override
                public void onSuccess(List<EnvironmentDTO> list) {
                    dtoList.clear();
                    dtoList = list;
                    populateData();
                }
            });
        }
    }


}
