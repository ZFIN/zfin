package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.*;

/**
 * Phenotype section of the Phenotype curation page.
 * It consists of three parts:
 * 1) Show and hide link
 * 2) Display expressions
 * 3) Construction zone to create a new expression.
 * <p/>
 * Ad 1)
 * A) The expression section can be hidden by clicking on 'hide' which then hides the display
 * part and the construction zone as well.
 * B) The state of the visibility is saved in the database and remembered for this publication for future.
 * in the curation_session table.
 * <p/>
 * Ad 2)
 * A) Displayed are all expressions (unless the experiment filter, aka banana bar when it was yellow instead of
 * green), ordered by figure, gene (non-gene experiments first) and fish.
 * B) Delete-Button: The Delete-button removes the expression, i.e.
 * expression_results, expression_pattern_figure. Clicking the button pops up a Java Script confirmation box
 * that indicates the action's consequences. The experiments the expressions are associated are not deleted.
 * C) When you mouse over an experiment its background color changes to a light green-blue that changes back to its
 * original background color upon mouse out
 * D) No group striping is implemented on the old page. I suggest grouping them by figure if no figure filter is set.
 * E) Clicking anywhere on an experiment row sets the selection check box and copies the values into the
 * construction zone. It keeps the light green-blue background color until this expression is unchecked.
 * F) expressed in: List all terms in alphabetical order by the superterm. Display composed terms using the format
 * [superterm:subterm]. If the term is 'unspecified' then highlight the term in orange. If a term is NOT expressed
 * the term should be highlighted in red (formerly [(not)])
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
 * G) Update-Button: The update button is disabled by default and will only be enabled when an existing experiment
 * is selected (and copied into the construction zone).
 * H) Add an experiment:
 * I)   Adding an experiment requires to either selecting a gene or an antibody or both and all other
 * attributes except the GenBank accession number which is optional.
 * II) Experiments have to be unique according to the combination
 * Gene/Fish/Environment/Assay/Antibody/GenBank, i.e. you cannot create two experiments with the same
 * values for these attributes. An error message is displayed below the construction zone if a new
 * experiment equals an existing one while highlighting the existing experiment in the list purple that
 * that the new experiment is conflicting with.
 * III) Updating an existing experiment is validated against the uniqueness constraint of II). An error
 * message is displayed when the update matches another existing experiment. Before an experiment is
 * updated a JavaScript alert box pops up to ask for confirmation.
 */
public class MutantModule extends Composite implements ExpressionSection<PhenotypeTermDTO, PhenotypeFigureStageDTO> {

    // div-elements
    public static final String SHOW_HIDE_EXPRESSIONS = "show-hide-expressions";
    public static final String EXPRESSIONS_DISPLAY = "display-expressions";
    public static final String CHECK_SIZE_BAR = "check-size-bar";
    public static final String MUTANTS_CONSTRUCTION_ZONE = "display-mutants-construction-zone";
    public static final String IMAGE_LOADING_EXPRESSION_SECTION = "image-loading-expression-section";
    public static final String EXPRESSIONS_DISPLAY_ERRORS = "display-expression-errors";

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showExpressionSection = new Hyperlink();
    private FlexTable constructionRow = new FlexTable();
    private MutantFlexTable displayTable;
    private ErrorHandler errorElement = new SimpleErrorElement(EXPRESSIONS_DISPLAY_ERRORS);
    private BoxCheckAndSize boxCheckAndSize;

    // construction zone
    private Button addButton = new Button("Add");
    private Button resetButton = new Button("Reset");
    private StageSelector stageSelector = new StageSelector();
    private ListBox figureList = new ListBox();
    private ListBox fishList = new ListBox();
    private ListBox environmentList = new ListBox();
    // this list is being populated through the DisplayExperimentTable
    private VerticalPanel experimentSelection = new VerticalPanel();
    private boolean showSelectedMutantsOnly;

    // all annotations that are selected
    private List<PhenotypeFigureStageDTO> selectedExpressions = new ArrayList<PhenotypeFigureStageDTO>(10);
    // all mutants displayed on the page (all or a subset defined by the filter elements)
    private List<PhenotypeFigureStageDTO> displayedExpressions = new ArrayList<PhenotypeFigureStageDTO>(20);
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, PhenotypeFigureStageDTO> displayTableMap = new HashMap<Integer, PhenotypeFigureStageDTO>(20);

    // attributes for duplicate row
    private String duplicateRowOriginalStyle;
    private int duplicateRowIndex;

    private String figureID;
    // flag that indicates if the experiment section is visible or not.
    private boolean sectionVisible = true;
    // used to control that this module is loaded only once.
    private boolean initialized;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private CurationPhenotypeRPCAsync phenotypeCurationRPCAsync = CurationPhenotypeRPC.App.getInstance();
    private SessionSaveServiceAsync sessionRPC = SessionSaveService.App.getInstance();

    public static final String HIDE = "hide";
    public static final String SHOW = "show";

