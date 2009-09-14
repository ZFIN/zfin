package org.zfin.curation.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.curation.dto.*;

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
 * B) The state of the visiblity is saved in the database and remembered for this publication for future.
 * in the curation_sesion table.
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
 * [superterm:subterm]. If the term is 'unspecified' then hightlight the term in orange. If a term is NOT expressed
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
public class FxExpressionModule extends Composite {

    // div-elements
    public static final String SHOW_HIDE_EXPRESSIONS = "show-hide-expressions";
    public static final String EXPRESSIONS_DISPLAY = "display-expressions";
    public static final String IMAGE_LOADING_EXPRESSION_SECTION = "image-loading-expression-section";
    public static final String EXPRESSIONS_DISPLAY_ERRORS = "display-expression-errors";

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showExpressionSection = new Hyperlink();
    private ZfinFlexTable displayTable;
    private Image loadingImage = new Image();
    private Label errorMessage = new Label();

    // construction zone
    private Button addButton = new Button("Add");
    private Button resetButton = new Button("Reset");
    private Button multiStageButton = new Button(SELECT_MULTIPLE_STAGES);
    private ListBox figureList = new ListBox();
    private HorizontalPanel startStage = new HorizontalPanel();
    private HorizontalPanel endStage = new HorizontalPanel();
    private VerticalPanel stageSelection = null;
    private ListBox startStageList = new ListBox();
    private ListBox endStageList = new ListBox();
    private ListBox multiStartStageList = new ListBox(true);
    // this list is being populated through the DisplayExperimentTable
    private ListBox experimentList = new ListBox(true);

    // all annotations that are selected
    private List<ExpressionFigureStageDTO> selectedExpressions = new ArrayList<ExpressionFigureStageDTO>();
    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionFigureStageDTO> displayedExpressions = new ArrayList<ExpressionFigureStageDTO>();
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, ExpressionFigureStageDTO> displayTableMap = new HashMap<Integer, ExpressionFigureStageDTO>();

    // injected by Experiment Module
    private List<ExperimentDTO> experiments;

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

    public static final String HIDE = "hide";
    public static final String SHOW = "show";
    public static final String PUSH_TO_PATO = "to pato";
    public static final String IN_PATO = "in pato";
    public static final String PUSHED = "pushed";
    private static final HTML HTML_NBSP = new HTML("&nbsp");

    private Set<ExpressedTermDTO> expressedTerms = new HashSet<ExpressedTermDTO>();
    // used for highlighting structures
    private ExpressedTermDTO expressedStructure;
    private boolean markStructures;
    private List<FigureDTO> allFigureDtos = new ArrayList<FigureDTO>();

    // injected variables
    private CurationEntryPoint curationEntryPoint;
    // Publication in question.
    private String publicationID;
    // filter set by the banana bar
    private ExperimentDTO experimentFilter = new ExperimentDTO();
    private static final String SELECT_MULTIPLE_STAGES = "Select multiple stages";
    private static final String SELECT_SINGLE_STAGE = "Select a stage range";

    public FxExpressionModule(CurationEntryPoint curationEntryPoint) {
        this.curationEntryPoint = curationEntryPoint;
        publicationID = curationEntryPoint.getPublicationID();
        displayTable = new ZfinFlexTable(HeaderName.getHeaderNames());
        initGUI();
    }

    /**
     * The data should only be loaded when the filter bar is initilized.
     * So this method is called from the FxFilterTable.
     */
    protected void runModule() {
        if (!initialized) {
            loadSectionVisibility();
            showExperimentList();
            initialized = true;
        }
    }

    private void initGUI() {
        initShowHideGUI();

        RootPanel.get(EXPRESSIONS_DISPLAY).add(displayTable);
        addChangeListeners();

        RootPanel.get(EXPRESSIONS_DISPLAY_ERRORS).add(errorMessage);
        RootPanel.get(IMAGE_LOADING_EXPRESSION_SECTION).add(loadingImage);
        errorMessage.setStyleName("error");
        loadingImage.setUrl("/images/ajax-loader.gif");
    }

