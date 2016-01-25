package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionAssayDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.gwt.root.util.WidgetUtil;

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
    @UiField
    Grid dataTable;

    // construction zone
    @UiField
    Button addButton;
    @UiField
    ListBoxWrapper geneList;
    @UiField
    ListBox fishList;
    @UiField
    ListBox environmentList;
    @UiField
    ListBox assayList;
    @UiField
    ListBox antibodyList;
    @UiField
    ListBox genbankList;
    @UiField
    Button updateButton;

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


    // attributes for duplicate row
    private String duplicateRowOriginalStyle;
    private int duplicateRowIndex;


    public ExpressionExperimentZoneView() {
        initWidget(uiBinder.createAndBindUi(this));
        displayTable = new ExperimentFlexTable(HeaderName.getHeaderNames());
        expressionExperimentPanel.add(displayTable);
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        presenter.onShowHideClick(showHideToggle.isVisible());
    }

    @UiHandler("addButton")
    void onAddModel(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addExpressionExperiment();
    }

    @UiHandler("updateButton")
    void onClickUpdateButton(@SuppressWarnings("unused") ClickEvent event) {
        clearError();
        presenter.updateExperiment();
    }

    @UiHandler("assayList")
    void onAssayChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
        presenter.onAssayChange();
    }

    @UiHandler("environmentList")
    void onEnvironmentChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
    }

    @UiHandler("genbankList")
    void onGenbankChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
    }

    @UiHandler("fishList")
    void onfishChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
    }

    @UiHandler("antibodyList")
    void onAntibodyChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
        presenter.onAntibodyChange();
    }

    @UiHandler("geneList")
    void onGeneChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
        presenter.onGeneChange();
    }


    protected void addGene(MarkerDTO gene, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        if (gene == null) {
            dataTable.setText(row, 0, "");
            return;
        }
        dataTable.setText(elementIndex + 1, 1, gene.getName());
    }

    public void addEnvironment(EnvironmentDTO environment, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 3, environment.getName());
    }

    protected void addFish(String fishName, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 2, fishName);
    }

    protected void addAssay(String assayName, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 4, assayName);
    }

    protected void addDeleteButton(ExperimentDTO experiment, ClickHandler handler, int elementIndex) {
        int row = elementIndex + 1;
        Button delete;
        if (experiment.isUsedInExpressions())
            delete = new Button("X :" + experiment.getNumberOfExpressions());
        else
            delete = new Button("X");
        delete.setTitle(experiment.getExperimentZdbID());
        delete.addClickHandler(handler);

        dataTable.setWidget(row, 7, delete);
    }

    protected CheckBox addCheckBox(ExperimentDTO experiment, ClickHandler handler, int elementIndex) {
        int row = elementIndex + 1;
        CheckBox checkBox = new CheckBox();
        checkBox.setTitle(experiment.getExperimentZdbID());
        checkBox.addClickHandler(handler);
        dataTable.setWidget(row, 0, checkBox);
        return checkBox;
    }

    protected void addGenBank(String genbankName, int elementIndex) {
        int row = elementIndex + 1;
        if (genbankName != null)
            dataTable.setText(row, 6, genbankName);
        else
            dataTable.setText(row, 6, "");
    }

    protected void addAntibody(MarkerDTO antibody, int elementIndex) {
        int row = elementIndex + 1;
        if (antibody != null)
            dataTable.setText(row, 5, antibody.getName());
        else
            dataTable.setText(row, 5, "");
    }

    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    public void removeAllDataRows() {
        dataTable.resizeRows(1);
    }

    protected void endTableUpdate(ClickHandler handler) {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, addButton);
        dataTable.setWidget(lastRow, col++, geneList);
        dataTable.setWidget(lastRow, col++, fishList);
        dataTable.setWidget(lastRow, col++, environmentList);
        dataTable.setWidget(lastRow, col++, assayList);
        dataTable.setWidget(lastRow, col++, antibodyList);
        dataTable.setWidget(lastRow, col++, genbankList);
        dataTable.setWidget(lastRow, col, updateButton);
        dataTable.addClickHandler(handler);
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

    /**
     * Retrieve all selected Experiments.
     *
     * @return set of selected experiments
     */
    public Set<ExperimentDTO> getSelectedExperiments() {
        return Collections.unmodifiableSet(selectedExperiments);
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
        errorElement.setText(message);
    }

    @Override
    public void clearError() {
        errorElement.setError("");
    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    protected void cleanupOnExit() {
        updateButton.setEnabled(true);
        updateButtonInProgress = false;
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

    public Button getUpdateButton() {
        return updateButton;
    }
}
