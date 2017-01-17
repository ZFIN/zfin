package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.curation.event.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.StringListBox;
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
    private ExpressionExperimentDTO lastSelectedExperiment;
    private Set<ExpressionExperimentDTO> selectedExperiments = new HashSet<>(5);
    // avoid double updates
    private boolean updateButtonInProgress;
    private boolean showSelectedExperimentsOnly;
    private Map<String, MarkerDTO> geneMap = new HashMap<>();
    private Map<String, FishDTO> fishMap = new HashMap<>();
    // filter set by the banana bar
    private ExpressionExperimentDTO experimentFilter = new ExpressionExperimentDTO();

    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();

    private List<ExpressionExperimentDTO> experimentList = new ArrayList<>(15);

    public ExpressionExperimentZonePresenter(ExpressionExperimentZoneView view, String publicationID, boolean debug) {
        this.view = view;
        this.publicationID = publicationID;
        this.debug = debug;
        experimentFilter = new ExpressionExperimentDTO();
        experimentFilter.setPublicationID(publicationID);
        view.setPresenter(this);
    }

    public void updateExperimentOnCurationFilter(ExpressionExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
        retrieveExperiments();
    }

    /**
     * When an expression record is added update the experiment section about it, ie the number
     * of expression records is incremented by 1.
     */
    public void notifyAddedExpression(Map<ExpressionExperimentDTO, Integer> expressionExperimentDTOMap) {
        for (ExpressionExperimentDTO experiment : experimentList) {
            for (ExpressionExperimentDTO sourceExperiment : selectedExperiments) {
                if (experiment.equals(sourceExperiment))
                    if (expressionExperimentDTOMap.get(experiment) != null)
                        experiment.setNumberOfExpressions(experiment.getNumberOfExpressions() + expressionExperimentDTOMap.get(experiment));
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
    public void notifyRemovedExpression(ExpressionExperimentDTO sourceExperiment) {
        for (ExpressionExperimentDTO experiment : experimentList) {
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
        final ExpressionExperimentDTO zoneExperiment = getExperimentFromConstructionZone(true);
        if (!isValidExperiment(zoneExperiment)) {
            view.cleanupOnExit();
            addButtonInProgress = false;
            return;
        }
        if (isEfgWildtypeCombo(zoneExperiment)) {
            //Window.alert("experiment exists: ");
            view.setError("Cannot create an experiment with an EFG and a wildtype fish!");
            view.cleanupOnExit();
            addButtonInProgress = false;
            return;
        }
        if (experimentExists(zoneExperiment, true)) {
            //Window.alert("experiment exists: ");
            view.setError("Experiment already exists. Experiments have to be unique!");
            view.cleanupOnExit();
            addButtonInProgress = false;
            return;
        }

        curationExperimentRPCAsync.createExpressionExperiment(zoneExperiment, new AddExperimentCallback());

    }

    private boolean isEfgWildtypeCombo(ExpressionExperimentDTO experimentDTO) {
        if (experimentDTO.getGene() != null) {
            if (experimentDTO.getFishDTO().isWildtype() && experimentDTO.getGene().getMarkerType().equals("Engineered Foreign Gene"))
                return true;
        }
        return false;
    }


    protected void populateDataTable() {
        int elementIndex = 0;
        for (ExpressionExperimentDTO experiment : experimentList) {
            if (showSelectedExperimentsOnly && !selectedExperiments.contains(experiment))
                continue;
            MarkerDTO gene = experiment.getGene();
            view.addGene(gene, elementIndex);
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
        view.endTableUpdate();
    }

    protected void onAssayChange() {
        String itemText = view.getAssayList().getItemText(view.getAssayList().getSelectedIndex());
        //Window.alert(itemText);
        if (ExpressionAssayDTO.isAntibodyAssay(itemText)) {
            view.getAntibodyList().setEnabled(true);
            String geneID = view.getGeneList().getValue(view.getGeneList().getSelectedIndex());
            curationExperimentRPCAsync.readAntibodiesByGene(publicationID, geneID, new RetrieveAntibodyList());
        } else {
            view.getAntibodyList().setEnabled(false);

        }
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
    public boolean experimentExists(ExpressionExperimentDTO updatedExperiment, boolean isNewExperiment) {
        int rowIndex = 1;
        for (ExpressionExperimentDTO experiment : experimentList) {
            if (experiment.equals(updatedExperiment)) {
                if (isNewExperiment || (!experiment.getExperimentZdbID().equals(updatedExperiment.getExperimentZdbID()))) {
                    if (view.showHideToggle.isVisible()) {
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
    private boolean isValidExperiment(ExpressionExperimentDTO experiment) {
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

    private ExpressionExperimentDTO getExperimentFromConstructionZone(boolean newExperiment) {
        ExpressionExperimentDTO updatedExperiment = new ExpressionExperimentDTO();
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
                updatedExperiment.setGenbankID(view.getGenbankList().getSelected());
                updatedExperiment.setGenbankNumber(view.getGenbankList().getSelectedText());
            }
        }
        ExperimentDTO env = new ExperimentDTO();
        String environmentID = view.getEnvironmentList().getSelected();
        String environmentName = view.getEnvironmentList().getSelectedText();
        env.setZdbID(environmentID);
        env.setName(environmentName);
        updatedExperiment.setEnvironment(env);
        // only use the antibody if the selection box is enabled.
        if (view.getAntibodyList().isEnabled()) {
            String antibodyID = view.getAntibodyList().getSelected();
            String antibodyName = view.getAntibodyList().getSelectedText();
            if (StringUtils.isNotEmpty(antibodyID) && !antibodyID.equals(StringUtils.NULL)) {
                MarkerDTO antibody = new MarkerDTO();
                antibody.setZdbID(antibodyID);
                antibody.setName(antibodyName);
                updatedExperiment.setAntibodyMarker(antibody);
            }
        }
        String fishID = view.getFishList().getSelected();
        updatedExperiment.setFishID(fishID);
        String fishName = view.getFishList().getSelectedText();
        updatedExperiment.setFishName(fishName);
        updatedExperiment.setFishDTO(fishMap.get(fishID));
        String geneID = view.getGeneList().getSelected();
        if (StringUtils.isNotEmpty(geneID) && !geneID.equals(StringUtils.NULL)) {
            updatedExperiment.setGene(geneMap.get(geneID));
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
        ExpressionExperimentDTO updatedExperiment = getExperimentFromConstructionZone(false);
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
        view.dataTable.addClickHandler(new ExperimentSelectClickHandler(null));
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
        retrieveFishList();

        // environment list
        updateEnvironmentList();
        String message;

        // assay list
        message = "Error while reading the assay list";
        curationExperimentRPCAsync.getAssays(new RetrieveAssayListCallback(message));

        // antibody list
        updateAntibodyList();
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                view.getLoadingImage().setVisible(false);
            }
        });
    }

    public void updateEnvironmentList() {
        String message = "Error while reading the environment";
        curationExperimentRPCAsync.getEnvironments(publicationID,
                new RetrieveEnvironmentListCallBack(view.getEnvironmentList(), message, view.errorElement));
    }

    public void updateAntibodyList() {
        curationExperimentRPCAsync.getAntibodies(publicationID, new AntibodySelectionListAsyncCallback(null));
    }

    public void retrieveFishList() {
        curationExperimentRPCAsync.getFishList(publicationID, new FishSelectionListAsyncCallback());
    }

    // Retrieve experiments from the server

    protected void retrieveExperiments() {
        curationExperimentRPCAsync.getExperimentsByFilter(experimentFilter, new RetrieveExperimentsCallback());
    }

    public void setExperimentFilter(ExpressionExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
    }

    public void clearErrorMessages() {
        view.clearError();
    }

    /**
     * Un-select all experiment check boxes.
     */
    public void unselectExperiment(ExpressionExperimentDTO experiment) {
        selectedExperiments.remove(experiment);
        populateDataTable();
    }

    /**
     * Set a single experiment (check mark).
     * All other experiments that may be checked are un-checked.
     *
     * @param experiment Experiment DTO
     */
    public void setSingleExperiment(ExpressionExperimentDTO experiment) {
        selectedExperiments.add(experiment);
        populateDataTable();
        showSelectedExperimentsOnly = false;
    }


    public void selectAntibody(ExpressionExperimentDTO selectedExperiment) {
        curationExperimentRPCAsync.getAntibodies(publicationID,
                new AntibodySelectionListAsyncCallback(selectedExperiment.getAntibodyMarker().getZdbID()));
    }

    public void setGene(ExpressionExperimentDTO selectedExperiment) {
        curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(selectedExperiment.getGene()));
    }

    public void readGenbankAccessions(ExpressionExperimentDTO selectedExperiment) {
        String geneID = selectedExperiment.getGene().getZdbID();
        String genBankID = selectedExperiment.getGenbankID();
        curationExperimentRPCAsync.readGenbankAccessions(publicationID, geneID, new GenbankSelectionListAsyncCallback(genBankID));
    }

    public void deleteExperiment(final ExpressionExperimentDTO experiment) {
        curationExperimentRPCAsync.deleteExperiment(experiment.getExperimentZdbID(), new DeleteExperimentCallback(experiment));
    }


    public void updateGenes() {
        if (lastSelectedExperiment != null)
            curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(lastSelectedExperiment.getGene()));
        else
            curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(null));

    }

    public void onShowHideClick(boolean visibility) {
        String errorMessage = "Error while trying to save experiment visibility";
        curationExperimentRPCAsync.setExperimentVisibilitySession(publicationID, visibility,
                new VoidAsyncCallback(errorMessage, view.errorElement, null));
        retrieveExperiments();
    }

    private void showHideClearAllLink() {
        if (selectedExperiments.size() > 0) {
            view.showToggleLinks(true);
        } else {
            view.showToggleLinks(false);
        }
    }

    public Set<ExpressionExperimentDTO> getSelectedExperiments() {
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
            StringListBox assayList = view.getAssayList();
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

        private ExpressionExperimentDTO selectedExperiment;


        public ExperimentSelectClickHandler(ExpressionExperimentDTO selectedExperiment) {
            this.selectedExperiment = selectedExperiment;
        }

        public void onClick(ClickEvent event) {
            event.stopPropagation();

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

            clearErrorMessages();
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

    private class AddExperimentCallback extends ZfinAsyncCallback<ExpressionExperimentDTO> {
        public AddExperimentCallback() {
            super("Error while creating experiment", view.errorElement);
        }

        @Override
        public void onSuccess(ExpressionExperimentDTO newExperiment) {
            super.onSuccess(newExperiment);
            addButtonInProgress = false;
            retrieveExperiments();
            AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.CREATE_EXPRESSION_EXPERIMENT, newExperiment.toString()));
        }

    }


    private class RetrieveExperimentsCallback extends ZfinAsyncCallback<List<ExpressionExperimentDTO>> {

        public RetrieveExperimentsCallback() {
            super("Error while reading Experiment Filters", view.errorElement, view.loadingImage);
        }

        @Override
        public void onSuccess(List<ExpressionExperimentDTO> list) {
            super.onSuccess(list);
            experimentList.clear();
            for (ExpressionExperimentDTO experiment : list) {
                if (experiment.getEnvironment().getName().startsWith("_"))
                    experiment.getEnvironment().setName(experiment.getEnvironment().getName().substring(1));
                experimentList.add(experiment);
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
            StringListBox geneList = view.getGeneList();
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

        private ExpressionExperimentDTO experiment;

        DeleteExperimentCallback(ExpressionExperimentDTO experiment) {
            super("Error while deleting Experiment", view.errorElement);
            this.experiment = experiment;
        }

        @Override
        public void onSuccess(Void exp) {
            experimentList.remove(experiment);
            populateDataTable();
            // also remove the figure annotations that were used with this experiments
            RemoveExpressionExperimentEvent event = new RemoveExpressionExperimentEvent(experiment);
            AppUtils.EVENT_BUS.fireEvent(event);
        }

    }

    private class GenbankSelectionListAsyncCallback extends ZfinAsyncCallback<List<ExpressionExperimentDTO>> {

        private String selectedGenBankID;

        public GenbankSelectionListAsyncCallback(String genBankID) {
            super("Error retrieving GenBank list", view.errorElement);
            this.selectedGenBankID = genBankID;
        }

        @Override
        public void onSuccess(List<ExpressionExperimentDTO> accessions) {
            StringListBox genbankList = view.getGenbankList();
            genbankList.clear();
            genbankList.addItem("");
            int rowIndex = 1;
            if (isDebug())
                GWT.log("Selected GeneBank ID: " + selectedGenBankID);
            for (ExpressionExperimentDTO accession : accessions) {
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
            StringListBox antibodyList = view.getAntibodyList();
            StringListBox assayList = view.getAssayList();
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
        public void onSuccess(List<MarkerDTO> antibodies) {
//                Window.alert("brought back: " + antibodies.size() );
            StringListBox antibodyList = view.getAntibodyList();
            String selectedAntibodyID = antibodyList.getValue(antibodyList.getSelectedIndex());
            //Window.alert("Selected Antibody: " + selectedAntibodyID);
            antibodyList.clear();
            antibodyList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO antibody : antibodies) {
                antibodyList.addItem(antibody.getName(), antibody.getZdbID());
                // make sure the selected antibody is still selected
                if (antibody.getZdbID().equals(selectedAntibodyID))
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
    private class GeneSelectionListAsyncCallback extends ZfinAsyncCallback<List<FilterSelectionBoxEntry>> {

        private MarkerDTO selectedGene;

        private GeneSelectionListAsyncCallback(MarkerDTO selectedGene) {
            super("Error retrieving gene selection list", view.errorElement);
            this.selectedGene = selectedGene;
        }

        @Override
        public void onSuccess(List<FilterSelectionBoxEntry> genes) {
            //Window.alert("brought back genes: " + genes.size());
            StringListBox geneList = view.getGeneList();
            geneList.clear();
            geneMap.clear();
            geneList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO gene : (List<MarkerDTO>) (List<?>) genes) {
                geneList.addItem(gene.getName(), gene.getZdbID());
                if (selectedGene != null && selectedGene.getZdbID() != null && gene.getZdbID().equals(selectedGene.getZdbID())) {
                    geneList.setSelectedIndex(rowIndex);
                }
                geneMap.put(gene.getZdbID(), gene);
                rowIndex++;
            }
        }
    }

    private class FishSelectionListAsyncCallback extends ZfinAsyncCallback<List<FilterSelectionBoxEntry>> {

        private FishSelectionListAsyncCallback() {
            super("Error retrieving fish selection list", view.errorElement);
        }

        @Override
        public void onSuccess(List<FilterSelectionBoxEntry> fishDTOList) {
            fishMap.clear();
            int index = 0;
            StringListBox listBox = view.getFishList();
            listBox.clear();
            for (FishDTO fish : (List<FishDTO>) (List<?>) fishDTOList) {
                if (fish.getName().startsWith("---")) {
                    listBox.addItem(fish.getHandle(), fish.getZdbID());
                    listBox.getElement().getElementsByTagName("option").getItem(index).setAttribute("disabled", "disabled");
                } else
                    listBox.addItem(fish.getHandle(), fish.getZdbID());
                index++;
                fishMap.put(fish.getZdbID(), fish);
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

        private ExpressionExperimentDTO experiment;

        public ExperimentDeleteClickListener(ExpressionExperimentDTO experiment) {
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
            event.stopPropagation();
        }

    }

    private class UpdateExperimentAsyncCallback extends ZfinAsyncCallback<ExpressionExperimentDTO> {

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
        public void onSuccess(ExpressionExperimentDTO updatedExperiment) {
            super.onSuccess(updatedExperiment);
            // update inline without reading all experiments again
            retrieveExperiments();
            //view.updateButton.setEnabled(true);
            updateButtonInProgress = false;
            // update expression section with new experiment attributes
            UpdateExpressionExperimentEvent event = new UpdateExpressionExperimentEvent(updatedExperiment);
            AppUtils.EVENT_BUS.fireEvent(event);
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
