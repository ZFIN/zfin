package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionAssayDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.StringUtils;

import java.util.*;

/**
 * Expression Experiment zone
 */
public class ExpressionExperimentZoneView extends Composite implements HandlesError {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExpressionExperimentZoneView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, ExpressionExperimentZoneView> {
    }

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    private ExpressionExperimentZonePresenter presenter;
    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    SimpleErrorElement errorElement;

    @UiField
    Image loadingImage;
    @UiField
    VerticalPanel expressionExperimentPanel;
    @UiField
    ToggleHyperlink showSelectExperiments;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();

    private ExperimentFlexTable displayTable;
    private boolean showSelectedExperimentsOnly;
    private Set<ExperimentDTO> selectedExperiments = new HashSet<>(5);
    private List<ExperimentDTO> experiments = new ArrayList<>(15);
    private ExperimentDTO lastSelectedExperiment;
    private ExperimentDTO lastAddedExperiment = new ExperimentDTO();
    // avoid double updates
    private boolean updateButtonInProgress;
    private boolean addButtonInProgress;

    // construction zone
    private Button addButton = new Button("Add");
    private ListBoxWrapper geneList = new ListBoxWrapper();
    private ListBox fishList = new ListBox();
    private ListBox environmentList = new ListBox();
    private ListBox assayList = new ListBox();
    private ListBox antibodyList = new ListBox();
    private ListBox genbankList = new ListBox();
    private Button updateButton = new Button("update");

    // attributes for duplicate row
    private String duplicateRowOriginalStyle;
    private int duplicateRowIndex;


    public ExpressionExperimentZoneView() {
        initWidget(uiBinder.createAndBindUi(this));
        displayTable = new ExperimentFlexTable(HeaderName.getHeaderNames());
        updateButton.addClickHandler(new UpdateExperimentClickListener());
        expressionExperimentPanel.add(displayTable);
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        presenter.onShowHideClick(showHideToggle.isVisible());
    }

    /**
     * Un-select all experiment check boxes.
     */
    public void unselectAllExperiments() {
        selectedExperiments.clear();
        showSelectedExperimentsOnly = false;
        displayTable.uncheckAllRecords();
    }

    /**
     * When an expression record is added update the experiment section about it, ie the number
     * of expression records is incremented by 1.
     */
    public void notifyAddedExpression() {
        for (ExperimentDTO experiment : experiments) {
            for (ExperimentDTO sourceExperiment : selectedExperiments) {
                if (experiment.equals(sourceExperiment))
                    experiment.setNumberOfExpressions(experiment.getNumberOfExpressions() + 1);
            }
        }
        unselectAllExperiments();
        if (showHideToggle.isVisible())
            displayTable.createExperimentTable();
    }

    public void setPresenter(ExpressionExperimentZonePresenter presenter) {
        this.presenter = presenter;
    }

    public void clearSelectedExperiments() {
        selectedExperiments.clear();
    }

    /**
     * Retrieve all selected Experiments.
     *
     * @return set of selected experiments
     */
    public Set<ExperimentDTO> getSelectedExperiments() {
        return Collections.unmodifiableSet(selectedExperiments);
    }

    public void addSelectedExperiment(ExperimentDTO experiment) {
        selectedExperiments.add(experiment);
        displayTable.createExperimentTable();
    }

    /**
     * When an expression record is removed update the experiment about it, ie the number
     * of expression records is decremented by 1.
     *
     * @param sourceExperiment experiment being removed
     */
    public void notifyRemovedExpression(ExperimentDTO sourceExperiment) {
        for (ExperimentDTO experiment : experiments) {
            if (experiment.getExperimentZdbID().equals(sourceExperiment.getExperimentZdbID()))
                experiment.setNumberOfExpressions(experiment.getNumberOfExpressions() - 1);
        }
        unselectAllExperiments();
        if (showHideToggle.isVisible())
            displayTable.createExperimentTable();
    }


    @Override
    public void setError(String message) {

    }

