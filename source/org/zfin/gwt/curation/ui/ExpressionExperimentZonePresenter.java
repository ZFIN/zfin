package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionAssayDTO;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.ListBoxWrapper;
import org.zfin.gwt.root.ui.SessionSaveService;
import org.zfin.gwt.root.ui.SessionSaveServiceAsync;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * construction zone
 */
public class ExpressionExperimentZonePresenter implements Presenter {

    private ExpressionExperimentZoneView view;
    private String publicationID;
    private boolean debug;

    // filter set by the banana bar
    private ExperimentDTO experimentFilter = new ExperimentDTO();
    private String figureID;

    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();
    private SessionSaveServiceAsync sessionRPC = SessionSaveService.App.getInstance();

    private List<ExperimentDTO> experiments = new ArrayList<>(15);

    // flag that indicates if the experiment section is visible or not.
    private boolean sectionVisible;


    public ExpressionExperimentZonePresenter(ExpressionExperimentZoneView view, String publicationID, boolean debug) {
        this.view = view;
        this.publicationID = publicationID;
        this.debug = debug;
        experimentFilter = new ExperimentDTO();
        experimentFilter.setPublicationID(publicationID);
        view.setPresenter(this);
    }

    public void bind() {
        addDynamicClickHandler();
    }

    private void addDynamicClickHandler() {
        // assay changes
        view.getAssayList().addChangeHandler(new AssayListChangeListener());

        // gene changes
        view.getGeneList().addChangeHandler(new GeneListChangeListener());

        // antibody changes
        view.getAntibodyList().addChangeHandler(new AntibodyListChangeListener());
        addChangeListenersToConstructionZoneElements();
    }

    private ErrorMessageCleanupListener errorMessageCleanupListener = new ErrorMessageCleanupListener();

    private class ErrorMessageCleanupListener implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            clearErrorMessages();
        }
    }


    private void addChangeListenersToConstructionZoneElements() {
        view.getAssayList().addChangeHandler(errorMessageCleanupListener);
        view.getGeneList().addChangeHandler(errorMessageCleanupListener);
        view.getAntibodyList().addChangeHandler(errorMessageCleanupListener);
        view.getFishList().addChangeHandler(errorMessageCleanupListener);
        view.getEnvironmentList().addChangeHandler(errorMessageCleanupListener);
        view.getGenbankList().addChangeHandler(errorMessageCleanupListener);
    }


    @Override
    public void go() {
        bind();
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
        curationExperimentRPCAsync.getExperimentsByFilter(experimentFilter, new RetrieveExperimentsCallback(experiments));
    }

    public void setExperimentFilter(ExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
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

    /**
     * Un-select all experiment check boxes.
     */
    public void unselectAllExperiments() {
        view.clearSelectedExperiments();
        view.showExperiments(false);
        view.getDisplayTable().uncheckAllRecords();
    }

    /**
     * Set a single experiment (check mark).
     * All other experiments that may be checked are un-checked.
     *
     * @param experiment Experiment DTO
     */
    public void setSingleExperiment(ExperimentDTO experiment) {
        view.clearSelectedExperiments();
        view.getSelectedExperiments().add(experiment);
        view.showExperiments(false);
        if (sectionVisible)
            view.getDisplayTable().createExperimentTable();
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


    // Handler

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

    private class AntibodyListChangeListener implements ChangeHandler {
        public void onChange(ChangeEvent event) {
            String antibodyID = view.getAntibodyList().getValue(view.getAntibodyList().getSelectedIndex());
            //Window.alert(antibodyID);
            curationExperimentRPCAsync.readGenesByAntibody(publicationID, antibodyID, new RetrieveGeneListByAntibodyCallBack());
        }
    }

    private class RetrieveExperimentsCallback extends ZfinAsyncCallback<List<ExperimentDTO>> {

        private List<ExperimentDTO> experiments = new ArrayList<>(10);

        public RetrieveExperimentsCallback(List<ExperimentDTO> experiments) {
            super("Error while reading Experiment Filters", view.errorElement, view.loadingImage);
            this.experiments = experiments;
        }

        @Override
        public void onSuccess(List<ExperimentDTO> list) {
            super.onSuccess(list);
            experiments.clear();
            for (ExperimentDTO id : list) {
                if (id.getEnvironment().getName().startsWith("_"))
                    id.getEnvironment().setName(id.getEnvironment().getName().substring(1));
                experiments.add(id);
            }
            Collections.sort(experiments);
            //Window.alert("SIZE: " + experiments.size());
            if (sectionVisible)
                view.getDisplayTable().createExperimentTable(experiments);
            // populate expression section
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

    private class AssayListChangeListener implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            String itemText = view.getAssayList().getItemText(view.getAssayList().getSelectedIndex());
            //Window.alert(itemText);
            if (ExpressionAssayDTO.isAntibodyAssay(itemText)) {
                view.getAntibodyList().setEnabled(true);
                String geneID = view.getGeneList().getValue(view.getGeneList().getSelectedIndex());
                // TODO: call gene on change listener: change from anonymous to real inner class
                curationExperimentRPCAsync.readAntibodiesByGene(publicationID, geneID, new RetrieveAntibodyList());
            } else
                view.getAntibodyList().setEnabled(false);
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
            //Window.alert("Success");
            experiments.remove(experiment);
            view.getDisplayTable().createExperimentTable();
            // also remove the figure annotations that were used with this experiments
///            expressionSection.removeFigureAnnotations(experiment);
///            fireEventSuccess();
        }

    }

    private class GeneListChangeListener implements ChangeHandler {

        public void onChange(ChangeEvent event) {
            String geneID = view.getGeneList().getValue(view.getGeneList().getSelectedIndex());
            String assayName = view.getAssayList().getItemText(view.getAssayList().getSelectedIndex());
            //Window.alert(itemText);
            // only fetch antibodies if the right assay is selected
            if (ExpressionAssayDTO.isAntibodyAssay(assayName)) {
                curationExperimentRPCAsync.readAntibodiesByGene(publicationID, geneID, new RetrieveAntibodyList());
            }
            curationExperimentRPCAsync.readGenbankAccessions(publicationID, geneID, new GenbankSelectionListAsyncCallback(null));
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
            super("Error retrieving gene selection list", view.getErrorElement());
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
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (sectionVisible) {
                setInitialValues();
            } else {
                setInitialValues();
                view.getDisplayTable().createConstructionZone();
            }
        }

        private void setInitialValues() {
            retrieveExperiments();
            retrieveConstructionZoneValues();
        }

    }

    public boolean isDebug() {
        return debug;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public boolean isSectionVisible() {
        return sectionVisible;
    }
}
