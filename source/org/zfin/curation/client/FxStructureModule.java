package org.zfin.curation.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.curation.dto.*;

import java.util.*;

/**
 */
public class FxStructureModule extends Composite {

    // div-elements
    public static final String SHOW_HIDE_STRUCTURES = "show-hide-structures";
    public static final String UPDATE_EXPERIMENTS = "update-experiments";
    public static final String NEW_TERM_SUGGESTION = "new-term-suggestion";
    public static final String STRUCTURES_DISPLAY = "display-structures";
    public static final String IMAGE_LOADING_STRUCTURE_SECTION = "image-loading-structure-section";
    public static final String STRUCTURES_DISPLAY_ERRORS = "display-structure-errors";
    public static final String CONSTRUCTION_ZONE = "structure-pile-construction-zone";

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showStructureSection = new Hyperlink();
    private ZfinFlexTable displayTable;
    private Image loadingImage = new Image();
    private Label errorMessage = new Label();
    private Button updateButtonAbove = new Button("Update Structures for Expressions");
    private Button updateButtonBelow = new Button("Update Structures for Expressions");
    private VerticalPanel suggestionDiv;
    // this list is being populated through the DisplayExperimentTable
    private ZfinListBox figureAnnotationList = new ZfinListBox(true);

    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<PileStructureDTO> displayedStructures = new ArrayList<PileStructureDTO>();
    // list of expressions selected in the expression section.
    private List<ExpressionFigureStageDTO> selectedFigureAnnotations = new ArrayList<ExpressionFigureStageDTO>();

    // flag that indicates if the experiment section is visible or not.
    private boolean sectionVisible;
    // used to control that this module is loaded only once.
    private boolean initialized;

    // This maps the display table and contains the full object that each
    // row is made up from
    private Map<Integer, PileStructureDTO> displayTableMap = new HashMap<Integer, PileStructureDTO>();
    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public static final String HIDE = "hide";
    public static final String SHOW = "show";
    private static final String ACTION = "action";
    public static final String NOT = "not";

    // injected variables
    private FxExpressionModule expressionModule;
    // Publication in question.
    private String publicationID;
    private static final String UNSPECIFIED = "unspecified";
    private static final String STRUCTURE_CONSTRUCTION_ZONE = "structure-construction-zone";

    public FxStructureModule(CurationEntryPoint curationEntryPoint) {
        expressionModule = curationEntryPoint.getExpressionModule();
        publicationID = curationEntryPoint.getPublicationID();
        displayTable = new ZfinFlexTable(HeaderName.getHeaderNames());
        initGUI();
    }

    private void initGUI() {
        initShowHideGUI();

        VerticalPanel panel = new VerticalPanel();
        updateButtonAbove.setTitle("Update Structures for Expressions");
        updateButtonAbove.addClickListener(new UpdateStructuresClickListener());
        updateButtonBelow.addClickListener(new UpdateStructuresClickListener());
        panel.add(updateButtonAbove);
        figureAnnotationList.setEnabled(false);
        panel.add(figureAnnotationList);
        RootPanel.get(UPDATE_EXPERIMENTS).add(panel);
        RootPanel.get(STRUCTURES_DISPLAY).add(displayTable);

        RootPanel.get(STRUCTURES_DISPLAY_ERRORS).add(errorMessage);
        RootPanel.get(IMAGE_LOADING_STRUCTURE_SECTION).add(loadingImage);
        errorMessage.setStyleName("error");
        loadingImage.setUrl("/images/ajax-loader.gif");

        suggestionDiv = new VerticalPanel();
        suggestionDiv.setVisible(false);
        StringBuilder title = new StringBuilder();
        suggestionDiv.setStyleName("red");
        suggestionDiv.add(new Label(title.toString()));
        RootPanel.get(NEW_TERM_SUGGESTION).add(suggestionDiv);

        figureAnnotationList.setVisibleItemCount(0);
    }

    private void initShowHideGUI() {
        RootPanel.get(SHOW_HIDE_STRUCTURES).add(panel);
        Label experimentLabel = new Label("Structures: ");
        experimentLabel.setStyleName("bold");
        panel.add(experimentLabel);
        showStructureSection.setStyleName("small");
        showStructureSection.setText(SHOW);
        showStructureSection.setTargetHistoryToken(SHOW);
        showStructureSection.addClickListener(new ShowHideStructureSectionListener());
        panel.add(showStructureSection);
    }

    /**
     * The data should only be loaded when the filter bar is initilized.
     * So this method is called from the FxFilterTable.
     */
    protected void runModule() {
        if (!initialized) {
            loadSectionVisibility();
            initialized = true;
        }
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationRPCAsync.readStructureSectionVisibility(publicationID, new SectionVisibilityCallback(message));
    }

    // Retrieve pile structures from the server
    protected void retrievePileStructures() {
        loadingImage.setVisible(true);
        curationRPCAsync.getStructures(publicationID, new RetrieveStructuresCallback());
    }

    private void createTableHeader() {
        displayTable.setWidth("100%");
        displayTable.setHeaderRow();
        displayTable.addStyleName("searchresults groupstripes-hover");
        displayTable.getRowFormatter().setStyleName(0, "table-header");
    }

