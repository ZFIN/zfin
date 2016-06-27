package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import org.zfin.gwt.curation.event.ChangeExperimentEvent;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.List;

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

    public void populateExperiments() {
        int elementIndex = 0;

        if (dtoList.isEmpty()) {
            view.emptyDataTable();
            return;
        }
        for (EnvironmentDTO dto : dtoList) {
            view.addExperiment(dto, elementIndex);
            final TextBox experimentTextBox = new TextBox();
            view.addExptTextBox(experimentTextBox, dto, elementIndex);
            DeleteImage deleteImage = new DeleteImage("Delete Experiment " + dto.getZdbID());
            deleteImage.addClickHandler(new DeleteExperimentClickHandler(dto));
            view.addDeleteButton(dto, deleteImage, elementIndex);
            final Button updateButton = new Button();
            updateButton.addClickHandler(new UpdateExperimentClickHandler(dto, experimentTextBox));
            view.addUpdateButton(dto, updateButton, elementIndex);
            elementIndex++;

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

    public void resetGUI() {
        view.experimentNameAddBox.setText("");
        view.clearError();
    }

    public void createExperiment() {
        EnvironmentDTO environmentDTO = getEnvironmentFromForm();

        ExperimentRPCService.App.getInstance().createExperiment(publicationID, environmentDTO, new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to save a Experiment: ", view.errorLabel) {
            public void onSuccess(List<EnvironmentDTO> experimentList) {
                ChangeExperimentEvent event = new ChangeExperimentEvent();
                AppUtils.EVENT_BUS.fireEvent(event);
                dtoList = experimentList;
                resetGUI();
                populateExperiments();
            }
        });
    }

    private EnvironmentDTO getEnvironmentFromForm() {
        EnvironmentDTO dto = new EnvironmentDTO();
        dto.setName(view.experimentNameAddBox.getText());

        return dto;
    }

    private class DeleteExperimentClickHandler implements ClickHandler {

        private EnvironmentDTO environmentDTO;

        public DeleteExperimentClickHandler(EnvironmentDTO environmentDTO) {
            this.environmentDTO = environmentDTO;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            String message = "Are you sure you want to delete this experiment?";
            if (!Window.confirm(message))
                return;

            ExperimentRPCService.App.getInstance().deleteExperiment(environmentDTO, new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to remove Experiment: ", view.errorLabel) {
                @Override
                public void onSuccess(List<EnvironmentDTO> list) {
                    fireEventSuccess();
                    ChangeExperimentEvent event = new ChangeExperimentEvent();
                    AppUtils.EVENT_BUS.fireEvent(event);
                    dtoList = list;
                    resetGUI();
                    populateExperiments();
                }
            });
        }
    }

    private class UpdateExperimentClickHandler implements ClickHandler {

        private EnvironmentDTO environmentDTO;
        TextBox experimentTextBox;

        public UpdateExperimentClickHandler(EnvironmentDTO environmentDTO, TextBox experimentTextBox) {

            this.environmentDTO = environmentDTO;
            this.experimentTextBox = experimentTextBox;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {

            ExperimentRPCService.App.getInstance().updateExperiment(environmentDTO, experimentTextBox.getValue(), new ZfinAsyncCallback<List<EnvironmentDTO>>("Failed to update Experiment: ", view.errorLabel) {
                @Override
                public void onSuccess(List<EnvironmentDTO> list) {
                    fireEventSuccess();
                    //Window.alert("Feature successfully created");

                    ChangeExperimentEvent event = new ChangeExperimentEvent();
                    AppUtils.EVENT_BUS.fireEvent(event);
                    dtoList = list;
                    resetGUI();
                    populateExperiments();
                }
            });
        }
    }

}