    private void initShowHideGUI() {
        RootPanel.get(SHOW_HIDE_EXPRESSIONS).add(panel);
        Label experimentLabel = new Label("Expression: ");
        experimentLabel.setStyleName("bold");
        panel.add(experimentLabel);
        showExpressionSection.setStyleName("small");
        showExpressionSection.setText(SHOW);
        showExpressionSection.setTargetHistoryToken(SHOW);
        showExpressionSection.addClickListener(new ShowHideExpressionSectionListener());
        panel.add(showExpressionSection);
        multiStartStageList.setVisibleItemCount(6);
        multiStartStageList.setVisible(false);
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationRPCAsync.readExpressionSectionVisibility(publicationID, new SectionVisiblityCallback(message));
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

        // show experiments
    }

    // Retrieve experiments from the server
    protected void retrieveExpressions() {
        loadingImage.setVisible(true);
        curationRPCAsync.getExpressionsByFilter(experimentFilter, figureID, new RetrieveExpressionsCallback());
    }

    private void addChangeListeners() {
        // start stage changes
        startStageList.addChangeListener(new StartStageChangeListener());
        startStageList.addChangeListener(errorMessageCleanupListener);
    }

    private void createTableHeader() {
        displayTable.setWidth("100%");
        displayTable.setHeaderRow();
        displayTable.addStyleName("searchresults groupstripes-hover");
        displayTable.getRowFormatter().setStyleName(0, "table-header");
    }

    public void createExpressionTable() {
        clearExpressionListTable();
        // header row index = 0
        createTableHeader();
        int rowIndex = 1;
        //Window.alert("Experiment List Size: " + experiments.size());
        ExpressionFigureStageDTO previousExpression = null;
        // first element is an odd group element
        int groupIndex = 1;
        for (ExpressionFigureStageDTO expression : displayedExpressions) {
            // rowindex minus the header row
            int experimentIndex = rowIndex - 1;
            displayTableMap.put(rowIndex, expression);
            SelectExpressionCheckBox checkbbox = new SelectExpressionCheckBox(null);
            checkbbox.setTitle(expression.getUniqueID());
            // if any figure annotations are already selected make sure they stay checked
            if (selectedExpressions.contains(expression))
                checkbbox.setChecked(true);
            checkbbox.addClickListener(new ExpressionSelectClickListener(experimentIndex, true));
            displayTable.setWidget(rowIndex, HeaderName.SELECT.getIndex(), checkbbox);
            displayTable.setText(rowIndex, HeaderName.FIGURE.getIndex(), expression.getFigureLabel());
            ExperimentDTO experiment = expression.getExperiment();
            displayTable.setText(rowIndex, HeaderName.FISH.getIndex(), experiment.getFishName());
            displayTable.setText(rowIndex, HeaderName.GENE.getIndex(), experiment.getGeneName());
            displayTable.setText(rowIndex, HeaderName.ENVIRONMENT.getIndex(), experiment.getEnvironmentDisplayValue());
            String assay = experiment.getAssayAbbreviation();
            if (!StringUtils.isEmpty(experiment.getGenbankNumber()))
                assay += " (" + experiment.getGenbankNumber() + ")";
            displayTable.setText(rowIndex, HeaderName.ASSAY.getIndex(), assay);
            displayTable.setText(rowIndex, HeaderName.ANTIBODY.getIndex(), experiment.getAntibody());
            displayTable.setText(rowIndex, HeaderName.STAGE_RANGE.getIndex(), expression.getStageRange());

            Widget terms = createTermList(expression);
            displayTable.setWidget(rowIndex, HeaderName.EXPRESSED_IN.getIndex(), terms);

            RemoveButton delete = new RemoveButton("X");
            delete.setTitle(expression.getUniqueID());
            delete.addClickListener(new DeleteFigureAnnotationClickListener(expression));
            displayTable.setWidget(rowIndex, HeaderName.DELETE.getIndex(), delete);

            if (expression.isPatoExists()) {
                Label inPato = new Label(IN_PATO);
                displayTable.setWidget(rowIndex, HeaderName.PUSH.getIndex(), inPato);
            } else {
                ZfinHyperlinkWithEventCancelBubble pushToPato = new ZfinHyperlinkWithEventCancelBubble(PUSH_TO_PATO, "anotherLink");
                pushToPato.setText(PUSH_TO_PATO);
                pushToPato.addClickListener(new CreatePatoListener(rowIndex, expression));
                displayTable.setWidget(rowIndex, HeaderName.PUSH.getIndex(), pushToPato);
            }
            groupIndex = setRowStyle(rowIndex, expression, previousExpression, groupIndex);
            rowIndex++;
            previousExpression = expression;
        }
        // add horizontal line
        displayTable.getFlexCellFormatter().setColSpan(rowIndex, 0, HeaderName.getHeaderNames().length);
        HTML html = new HTML("<hr/>");
        displayTable.setWidget(rowIndex, 0, html);

        createConstructionZone();
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
            } else if (term.getSupertermName().equals("unspecified")) {
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
        classSpan.append(term.getComposedTerm());
        if (classNamePrefix != null || markStructures)
            classSpan.append("</span>");
        return classSpan.toString();
    }

