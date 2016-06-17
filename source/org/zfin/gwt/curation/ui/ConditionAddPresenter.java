package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionAddPresenter implements HandlesError {

    private ConditionAddView view;

    private String publicationID;

    private List<EnvironmentDTO> dtoList;

    public ConditionAddPresenter(ConditionAddView view, String publicationID) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {
        loadExperiments();
    }

    private void loadExperiments() {
        ExperimentRPCService.App.getInstance().getExperimentList(publicationID, new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to retrieve experiments: ", view.errorLabel) {
            public void onSuccess(List<EnvironmentDTO> experimentList) {
                dtoList = experimentList;
                for (EnvironmentDTO dto : experimentList) {
                    view.experimentSelectionList.addItem(dto.getName(), dto.getZdbID());
                }
                populateData();
            }
        });

    }

    private void populateData() {
        int elementIndex = 0;

        if (dtoList.isEmpty()) {
            view.emptyDataTable();
            return;
        }
        for (EnvironmentDTO dto : dtoList) {
            int index = 0;
            for (ConditionDTO conditionDTO : dto.getConditionDTOList()) {
                view.addCondition(dto, conditionDTO, elementIndex);
                DeleteImage deleteImage = new DeleteImage("Delete Note " + conditionDTO.getZdbID());
                deleteImage.addClickHandler(new DeleteConditionClickHandler(conditionDTO, this));
                view.addDeleteButton(deleteImage, elementIndex);
                elementIndex++;
            }
        }
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
            ontologyDependencyMap.put("ZECO:0000111", Arrays.asList(true, false, false, false));
            ontologyDependencyMap.put("ZECO:0000239", Arrays.asList(true, false, false, false));
            ontologyDependencyMap.put("ZECO:0000143", Arrays.asList(false, true, false, false));
            ontologyDependencyMap.put("ZECO:0000229", Arrays.asList(false, true, true, false));
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
        List<Boolean> visibilityVector = getVisibilityMatrixOfDependentOntologies(termID);
        for (int index = 0; index < getListOfTermEntries().size(); index++) {
            TermEntry termEntry = getListOfTermEntries().get(index);
            if (visibilityVector == null)
                termEntry.setVisible(false);
            else
                termEntry.setVisible(visibilityVector.get(index));

        }
    }

    private void handleDirty() {
        view.createExperimentConditionButton.setEnabled(true);
    }


    public void resetGUI() {
        view.zecoTermEntry.reset();
        view.aoTermEntry.reset();
        view.goCcTermEntry.reset();
        view.taxonTermEntry.reset();
        view.clearError();
        setVisibility("");
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
        return false;
    }

    private ConditionDTO getConditionFromFrom() {
        ConditionDTO dto = new ConditionDTO();
        dto.setEnvironmentZdbID(view.experimentSelectionList.getSelected());
        dto.setZecoTerm(view.zecoTermEntry.getTermTextBox().getSelectedTerm());
        dto.setAoTerm(view.aoTermEntry.getTermTextBox().getSelectedTerm());
        dto.setGoCCTerm(view.goCcTermEntry.getTermTextBox().getSelectedTerm());
        dto.setTaxonTerm(view.taxonTermEntry.getTermTextBox().getSelectedTerm());
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