    private Set<PhenotypeTermDTO> expressedTerms = new HashSet<PhenotypeTermDTO>(20);
    // used for highlighting structures
    private ExpressedTermDTO expressedStructure;
    private boolean markStructures;
    private List<FigureDTO> allFigureDtos = new ArrayList<FigureDTO>(10);

    // injected variables
    private StructurePile structurePile;

    // Publication in question.
    private String publicationID;
    // filter set by the banana bar
    private ExperimentDTO experimentFilter = new ExperimentDTO();

    public MutantModule(String publicationID) {
        this.publicationID = publicationID;
        displayTable = new MutantFlexTable(HeaderName.getHeaderNames());
        initGUI();
    }

    public void setPileStructure(StructurePile structurePile) {
        this.structurePile = structurePile;
    }

    /**
     * The data should only be loaded when the filter bar is initialized.
     * So this method is called from the FxFilterTable.
     */
    public void runModule() {
        if (!initialized) {
            setInitialValues();
            initialized = true;
        }
    }

    private void initGUI() {
        initShowHideGUI();
        experimentSelection.setVisible(false);

        initCheckAndSize();
        displayTable.setWidth("100%");
        RootPanel.get(EXPRESSIONS_DISPLAY).add(displayTable);
        RootPanel.get(MUTANTS_CONSTRUCTION_ZONE).add(constructionRow);
        addChangeHandlers();
    }

    private void addChangeHandlers() {
        figureList.addChangeHandler(new ClearErrorMessagesChangeHandler(errorElement));
        fishList.addChangeHandler(new ClearErrorMessagesChangeHandler(errorElement));
        environmentList.addChangeHandler(new ClearErrorMessagesChangeHandler(errorElement));
    }

    private void initCheckAndSize() {
        boxCheckAndSize = new BoxCheckAndSize(true, RootPanel.get(EXPRESSIONS_DISPLAY), publicationID);
        boxCheckAndSize.addCheckAllClickHandler(new SelectUnselectAllMutantsClickHandler(true));
        boxCheckAndSize.addUnSelectAllClickHandler(new SelectUnselectAllMutantsClickHandler(false));
        boxCheckAndSize.setWidth("100%");
        boxCheckAndSize.setStyleName("right-align");
        RootPanel.get(CHECK_SIZE_BAR).add(boxCheckAndSize);
    }

    private void initShowHideGUI() {
        RootPanel.get(SHOW_HIDE_EXPRESSIONS).add(panel);
        Label experimentLabel = new Label("Mutants: ");
        experimentLabel.setStyleName(WidgetUtil.BOLD);
        panel.add(experimentLabel);
    }

    private void setInitialValues() {
        retrieveExpressions();
        retrieveConstructionZoneValues();
        retrieveSessionValues();
    }

    private void retrieveSessionValues() {
        boxCheckAndSize.initializeSessionVariables();
    }

    private void retrieveConstructionZoneValues() {
        // figure list
        curationRPCAsync.getFigures(publicationID, new RetrieveFiguresCallback());

        // stage list
        curationRPCAsync.getStages(new RetrieveStageListCallback());
        // retrieve fish list
        // fish (genotype) list
        String message = "Error while reading Genotypes";
        curationRPCAsync.getGenotypes(publicationID, new RetrieveGenotypeListCallBack(fishList, message, errorElement));

        // environment list
        message = "Error while reading the environment";
        curationRPCAsync.getEnvironments(publicationID,
                new RetrieveEnvironmentListCallBack(environmentList, message, errorElement));

        // set stage selector mode from session
        sessionRPC.isStageSelectorSingleMode(publicationID, new RetrieveStageSelectorCallback(errorElement, stageSelector));

    }

    // Retrieve experiments from the server

    public void retrieveExpressions() {
        phenotypeCurationRPCAsync.getExpressionsByFilter(experimentFilter, figureID, new RetrieveExpressionsCallback());
    }


    private void recordAllExpressedTerms() {
        expressedTerms.clear();
        for (PhenotypeFigureStageDTO expression : this.displayedExpressions) {
            expressedTerms.addAll(expression.getExpressedTerms());
        }
    }

    private String createSpanElement(PhenotypeTermDTO term, String classNamePrefix) {
        if (term == null)
            return null;

        StringBuilder classSpan = new StringBuilder(50);
        if (markStructures && (term != null && term.equals(expressedStructure))) {
            if (classNamePrefix == null)
                classSpan.append("<span class='bold'>");
            else
                classSpan.append("<span class='" + classNamePrefix + " bold'>");
        } else {
            if (classNamePrefix != null)
                classSpan.append("<span class='" + classNamePrefix + "'>");
        }
        classSpan.append(term.getDisplayName());
        if (classNamePrefix != null || markStructures)
            classSpan.append("</span>");
        return classSpan.toString();
    }

    public Set<PhenotypeTermDTO> getExpressedTermDTOs() {
        return expressedTerms;
    }