    private void clearExpressionListTable() {
        int rowCount = displayTable.getRowCount();
        // Note: make sure to remove rows in reverse order
        // otherwise you get random displays of records!
        if (rowCount < 2)
            return;

        for (int i = rowCount - 1; i >= 0; i--) {
            displayTable.removeRow(i);
        }
    }

    private void createConstructionZone() {
        int rowIndex = displayTable.getRowCount() + 1;
        addButton.addClickListener(new AddExpressionClickListener());
        addButton.setEnabled(false);
        displayTable.setWidget(rowIndex, HeaderName.SELECT.getIndex(), addButton);
        displayTable.setWidget(rowIndex, HeaderName.FIGURE.getIndex(), figureList);
        displayTable.setWidget(rowIndex, HeaderName.GENE.getIndex(), experimentList);
        experimentList.addClickListener(new ExperimentClickListener());
        displayTable.getFlexCellFormatter().setColSpan(rowIndex, HeaderName.GENE.getIndex(), 5);
        // TODO: move into a separate class to make it reusable
        if (stageSelection == null) {
            stageSelection = new VerticalPanel();
            startStage.add(new Label("Start:"));
            startStage.add(HTML_NBSP);
            startStage.add(startStageList);
            endStage.add(new Label("End:"));
            endStage.add(HTML_NBSP);
            endStage.add(endStageList);
            stageSelection.add(startStage);
            stageSelection.add(endStage);
            stageSelection.add(multiStageButton);
            stageSelection.add(multiStartStageList);
            startStageList.addChangeListener(new StartStageChangeListener());
            multiStageButton.addClickListener(new MultiStageButtonClickListener());
            stageSelection.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        }
        displayTable.setWidget(rowIndex, HeaderName.GENE.getIndex() + 2, stageSelection);
        resetButton.addClickListener(new ResetExpressionConstructionClickListener());
        displayTable.setWidget(rowIndex, HeaderName.GENE.getIndex() + 4, resetButton);
    }


    // Returns the boolean: isOddGroup
    private int setRowStyle(int rowIndex, ExpressionFigureStageDTO currentExperiment, ExpressionFigureStageDTO previousExperiment, int groupIndex) {
        StringBuilder sb = new StringBuilder();
        // check even/odd row
        if (rowIndex % 2 == 0)
            sb.append("even");
        else
            sb.append("odd");

        // check if newgroup or oldgroup
        if (previousExperiment == null) {
            sb.append(" newgroup");
        } else {
            String previousGeneID = previousExperiment.getUniqueID();
            String currentGeneID = currentExperiment.getUniqueID();
            if (previousGeneID == null && currentGeneID == null)
                sb.append(" oldgroup");
            else if (previousGeneID == null) {
                sb.append(" newgroup");
                groupIndex++;
            } else if (previousGeneID.equals(currentGeneID)) {
                sb.append(" oldgroup");
            } else {
                sb.append(" newgroup");
                groupIndex++;
            }
        }

        // check if odd group or even group
        if (groupIndex % 2 == 0)
            sb.append(" evengroup");
        else
            sb.append(" oddgroup");

        // add row
        sb.append(" experiment-row ");
        displayTable.getRowFormatter().setStyleName(rowIndex, sb.toString());
        //table.getRowFormatter().getElement(rowIndex).setId(EXPERIMENT_ROW_INDEX_ID + rowIndex);
        return groupIndex;
    }

