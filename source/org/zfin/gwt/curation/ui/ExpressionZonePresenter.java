package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.curation.event.AddExpressionExperimentEvent;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.RetrieveStageSelectorCallback;
import org.zfin.gwt.root.ui.SessionSaveService;
import org.zfin.gwt.root.ui.SessionSaveServiceAsync;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;

import java.util.*;

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
    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionFigureStageDTO> displayedExpressions = new ArrayList<>(15);
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, ExpressionFigureStageDTO> displayTableMap = new HashMap<>(15);

    // 20 expressed terms is a bit higher than the average
    // number of expressed terms used in a single publication.
    // Contains all distinct expressed terms for this publication
    private Set<ExpressedTermDTO> expressedTerms = new HashSet<>(20);


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
        curationRPCAsync.getExpressionsByFilter(experimentFilter, figureID, new RetrieveExpressionsCallback());
    }

    private void retrieveConstructionZoneValues() {
        refreshFigure();
        // stage list
        curationRPCAsync.getStages(new RetrieveStageListCallback());

        // stage selector
        sessionRPC.isStageSelectorSingleMode(publicationID, new RetrieveStageSelectorCallback(view.getErrorElement(), view.getStageSelector()));
    }

    public void refreshFigure() {
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

    public void postUpdateStructuresOnExpression() {
        view.postUpdateStructuresOnExpression();
        // re-load expression table
        retrieveExpressions();
    }


    private void recordAllExpressedTerms() {
        expressedTerms.clear();
        for (ExpressionFigureStageDTO expression : this.displayedExpressions) {
            expressedTerms.addAll(expression.getExpressedTerms());
        }
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
        curationRPCAsync.createFigureAnnotations(newFigureAnnotations, new AddExpressionCallback(newFigureAnnotations));

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
        for (ExpressionFigureStageDTO existingExpression : displayedExpressions) {
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
                ExpressionExperimentDTO experiment = id.getExperiment();
                if (experiment.getEnvironment().getName().startsWith("_"))
                    experiment.getEnvironment().setName(experiment.getEnvironment().getName().substring(1));
                displayedExpressions.add(id);
            }
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

    private class AddExpressionCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        public AddExpressionCallback(List<ExpressionFigureStageDTO> experiment) {
            super("Error while creating experiment", view.errorElement);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> newAnnotations) {
            displayedExpressions.addAll(newAnnotations);
            Collections.sort(displayedExpressions);
            view.getDisplayTable().createExpressionTable();
            recordAllExpressedTerms();
            addButtonInProgress = false;
            view.addButton.setEnabled(true);
            clearErrorMessages();
            view.stageSelector.resetGui();
            AppUtils.EVENT_BUS.fireEvent(new AddExpressionExperimentEvent());
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
