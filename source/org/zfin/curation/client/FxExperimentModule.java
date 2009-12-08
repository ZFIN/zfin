package org.zfin.curation.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import org.zfin.framework.presentation.dto.*;
import org.zfin.framework.presentation.gwtutils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Experiment section of the FX curation page.
 * It consists of three parts:
 * 1) Show and hide link
 * 2) Display experiments
 * 3) Construction zone to create experiments or update existing experiments
 * <p/>
 * Ad 1)
 * A) The experiment section can be hidden by clicking on 'hide' which then hides the display
 * part, i.e. the construction zone will still be displayed and the link will change to 'show'.
 * If a new experiment is added a note is displayed about the success which otherwise would be invisible.
 * B) The state of the visiblity is saved in the database and remembered for this publication for future.
 * <p/>
 * Ad 2)
 * A) Displayed are all experiments (unless the experiment filter, aka banana bar when it was yellow instead of
 * green), ordered by gene (non-gene experiments first) and fish.
 * B) Delete-Button: The Delete-button removes the experiment and all associated records, i.e.
 * expression_results, expression_pattern_figure, etc.
 * The number on the Delete-button indicates the number of expression_results records that are annotated
 * with this experiment. Clicking the button pops up a Java Script confirmation box that indicates the
 * action's consequences.
 * The ZDB ID of the expression_experiment is displayed on mouse over.
 * C) When you mouse over an experiment its background color changes to a light green-blue that changes back to its
 * original background color upon mouse out
 * D) Striping backgrounds are done by genes, i.e. all experiments for the same gene have the same background.
 * the next experiment with a different gene shows the alternate background color.
 * E) Clicking anywhere on an experiment row sets the selection radio bar and copies the values into the
 * construction zone. It also selects the experiment in the experiment box in the expression section.
 * It keeps the light green-blue background color until another experiment is selected or the page is refreshed.
 * F) GenBank Number: If an experiment has a GenBank number associated with a clone (EST or cDNA) then hovering
 * the mouse over the value will display the clone name and the marker type.
 * <p/>
 * Ad 3)
 * A) The Add-button is always enabled. Clicking it will try to create a new experiment. If the experiment is not
 * unique (compound PK: gene,fish,environment,assay, antibody and GenBank) an error message is displayed below
 * the display table and the row of the experiment that it equals to changes the background color to pruple.
 * B) Gene-Selection-Box: By default the gene selection box does not select a gene. Genes that are attributed to
 * this publication are listed.
 * I) Antibody-Selection-Box: Selecting a specific gene updates the list of antibodies if an antibody assay
 * is selected. Only antibodies that are attributed to the publication and antibodies that have the gene
 * as a related marker are listed.
 * II)GenBank-Selection-Box: Selecting a gene updates the GenBank-Selection-Box: GenBank accession numbers that
 * belong the given gene or clones that are related to the gene (EST or cDNA) are listed.
 * C) Fish-Selection-Box: It list first WT then the list of non-wildtype genotypes attributes to this publication
 * and then all other wild-type genotypes. No cross-interaction upon selection.
 * D) Environment-Selection-Box: This lists first: Standard, Generic-control and the all environments defined in
 * the environment tab. No cross-interaction upon selection.
 * E) Assay-Selection-Box: This lists all assays defined in the expression_pattern_assay according to the
 * display order defined therein.
 * I) Antibody-Selection-Box: If an antibody assay is selected (currently: IHC, WB or OTHER) the
 * Antibody-Selection-Box is enabled. If a different assay is selected then this selection box is disabled.
 * F) Antibody-Selection-Box: This box is only enabled if an antibody assay is selected.
 * G) Update-Button: The update buttton is disabled by default and will only be enabled when an existing experiment
 * is selected (and copied into the construction zone).
 * H) Add an experiment:
 * I)   Adding an experiment requires to either selecting a gene or an antibody or both and all other
 * attributes except the GenBank accession number which is optional.
 * II) Experiments have to be unqiue according to the combination
 * Gene/Fish/Environment/Assay/Antibody/GenBank, i.e. you cannot create two experiments with the same
 * values for these attributes. An error message is displayed below the construction zone if a new
 * experiment equals an existing one while hightlighting the existing experiment in the list purple that
 * that the new experiment is conflicting with.
 * III) Updating an existing experiment is validated against the uniqueness constraint of II). An error
 * message is displayed when the update matches another existing experiment. Before an experiment is
 * updated a JavaScript alert box pops up to ask for confirmation.
 */
