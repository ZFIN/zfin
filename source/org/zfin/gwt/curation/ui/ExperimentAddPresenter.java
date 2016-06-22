package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.event.SelectAutoCompleteEvent;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.DeleteImage;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

public class ExperimentAddPresenter implements HandlesError {

    private ExperimentAddView view;

    private String publicationID;

    private List<EnvironmentDTO> dtoList;

    public ExperimentAddPresenter(ExperimentAddView view, String publicationID) {
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

                populateExperiments();
            }
        });

    }

    private void populateExperiments() {
        int elementIndex = 0;

        if (dtoList.isEmpty()) {
            view.emptyDataTable();
            return;
        }
        for (EnvironmentDTO dto : dtoList) {
            int index = 0;

          view.addExperiment(dto, elementIndex);
            //if (dto.getConditionDTOList()==null) {
                DeleteImage deleteImage = new DeleteImage("Delete Note " + dto.getZdbID());
                deleteImage.addClickHandler(new DeleteExperimentClickHandler(dto, this));
                view.addDeleteButton(deleteImage, elementIndex);
                elementIndex++;
            //}
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



    private void handleDirty() {
        view.addExperimentButton.setEnabled(true);
    }


    public void resetGUI() {

        view.experimentNameAddBox.setText("");

        view.clearError();
        //setVisibility("");
    }

    public void createExperiment() {
       EnvironmentDTO environmentDTO = getEnvironmentFromForm();

        Window.alert(environmentDTO.getName());

       // view.clearError();
        ExperimentRPCService.App.getInstance().createExperiment(publicationID, environmentDTO, new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to save a Experiment: ", view.errorLabel) {
            public void onSuccess(List<EnvironmentDTO> experimentList) {
                ConditionAddView cdView=new ConditionAddView();
                dtoList = experimentList;

                for (EnvironmentDTO dto : experimentList) {
                    resetGUI();

                    cdView.experimentSelectionList.addItem(dto.getName(), dto.getZdbID());
                }
                populateExperiments();
            }
        });
    }

    private boolean formIsValidated() {
       /* if (view.zecoTermEntry.getTermTextBox().hasValidateTerm())
            return true;
        if (view.aoTermEntry.getTermTextBox().hasValidateTerm() && view.aoTermEntry.isVisible())
            return true;
        if (view.goCcTermEntry.getTermTextBox().hasValidateTerm() && view.goCcTermEntry.isVisible())
            return true;
        if (view.taxonTermEntry.getTermTextBox().hasValidateTerm() && view.taxonTermEntry.isVisible())
            return true;*/
        return false;
    }

    private EnvironmentDTO getEnvironmentFromForm() {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setName(view.experimentNameAddBox.getText());

        return dto;
    }

    private class DeleteExperimentClickHandler implements ClickHandler {

        private EnvironmentDTO environmentDTO;
        private ExperimentAddPresenter presenter;


        public DeleteExperimentClickHandler(EnvironmentDTO environmentDTO, ExperimentAddPresenter presenter) {
            this.environmentDTO = environmentDTO;
            this.presenter = presenter;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
           String message = "Are you sure you want to delete this experiment?";
            if (!Window.confirm(message))
                return;

            ExperimentRPCService.App.getInstance().deleteExperiment(environmentDTO, new FeatureEditCallBack<List<EnvironmentDTO>>("Failed to remove Experiment: ", presenter) {
                @Override
                public void onSuccess(List<EnvironmentDTO> list) {
                    dtoList.clear();
                    dtoList = list;
                    populateExperiments();
                }
            });
        }
    }


}
