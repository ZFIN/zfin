package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.*;

/**
 * Phenotype section of the Phenotype curation page.
 * It consists of three parts:
 * 1) Show and hide link
 * 2) Display expressions
 * 3) Construction zone to create a new expression.
 * <p>
 * Ad 1)
 * A) The expression section can be hidden by clicking on 'hide' which then hides the display
 * part and the construction zone as well.
 * B) The state of the visibility is saved in the database and remembered for this publication for future.
 * in the curation_session table.
 * <p>
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
 * <p>
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
 * C) Fish-Selection-Box: It list first WT then the list of non-wildtype fish attributes to this publication
 * and then all other wild-type fish. No cross-interaction upon selection.
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
public class MutantModule extends Composite implements ExpressionSection<PhenotypeStatementDTO, PhenotypeExperimentDTO> {

    // div-elements
    public static final String SHOW_HIDE_EXPRESSIONS = "show-hide-expressions";

    public static final String EXPRESSIONS_DISPLAY = "display-expressions";
    public static final String EAP_DISPLAY = "display-eap";
    public static final String CHECK_SIZE_BAR = "check-size-bar";
    public static final String MUTANTS_CONSTRUCTION_ZONE = "display-mutants-construction-zone";
    public static final String IMAGE_LOADING_EXPRESSION_SECTION = "image-loading-expression-section";
    public static final String EXPRESSIONS_DISPLAY_ERRORS = "display-expression-errors";
    public static final String SHOW_HIDE_EAP = "show-hide-eap";


    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showExpressionSection = new Hyperlink();

    private HorizontalPanel eapPanel = new HorizontalPanel();
    private Hyperlink showEapSection = new Hyperlink();
    private FlexTable eapTable = new FlexTable();

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
    private List<PhenotypeExperimentDTO> selectedExpressions = new ArrayList<>(10);
    // all mutants displayed on the page (all or a subset defined by the filter elements)
    private List<PhenotypeExperimentDTO> displayedExpressions = new ArrayList<>(20);
    private List<ExpressionPhenotypeExperimentDTO> displayedEaps = new ArrayList<>(20);
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, PhenotypeExperimentDTO> displayTableMap = new HashMap<>(20);

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

    private Set<PhenotypeStatementDTO> expressedTerms = new HashSet<>(20);
    // used for highlighting structures
    private ExpressedTermDTO expressedStructure;
    private boolean markStructures;
    private List<FigureDTO> allFigureDtos = new ArrayList<>(10);

    // injected variables
    private StructurePile structurePile;

    // Publication in question.
    private String publicationID;
    // filter set by the banana bar
    private ExpressionExperimentDTO experimentFilter = new ExpressionExperimentDTO();

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
        setInitialValues();
    }

    public void reInit() {
        setInitialValues();
    }

    private void initGUI() {
        initShowHideGUI();

        experimentSelection.setVisible(false);


        initCheckAndSize();
        displayTable.setWidth("100%");
        RootPanel.get(EXPRESSIONS_DISPLAY).add(displayTable);

        RootPanel.get(MUTANTS_CONSTRUCTION_ZONE).add(constructionRow);
        RootPanel.get(SHOW_HIDE_EXPRESSIONS).add(panel);
        RootPanel.get(SHOW_HIDE_EAP).add(eapPanel);

        Label eapLabel = new Label("Expression Phenotypes: ");
        eapLabel.setStyleName(WidgetUtil.BOLD);
        eapPanel.add(eapLabel);
        showEapSection.setStyleName("small");
        showEapSection.setText(HIDE);
        showEapSection.setTargetHistoryToken(HIDE);
        showEapSection.addClickHandler(new ShowHideEapSectionHandler());
        eapPanel.add(showEapSection);

        eapTable.setWidth("100%");
        RootPanel.get(EAP_DISPLAY).add(eapTable);

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
        Label experimentLabel = new Label("Anatomy/GO Phenotypes: ");
        experimentLabel.setStyleName(WidgetUtil.BOLD);
        panel.add(experimentLabel);
    }

    public void setInitialValues() {
        retrieveExpressions();
        retrieveConstructionZoneValues();
        retrieveEaps();
        retrieveSessionValues();
    }

    private void retrieveSessionValues() {
        boxCheckAndSize.initializeSessionVariables();
    }


    public void retrieveConstructionZoneValues() {
        // figure list
        refreshFigureList();

        // stage list
        curationRPCAsync.getStages(new RetrieveStageListCallback());

        // retrieve fish list
        retrieveFishList();

        // environment list
        retrieveExperimentConditionList();

        // set stage selector mode from session
        sessionRPC.isStageSelectorSingleMode(publicationID, new RetrieveStageSelectorCallback(errorElement, stageSelector));

    }

    public void retrieveExperimentConditionList() {
        String message;
        message = "Error while reading the environment";
        curationRPCAsync.getEnvironments(publicationID,
                new RetrieveEnvironmentListCallBack(environmentList, message, errorElement));
    }

    public void retrieveFishList() {
        curationRPCAsync.getFishList(publicationID,
                new RetrieveFishListCallBack(fishList));
    }

    public void refreshFigureList() {
        curationRPCAsync.getFigures(publicationID, new RetrieveFiguresCallback());
    }

    public void updateFish() {
        fishList.clear();
        curationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack(fishList));
    }


    // Retrieve experiments from the server

    public void retrieveExpressions() {
        phenotypeCurationRPCAsync.getExpressionsByFilter(experimentFilter, figureID, new RetrieveExpressionsCallback());
    }

    public void retrieveEaps() {
        phenotypeCurationRPCAsync.getPhenotypeFromExpressionsByFilter(experimentFilter, figureID, new RetrieveEapsCallback());
    }


    private void recordAllExpressedTerms() {
        expressedTerms.clear();
        for (PhenotypeExperimentDTO expression : this.displayedExpressions) {
            expressedTerms.addAll(expression.getExpressedTerms());
        }
    }

    private String createSpanElement(PhenotypeStatementDTO pheno, String classNamePrefix) {
        if (pheno == null)
            return null;

        StringBuilder classSpan = new StringBuilder(50);
        if (markStructures && (pheno.equals(expressedStructure))) {
            if (classNamePrefix == null)
                classSpan.append("<span class='bold'>");
            else
                classSpan.append("<span class='" + classNamePrefix + " bold'>");
        } else {
            if (classNamePrefix != null)
                classSpan.append("<span class='" + classNamePrefix + "'>");
        }
        classSpan.append(pheno.getDisplayName());
        if (classNamePrefix != null || markStructures)
            classSpan.append("</span>");
        return classSpan.toString();
    }


    public Set<PhenotypeStatementDTO> getExpressedTermDTOs() {
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
    public void updateFigureAnnotations(List<PhenotypeExperimentDTO> updatedFigureAnnotations) {

        for (PhenotypeExperimentDTO updatedEfs : updatedFigureAnnotations) {
            for (PhenotypeExperimentDTO efs : displayedExpressions) {
                if (efs.getId() == updatedEfs.getId())
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
        //stageSelector.resetGui();
        //fishList.setItemSelected(0, true);
        //figureList.setItemSelected(0, true);
        //environmentList.setItemSelected(0, true);
        retrieveExpressions();
    }

    /**
     * This method updates the structure pile with the checked figure annotation.
     */
    protected void sendFigureAnnotationsToStructureSection() {
        structurePile.updateFigureAnnotations(selectedExpressions);
    }

    private void saveCheckStatusInSession(PhenotypeExperimentDTO checkedExpression, boolean isChecked) {
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
    public List<PhenotypeExperimentDTO> getSelectedExpressions() {
        return selectedExpressions;
    }

    public void applyFilterElements(String figureID, ExpressionExperimentDTO filter) {
        setFigureID(figureID);
        // needed for new expression retrieval
        this.experimentFilter = filter;
        // un-check all checked expressions if any of the filters is set except the ones that are not hidden
        // by the filter
        if (StringUtils.isNotEmpty(figureID) || filter.getGene() != null
                || StringUtils.isNotEmpty(filter.getFishID())) {
            for (PhenotypeExperimentDTO expression : selectedExpressions) {
                if (StringUtils.isNotEmpty(figureID) && !expression.getFigure().getZdbID().equals(figureID)) {
                    uncheckExpressionRecord(expression);
                }
            }
        }
        selectedExpressions.clear();
        retrieveExpressions();
        retrieveEaps();
    }

    private void uncheckExpressionRecord(PhenotypeExperimentDTO expression) {
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
                selectedExpressions = new ArrayList<>(15);
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

        private PhenotypeExperimentDTO checkedExpression;
        private CheckBox checkbox;

        private MutantSelectClickHandler(PhenotypeExperimentDTO checkedExpression, CheckBox checkbox) {
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
            WidgetUtil.selectListBox(fishList, checkedExpression.getFish().getZdbID());
            //Window.alert("Selected Expressions: "+selectedExpressions.size());
            sendFigureAnnotationsToStructureSection();
            displayTable.showHideClearAllLink();
        }

    }

    private PhenotypeExperimentDTO createBasicPhenoFigDto(String figID, String fishID, String environmentID) {
        PhenotypeExperimentDTO newMutantFigureStage = new PhenotypeExperimentDTO();
        ExperimentDTO envDto = new ExperimentDTO();
        envDto.setZdbID(environmentID);
        newMutantFigureStage.setEnvironment(envDto);
        FigureDTO figDto = new FigureDTO();
        figDto.setZdbID(figID);
        newMutantFigureStage.setFigure(figDto);
        FishDTO fishDto = new FishDTO();
        fishDto.setZdbID(fishID);
        newMutantFigureStage.setFish(fishDto);
        return newMutantFigureStage;
    }

    private void addStageToPhenoFigStageDto(String startStageID, String endStageID, PhenotypeExperimentDTO phenotypeFigureStageDTO) {
        //Window.alert("Experiment size: " + experiments);

        StageDTO start = new StageDTO();
        start.setZdbID(startStageID);
        phenotypeFigureStageDTO.setStart(start);
        StageDTO end = new StageDTO();
        end.setZdbID(endStageID);
        phenotypeFigureStageDTO.setEnd(end);

    }

    private class DeleteFigureAnnotationClickHandler implements ClickHandler {

        private PhenotypeExperimentDTO expressionFigureStage;

        public DeleteFigureAnnotationClickHandler(PhenotypeExperimentDTO expressionFigureStage) {
            this.expressionFigureStage = expressionFigureStage;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            String message = "Are you sure you want to delete this figure annotation?";
            if (!Window.confirm(message))
                return;
            deleteExperiment(expressionFigureStage);
        }

        private void deleteExperiment(PhenotypeExperimentDTO figureAnnotation) {
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

        private PhenotypeExperimentDTO figureAnnotation;

        DeleteFigureAnnotationCallback(PhenotypeExperimentDTO figureAnnotation) {
            super("Error while deleting Figure Annotation", errorElement);
            this.figureAnnotation = figureAnnotation;
        }

        @Override
        public void onSuccess(Void exp) {
            super.onSuccess(exp);
            //Window.alert("Success removing");
            // remove from the dashboard list
            displayedExpressions.remove(figureAnnotation);
            selectedExpressions.remove(figureAnnotation);
            // re-create the display table
            displayTable.createMutantTable();
            // update expression list in structure section.
            sendFigureAnnotationsToStructureSection();
            clearErrorMessages();
            postUpdateStructuresOnExpression();
            AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.REMOVE_PHENTOTYPE_EXPERIMENT));
        }

    }

    private class RetrieveExpressionsCallback extends ZfinAsyncCallback<List<PhenotypeExperimentDTO>> {

        public RetrieveExpressionsCallback() {
            super("Error while reading Experiment Filters", errorElement, IMAGE_LOADING_EXPRESSION_SECTION);
        }

        @Override
        public void onSuccess(List<PhenotypeExperimentDTO> list) {
            super.onSuccess(list);
            displayedExpressions.clear();
            if (list == null)
                return;

            for (PhenotypeExperimentDTO id : list) {
                displayedExpressions.add(id);
            }
            Collections.sort(displayedExpressions);
            if (sectionVisible)
                displayTable.createMutantTable();
            recordAllExpressedTerms();
            // phenotypeCurationRPCAsync.getFigureAnnotationCheckmarkStatus(publicationID, new FigureAnnotationCheckmarkStatusCallback());
        }

    }

    private class RetrieveEapsCallback extends ZfinAsyncCallback<List<ExpressionPhenotypeExperimentDTO>> {

        public RetrieveEapsCallback() {
            super("Error while reading Experiment Filters", errorElement, IMAGE_LOADING_EXPRESSION_SECTION);
        }

        @Override
        public void onSuccess(List<ExpressionPhenotypeExperimentDTO> list) {
            super.onSuccess(list);
            displayedEaps.clear();
            if (list == null)
                return;

            for (ExpressionPhenotypeExperimentDTO id : list) {
                displayedEaps.add(id);
            }
            Collections.sort(displayedEaps);
            createEapTable();
        }

    }

    /**
     * Show or hide expression section
     */
    private class ShowHideEapSectionHandler implements ClickHandler {

        public void onClick(ClickEvent event) {
            String errorMessage = "Error while trying to save expression visibility";
            if (sectionVisible) {
                // hide experiments
                eapTable.setVisible(false);
                showEapSection.setText(SHOW);
                sectionVisible = false;
            } else {
                // display experiments
                // check if it already exists
                if (eapTable != null && eapTable.getRowCount() > 0) {
                    eapTable.setVisible(true);
                } else {
                    retrieveEaps();
                }
                showEapSection.setText(HIDE);
                sectionVisible = true;
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
            List<PhenotypeExperimentDTO> newFigureAnnotations = getMutantFigureStageFromConstructionZone();
            for (PhenotypeExperimentDTO mutant : newFigureAnnotations) {
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
            phenotypeCurationRPCAsync.createPhenotypeExperiments(newFigureAnnotations, new AddMutantCallback(newFigureAnnotations));
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
        private List<PhenotypeExperimentDTO> getMutantFigureStageFromConstructionZone() {
            String figID = figureList.getValue(figureList.getSelectedIndex());
            String startStageID = stageSelector.getSelectedStartStageID();
            String endStageID = stageSelector.getSelectedEndStageID();
            String fishID = fishList.getValue(fishList.getSelectedIndex());
            String environmentID = environmentList.getValue(environmentList.getSelectedIndex());

            List<PhenotypeExperimentDTO> phenotypeFigureStageDTOList = new ArrayList<>(4);
            if (stageSelector.isDualStageMode()) {
                PhenotypeExperimentDTO dto = createBasicPhenoFigDto(figID, fishID, environmentID);
                phenotypeFigureStageDTOList.add(dto);
                addStageToPhenoFigStageDto(startStageID, endStageID, dto);
                dto.setPublicationID(publicationID);
            } else {
                List<String> stageIDs = stageSelector.getSelectedStageIDs();
                for (String stageID : stageIDs) {
                    PhenotypeExperimentDTO dto = createBasicPhenoFigDto(figID, fishID, environmentID);
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
         * false if experiment is different from all other experiments
         */
        private boolean isMutantFigureStageOnPile(PhenotypeExperimentDTO mutant) {
            int rowIndex = 1;
            for (PhenotypeExperimentDTO existingMutant : displayedExpressions) {
                if (existingMutant.getId() == mutant.getId()) {
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
    private boolean isValidExperiment(PhenotypeExperimentDTO mutant) {
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
        if (mutant.getFish() == null || StringUtils.isEmpty(mutant.getFish().getZdbID())) {
            errorElement.setError("No fish is selected!");
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


    private void createEapTable() {
        for (; eapTable.getRowCount() > 0; ) {
            eapTable.removeRow(0);
        }

        eapTable.clear();

        int eapRowIndex = 1;
        //Window.alert("Experiment List Size: " + experiments.size());
        ExpressionPhenotypeExperimentDTO previousEapExpression = null;
        // first element is an odd group element

        List<ExpressionPhenotypeExperimentDTO> eapexpressionFigureStageDTOs;
        if (showSelectedMutantsOnly) {
            eapexpressionFigureStageDTOs = new ArrayList<>(20);
        } else {
            eapexpressionFigureStageDTOs = displayedEaps;
        }

        for (ExpressionPhenotypeExperimentDTO eapexpression : eapexpressionFigureStageDTOs) {

            // row index minus the header row

            Label figure = new Label(eapexpression.getFigure().getLabel());
            figure.setTitle(eapexpression.getFigure().getZdbID());
            eapTable.setWidget(eapRowIndex, HeaderName.FIGURE.getIndex(), figure);
            Label fish = new Label(eapexpression.getFish().getHandle());
            fish.setTitle(eapexpression.getFish().getZdbID());
            eapTable.setWidget(eapRowIndex, HeaderName.FISH.getIndex(), fish);
            Widget environment = new Label(eapexpression.getExperiment().getName());
            environment.setTitle(eapexpression.getExperiment().getZdbID());
            eapTable.setWidget(eapRowIndex, HeaderName.ENVIRONMENT.getIndex(), environment);
            eapTable.setText(eapRowIndex, HeaderName.STAGE_RANGE.getIndex(), eapexpression.getStageRange());
            Widget eapTerms = createEapList(eapexpression);
            eapTable.setWidget(eapRowIndex, HeaderName.EXPRESSED_IN.getIndex(), eapTerms);

            eapRowIndex++;
        }

    }

    private Widget createEapList(ExpressionPhenotypeExperimentDTO mutants) {
        // create phenotype list
        VerticalPanel eapPhenotypePanel = new VerticalPanel();
        eapPhenotypePanel.setStyleName("phenotype-table");
        List<ExpressionPhenotypeStatementDTO> terms = mutants.getExpressedTerms();
        if (terms == null || terms.isEmpty()) {
            Label label = new Label("Phenotype unspecified.");
            label.setStyleName("term-unspecified");
            eapPhenotypePanel.add(label);
            return eapPhenotypePanel;
        }
        for (ExpressionPhenotypeStatementDTO eap : terms) {
            HTML eapPhenotype = new HTML(eap.getDisplayName());
            eapPhenotype.setTitle(eap.getId() + "");
            eapPhenotypePanel.add(eapPhenotype);
        }
        return eapPhenotypePanel;
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
            PhenotypeExperimentDTO previousExpression = null;
            // first element is an odd group element
            int groupIndex = 1;

            List<PhenotypeExperimentDTO> expressionFigureStageDTOs;
            if (showSelectedMutantsOnly) {
                expressionFigureStageDTOs = new ArrayList<>(20);
                expressionFigureStageDTOs.addAll(selectedExpressions);
            } else {
                expressionFigureStageDTOs = displayedExpressions;
            }

            for (PhenotypeExperimentDTO expression : expressionFigureStageDTOs) {

                // row index minus the header row
                displayTableMap.put(rowIndex, expression);
                CheckBox checkbox = new CheckBox();
                checkbox.setTitle(Long.toString(expression.getId()));
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
                Label fish = new Label(expression.getFish().getHandle());
                fish.setTitle(expression.getFish().getZdbID());
                setWidget(rowIndex, HeaderName.FISH.getIndex(), fish);
                Widget environment = new Label(expression.getEnvironment().getName());
                environment.setTitle(expression.getEnvironment().getZdbID());
                setWidget(rowIndex, HeaderName.ENVIRONMENT.getIndex(), environment);
                setText(rowIndex, HeaderName.STAGE_RANGE.getIndex(), expression.getStageRange());

                Widget terms = createTermList(expression);
                setWidget(rowIndex, HeaderName.EXPRESSED_IN.getIndex(), terms);

                Button delete = new Button("X");
                delete.setTitle(Long.toString(expression.getId()));
                delete.addClickHandler(new DeleteFigureAnnotationClickHandler(expression));
                setWidget(rowIndex, HeaderName.DELETE.getIndex(), delete);

                long previousID = 0;
                if (previousExpression != null)
                    previousID = previousExpression.getId();

                groupIndex = setRowStyle(rowIndex, Long.toString(expression.getId()), Long.toString(previousID), groupIndex);
                rowIndex++;
                previousExpression = expression;
            }
            createBottomClearAllLinkRow(rowIndex);
            showHideClearAllLink();
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


        private Widget createTermList(PhenotypeExperimentDTO mutants) {
            // create phenotype list
            VerticalPanel phenotypePanel = new VerticalPanel();
            phenotypePanel.setStyleName("phenotype-table");
            List<PhenotypeStatementDTO> terms = mutants.getExpressedTerms();
            if (terms == null || terms.isEmpty()) {
                Label label = new Label("Phenotype unspecified.");
                label.setStyleName("term-unspecified");
                phenotypePanel.add(label);
                return phenotypePanel;
            }
            for (PhenotypeStatementDTO pheno : terms) {
                StringBuilder text = new StringBuilder(50);
                String classSpan;
                if (pheno.getTag().equals("normal")) {
                    classSpan = createSpanElement(pheno, WidgetUtil.PHENOTYPE_NORMAL);
                } else if (pheno.getTag().equals("ameliorated")) {
                    classSpan = createSpanElement(pheno, WidgetUtil.PHENOTYPE_AMELIORATED);
                } else if (pheno.getTag().equals("exacerbated")) {
                    classSpan = createSpanElement(pheno, WidgetUtil.PHENOTYPE_EXACERBATED);
                } else if (pheno.getEntity().getSuperTerm().getTermName().equals("unspecified")) {
                    classSpan = createSpanElement(pheno, "term-unspecified");
                } else {
                    classSpan = createSpanElement(pheno, null);
                }
                text.append(classSpan);
                HTML phenotype = new HTML(text.toString());
                phenotype.setTitle(pheno.getId() + "");
                phenotypePanel.add(phenotype);
            }
            return phenotypePanel;
        }

        /**
         * Un-check all checked records.
         * Save the checkbox status in session.
         */
        @Override
        public void uncheckAllRecords() {
            super.uncheckAllRecords();
            uncheckAllCheckStatusInSession();
        }

        private void uncheckAllCheckStatusInSession() {
            for (PhenotypeExperimentDTO expression : selectedExpressions) {
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

    private class AddMutantCallback extends ZfinAsyncCallback<List<PhenotypeExperimentDTO>> {

        private List<PhenotypeExperimentDTO> figureAnnotations;

        public AddMutantCallback(List<PhenotypeExperimentDTO> experiment) {
            super("Error while creating experiment", errorElement);
            this.figureAnnotations = experiment;
        }

        @Override
        public void onSuccess(List<PhenotypeExperimentDTO> newAnnotations) {
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

    public void setExperimentFilter(ExpressionExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
    }

    private void updateFigureListBox() {
        figureList.clear();
        for (FigureDTO figureDTO : allFigureDtos) {
            if (figureID == null || figureDTO.getZdbID().equals(figureID))
                figureList.addItem(figureDTO.getLabel(), figureDTO.getZdbID());
        }
    }

    public class RetrieveFiguresCallback extends RetrieveSelectionBoxValueCallback {


        public RetrieveFiguresCallback() {
            super(figureList, false, errorElement);
        }

        @Override
        public void onSuccess(List<FilterSelectionBoxEntry> list) {
            super.onSuccess(list);
            allFigureDtos = new ArrayList<>((List<FigureDTO>) (List<?>) list);
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

    /**
     * Remove the figure annotations that were used in a given experiment that was deleted.
     *
     * @param deletedExperiment experiment that was deleted
     */
    public void removeFigureAnnotations(ExpressionExperimentDTO deletedExperiment) {
        Collection<ExpressionFigureStageDTO> toBeDeleted = new ArrayList<>();
        for (PhenotypeExperimentDTO efs : displayedExpressions) {
/*
            ExpressionExperimentDTO expDto = efs.getExperiment();
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
        FISH(2, "Fish"),
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