public class FxExperimentModule extends Composite implements ExperimentSection{

    // div-elements
    public static final String SHOW_HIDE_EXPERIMENTS = "show-hide-experiments";
    public static final String EXPERIMENTS_DISPLAY = "display-experiment";
    public static final String IMAGE_LOADING = "image-loading";
    public static final String EXPERIMENTS_DISPLAY_ERRORS = "display-experiment-errors";
    public static final String HIDE = "hide";
    public static final String SHOW = "show";

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showExperimentSection = new Hyperlink();
    private ExperimentFlexTable displayTable;
    private Image loadingImage = new Image();
    private Label errorMessage = new Label();


    // construction zone
    private Button addButton = new Button("Add");
    private ListBox geneList = new ListBox();
    private ListBox fishList = new ListBox();
    private ListBox environmentList = new ListBox();
    private ListBox assayList = new ListBox();
    private ListBox antibodyList = new ListBox();
    private ListBox genbankList = new ListBox();
    private Button updateButton = new Button("update");

    private ExperimentDTO lastSelectedExperiment;
    private Set<ExperimentDTO> selectedExperiments = new HashSet<ExperimentDTO>();
    private List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>();
    private boolean showSelectedExperimentsOnly;

    // attributes for duplicate row
    private String duplicateRowOriginalStyle;
    private int duplicateRowIndex;

    // flag that indicates if the experiment section is visible or not.
    private boolean sectionVisible;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();

    // injected variables
    private String publicationID;
    private ExpressionSection expressionSection;
    // filter set by the banana bar
    private ExperimentDTO experimentFilter;


    public FxExperimentModule(String publicationID) {
        this.publicationID= publicationID;
        displayTable = new ExperimentFlexTable(HeaderName.getHeaderNames());
        initGUI();
    }

    /**
     * This loads the module. Run this method only once at startup.
     * If you need to reload call the method
     * experimentTable.retrieveExperiments()
     */
    public void runModule() {
        loadSectionVisibility();
    }

    private void initGUI() {
        initShowHideGUI();

        RootPanel.get(EXPERIMENTS_DISPLAY).add(displayTable);
        // add click listener to update the record
        updateButton.addClickHandler(new UpdateExperimentClickListener());
        addChangeHandler();

        RootPanel.get(EXPERIMENTS_DISPLAY_ERRORS).add(errorMessage);
        RootPanel.get(IMAGE_LOADING).add(loadingImage);
        errorMessage.setStyleName("error");
        loadingImage.setUrl("/images/ajax-loader.gif");
    }

    private void initShowHideGUI() {
        RootPanel.get(SHOW_HIDE_EXPERIMENTS).add(panel);
        Label experimentLabel = new Label("Experiment: ");
        experimentLabel.setStyleName("bold");
        panel.add(experimentLabel);
        showExperimentSection.setStyleName("small");
        showExperimentSection.setText(SHOW);
        showExperimentSection.setTargetHistoryToken(SHOW);
        showExperimentSection.addClickHandler(new ShowExperimentSectionListener());
        panel.add(showExperimentSection);
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationExperimentRPCAsync.readExperimentSectionVisibility(publicationID,
                new RetrieveSectionVisibilityCallback(message));
    }

    private void setInitialValues() {
        retrieveExperiments();
        retrieveConstructionZoneValues();
    }

    private void retrieveConstructionZoneValues() {
        // gene list
        curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(null));

