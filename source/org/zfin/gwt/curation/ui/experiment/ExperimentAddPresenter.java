package org.zfin.gwt.curation.ui.experiment;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.REST;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.gwt.root.ui.ZfinAsynchronousCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.List;

import static org.zfin.gwt.curation.ui.CurationEntryPoint.experimentService;

public class ExperimentAddPresenter implements HandlesError {

    private ExperimentAddView view;

    private String publicationID;

    private List<ExperimentDTO> dtoList;

    public ExperimentAddPresenter(ExperimentAddView view, String publicationID) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {
        loadExperiments();
    }

    private void loadExperiments() {
        AppUtils.fireAjaxCall(ExperimentModule.getModuleInfo(), AjaxCallEventType.GET_EXPERIMENT_LIST_START);

        try {
            REST.withCallback(new ZfinAsynchronousCallback<List<ExperimentDTO>>("Failed to retrieve experiments: ", view.errorLabel,
                    ExperimentModule.getModuleInfo(), AjaxCallEventType.GET_EXPERIMENT_LIST_STOP) {
                public void onSuccess(Method method, List<ExperimentDTO> experimentList) {
                    super.onFinish();
                    dtoList = experimentList;
                    populateExperiments();
                }
            })
                    .call(experimentService)
                    .getExperiments(publicationID);
        } catch (ValidationException e) {
            e.printStackTrace();
        }

    }

    public void populateExperiments() {
        int elementIndex = 0;

        if (dtoList.isEmpty()) {
            view.emptyDataTable();
            return;
        }
        for (ExperimentDTO dto : dtoList) {
            //view.addExperiment(dto, elementIndex);
            final TextBox experimentTextBox = new TextBox();
            view.addExptTextBox(experimentTextBox, dto, elementIndex);
            DeleteImage deleteImage = new DeleteImage("Delete Experiment " + dto.getZdbID());
            deleteImage.addClickHandler(new DeleteExperimentClickHandler(dto));
            view.addDeleteButton(dto, deleteImage, elementIndex);
            final Button updateButton = new Button();
            updateButton.addClickHandler(new UpdateExperimentClickHandler(dto, experimentTextBox));
            view.addUpdateButton(updateButton, elementIndex);
            elementIndex++;
        }
        view.addConstructionRow(elementIndex);
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
        final ExperimentDTO environmentDTO = getEnvironmentFromForm();
        if(environmentDTO.getName().isEmpty())
            return;
        try {
            REST.withCallback(new ZfinAsynchronousCallback<List<ExperimentDTO>>("Failed to save a Experiment: ", view.errorLabel) {
                public void onSuccess(Method method, List<ExperimentDTO> experimentList) {
                    AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.CREATE_EXPERIMENT, environmentDTO.getName()));
                    dtoList = experimentList;
                    resetGUI();
                    populateExperiments();
                }
            })
                    .call(experimentService)
                    .createExperiment(publicationID, environmentDTO);
        } catch (ValidationException | TermNotFoundException e) {
            // ignore as this call is asynchronous
        }
    }

    private ExperimentDTO getEnvironmentFromForm() {
        ExperimentDTO dto = new ExperimentDTO();
        dto.setName(view.experimentNameAddBox.getText());

        return dto;
    }

    private class DeleteExperimentClickHandler implements ClickHandler {

        private final ExperimentDTO environmentDTO;

        public DeleteExperimentClickHandler(ExperimentDTO environmentDTO) {
            this.environmentDTO = environmentDTO;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            String message = "Are you sure you want to delete this experiment?";
            if (!Window.confirm(message))
                return;

            try {
                REST.withCallback(new ZfinAsynchronousCallback<List<ExperimentDTO>>("Failed to remove Experiment: ", view.errorLabel) {
                    @Override
                    public void onSuccess(Method method, List<ExperimentDTO> list) {
                        AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.REMOVE_EXPERIMENT, environmentDTO.getName()));
                        dtoList = list;
                        resetGUI();
                        populateExperiments();
                    }
                })
                        .call(experimentService)
                        .deleteExperiment(publicationID, environmentDTO);
            } catch (ValidationException | TermNotFoundException e) {
                // ignore as this call is asynchronous
            }
        }
    }

    private class UpdateExperimentClickHandler implements ClickHandler {

        private ExperimentDTO experimentDTO;
        TextBox experimentTextBox;

        public UpdateExperimentClickHandler(ExperimentDTO experimentDTO, TextBox experimentTextBox) {

            this.experimentDTO = experimentDTO;
            this.experimentTextBox = experimentTextBox;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            experimentDTO.setName(experimentTextBox.getValue());
            try {
                REST.withCallback(new ZfinAsynchronousCallback<List<ExperimentDTO>>("Failed to update an Experiment: ", view.errorLabel) {
                    public void onSuccess(Method method, List<ExperimentDTO> experimentList) {
                        AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.UPDATE_EXPERIMENT));
                        dtoList = experimentList;
                        resetGUI();
                        populateExperiments();
                    }
                })
                        .call(experimentService)
                        .updateExperiment(publicationID, experimentDTO);
            } catch (ValidationException | TermNotFoundException e) {
                // ignore as this call is asynchronous
            }
        }
    }

}