    public void setShowHideWidget(Hyperlink showHideLink) {
        showExpressionSection = showHideLink;
    }

    /**
     * Select (highlight) a given experiment.
     *
     * @param experimentID experiment
     */
    void selectExperiment(String experimentID) {
        int totalStages = experimentList.getItemCount();
        for (int index = 0; index < totalStages; index++) {
            String value = experimentList.getValue(index);
            if (value.equals(experimentID)) {
                experimentList.setSelectedIndex(index);
                addButton.setEnabled(true);
            }
        }
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
        createExpressionTable();
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
        createExpressionTable();
        recordAllExpressedTerms();
        // remove check marks from annotations.
    }

    /**
     * Retrieve the expression list again.
     * remove check marks from expressions.
     * update expressedTerms collection.
     */
    public void postUpdateStructuresOnExpression() {
        retrieveExpressions();
        selectedExpressions.clear();
        experimentList.setSelectedIndex(-1);
    }

    /**
     * This method updates the structure pile with the checked figure annotation.
     */
    protected void sendFigureAnnotationsToStructureSection() {
        FxStructureModule structureTable = curationEntryPoint.getStructureModule();
        structureTable.updateFigureAnnotations(selectedExpressions);
    }

    /**
     * This Click Listener is activated upon clicking the selection check box in the
     * Expression display section. It should do two things:
     * 1) copy the values for the experiment / figure / stage range into the construction zone
     * 2) Hightlight the selected expression record.
     * 3) Save the check mark status (checked or unchecked) in session.
     * 4) Copy the figure annotation into the structure section.
     */
    private class ExpressionSelectClickListener implements ClickListener {

        private int rowIndex;
        private ExpressionFigureStageDTO checkedExpression;
        boolean eventFromCheckBox;

        public ExpressionSelectClickListener(int rowIndex, boolean eventFromCheckBox) {
            this.rowIndex = rowIndex;
            this.eventFromCheckBox = eventFromCheckBox;
        }

        public void onClick(Widget widget) {
            clearErrorMessages();
            // store selected experiment for update purposes
            checkedExpression = displayedExpressions.get(rowIndex);
            selectUnselectFigure();
            selectCheckedExperiment();
            selectStartStage();
            selectEndStage();
            CheckBox checkBox = (CheckBox) widget;
            boolean isChecked = checkBox.isChecked();
            if (!eventFromCheckBox) {
                checkBox.setChecked(!isChecked);
                isChecked = !isChecked;
            }
            saveCheckStatusInSession(isChecked);
            if (isChecked)
                selectedExpressions.add(checkedExpression);
            else
                selectedExpressions.remove(checkedExpression);
            sendFigureAnnotationsToStructureSection();
            addButton.setEnabled(true);
        }

        private void saveCheckStatusInSession(boolean isChecked) {
            String errorMessage = "Error while saving expression check mark status.";
            curationRPCAsync.setFigureAnnotationStatus(checkedExpression, isChecked, new VoidAsyncCallback(new Label(errorMessage), null));
        }

        /**
         * Select the checked efs (experiment figure stage) in the selection box.
         */
        private void selectCheckedExperiment() {
            String experimentZdbID = checkedExpression.getExperiment().getExperimentZdbID();
            selectExperiment(experimentZdbID);
        }

        /**
         * Select the checked start stage in the selection box.
         */
        private void selectStartStage() {
            String toBeSelectedStage = checkedExpression.getStart().getZdbID();
            int totalStages = startStageList.getItemCount();
            for (int index = 0; index < totalStages; index++) {
                String value = startStageList.getValue(index);
                if (value.equals(toBeSelectedStage))
                    startStageList.setSelectedIndex(index);
            }
        }