        // fish (genotype) list
        String message = "Error while reading Genotypes";
        curationExperimentRPCAsync.getGenotypes(publicationID,
                new RetrieveGenotypeListCallback(message));

        // environment list
        message = "Error while reading the environment";
        curationExperimentRPCAsync.getEnvironments(publicationID,
                new RetrieveEnvironmentListCallback(message));

        // assay list
        message = "Error while reading the assay list";
        curationExperimentRPCAsync.getAssays(new RetrieveAssayListCallback(message));

        // antibody list
        curationExperimentRPCAsync.getAntibodies(publicationID, new AntibodySelectionListAsyncCallback(null));
    }

    // Retrieve experiments from the server
    protected void retrieveExperiments() {
        loadingImage.setVisible(true);
        curationExperimentRPCAsync.getExperimentsByFilter(experimentFilter, new RetrieveExperimentsCallback(experiments));
    }

    private void addChangeHandler() {
        // assay changes
        assayList.addChangeHandler(new AssayListChangeListener());

        // gene changes
        geneList.addChangeHandler(new GeneListChangeListener());

        // antibody changes
        antibodyList.addChangeHandler(new AntibodyListChangeListener());
        addChangeListenersToConstructionZoneElements();
    }

    private void addChangeListenersToConstructionZoneElements() {
        assayList.addChangeHandler(errorMessageCleanupListener);
        geneList.addChangeHandler(errorMessageCleanupListener);
        antibodyList.addChangeHandler(errorMessageCleanupListener);
        fishList.addChangeHandler(errorMessageCleanupListener);
        environmentList.addChangeHandler(errorMessageCleanupListener);
        genbankList.addChangeHandler(errorMessageCleanupListener);
    }


    //ToDo: implements debug feature
    private boolean isDebug() {
        return false;
    }

    /**
     * When an expression record is removed update the experiment about it, ie the number
     * of expression records isdecrements by 1.
     *
     * @param sourceExperiment experiment being removed
     */
    public void notifyRemovedExpression(ExperimentDTO sourceExperiment) {
        for (ExperimentDTO experiment : experiments) {
            if (experiment.getExperimentZdbID().equals(sourceExperiment.getExperimentZdbID()))
                experiment.setNumberOfExpressions(experiment.getNumberOfExpressions() - 1);
        }
        unselectAllExperiments();
        if (sectionVisible)
            displayTable.createExperimentTable();
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
        if (sectionVisible)
            displayTable.createExperimentTable();
    }

    /**
     * Unselect all experiment check boxes.
     */
    public void unselectAllExperiments() {
        selectedExperiments.clear();
        showSelectedExperimentsOnly = false;
        displayTable.uncheckAllRecords();
    }

    public void showExperiments(boolean showSelectedExperimentsOnly) {
        this.showSelectedExperimentsOnly = showSelectedExperimentsOnly;
        displayTable.createExperimentTable();
    }

    /**
     * Set a single experiment (check mark).
     * All other experiments that may be checked are un-checked.
     *
     * @param experiment Experiment DTO
     */
    public void setSingleExperiment(ExperimentDTO experiment) {
        selectedExperiments.clear();
        selectedExperiments.add(experiment);
        showSelectedExperimentsOnly = false;
        displayTable.createExperimentTable();
    }

    /**
     * Retrieve all selected Experiments.
     *
     * @return set of selected experiments
     */
    public Set<ExperimentDTO> getSelectedExperiment() {
        return selectedExperiments;
    }

    /**
     * Apply the provided filter elements, re-read the experiments and show the
     * new list of experiments according to the filters.
     *
     * @param experimentFilter Experiment filter
     */
    public void applyFilterElements(ExperimentDTO experimentFilter) {
        setExperimentFilter(experimentFilter);
        unselectAllExperiments();
        retrieveExperiments();
    }