    /**
     * Use to re-display (if visible) the figure annotations and highlight the given
     * structure bold as it needs to be removed before a structure from the structure pile
     * can be deleted.
     *
     * @param dto  ExpressedTermDTO
     * @param mark boolean: if true bold face the structures, if false undo the bold facing.
     */
    public void markStructuresForDeletion(ExpressedTermDTO dto, boolean mark) {
        if (mark) {
            markStructures = true;
            expressedStructure = dto;
        }
        displayTable.createMutantTable();
        markStructures = false;
    }

    /**
     * Update the given figure annotation with new expressed terms.
     * This is called from the structure section.
     *
     * @param updatedFigureAnnotations ExpressionFigureStageDTO
     */
    public void updateFigureAnnotations(List<PhenotypeFigureStageDTO> updatedFigureAnnotations) {

        for (PhenotypeFigureStageDTO updatedEfs : updatedFigureAnnotations) {
            for (PhenotypeFigureStageDTO efs : displayedExpressions) {
                if (efs.getUniqueID().equals(updatedEfs.getUniqueID()))
                    efs.setExpressedTerms(updatedEfs.getExpressedTerms());
            }
        }
        displayTable.createMutantTable();
        recordAllExpressedTerms();
        // remove check marks from annotations.
    }

    /**
     * 1) Retrieve the expression list again.
     * 2) remove check marks from expressions.
     * 3) update expressedTerms collection.
     * 4) un-check all experiments in the experiment section.
     */
    public void postUpdateStructuresOnExpression() {
        selectedExpressions.clear();
        showSelectedMutantsOnly = false;
        displayTable.uncheckAllRecords();
        stageSelector.resetGui();
        fishList.setItemSelected(0, true);
        figureList.setItemSelected(0, true);
        environmentList.setItemSelected(0, true);
        retrieveExpressions();
    }

    /**
     * This method updates the structure pile with the checked figure annotation.
     */
    protected void sendFigureAnnotationsToStructureSection() {
        structurePile.updateFigureAnnotations(selectedExpressions);
    }

    private void saveCheckStatusInSession(PhenotypeFigureStageDTO checkedExpression, boolean isChecked) {
        String message = "Error while saving expression check mark status.";
        //phenotypeCurationRPCAsync.setFigureAnnotationStatus(checkedExpression, isChecked, new VoidAsyncCallback(new Label(message), null));
    }

    public void showExpression(boolean showSelectedMutants) {
        //Window.alert("HIO");
        this.showSelectedMutantsOnly = showSelectedMutants;
        displayTable.createMutantTable();
    }

    /**
     * Retrieve the list of expression records that are selected.
     *
     * @return list of expression figure stage info
     */
    public List<PhenotypeFigureStageDTO> getSelectedExpressions() {
        return selectedExpressions;
    }

    public void applyFilterElements(String figureID, ExperimentDTO filter) {
        setFigureID(figureID);
        // needed for new expression retrieval
        this.experimentFilter = filter;
        // un-check all checked expressions if any of the filters is set except the ones that are not hidden
        // by the filter
        if (StringUtils.isNotEmpty(figureID) || filter.getGene() != null
                || StringUtils.isNotEmpty(filter.getFishID())) {
            for (PhenotypeFigureStageDTO expression : selectedExpressions) {
                if (StringUtils.isNotEmpty(figureID) && !expression.getFigure().getZdbID().equals(figureID)) {
                    uncheckExpressionRecord(expression);
                }
/*
                if (StringUtils.isNotEmpty(filter.getFishID()) &&
                        !expression.getExperiment().getFishID().equals(filter.getFishID())) {
                    uncheckExpressionRecord(expression);
                }
*/
            }
        }
        selectedExpressions.clear();
        retrieveExpressions();
    }

    private void uncheckExpressionRecord(PhenotypeFigureStageDTO expression) {
        saveCheckStatusInSession(expression, false);
        displayTable.showClearAllLink();
        displayTable.showHideClearAllLink();

    }

    // ****************** Handlers, Callbacks, etc.


    private class SelectUnselectAllMutantsClickHandler implements ClickHandler {

        private boolean checkAll;

        SelectUnselectAllMutantsClickHandler(boolean checkAll) {
            this.checkAll = checkAll;
        }

        public void onClick(ClickEvent clickEvent) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            clearErrorMessages();
            // store selected experiment for update purposes
            //saveCheckStatusInSession(checkedExpression, checkbox.getValue());

