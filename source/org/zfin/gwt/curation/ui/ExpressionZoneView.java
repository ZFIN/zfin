package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.event.SelectExpressionExperimentEvent;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.*;

/**
 * Expression zone
 */
public class ExpressionZoneView extends Composite implements HandlesError {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExpressionZoneView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, ExpressionZoneView> {
    }

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    SimpleErrorElement errorElement;
    // construction zone
    @UiField
    Button addButton;
    @UiField
    Button resetButton;
    @UiField
    StageSelector stageSelector;
    @UiField
    ListBox figureList;
    // this list is being populated through the DisplayExperimentTable
    private VerticalPanel experimentSelection = new VerticalPanel();
    private boolean showSelectedExpressionOnly;

    @UiField
    Image loadingImage;
    @UiField
    VerticalPanel expressionPanel;

    private StructurePilePresenter structurePilePresenter;
    private ExpressionZonePresenter expressionZonePresenter;

    public ExpressionZoneView() {
        initWidget(uiBinder.createAndBindUi(this));
        stageSelector.setPublicationID(publicationID);
        displayTable = new ExpressionFlexTable(HeaderName.getHeaderNames());
        expressionPanel.add(displayTable);
    }

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showExpressionSection = new Hyperlink();
    private VerticalPanel displayPanel = new VerticalPanel();
    private FlexTable constructionRow = new FlexTable();
    private ExpressionFlexTable displayTable;

    // all annotations that are selected
    private List<ExpressionFigureStageDTO> selectedExpressions = new ArrayList<>(5);
    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionFigureStageDTO> displayedExpressions = new ArrayList<>(15);
    // This maps the display table and contains the full objects that each
    // row is made up from
    private Map<Integer, ExpressionFigureStageDTO> displayTableMap = new HashMap<>(15);

    // attributes for duplicate row
    protected String duplicateRowOriginalStyle;
    protected int duplicateRowIndex;

    private String figureID;
    // used to control that this module is loaded only once.
    private boolean initialized;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    public static final String HIDE = "hide";
    public static final String SHOW = "show";
    public static final String PUSH_TO_PATO = "to pato";
    public static final String IN_PATO = "in pato";
    public static final String PUSHED = "pushed";

    // 20 expressed terms is a bit higher than the average
    // number of expressed terms used in a single publication.
    // Contains all distinct expressed terms for this publication
    private Set<ExpressedTermDTO> expressedTerms = new HashSet<>(20);
    // used for highlighting structures
    private ExpressedTermDTO expressedStructure;
    private boolean markStructures;

    // injected variables
    private ExpressionExperimentZoneView experimentSection;

    // Publication in question.
    private String publicationID;
    // filter set by the banana bar
    private ExperimentDTO experimentFilter = new ExperimentDTO();

/*
    @UiConstructor
    public ExpressionZoneView(ExpressionExperimentZoneView experimentSection) {
        this.experimentSection = experimentSection;
//        stageSelector.setPublicationID(publicationID);
    }
*/

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

    public void setExperimentSection(ExpressionExperimentZoneView experimentSection) {
        this.experimentSection = experimentSection;
    }

    @UiHandler("addButton")
    void onClickAddExpression(@SuppressWarnings("unused") ClickEvent event) {
        expressionZonePresenter.addExpressions();
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationRPCAsync.readExpressionSectionVisibility(publicationID, new SectionVisibilityCallback(message));
    }

    // Retrieve experiments from the server


    protected void recordAllExpressedTerms() {
        expressedTerms.clear();
        for (ExpressionFigureStageDTO expression : this.displayedExpressions) {
            expressedTerms.addAll(expression.getExpressedTerms());
        }
    }

    public void setExpressionZonePresenter(ExpressionZonePresenter expressionZonePresenter) {
        this.expressionZonePresenter = expressionZonePresenter;
    }

    public void setStructurePilePresenter(StructurePilePresenter structurePilePresenter) {
        this.structurePilePresenter = structurePilePresenter;
    }

    private Widget createTermList(List<ExpressionFigureStageDTO> expressionList) {
        ExpressionFigureStageDTO aggregated = new ExpressionFigureStageDTO();
        List<ExpressedTermDTO> expressedTermDTOs = new ArrayList<>(5);
        Set<ExpressedTermDTO> expressedTermSet = new HashSet<>(5);
        for (ExpressionFigureStageDTO dto : expressionList) {
            removeUnspecifiedTerm(dto.getExpressedTerms());
            expressedTermSet.addAll(dto.getExpressedTerms());
        }
        expressedTermDTOs.addAll(expressedTermSet);
        aggregated.setExpressedTerms(expressedTermDTOs);
        return createTermList(aggregated);
    }