// *************** Handlers, Callbacks, etc.

    /**
     * This Click Handler is activated upon clicking the selection check box in the
     * Experiment display section. It should do two things:
     * 1) copy the values for the experiment into the construction zone
     * 2) select the experiment in the textarea of the expression section
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
                if (environment.equals(selectedExperiment.getEnvironment())) {
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
            String geneID = selectedExperiment.getGeneZdbID();
            String genBankID = selectedExperiment.getGenbankID();
            curationExperimentRPCAsync.readGenbankAccessions(publicationID, geneID, new GenbankSelectionListAsyncCallback(genBankID));
        }

        // create gene list and select the gene of the experiment
        private void selectGene() {
            // first retrieve the full list of genes and then
            // select the gene in question.
            curationExperimentRPCAsync.getGenes(publicationID, new GeneSelectionListAsyncCallback(selectedExperiment.getGeneZdbID()));
        }

        // create antibody list and select the antibody of the experiment
        private void selectAntibody() {
            // first retrieve the full list of genes and then
            // select the gene in question.
            // only get antibody list if assay is compatible
            if (ExpressionAssayDTO.isAntibodyAssay(selectedExperiment.getAssay()))
                curationExperimentRPCAsync.getAntibodies(publicationID, new AntibodySelectionListAsyncCallback(selectedExperiment.getAntibodyID()));
            else
                antibodyList.setEnabled(false);
        }

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
        String environmentID = environmentList.getValue(environmentList.getSelectedIndex());
        updatedExperiment.setEnvironmentID(environmentID);
        String environment = environmentList.getItemText(environmentList.getSelectedIndex());
        updatedExperiment.setEnvironment(environment);
        // only use the antibody if the selection box is enabled.
        if (antibodyList.isEnabled()) {
            String antibodyID = antibodyList.getValue(antibodyList.getSelectedIndex());
            String antibodyName = antibodyList.getItemText(antibodyList.getSelectedIndex());
            if (StringUtils.isEmpty(antibodyID) || antibodyID.equals(StringUtils.NULL)) {
                antibodyID = null;
            }
            updatedExperiment.setAntibodyID(antibodyID);
            updatedExperiment.setAntibody(antibodyName);
        }
        String fishID = fishList.getValue(fishList.getSelectedIndex());
        updatedExperiment.setFishID(fishID);
        String fishName = fishList.getItemText(fishList.getSelectedIndex());
        updatedExperiment.setFishName(fishName);
        String geneID = geneList.getValue(geneList.getSelectedIndex());
        if (StringUtils.isEmpty(geneID) || geneID.equals(StringUtils.NULL))
            geneID = null;
        updatedExperiment.setGeneZdbID(geneID);
        String geneName = geneList.getItemText(geneList.getSelectedIndex());
        if (StringUtils.isEmpty(geneName) || geneName.equals(StringUtils.NULL))
            geneName = null;
        updatedExperiment.setGeneName(geneName);
        updatedExperiment.setPublicationID(publicationID);
        return updatedExperiment;
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

    private void deleteExperiment(final ExperimentDTO experiment) {
        curationExperimentRPCAsync.deleteExperiment(experiment.getExperimentZdbID(), new DeleteExperimentCallback(experiment));
    }

    private class RetrieveExperimentsCallback extends ZfinAsyncCallback<List<ExperimentDTO>> {

        List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>();

        public RetrieveExperimentsCallback(List<ExperimentDTO> experiments) {
            super("Error while reading Experiment Filters", errorMessage);
            this.experiments = experiments;
        }

        public void onSuccess(List<ExperimentDTO> list) {

            experiments.clear();
            for (ExperimentDTO id : list) {
                if (id.getEnvironment().startsWith("_"))
                    id.setEnvironment(id.getEnvironment().substring(1));
                experiments.add(id);
            }
            //Window.alert("SIZE: " + experiments.size());
            if (sectionVisible)
                displayTable.createExperimentTable();
            // populate expression section
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class DeleteExperimentCallback extends ZfinAsyncCallback<Void> {

        private ExperimentDTO experiment;

        DeleteExperimentCallback(ExperimentDTO experiment) {
            super("Error while deleting Experiment", errorMessage);
            this.experiment = experiment;
        }

        public void onSuccess(Void exp) {
            //Window.alert("Success");
            experiments.remove(experiment);
            displayTable.createExperimentTable();
            // also remove the figure annotations that were used with this experiments
            expressionSection.removeFigureAnnotations(experiment);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class RetrieveAntibodyList extends ZfinAsyncCallback<List<MarkerDTO>> {

        public RetrieveAntibodyList() {
            super("Error retrievin Antibody list", errorMessage);
        }

        public void onSuccess(List<MarkerDTO> genes) {
//                Window.alert("brought back: " + genes.size() );
            String selectedAntibodyID = antibodyList.getValue(antibodyList.getSelectedIndex());
            //Window.alert("Selected Antibody: " + selectedAntibodyID);
            antibodyList.clear();
            antibodyList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO gene : genes) {
                antibodyList.addItem(gene.getAbbreviation(), gene.getZdbID());
                // make sure the selected antibody is still selected
                if (gene.getZdbID().equals(selectedAntibodyID))
                    antibodyList.setItemSelected(rowIndex, true);
                rowIndex++;
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * Callback class to populate the gene selection box in the construction zone.
     * This class is called when:
     * 1) initializing the construction zone values
     * 2) copying an existing experiment into the construction zone and
     * selecting the gene of the selected epxeriment
     */
    private class GeneSelectionListAsyncCallback extends ZfinAsyncCallback<List<MarkerDTO>> {

        private String selectedGeneID;

        private GeneSelectionListAsyncCallback(String selectedGeneID) {
            super("Error retrievin gene selection list", errorMessage);
            this.selectedGeneID = selectedGeneID;
        }

        public void onSuccess(List<MarkerDTO> genes) {
            //Window.alert("brought back: " + experiments.size());
            geneList.clear();
            geneList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO gene : genes) {
                geneList.addItem(gene.getAbbreviation(), gene.getZdbID());
                if (selectedGeneID != null && gene.getZdbID().equals(selectedGeneID))
                    geneList.setSelectedIndex(rowIndex);
                rowIndex++;
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * Callback class to populate the antibody  selection box in the construction zone.
     * This class is called when:
     * 1) initializing the construction zone values
     * 2) copying an existing experiment into the construction zone and
     * selecting the antibody of the selected epxeriment
     */
    private class AntibodySelectionListAsyncCallback extends ZfinAsyncCallback<List<MarkerDTO>> {

        private String selectedAntibodyID;

        private AntibodySelectionListAsyncCallback(String selectedAntibodyID) {
            super("Error readin antibody list", errorMessage);
            this.selectedAntibodyID = selectedAntibodyID;
        }

        public void onSuccess(List<MarkerDTO> antibodies) {
            //Window.alert("brought back: " + experiments.size() );
            antibodyList.clear();
            antibodyList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO antibody : antibodies) {
                antibodyList.addItem(antibody.getAbbreviation(), antibody.getZdbID());
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

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class GenbankSelectionListAsyncCallback extends ZfinAsyncCallback<List<ExperimentDTO>> {

        private String selectedGenBankID;

        public GenbankSelectionListAsyncCallback(String genBankID) {
            super("Error retrieving GenBank list", errorMessage);
            this.selectedGenBankID = genBankID;
        }

        public void onSuccess(List<ExperimentDTO> accessions) {
            genbankList.clear();
            genbankList.addItem("");
            int rowIndex = 1;
            if (isDebug())
                Window.alert("Selected GeneBank ID: " + selectedGenBankID);
            for (ExperimentDTO accession : accessions) {
                genbankList.addItem(accession.getGenbankNumber(), accession.getGenbankID());
                if (selectedGenBankID != null && accession.getGenbankID().equals(selectedGenBankID))
                    genbankList.setSelectedIndex(rowIndex);
                rowIndex++;
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class ShowExperimentSectionListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            String errorMessage = "Error while trying to save experiment visibility";
            if (sectionVisible) {
                // hide experiments
                displayTable.clearTable();
                //createConstructionZone();
                showExperimentSection.setText(SHOW);
                sectionVisible = false;
                curationExperimentRPCAsync.setExperimentVisibilitySession(publicationID, false,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            } else {
                // display experiments
                // check if we need to re-read the construction zone values again from the server
                // Assumes that the construction zone is always displayed: in hidden or display state.
                retrieveExperiments();
                if (displayTable.getRowCount() == 0)
                    retrieveConstructionZoneValues();
                showExperimentSection.setText(HIDE);
                sectionVisible = true;
                curationExperimentRPCAsync.setExperimentVisibilitySession(publicationID, true,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            }
            clearErrorMessages();
        }

    }

    // avoid double updates
    private boolean updateButtonInProgress;
    private boolean addButtonInProgress;

    private class AddExperimentClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            // do not proceed if it just has been clicked once
            // and is being worked on
            if (addButtonInProgress)
                return;
            addButtonInProgress = true;
            final ExperimentDTO zoneExperiment = getExperimentFromConstructionZone(true);
            if (!validateExperiment(zoneExperiment)) {
                cleanupOnExit();
                return;
            }
            if (experimentExists(zoneExperiment, true)) {
                //Window.alert("experiment exists: ");
                errorMessage.setText("Experiment already exists. Experiments have to be unique!");
                cleanupOnExit();
                return;
            }

            loadingImage.setVisible(true);
            curationExperimentRPCAsync.createExpressionExperiment(zoneExperiment, new AddExperimentCallback());
        }

        private void cleanupOnExit() {
            addButtonInProgress = false;
            loadingImage.setVisible(false);
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
     *         false if experiment is different from all other experiments
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
                    if (sectionVisible) {
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
    private boolean validateExperiment(ExperimentDTO experiment) {
        boolean isValid = true;
        if (StringUtils.isEmpty(experiment.getAntibodyID()) &&
                StringUtils.isEmpty(experiment.getGeneZdbID())) {
            errorMessage.setText("You need to select at least a gene or an antibody");
            return false;
        }
        if (StringUtils.isEmpty(experiment.getFishID()) || experiment.getFishID().equals("null")) {
            errorMessage.setText("You need to select a fish (genotype).");
            return false;
        }
        if (StringUtils.isEmpty(experiment.getEnvironment())) {
            errorMessage.setText("You need to select an environment (experiment).");
            return false;
        }
        if (StringUtils.isEmpty(experiment.getAssay())) {
            errorMessage.setText("You need to select an assay.");
            return false;
        }
        return isValid;
    }

    /**
     * Remove error messages
     * unmark duplicate experiments
     */
    public void clearErrorMessages() {
        errorMessage.setText(null);
        if (duplicateRowIndex > 0)
            displayTable.getRowFormatter().setStyleName(duplicateRowIndex, duplicateRowOriginalStyle);
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
                experimentDTOs = new ArrayList<ExperimentDTO>();
                experimentDTOs.addAll(selectedExperiments);
            } else {
                experimentDTOs = experiments;
            }
            //Window.alert("createExperimentTable.selectedExperiments: "+selectedExperiments.size());
            for (ExperimentDTO experiment : experimentDTOs) {
                // rowindex minus the header row
                CheckBox checkBox = new CheckBox("");
                checkBox.setTitle(experiment.getExperimentZdbID());
                checkBox.addClickHandler(new ExperimentSelectClickHandler(experiment, checkBox));

                if (selectedExperiments.contains(experiment)) {
                    //Window.alert("contains: ");
                    checkBox.setValue(true);
                }

                //Window.alert("Experiment: " + experiment.getGeneName());
                setWidget(rowIndex, HeaderName.SELECT.getIndex(), checkBox);
                setText(rowIndex, HeaderName.GENE.getIndex(), experiment.getGeneName());
                setText(rowIndex, HeaderName.FISH.getIndex(), experiment.getFishName());
                setText(rowIndex, HeaderName.ENVIRONMENT.getIndex(), experiment.getEnvironmentDisplayValue());
                setText(rowIndex, HeaderName.ASSAY.getIndex(), experiment.getAssay());
                setText(rowIndex, HeaderName.ANTIBODY.getIndex(), experiment.getAntibody());
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
                if (previousExperiment != null)
                    previousID = previousExperiment.getGeneZdbID();
                groupIndex = setRowStyle(rowIndex, experiment.getGeneZdbID(), previousID, groupIndex);
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

        protected void createTableHeader() {
            super.createTableHeader();
            for (HeaderName name : headerNames) {
                if (name.index != 0)
                    setText(selectionCheckBoxColumn, name.index, name.getName());
            }
        }

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

    private ErrorMessageCleanupListener errorMessageCleanupListener = new ErrorMessageCleanupListener();

    private class AddExperimentCallback extends ZfinAsyncCallback<ExperimentDTO> {
        public AddExperimentCallback() {
            super("Error while creating experiment", errorMessage);
        }

        public void onSuccess(ExperimentDTO newExperiment) {
            addButtonInProgress = false;
            retrieveExperiments();
            if (!sectionVisible)
                errorMessage.setText("Added new Experiment: " + newExperiment.toString());
            // add this experiment to the expression section
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(false);
        }

    }

    private class ErrorMessageCleanupListener implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            clearErrorMessages();
        }
    }

    public void setExperimentFilter(ExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
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
            if (!validateExperiment(updatedExperiment)) {
                cleanupOnExit();
                return;
            }

            // check if the experiment already exists
            if (experimentExists(updatedExperiment, false)) {
                errorMessage.setText("Another experiment with these attributes exists. " +
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

    private class UpdateExperimentAsyncCallback extends ZfinAsyncCallback<ExperimentDTO> {

        private UpdateExperimentAsyncCallback() {
            super("Error while updating experiment", errorMessage);
        }

        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
            updateButton.setEnabled(true);
            updateButtonInProgress = false;
        }

        // Refresh the experiment list
        public void onSuccess(ExperimentDTO updatedExperiment) {
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
                            if (isDebug())
                                Window.alert(updatedExperiment.toString());
                            updateTextInUpdatedRow(row, updatedExperiment);
                        }
                    }
                }
            }

            updateButton.setEnabled(true);
            updateButtonInProgress = false;
            // update expression section with new experiment attributes
            expressionSection.retrieveExpressions();
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * This method updates the values in a given row with the provided
     * experiment. Neither the selection box nor the delete button are changed.
     *
     * @param row        row index
     * @param experiment experiment
     */
    private void updateTextInUpdatedRow(int row, ExperimentDTO experiment) {
        displayTable.setText(row, HeaderName.GENE.getIndex(), experiment.getGeneName());
        displayTable.setText(row, HeaderName.FISH.getIndex(), experiment.getFishName());
        displayTable.setText(row, HeaderName.ENVIRONMENT.getIndex(), experiment.getEnvironmentDisplayValue());
        displayTable.setText(row, HeaderName.ASSAY.getIndex(), experiment.getAssay());
        displayTable.setText(row, HeaderName.ANTIBODY.getIndex(), experiment.getAntibody());
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

    private class RetrieveGeneListByAntibodyCallBack extends ZfinAsyncCallback<List<MarkerDTO>> {
        public RetrieveGeneListByAntibodyCallBack() {
            super("Error while readin genes by antibodies", FxExperimentModule.this.errorMessage);
        }

        public void onSuccess(List<MarkerDTO> genes) {
            //                Window.alert("brought back: " + genes.size() );
            String selectedGeneID = geneList.getValue(geneList.getSelectedIndex());
            //Window.alert("Selected Gene: " + selectedGeneID);
            geneList.clear();
            geneList.addItem("");
            int rowIndex = 1;
            for (MarkerDTO gene : genes) {
                geneList.addItem(gene.getAbbreviation(), gene.getZdbID());
                // make sure the selected gene is still selected
                if (gene.getZdbID().equals(selectedGeneID))
                    geneList.setItemSelected(rowIndex, true);
                rowIndex++;
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class AntibodyListChangeListener implements ChangeHandler {
        public void onChange(ChangeEvent event) {
            String antibodyID = antibodyList.getValue(antibodyList.getSelectedIndex());
            //Window.alert(antibodyID);
            curationExperimentRPCAsync.readGenesByAntibody(publicationID, antibodyID, new RetrieveGeneListByAntibodyCallBack());
        }
    }

    private class GeneListChangeListener implements ChangeHandler {
        public void onChange(ChangeEvent event) {
            String geneID = geneList.getValue(geneList.getSelectedIndex());
            //Window.alert(geneID);
            String assayName = assayList.getItemText(assayList.getSelectedIndex());
            //Window.alert(itemText);
            // only fetch antibodies if the right assay is selected
            if (ExpressionAssayDTO.isAntibodyAssay(assayName)) {
                curationExperimentRPCAsync.readAntibodiesByGene(publicationID, geneID, new RetrieveAntibodyList());
            }
            curationExperimentRPCAsync.readGenbankAccessions(publicationID, geneID, new GenbankSelectionListAsyncCallback(null));
        }
    }

    private class AssayListChangeListener implements ChangeHandler {
        public void onChange(ChangeEvent event) {
            String itemText = assayList.getItemText(assayList.getSelectedIndex());
            //Window.alert(itemText);
            if (ExpressionAssayDTO.isAntibodyAssay(itemText)) {
                antibodyList.setEnabled(true);
                String geneID = geneList.getValue(geneList.getSelectedIndex());
                // TODO: call gene on change listener: change from anaonymous to real inner class
                curationExperimentRPCAsync.readAntibodiesByGene(publicationID, geneID, new RetrieveAntibodyList());
            } else
                antibodyList.setEnabled(false);
        }
    }

    private class RetrieveAssayListCallback extends ZfinAsyncCallback<List<String>> {
        public RetrieveAssayListCallback(String message) {
            super(message, FxExperimentModule.this.errorMessage);
        }

        public void onSuccess(List<String> assays) {
            //Window.alert("brought back: " + experiments.size() );
            for (String assay : assays) {
                assayList.addItem(assay);
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class RetrieveEnvironmentListCallback extends ZfinAsyncCallback<List<EnvironmentDTO>> {
        public RetrieveEnvironmentListCallback(String message) {
            super(message, FxExperimentModule.this.errorMessage);
        }

        public void onSuccess(List<EnvironmentDTO> environments) {
            //Window.alert("brought back: " + experiments.size() );
            for (EnvironmentDTO environmentDTO : environments) {
                String name = environmentDTO.getName();
                if (name.startsWith("_"))
                    name = name.substring(1);
                environmentList.addItem(name, environmentDTO.getZdbID());
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class RetrieveSectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public RetrieveSectionVisibilityCallback(String message) {
            super(message, FxExperimentModule.this.errorMessage);
        }

        public void onSuccess(Boolean visible) {
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (sectionVisible) {
                setInitialValues();
                showExperimentSection.setText(HIDE);
            } else {
                setInitialValues();
                displayTable.createConstructionZone();
                showExperimentSection.setText(SHOW);
                loadingImage.setVisible(false);
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class RetrieveGenotypeListCallback extends ZfinAsyncCallback<List<FishDTO>> {
        public RetrieveGenotypeListCallback(String message) {
            super(message, FxExperimentModule.this.errorMessage);
        }

        public void onSuccess(List<FishDTO> genotypes) {
            //Window.alert("brought back: " + genotypes.size() );
            for (FishDTO genotypeHandle : genotypes) {
                fishList.addItem(genotypeHandle.getName(), genotypeHandle.getZdbID());
            }
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
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

        private HeaderName(int index, String value) {
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

        private ToggleLink(String value) {
            this.text = value;
        }

        public String getText() {
            return text;
        }
    }
}
