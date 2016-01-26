package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.curation.event.SelectExperimentEvent;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ListBoxWrapper;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;

import java.util.*;

/**
 * construction zone
 */
public class ExpressionExperimentZonePresenter implements Presenter {

    private ExpressionExperimentZoneView view;
    private String publicationID;
    private boolean debug;
    private boolean addButtonInProgress;
    private ExperimentDTO lastAddedExperiment = new ExperimentDTO();
    private ExperimentDTO lastSelectedExperiment;
    private Set<ExperimentDTO> selectedExperiments = new HashSet<>(5);
    // avoid double updates
    private boolean updateButtonInProgress;
    private boolean showSelectedExperimentsOnly;
    private int duplicateRowIndex;
    private String duplicateRowOriginalStyle;

    // filter set by the banana bar
    private ExperimentDTO experimentFilter = new ExperimentDTO();

    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();

    private List<ExperimentDTO> experimentList = new ArrayList<>(15);

    public ExpressionExperimentZonePresenter(ExpressionExperimentZoneView view, String publicationID, boolean debug) {
        this.view = view;
        this.publicationID = publicationID;
        this.debug = debug;
        experimentFilter = new ExperimentDTO();
        experimentFilter.setPublicationID(publicationID);
        view.setPresenter(this);
    }

    public void updateExperimentOnCurationFilter(ExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
        retrieveExperiments();
    }

    /**
     * When an expression record is added update the experiment section about it, ie the number
     * of expression records is incremented by 1.
     */
    public void notifyAddedExpression() {
        for (ExperimentDTO experiment : experimentList) {
            for (ExperimentDTO sourceExperiment : selectedExperiments) {
                if (experiment.equals(sourceExperiment))
                    experiment.setNumberOfExpressions(experiment.getNumberOfExpressions() + 1);
            }
        }
        finishExpressionNotification();
    }

    private void finishExpressionNotification() {
        unselectAllExperiments();
        if (view.showHideToggle.isVisible())
            populateDataTable();
    }

    /**
     * When an expression record is removed update the experiment about it, ie the number
     * of expression records is decremented by 1.
     *
     * @param sourceExperiment experiment being removed
     */
    public void notifyRemovedExpression(ExperimentDTO sourceExperiment) {
        for (ExperimentDTO experiment : experimentList) {
            if (experiment.getExperimentZdbID().equals(sourceExperiment.getExperimentZdbID()))
                experiment.setNumberOfExpressions(experiment.getNumberOfExpressions() - 1);
        }
        finishExpressionNotification();
    }


    public void addExpressionExperiment() {
        // do not proceed if it just has been clicked once
        // and is being worked on
        if (addButtonInProgress)
            return;
        addButtonInProgress = true;
        final ExperimentDTO zoneExperiment = getExperimentFromConstructionZone(true);
        if (!isValidExperiment(zoneExperiment)) {
            view.cleanupOnExit();
            return;
        }
        if (experimentExists(zoneExperiment, true)) {
            //Window.alert("experiment exists: ");
            view.setError("Experiment already exists. Experiments have to be unique!");
            view.cleanupOnExit();
            return;
        }

        curationExperimentRPCAsync.createExpressionExperiment(zoneExperiment, new AddExperimentCallback());

    }

    protected void populateDataTable() {
        int elementIndex = 0;
        for (ExperimentDTO experiment : experimentList) {
            if (showSelectedExperimentsOnly && !selectedExperiments.contains(experiment))
                continue;
            view.addGene(experiment.getGene(), elementIndex);
            view.addFish(experiment.getFishName(), elementIndex);
            view.addEnvironment(experiment.getEnvironment(), elementIndex);
            view.addAssay(experiment.getAssay(), elementIndex);
            view.addAntibody(experiment.getAntibodyMarker(), elementIndex);
            view.addGenBank(experiment.getGenbankNumber(), elementIndex);
            CheckBox checkBox = view.addCheckBox(experiment, new ExperimentSelectClickHandler(experiment), elementIndex);
            view.addDeleteButton(experiment, new ExperimentDeleteClickListener(experiment), elementIndex);
            elementIndex++;
            if (selectedExperiments.contains(experiment))
                checkBox.setValue(true);
            else
                checkBox.setValue(false);
        }
        view.endTableUpdate(new ExperimentSelectClickHandler(null));
    }

