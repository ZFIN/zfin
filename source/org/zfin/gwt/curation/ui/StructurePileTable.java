package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ListItem;
import org.zfin.gwt.root.ui.UnorderedList;
import org.zfin.gwt.root.util.StageRangeIntersection;
import org.zfin.gwt.root.util.StageRangeUnion;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display Table for the structure pile.
 */
class StructurePileTable extends ZfinFlexTable {

    private StructureAlternateComposite suggestionBox;
    private static final String ACTION = "action";
    private static final String STRUCTURE_CONSTRUCTION_ZONE = "structure-construction-zone";
    protected Label errorLabel;

    private ExpressionSection expressionModule;
    private ConstructionZone pileStructureClickListener;

    private HeaderName[] headerNames;
    // This maps the display table and contains the full object that each
    // row is made up from
    private Map<Integer, PileStructureDTO> displayTableMap = new HashMap<Integer, PileStructureDTO>();
    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private List<PileStructureDTO> displayedStructures;
    private AsyncCallback removeStructureCallBack;
    private AsyncCallback createStructureCallback;
    private String publicationID;

    StructurePileTable(List<PileStructureDTO> displayedStructures, StructureAlternateComposite suggestionDiv, Label errorLabel) {
        super(HeaderName.values().length, -1);
        this.displayedStructures = displayedStructures;
        this.suggestionBox = suggestionDiv;
        this.headerNames = HeaderName.values();
        this.errorLabel = errorLabel;
    }

    public void setExpressionSection(ExpressionSection expressionModule){
        this.expressionModule = expressionModule;
        createStructureTable();
    }

    public void setRemoveStructureCallBack(AsyncCallback removeStructureCallBack) {
        this.removeStructureCallBack = removeStructureCallBack;
    }

