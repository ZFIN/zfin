package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.curation.event.AddExpressionExperimentEvent;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.RetrieveStageSelectorCallback;
import org.zfin.gwt.root.ui.SessionSaveService;
import org.zfin.gwt.root.ui.SessionSaveServiceAsync;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;

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

    // Typical number of figures used per publication is less than 5.
    private List<FigureDTO> allFigureDtos = new ArrayList<>(5);
    // filter set by the banana bar
    private ExpressionExperimentDTO experimentFilter = new ExpressionExperimentDTO();
    private String figureID;
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, ExpressionFigureStageDTO> displayTableMap = new HashMap<>(15);

    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private SessionSaveServiceAsync sessionRPC = SessionSaveService.App.getInstance();

    // avoid double updates
    private boolean addButtonInProgress;

    public ExpressionZonePresenter(ExpressionZoneView view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
        experimentFilter = new ExpressionExperimentDTO();
        experimentFilter.setPublicationID(publicationID);
    }

    @Override
    public void go() {
        retrieveConstructionZoneValues();
    }


    public void setExperimentFilter(ExpressionExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
    }

    public void retrieveExpressions() {
        view.getLoadingImage().setVisible(true);
        AppUtils.fireAjaxCall(ExpressionModule.getModuleInfo(), AjaxCallEventType.GET_EXPRESSIONS_BY_FILTER_START);
        curationRPCAsync.getExpressionsByFilter(experimentFilter, figureID, new RetrieveExpressionsCallback());
    }

    private void retrieveConstructionZoneValues() {
        refreshFigure();
        // stage list
        AppUtils.fireAjaxCall(ExpressionModule.getModuleInfo(), AjaxCallEventType.GET_STAGE_LIST_START);
        curationRPCAsync.getStages(new RetrieveStageListCallback());

        // stage selector
        AppUtils.fireAjaxCall(ExpressionModule.getModuleInfo(), AjaxCallEventType.IS_STAGE_SELECTOR_SINGLE_MODE_START);
        sessionRPC.isStageSelectorSingleMode(publicationID, new RetrieveStageSelectorCallback(view.getErrorElement(),
                view.getStageSelector(),
                ExpressionModule.getModuleInfo(),
                AjaxCallEventType.IS_STAGE_SELECTOR_SINGLE_MODE_STOP));
    }

    public void refreshFigure() {
        AppUtils.fireAjaxCall(ExpressionModule.getModuleInfo(), AjaxCallEventType.GET_FIGURE_LIST_START);
        curationRPCAsync.getFigures(publicationID, new RetrieveFiguresCallback());
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

    public void postUpdateStructuresOnExpression(List<ExpressionFigureStageDTO> figureStageDTOList) {
        view.getDisplayTable().postUpdateStructures(figureStageDTOList);
    }


    public void addExpressions() {
        // do not proceed if it just has been clicked once
        // and is being worked on
        if (addButtonInProgress) {
            view.addButton.setEnabled(false);
            return;
        }
        addButtonInProgress = true;
        boolean expressionsExist = false;
        List<ExpressionFigureStageDTO> newFigureAnnotations = getExpressionsFromConstructionZone();
        for (ExpressionFigureStageDTO expression : newFigureAnnotations) {
            if (!isValidExperiment(expression)) {
                cleanupOnExit();
                return;
            }
            if (expressionFigureStageExists(expression)) {
                view.errorElement.setError("Expression already exists. Expressions have to be unique!");
                cleanupOnExit();
                expressionsExist = true;
            }
        }
        if (view.stageSelector.isMultiStageMode() && !view.stageSelector.isMultiStageSelected()) {
            view.errorElement.setError("No stage selected.  Please select at least one stage.");
            cleanupOnExit();
            return;
        }
        if (newFigureAnnotations.isEmpty()) {
            view.errorElement.setError("No experiment selected. Please select at least one experiment!");
            cleanupOnExit();
            return;
        }
        if (expressionsExist)
            return;
        view.loadingImage.setVisible(true);
        curationRPCAsync.createFigureAnnotations(newFigureAnnotations, new AddExpressionCallback());

    }

    private void cleanupOnExit() {
        view.addButton.setEnabled(true);
        addButtonInProgress = false;
    }

    /**
     * Check if the expression already exists in the list.
     * Expressions have to be unique.
     *
     * @param expression expression figure stage DTO
     * @return true if experiment is found in the full list (new experiment) or in the list except itself
     * false if experiment is different from all other experiments
     */
    public boolean expressionFigureStageExists(ExpressionFigureStageDTO expression) {
        int rowIndex = 1;
        for (ExpressionFigureStageDTO existingExpression : view.getDisplayTable().getDisplayExpressionList()) {
            if (existingExpression.getUniqueID().equals(expression.getUniqueID())) {
                view.duplicateRowIndex = rowIndex;
                view.duplicateRowOriginalStyle = view.getDisplayTable().getRowFormatter().getStyleName(rowIndex);
                view.getDisplayTable().getRowFormatter().setStyleName(rowIndex, "experiment-duplicate");
                return true;
            }
            rowIndex++;
        }
        return false;
    }


    /**
     * Create a list of Expression records that should be created as selected in the
     * construction zone. Note, there could be more than one experiment selected!
     *
     * @return list of Expression Figure Stage objects.
     */
    private List<ExpressionFigureStageDTO> getExpressionsFromConstructionZone() {
        String figureID = view.figureList.getValue(view.figureList.getSelectedIndex());
        String startStageID = view.stageSelector.getSelectedStartStageID();
        String endStageID = view.stageSelector.getSelectedEndStageID();

        List<ExpressionFigureStageDTO> expressions = new ArrayList<>();
        if (view.stageSelector.isDualStageMode()) {
            addFigureAnnotationsToList(figureID, startStageID, endStageID, expressions);
        } else {
            List<String> stageIDs = view.stageSelector.getSelectedStageIDs();
            for (String stageID : stageIDs) {
                addFigureAnnotationsToList(figureID, stageID, stageID, expressions);
            }
        }
        return expressions;
    }

    private void addFigureAnnotationsToList(String figureID, String startStageID, String endStageID, List<ExpressionFigureStageDTO> expressions) {
        //Window.alert("Experiment size: " + experiments);

        for (ExpressionExperimentDTO experiment : view.getSelectedExperiments()) {
            ExpressionFigureStageDTO newExpression = new ExpressionFigureStageDTO();
            newExpression.setExperiment(experiment);
            StageDTO start = new StageDTO();
            start.setZdbID(startStageID);
            newExpression.setStart(start);
            StageDTO end = new StageDTO();
            end.setZdbID(endStageID);
            newExpression.setEnd(end);
            FigureDTO figureDTO = new FigureDTO();
            figureDTO.setZdbID(figureID);
            newExpression.setFigure(figureDTO);
            expressions.add(newExpression);
        }
    }

    /**
     * Check that the expression is valid:
     * 1) figure defined
     * 2) start and end stage defined
     * 3) experiment ID defined
     *
     * @param expression figure stage DTO
     * @return boolean
     */
    private boolean isValidExperiment(ExpressionFigureStageDTO expression) {
        if (StringUtils.isEmpty(expression.getStart().getZdbID()))
            return false;
        if (StringUtils.isEmpty(expression.getEnd().getZdbID()))
            return false;
        if (StringUtils.isEmpty(expression.getFigure().getZdbID()))
            return false;
        if (expression.getExperiment() == null || StringUtils.isEmpty(expression.getExperiment().getExperimentZdbID())) {
            view.errorElement.setError("No Experiment is selected!");
            return false;
        }
        // check that end stage comes after start stage
        if (view.stageSelector.isDualStageMode()) {
            if (view.stageSelector.validDualStageSelection() != null) {
                view.errorElement.setError(view.stageSelector.validDualStageSelection());
                return false;
            }
        } else {
            if (view.stageSelector.validMultiStageSelection() != null) {
                view.errorElement.setError(view.stageSelector.validMultiStageSelection());
                return false;
            }
        }
        return true;
    }

    public void updateExpressionOnCurationFilter(ExpressionExperimentDTO experimentFilter, String figureID) {
        this.experimentFilter = experimentFilter;
        this.figureID = figureID;
        retrieveExpressions();
        view.updateFigureListBox(allFigureDtos, figureID);
    }


    // Handlers
    public class RetrieveFiguresCallback extends RetrieveSelectionBoxValueCallback {


        public RetrieveFiguresCallback() {
            super(view.figureList, view.errorElement,
                    ExpressionModule.getModuleInfo(), AjaxCallEventType.GET_FIGURE_LIST_STOP);
            setAddAllItem(false);
        }

        public void onSuccess(List<FilterSelectionBoxEntry> list) {
            super.onSuccess(list);
            allFigureDtos = new ArrayList<>((List<FigureDTO>) (List<?>) list);
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
            super("Error while reading Figure Filters", view.getErrorElement(),
                    ExpressionModule.getModuleInfo(), AjaxCallEventType.GET_STAGE_LIST_STOP);
        }

        public void onSuccess(List<StageDTO> stages) {
            super.onFinish();
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
            super("Error while reading Experiment Filters", view.getErrorElement(),
                    ExpressionModule.getModuleInfo(), AjaxCallEventType.GET_EXPRESSIONS_BY_FILTER_STOP);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> list) {
            super.onFinish();
            if (list == null)
                return;
            //Window.alert("Size "+list.size());
            view.getDisplayTable().createExpressionTable(list);
            curationRPCAsync.getFigureAnnotationCheckmarkStatus(publicationID, new FigureAnnotationCheckmarkStatusCallback());
            view.setLoadingImageVisibility(false);
        }

        public void onFailureCleanup() {
            view.setLoadingImageVisibility(true);
        }
    }

    private class AddExpressionCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        public AddExpressionCallback() {
            super("Error while creating experiment", view.errorElement);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> newAnnotations) {
            view.getDisplayTable().addNewExpressions(newAnnotations);
            addButtonInProgress = false;
            view.addButton.setEnabled(true);
            clearErrorMessages();
            view.stageSelector.resetGui();
            Map<ExpressionExperimentDTO, Integer> expressionExperimentDTOMap = new HashMap<>();
            for (ExpressionFigureStageDTO dto : newAnnotations) {
                ExpressionExperimentDTO experimentDTO = dto.getExperiment();
                if (expressionExperimentDTOMap.containsKey(experimentDTO)) {
                    expressionExperimentDTOMap.put(experimentDTO, expressionExperimentDTOMap.get(experimentDTO) + 1);
                } else {
                    expressionExperimentDTOMap.put(experimentDTO, 1);
                }
            }
            AppUtils.EVENT_BUS.fireEvent(new AddExpressionExperimentEvent(expressionExperimentDTOMap));
            view.loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            view.loadingImage.setVisible(false);
        }

    }


    private class FigureAnnotationCheckmarkStatusCallback implements AsyncCallback<CheckMarkStatusDTO> {
        public void onFailure(Throwable throwable) {
            if (throwable instanceof PublicationNotFoundException) {
                GWT.log(String.valueOf(throwable));
            } else {
                GWT.log("Fatal exception: " + throwable);
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
                    if (String.valueOf(dto.getID()).equals(checkBox.getTitle())) {
                        checkBox.setValue(true);
                        ExpressionFigureStageDTO checkedExpression = displayTableMap.get(row);
                    }
                }
            }
            view.getDisplayTable().showHideClearAllLink();
        }
    }


}