    protected void onAssayChange() {
        String itemText = view.getAssayList().getItemText(view.getAssayList().getSelectedIndex());
        //Window.alert(itemText);
        if (ExpressionAssayDTO.isAntibodyAssay(itemText)) {
            view.getAntibodyList().setEnabled(true);
            String geneID = view.getGeneList().getValue(view.getGeneList().getSelectedIndex());
            curationExperimentRPCAsync.readAntibodiesByGene(publicationID, geneID, new RetrieveAntibodyList());
        } else
            view.getAntibodyList().setEnabled(false);
    }

    public void onAntibodyChange() {
        String antibodyID = view.getAntibodyList().getValue(view.getAntibodyList().getSelectedIndex());
        //Window.alert(antibodyID);
        curationExperimentRPCAsync.readGenesByAntibody(publicationID, antibodyID, new RetrieveGeneListByAntibodyCallBack());
    }

    public void onGeneChange() {
        String geneID = view.getGeneList().getValue(view.getGeneList().getSelectedIndex());
        String assayName = view.getAssayList().getItemText(view.getAssayList().getSelectedIndex());
        //Window.alert(itemText);
        // only fetch antibodies if the right assay is selected
        if (ExpressionAssayDTO.isAntibodyAssay(assayName)) {
            curationExperimentRPCAsync.readAntibodiesByGene(publicationID, geneID, new RetrieveAntibodyList());
        }
        curationExperimentRPCAsync.readGenbankAccessions(publicationID, geneID, new GenbankSelectionListAsyncCallback(null));
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
        for (ExperimentDTO experiment : experimentList) {
            if (experiment.equals(updatedExperiment)) {
                if (isNewExperiment || (!experiment.getExperimentZdbID().equals(updatedExperiment.getExperimentZdbID()))) {
                    if (view.showHideToggle.isVisible()) {
                        duplicateRowIndex = rowIndex;
                        duplicateRowOriginalStyle = view.dataTable.getRowFormatter().getStyleName(rowIndex);
                        view.dataTable.getRowFormatter().setStyleName(rowIndex, "experiment-duplicate");
                    }
                    return true;
                }
            }
            rowIndex++;
        }

        return false;
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
            view.setError("You need to select at least a gene or an antibody");
            return false;
        }
        if (StringUtils.isEmpty(experiment.getFishID()) || experiment.getFishID().equals("null")) {
            view.setError("You need to select a fish (genotype).");
            return false;
        }
        if (experiment.getEnvironment() == null || StringUtils.isEmpty(experiment.getEnvironment().getZdbID())) {
            view.setError("You need to select an environment (experiment).");
            return false;
        }
        if (StringUtils.isEmpty(experiment.getAssay())) {
            view.setError("You need to select an assay.");
            return false;
        }
        return true;
    }

    private ExperimentDTO getExperimentFromConstructionZone(boolean newExperiment) {
        ExperimentDTO updatedExperiment = new ExperimentDTO();
        if (!newExperiment)
            updatedExperiment.setExperimentZdbID(lastSelectedExperiment.getExperimentZdbID());
        String assay = view.getAssayList().getValue(view.getAssayList().getSelectedIndex());
        updatedExperiment.setAssay(assay);
        int index = view.getGenbankList().getSelectedIndex();
        if (index > -1) {
            // if none is selected set explicitly to null
            if (index == 0)
                updatedExperiment.setGenbankID(null);
            else {
                updatedExperiment.setGenbankID(view.getGenbankList().getValue(index));
                updatedExperiment.setGenbankNumber(view.getGenbankList().getItemText(index));
            }
        }
        EnvironmentDTO env = new EnvironmentDTO();
        String environmentID = view.getEnvironmentList().getValue(view.getEnvironmentList().getSelectedIndex());
        String environmentName = view.getEnvironmentList().getItemText(view.getEnvironmentList().getSelectedIndex());
        env.setZdbID(environmentID);
        env.setName(environmentName);
        updatedExperiment.setEnvironment(env);
        // only use the antibody if the selection box is enabled.
        if (view.getAntibodyList().isEnabled()) {
            String antibodyID = view.getAntibodyList().getValue(view.getAntibodyList().getSelectedIndex());
            String antibodyName = view.getAntibodyList().getItemText(view.getAntibodyList().getSelectedIndex());
            if (StringUtils.isNotEmpty(antibodyID) && !antibodyID.equals(StringUtils.NULL)) {
                MarkerDTO antibody = new MarkerDTO();
                antibody.setZdbID(antibodyID);
                antibody.setName(antibodyName);
                updatedExperiment.setAntibodyMarker(antibody);
            }
        }
        String fishID = view.getFishList().getValue(view.getFishList().getSelectedIndex());
        updatedExperiment.setFishID(fishID);
        String fishName = view.getFishList().getItemText(view.getFishList().getSelectedIndex());
        updatedExperiment.setFishName(fishName);
        String geneID = view.getGeneList().getValue(view.getGeneList().getSelectedIndex());
        if (StringUtils.isNotEmpty(geneID) && !geneID.equals(StringUtils.NULL)) {
            MarkerDTO gene = new MarkerDTO();
            gene.setZdbID(geneID);
            updatedExperiment.setGene(gene);
        }
        updatedExperiment.setPublicationID(publicationID);
        return updatedExperiment;
    }

    public void updateExperiment() {
        // do not proceed if it just has been clicked once
        // and is being worked on. It probably is a double-submit from GWT!
        if (updateButtonInProgress)
            return;
        updateButtonInProgress = true;
        view.updateButton.setEnabled(false);
        //Window.alert(itemText);
        ExperimentDTO updatedExperiment = getExperimentFromConstructionZone(false);
        if (!isValidExperiment(updatedExperiment)) {
            cleanupOnExit();
            return;
        }

        // check if the experiment already exists
        if (experimentExists(updatedExperiment, false)) {
            view.setError("Another experiment with these attributes exists. " +
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

    protected void cleanupOnExit() {
        view.updateButton.setEnabled(true);
        updateButtonInProgress = false;
    }


    //////////////////////////////// Handler and Listener  ////////////////////////////

    @Override
    public void go() {
        view.showSelectedAllLink.addClickHandler(new ShowSelectedExperimentsClickHandler());
        view.clearLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                showSelectedExperimentsOnly = false;
                selectedExperiments.clear();
                populateDataTable();
                view.showToggleLinks(false);
            }
        });
        loadSectionVisibility();
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationExperimentRPCAsync.readExperimentSectionVisibility(publicationID,
                new RetrieveSectionVisibilityCallback(message));
    }

    private void retrieveConstructionZoneValues() {
        // gene list
        curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(null));

        // fish (genotype) list
        String message = "Error while reading Fish";
        curationExperimentRPCAsync.getFishList(publicationID,
                new RetrieveDTOListCallBack<FishDTO>(view.getFishList(), message, view.errorElement));

        // environment list
        message = "Error while reading the environment";
        curationExperimentRPCAsync.getEnvironments(publicationID,
                new RetrieveEnvironmentListCallBack(view.getEnvironmentList(), message, view.errorElement));

        // assay list
        message = "Error while reading the assay list";
        curationExperimentRPCAsync.getAssays(new RetrieveAssayListCallback(message));

        // antibody list
        curationExperimentRPCAsync.getAntibodies(publicationID, new AntibodySelectionListAsyncCallback(null));
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                view.getLoadingImage().setVisible(false);
            }
        });
    }

    // Retrieve experiments from the server

    protected void retrieveExperiments() {
        curationExperimentRPCAsync.getExperimentsByFilter(experimentFilter, new RetrieveExperimentsCallback());
    }

    public void setExperimentFilter(ExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
    }

    public void clearErrorMessages() {
        view.clearError();
    }

    /**
     * Un-select all experiment check boxes.
     */
    public void unselectExperiment(ExperimentDTO experiment) {
        selectedExperiments.remove(experiment);
        populateDataTable();
    }

    /**
     * Set a single experiment (check mark).
     * All other experiments that may be checked are un-checked.
     *
     * @param experiment Experiment DTO
     */
    public void setSingleExperiment(ExperimentDTO experiment) {
        selectedExperiments.add(experiment);
        populateDataTable();
        showSelectedExperimentsOnly = false;
    }


    public void selectAntibody(ExperimentDTO selectedExperiment) {
        curationExperimentRPCAsync.getAntibodies(publicationID,
                new AntibodySelectionListAsyncCallback(selectedExperiment.getAntibodyMarker().getZdbID()));
    }

    public void setGene(ExperimentDTO selectedExperiment) {
        curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(selectedExperiment.getGene()));
    }

    public void readGenbankAccessions(ExperimentDTO selectedExperiment) {
        String geneID = selectedExperiment.getGene().getZdbID();
        String genBankID = selectedExperiment.getGenbankID();
        curationExperimentRPCAsync.readGenbankAccessions(publicationID, geneID, new GenbankSelectionListAsyncCallback(genBankID));
    }

    public void deleteExperiment(final ExperimentDTO experiment) {
        curationExperimentRPCAsync.deleteExperiment(experiment.getExperimentZdbID(), new DeleteExperimentCallback(experiment));
    }


    public void updateGenes() {
        MarkerDTO lastAddedMarker = view.getLastAddedExperiment().getGene();
        curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(lastAddedMarker));
    }

    public void onShowHideClick(boolean visibility) {
        String errorMessage = "Error while trying to save experiment visibility";
        curationExperimentRPCAsync.setExperimentVisibilitySession(publicationID, visibility,
                new VoidAsyncCallback(errorMessage, view.errorElement, null));
        retrieveExperiments();
/////TODO         if (view.getDisplayTable().getRowCount() == 0)
        ///  retrieveConstructionZoneValues();
    }

    private void showHideClearAllLink() {
        if (selectedExperiments.size() > 0) {
            view.showToggleLinks(true);
        } else {
            view.showToggleLinks(false);
        }
    }

    public Set<ExperimentDTO> getSelectedExperiments() {
        return selectedExperiments;
    }

    public void unselectAllExperiments() {
        selectedExperiments.clear();
        view.showToggleLinks(false);
    }


