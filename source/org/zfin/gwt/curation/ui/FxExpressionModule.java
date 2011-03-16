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
 * Expression section of the FX curation page.
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
public class FxExpressionModule extends Composite implements ExpressionSection<ExpressedTermDTO, ExpressionFigureStageDTO> {

    // div-elements
    public static final String SHOW_HIDE_EXPRESSIONS = "show-hide-expressions";
    public static final String EXPRESSIONS_DISPLAY = "display-expressions";
    public static final String IMAGE_LOADING_EXPRESSION_SECTION = "image-loading-expression-section";
    public static final String EXPRESSIONS_DISPLAY_ERRORS = "display-expression-errors";

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showExpressionSection = new Hyperlink();
    private VerticalPanel displayPanel = new VerticalPanel();
    private FlexTable constructionRow = new FlexTable();
    private ExpressionFlexTable displayTable;
    private Image loadingImage = new Image();
    private ErrorHandler errorElement = new SimpleErrorElement();

    // construction zone
    private Button addButton = new Button("Add");
    private Button resetButton = new Button("Reset");
    private StageSelector stageSelector = new StageSelector();
    private ListBox figureList = new ListBox();
    // this list is being populated through the DisplayExperimentTable
    private VerticalPanel experimentSelection = new VerticalPanel();
    private boolean showSelectedExpressionOnly;

    // all annotations that are selected
    private List<ExpressionFigureStageDTO> selectedExpressions = new ArrayList<ExpressionFigureStageDTO>(5);
    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionFigureStageDTO> displayedExpressions = new ArrayList<ExpressionFigureStageDTO>(15);
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, ExpressionFigureStageDTO> displayTableMap = new HashMap<Integer, ExpressionFigureStageDTO>(15);

    // attributes for duplicate row
    private String duplicateRowOriginalStyle;
    private int duplicateRowIndex;

    private String figureID;
    // flag that indicates if the experiment section is visible or not.
    private boolean sectionVisible;
    // used to control that this module is loaded only once.
    private boolean initialized;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private SessionSaveServiceAsync sessionRPC = SessionSaveService.App.getInstance();
    public static final String HIDE = "hide";
    public static final String SHOW = "show";
    public static final String PUSH_TO_PATO = "to pato";
    public static final String IN_PATO = "in pato";
    public static final String PUSHED = "pushed";

    // 20 expressed terms is a bit higher than the average
    // number of expressed terms used in a single publication.
    // Contains all distinct expressed terms for this publication 
    private Set<ExpressedTermDTO> expressedTerms = new HashSet<ExpressedTermDTO>(20);
    // used for highlighting structures
    private ExpressedTermDTO expressedStructure;
    private boolean markStructures;
    // Typical number of figures used per publication is less than 5.
    private List<FigureDTO> allFigureDtos = new ArrayList<FigureDTO>(5);

    // injected variables
    private ExperimentSection experimentSection;
    private StructurePile structurePile;

    // Publication in question.
    private String publicationID;
    // filter set by the banana bar
    private ExperimentDTO experimentFilter = new ExperimentDTO();