    private void removeUnspecifiedTerm(List<ExpressedTermDTO> expressedTerms) {
        if (expressedTerms == null)
            return;
        for (Iterator<ExpressedTermDTO> it = expressedTerms.iterator(); it.hasNext(); )
            if (it.next().getEntity().isUnspecified())
                it.remove();
    }

    private Widget createTermList(ExpressionFigureStageDTO expression) {
        // create comma-delimited list
        //Window.alert("Buffer DTO: " + expression.getUniqueID());
        List<ExpressedTermDTO> terms = expression.getExpressedTerms();
        StringBuilder text = new StringBuilder();
        int index = 1;
        if (terms == null || terms.size() == 0) {
            text.append("<span class=\"term-unspecified\">unspecified</span>");
        } else {
            for (ExpressedTermDTO term : terms) {
                String classSpan;
                if (!term.isExpressionFound()) {
                    classSpan = createSpanElement(term, WidgetUtil.RED);
                } else if (term.getEntity().isUnspecified()) {
                    classSpan = createSpanElement(term, "term-unspecified");
                } else {
                    classSpan = createSpanElement(term, null);
                }
                text.append(classSpan);
                if (index < terms.size())
                    text.append(", ");
                index++;
            }
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
        classSpan.append(term.getHtmlDisplayName());
        if (classNamePrefix != null || markStructures)
            classSpan.append("</span>");
        return classSpan.toString();
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
        experimentSection.unselectAllExperiments();
    }

    /**
     * This method updates the structure pile with the checked figure annotation.
     */
    protected void sendFigureAnnotationsToStructureSection() {
        SelectExpressionEvent event = new SelectExpressionEvent(selectedExpressions);
        AppUtils.EVENT_BUS.fireEvent(event);
    }

    private void saveCheckStatusInSession(ExpressionFigureStageDTO checkedExpression, boolean isChecked) {
        String errorMessage = "Error while saving expression check mark status.";
//TODO
/*
        curationRPCAsync.setFigureAnnotationStatus(checkedExpression, isChecked,
                new VoidAsyncCallback(errorMessage, errorElement, IMAGE_LOADING_EXPRESSION_SECTION));
*/
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

    protected Set<ExperimentDTO> getSelectedExperiments() {
        return experimentSection.getSelectedExperiments();
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
            } else {
                selectedExpressions.remove(checkedExpression);
            }
            SelectExpressionExperimentEvent selectEvent = new SelectExpressionExperimentEvent(checkbox.getValue(), checkedExpression.getExperiment());
            AppUtils.EVENT_BUS.fireEvent(selectEvent);
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

        List<ExpressionFigureStageDTO> expressions = new ArrayList<>();
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

    private class ExpressionSelectCopyStructureButtonClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            if (hasExpressedTerms(selectedExpressions))
                displayTable.copyStructures.setEnabled(true);
            else
                displayTable.copyStructures.setEnabled(false);
        }
    }

    private class ExpressionSelectPasteStructureButtonClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            if (expressionFigureStageDTOBuffer != null && expressionFigureStageDTOBuffer.size() > 0 && selectedExpressions.size() > 0)
                displayTable.pasteStructures.setEnabled(true);
            else
                displayTable.pasteStructures.setEnabled(false);
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
        loadingImage.setVisible(true);
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
            Window.alert("Success");
            // remove from the dashboard list
            displayedExpressions.remove(figureAnnotation);
            selectedExpressions.remove(figureAnnotation);
            // re-create the display table
            displayTable.createExpressionTable();
            // update expression list in structure section.
            sendFigureAnnotationsToStructureSection();
            ////experimentSection.notifyRemovedExpression(figureAnnotation.getExperiment());
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