        /**
         * Select the checked end stage in the selection box.
         */
        private void selectEndStage() {
            String toBeSelectedStage = checkedExpression.getEnd().getZdbID();
            int totalStages = endStageList.getItemCount();
            for (int index = 0; index < totalStages; index++) {
                String value = endStageList.getValue(index);
                if (value.equals(toBeSelectedStage))
                    endStageList.setSelectedIndex(index);
            }
        }

        // select or unselect (checkbox) the figure in the figure list
        private void selectUnselectFigure() {
            String figureID = checkedExpression.getFigureID();
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
        String startStageID = startStageList.getValue(startStageList.getSelectedIndex());
        String endStageID = endStageList.getValue(endStageList.getSelectedIndex());

        //multiStartStageList.get
        List<ExpressionFigureStageDTO> expressions = new ArrayList<ExpressionFigureStageDTO>();
        if (startStage.isVisible()) {
            addFigureAnnotationsToList(figureID, startStageID, endStageID, expressions);
        } else {
            int stageCount = multiStartStageList.getItemCount();
            for (int index = 0; index < stageCount; index++) {
                if (multiStartStageList.isItemSelected(index)) {
                    startStageID = multiStartStageList.getValue(index);
                    addFigureAnnotationsToList(figureID, startStageID, startStageID, expressions);
                }
            }
        }
        return expressions;
    }

    private void addFigureAnnotationsToList(String figureID, String startStageID, String endStageID, List<ExpressionFigureStageDTO> expressions) {
        int expressionCount = experimentList.getItemCount();
        for (int index = 0; index < expressionCount; index++) {
            if (experimentList.isItemSelected(index)) {
                String experimentID = experimentList.getValue(index);
                ExperimentDTO experiment = getExperimentDtoFromID(experimentID);
                ExpressionFigureStageDTO newExpression = new ExpressionFigureStageDTO();
                newExpression.setExperiment(experiment);
                StageDTO start = new StageDTO();
                start.setZdbID(startStageID);
                newExpression.setStart(start);
                StageDTO end = new StageDTO();
                end.setZdbID(endStageID);
                newExpression.setEnd(end);
                newExpression.setFigureID(figureID);
                expressions.add(newExpression);
            }
        }
    }

    private ExperimentDTO getExperimentDtoFromID(String experimentID) {
        for (ExperimentDTO experiment : experiments) {
            if (experiment.getExperimentZdbID().equals(experimentID))
                return experiment;
        }
        errorMessage.setText("No experiment found for ID: " + experimentID);
        return null;
    }

    private class DeleteFigureAnnotationClickListener implements ClickListener {

        private ExpressionFigureStageDTO expressionFigureStage;

        public DeleteFigureAnnotationClickListener(ExpressionFigureStageDTO expressionFigureStage) {
            this.expressionFigureStage = expressionFigureStage;
        }

        public void onClick(Widget widget) {
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
            super("Error while deleting Figure Annotation", errorMessage);
            this.figureAnnotation = figureAnnotation;
        }

