package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.RetrieveStageSelectorCallback;
import org.zfin.gwt.root.ui.SessionSaveService;
import org.zfin.gwt.root.ui.SessionSaveServiceAsync;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * construction zone
 */
public class ExpressionZonePresenter implements Presenter {

    private ExpressionZoneView view;
    private String publicationID;
    private boolean processing = false;

    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionPileStructureDTO> displayedStructures = new ArrayList<ExpressionPileStructureDTO>(10);
    // Typical number of figures used per publication is less than 5.
    private List<FigureDTO> allFigureDtos = new ArrayList<>(5);
    // filter set by the banana bar
    private ExperimentDTO experimentFilter = new ExperimentDTO();
    private String figureID;
    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionFigureStageDTO> displayedExpressions = new ArrayList<ExpressionFigureStageDTO>(15);
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, ExpressionFigureStageDTO> displayTableMap = new HashMap<Integer, ExpressionFigureStageDTO>(15);


    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private SessionSaveServiceAsync sessionRPC = SessionSaveService.App.getInstance();

    public ExpressionZonePresenter(ExpressionZoneView view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
        experimentFilter = new ExperimentDTO();
        experimentFilter.setPublicationID(publicationID);
    }

    public void bind() {
        //view.getStructurePileTable().setRemoveStructureCallBack(new RemovePileStructureCallback());
        addDynamicClickHandler();
    }

    private void addDynamicClickHandler() {

    }

    @Override
    public void go() {
        retrieveConstructionZoneValues();
        retrieveExpressions();
        bind();
    }


    public void setExperimentFilter(ExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
    }

    public void retrieveExpressions() {
        curationRPCAsync.getExpressionsByFilter(experimentFilter, figureID, new RetrieveExpressionsCallback());
    }

    private void retrieveConstructionZoneValues() {
        // figure list
        curationRPCAsync.getFigures(publicationID, new RetrieveFiguresCallback());

        // stage list
        curationRPCAsync.getStages(new RetrieveStageListCallback());

        // stage selector
        sessionRPC.isStageSelectorSingleMode(publicationID, new RetrieveStageSelectorCallback(view.getErrorElement(), view.getStageSelector()));
    }


    public void setError(String message) {
        view.getErrorElement().setText(message);
    }

    public void clearErrorMessages() {
        view.getErrorElement().setError("");
    }

    private void resetUI() {
        view.getErrorElement().clearAllErrors();
        clearErrorMessages();
    }

    public void postUpdateStructuresOnExpression() {
/*
        selectedExpressions.clear();
        showSelectedExpressionOnly = false;
        view.getDisplayTable().uncheckAllRecords();
        experimentSection.unselectAllExperiments();
*/
        Window.alert("updated....");
    }

    public class RetrieveFiguresCallback extends ZfinAsyncCallback<List<FigureDTO>> {


        public RetrieveFiguresCallback() {
            super("Error while reading Figure Filters", view.getErrorElement());
        }

        public void onSuccess(List<FigureDTO> list) {

            allFigureDtos = new ArrayList<>(list);
            view.updateFigureListBox(allFigureDtos);
            //Window.alert("SIZE: " + experiments.size());
            view.setLoadingImageVisibility(false);
        }

        public void onFailureCleanup() {
            view.setLoadingImageVisibility(true);
        }
    }

    /**
     * Callback for reading all stages.
     */
    public class RetrieveStageListCallback extends ZfinAsyncCallback<List<StageDTO>> {

        public RetrieveStageListCallback() {
            super("Error while reading Figure Filters", view.getErrorElement());
        }

        public void onSuccess(List<StageDTO> stages) {

            //Window.alert("SIZE: " + experiments.size());
            view.getStageSelector().setStageList(stages);
            view.setLoadingImageVisibility(false);
        }

        public void onFailureCleanup() {
            view.setLoadingImageVisibility(true);
        }
    }

    private class RetrieveExpressionsCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        public RetrieveExpressionsCallback() {
            super("Error while reading Experiment Filters", view.getErrorElement());
        }

        public void onSuccess(List<ExpressionFigureStageDTO> list) {

            displayedExpressions.clear();
            if (list == null)
                return;

            for (ExpressionFigureStageDTO id : list) {
                ExperimentDTO experiment = id.getExperiment();
                if (experiment.getEnvironment().getName().startsWith("_"))
                    experiment.getEnvironment().setName(experiment.getEnvironment().getName().substring(1));
                displayedExpressions.add(id);
            }
            //Window.alert("SIZE: " + experiments.size());
///            if (sectionVisible)
            view.getDisplayTable().createExpressionTable();
            view.recordAllExpressedTerms();
            curationRPCAsync.getFigureAnnotationCheckmarkStatus(publicationID, new FigureAnnotationCheckmarkStatusCallback());
            view.getDisplayTable().createExpressionTable(displayedExpressions);
            view.setLoadingImageVisibility(false);
        }

        public void onFailureCleanup() {
            view.setLoadingImageVisibility(true);
        }
    }

    private class FigureAnnotationCheckmarkStatusCallback implements AsyncCallback<CheckMarkStatusDTO> {
        public void onFailure(Throwable throwable) {
            if (throwable instanceof PublicationNotFoundException) {
                Window.alert(String.valueOf(throwable));
            } else {
                Window.alert("Fatal exception: " + throwable);
            }
        }

        public void onSuccess(CheckMarkStatusDTO filterValues) {
            //Window.alert("brought back: " + filterValues.getFigureAnnotations().size());
            if (filterValues == null)
                return;

            int maxRows = view.getDisplayTable().getRowCount();
            for (int row = 1; row < maxRows; row++) {
                if (!view.getDisplayTable().isCellPresent(row, 0))
                    continue;
                Widget widget = view.getDisplayTable().getWidget(row, 0);
                if (widget == null || !(widget instanceof CheckBox))
                    continue;

                CheckBox checkBox = (CheckBox) widget;
                for (ExpressionFigureStageDTO dto : filterValues.getFigureAnnotations()) {
                    if (dto.getUniqueID().equals(checkBox.getTitle())) {
                        checkBox.setValue(true);
                        ExpressionFigureStageDTO checkedExpression = displayTableMap.get(row);
///                        selectedExpressions.add(checkedExpression);
                    }
                }
            }
            view.getDisplayTable().showHideClearAllLink();
///            structurePile.updateFigureAnnotations(selectedExpressions);
        }
    }


}