    public void setCreateStructureCallback(AsyncCallback createStructureCallback) {
        this.createStructureCallback = createStructureCallback;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public void createStructureTable() {
        clearTable();
        displayTableMap.clear();
        // header row index = 0
        createTableHeader();
        int rowIndex = 1;
        //Window.alert("Experiment List Size: " + experiments.size());
        // first element is an odd group element
        int groupIndex = 1;
        for (PileStructureDTO structure : displayedStructures) {
            // put the object in the map for later retrieval
            displayTableMap.put(rowIndex, structure);
            //Window.alert("Experiment: " + experiment.getGeneName());
            String radioButtonName = ACTION + structure.getZdbID();
            RadioButton nothing = new RadioButton(radioButtonName);
            nothing.setValue(true);
            nothing.setVisible(false);
            setWidget(rowIndex, HeaderName.NOTHING.getIndex(), nothing);
            //displayTable.getCellFormatter().setWidth(rowIndex, HeaderName.NOTHING.getIndex(), "0");
            RadioButton remove = new RadioButton(radioButtonName);
            setWidget(rowIndex, HeaderName.REMOVE_FROM_EXPRESSION.getIndex(), remove);
            RadioButton add = new RadioButton(radioButtonName);
            getCellFormatter().setWidth(rowIndex, HeaderName.REMOVE_FROM_EXPRESSION.getIndex(), "15");
            setWidget(rowIndex, HeaderName.ADD.getIndex(), add);
            getCellFormatter().setWidth(rowIndex, HeaderName.ADD.getIndex(), "15");
            CheckBox checkBox = new CheckBox();
            setWidget(rowIndex, HeaderName.MODIFIER.getIndex(), checkBox);
            getCellFormatter().setWidth(rowIndex, HeaderName.MODIFIER.getIndex(), "20");
            HorizontalPanel postComposedTerm = new HorizontalPanel();
            ExpressedTermDTO expressedTerm = structure.getExpressedTerm();
            postComposedTerm.setTitle(expressedTerm.getUniqueID());
            createStructureElement(postComposedTerm, expressedTerm);
            setWidget(rowIndex, HeaderName.STRUCTURE.getIndex(), postComposedTerm);
            checkBox.addClickHandler(new NotClickHandler(rowIndex, structure));
            add.addClickHandler(new AddActionButtonListener(rowIndex, structure));
            Label stage = new Label(structure.getStageRange());
            setWidget(rowIndex, HeaderName.STAGE.getIndex(), stage);
            Button delete = new Button("X");
            String title = createDeleteButtonTitle(structure);
            delete.setTitle(title);
            delete.addClickHandler(new RemovePileStructureClickHandler(structure, errorLabel, expressionModule, removeStructureCallBack));
            setWidget(rowIndex, HeaderName.REMOVE.getIndex(), delete);
            setRowStyle(rowIndex, groupIndex);
            rowIndex++;
        }
        // add horizontal line
        getFlexCellFormatter().setColSpan(rowIndex, 0, HeaderName.getHeaderNames().length);
        HTML html = new HTML("<hr/>");
        setWidget(rowIndex, 0, html);
    }

    protected Map<Integer, PileStructureDTO> getDisplayTableMap() {
        return displayTableMap;
    }

    public void onClick(ClickEvent event) {
        // do not rotate radio button if no expression is selected.
        if (expressionModule.getSelectedExpressions() == null || expressionModule.getSelectedExpressions().isEmpty())
            return;

        HTMLTable.Cell htmlCell = getCellForEvent(event);
        int row = htmlCell.getRowIndex();
        int cell = htmlCell.getCellIndex();
        //Window.alert(row + " : Event: " + event);
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
        //Window.alert("nothing: "+ nothing.getValue());
        if (nothing.getValue()) {
            add.setValue(true);
        } else if (add.getValue()) {
            remove.setValue(true);
        } else {
            nothing.setValue(true);
        }
        PileStructureDTO selectedPileStructure = displayTableMap.get(row);
        checkNeedForAlternativeStructures(selectedPileStructure, row);

    }

    protected RadioButton getNothingRadioButton(int row) {
        return (RadioButton) getWidget(row, HeaderName.NOTHING.getIndex());
    }

    protected RadioButton getAddRadioButton(int row) {
        return (RadioButton) getWidget(row, HeaderName.ADD.getIndex());
    }

    protected RadioButton getRemoveRadioButton(int row) {
        return (RadioButton) getWidget(row, HeaderName.REMOVE_FROM_EXPRESSION.getIndex());
    }

    protected CheckBox getNotCheckBox(int row) {
        return (CheckBox) getWidget(row, HeaderName.MODIFIER.getIndex());
    }

    private String createDeleteButtonTitle(PileStructureDTO structure) {
        String title = structure.getZdbID();
        title += ":";
        title += structure.getCreator();
        title += ":";
        title += structure.getDate().toString().substring(0, 20);
        return title;
    }

    private List<ExpressionFigureStageDTO> getSelectedExpressions() {
        return expressionModule.getSelectedExpressions();
    }

    protected void checkNeedForAlternativeStructures(PileStructureDTO selectedPileStructure, int row) {
        // check if there is at least one figure annotation selected.
        // if structure is not available, i.e. out of the stage range check if there is an ancestor (develops_from)
        // or a descendant (develops_to) structure that could be used instead.
        RadioButton nothing = getNothingRadioButton(row);
        RadioButton add = getAddRadioButton(row);
        if (!add.getValue())
            return;

        if (!getSelectedExpressions().isEmpty() && add.getValue()) {
            StageRangeUnion union = new StageRangeUnion(expressionModule.getSelectedExpressions());
            StageRangeIntersection intersection = new StageRangeIntersection(union.getStart(), union.getEnd());
            if (!intersection.isFullOverlap(selectedPileStructure.getStart(), selectedPileStructure.getEnd())) {
                suggestionBox.setVisible(true);
                noStageOverlapTitle(selectedPileStructure.getExpressedTerm(), intersection);
                curationRPCAsync.getTermsWithStageOverlap(selectedPileStructure, intersection,
                        new StageOverlapTermsCallback(selectedPileStructure));
                // set action button to 'nothing'. We do not allow to add a structure without stage overlap.
                nothing.setValue(true);
            }
        }
    }

    private void createStructureElement(HorizontalPanel postcomposedTerm, ExpressedTermDTO expressedTerm) {
        Hyperlink superterm = new Hyperlink(expressedTerm.getSupertermName(), STRUCTURE_CONSTRUCTION_ZONE);
        superterm.addClickHandler(new InternalPileStructureClickHandler(expressedTerm, PostComposedPart.SUPERTERM));
        postcomposedTerm.add(superterm);
        Hyperlink subTerm;
        if (expressedTerm.getSubtermID() != null) {
            Label colon = new Label(" : ");
            postcomposedTerm.add(colon);
            subTerm = new Hyperlink(expressedTerm.getSubtermName(), STRUCTURE_CONSTRUCTION_ZONE);
            subTerm.addClickHandler(new InternalPileStructureClickHandler(expressedTerm, PostComposedPart.SUBTERM));
            postcomposedTerm.add(subTerm);
        }
    }


    /**
     * Set table header row, assuming it is rowIndex = 0;
     */
    @Override
    public void setHeaderRow() {
        int rowIndex = 0;
        for (HeaderName name : headerNames) {
            if (name.getIndex() == 0) {
                // do nothing
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
            } else if (name.getIndex() == 3) {
                setText(rowIndex, name.index, name.getName());
                getCellFormatter().setStyleName(rowIndex, name.index, WidgetUtil.RED_MODIFIER);
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
        if (terms == null || terms.isEmpty())
            return;

        int numOfRows = getRowCount();
        for (int row = 1; row < numOfRows - 1; row++) {
            if (!isCellPresent(row, HeaderName.STRUCTURE.getIndex()))
                continue;

            Widget widget = getWidget(row, HeaderName.STRUCTURE.getIndex());
            if (widget instanceof HorizontalPanel) {
                HorizontalPanel structurePanel = (HorizontalPanel) widget;
                CheckBox modifier = (CheckBox) getWidget(row, HeaderName.MODIFIER.getIndex());
                for (ExpressedTermDTO term : terms) {
                    if (structurePanel.getTitle().equals(term.getUniqueID())) {
                        RadioButton addButton = (RadioButton) getWidget(row, HeaderName.ADD.getIndex());
                        addButton.setValue(true);
                        // set modifier
                        if (term.isExpressionFound())
                            modifier.setValue(false);
                        else {
                            modifier.setValue(true);
                        }
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
                doNothingButton.setValue(true);
            } else
                break;
            CheckBox modifier = (CheckBox) getWidget(row, HeaderName.MODIFIER.getIndex());
            modifier.setValue(false);
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
        // make red if structure is not expressed
        setNotExpressedStyle(row);
    }

    /**
     * Mark the post-composed term red if it is not expressed. Otherwise use default style.
     *
     * @param row row number
     */
    private void setNotExpressedStyle(int row) {
        CheckBox notExpressedCheckBox = getNotCheckBox(row);
        Hyperlink superTerm = getSuperterm(row);
        Hyperlink subTerm = getSubterm(row);
        if (notExpressedCheckBox.getValue()) {
            WidgetUtil.addOrRemoveCssStyle(superTerm, WidgetUtil.RED_HYPERLINK, true);
            if (subTerm != null)
                WidgetUtil.addOrRemoveCssStyle(subTerm, WidgetUtil.RED_HYPERLINK, true);
        } else {
            WidgetUtil.addOrRemoveCssStyle(superTerm, WidgetUtil.RED_HYPERLINK, false);
            if (subTerm != null)
                WidgetUtil.addOrRemoveCssStyle(subTerm, WidgetUtil.RED_HYPERLINK, false);
        }
    }

    private Hyperlink getSuperterm(int row) {
        Widget widget = getWidget(row, HeaderName.STRUCTURE.getIndex());
        if (!(widget instanceof HorizontalPanel))
            return null;

        HorizontalPanel structurePanel = (HorizontalPanel) widget;
        return (Hyperlink) structurePanel.getWidget(0);
    }

    private Hyperlink getSubterm(int row) {
        Widget widget = getWidget(row, HeaderName.STRUCTURE.getIndex());
        if (!(widget instanceof HorizontalPanel))
            return null;

        HorizontalPanel structurePanel = (HorizontalPanel) widget;
        int numOfWidgets = structurePanel.getWidgetCount();
        if (numOfWidgets > 1)
            return (Hyperlink) structurePanel.getWidget(2);
        else
            return null;
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

    public void onNotCheckBoxClick(int row) {
        CheckBox notCheckBox = (CheckBox) getWidget(row, HeaderName.MODIFIER.getIndex());
        if (notCheckBox.getValue())
            notCheckBox.setValue(true);
        else
            notCheckBox.setValue(false);

        setNotExpressedStyle(row);
    }

    private void noStageOverlapTitle(ExpressedTermDTO expressedTerm, StageRangeIntersection intersection) {
        suggestionBox.clear();
        StringBuilder message = new StringBuilder("'");
        message.append(expressedTerm.getDisplayName());
        message.append("' has no stage overlap with one or more of the selected expressions: ");
/*
        boolean isMultipleStage = intersection.getStartHours() != intersection.getEndHours();
        message.append("[");
        message.append(intersection.getStart().getName());
        if (isMultipleStage) {
            message.append(",");
            message.append(intersection.getEnd().getName());
        }
        message.append("]");
*/
        HorizontalPanel hor = new HorizontalPanel();
        Label note = new Label(message.toString());
        note.setStyleName("error");
        hor.add(note);
        suggestionBox.add(hor);
    }

    public void setPileStructureClickListener(ConstructionZone pileStructureClickListener) {
        this.pileStructureClickListener = pileStructureClickListener;
    }

    // ********* Click Handler, etc
    private class NotClickHandler implements ClickHandler {

        private RadioButton add;
        private RadioButton nothing;
        private PileStructureDTO pileStructure;
        private int row;

        private NotClickHandler(int row, PileStructureDTO pileStructure) {
            super();
            this.row = row;
            this.pileStructure = pileStructure;
            add = (RadioButton) getWidget(row, HeaderName.ADD.getIndex());
            nothing = (RadioButton) getWidget(row, HeaderName.NOTHING.getIndex());
        }

        public void onClick(ClickEvent event) {
            onNotCheckBoxClick(row);
            if (nothing.getValue()) {
                add.setValue(true);
                checkNeedForAlternativeStructures(pileStructure, row);
            }
        }
    }

    private class AddActionButtonListener implements ClickHandler {

        private PileStructureDTO pileStructure;
        private int row;

        private AddActionButtonListener(int row, PileStructureDTO pileStructure) {
            super();
            this.row = row;
            this.pileStructure = pileStructure;
        }

        public void onClick(ClickEvent event) {
            checkNeedForAlternativeStructures(pileStructure, row);
        }
    }

    private class StageOverlapTermsCallback extends ZfinAsyncCallback<List<RelatedPileStructureDTO>> {

        private PileStructureDTO selectedPileStructure;

        StageOverlapTermsCallback(PileStructureDTO selectedPileStructure) {
            super("Error while trying to find terms that have an overlap with a given stage", errorLabel);
            this.selectedPileStructure = selectedPileStructure;
        }

        public void onSuccess(List<RelatedPileStructureDTO> terms) {
            //Window.alert("Success");
            // update the expression list
            if (terms != null && !terms.isEmpty()) {
                suggestionBox.setVisible(true);
                UnorderedList ul = new UnorderedList();
                VerticalPanel verticalPanel = new VerticalPanel();
                verticalPanel.add(new Label("Alternate structures with overlapping stages:"));
                List<RelatedPileStructureDTO> existingStructures = getExistingStructures(terms);
                List<RelatedPileStructureDTO> newStructures = getNewStructures(terms);

                // add existing structures
                if (!existingStructures.isEmpty()) {
                    createListItems(ul, existingStructures);
                }
                // add new structures
                if (!newStructures.isEmpty()) {
                    ListItem itemTwo = new ListItem();
                    ul.add(itemTwo);
                    itemTwo.add(new Label("not yet in pile, click to add:"));
                    UnorderedList termList = new UnorderedList();
                    for (RelatedPileStructureDTO structure : newStructures) {
                        FlexTable infoTable = new FlexTable();
                        infoTable.setCellPadding(5);
                        setSubterm(structure);
                        Hyperlink term = new Hyperlink(structure.getExpressedTerm().getDisplayName(), "show");
                        term.addClickHandler(new CreateSuggestedTermClickHandler(structure.getExpressedTerm(), publicationID, createStructureCallback));
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
                suggestionBox.add(verticalPanel);
            } else {
                suggestionBox.add(new Label("No alternate structures on immediate children or parents found."));
            }
            Hyperlink hideSuggestionBox = new Hyperlink("Hide Suggestions", "HideSuggestionBox");
            hideSuggestionBox.addClickHandler(new HideSuggestionBox());
            suggestionBox.add(hideSuggestionBox);
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
                infoTable.setWidget(0, 1, new Label(structure.getExpressedTerm().getDisplayName()));
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

            for (PileStructureDTO term : getDisplayTableMap().values()) {
                if (term.getExpressedTerm().getSupertermName().equals(structure.getExpressedTerm().getSupertermName()))
                    return true;
            }
            return false;
        }

        public void onFailureCleanup() {

        }
    }

    private class InternalPileStructureClickHandler implements ClickHandler {

        private ExpressedTermDTO expressedTerm;
        private PostComposedPart pileEntity;

        public InternalPileStructureClickHandler(ExpressedTermDTO expressedTerm, PostComposedPart pileEntity) {
            this.expressedTerm = expressedTerm;
            this.pileEntity = pileEntity;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            pileStructureClickListener.prepopulateConstructionZone(expressedTerm, pileEntity);
        }

    }


    enum HeaderName {
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
            return HeaderName.values();
        }
    }

    private class HideSuggestionBox implements ClickHandler {

        public void onClick(ClickEvent clickEvent) {
            suggestionBox.setVisible(false);
        }
    }
}