    public void createStructureTable() {
        clearExpressionListTable();
        displayTableMap.clear();
        // header row index = 0
        createTableHeader();
        int rowIndex = 1;
        //Window.alert("Experiment List Size: " + experiments.size());
        // first element is an odd group element
        int groupIndex = 1;
        for (PileStructureDTO structure : this.displayedStructures) {
            // put the object in the map for later retrieval
            displayTableMap.put(rowIndex, structure);
            //Window.alert("Experiment: " + experiment.getGeneName());
            String radioButtonName = ACTION + structure.getZdbID();
            SelectRadioButton nothing = new SelectRadioButton(radioButtonName);
            nothing.setChecked(true);
            nothing.setVisible(false);
            displayTable.setWidget(rowIndex, HeaderName.NOTHING.getIndex(), nothing);
            //displayTable.getCellFormatter().setWidth(rowIndex, HeaderName.NOTHING.getIndex(), "0");
            RadioButton remove = new RadioButton(radioButtonName);
            displayTable.setWidget(rowIndex, HeaderName.REMOVE_FROM_EXPRESSION.getIndex(), remove);
            RadioButton add = new RadioButton(radioButtonName);
            displayTable.getCellFormatter().setWidth(rowIndex, HeaderName.REMOVE_FROM_EXPRESSION.getIndex(), "15");
            displayTable.setWidget(rowIndex, HeaderName.ADD.getIndex(), add);
            displayTable.getCellFormatter().setWidth(rowIndex, HeaderName.ADD.getIndex(), "15");
            CheckModifierCheckbox checkBox = new CheckModifierCheckbox();
            displayTable.setWidget(rowIndex, HeaderName.MODIFIER.getIndex(), checkBox);
            displayTable.getCellFormatter().setWidth(rowIndex, HeaderName.MODIFIER.getIndex(), "20");
            HorizontalPanel postcomposedTerm = new HorizontalPanel();
            ExpressedTermDTO expressedTerm = structure.getExpressedTerm();
            postcomposedTerm.setTitle(expressedTerm.getUniqueID());
            createStructureElement(postcomposedTerm, expressedTerm);
            displayTable.setWidget(rowIndex, HeaderName.STRUCTURE.getIndex(), postcomposedTerm);
            checkBox.addClickListener(new NotClickListener(rowIndex, structure));
            add.addClickListener(new AddActionButtonListener(rowIndex, structure));
            Label stage = new Label(structure.getStageRange());
            displayTable.setWidget(rowIndex, HeaderName.STAGE.getIndex(), stage);
            Button delete = new Button("X");
            String title = createDeleteButtonTitle(structure);
            delete.setTitle(title);
            delete.addClickListener(new RemovePileStructureClickListener(structure));
            displayTable.setWidget(rowIndex, HeaderName.REMOVE.getIndex(), delete);
            groupIndex = setRowStyle(rowIndex, groupIndex);
            rowIndex++;
        }
        // add horizontal line
        displayTable.getFlexCellFormatter().setColSpan(rowIndex, 0, HeaderName.getHeaderNames().length);
        HTML html = new HTML("<hr/>");
        displayTable.setWidget(rowIndex, 0, html);

        createConstructionZone();
    }

    private String createDeleteButtonTitle(PileStructureDTO structure) {
        String title = structure.getZdbID();
        title += ":";
        title += structure.getCreator();
        title += ":";
        title += structure.getDate().toString().substring(0, 20);
        return title;
    }

    private void createStructureElement(HorizontalPanel postcomposedTerm, ExpressedTermDTO expressedTerm) {
        ZfinHyperlinkWithEventCancelBubble superterm = new ZfinHyperlinkWithEventCancelBubble(expressedTerm.getSupertermName(), STRUCTURE_CONSTRUCTION_ZONE);
        superterm.addClickListener(new PileStructureClickListener(expressedTerm, PileEntity.SUPER_TERM));
        postcomposedTerm.add(superterm);
        ZfinHyperlinkWithEventCancelBubble subTerm;
        if (expressedTerm.getSubtermID() != null) {
            Label colon = new Label(" : ");
            postcomposedTerm.add(colon);
            subTerm = new ZfinHyperlinkWithEventCancelBubble(expressedTerm.getSubtermName(), STRUCTURE_CONSTRUCTION_ZONE);
            subTerm.addClickListener(new PileStructureClickListener(expressedTerm, PileEntity.SUBTERM));
            postcomposedTerm.add(subTerm);
        }
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
        displayTable.setWidget(rowIndex, HeaderName.NOTHING.getIndex(), updateButtonBelow);
        displayTable.getFlexCellFormatter().setColSpan(rowIndex, HeaderName.NOTHING.getIndex(), 5);
    }


    // Returns the boolean: isOddGroup
    private int setRowStyle(int rowIndex, int groupIndex) {
        StringBuilder sb = new StringBuilder();
        // check even/odd row
        if (rowIndex % 2 == 0)
            sb.append("even newgroup evengroup");
        else
            sb.append("odd newgroup oddgroup");
        // 
        sb.append(" experiment-row ");
        displayTable.getRowFormatter().setStyleName(rowIndex, sb.toString());
        return groupIndex;
    }

    public void setShowHideWidget(Hyperlink showHideLink) {
        showStructureSection = showHideLink;
    }

    /**
     * Called from the expression module.
     *
     * @param selectedFigureAnnotations list of figure annotations.
     */
    public void setSelectedFigureAnnotations(List<ExpressionFigureStageDTO> selectedFigureAnnotations) {
        this.selectedFigureAnnotations = selectedFigureAnnotations;
    }