    public FxExpressionModule(ExperimentSection experimentSection, String publicationID) {
        this.experimentSection = experimentSection;
        this.publicationID = publicationID;
        stageSelector.setPublicationID(publicationID);
        displayTable = new ExpressionFlexTable(HeaderName.getHeaderNames());
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
            loadSectionVisibility();
            initialized = true;
        }
    }

    private void initGUI() {
        initShowHideGUI();
        experimentSelection.setVisible(false);
        displayPanel.setWidth("100%");
        displayPanel.add(constructionRow);
        displayPanel.add(new HTML("&nbsp;"));
        displayPanel.add(displayTable);
        RootPanel.get(EXPRESSIONS_DISPLAY).add(displayPanel);
    }

    private void initShowHideGUI() {
        RootPanel.get(SHOW_HIDE_EXPRESSIONS).add(panel);
        Label experimentLabel = new Label("Expression: ");
        experimentLabel.setStyleName("bold");
        panel.add(experimentLabel);
        showExpressionSection.setStyleName("small");
        showExpressionSection.setText(SHOW);
        showExpressionSection.setTargetHistoryToken(SHOW);
        showExpressionSection.addClickHandler(new ShowHideExpressionSectionHandler());
        panel.add(showExpressionSection);
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationRPCAsync.readExpressionSectionVisibility(publicationID, new SectionVisibilityCallback(message));
    }

    private void setInitialValues() {
        retrieveExpressions();
        retrieveConstructionZoneValues();
    }

    private void retrieveConstructionZoneValues() {
        // figure list
        curationRPCAsync.getFigures(publicationID, new RetrieveFiguresCallback());

        // stage list
        curationRPCAsync.getStages(new RetrieveStageListCallback());

        // stage selector
        sessionRPC.isStageSelectorSingleMode(publicationID, new RetrieveStageSelectorCallback(errorElement, stageSelector));
    }

    // Retrieve experiments from the server

    public void retrieveExpressions() {
        loadingImage.setVisible(true);
        curationRPCAsync.getExpressionsByFilter(experimentFilter, figureID, new RetrieveExpressionsCallback());
    }


    private void recordAllExpressedTerms() {
        expressedTerms.clear();
        for (ExpressionFigureStageDTO expression : this.displayedExpressions) {
            expressedTerms.addAll(expression.getExpressedTerms());
        }
    }

    private Widget createTermList(ExpressionFigureStageDTO expression) {
        // create comma-delimited list
        List<ExpressedTermDTO> terms = expression.getExpressedTerms();
        StringBuilder text = new StringBuilder();
        int index = 1;
        for (ExpressedTermDTO term : terms) {
            String classSpan;
            if (!term.isExpressionFound()) {
                classSpan = createSpanElement(term, WidgetUtil.RED);
            } else if (term.getSuperterm().getName().equals("unspecified")) {
                classSpan = createSpanElement(term, "term-unspecified");
            } else {
                classSpan = createSpanElement(term, null);
            }
            text.append(classSpan);
            if (index < terms.size())
                text.append(", ");
            index++;
        }
        HTML html = new HTML();
        html.setHTML(text.toString());
        return html;
    }

    private String createSpanElement(ExpressedTermDTO term, String classNamePrefix) {
        StringBuilder classSpan = new StringBuilder();
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

    private void createConstructionZone() {
        addButton.addClickHandler(new AddExpressionClickListener());
        constructionRow.setWidget(0, 2, stageSelector.getPanelTitle());
        constructionRow.setWidget(1, 0, addButton);
        constructionRow.setWidget(1, 1, figureList);
        HorizontalPanel pan = new HorizontalPanel();
        pan.add(stageSelector.getStartStagePanel());
        pan.add(stageSelector.getMultiStagePanel());
        constructionRow.setWidget(1, 2, pan);
        resetButton.addClickHandler(new ResetExpressionConstructionClickListener());
        constructionRow.setWidget(1, 3, resetButton);
        constructionRow.setWidget(2, 2, stageSelector.getEndStagePanel());
        constructionRow.setWidget(3, 2, stageSelector.getTogglePanel());
//        constructionRow.setWidget(4, 0, errorMessage);
        constructionRow.getFlexCellFormatter().setColSpan(4, 0, 4);

    }

    public Set<ExpressedTermDTO> getExpressedTermDTOs() {
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
        displayTable.createExpressionTable();
        markStructures = false;
    }

    /**
     * Update the given figure annotation with new expressed terms.
     * This is called from the structure section.
     *
     * @param updatedFigureAnnotations ExpressionFigureStageDTO
     */
    public void updateFigureAnnotations(List<ExpressionFigureStageDTO> updatedFigureAnnotations) {

        for (ExpressionFigureStageDTO updatedEfs : updatedFigureAnnotations) {
            for (ExpressionFigureStageDTO efs : displayedExpressions) {
                if (efs.getUniqueID().equals(updatedEfs.getUniqueID()))
                    efs.setExpressedTerms(updatedEfs.getExpressedTerms());
            }
        }
        displayTable.createExpressionTable();
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
        showSelectedExpressionOnly = false;
        displayTable.uncheckAllRecords();
        retrieveExpressions();
        experimentSection.unselectAllExperiments();
    }

    /**
     * This method updates the structure pile with the checked figure annotation.
     */
    protected void sendFigureAnnotationsToStructureSection() {
        structurePile.updateFigureAnnotations(selectedExpressions);
    }

    private void saveCheckStatusInSession(ExpressionFigureStageDTO checkedExpression, boolean isChecked) {
        String errorMessage = "Error while saving expression check mark status.";
        curationRPCAsync.setFigureAnnotationStatus(checkedExpression, isChecked,
                new VoidAsyncCallback(errorMessage, errorElement, IMAGE_LOADING_EXPRESSION_SECTION));
    }

    private void unceckAllCheckStatusInSession() {
        for (ExpressionFigureStageDTO expression : selectedExpressions) {
            saveCheckStatusInSession(expression, false);
        }
        selectedExpressions.clear();
        sendFigureAnnotationsToStructureSection();
    }


    public void showExpression(boolean showSelectedExpressionOnly) {
        //Window.alert("HIO");
        this.showSelectedExpressionOnly = showSelectedExpressionOnly;
        displayTable.createExpressionTable();
    }

    private Set<ExperimentDTO> getSelectedExperiments() {
        return experimentSection.getSelectedExperiment();
    }

    /**
     * Retrieve the list of expression records that are selected.
     *
     * @return list of expression figure stage info
     */
    public List<ExpressionFigureStageDTO> getSelectedExpressions() {
        return selectedExpressions;
    }

    public void applyFilterElements(String figureID, ExperimentDTO experimentFilter) {
        setFigureID(figureID);
        // needed for new expression retrieval
        this.experimentFilter = experimentFilter;
        // un-check all checked expressions if any of the filters is set except the ones that are not hidden
        // by the filter
        if (StringUtils.isNotEmpty(figureID) || experimentFilter.getGene() != null
                || StringUtils.isNotEmpty(experimentFilter.getFishID())) {
            for (ExpressionFigureStageDTO expression : selectedExpressions) {
                if (StringUtils.isNotEmpty(figureID) && !expression.getFigure().getZdbID().equals(figureID)) {
                    uncheckExpressionRecord(expression);
                }
                if (experimentFilter.getGene() != null &&
                        !expression.getExperiment().getGene().getZdbID().equals(experimentFilter.getGene().getZdbID())) {
                    uncheckExpressionRecord(expression);
                }
                if (StringUtils.isNotEmpty(experimentFilter.getFishID()) &&
                        !expression.getExperiment().getFishID().equals(experimentFilter.getFishID())) {
                    uncheckExpressionRecord(expression);
                }
            }
        }
        selectedExpressions.clear();
        retrieveExpressions();
    }

    private void uncheckExpressionRecord(ExpressionFigureStageDTO expression) {
        saveCheckStatusInSession(expression, false);
        displayTable.showClearAllLink();
        displayTable.showHideClearAllLink();

    }

    // ****************** Handlers, Callbacks, etc.

    /**
     * This Click Listener is activated upon clicking the selection check box in the
     * Expression display section. It should do two things:
     * 1) copy the values for the experiment / figure / stage range into the construction zone
     * 2) Highlight the selected expression record.
     * 3) Save the check mark status (checked or unchecked) in session.
     * 4) Copy the figure annotation into the structure section.
     */
    private class ExpressionSelectClickHandler implements ClickHandler {

        private ExpressionFigureStageDTO checkedExpression;
        private CheckBox checkbox;

        private ExpressionSelectClickHandler(ExpressionFigureStageDTO checkedExpression, CheckBox checkbox) {
            this.checkedExpression = checkedExpression;
            this.checkbox = checkbox;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            clearErrorMessages();
            // store selected experiment for update purposes
            selectUnselectFigure();
            stageSelector.selectStartStage(checkedExpression.getStart().getZdbID());
            stageSelector.selectEndStage(checkedExpression.getEnd().getZdbID());
            saveCheckStatusInSession(checkedExpression, checkbox.getValue());
            if (checkbox.getValue()) {
                selectedExpressions.add(checkedExpression);
                experimentSection.setSingleExperiment(checkedExpression.getExperiment());
            } else {
                selectedExpressions.remove(checkedExpression);
                experimentSection.unselectAllExperiments();
            }
            //Window.alert("Selected Expressions: "+selectedExpressions.size());
            sendFigureAnnotationsToStructureSection();
            displayTable.showHideClearAllLink();
        }

        // select or un-select (checkbox) the figure in the figure list

        private void selectUnselectFigure() {
            String figureID = checkedExpression.getFigure().getZdbID();
            int totalFigs = figureList.getItemCount();
            for (int index = 0; index < totalFigs; index++) {
                String value = figureList.getValue(index);
                if (value.equals(figureID))
                    figureList.setSelectedIndex(index);
            }
        }

    }

    /**
     * Create a list of Expression records that should be created as selected in the
     * construction zone. Note, there could be more than one experiment selected!
     *
     * @return list of Expression Figure Stage objects.
     */
    private List<ExpressionFigureStageDTO> getExpressionsFromConstructionZone() {
        String figureID = figureList.getValue(figureList.getSelectedIndex());
        String startStageID = stageSelector.getSelectedStartStageID();
        String endStageID = stageSelector.getSelectedEndStageID();

        List<ExpressionFigureStageDTO> expressions = new ArrayList<ExpressionFigureStageDTO>();
        if (stageSelector.isDualStageMode()) {
            addFigureAnnotationsToList(figureID, startStageID, endStageID, expressions);
        } else {
            List<String> stageIDs = stageSelector.getSelectedStageIDs();
            for (String stageID : stageIDs) {
                addFigureAnnotationsToList(figureID, stageID, stageID, expressions);
            }
        }
        return expressions;
    }

    private void addFigureAnnotationsToList(String figureID, String startStageID, String endStageID, List<ExpressionFigureStageDTO> expressions) {
        //Window.alert("Experiment size: " + experiments);

        for (ExperimentDTO experiment : getSelectedExperiments()) {
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

    private class DeleteFigureAnnotationClickHandler implements ClickHandler {

        private ExpressionFigureStageDTO expressionFigureStage;

        public DeleteFigureAnnotationClickHandler(ExpressionFigureStageDTO expressionFigureStage) {
            this.expressionFigureStage = expressionFigureStage;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            String message = "Are you sure you want to delete this figure annotation?";
            if (!Window.confirm(message))
                return;
            deleteExperiment(expressionFigureStage);
        }

    }

    private void deleteExperiment(ExpressionFigureStageDTO figureAnnotation) {
        curationRPCAsync.deleteFigureAnnotation(figureAnnotation, new DeleteFigureAnnotationCallback(figureAnnotation));
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

        private ExpressionFigureStageDTO figureAnnotation;

        DeleteFigureAnnotationCallback(ExpressionFigureStageDTO figureAnnotation) {
            super("Error while deleting Figure Annotation", errorElement);
            this.figureAnnotation = figureAnnotation;
        }

        public void onSuccess(Void exp) {
            //Window.alert("Success");
            // remove from the dashboard list
            displayedExpressions.remove(figureAnnotation);
            selectedExpressions.remove(figureAnnotation);
            // re-create the display table
            displayTable.createExpressionTable();
            // update expression list in structure section.
            sendFigureAnnotationsToStructureSection();
            experimentSection.notifyRemovedExpression(figureAnnotation.getExperiment());
            clearErrorMessages();
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class CreatePatoRecordCallback extends ZfinAsyncCallback<Void> {

        private int row;

        CreatePatoRecordCallback(int row) {
            super("Error while deleting Figure Annotation", errorElement);
            this.row = row;
        }

        public void onSuccess(Void exp) {
            //Window.alert("Success");
            Label pushed = new Label(PUSHED);
            displayTable.setWidget(row, HeaderName.PUSH.getIndex(), pushed);
            clearErrorMessages();
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class RetrieveExpressionsCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        public RetrieveExpressionsCallback() {
            super("Error while reading Experiment Filters", errorElement);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> list) {

            displayedExpressions.clear();
            if (list == null)
                return;

            for (ExpressionFigureStageDTO id : list) {
                ExperimentDTO experiment = id.getExperiment();
                if (experiment.getEnvironment().getName().startsWith("_"))
                    experiment.getEnvironment().setName(experiment.getEnvironment().getName().substring(1));
                displayedExpressions.add(id);
            }
            //Window.alert("SIZE: " + experiments.size());
            if (sectionVisible)
                displayTable.createExpressionTable();
            recordAllExpressedTerms();
            curationRPCAsync.getFigureAnnotationCheckmarkStatus(publicationID, new FigureAnnotationCheckmarkStatusCallback());
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
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
                displayPanel.setVisible(false);
                showExpressionSection.setText(SHOW);
                sectionVisible = false;
                curationRPCAsync.setExpressionVisibilitySession(publicationID, false,
                        new VoidAsyncCallback(errorMessage, errorElement, IMAGE_LOADING_EXPRESSION_SECTION));
            } else {
                // display experiments
                // check if it already exists
                if (displayTable != null && displayTable.getRowCount() > 0) {
                    displayPanel.setVisible(true);
                } else {
                    retrieveExpressions();
                    if (displayTable.getRowCount() == 0)
                        retrieveConstructionZoneValues();
                }
                showExpressionSection.setText(HIDE);
                sectionVisible = true;
                curationRPCAsync.setExpressionVisibilitySession(publicationID, true,
                        new VoidAsyncCallback(errorMessage, errorElement, IMAGE_LOADING_EXPRESSION_SECTION));
            }
            clearErrorMessages();
        }

    }

    /**
     * Push the expression record to Pato, i.e. create a pato expression record.
     * Change the text from 'to pato' to 'in pato'.
     */
    private class CreatePatoHandler implements ClickHandler {

        private int row;
        private ExpressionFigureStageDTO efs;

        private CreatePatoHandler(int row, ExpressionFigureStageDTO efs) {
            this.row = row;
            this.efs = efs;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            curationRPCAsync.createPatoRecord(efs, new CreatePatoRecordCallback(row));
        }

    }

    // avoid double updates
    private boolean addButtonInProgress;

    private class AddExpressionClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            // do not proceed if it just has been clicked once
            // and is being worked on
            if (addButtonInProgress) {
                addButton.setEnabled(false);
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
            loadingImage.setVisible(true);
            curationRPCAsync.createFigureAnnotations(newFigureAnnotations, new AddExpressionCallback(newFigureAnnotations));
        }

        private void cleanupOnExit() {
            addButton.setEnabled(true);
            addButtonInProgress = false;
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
     * Check if the expression already exists in the list.
     * Expressions have to be unique.
     *
     * @param expression expression figure stage DTO
     * @return true if experiment is found in the full list (new experiment) or in the list except itself
     *         false if experiment is different from all other experiments
     */
    public boolean expressionFigureStageExists(ExpressionFigureStageDTO expression) {
        int rowIndex = 1;
        for (ExpressionFigureStageDTO existingExpression : displayedExpressions) {
            if (existingExpression.getUniqueID().equals(expression.getUniqueID())) {
                duplicateRowIndex = rowIndex;
                duplicateRowOriginalStyle = displayTable.getRowFormatter().getStyleName(rowIndex);
                displayTable.getRowFormatter().setStyleName(rowIndex, "experiment-duplicate");
                return true;
            }
            rowIndex++;
        }
        return false;
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
            errorElement.setError("No Experiment is selected!");
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
     * un-mark duplicate experiments
     */
    public void clearErrorMessages() {
        errorElement.clearAllErrors();
        if (duplicateRowIndex > 0)
            displayTable.getRowFormatter().setStyleName(duplicateRowIndex, duplicateRowOriginalStyle);
        experimentSection.clearErrorMessages();
    }

    class ExpressionFlexTable extends ZfinFlexTable {

        private HeaderName[] headerNames;

        ExpressionFlexTable(HeaderName[] headerNames) {
            super(headerNames.length, HeaderName.SELECT.index);
            this.headerNames = headerNames;
            setToggleHyperlink(ToggleLink.SHOW_SELECTED_EXPRESSIONS_ONLY.getText(), ToggleLink.SHOW_ALL_EXPRESSIONS.getText());
            addToggleHyperlinkClickHandler(new ShowSelectedExpressionClickHandler(showSelectedRecords));
        }

        protected void createExpressionTable() {
            clearTable();
            createConstructionZone();
            // header row index = 0
            createTableHeader();
            int rowIndex = 1;
            //Window.alert("Experiment List Size: " + experiments.size());
            ExpressionFigureStageDTO previousExpression = null;
            // first element is an odd group element
            int groupIndex = 1;

            List<ExpressionFigureStageDTO> expressionFigureStageDTOs;
            if (showSelectedExpressionOnly) {
                expressionFigureStageDTOs = new ArrayList<ExpressionFigureStageDTO>();
                expressionFigureStageDTOs.addAll(selectedExpressions);
            } else {
                expressionFigureStageDTOs = displayedExpressions;
            }

            for (ExpressionFigureStageDTO expression : expressionFigureStageDTOs) {

                // rowindex minus the header row
                displayTableMap.put(rowIndex, expression);
                CheckBox checkbbox = new CheckBox(null);
                checkbbox.setTitle(expression.getUniqueID());
                // if any figure annotations are already selected make sure they stay checked
                if (selectedExpressions.contains(expression)) {
                    checkbbox.setValue(true);
                    //Window.alert("Checkbox");
                    //showClearAllLink();
                }
                checkbbox.addClickHandler(new ExpressionSelectClickHandler(expression, checkbbox));
                setWidget(rowIndex, HeaderName.SELECT.getIndex(), checkbbox);

                ExperimentDTO experiment = expression.getExperiment();
                setWidgetWithNameAndIdLabel(rowIndex, HeaderName.FIGURE.getIndex(), expression.getFigure().getLabel(), expression.getFigure().getZdbID());
                setWidgetWithNameAndIdLabel(rowIndex, HeaderName.FISH.getIndex(), experiment.getFishName(), experiment.getFishID());
                MarkerDTO gene = experiment.getGene();
                if (gene != null)
                    setWidgetWithNameAndIdLabel(rowIndex, HeaderName.GENE.getIndex(), experiment.getGene().getName(), gene.getZdbID());
                setWidgetWithNameAndIdLabel(rowIndex, HeaderName.ENVIRONMENT.getIndex(), experiment.getEnvironment().getName(), experiment.getEnvironment().getZdbID());

                String assay = experiment.getAssayAbbreviation();
                if (!StringUtils.isEmpty(experiment.getGenbankNumber()))
                    assay += " (" + experiment.getGenbankNumber() + ")";
                setText(rowIndex, HeaderName.ASSAY.getIndex(), assay);
                MarkerDTO antibody = experiment.getAntibodyMarker();
                if (antibody != null)
                    setText(rowIndex, HeaderName.ANTIBODY.getIndex(), antibody.getName());
                setText(rowIndex, HeaderName.STAGE_RANGE.getIndex(), expression.getStageRange());

                Widget terms = createTermList(expression);
                setWidget(rowIndex, HeaderName.EXPRESSED_IN.getIndex(), terms);

                Button delete = new Button("X");
                delete.setTitle(expression.getUniqueID());
                delete.addClickHandler(new DeleteFigureAnnotationClickHandler(expression));
                setWidget(rowIndex, HeaderName.DELETE.getIndex(), delete);

                if (expression.isPatoExists()) {
                    Label inPato = new Label(IN_PATO);
                    setWidget(rowIndex, HeaderName.PUSH.getIndex(), inPato);
                } else {
                    Hyperlink pushToPato = new Hyperlink(PUSH_TO_PATO, PUSH_TO_PATO);
                    pushToPato.setText(PUSH_TO_PATO);
                    pushToPato.addClickHandler(new CreatePatoHandler(rowIndex, expression));
                    setWidget(rowIndex, HeaderName.PUSH.getIndex(), pushToPato);
                }
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

        /**
         * Add a label to the composite
         *
         * @param rowIndex index
         * @param name     name of label
         * @param title    name of title
         */
        private void setWidgetWithNameAndIdLabel(int rowIndex, int columnIndex, String name, String title) {
            Widget widget = new Label(name);
            widget.setTitle(title);
            setWidget(rowIndex, columnIndex, widget);
        }

        /**
         * Uncheck all checked records.
         * Save the checkbox status in session.
         */
        public void uncheckAllRecords() {
            super.uncheckAllRecords();
            unceckAllCheckStatusInSession();
        }


        public void onClick(ClickEvent clickEvent) {
            HTMLTable.Cell cell = getCellForEvent(clickEvent);
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

        protected void createTableHeader() {
            super.createTableHeader();
            for (HeaderName name : headerNames) {
                if (name.index != 0)
                    setText(0, name.index, name.getName());
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

    private class AddExpressionCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        private List<ExpressionFigureStageDTO> figureAnnotations;

        public AddExpressionCallback(List<ExpressionFigureStageDTO> experiment) {
            super("Error while creating experiment", errorElement);
            this.figureAnnotations = experiment;
        }

        public void onSuccess(List<ExpressionFigureStageDTO> newAnnotations) {
            displayedExpressions.addAll(newAnnotations);
            Collections.sort(displayedExpressions);
            displayTable.createExpressionTable();
            recordAllExpressedTerms();
            addButtonInProgress = false;
            addButton.setEnabled(true);
            loadingImage.setVisible(false);
            clearErrorMessages();
            stageSelector.resetGui();
            experimentSection.notifyAddedExpression();
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(false);
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

        public void onSuccess(List<FigureDTO> list) {

            allFigureDtos = new ArrayList<FigureDTO>(list);
            updateFigureListBox();
            //Window.alert("SIZE: " + experiments.size());
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * Callback for reading all stages.
     */
    public class RetrieveStageListCallback extends ZfinAsyncCallback<List<StageDTO>> {

        public RetrieveStageListCallback() {
            super("Error while reading Figure Filters", errorElement);
        }

        public void onSuccess(List<StageDTO> stages) {

            //Window.alert("SIZE: " + experiments.size());
            stageSelector.setStageList(stages);
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
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
                        ExpressionFigureStageDTO checkedExpression = displayTableMap.get(row);
                        selectedExpressions.add(checkedExpression);
                    }
                }
            }
            displayTable.showHideClearAllLink();
            structurePile.updateFigureAnnotations(selectedExpressions);
        }
    }

    private class SectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public SectionVisibilityCallback(String message) {
            super(message, errorElement);
        }

        public void onSuccess(Boolean visible) {
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (displayedExpressions == null || displayedExpressions.isEmpty()) {
                setInitialValues();
            } else {
                displayTable.createExpressionTable();
                displayTable.showHideClearAllLink();
            }
            if (sectionVisible) {
                showExpressionSection.setText(HIDE);
            } else {
                showExpressionSection.setText(SHOW);
            }
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * Remove the figure annotations that were used in a given experiment that was deleted.
     *
     * @param deletedExperiment experiment that was deleted
     */
    public void removeFigureAnnotations(ExperimentDTO deletedExperiment) {
        List<ExpressionFigureStageDTO> toBeDeleted = new ArrayList<ExpressionFigureStageDTO>();
        for (ExpressionFigureStageDTO efs : displayedExpressions) {
            ExperimentDTO expDto = efs.getExperiment();
            if (expDto.getExperimentZdbID().equals(deletedExperiment.getExperimentZdbID()))
                toBeDeleted.add(efs);
        }
        for (ExpressionFigureStageDTO efs : toBeDeleted) {
            displayedExpressions.remove(efs);
        }
        displayTable.createExpressionTable();
    }

    private enum HeaderName {
        SELECT(0, ""),
        FIGURE(1, "Figure"),
        GENE(2, "Gene"),
        FISH(3, "Fish"),
        ENVIRONMENT(4, "Environment"),
        ASSAY(5, "Assay"),
        ANTIBODY(6, "Ab"),
        STAGE_RANGE(7, "Stage Range"),
        EXPRESSED_IN(8, "Expressed in"),
        DELETE(9, "Delete"),
        PUSH(10, "Push");

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
            return FxExpressionModule.HeaderName.values();
        }
    }


}