    public class CopyExpressionsCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        CopyExpressionsCallback() {
            super("Error while deleting Figure Annotation", errorElement);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> exp) {
            //Window.alert("Success copied");
            clearErrorMessages();
            postUpdateStructuresOnExpression();
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
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

    private List<ExpressionFigureStageDTO> expressionFigureStageDTOBuffer = new ArrayList<>(3);

    /**
     * Copy selected Expression/Figure/Stage records into buffer
     */
    private class CopyStructuresButtonHandler implements ClickHandler {

        public void onClick(ClickEvent event) {
            expressionFigureStageDTOBuffer.clear();
            expressionFigureStageDTOBuffer.addAll(selectedExpressions);
            displayTable.clearBuffer.setEnabled(true);
/*
            if (hasExpressedTerms(selectedExpressions)) {
                displayTable.copyStructures.setEnabled(true);
            }
*/
            displayTable.showBufferLastRow(1, createTermList(selectedExpressions));
            // un-select all records to have a clean slate for selecting the
            // records to paste into
            displayTable.uncheckAllRecords();
        }


    }

    private boolean hasExpressedTerms(List<ExpressionFigureStageDTO> selectedExpressions) {
        if (selectedExpressions == null)
            return false;
        for (ExpressionFigureStageDTO dto : selectedExpressions) {
            if (hasSpecifiedTerm(dto.getExpressedTerms()))
                if (dto.getExpressedTerms().size() > 0)
                    return true;
        }
        return false;
    }

    private boolean hasSpecifiedTerm(List<ExpressedTermDTO> expressedTerms) {
        if (expressedTerms == null)
            return false;
        for (ExpressedTermDTO dto : expressedTerms) {
            if (!dto.getEntity().isUnspecified())
                return true;
        }
        return false;
    }

    private class PasteStructuresButtonHandler implements ClickHandler {

        public void onClick(ClickEvent event) {
            List<ExpressionFigureStageDTO> copyFromExpressions = expressionFigureStageDTOBuffer;
            List<ExpressionFigureStageDTO> copyToExpressions = selectedExpressions;
            curationRPCAsync.copyExpressions(copyFromExpressions, copyToExpressions, new CopyExpressionsCallback());
            displayTable.uncheckAllRecords();
        }

        private List<ExpressionFigureStageDTO> getEfsFromMap(List<Integer> copyFromCheckBoxNumber) {
            if (copyFromCheckBoxNumber == null)
                return null;
            List<ExpressionFigureStageDTO> efsList = new ArrayList<>(copyFromCheckBoxNumber.size());
            for (Integer rowIndex : copyFromCheckBoxNumber) {
                efsList.add(displayTableMap.get(rowIndex));
            }
            return efsList;
        }

    }

    private class ClearExpressionBufferHandler implements ClickHandler {

        public void onClick(ClickEvent event) {
            // clear copy/paste buffer
            expressionFigureStageDTOBuffer.clear();
            // override display buffer with empty widget
            displayTable.showBufferLastRow(1, new Label());
            // disable button as there is nothing left to be cleared.
            displayTable.clearBuffer.setEnabled(false);
            // disable paste button as well
            displayTable.pasteStructures.setEnabled(false);
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
     * false if experiment is different from all other experiments
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
///        experimentSection.clearErrorMessages();
    }

    class ExpressionFlexTable extends ZfinFlexTable {

        private HeaderName[] headerNames;
        // copy paste panel
        private HorizontalPanel copyPastePanel = new HorizontalPanel();


        ExpressionFlexTable(HeaderName[] headerNames) {
            super(headerNames.length, new int[]{HeaderName.SELECT.index});
            this.headerNames = headerNames;
            setToggleHyperlink(ToggleLink.SHOW_SELECTED_EXPRESSIONS_ONLY.getText(), ToggleLink.SHOW_ALL_EXPRESSIONS.getText());
            addToggleHyperlinkClickHandler(new ShowSelectedExpressionClickHandler(showSelectedRecords));
            initCopyExpressionPanel();
        }

        protected void createExpressionTable(List<ExpressionFigureStageDTO> list) {
            displayedExpressions = list;
            createExpressionTable();
        }

        protected void createExpressionTable() {
            clearTable();
            // header row index = 0
            createTableHeader();
            int rowIndex = 1;
            //Window.alert("Experiment List Size: " + experiments.size());
            ExpressionFigureStageDTO previousExpression = null;
            // first element is an odd group element
            int groupIndex = 1;

            List<ExpressionFigureStageDTO> expressionFigureStageDTOs;
            if (showSelectedExpressionOnly) {
                expressionFigureStageDTOs = new ArrayList<>();
                expressionFigureStageDTOs.addAll(selectedExpressions);
            } else {
                expressionFigureStageDTOs = displayedExpressions;
            }

            for (ExpressionFigureStageDTO expression : expressionFigureStageDTOs) {

                // rowindex minus the header row
                displayTableMap.put(rowIndex, expression);
                CheckBox checkBox = new CheckBox();
                checkBox.setTitle(expression.getUniqueID());
                // if any figure annotations are already selected make sure they stay checked
                if (selectedExpressions.contains(expression)) {
                    checkBox.setValue(true);
                    //Window.alert("Checkbox");
                    //showClearAllLink();
                }
                checkBox.addClickHandler(new ExpressionSelectClickHandler(expression, checkBox));
                checkBox.addClickHandler(new ExpressionSelectPasteStructureButtonClickHandler());
                checkBox.addClickHandler(new ExpressionSelectCopyStructureButtonClickHandler());
                setWidget(rowIndex, HeaderName.SELECT.getIndex(), checkBox);

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
            handleCopyStructureBuffer();
            //Window.alert("HIO II");
        }

        private void handleCopyStructureBuffer() {
            if (expressionFigureStageDTOBuffer != null && expressionFigureStageDTOBuffer.size() > 0) {
                displayTable.showBufferLastRow(1, createTermList(expressionFigureStageDTOBuffer));
            }
        }

        public void createBottomClearAllLinkRow(int rowIndex) {
            super.createBottomClearAllLinkRow(rowIndex, headerNames.length - 3);
            //Window.alert("create bottom row: " + copyPastePanel.getWidgetCount());
            setWidget(rowIndex, 1, copyPastePanel);
        }

        Button copyStructures = new Button("Copy Structures", new CopyStructuresButtonHandler());
        Button pasteStructures = new Button("Paste Structures", new PasteStructuresButtonHandler());
        Button clearBuffer = new Button("Clear Buffer", new ClearExpressionBufferHandler());

        private void initCopyExpressionPanel() {
            copyStructures.setEnabled(false);
            pasteStructures.setEnabled(false);
            clearBuffer.setEnabled(false);
            copyPastePanel.add(copyStructures);
            copyPastePanel.add(pasteStructures);
            copyPastePanel.add(clearBuffer);
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
            displayTable.copyStructures.setEnabled(false);
            displayTable.pasteStructures.setEnabled(false);
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
                if (name.index != 0) {
                    setText(0, name.index, name.getName());
                    String alignment = "bold";
                    getCellFormatter().setStyleName(0, name.index, alignment);
                }
            }
        }

        public List<Integer> getCheckBox(int column) {
            List<Integer> numbers = new ArrayList<>(5);
            int rowCount = getRowCount();
            for (Integer rowIndex = 1; rowIndex < rowCount; rowIndex++) {
                Widget widget;
                try {
                    widget = getWidget(rowIndex, column);
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                if (widget == null || !(widget instanceof CheckBox))
                    continue;
                CheckBox checkBox = (CheckBox) widget;
                if (checkBox.getValue())
                    numbers.add(rowIndex);
            }
            return numbers;
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
        updateFigureListBox(null);
    }

    public void setExperimentFilter(ExperimentDTO experimentFilter) {
        this.experimentFilter = experimentFilter;
    }

    public void updateFigureListBox(List<FigureDTO> allFigureDtos) {
        figureList.clear();
        for (FigureDTO figureDTO : allFigureDtos) {
            if (figureID == null || figureDTO.getZdbID().equals(figureID))
                figureList.addItem(figureDTO.getLabel(), figureDTO.getZdbID());
        }
    }

    private class SectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        // flag that indicates if the experiment section is visible or not.
        private boolean sectionVisible;

        public SectionVisibilityCallback(String message) {
            super(message, errorElement);
        }

        public void onSuccess(Boolean visible) {
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (displayedExpressions == null || displayedExpressions.isEmpty()) {
                //todo setInitialValues();
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
        List<ExpressionFigureStageDTO> toBeDeleted = new ArrayList<>();
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
        SELECT(0, "", Alignment.CENTER),
        FIGURE(1, "Figure", Alignment.LEFT),
        GENE(2, "Gene", Alignment.LEFT),
        FISH(3, "Fish", Alignment.LEFT),
        ENVIRONMENT(4, "Env ", Alignment.LEFT),
        ASSAY(5, "Assay", Alignment.LEFT),
        ANTIBODY(6, "Ab", Alignment.LEFT),
        STAGE_RANGE(7, "Stage Range", Alignment.LEFT),
        EXPRESSED_IN(8, "Expressed in", Alignment.LEFT),
        DELETE(9, "Del", Alignment.CENTER),
        PUSH(10, "Push", Alignment.LEFT);

        private int index;
        private String value;
        Alignment alignment;

        private HeaderName(int index, String value, Alignment alignment) {
            this.index = index;
            this.value = value;
            this.alignment = alignment;
        }

        public String getName() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        public Alignment getAlignment() {
            return alignment;
        }

        public static HeaderName[] getHeaderNames() {
            return ExpressionZoneView.HeaderName.values();
        }
    }

    private enum Alignment {
        LEFT, RIGHT, CENTER
    }


    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        structurePilePresenter.retrieveStructurePile();
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

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    public SimpleErrorElement getErrorElement() {
        return errorElement;
    }

    public StageSelector getStageSelector() {
        return stageSelector;
    }

    public void setLoadingImageVisibility(boolean show) {
        loadingImage.setVisible(show);
    }

    public ExpressionFlexTable getDisplayTable() {
        return displayTable;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }
}