    /**
     * 1) Add the provided efs to the expression text area
     * 2) Set radio button to 'add' for expressed structures if all checked ones share the structure
     * 3) highlight structures that fall into the intersection of all checked annotations.
     * <p/>
     * Called by the Expression Module.
     *
     * @param figureAnnotations figure annotations
     */
    public void updateFigureAnnotations(List<ExpressionFigureStageDTO> figureAnnotations) {
        // Todo: This statement should go somehwere in a more central location.
        suggestionDiv.setVisible(false);
        if (figureAnnotations == null)
            return;

        selectedFigureAnnotations = figureAnnotations;
        showFigureAnnotationList();
        List<ExpressedTermDTO> intersectionOfStructures = createIntersectionOfStructures(figureAnnotations);
        selectUnslectStructuresOnPile(intersectionOfStructures);
        StageRangeUnion stageUnion = new StageRangeUnion(figureAnnotations);
        StageRangeIntersection stageIntersection = new StageRangeIntersection(stageUnion.getStart(), stageUnion.getEnd());
        displayTable.markOverlappingStructures(stageIntersection);
    }

    /**
     * Show the list of figure annotations
     */
    private void showFigureAnnotationList() {
        figureAnnotationList.clear();
        if (selectedFigureAnnotations == null)
            return;

        int visibleSize = 8;
        if (selectedFigureAnnotations.size() < visibleSize)
            visibleSize = selectedFigureAnnotations.size();
        figureAnnotationList.setVisibleItemCount(visibleSize);
        for (ExpressionFigureStageDTO figureAnnotation : selectedFigureAnnotations) {
            String displayName = concatenateFigureAnnotationDisplay(figureAnnotation);
            figureAnnotationList.addItem(displayName, figureAnnotation.getUniqueID());
        }
    }

    private String concatenateFigureAnnotationDisplay(ExpressionFigureStageDTO figureAnnotation) {
        String displayText = figureAnnotation.getFigureLabel() + " ";
        displayText += FxExperimentModule.concatenatedExperimentText(figureAnnotation.getExperiment());
        displayText += " " + figureAnnotation.getStart().getName();
        if (!figureAnnotation.getEnd().getName().equals(figureAnnotation.getStart().getName()))
            displayText += " - " + figureAnnotation.getEnd().getName();
        return displayText;
    }

    @SuppressWarnings("unchecked")
    private List<ExpressedTermDTO> createIntersectionOfStructures(List<ExpressionFigureStageDTO> figureAnnotations) {
        List<ExpressedTermDTO> intersectionOfStructures = new ArrayList<ExpressedTermDTO>();
        int index = 0;
        for (ExpressionFigureStageDTO figureAnnotation : figureAnnotations) {
            if (index == 0)
                intersectionOfStructures.addAll(figureAnnotation.getExpressedTerms());
            else {
                intersectionOfStructures = (List<ExpressedTermDTO>) CollectionUtils.intersection(intersectionOfStructures, figureAnnotation.getExpressedTerms());
            }
            index++;
        }
        return intersectionOfStructures;
    }

    private List<ExpressedTermDTO> createUnionOfStructures(List<ExpressionFigureStageDTO> figureAnnotations) {
        List<ExpressedTermDTO> intersectionOfStructures = new ArrayList<ExpressedTermDTO>();
        int index = 0;
        for (ExpressionFigureStageDTO figureAnnotation : figureAnnotations) {
            if (index == 0)
                intersectionOfStructures.addAll(figureAnnotation.getExpressedTerms());
            else {
                intersectionOfStructures = (List<ExpressedTermDTO>) CollectionUtils.intersection(intersectionOfStructures, figureAnnotation.getExpressedTerms());
            }
            index++;
        }
        return intersectionOfStructures;
    }

    /**
     * Select or unselect the structure on the pile.
     *
     * @param expressedTerms list of ExpressedTermDTO
     */
    private void selectUnslectStructuresOnPile(List<ExpressedTermDTO> expressedTerms) {
        if (expressedTerms == null)
            return;

        displayTable.setCommonStructures(expressedTerms);
    }

    private class RetrieveStructuresCallback extends ZfinAsyncCallback<List<PileStructureDTO>> {

        public RetrieveStructuresCallback() {
            super("Error while reading Structures", errorMessage);
        }