    @Override
    public void clearError() {

    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    class ExperimentFlexTable extends ZfinFlexTable {

        private HeaderName[] headerNames;

        // attributes for last selected row
        private String previousRowStyle;
        private int previousRowIndex;


        ExperimentFlexTable(HeaderName[] headerNames) {
            super(headerNames.length, HeaderName.SELECT.index);
            this.headerNames = headerNames;
            addClickListenerToClearAllEvent(new UpdateExperimentsOnExpressionClickHandler());
            setToggleHyperlink(ToggleLink.SHOW_SELECTED_EXPERIMENTS_ONLY.getText(), ToggleLink.SHOW_ALL_EXPERIMENTS.getText());
            addToggleHyperlinkClickHandler(new ShowSelectedExperimentsClickHandler(showSelectedRecords));
        }

        public void createExperimentTable(List<ExperimentDTO> experimentDTOs) {
            experiments = experimentDTOs;
            createExperimentTable();
        }

        public void createExperimentTable() {
            clearTable();
            // header row index = 0
            createTableHeader();
            int rowIndex = 1;
            ExperimentDTO previousExperiment = null;
            // first element is an odd group element
            int groupIndex = 1;
            List<ExperimentDTO> experimentDTOs;
            if (showSelectedExperimentsOnly) {
                experimentDTOs = new ArrayList<>();
                experimentDTOs.addAll(selectedExperiments);
            } else {
                experimentDTOs = experiments;
            }
            //Window.alert("createExperimentTable.selectedExperiments: "+selectedExperiments.size());
            for (ExperimentDTO experiment : experimentDTOs) {
                // row index minus the header row
                CheckBox checkBox = new CheckBox("");
                checkBox.setTitle(experiment.getExperimentZdbID());
                checkBox.addClickHandler(new ExperimentSelectClickHandler(experiment, checkBox));

                if (selectedExperiments.contains(experiment)) {
                    //Window.alert("contains: ");
                    checkBox.setValue(true);
                }

                //Window.alert("Experiment: " + experiment.getGeneName());
                setWidget(rowIndex, HeaderName.SELECT.getIndex(), checkBox);
                MarkerDTO gene = experiment.getGene();
                if (gene != null)
                    setText(rowIndex, HeaderName.GENE.getIndex(), gene.getName());
                setText(rowIndex, HeaderName.FISH.getIndex(), experiment.getFishName());

                Widget environment = new Label(experiment.getEnvironment().getName());
                environment.setTitle(experiment.getEnvironment().getZdbID());
                setWidget(rowIndex, HeaderName.ENVIRONMENT.getIndex(), environment);
                setText(rowIndex, HeaderName.ASSAY.getIndex(), experiment.getAssay());
                MarkerDTO antibody = experiment.getAntibodyMarker();
                if (antibody != null)
                    setText(rowIndex, HeaderName.ANTIBODY.getIndex(), antibody.getName());
                if (!StringUtils.isEmpty(experiment.getCloneID())) {
                    Label genBankLabel = new Label(experiment.getGenbankNumber());
                    genBankLabel.setTitle(experiment.getCloneName());
                    setWidget(rowIndex, HeaderName.GENBANK.getIndex(), genBankLabel);
                } else {
                    setText(rowIndex, HeaderName.GENBANK.getIndex(), experiment.getGenbankNumber());
                }

                Button delete;
                if (experiment.isUsedInExpressions())
                    delete = new Button("X :" + experiment.getNumberOfExpressions());
                else
                    delete = new Button("X");
                delete.setTitle(experiment.getExperimentZdbID());
                delete.addClickHandler(new ExperimentDeleteClickListener(experiment));
                setWidget(rowIndex, HeaderName.DELETE.getIndex(), delete);
                String previousID = null;
                if (previousExperiment != null) {
                    MarkerDTO previousGene = previousExperiment.getGene();
                    if (previousGene != null)
                        previousID = previousGene.getZdbID();
                }
                MarkerDTO currentGene = experiment.getGene();
                if (currentGene != null)
                    groupIndex = setRowStyle(rowIndex, currentGene.getZdbID(), previousID, groupIndex);
                else
                    groupIndex = setRowStyle(rowIndex, null, previousID, groupIndex);
                rowIndex++;
                previousExperiment = experiment;
            }
            // add horizontal line
            getFlexCellFormatter().setColSpan(rowIndex, 0, HeaderName.getHeaderNames().length);
            createBottomClearAllLinkRow(rowIndex);
            createConstructionZone();
            showHideClearAllLink();
        }

        public void onClick(ClickEvent clickEvent) {
            HTMLTable.Cell cell = getCellForEvent(clickEvent);
            Widget widget = getWidget(cell.getRowIndex(), 0);
            int rowIndex = cell.getRowIndex();
            // only checkboxes are in the first column
            if (widget == null || !(widget instanceof CheckBox))
                return;
            CheckBox checkBox = (CheckBox) widget;
            if (checkBox.getValue())
                checkBox.setValue(false);
            else
                checkBox.setValue(true);
            //Window.alert("CheckBox: "+checkBox.getValue());
            checkBox.fireEvent(clickEvent);
            showHideClearAllLink();
            // reset the previously changed row style
            // if it was modified
            if (previousRowIndex > 0)
                getRowFormatter().setStyleName(previousRowIndex, previousRowStyle);

            previousRowStyle = getRowFormatter().getStyleName(rowIndex);
            previousRowIndex = rowIndex;
            // mark the experiment being modified
            getRowFormatter().setStyleName(rowIndex, "experiment-selected");
            showSelectedRecords.hideHyperlink(isAllUnchecked());
        }

        @Override
        protected void createTableHeader() {
            super.createTableHeader();
            for (HeaderName name : headerNames) {
                if (name.index != 0) {
                    setText(selectionCheckBoxColumn, name.index, name.getName());
                    getRowFormatter().addStyleName(0, "bold");
                }
            }
        }

        @Override
        protected void clearTable() {
            int rowCount = getRowCount();
            // Note: make sure to remove rows in reverse order
            // otherwise you get random displays of records!
            if (rowCount < 2)
                return;

            for (int i = rowCount - 2; i >= 0; i--) {
                removeRow(i);
            }
        }

        protected void createConstructionZone() {
            int rowIndex = getRowCount() + 1;
            addButton.addClickHandler(new AddExperimentClickListener());
            setWidget(rowIndex, HeaderName.SELECT.getIndex(), addButton);
            setWidget(rowIndex, HeaderName.GENE.getIndex(), geneList);
            setWidget(rowIndex, HeaderName.FISH.getIndex(), fishList);
            setWidget(rowIndex, HeaderName.ENVIRONMENT.getIndex(), environmentList);
            setWidget(rowIndex, HeaderName.ASSAY.getIndex(), assayList);
            setWidget(rowIndex, HeaderName.ANTIBODY.getIndex(), antibodyList);
            setWidget(rowIndex, HeaderName.GENBANK.getIndex(), genbankList);
            updateButton.setEnabled(false);
            setWidget(rowIndex - 1, HeaderName.getHeaderNames().length - 1, updateButton);
            setWidget(rowIndex, HeaderName.DELETE.getIndex(), updateButton);
        }


        /**
         * Remove error messages
         * un-mark duplicate experiments
         */
        public void clearErrorMessages() {
            errorElement.clearAllErrors();
            if (duplicateRowIndex > 0) {
                displayTable.getRowFormatter().setStyleName(duplicateRowIndex, duplicateRowOriginalStyle);
            }

        }


        // *************** Handlers, Callbacks, etc.

        /**
         * This Click Handler is activated upon clicking the selection check box in the
         * Experiment display section. It should do two things:
         * 1) copy the values for the experiment into the construction zone
         * 2) select the experiment in the text area of the expression section
         */
        private class ExperimentSelectClickHandler implements ClickHandler {

            private CheckBox checkBox;
            private ExperimentDTO selectedExperiment;


            public ExperimentSelectClickHandler(ExperimentDTO selectedExperiment, CheckBox checkBox) {
                this.checkBox = checkBox;
                this.selectedExperiment = selectedExperiment;
            }

            public void onClick(ClickEvent event) {
                //Window.alert("Exp size: "+selectedExperiments.size());
                if (checkBox.getValue())
                    selectedExperiments.add(selectedExperiment);
                else
                    selectedExperiments.remove(selectedExperiment);
                lastSelectedExperiment = selectedExperiment;
                //Window.alert("Exp size II: "+selectedExperiments.size());

                DOM.eventCancelBubble(Event.getCurrentEvent(), true);
                clearErrorMessages();
                //Window.alert("ExperimentSelectClickListener"+experiment.getExperimentZdbID());
                // store selected experiment for update purposes
                selectGene();
                selectFish();
                selectEnvironment();
                selectAssay();
                selectAntibody();
                selectGenBank();
                updateButton.setEnabled(true);
                displayTable.showHideClearAllLink();
            }

            /**
             * Copy the value of the fish into the construction zone field
             */
            private void selectFish() {
                int numberOfEntries = fishList.getItemCount();
                for (int row = 0; row < numberOfEntries; row++) {
                    String fishID = fishList.getValue(row);
                    String fishName = fishList.getItemText(row);
                    //
                    if (selectedExperiment.getFishID() != null) {
                        if (fishID.equals(selectedExperiment.getFishID())) {
                            fishList.setSelectedIndex(row);
                            break;
                        }
                    } else {
                        if (fishName.equals(selectedExperiment.getFishName())) {
                            fishList.setSelectedIndex(row);
                            break;
                        }
                    }

                }
            }

            /**
             * Copy the value of the environment into the construction zone field
             */
            private void selectEnvironment() {
                int numberOfEntries = environmentList.getItemCount();
                for (int row = 0; row < numberOfEntries; row++) {
                    String environment = environmentList.getItemText(row);
                    if (environment.equals(selectedExperiment.getEnvironment().getName())) {
                        environmentList.setSelectedIndex(row);
                        break;
                    }

                }
            }

            /**
             * Copy the value of the assay into the construction zone field
             */
            private void selectAssay() {
                int numberOfEntries = assayList.getItemCount();
                for (int row = 0; row < numberOfEntries; row++) {
                    String assay = assayList.getValue(row);
                    if (assay.equals(selectedExperiment.getAssay())) {
                        assayList.setSelectedIndex(row);
                        break;
                    }
                }
            }

            /**
             * Copy the value of the assay into the construction zone field
             */
            private void selectGenBank() {
                if (selectedExperiment.getGene() != null) {
                    presenter.readGenbankAccessions(selectedExperiment);
                }
            }

            // create gene list and select the gene of the experiment

            private void selectGene() {
                // first retrieve the full list of genes and then
                // select the gene in question.
                presenter.setGene(selectedExperiment);
            }

            // create antibody list and select the antibody of the experiment

            private void selectAntibody() {
                // first retrieve the full list of genes and then
                // select the gene in question.
                // only get antibody list if assay is compatible
                if (ExpressionAssayDTO.isAntibodyAssay(selectedExperiment.getAssay()) && selectedExperiment.getAntibodyMarker() != null) {
                    presenter.selectAntibody(selectedExperiment);
                } else {
                    antibodyList.setEnabled(false);
                }
            }

        }

        private class AddExperimentClickListener implements ClickHandler {

            public void onClick(ClickEvent event) {
                // do not proceed if it just has been clicked once
                // and is being worked on
                if (addButtonInProgress)
                    return;
                addButtonInProgress = true;
                final ExperimentDTO zoneExperiment = getExperimentFromConstructionZone(true);
                if (!isValidExperiment(zoneExperiment)) {
                    cleanupOnExit();
                    return;
                }
                if (experimentExists(zoneExperiment, true)) {
                    //Window.alert("experiment exists: ");
                    errorElement.setError("Experiment already exists. Experiments have to be unique!");
                    cleanupOnExit();
                    return;
                }

                curationExperimentRPCAsync.createExpressionExperiment(zoneExperiment, new AddExperimentCallback());
            }

            private void cleanupOnExit() {
                addButtonInProgress = false;
            }

        }

        private class ExperimentDeleteClickListener implements ClickHandler {

            private ExperimentDTO experiment;

            public ExperimentDeleteClickListener(ExperimentDTO experiment) {
                this.experiment = experiment;
            }

            public void onClick(ClickEvent event) {
                String message;
                if (experiment.isUsedInExpressions())
                    message = "Are you sure you want to delete this experiment and its " + experiment.getNumberOfExpressions() + " expressions?";
                else
                    message = "Are you sure you want to delete this experiment?";
                if (!Window.confirm(message))
                    return;
                presenter.deleteExperiment(experiment);
            }

        }

        private class AddExperimentCallback extends ZfinAsyncCallback<ExperimentDTO> {
            public AddExperimentCallback() {
                super("Error while creating experiment", errorElement);
            }

            @Override
            public void onSuccess(ExperimentDTO newExperiment) {
                super.onSuccess(newExperiment);
                addButtonInProgress = false;
                presenter.retrieveExperiments();
                if (!showHideToggle.isVisible()) {
                    errorElement.setError("Added new Experiment: " + newExperiment.toString());
                }
                // add this experiment to the expression section
                lastAddedExperiment = newExperiment;
                fireEventSuccess();
            }

        }

        /**
         * Show or hide expression section
         */
        private class ShowSelectedExperimentsClickHandler implements ClickHandler {

            private ToggleHyperlink showExperiments;

            private ShowSelectedExperimentsClickHandler(ToggleHyperlink showExperiments) {
                this.showExperiments = showExperiments;
            }

            /**
             * This onclick handler is called after the intrinsic handler of the ToggleHyperlink
             * has set the text already.
             *
             * @param event click event
             */
            public void onClick(ClickEvent event) {
                boolean showSelectedExperimentsOnly = showExperiments.getToggleStatus();
                //Window.alert("show only "+ showSelectedExperimentsOnly);
                showExperiments(!showSelectedExperimentsOnly);
            }

        }

        private class UpdateExperimentsOnExpressionClickHandler implements ClickHandler {

            public void onClick(ClickEvent event) {
                selectedExperiments.clear();
                resetShowAllRecords();
            }
        }
    }

    /**
     * Check if the experiment already exists in the list.
     * Experiments have to be unique.
     *
     * @param updatedExperiment experiment DTO
     * @param isNewExperiment   true: the experiment is going to be a new experiment added to the list
     *                          false: the experiment is an update
     * @return true if experiment is found in the full list (new experiment) or in the list except itself
     * false if experiment is different from all other experiments
     */
    public boolean experimentExists(ExperimentDTO updatedExperiment, boolean isNewExperiment) {
        int rowIndex = 1;
/*
        Window.alert("Is New: "+isNewExperiment);
        Window.alert("updated experiment: "+updatedExperiment.getExperimentZdbID());
*/
        for (ExperimentDTO experiment : experiments) {
            if (experiment.equals(updatedExperiment)) {
                if (isNewExperiment || (!experiment.getExperimentZdbID().equals(updatedExperiment.getExperimentZdbID()))) {
                    if (showHideToggle.isVisible()) {
                        duplicateRowIndex = rowIndex;
                        duplicateRowOriginalStyle = displayTable.getRowFormatter().getStyleName(rowIndex);
                        displayTable.getRowFormatter().setStyleName(rowIndex, "experiment-duplicate");
                    }
                    return true;
                }
            }
            rowIndex++;
        }

        return false;
    }


    private ExperimentDTO getExperimentFromConstructionZone(boolean newExperiment) {
        ExperimentDTO updatedExperiment = new ExperimentDTO();
        if (!newExperiment)
            updatedExperiment.setExperimentZdbID(lastSelectedExperiment.getExperimentZdbID());
        String assay = assayList.getValue(assayList.getSelectedIndex());
        updatedExperiment.setAssay(assay);
        int index = genbankList.getSelectedIndex();
        if (index > -1) {
            // if none is selected set explicitly to null
            if (index == 0)
                updatedExperiment.setGenbankID(null);
            else {
                updatedExperiment.setGenbankID(genbankList.getValue(index));
                updatedExperiment.setGenbankNumber(genbankList.getItemText(index));
            }
        }
        EnvironmentDTO env = new EnvironmentDTO();
        String environmentID = environmentList.getValue(environmentList.getSelectedIndex());
        String environmentName = environmentList.getItemText(environmentList.getSelectedIndex());
        env.setZdbID(environmentID);
        env.setName(environmentName);
        updatedExperiment.setEnvironment(env);
        // only use the antibody if the selection box is enabled.
        if (antibodyList.isEnabled()) {
            String antibodyID = antibodyList.getValue(antibodyList.getSelectedIndex());
            String antibodyName = antibodyList.getItemText(antibodyList.getSelectedIndex());
            if (StringUtils.isNotEmpty(antibodyID) && !antibodyID.equals(StringUtils.NULL)) {
                MarkerDTO antibody = new MarkerDTO();
                antibody.setZdbID(antibodyID);
                antibody.setName(antibodyName);
                updatedExperiment.setAntibodyMarker(antibody);
            }
        }
        String fishID = fishList.getValue(fishList.getSelectedIndex());
        updatedExperiment.setFishID(fishID);
        String fishName = fishList.getItemText(fishList.getSelectedIndex());
        updatedExperiment.setFishName(fishName);
        String geneID = geneList.getValue(geneList.getSelectedIndex());
        if (StringUtils.isNotEmpty(geneID) && !geneID.equals(StringUtils.NULL)) {
            MarkerDTO gene = new MarkerDTO();
            gene.setZdbID(geneID);
            updatedExperiment.setGene(gene);
        }
        updatedExperiment.setPublicationID(presenter.getPublicationID());
        return updatedExperiment;
    }

    /**
     * Check that the experiment is valid:
     * 1) gene or antibody defined
     * 2) fish defined
     * 3) environment defined
     * 4) assay defined
     *
     * @param experiment experiment DTO
     * @return boolean
     */
    private boolean isValidExperiment(ExperimentDTO experiment) {
        if (experiment.getAntibodyMarker() == null && experiment.getGene() == null) {
            errorElement.setError("You need to select at least a gene or an antibody");
            return false;
        }
        if (StringUtils.isEmpty(experiment.getFishID()) || experiment.getFishID().equals("null")) {
            errorElement.setError("You need to select a fish (genotype).");
            return false;
        }
        if (experiment.getEnvironment() == null || StringUtils.isEmpty(experiment.getEnvironment().getZdbID())) {
            errorElement.setError("You need to select an environment (experiment).");
            return false;
        }
        if (StringUtils.isEmpty(experiment.getAssay())) {
            errorElement.setError("You need to select an assay.");
            return false;
        }
        return true;
    }

    /**
     * This method updates the values in a given row with the provided
     * experiment. Neither the selection box nor the delete button are changed.
     *
     * @param row        row index
     * @param experiment experiment
     */
    private void updateTextInUpdatedRow(int row, ExperimentDTO experiment) {
        if (experiment.getGene() != null)
            displayTable.setText(row, HeaderName.GENE.getIndex(), experiment.getGene().getName());
        displayTable.setText(row, HeaderName.FISH.getIndex(), experiment.getFishName());
        Widget environment = new Label(experiment.getEnvironment().getName());
        environment.setTitle(experiment.getEnvironment().getZdbID());
        displayTable.setWidget(row, HeaderName.ENVIRONMENT.getIndex(), environment);
        displayTable.setText(row, HeaderName.ASSAY.getIndex(), experiment.getAssay());
        if (experiment.getAntibodyMarker() != null) {
            displayTable.setText(row, HeaderName.ANTIBODY.getIndex(), experiment.getAntibodyMarker().getName());
        }
        displayTable.setText(row, HeaderName.GENBANK.getIndex(), experiment.getGenbankNumber());
        // update experiment in list
        int index = 0;
        for (ExperimentDTO currentExperiment : experiments) {
            if (currentExperiment.getExperimentZdbID().equals(experiment.getExperimentZdbID()))
                experiments.set(index, experiment);
            index++;
        }
        lastSelectedExperiment = experiment;
    }


    private class UpdateExperimentAsyncCallback extends ZfinAsyncCallback<ExperimentDTO> {

        private UpdateExperimentAsyncCallback() {
            super("Error while updating experiment", errorElement);
        }

        @Override
        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
            updateButton.setEnabled(true);
            updateButtonInProgress = false;
        }

        @Override
        public void onSuccess(ExperimentDTO updatedExperiment) {
            super.onSuccess(updatedExperiment);
            fireEventSuccess();
            // update inline without reading all experiments again
            //retrieveExperiments();
            int rowCount = displayTable.getRowCount();
            for (int row = 1; row < rowCount; row++) {
                Widget widget = displayTable.getWidget(row, HeaderName.SELECT.getIndex());
                if (widget != null) {
                    if (widget instanceof CheckBox) {
                        CheckBox selectButton = (CheckBox) widget;
                        if (selectButton.getTitle().equals(updatedExperiment.getExperimentZdbID())) {
                            //selectButton.setChecked(true);
                            if (presenter.isDebug())
                                Window.alert(updatedExperiment.toString());
                            updateTextInUpdatedRow(row, updatedExperiment);
                        }
                    }
                }
            }

            updateButton.setEnabled(true);
            updateButtonInProgress = false;
            // update expression section with new experiment attributes
//            expressionSection.retrieveExpressions();
        }

    }