            displayTable.selectAllNoneRecords(checkAll);
            if (checkAll)
                selectedExpressions = displayedExpressions;
            else
                selectedExpressions = new ArrayList<PhenotypeFigureStageDTO>(15);
            //Window.alert("Selected Expressions: " + selectedExpressions.size());
            sendFigureAnnotationsToStructureSection();
        }
    }

    /**
     * This Click Listener is activated upon clicking the selection check box in the
     * Expression display section. It should do two things:
     * 1) copy the values for the experiment / figure / stage range into the construction zone
     * 2) Highlight the selected expression record.
     * 3) Save the check mark status (checked or unchecked) in session.
     * 4) Copy the figure annotation into the structure section.
     */
    private class MutantSelectClickHandler implements ClickHandler {

        private PhenotypeFigureStageDTO checkedExpression;
        private CheckBox checkbox;

        private MutantSelectClickHandler(PhenotypeFigureStageDTO checkedExpression, CheckBox checkbox) {
            this.checkedExpression = checkedExpression;
            this.checkbox = checkbox;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            clearErrorMessages();
            // store selected experiment for update purposes
            WidgetUtil.selectListBox(figureList, checkedExpression.getFigure().getZdbID());
            stageSelector.selectStartStage(checkedExpression.getStart().getZdbID());
            stageSelector.selectEndStage(checkedExpression.getEnd().getZdbID());
            saveCheckStatusInSession(checkedExpression, checkbox.getValue());
            if (checkbox.getValue()) {
                selectedExpressions.add(checkedExpression);
            } else {
                selectedExpressions.remove(checkedExpression);
            }
            WidgetUtil.selectListBox(environmentList, checkedExpression.getEnvironment().getZdbID());
            WidgetUtil.selectListBox(fishList, checkedExpression.getGenotype().getZdbID());
            //Window.alert("Selected Expressions: "+selectedExpressions.size());
            sendFigureAnnotationsToStructureSection();
            displayTable.showHideClearAllLink();
        }

    }

    private PhenotypeFigureStageDTO createBasicPhenoFigDto(String figID, String fishID, String environmentID) {
        PhenotypeFigureStageDTO newMutantFigureStage = new PhenotypeFigureStageDTO();
        EnvironmentDTO envDto = new EnvironmentDTO();
        envDto.setZdbID(environmentID);
        newMutantFigureStage.setEnvironment(envDto);
        FigureDTO figDto = new FigureDTO();
        figDto.setZdbID(figID);
        newMutantFigureStage.setFigure(figDto);
        GenotypeDTO fishDto = new GenotypeDTO();
        fishDto.setZdbID(fishID);
        newMutantFigureStage.setGenotype(fishDto);
        return newMutantFigureStage;
    }

    private void addStageToPhenoFigStageDto(String startStageID, String endStageID, PhenotypeFigureStageDTO phenotypeFigureStageDTO) {
        //Window.alert("Experiment size: " + experiments);

        StageDTO start = new StageDTO();
        start.setZdbID(startStageID);
        phenotypeFigureStageDTO.setStart(start);
        StageDTO end = new StageDTO();
        end.setZdbID(endStageID);
        phenotypeFigureStageDTO.setEnd(end);

    }

    private class DeleteFigureAnnotationClickHandler implements ClickHandler {

        private PhenotypeFigureStageDTO expressionFigureStage;

        public DeleteFigureAnnotationClickHandler(PhenotypeFigureStageDTO expressionFigureStage) {
            this.expressionFigureStage = expressionFigureStage;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            String message = "Are you sure you want to delete this figure annotation?";
            if (!Window.confirm(message))
                return;
            deleteExperiment(expressionFigureStage);
        }

        private void deleteExperiment(PhenotypeFigureStageDTO figureAnnotation) {
            phenotypeCurationRPCAsync.deleteFigureAnnotation(figureAnnotation, new DeleteFigureAnnotationCallback(figureAnnotation));
        }

    }

    /**
     * Add entry to a listBox widget. Ensure that only distinct entries are added.
     *
     * @param listBox GWTListBox
     * @param display display string
     * @param id      id string
     */
    protected void addDistinctEntryToGuiList(ListBox listBox, String display, String id) {
        int numOfRows = listBox.getItemCount();
        for (int row = 0; row < numOfRows; row++) {
            String listId = listBox.getValue(row);
            if (id.equals(listId)) {
                return;
            }
        }
        listBox.addItem(display, id);
    }

    private class DeleteFigureAnnotationCallback extends ZfinAsyncCallback<Void> {

        private PhenotypeFigureStageDTO figureAnnotation;

        DeleteFigureAnnotationCallback(PhenotypeFigureStageDTO figureAnnotation) {
            super("Error while deleting Figure Annotation", errorElement);
            this.figureAnnotation = figureAnnotation;
        }

        @Override
        public void onSuccess(Void exp) {
            super.onSuccess(exp);
            //Window.alert("Success");
            // remove from the dashboard list
            displayedExpressions.remove(figureAnnotation);
            selectedExpressions.remove(figureAnnotation);
            // re-create the display table
            displayTable.createMutantTable();
            // update expression list in structure section.
            sendFigureAnnotationsToStructureSection();
            clearErrorMessages();
            postUpdateStructuresOnExpression();
        }

    }

    private class RetrieveExpressionsCallback extends ZfinAsyncCallback<List<PhenotypeFigureStageDTO>> {

        public RetrieveExpressionsCallback() {
            super("Error while reading Experiment Filters", errorElement, IMAGE_LOADING_EXPRESSION_SECTION);
        }

        @Override
        public void onSuccess(List<PhenotypeFigureStageDTO> list) {
            super.onSuccess(list);
            displayedExpressions.clear();
            if (list == null)
                return;

            for (PhenotypeFigureStageDTO id : list) {
                displayedExpressions.add(id);
            }
            Collections.sort(displayedExpressions);
            //Window.alert("SIZE: " + experiments.size());
            if (sectionVisible)
                displayTable.createMutantTable();
            recordAllExpressedTerms();
            // phenotypeCurationRPCAsync.getFigureAnnotationCheckmarkStatus(publicationID, new FigureAnnotationCheckmarkStatusCallback());
        }

    }

    /**
     * Show or hide expression section
     */
    private class ShowHideExpressionSectionHandler implements ClickHandler {

        public void onClick(ClickEvent event) {
            String errorMessage = "Error while trying to save expression visibility";
            if (sectionVisible) {
                // hide experiments
                displayTable.setVisible(false);
                showExpressionSection.setText(SHOW);
                sectionVisible = false;
                // phenotypeCurationRPCAsync.setExpressionVisibilitySession(publicationID, false,
                ///new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            } else {
                // display experiments
                // check if it already exists
                if (displayTable != null && displayTable.getRowCount() > 0) {
                    displayTable.setVisible(true);
                } else {
                    retrieveExpressions();
                    if (displayTable.getRowCount() == 0)
                        retrieveConstructionZoneValues();
                }
                showExpressionSection.setText(HIDE);
                sectionVisible = true;
                //phenotypeCurationRPCAsync.setExpressionVisibilitySession(publicationID, true,
                /// new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            }
            clearErrorMessages();
        }

    }

    // avoid double updates
    private boolean addButtonInProgress;

    private class AddMutantClickHandler implements ClickHandler {

        public void onClick(ClickEvent event) {
            // do not proceed if it just has been clicked once
            // and is being worked on
            if (addButtonInProgress) {
                addButton.setEnabled(false);
                return;
            }
            addButtonInProgress = true;
            boolean expressionsExist = false;
            List<PhenotypeFigureStageDTO> newFigureAnnotations = getMutantFigureStageFromConstructionZone();
            for (PhenotypeFigureStageDTO mutant : newFigureAnnotations) {
                if (!isValidExperiment(mutant)) {
                    cleanupOnExit();
                    return;
                }
                if (isMutantFigureStageOnPile(mutant)) {
                    errorElement.setError("Expression already exists. Expressions have to be unique!");
                    cleanupOnExit();
                    expressionsExist = true;
                }
            }
            if (stageSelector.isMultiStageMode() && !stageSelector.isMultiStageSelected()) {
                errorElement.setError("No stage selected.  Please select at least one stage.");
                cleanupOnExit();
                return;
            }
            if (newFigureAnnotations.isEmpty()) {
                errorElement.setError("No experiment selected. Please select at least one experiment!");
                cleanupOnExit();
                return;
            }
            if (expressionsExist)
                return;
            phenotypeCurationRPCAsync.createMutantFigureStages(newFigureAnnotations, new AddMutantCallback(newFigureAnnotations));
        }

        private void cleanupOnExit() {
            addButton.setEnabled(true);
            addButtonInProgress = false;
        }

        /**
         * Create a list of Expression records that should be created as selected in the
         * construction zone. Note, there could be more than one experiment selected!
         *
         * @return list of Expression Figure Stage objects.
         */
        private List<PhenotypeFigureStageDTO> getMutantFigureStageFromConstructionZone() {
            String figID = figureList.getValue(figureList.getSelectedIndex());
            String startStageID = stageSelector.getSelectedStartStageID();
            String endStageID = stageSelector.getSelectedEndStageID();
            String fishID = fishList.getValue(fishList.getSelectedIndex());
            String environmentID = environmentList.getValue(environmentList.getSelectedIndex());

            List<PhenotypeFigureStageDTO> phenotypeFigureStageDTOList = new ArrayList<PhenotypeFigureStageDTO>(4);
            if (stageSelector.isDualStageMode()) {
                PhenotypeFigureStageDTO dto = createBasicPhenoFigDto(figID, fishID, environmentID);
                phenotypeFigureStageDTOList.add(dto);
                addStageToPhenoFigStageDto(startStageID, endStageID, dto);
                dto.setPublicationID(publicationID);
            } else {
                List<String> stageIDs = stageSelector.getSelectedStageIDs();
                for (String stageID : stageIDs) {
                    PhenotypeFigureStageDTO dto = createBasicPhenoFigDto(figID, fishID, environmentID);
                    dto.setPublicationID(publicationID);
                    phenotypeFigureStageDTOList.add(dto);
                    addStageToPhenoFigStageDto(stageID, stageID, dto);
                }
            }
            return phenotypeFigureStageDTOList;
        }

        /**
         * Check if the mutant figure stage already exists in the list.
         * Expressions have to be unique.
         *
         * @param mutant expression figure stage DTO
         * @return true if experiment is found in the full list (new experiment) or in the list except itself
         *         false if experiment is different from all other experiments
         */
        private boolean isMutantFigureStageOnPile(PhenotypeFigureStageDTO mutant) {
            int rowIndex = 1;
            for (PhenotypeFigureStageDTO existingMutant : displayedExpressions) {
                if (existingMutant.getUniqueID().equals(mutant.getUniqueID())) {
                    duplicateRowIndex = rowIndex;
                    duplicateRowOriginalStyle = displayTable.getRowFormatter().getStyleName(rowIndex);
                    displayTable.getRowFormatter().setStyleName(rowIndex, "experiment-duplicate");
                    return true;
                }
                rowIndex++;
            }
            return false;
        }

    }

    /**
     * Reset figure, experiments and stage info to default values.
     */
    private class ResetExpressionConstructionClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            figureList.setSelectedIndex(0);
            stageSelector.setGuiToDefault();
            clearErrorMessages();
        }

    }

    /**
     * Check that the expression is valid:
     * 1) figure defined
     * 2) start and end stage defined
     * 3) experiment ID defined
     *
     * @param mutant figure stage DTO
     * @return boolean
     */
    private boolean isValidExperiment(PhenotypeFigureStageDTO mutant) {
        if (StringUtils.isEmpty(mutant.getStart().getZdbID()))
            return false;
        if (StringUtils.isEmpty(mutant.getEnd().getZdbID()))
            return false;
        if (mutant.getFigure() == null || StringUtils.isEmpty(mutant.getFigure().getZdbID())) {
            errorElement.setError("No Figure is selected!");
            return false;
        }
        if (mutant.getEnvironment() == null || StringUtils.isEmpty(mutant.getEnvironment().getZdbID())) {
            errorElement.setError("No environment is selected!");
            return false;
        }
        if (mutant.getGenotype() == null || StringUtils.isEmpty(mutant.getGenotype().getZdbID())) {
            errorElement.setError("No genotypes is selected!");
            return false;
        }
        // check that end stage comes after start stage
        if (stageSelector.isDualStageMode()) {
            if (stageSelector.validDualStageSelection() != null) {
                errorElement.setError(stageSelector.validDualStageSelection());
                return false;
            }
        } else {
            if (stageSelector.validMultiStageSelection() != null) {
                errorElement.setError(stageSelector.validMultiStageSelection());
                return false;
            }
        }
        return true;
    }

    /**
     * Remove error messages
     * un-mark duplicate mutant records
     */
    public void clearErrorMessages() {
        errorElement.clearAllErrors();
        if (duplicateRowIndex > 0)
            displayTable.getRowFormatter().setStyleName(duplicateRowIndex, duplicateRowOriginalStyle);
    }

    private class MutantFlexTable extends ZfinFlexTable {

        private HeaderName[] headerNames;

        MutantFlexTable(HeaderName[] headerNames) {
            super(headerNames.length, HeaderName.SELECT.index);
            this.headerNames = headerNames;
            setToggleHyperlink(ToggleLink.SHOW_SELECTED_EXPRESSIONS_ONLY.getText(), ToggleLink.SHOW_ALL_EXPRESSIONS.getText());
            addToggleHyperlinkClickHandler(new ShowSelectedExpressionClickHandler(showSelectedRecords));
        }

        protected void createMutantTable() {
            clearTable();
            createConstructionZone();
            // header row index = 0
            createTableHeader();
            int rowIndex = 1;
            //Window.alert("Experiment List Size: " + experiments.size());
            PhenotypeFigureStageDTO previousExpression = null;
            // first element is an odd group element
            int groupIndex = 1;

            List<PhenotypeFigureStageDTO> expressionFigureStageDTOs;
            if (showSelectedMutantsOnly) {
                expressionFigureStageDTOs = new ArrayList<PhenotypeFigureStageDTO>(20);
                expressionFigureStageDTOs.addAll(selectedExpressions);
            } else {
                expressionFigureStageDTOs = displayedExpressions;
            }

            for (PhenotypeFigureStageDTO expression : expressionFigureStageDTOs) {

                // row index minus the header row
                displayTableMap.put(rowIndex, expression);
                CheckBox checkbox = new CheckBox(null);
                checkbox.setTitle(expression.getUniqueID());
                // if any figure annotations are already selected make sure they stay checked
                if (selectedExpressions.contains(expression)) {
                    checkbox.setValue(true);
                    //Window.alert("Checkbox");
                    //showClearAllLink();
                }
                checkbox.addClickHandler(new MutantSelectClickHandler(expression, checkbox));
                setWidget(rowIndex, HeaderName.SELECT.getIndex(), checkbox);
                Label figure = new Label(expression.getFigure().getLabel());
                figure.setTitle(expression.getFigure().getZdbID());
                setWidget(rowIndex, HeaderName.FIGURE.getIndex(), figure);
                Label genotype = new Label(expression.getGenotype().getHandle());
                genotype.setTitle(expression.getGenotype().getZdbID());
                setWidget(rowIndex, HeaderName.FISH.getIndex(), genotype);
                Widget environment = new Label(expression.getEnvironment().getName());
                environment.setTitle(expression.getEnvironment().getZdbID());
                setWidget(rowIndex, HeaderName.ENVIRONMENT.getIndex(), environment);
                setText(rowIndex, HeaderName.STAGE_RANGE.getIndex(), expression.getStageRange());

                Widget terms = createTermList(expression);
                setWidget(rowIndex, HeaderName.EXPRESSED_IN.getIndex(), terms);

                Button delete = new Button("X");
                delete.setTitle(expression.getUniqueID());
                delete.addClickHandler(new DeleteFigureAnnotationClickHandler(expression));
                setWidget(rowIndex, HeaderName.DELETE.getIndex(), delete);

                String previousID = null;
                if (previousExpression != null)
                    previousID = previousExpression.getUniqueID();

                groupIndex = setRowStyle(rowIndex, expression.getUniqueID(), previousID, groupIndex);
                rowIndex++;
                previousExpression = expression;
            }
            createBottomClearAllLinkRow(rowIndex);
            //Window.alert("HIO");
            showHideClearAllLink();
            //Window.alert("HIO II");
        }

        private void createConstructionZone() {
            stageSelector.setPublicationID(publicationID);
            addButton.addClickHandler(new AddMutantClickHandler());
            constructionRow.setWidget(0, 4, stageSelector.getPanelTitle());
            constructionRow.setWidget(1, 0, addButton);
            constructionRow.setWidget(1, 1, figureList);
            constructionRow.setWidget(1, 2, fishList);
            constructionRow.setWidget(1, 3, environmentList);
            HorizontalPanel pan = new HorizontalPanel();
            pan.add(stageSelector.getStartStagePanel());
            pan.add(stageSelector.getMultiStagePanel());
            constructionRow.setWidget(1, 4, pan);
            resetButton.addClickHandler(new ResetExpressionConstructionClickListener());
            constructionRow.setWidget(1, 5, resetButton);
            constructionRow.setWidget(2, 4, stageSelector.getEndStagePanel());
            constructionRow.setWidget(3, 4, stageSelector.getTogglePanel());
            constructionRow.getFlexCellFormatter().setColSpan(4, 0, 6);
        }


        private Widget createTermList(PhenotypeFigureStageDTO mutants) {
            // create phenotype list
            VerticalPanel phenotypePanel = new VerticalPanel();
            phenotypePanel.setStyleName("phenotype-table");
            List<PhenotypeTermDTO> terms = mutants.getExpressedTerms();
            int index = 1;
            for (final PhenotypeTermDTO term : terms) {
                StringBuilder text = new StringBuilder(50);
                String classSpan;
                if (term.getTag().equals("normal")) {
                    classSpan = createSpanElement(term, WidgetUtil.PHENOTYPE_NORMAL);
                } else if (term.getSuperterm().getTermName().equals("unspecified")) {
                    classSpan = createSpanElement(term, "term-unspecified");
                } else {

                    classSpan = createSpanElement(term, null);
                }
                text.append(classSpan);
                HTML phenotype = new HTML(text.toString());
                phenotype.setTitle(term.getZdbID());
                phenotypePanel.add(phenotype);
                index++;
            }
            return phenotypePanel;
        }

        /**
         * Un-check all checked records.
         * Save the checkbox status in session.
         */
        @Override
        protected void uncheckAllRecords() {
            super.uncheckAllRecords();
            uncheckAllCheckStatusInSession();
        }

        private void uncheckAllCheckStatusInSession() {
            for (PhenotypeFigureStageDTO expression : selectedExpressions) {
                saveCheckStatusInSession(expression, false);
            }
            selectedExpressions.clear();
            sendFigureAnnotationsToStructureSection();
        }

        @SuppressWarnings({"CastToConcreteClass"})
        public void onClick(ClickEvent clickEvent) {
            Cell cell = getCellForEvent(clickEvent);
            Widget widget = getWidget(cell.getRowIndex(), 0);
            if (widget == null || !(widget instanceof CheckBox))
                return;
            CheckBox checkBox = (CheckBox) widget;
            if (checkBox.getValue())
                checkBox.setValue(false);
            else
                checkBox.setValue(true);
            checkBox.fireEvent(clickEvent);
            showSelectedRecords.hideHyperlink(isAllUnchecked());
        }

        @Override
        protected void createTableHeader() {
            super.createTableHeader();
            for (HeaderName name : headerNames) {
                if (name.index != 0) {
                    setText(0, name.index, name.getName());
                    getCellFormatter().setStyleName(0, name.index, WidgetUtil.BOLD);
                }
            }
        }

        public void selectAllNoneRecords(boolean checkAll) {
            int rows = getRowCount();
            for (int rowIndex = 1; rowIndex < rows; rowIndex++) {
                Widget widget = getWidget(rowIndex, HeaderName.SELECT.getIndex());
                if (widget == null || !(widget instanceof CheckBox))
                    continue;
                CheckBox checkbox = (CheckBox) widget;
                checkbox.setValue(checkAll);
            }
        }

        /**
         * Show or hide expression section
         */
        private class ShowSelectedExpressionClickHandler implements ClickHandler {

            private ToggleHyperlink showExperiments;

            private ShowSelectedExpressionClickHandler(ToggleHyperlink showExperiments) {
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
                showExpression(!showSelectedExperimentsOnly);
            }

        }

    }

    protected enum ToggleLink {

        SHOW_SELECTED_EXPRESSIONS_ONLY("Show Selected Expressions Only"),
        SHOW_ALL_EXPRESSIONS("Show All Expressions");

        private String text;

        private ToggleLink(String value) {
            this.text = value;
        }

        public String getText() {
            return text;
        }
    }

    private class AddMutantCallback extends ZfinAsyncCallback<List<PhenotypeFigureStageDTO>> {

        private List<PhenotypeFigureStageDTO> figureAnnotations;

        public AddMutantCallback(List<PhenotypeFigureStageDTO> experiment) {
            super("Error while creating experiment", errorElement);
            this.figureAnnotations = experiment;
        }

        @Override
        public void onSuccess(List<PhenotypeFigureStageDTO> newAnnotations) {
            super.onSuccess(newAnnotations);
            displayedExpressions.addAll(newAnnotations);
            Collections.sort(displayedExpressions);
            displayTable.createMutantTable();
            recordAllExpressedTerms();
            addButtonInProgress = false;
            addButton.setEnabled(true);
            clearErrorMessages();
            postUpdateStructuresOnExpression();
        }

    }

    public void setFigureID(String figureID) {
        this.figureID = figureID;
        // update figure list
        updateFigureListBox();
    }

    public void setExperimentFilter(ExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
    }

    private void updateFigureListBox() {
        figureList.clear();
        for (FigureDTO figureDTO : allFigureDtos) {
            if (figureID == null || figureDTO.getZdbID().equals(figureID))
                figureList.addItem(figureDTO.getLabel(), figureDTO.getZdbID());
        }
    }

    public class RetrieveFiguresCallback extends ZfinAsyncCallback<List<FigureDTO>> {


        public RetrieveFiguresCallback() {
            super("Error while reading Figure Filters", errorElement);
        }

        @Override
        public void onSuccess(List<FigureDTO> list) {
            super.onSuccess(list);
            allFigureDtos = new ArrayList<FigureDTO>(list);
            updateFigureListBox();
            //Window.alert("SIZE: " + experiments.size());
        }

    }

    /**
     * Callback for reading all stages.
     */
    public class RetrieveStageListCallback extends ZfinAsyncCallback<List<StageDTO>> {

        public RetrieveStageListCallback() {
            super("Error while reading Figure Filters", errorElement);
        }

        @Override
        public void onSuccess(List<StageDTO> stages) {
            super.onSuccess(stages);
            //Window.alert("SIZE: " + experiments.size());
            stageSelector.setStageList(stages);
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

            int maxRows = displayTable.getRowCount();
            for (int row = 1; row < maxRows; row++) {
                if (!displayTable.isCellPresent(row, 0))
                    continue;
                Widget widget = displayTable.getWidget(row, 0);
                if (widget == null || !(widget instanceof CheckBox))
                    continue;

                CheckBox checkBox = (CheckBox) widget;
                for (ExpressionFigureStageDTO dto : filterValues.getFigureAnnotations()) {
                    if (dto.getUniqueID().equals(checkBox.getTitle())) {
                        checkBox.setValue(true);
                        PhenotypeFigureStageDTO checkedExpression = displayTableMap.get(row);
                        selectedExpressions.add(checkedExpression);
                    }
                }
            }
            displayTable.showHideClearAllLink();
            ////structurePile.updateFigureAnnotations(selectedExpressions);
        }
    }

    private class SectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public SectionVisibilityCallback(String message) {
            super(message, errorElement);
        }

        public void onSuccess(Boolean visible) {
            super.onSuccess(visible);
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (displayedExpressions == null || displayedExpressions.isEmpty()) {
                setInitialValues();
            } else {
                displayTable.createMutantTable();
                displayTable.showHideClearAllLink();
            }
            if (sectionVisible) {
                showExpressionSection.setText(HIDE);
            } else {
                showExpressionSection.setText(SHOW);
            }
        }

    }

    /**
     * Remove the figure annotations that were used in a given experiment that was deleted.
     *
     * @param deletedExperiment experiment that was deleted
     */
    public void removeFigureAnnotations(ExperimentDTO deletedExperiment) {
        Collection<ExpressionFigureStageDTO> toBeDeleted = new ArrayList<ExpressionFigureStageDTO>();
        for (PhenotypeFigureStageDTO efs : displayedExpressions) {
/*
            ExperimentDTO expDto = efs.getExperiment();
            if (expDto.getExperimentZdbID().equals(deletedExperiment.getExperimentZdbID()))
                toBeDeleted.add(efs);
*/
        }
        for (ExpressionFigureStageDTO efs : toBeDeleted) {
            displayedExpressions.remove(efs);
        }
        displayTable.createMutantTable();
    }

    private enum HeaderName {
        SELECT(0, ""),
        FIGURE(1, "Figure"),
        FISH(2, "Genotype"),
        ENVIRONMENT(3, "Environment"),
        STAGE_RANGE(4, "Stage Range"),
        EXPRESSED_IN(5, "Phenotype"),
        DELETE(6, "Delete");

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
            return MutantModule.HeaderName.values();
        }
    }


}