        public void onSuccess(List<PileStructureDTO> list) {

            displayedStructures.clear();
            if (list == null)
                return;

            for (PileStructureDTO structure : list) {
                // do not add 'unspecfied'
                if (!structure.getExpressedTerm().getSupertermName().equals(UNSPECIFIED))
                    displayedStructures.add(structure);
            }
            //Window.alert("SIZE: " + experiments.size());
            createStructureTable();
            updateFigureAnnotations(selectedFigureAnnotations);
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * Show or hide structure pile section.
     */
    private class ShowHideStructureSectionListener implements ClickListener {

        public void onClick(Widget widget) {
            String errorMessage = "Error while trying to save structure visibility";
            if (sectionVisible) {
                // hide section
                showStructureSection(false);
                showStructureSection.setText(SHOW);
                sectionVisible = false;
                curationRPCAsync.setStructureVisibilitySession(publicationID, false,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            } else {
                // display structure pile
                // check if it already exists
                if (displayTable != null && displayTable.getRowCount() > 0) {
                    showStructureSection(true);
                } else {
                    retrievePileStructures();
                }
                showStructureSection.setText(HIDE);
                sectionVisible = true;
                curationRPCAsync.setStructureVisibilitySession(publicationID, true,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            }
            clearErrorMessages();
        }

    }

    private void showStructureSection(boolean show) {
        if (show) {
            displayTable.setVisible(true);
            updateButtonAbove.setVisible(true);
            updateButtonBelow.setVisible(true);
            figureAnnotationList.setVisible(true);
            RootPanel.get(CONSTRUCTION_ZONE).setVisible(true);
        } else {
            displayTable.setVisible(false);
            updateButtonAbove.setVisible(false);
            updateButtonBelow.setVisible(false);
            figureAnnotationList.setVisible(false);
            RootPanel.get(CONSTRUCTION_ZONE).setVisible(false);
        }

    }

    /**
     * Remove error messages.
     * Unmark structures in figure annotations.
     */
    public void clearErrorMessages() {
        errorMessage.setText(null);
        expressionModule.markStructuresForDeletion(null, false);
    }

    public class ZfinFlexTable extends FlexTable implements TableListener {

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
            // check if the row has a structure
            if (widget == null || !(widget instanceof RadioButton))
                return;
            // if checkbox is checked or removed do not rotate radio buttons
            if (cell == HeaderName.MODIFIER.getIndex() || cell == HeaderName.REMOVE.getIndex())
                return;
            // if a radio button is clicked do not rotate
            if (cell == HeaderName.NOTHING.getIndex() || cell == HeaderName.REMOVE_FROM_EXPRESSION.getIndex() ||
                    cell == HeaderName.ADD.getIndex())
                return;

            RadioButton nothing = (RadioButton) widget;
            RadioButton remove = (RadioButton) getWidget(row, HeaderName.REMOVE_FROM_EXPRESSION.getIndex());
            RadioButton add = (RadioButton) getWidget(row, HeaderName.ADD.getIndex());
            if (nothing.isChecked()) {
                add.setChecked(true);
            } else if (add.isChecked()) {
                remove.setChecked(true);
            } else {
                nothing.setChecked(true);
            }
            PileStructureDTO selectedPileStructure = displayTableMap.get(row);
            checkNeedForAlternativeStructures(selectedPileStructure, nothing, add);

        }

        protected void checkNeedForAlternativeStructures(PileStructureDTO selectedPileStructure, RadioButton nothing, RadioButton add) {
            // check if there is at least one figure annotation selected.
            // if structure is not available, i.e. out of the stage range check if there is an ancestor (develops_from)
            // or a descendent (develops_to) structure that could be used instead.
            if (figureAnnotationList.getItemCount() > 0 && add.isChecked()) {
                StageRangeUnion union = new StageRangeUnion(selectedFigureAnnotations);
                StageRangeIntersection intersection = new StageRangeIntersection(union.getStart(), union.getEnd());
                if (!intersection.isFullOverlap(selectedPileStructure.getStart(), selectedPileStructure.getEnd())) {
                    suggestionDiv.setVisible(true);
                    noStageOverlapTitle(selectedPileStructure.getExpressedTerm(), intersection);
                    curationRPCAsync.getTermsWithStageOverlap(selectedPileStructure, intersection,
                            new StageOverlapTermsCallback(selectedPileStructure));
                    // set action button to 'nothing'. We do not allow to add a structure without stage overlap.
                    nothing.setChecked(true);
                }
            }
        }

        /**
         * Set table header row, assuming it is rowIndex = 0;
         */
        public void setHeaderRow() {
            int rowIndex = 0;
            for (HeaderName name : headerNames) {
                if (name.getIndex() == 0) {
/*
                    HTML phi = new HTML("&Phi;");
                    setWidget(rowIndex, name.index, phi);
                    WidgetUtil.addOrRemoveCssStyle(phi, WidgetUtil.BOLD, true);
*/
                } else if (name.getIndex() == 1) {
                    HTML cross = new HTML("&otimes;");
                    setWidget(rowIndex, name.index, cross);
                    WidgetUtil.addOrRemoveCssStyle(cross, WidgetUtil.RED, true);
                    WidgetUtil.addOrRemoveCssStyle(cross, WidgetUtil.BOLD, true);
                } else if (name.getIndex() == 2) {
                    HTML plus = new HTML("+");
                    setWidget(rowIndex, name.index, plus);
                    WidgetUtil.addOrRemoveCssStyle(plus, WidgetUtil.GREEN, true);
                    WidgetUtil.addOrRemoveCssStyle(plus, WidgetUtil.BOLD, true);
                } else
                    setText(rowIndex, name.index, name.getName());
            }
        }

        /**
         * Fish out the row the structure resides on the pile that matches the expressed term.
         * If the expressed term is not found it returns -1
         *
         * @param term Expressed Term
         * @return row number
         */
        public int getRowByExpressedTerm(ExpressedTermDTO term) {
            int numOfRows = getRowCount();
            for (int row = 0; row < numOfRows; row++) {
                HorizontalPanel structurePanel = (HorizontalPanel) getWidget(row, HeaderName.STRUCTURE.getIndex());
                if (structurePanel != null && structurePanel.getTitle().equals(term.getUniqueID()))
                    return row;
            }
            return -1;
        }

        /**
         * Sets the given terms to 'Add' and all others to 'Do nothing'
         *
         * @param terms terms to be set to 'add'
         */
        public void setCommonStructures(List<ExpressedTermDTO> terms) {
            resetActionButtons();
            if (terms == null || terms.size() == 0)
                return;

            int numOfRows = getRowCount();
            for (int row = 1; row < numOfRows - 1; row++) {
                if (!isCellPresent(row, HeaderName.STRUCTURE.getIndex()))
                    continue;

                Widget widget = getWidget(row, HeaderName.STRUCTURE.getIndex());
                if (widget instanceof HorizontalPanel) {
                    HorizontalPanel structurePanel = (HorizontalPanel) widget;
                    CheckModifierCheckbox modifier = (CheckModifierCheckbox) getWidget(row, HeaderName.MODIFIER.getIndex());
                    for (ExpressedTermDTO term : terms) {
                        if (structurePanel.getTitle().equals(term.getUniqueID())) {
                            RadioButton addButton = (RadioButton) getWidget(row, HeaderName.ADD.getIndex());
                            addButton.setChecked(true);
                            // set modifier 
                            if (term.isExpressionFound())
                                modifier.setChecked(false);
                            else
                                modifier.setChecked(true);
                            modifier.fireCheckEvent();
                        }
                    }
                } else
                    break;
            }
        }

        /**
         * Set all radio buttons to 'do-nothing'
         * Uncheck all not-modifier
         * undo bold facing
         */
        public void resetActionButtons() {
            int numOfRows = getRowCount();
            for (int row = 1; row < numOfRows - 1; row++) {
                Widget widget = getWidget(row, HeaderName.NOTHING.getIndex());
                if (widget instanceof RadioButton) {
                    RadioButton doNothingButton = (RadioButton) widget;
                    doNothingButton.setChecked(true);
                } else
                    break;
                CheckModifierCheckbox modifier = (CheckModifierCheckbox) getWidget(row, HeaderName.MODIFIER.getIndex());
                modifier.setChecked(false);
                modifier.resetCheckmarks();
                unsetColor(row);
                highlightStructure(row, false);
            }
        }

        public void highlightStructure(int row, boolean highlight) {
            Widget widget = getWidget(row, HeaderName.STRUCTURE.getIndex());
            if (!(widget instanceof HorizontalPanel))
                return;

            HorizontalPanel structurePanel = (HorizontalPanel) widget;
            Hyperlink superTerm = (Hyperlink) structurePanel.getWidget(0);
            WidgetUtil.addOrRemoveCssStyle(superTerm, WidgetUtil.BOLD, highlight);
            int numOfWidgets = structurePanel.getWidgetCount();
            if (numOfWidgets > 1) {
                Hyperlink subTerm = (Hyperlink) structurePanel.getWidget(2);
                WidgetUtil.addOrRemoveCssStyle(subTerm, WidgetUtil.BOLD, highlight);
            }
        }

        public void unsetColor(int row) {
            Widget widget = getWidget(row, HeaderName.STRUCTURE.getIndex());
            if (!(widget instanceof HorizontalPanel))
                return;

            HorizontalPanel structurePanel = (HorizontalPanel) widget;
            Hyperlink superTerm = (Hyperlink) structurePanel.getWidget(0);
            WidgetUtil.addOrRemoveCssStyle(superTerm, WidgetUtil.RED_HYPERLINK, false);
            int numOfWidgets = structurePanel.getWidgetCount();
            if (numOfWidgets > 1) {
                Hyperlink subTerm = (Hyperlink) structurePanel.getWidget(2);
                WidgetUtil.addOrRemoveCssStyle(subTerm, WidgetUtil.RED_HYPERLINK, false);
            }
        }

        public void markOverlappingStructures(StageRangeIntersection stageIntersection) {
            int numOfRows = getRowCount();
            for (int row = 1; row < numOfRows - 1; row++) {
                PileStructureDTO structure = displayTableMap.get(row);
                if (structure == null)
                    continue;
                if (stageIntersection.isFullOverlap(structure.getStart(), structure.getEnd()))
                    highlightStructure(row, true);
                else
                    highlightStructure(row, false);
            }
        }

    }

    private void noStageOverlapTitle(ExpressedTermDTO expressedTerm, StageRangeIntersection intersection) {
        suggestionDiv.clear();
        StringBuilder message = new StringBuilder("'");
        message.append(expressedTerm.getComposedTerm());
        message.append("' has no stage overlap with the stages of the selected expressions: ");
        boolean isMultipleStage = intersection.getStartHours() != intersection.getEndHours();
        message.append("[");
        message.append(intersection.getStart().getName());
        if (isMultipleStage) {
            message.append(",");
            message.append(intersection.getEnd().getName());
        }
        message.append("]");
        HorizontalPanel hor = new HorizontalPanel();
        Label note = new Label(message.toString());
        note.setStyleName("error");
        hor.add(note);
        suggestionDiv.add(hor);
    }

    private class SectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public SectionVisibilityCallback(String message) {
            super(message, FxStructureModule.this.errorMessage);
        }

        public void onSuccess(Boolean visible) {
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (sectionVisible) {
                if (displayedStructures == null || displayedStructures.size() == 0) {
                    retrievePileStructures();
                } else {
                    createStructureTable();
                }
                showStructureSection.setText(HIDE);
                showStructureSection(true);
            } else {
                retrievePileStructures();
                showStructureSection.setText(SHOW);
                showStructureSection(false);
            }
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private enum HeaderName {
        NOTHING(0, ""),
        REMOVE_FROM_EXPRESSION(1, ""),
        ADD(2, ""),
        MODIFIER(3, "not"),
        STRUCTURE(4, "Structure"),
        STAGE(5, "Stage Range"),
        REMOVE(6, "Remove");

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
            return FxStructureModule.HeaderName.values();
        }
    }

    private class NotClickListener implements ClickListener {

        private CheckBox checkBox;
        private Hyperlink superTerm;
        private Hyperlink subTerm;
        private RadioButton add;
        private SelectRadioButton nothing;
        private PileStructureDTO pileStructure;

        private NotClickListener(int row, PileStructureDTO pileStructure) {
            super();
            this.pileStructure = pileStructure;
            HorizontalPanel panel = (HorizontalPanel) displayTable.getWidget(row, HeaderName.STRUCTURE.getIndex());
            superTerm = (Hyperlink) panel.getWidget(0);
            int numOfWidgets = panel.getWidgetCount();
            if (numOfWidgets > 1)
                subTerm = (Hyperlink) panel.getWidget(2);
            add = (RadioButton) displayTable.getWidget(row, HeaderName.ADD.getIndex());
            nothing = (SelectRadioButton) displayTable.getWidget(row, HeaderName.NOTHING.getIndex());
            checkBox = (CheckModifierCheckbox) displayTable.getWidget(row, HeaderName.MODIFIER.getIndex());
        }

        public void onClick(Widget widget) {
            if (checkBox.isChecked()) {
                checkBox.setChecked(true);
                WidgetUtil.addOrRemoveCssStyle(superTerm, WidgetUtil.RED_HYPERLINK, true);
                if (subTerm != null)
                    WidgetUtil.addOrRemoveCssStyle(subTerm, WidgetUtil.RED_HYPERLINK, true);
            } else {
                checkBox.setChecked(false);
                WidgetUtil.addOrRemoveCssStyle(superTerm, WidgetUtil.RED_HYPERLINK, false);
                if (subTerm != null)
                    WidgetUtil.addOrRemoveCssStyle(subTerm, WidgetUtil.RED_HYPERLINK, false);
            }
            if (nothing.isChecked()) {
                add.setChecked(true);
                displayTable.checkNeedForAlternativeStructures(pileStructure, nothing, add);
            }
        }
    }

    private class AddActionButtonListener implements ClickListener {

        private RadioButton add;
        private SelectRadioButton nothing;
        private PileStructureDTO pileStructure;

        private AddActionButtonListener(int row, PileStructureDTO pileStructure) {
            super();
            this.pileStructure = pileStructure;
            add = (RadioButton) displayTable.getWidget(row, HeaderName.ADD.getIndex());
            nothing = (SelectRadioButton) displayTable.getWidget(row, HeaderName.NOTHING.getIndex());
        }

        public void onClick(Widget widget) {
            // the click checked the add-option
            if (add.isChecked()) {
                displayTable.checkNeedForAlternativeStructures(pileStructure, nothing, add);
            }
        }
    }

    private class RemovePileStructureClickListener implements ClickListener {

        private PileStructureDTO structure;

        private RemovePileStructureClickListener(PileStructureDTO structure) {
            this.structure = structure;
        }

        public void onClick(Widget widget) {
            ExpressedTermDTO dto = structure.getExpressedTerm();
            if (expressionModule.getExpressedTermDTOs().contains(dto)) {
                //Window.alert("Please remove the expression records first");
                errorMessage.setText("Please remove the expression records that use this structure first.");
                expressionModule.markStructuresForDeletion(dto, true);
            } else {
                boolean confirmed = Window.confirm("Do you really want to delete this structure form the pile");
                if (confirmed)
                    curationRPCAsync.deleteStructure(structure, new RemovePileStructureCallback(structure));
            }
        }
    }

    private class RemovePileStructureCallback extends ZfinAsyncCallback<Void> {

        private PileStructureDTO structure;

        RemovePileStructureCallback(PileStructureDTO structure) {
            super("Error while deleting Figure Annotation", errorMessage);
            this.structure = structure;
        }

        public void onSuccess(Void exp) {
            //Window.alert("Success");
            // remove from the dashboard list
            displayedStructures.remove(structure);
            // recreate table to update the correct striping
            createStructureTable();
            updateFigureAnnotations(selectedFigureAnnotations);
            loadingImage.setVisible(false);
            clearErrorMessages();
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class SelectRadioButton extends RadioButton {

        private ClickListenerCollection clickListener = new ClickListenerCollection();

        public SelectRadioButton(String name) {
            super(name);
            this.sinkEvents(Event.ONCLICK);
        }

        public void onBrowserEvent(Event event) {
            switch (DOM.eventGetType(event)) {
                case Event.ONCLICK:
                    clickRadioButton();
            }
        }

        public void addClickListener(ClickListener listener) {
            clickListener.add(listener);
        }

        public void clickRadioButton() {
            clickListener.fireClick(this);
        }

    }

    private class CheckModifierCheckbox extends CheckBox {

        private ClickListenerCollection clickListener = new ClickListenerCollection();

        public CheckModifierCheckbox() {
            this.sinkEvents(Event.ONCLICK);
        }

        public void onBrowserEvent(Event event) {
            switch (DOM.eventGetType(event)) {
                case Event.ONCLICK:
                    fireCheckEvent();
            }
        }

        public void addClickListener(ClickListener listener) {
            clickListener.add(listener);
        }

        public void resetCheckmarks() {
            setChecked(false);
        }

        public void fireCheckEvent() {
            clickListener.fireClick(this);
        }

    }


    private class UpdateStructuresClickListener implements ClickListener {

        public void onClick(Widget widget) {
            //Window.alert("Update Structures");
            UpdateExpressionDTO updateEntity = getSelectedStructures();
            List<ExpressionFigureStageDTO> efs = getFigureAnnotations();
            updateEntity.setFigureAnnotations(efs);
            curationRPCAsync.updateStructuresForExpression(updateEntity, new UpdateExpressionCallback());
        }
    }

    private class UpdateExpressionCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        UpdateExpressionCallback() {
            super("Error while update Figure Annotations with pile structures ", errorMessage);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> updatedFigureAnnotations) {
            //Window.alert("Success");
            // update the expression list
            expressionModule.postUpdateStructuresOnExpression();
            figureAnnotationList.clear();
            resetStructureHighlighting();
            loadingImage.setVisible(false);
            clearErrorMessages();
            suggestionDiv.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class StageOverlapTermsCallback extends ZfinAsyncCallback<List<RelatedPileStructureDTO>> {

        private PileStructureDTO selectedPileStructure;

        StageOverlapTermsCallback(PileStructureDTO selectedPileStructure) {
            super("Error while trying to find terms that have an overlap with a given stage", errorMessage);
            this.selectedPileStructure = selectedPileStructure;
        }

        public void onSuccess(List<RelatedPileStructureDTO> terms) {
            //Window.alert("Success");
            // update the expression list
            if (terms != null && terms.size() > 0) {
                suggestionDiv.setVisible(true);
                UnorderedList ul = new UnorderedList();
                VerticalPanel verticalPanel = new VerticalPanel();
                verticalPanel.add(new Label("Alternate structures with overlapping stages:"));
                List<RelatedPileStructureDTO> existingStructures = getExistingStructures(terms);
                List<RelatedPileStructureDTO> newStructures = getNewStructures(terms);

                // add existing structures
                if (existingStructures.size() > 0) {
                    createListItems(ul, existingStructures);
                }
                // add new structures
                if (newStructures.size() > 0) {
                    ListItem itemTwo = new ListItem();
                    ul.add(itemTwo);
                    itemTwo.add(new Label("not yet in pile, click to add:"));
                    UnorderedList termList = new UnorderedList();
                    for (RelatedPileStructureDTO structure : newStructures) {
                        FlexTable infoTable = new FlexTable();
                        infoTable.setCellPadding(5);
                        setSubterm(structure);
                        Hyperlink term = new Hyperlink(structure.getExpressedTerm().getComposedTerm(), "show");
                        term.addClickListener(new CreateSuggestedTermClickListener(structure.getExpressedTerm()));
                        infoTable.setWidget(0, 0, new Label(structure.getRelationship()));
                        infoTable.setWidget(0, 1, term);
                        infoTable.setWidget(0, 2, new Label("[" + structure.getStageRange() + "]"));
                        ListItem ltItem = new ListItem();
                        ltItem.add(infoTable);
                        termList.add(ltItem);
                    }
                    ul.add(itemTwo);
                    ul.add(termList);
                }
                verticalPanel.add(ul);
                suggestionDiv.add(verticalPanel);
            } else {
                suggestionDiv.add(new Label("No alternate structures on immediate children or parents found."));
            }
        }

        private void setSubterm(RelatedPileStructureDTO structure) {
            structure.getExpressedTerm().setSubtermID(selectedPileStructure.getExpressedTerm().getSubtermID());
            structure.getExpressedTerm().setSubtermName(selectedPileStructure.getExpressedTerm().getSubtermName());
            structure.getExpressedTerm().setSubtermOntology(selectedPileStructure.getExpressedTerm().getSubtermOntology());
        }

        private void createListItems(UnorderedList ul, List<RelatedPileStructureDTO> existingStructures) {
            ListItem itemOne = new ListItem();
            ul.add(itemOne);
            itemOne.add(new Label("already in  pile:"));
            UnorderedList termList = new UnorderedList();
            for (RelatedPileStructureDTO structure : existingStructures) {
                FlexTable infoTable = new FlexTable();
                infoTable.setCellPadding(5);
                infoTable.setWidget(0, 0, new Label(structure.getRelationship()));
                infoTable.setWidget(0, 1, new Label(structure.getExpressedTerm().getComposedTerm()));
                infoTable.setWidget(0, 2, new Label("[" + structure.getStageRange() + "]"));
                ListItem ltItem = new ListItem();
                ltItem.add(infoTable);
                termList.add(ltItem);
            }
            ul.add(itemOne);
            ul.add(termList);
        }

        private List<RelatedPileStructureDTO> getExistingStructures(List<RelatedPileStructureDTO> terms) {
            List<RelatedPileStructureDTO> structures = new ArrayList<RelatedPileStructureDTO>();
            for (RelatedPileStructureDTO term : terms)
                if (structureExistsInPile(term))
                    structures.add(term);
            return structures;
        }

        private List<RelatedPileStructureDTO> getNewStructures(List<RelatedPileStructureDTO> terms) {
            List<RelatedPileStructureDTO> structures = new ArrayList<RelatedPileStructureDTO>();
            for (RelatedPileStructureDTO term : terms)
                if (!structureExistsInPile(term))
                    structures.add(term);
            return structures;
        }

        private boolean structureExistsInPile(PileStructureDTO structure) {
            if (structure == null)
                return false;

            for (PileStructureDTO term : displayTableMap.values()) {
                if (term.getExpressedTerm().getSupertermName().equals(structure.getExpressedTerm().getSupertermName()))
                    return true;
            }
            return false;
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * Undo all 'add', 'remove', 'not' selections and highlighting
     */
    public void resetStructureHighlighting() {
        displayTable.resetActionButtons();
    }


    private List<ExpressionFigureStageDTO> getFigureAnnotations() {
        List<ExpressionFigureStageDTO> efses = new ArrayList<ExpressionFigureStageDTO>();
        int num = figureAnnotationList.getItemCount();
        for (int index = 0; index < num; index++) {
            ExpressionFigureStageDTO dto = new ExpressionFigureStageDTO();
            dto.setUniqueID(figureAnnotationList.getValue(index));
            dto.getExperiment().setPublicationID(publicationID);
            efses.add(dto);
        }
        return efses;
    }

    private UpdateExpressionDTO getSelectedStructures() {
        Set<Integer> keys = displayTableMap.keySet();
        UpdateExpressionDTO dto = new UpdateExpressionDTO();
        for (Integer row : keys) {
            RadioButton add = (RadioButton) displayTable.getWidget(row, HeaderName.ADD.getIndex());
            RadioButton remove = (RadioButton) displayTable.getWidget(row, HeaderName.REMOVE_FROM_EXPRESSION.getIndex());
            CheckBox modifier = (CheckBox) displayTable.getWidget(row, HeaderName.MODIFIER.getIndex());
            PileStructureAnnotationDTO psa = new PileStructureAnnotationDTO();
            if (add.isChecked() || remove.isChecked()) {
                PileStructureDTO term = displayTableMap.get(row).copy();
                psa.setExpressedTerm(term.getExpressedTerm());
                psa.setExpressed(!modifier.isChecked());
                psa.setZdbID(term.getZdbID());
            }
            if (add.isChecked())
                psa.setAction(PileStructureAnnotationDTO.Action.ADD);
            else if (remove.isChecked())
                psa.setAction(PileStructureAnnotationDTO.Action.REMOVE);
            if (add.isChecked() || remove.isChecked())
                dto.addPileStructureAnnotationDTO(psa);
        }
        return dto;
    }

    private class CreateSuggestedTermClickListener implements ClickListener {

        private ExpressedTermDTO expressedTerm;

        public CreateSuggestedTermClickListener(ExpressedTermDTO expressedTerm) {
            this.expressedTerm = expressedTerm;
        }

        public void onClick(Widget widget) {
            //Window.alert("Create new post-composed term: " + expressedTerm.getComposedTerm());
            curationRPCAsync.createPileStructure(expressedTerm, publicationID, new CreatePileStructureCallback());
        }
    }

    private class CreatePileStructureCallback implements AsyncCallback<PileStructureDTO> {

        public void onFailure(Throwable throwable) {
            if (throwable instanceof PileStructureExistsException) {
                errorMessage.setText(throwable.getMessage());
            }
            errorMessage.setText(throwable.getMessage());
        }

        public void onSuccess(PileStructureDTO pileStructure) {
            //Window.alert("Success");
            displayedStructures.add(pileStructure);
            Collections.sort(displayedStructures);
            createStructureTable();
            updateFigureAnnotations(selectedFigureAnnotations);
            suggestionDiv.setVisible(false);
            clearErrorMessages();
        }
    }

    private class PileStructureClickListener implements ClickListener {

        private ExpressedTermDTO expressedTerm;
        private PileEntity pileEntity;

        public PileStructureClickListener(ExpressedTermDTO expressedTerm, PileEntity pileEntity) {
            this.expressedTerm = expressedTerm;
            this.pileEntity = pileEntity;
        }

        public void onClick(Widget widget) {
            //Window.alert("Success");
            if (pileEntity == PileEntity.SUPER_TERM)
                populateConstructionZoneSuperterm(expressedTerm.getSupertermName(), expressedTerm.getSupertermOboID(),
                        expressedTerm.getSubtermName(), expressedTerm.getSubtermOboID());
            if (pileEntity == PileEntity.SUBTERM)
                populateConstructionZoneSubterm(expressedTerm.getSupertermName(), expressedTerm.getSupertermOboID(),
                        expressedTerm.getSubtermName(), expressedTerm.getSubtermOboID());
        }

    }

    enum PileEntity {
        SUPER_TERM, SUBTERM, QUALITY
    }

    /**
     * Call two external JS functions that talk to the construction zone and use phenote
     * until we convert that partinto GWT as well.
     *
     * @param superTerm      super term
     * @param superTermOboID super term OBO id
     * @param subTerm        subterm
     * @param subTermOboID   subterm OBO id
     */
    private native void populateConstructionZoneSuperterm(String superTerm, String superTermOboID, String subTerm, String subTermOboID)/*-{
        $wnd.setTerms(superTerm,superTermOboID,subTerm,subTermOboID);
        $wnd.getTermInfo(superTermOboID,superTerm, 'superterm');
    }-*/;

    /**
     * Call two external JS functions that talk to the construction zone and use phenote
     * until we convert that partinto GWT as well.
     *
     * @param superTerm      super term
     * @param superTermOboID super term OBO id
     * @param subTerm        subterm
     * @param subTermOboID   subterm OBO id
     */
    private native void populateConstructionZoneSubterm(String superTerm, String superTermOboID, String subTerm, String subTermOboID)/*-{
        $wnd.setTerms(superTerm,superTermOboID,subTerm,subTermOboID);
        $wnd.getTermInfo(subTermOboID,subTerm, 'subterm');
    }-*/;

    public void addNewPileStructure(String publicationID, String superterm, String subterm, String ontology) {
        ExpressedTermDTO expressedTerm = new ExpressedTermDTO();
        expressedTerm.setSupertermName(superterm);
        expressedTerm.setSubtermName(subterm);
        expressedTerm.setSubtermOntology(ontology);
        //Window.alert("Create new post-composed pile structure: " + expressedTerm.getComposedTerm());
        curationRPCAsync.createPileStructure(expressedTerm, publicationID, new CreatePileStructureCallback());
    }

    public class UnorderedList extends ComplexPanel {
        public UnorderedList() {
            setElement(DOM.createElement("ul"));
        }

        public void add(Widget w) {
            super.add(w, getElement());
        }

        public void insert(Widget w, int beforeIndex) {
            super.insert(w, getElement(), beforeIndex, true);
        }

    }

    public class ListItem extends ComplexPanel implements HasText {
        public ListItem() {
            setElement(DOM.createElement("li"));
        }

        public void add(Widget w) {
            super.add(w, getElement());
        }

        public void insert(Widget w, int beforeIndex) {
            super.insert(w, getElement(), beforeIndex, true);
        }

        public String getText() {
            return DOM.getInnerText(getElement());
        }

        public void setText(String text) {
            DOM.setInnerText(getElement(), (text == null) ? "" : text);
        }

    }

}