//////////////////////// Handler

    private class RetrieveAssayListCallback extends ZfinAsyncCallback<List<String>> {
        public RetrieveAssayListCallback(String message) {
            super(message, view.errorElement);
        }

        @Override
        public void onSuccess(List<String> assays) {
            //Window.alert("brought back: " + experiments.size() );
            ListBox assayList = view.getAssayList();
            assayList.clear();
            for (String assay : assays) {
                assayList.addItem(assay);
            }
        }

    }

    /**
     * This Click Handler is activated upon clicking the selection check box in the
     * Experiment display section. It should do two things:
     * 1) copy the values for the experiment into the construction zone
     * 2) select the experiment in the text area of the expression section
     */
    private class ExperimentSelectClickHandler implements ClickHandler {

        private ExperimentDTO selectedExperiment;


        public ExperimentSelectClickHandler(ExperimentDTO selectedExperiment) {
            this.selectedExperiment = selectedExperiment;
        }

        public void onClick(ClickEvent event) {
            boolean isCheckBoxClick = false;
            if (event.getSource() instanceof CheckBox)
                isCheckBoxClick = true;
            HTMLTable.Cell cell = view.dataTable.getCellForEvent(event);
            Widget widget = view.dataTable.getWidget(cell.getRowIndex(), 0);
            int rowIndex = cell.getRowIndex();

            // only checkboxes are in the first column
            if (widget == null || !(widget instanceof CheckBox))
                return;
            CheckBox checkBox = (CheckBox) widget;

            DOM.eventCancelBubble(Event.getCurrentEvent(), false);
            selectedExperiment = experimentList.get(rowIndex - 1);
            // if click came from checkbox event the check has already happened.
            if (!isCheckBoxClick)
                checkBox.setValue(!checkBox.getValue());
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
            setAntibody();
            selectGenBank();
            view.updateButton.setEnabled(true);
            SelectExperimentEvent selectExperimentEvent = new SelectExperimentEvent(selectedExperiment);
            AppUtils.EVENT_BUS.fireEvent(selectExperimentEvent);
            showHideClearAllLink();
        }

        /**
         * Copy the value of the fish into the construction zone field
         */
        private void selectFish() {
            int numberOfEntries = view.getFishList().getItemCount();
            for (int row = 0; row < numberOfEntries; row++) {
                String fishID = view.getFishList().getValue(row);
                String fishName = view.getFishList().getItemText(row);
                //
                if (selectedExperiment.getFishID() != null) {
                    if (fishID.equals(selectedExperiment.getFishID())) {
                        view.getFishList().setSelectedIndex(row);
                        break;
                    }
                } else {
                    if (fishName.equals(selectedExperiment.getFishName())) {
                        view.getFishList().setSelectedIndex(row);
                        break;
                    }
                }

            }
        }

        /**
         * Copy the value of the environment into the construction zone field
         */
        private void selectEnvironment() {
            int numberOfEntries = view.getEnvironmentList().getItemCount();
            for (int row = 0; row < numberOfEntries; row++) {
                String environment = view.getEnvironmentList().getItemText(row);
                if (environment.equals(selectedExperiment.getEnvironment().getName())) {
                    view.getEnvironmentList().setSelectedIndex(row);
                    break;
                }

            }
        }

        /**
         * Copy the value of the assay into the construction zone field
         */
        private void selectAssay() {
            int numberOfEntries = view.getAssayList().getItemCount();
            for (int row = 0; row < numberOfEntries; row++) {
                String assay = view.getAssayList().getValue(row);
                if (assay.equals(selectedExperiment.getAssay())) {
                    view.getAssayList().setSelectedIndex(row);
                    break;
                }
            }
        }

        /**
         * Copy the value of the assay into the construction zone field
         */
        private void selectGenBank() {
            if (selectedExperiment.getGene() != null) {
                readGenbankAccessions(selectedExperiment);
            }
        }

        // create gene list and select the gene of the experiment

        private void selectGene() {
            // first retrieve the full list of genes and then
            // select the gene in question.
            setGene(selectedExperiment);
        }

        // create antibody list and select the antibody of the experiment

        private void setAntibody() {
            // first retrieve the full list of genes and then
            // select the gene in question.
            // only get antibody list if assay is compatible
            if (ExpressionAssayDTO.isAntibodyAssay(selectedExperiment.getAssay()) && selectedExperiment.getAntibodyMarker() != null) {
                selectAntibody(selectedExperiment);
            } else {
                view.getAntibodyList().setEnabled(false);
            }
        }

    }

    private class AddExperimentCallback extends ZfinAsyncCallback<ExperimentDTO> {
        public AddExperimentCallback() {
            super("Error while creating experiment", view.errorElement);
        }

        @Override
        public void onSuccess(ExperimentDTO newExperiment) {
            super.onSuccess(newExperiment);
            addButtonInProgress = false;
            retrieveExperiments();
            // add this experiment to the expression section
            lastAddedExperiment = newExperiment;
        }

    }


    private class RetrieveExperimentsCallback extends ZfinAsyncCallback<List<ExperimentDTO>> {

        public RetrieveExperimentsCallback() {
            super("Error while reading Experiment Filters", view.errorElement, view.loadingImage);
        }

        @Override
        public void onSuccess(List<ExperimentDTO> list) {
            super.onSuccess(list);
            experimentList.clear();
            for (ExperimentDTO id : list) {
                if (id.getEnvironment().getName().startsWith("_"))
                    id.getEnvironment().setName(id.getEnvironment().getName().substring(1));
                experimentList.add(id);
            }
            Collections.sort(experimentList);
            //Window.alert("SIZE: " + experiments.size());
            populateDataTable();
        }

    }

    private class RetrieveGeneListByAntibodyCallBack extends ZfinAsyncCallback<List<MarkerDTO>> {
        public RetrieveGeneListByAntibodyCallBack() {
            super("Error while reading genes by antibodies", view.errorElement);
        }

        @Override
        public void onSuccess(List<MarkerDTO> genes) {
            //                Window.alert("brought back: " + genes.size() );
            ListBox geneList = view.getGeneList();
            String selectedGeneID = geneList.getValue(geneList.getSelectedIndex());
            //Window.alert("Selected Gene: " + selectedGeneID);
            geneList.clear();
            geneList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO gene : genes) {
                geneList.addItem(gene.getName(), gene.getZdbID());
                // make sure the selected gene is still selected
                if (gene.getZdbID().equals(selectedGeneID))
                    geneList.setItemSelected(rowIndex, true);
                rowIndex++;
            }
        }

    }

    private class DeleteExperimentCallback extends ZfinAsyncCallback<Void> {

        private ExperimentDTO experiment;

        DeleteExperimentCallback(ExperimentDTO experiment) {
            super("Error while deleting Experiment", view.errorElement);
            this.experiment = experiment;
        }

        @Override
        public void onSuccess(Void exp) {
            experimentList.remove(experiment);
            populateDataTable();
            // also remove the figure annotations that were used with this experiments
///            expressionSection.removeFigureAnnotations(experiment);
///            fireEventSuccess();
        }

    }

    private class GenbankSelectionListAsyncCallback extends ZfinAsyncCallback<List<ExperimentDTO>> {

        private String selectedGenBankID;

        public GenbankSelectionListAsyncCallback(String genBankID) {
            super("Error retrieving GenBank list", view.errorElement);
            this.selectedGenBankID = genBankID;
        }

        @Override
        public void onSuccess(List<ExperimentDTO> accessions) {
            ListBox genbankList = view.getGenbankList();
            genbankList.clear();
            genbankList.addItem("");
            int rowIndex = 1;
            if (isDebug())
                Window.alert("Selected GeneBank ID: " + selectedGenBankID);
            for (ExperimentDTO accession : accessions) {
                genbankList.addItem(accession.getGenbankNumber(), accession.getGenbankID());
                if (selectedGenBankID != null && accession.getGenbankID().equals(selectedGenBankID)) {
                    genbankList.setSelectedIndex(rowIndex);
                }
                rowIndex++;
            }
        }

    }

    /**
     * Callback class to populate the antibody  selection box in the construction zone.
     * This class is called when:
     * 1) initializing the construction zone values
     * 2) copying an existing experiment into the construction zone and
     * selecting the antibody of the selected experiment
     */
    class AntibodySelectionListAsyncCallback extends ZfinAsyncCallback<List<MarkerDTO>> {

        private String selectedAntibodyID;

        private AntibodySelectionListAsyncCallback(String selectedAntibodyID) {
            super("Error reading antibody list", view.errorElement);
            this.selectedAntibodyID = selectedAntibodyID;
        }

        @Override
        public void onSuccess(List<MarkerDTO> antibodies) {
            //Window.alert("brought back: " + experiments.size() );
            ListBox antibodyList = view.getAntibodyList();
            ListBox assayList = view.getAssayList();
            antibodyList.clear();
            antibodyList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO antibody : antibodies) {
                antibodyList.addItem(antibody.getName(), antibody.getZdbID());
                if (selectedAntibodyID != null && antibody.getZdbID().equals(selectedAntibodyID))
                    antibodyList.setSelectedIndex(rowIndex);
                rowIndex++;
            }
            int selectedIndex = assayList.getSelectedIndex();
            if (selectedIndex == -1) {
                //Window.alert("No Assay populated yet.");
                return;
            }

            String itemText = assayList.getItemText(selectedIndex);
            // enable list if assay is compatible with assay selection
            if (ExpressionAssayDTO.isAntibodyAssay(itemText))
                antibodyList.setEnabled(true);
            else
                antibodyList.setEnabled(false);
        }
    }

    private class RetrieveAntibodyList extends ZfinAsyncCallback<List<MarkerDTO>> {

        public RetrieveAntibodyList() {
            super("Error retrieving Antibody list", view.errorElement);
        }

        @Override
        public void onSuccess(List<MarkerDTO> genes) {
//                Window.alert("brought back: " + genes.size() );
            ListBox antibodyList = view.getAntibodyList();
            String selectedAntibodyID = antibodyList.getValue(antibodyList.getSelectedIndex());
            //Window.alert("Selected Antibody: " + selectedAntibodyID);
            antibodyList.clear();
            antibodyList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO gene : genes) {
                antibodyList.addItem(gene.getName(), gene.getZdbID());
                // make sure the selected antibody is still selected
                if (gene.getZdbID().equals(selectedAntibodyID))
                    antibodyList.setItemSelected(rowIndex, true);
                rowIndex++;
            }
        }

    }


    /**
     * Callback class to populate the gene selection box in the construction zone.
     * This class is called when:
     * 1) initializing the construction zone values
     * 2) copying an existing experiment into the construction zone and
     * selecting the gene of the selected experiment
     */
    private class GeneSelectionListAsyncCallback extends ZfinAsyncCallback<List<MarkerDTO>> {

        private MarkerDTO selectedGene;

        private GeneSelectionListAsyncCallback(MarkerDTO selectedGene) {
            super("Error retrieving gene selection list", view.errorElement);
            this.selectedGene = selectedGene;
        }

        @Override
        public void onSuccess(List<MarkerDTO> genes) {
            //Window.alert("brought back: " + experiments.size());
            ListBoxWrapper geneList = view.getGeneList();
            geneList.clear();
            geneList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO gene : genes) {
                geneList.addItem(gene.getName(), gene.getZdbID());
                if (selectedGene != null && selectedGene.getZdbID() != null && gene.getZdbID().equals(selectedGene.getZdbID())) {
                    geneList.setSelectedIndex(rowIndex);
                }
                rowIndex++;
            }
        }
    }


    private class RetrieveSectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public RetrieveSectionVisibilityCallback(String message) {
            super(message, view.errorElement, view.loadingImage);
        }

        @Override
        public void onSuccess(Boolean visible) {
            super.onSuccess(visible);
            setInitialValues();
            view.getShowHideToggle().setVisibility(visible);
        }

        private void setInitialValues() {
            retrieveConstructionZoneValues();
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
            deleteExperiment(experiment);
        }

    }

    private class UpdateExperimentAsyncCallback extends ZfinAsyncCallback<ExperimentDTO> {

        private UpdateExperimentAsyncCallback() {
            super("Error while updating experiment", view.errorElement);
        }

        @Override
        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
            view.updateButton.setEnabled(true);
            updateButtonInProgress = false;
        }

        @Override
        public void onSuccess(ExperimentDTO updatedExperiment) {
            super.onSuccess(updatedExperiment);
            /////fireEventSuccess();
            // update inline without reading all experiments again
            retrieveExperiments();
            view.updateButton.setEnabled(true);
            updateButtonInProgress = false;
            // update expression section with new experiment attributes
//            expressionSection.retrieveExpressions();
        }

    }

    /**
     * Show or hide expression section
     */
    private class ShowSelectedExperimentsClickHandler implements ClickHandler {

        /**
         * This onclick handler is called after the intrinsic handler of the ToggleHyperlink
         * has set the text already.
         *
         * @param event click event
         */
        public void onClick(ClickEvent event) {
            showSelectedExperimentsOnly = !view.showSelectedAllLink.getToggleStatus();
            populateDataTable();
        }

    }

    public boolean isDebug() {
        return debug;
    }

}