    private class UpdateExperimentClickListener implements ClickHandler {
        public void onClick(ClickEvent event) {
            // do not proceed if it just has been clicked once
            // and is being worked on. It probably is a double-submit from GWT!
            if (updateButtonInProgress)
                return;
            updateButtonInProgress = true;
            updateButton.setEnabled(false);
            //Window.alert(itemText);
            ExperimentDTO updatedExperiment = getExperimentFromConstructionZone(false);
            if (!isValidExperiment(updatedExperiment)) {
                cleanupOnExit();
                return;
            }

            // check if the experiment already exists
            if (experimentExists(updatedExperiment, false)) {
                errorElement.setError("Another experiment with these attributes exists. " +
                        "Experiments have to be unique!");
                cleanupOnExit();
                return;
            }

            if (!Window.confirm("Do you really want to update this record")) {
                cleanupOnExit();
                return;
            }

            curationExperimentRPCAsync.updateExperiment(updatedExperiment, new UpdateExperimentAsyncCallback());
        }

        private void cleanupOnExit() {
            updateButton.setEnabled(true);
            updateButtonInProgress = false;
        }
    }


    public void showExperiments(boolean selectedExperimentsOnly) {
        this.showSelectedExperimentsOnly = selectedExperimentsOnly;
        displayTable.createExperimentTable();
    }