        public void onSuccess(Void exp) {
            //Window.alert("Success");
            // remove from the dashboard list
            displayedExpressions.remove(figureAnnotation);
            selectedExpressions.remove(figureAnnotation);
            // remove from display
            int numOfRows = displayTable.getRowCount();
            // re-create the display table
            createExpressionTable();
            // update expression list in structure section.
            sendFigureAnnotationsToStructureSection();
            curationEntryPoint.getExperimentModule().notifyRemovedExpression(figureAnnotation.getExperiment());
            clearErrorMessages();
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class CreatePatoRecordCallback extends ZfinAsyncCallback<Void> {

        private int row;

        CreatePatoRecordCallback(int row) {
            super("Error while deleting Figure Annotation", errorMessage);
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
            super("Error while reading Experiment Filters", errorMessage);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> list) {

            displayedExpressions.clear();
            if (list == null)
                return;

            for (ExpressionFigureStageDTO id : list) {
                ExperimentDTO experiment = id.getExperiment();
                if (experiment.getEnvironment().startsWith("_"))
                    experiment.setEnvironment(experiment.getEnvironment().substring(1));
                displayedExpressions.add(id);
            }
            //Window.alert("SIZE: " + experiments.size());
            if (sectionVisible)
                createExpressionTable();
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
    private class ShowHideExpressionSectionListener implements ClickListener {

        public void onClick(Widget widget) {
            String errorMessage = "Error while trying to save expression visibility";
            if (sectionVisible) {
                // hide experiments
                displayTable.setVisible(false);
                showExpressionSection.setText(SHOW);
                sectionVisible = false;
                curationRPCAsync.setExpressionVisibilitySession(publicationID, false,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
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
                curationRPCAsync.setExpressionVisibilitySession(publicationID, true,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            }
            clearErrorMessages();
        }

    }

    /**
     * Push the expression record to Pato, i.e. create a pato expression record.
     * Change the text from 'to pato' to 'in pato'.
     */
    private class CreatePatoListener implements ClickListener {

        private int row;
        private ExpressionFigureStageDTO efs;

        private CreatePatoListener(int row, ExpressionFigureStageDTO efs) {
            this.row = row;
            this.efs = efs;
        }

        public void onClick(Widget widget) {
            curationRPCAsync.createPatoRecord(efs, new CreatePatoRecordCallback(row));
        }

    }

    // avoid double updates
    private boolean addButtonInProgress;

    private class AddExpressionClickListener implements ClickListener {

        public void onClick(Widget widget) {
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
                if (!validateExperiment(expression)) {
                    cleanupOnExit();
                    return;
                }
                if (expressionFigureStageExists(expression)) {
                    errorMessage.setText("Expression already exists. Expressions have to be unique!");
                    cleanupOnExit();
                    expressionsExist = true;
                }
            }
            if (!startStage.isVisible() && multiStartStageList.getSelectedIndex() < 0) {
                errorMessage.setText("No stage selected.  Please select at least one stage.");
                cleanupOnExit();
                return;
            }
            if (newFigureAnnotations.size() == 0) {
                errorMessage.setText("No experiment selected. Please select at least one experiment!");
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
    private class ResetExpressionConstructionClickListener implements ClickListener {

        public void onClick(Widget widget) {
            addButton.setEnabled(false);
            startStageList.setSelectedIndex(0);
            endStageList.setSelectedIndex(0);
            experimentList.setSelectedIndex(-1);
            figureList.setSelectedIndex(0);
            multiStartStageList.setSelectedIndex(-1);
            multiStageButton.setText(SELECT_MULTIPLE_STAGES);
            multiStartStageList.setVisible(false);
            startStage.setVisible(true);
            endStage.setVisible(true);
        }

    }

    private class ExperimentClickListener implements ClickListener {

        public void onClick(Widget widget) {
            addButton.setEnabled(true);
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
    private boolean validateExperiment(ExpressionFigureStageDTO expression) {
        if (StringUtils.isEmpty(expression.getStart().getZdbID()))
            return false;
        if (StringUtils.isEmpty(expression.getEnd().getZdbID()))
            return false;
        if (StringUtils.isEmpty(expression.getFigureID()))
            return false;
        if (expression.getExperiment() == null || StringUtils.isEmpty(expression.getExperiment().getExperimentZdbID())) {
            errorMessage.setText("No Experiment is selected!");
            return false;
        }
        // check that end stage comes after start stage
        if (startStage.isVisible()) {
            if (startStageList.getSelectedIndex() > endStageList.getSelectedIndex()) {
                errorMessage.setText("Selected Start Stage comes after selected End Stage! Please correct your choices.");
                return false;
            }
        } else {
            if (multiStartStageList.getSelectedIndex() < 0) {
                errorMessage.setText("No stage selected.  Please select at least one stage.");
                return false;
            }
        }
        return true;
    }

    /**
     * Remove error messages
     * unmark duplicate experiments
     */
    public void clearErrorMessages() {
        errorMessage.setText(null);
        if (duplicateRowIndex > 0)
            displayTable.getRowFormatter().setStyleName(duplicateRowIndex, duplicateRowOriginalStyle);
        curationEntryPoint.getExperimentModule().clearErrorMessages();
    }

    /**
     * unselect experiment selection box.
     */
    private void unselectExperiments() {
        experimentList.setSelectedIndex(-1);
    }

    class ZfinFlexTable extends FlexTable implements TableListener {

        private HeaderName[] headerNames;

        ZfinFlexTable(HeaderName[] headerNames) {
            super();
            this.headerNames = headerNames;
            addTableListener(this);
        }

        public void onCellClicked(SourcesTableEvents actor, int row, int cell) {
            //Window.alert(row + " : " + cell);
            int firstCell = 0;
            Widget widget = getWidget(row, firstCell);
            if (widget == null || !(widget instanceof CheckBox))
                return;

            CheckBox checkBox = (CheckBox) widget;
            (new ExpressionSelectClickListener(row - 1, false)).onClick(checkBox);
        }

        /**
         * Set table header row, assuming it is rowIndex = 0;
         */
        public void setHeaderRow() {
            int rowIndex = 0;
            for (HeaderName name : headerNames) {
                setText(rowIndex, name.index, name.getName());
                if (name.equals(HeaderName.EXPRESSED_IN))
                    getFlexCellFormatter().setWidth(rowIndex, name.index, "25%");
            }
        }
    }

    private class AddExpressionCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        private List<ExpressionFigureStageDTO> figureAnnotations;

        public AddExpressionCallback(List<ExpressionFigureStageDTO> experiment) {
            super("Error while creating experiment", errorMessage);
            this.figureAnnotations = experiment;
        }

        public void onSuccess(List<ExpressionFigureStageDTO> newAnnotations) {
            displayedExpressions.addAll(newAnnotations);
            Collections.sort(displayedExpressions);
            createExpressionTable();
            recordAllExpressedTerms();
            addButtonInProgress = false;
            addButton.setEnabled(true);
            loadingImage.setVisible(false);
            clearErrorMessages();
            unselectExperiments();
            curationEntryPoint.getExperimentModule().notifyAddedExpression(getExperiments());
        }

        // Extract the list of experiments
        private List<ExperimentDTO> getExperiments() {
            List<ExperimentDTO> experiments = new ArrayList<ExperimentDTO>();
            if (figureAnnotations == null)
                return experiments;
            for (ExpressionFigureStageDTO efs : figureAnnotations) {
                experiments.add(efs.getExperiment());
            }
            return experiments;
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }

    }

    private class SelectExpressionCheckBox extends CheckBox {

        private ClickListenerCollection clickListener = new ClickListenerCollection();

        public SelectExpressionCheckBox(String name) {
            super(name);
            this.sinkEvents(Event.ONCLICK);
        }

        public void onBrowserEvent(Event event) {
            switch (DOM.eventGetType(event)) {
                case Event.ONCLICK:
                    clickListener.fireClick(this);
                    DOM.eventCancelBubble(event, true);
            }
        }

        public void addClickListener(ClickListener listener) {
            clickListener.add(listener);
        }

    }

    private class RemoveButton extends Button {

        private ClickListenerCollection clickListener = new ClickListenerCollection();

        public RemoveButton(String name) {
            super(name);
            this.sinkEvents(Event.ONCLICK);
        }

        public void onBrowserEvent(Event event) {
            switch (DOM.eventGetType(event)) {
                case Event.ONCLICK:
                    clickListener.fireClick(this);
                    DOM.eventCancelBubble(event, true);
            }
        }

        public void addClickListener(ClickListener listener) {
            clickListener.add(listener);
        }

    }

    private ErrorMessageCleanupListener errorMessageCleanupListener = new ErrorMessageCleanupListener();

    private class ErrorMessageCleanupListener implements ChangeListener {

        public void onChange(Widget widget) {
            clearErrorMessages();
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

    private class StartStageChangeListener implements ChangeListener {
        public void onChange(Widget widget) {
            int startStageIndex = startStageList.getSelectedIndex();
            // always set end stage = start stage when changing start stage.
            endStageList.setSelectedIndex(startStageIndex);
        }
    }

    private class MultiStageButtonClickListener implements ClickListener {
        public void onClick(Widget widget) {
            if (startStage.isVisible()) {
                startStage.setVisible(false);
                endStage.setVisible(false);
                multiStartStageList.setVisible(true);
                multiStageButton.setText(SELECT_SINGLE_STAGE);
            } else {
                startStage.setVisible(true);
                endStage.setVisible(true);
                multiStartStageList.setVisible(false);
                multiStageButton.setText(SELECT_MULTIPLE_STAGES);
            }
        }
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
            super("Error while reading Figure Filters", errorMessage);
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
            super("Error while reading Figure Filters", errorMessage);
        }

        public void onSuccess(List<StageDTO> stages) {

            startStageList.clear();
            endStageList.clear();
            multiStartStageList.clear();
            for (StageDTO stageDTO : stages) {
                startStageList.addItem(stageDTO.getName(), stageDTO.getZdbID());
                endStageList.addItem(stageDTO.getName(), stageDTO.getZdbID());
                multiStartStageList.addItem(stageDTO.getName(), stageDTO.getZdbID());
            }
            //Window.alert("SIZE: " + experiments.size());
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class FigureAnnotationCheckmarkStatusCallback implements AsyncCallback<CheckMarkStatusDTO> {
        public void onFailure(Throwable throwable) {
            if (throwable instanceof PublicationNotFoundException) {
                Window.alert("" + throwable);
            } else {
                Window.alert("Fatal exception: " + throwable);
            }
        }

        public void onSuccess(CheckMarkStatusDTO filterValues) {
            //Window.alert("brought back: " + filterValues.getFigureAnnotations().size());
            if (filterValues != null) {
                int maxRows = displayTable.getRowCount();
                for (int row = 1; row < maxRows; row++) {
                    if (!displayTable.isCellPresent(row, 0))
                        continue;
                    Widget widget = displayTable.getWidget(row, 0);
                    if (widget == null || !(widget instanceof SelectExpressionCheckBox))
                        continue;

                    SelectExpressionCheckBox checkBox = (SelectExpressionCheckBox) widget;
                    for (ExpressionFigureStageDTO dto : filterValues.getFigureAnnotations()) {
                        if (dto.getUniqueID().equals(checkBox.getTitle())) {
                            checkBox.setChecked(true);
                            ExpressionFigureStageDTO checkedExpression = displayTableMap.get(row);
                            selectedExpressions.add(checkedExpression);
                        }
                    }
                }
            }
            curationEntryPoint.getStructureModule().setSelectedFigureAnnotations(selectedExpressions);
            curationEntryPoint.getStructureModule().runModule();
        }
    }

    private class SectionVisiblityCallback extends ZfinAsyncCallback<Boolean> {
        public SectionVisiblityCallback(String message) {
            super(message, errorMessage);
        }

        public void onSuccess(Boolean visible) {
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (displayedExpressions == null || displayedExpressions.size() == 0) {
                setInitialValues();
            } else {
                createExpressionTable();
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
    protected void removeFigureAnnotations(ExperimentDTO deletedExperiment) {
        List<ExpressionFigureStageDTO> toBeDeleted = new ArrayList<ExpressionFigureStageDTO>();
        for (ExpressionFigureStageDTO efs : displayedExpressions) {
            ExperimentDTO expDto = efs.getExperiment();
            if (expDto.getExperimentZdbID().equals(deletedExperiment.getExperimentZdbID()))
                toBeDeleted.add(efs);
        }
        for (ExpressionFigureStageDTO efs : toBeDeleted) {
            displayedExpressions.remove(efs);
        }
        createExpressionTable();
    }

    /**
     * Injection of the experiments
     *
     * @param experiments list of ExperimentDTO
     */
    protected void setExperiments(List<ExperimentDTO> experiments) {
        this.experiments = experiments;
        showExperimentList();
    }

    /**
     * Show the list of experiments
     */
    private void showExperimentList() {
        experimentList.clear();
        if (experiments == null)
            return;

        int maxVisibleSize = 8;
        if (experiments.size() < maxVisibleSize)
            maxVisibleSize = experiments.size();
        experimentList.setVisibleItemCount(maxVisibleSize);
        for (ExperimentDTO experiment : experiments) {
            String displayName = FxExperimentModule.concatenatedExperimentText(experiment);
            experimentList.addItem(displayName, experiment.getExperimentZdbID());
        }
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