    private enum HeaderName {
        SELECT(0, "Select"),
        GENE(1, "Gene"),
        FISH(2, "Fish"),
        ENVIRONMENT(3, "Environment"),
        ASSAY(4, "Assay"),
        ANTIBODY(5, "Antibody"),
        GENBANK(6, "GenBank"),
        DELETE(7, "Delete");

        private int index;
        private String value;

        HeaderName(int index, String value) {
            this.index = index;
            this.value = value;
        }

        public String getName() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        public static HeaderName[] getHeaderNames() {
            return values();
        }
    }

    protected enum ToggleLink {

        SHOW_SELECTED_EXPERIMENTS_ONLY("Show Selected Experiments Only"),
        SHOW_ALL_EXPERIMENTS("Show All Experiments");

        private String text;

        ToggleLink(String value) {
            this.text = value;
        }

        public String getText() {
            return text;
        }
    }


    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    public SimpleErrorElement getErrorElement() {
        return errorElement;
    }

    public ExperimentFlexTable getDisplayTable() {
        return displayTable;
    }

    public ListBoxWrapper getGeneList() {
        return geneList;
    }

    public ListBox getAntibodyList() {
        return antibodyList;
    }

    public ListBox getAssayList() {
        return assayList;
    }

    public ListBox getEnvironmentList() {
        return environmentList;
    }

    public ListBox getFishList() {
        return fishList;
    }

    public ListBox getGenbankList() {
        return genbankList;
    }

    public ExperimentDTO getLastAddedExperiment() {
        return lastAddedExperiment;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }

    public ShowHideToggle getShowHideToggle() {
        return showHideToggle;
    }
}
