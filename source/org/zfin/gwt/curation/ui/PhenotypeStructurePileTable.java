package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display Table for the structure pile.
 */
class PhenotypeStructurePileTable extends ZfinFlexTable {

    private StructureAlternateComposite suggestionBox;
    private static final String ACTION = "action";
    private static final String STRUCTURE_CONSTRUCTION_ZONE = "structure-construction-zone";
    protected ErrorHandler errorLabel;

    private ExpressionSection expressionModule;
    private ConstructionZone pileStructureClickListener;

    private HeaderName[] headerNames;
    // This maps the display table and contains the full object that each
    // row is made up from
    private Map<Integer, PhenotypePileStructureDTO> displayTableMap = new HashMap<Integer, PhenotypePileStructureDTO>();
    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private List<PhenotypePileStructureDTO> displayedStructures;
    private AsyncCallback removeStructureCallBack;
    private AsyncCallback createStructureCallback;
    private String publicationID;
    private Enum headerEnumeration;

    PhenotypeStructurePileTable(List<PhenotypePileStructureDTO> displayedStructures, StructureAlternateComposite suggestionDiv, ErrorHandler errorLabel) {
        super(HeaderName.values().length, -1);
        this.displayedStructures = displayedStructures;
        this.suggestionBox = suggestionDiv;
        this.headerNames = HeaderName.values();
        this.errorLabel = errorLabel;
    }

    public void setHeaderNames(Enum enumeration) {
        headerEnumeration = enumeration;
    }

    public void setExpressionSection(ExpressionSection expressionModule) {
        this.expressionModule = expressionModule;
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
        for (PhenotypePileStructureDTO structure : displayedStructures) {
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
            HorizontalPanel entity = new HorizontalPanel();
            PhenotypeStatementDTO expressedTerm = structure.getPhenotypeTerm();
            entity.setTitle(expressedTerm.getUniqueID());
            createEntityStructureElement(entity, expressedTerm);
            setWidget(rowIndex, HeaderName.ENTITY.getIndex(), entity);
            HorizontalPanel qualityTerm = new HorizontalPanel();
            createQualityElement(qualityTerm, expressedTerm);
            setWidget(rowIndex, HeaderName.QUALITY.getIndex(), qualityTerm);
            HorizontalPanel relatedEntity = new HorizontalPanel();
            relatedEntity.setTitle(expressedTerm.getUniqueID());
            createRelatedEntityStructureElement(relatedEntity, expressedTerm);
            setWidget(rowIndex, HeaderName.RELATED_ENTITY.getIndex(), relatedEntity);
            add.addClickHandler(new AddActionButtonListener(rowIndex, structure));
            Widget tagLabel = new HTML(structure.getPhenotypeTerm().getTag());
            setWidget(rowIndex, HeaderName.TAG.getIndex(), tagLabel);
            Button delete = new Button("X");
            String title = createDeleteButtonTitle(structure);
            delete.setTitle(title);
            delete.addClickHandler(new RemovePhenotypePileStructureClickHandler(structure, errorLabel, expressionModule, removeStructureCallBack));
            setWidget(rowIndex, HeaderName.REMOVE.getIndex(), delete);
            setRowStyle(rowIndex, groupIndex);
            rowIndex++;
        }
        // add horizontal line
        getFlexCellFormatter().setColSpan(rowIndex, 0, HeaderName.getHeaderNames().length);
        HTML html = new HTML("<hr/>");
        setWidget(rowIndex, 0, html);
    }

    protected Map<Integer, PhenotypePileStructureDTO> getDisplayTableMap() {
        return displayTableMap;
    }

    public void onClick(ClickEvent event) {
        // do not rotate radio button if no expression is selected.
        if (expressionModule.getSelectedExpressions() == null || expressionModule.getSelectedExpressions().isEmpty())
            return;

        Cell htmlCell = getCellForEvent(event);
        int row = htmlCell.getRowIndex();
        int cell = htmlCell.getCellIndex();
        //Window.alert(row + " : Event: " + event);
        int firstCell = 0;
        Widget widget = getWidget(row, firstCell);
        // check if the row has a structure
        if (widget == null || !(widget instanceof RadioButton))
            return;
        // if checkbox is checked or removed do not rotate radio buttons
        if (cell == HeaderName.REMOVE.getIndex())
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
        PhenotypePileStructureDTO selectedPileStructure = displayTableMap.get(row);

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

    private String createDeleteButtonTitle(PhenotypePileStructureDTO structure) {
        String title = structure.getZdbID();
        title += ":";
        title += structure.getCreator();
        title += ":";
        title += structure.getDate().toString().substring(0, 20);
        return title;
    }

    private void createEntityStructureElement(HorizontalPanel postcomposedTerm, PhenotypeStatementDTO phenotypeTermDTO) {
        Hyperlink superterm = new Hyperlink(phenotypeTermDTO.getEntity().getSuperTerm().getTermName(), STRUCTURE_CONSTRUCTION_ZONE);
        superterm.addClickHandler(new InternalPileStructureClickHandler(phenotypeTermDTO, EntityPart.ENTITY_SUPERTERM));
        postcomposedTerm.add(superterm);
        Hyperlink subTerm;
        if (phenotypeTermDTO.getEntity().getSubTerm() != null) {
            Label colon = new Label(" : ");
            postcomposedTerm.add(colon);
            subTerm = new Hyperlink(phenotypeTermDTO.getEntity().getSubTerm().getTermName(), STRUCTURE_CONSTRUCTION_ZONE);
            subTerm.addClickHandler(new InternalPileStructureClickHandler(phenotypeTermDTO, EntityPart.ENTITY_SUBTERM));
            postcomposedTerm.add(subTerm);
        }
    }

    private void createRelatedEntityStructureElement(HorizontalPanel postcomposedTerm, PhenotypeStatementDTO phenotypeTermDTO) {
        if (phenotypeTermDTO.getRelatedEntity() == null || phenotypeTermDTO.getRelatedEntity().getSuperTerm() == null)
            return;

        Hyperlink superterm = new Hyperlink(phenotypeTermDTO.getRelatedEntity().getSuperTerm().getTermName(), STRUCTURE_CONSTRUCTION_ZONE);
        superterm.addClickHandler(new InternalPileStructureClickHandler(phenotypeTermDTO, EntityPart.RELATED_ENTITY_SUPERTERM));
        postcomposedTerm.add(superterm);
        Hyperlink subTerm;
        if (phenotypeTermDTO.getRelatedEntity().getSubTerm() != null) {
            Label colon = new Label(" : ");
            postcomposedTerm.add(colon);
            subTerm = new Hyperlink(phenotypeTermDTO.getRelatedEntity().getSubTerm().getTermName(), STRUCTURE_CONSTRUCTION_ZONE);
            subTerm.addClickHandler(new InternalPileStructureClickHandler(phenotypeTermDTO, EntityPart.RELATED_ENTITY_SUBTERM));
            postcomposedTerm.add(subTerm);
        }
    }

    private void createQualityElement(Panel qualityTerm, PhenotypeStatementDTO phenotypeTerm) {
        Hyperlink quality = new Hyperlink(phenotypeTerm.getQuality().getName(), STRUCTURE_CONSTRUCTION_ZONE);
        quality.addClickHandler(new InternalPileStructureClickHandler(phenotypeTerm, EntityPart.QUALITY));
        qualityTerm.add(quality);
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
            } else {
                setText(rowIndex, name.index, name.getName());
                getCellFormatter().setStyleName(rowIndex, name.index, WidgetUtil.BOLD);
            }
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
            HorizontalPanel structurePanel = (HorizontalPanel) getWidget(row, HeaderName.ENTITY.getIndex());
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
    public void setCommonStructures(List<PhenotypeStatementDTO> terms) {
        resetActionButtons();
        if (terms == null || terms.isEmpty())
            return;

        int numOfRows = getRowCount();
        for (int row = 1; row < numOfRows - 1; row++) {
            if (!isCellPresent(row, HeaderName.ENTITY.getIndex()))
                continue;

            Widget widget = getWidget(row, HeaderName.ENTITY.getIndex());
            if (widget instanceof HorizontalPanel) {
                HorizontalPanel structurePanel = (HorizontalPanel) widget;
                for (PhenotypeStatementDTO term : terms) {
                    if (structurePanel.getTitle().equals(term.getUniqueID())) {
                        RadioButton addButton = (RadioButton) getWidget(row, HeaderName.ADD.getIndex());
                        addButton.setValue(true);
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
            unsetColor(row);
            highlightStructure(row, false);
        }
    }

    public void highlightStructure(int row, boolean highlight) {
        Widget widget = getWidget(row, HeaderName.ENTITY.getIndex());
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

    private Hyperlink getSuperterm(int row) {
        Widget widget = getWidget(row, HeaderName.ENTITY.getIndex());
        if (!(widget instanceof HorizontalPanel))
            return null;

        HorizontalPanel structurePanel = (HorizontalPanel) widget;
        return (Hyperlink) structurePanel.getWidget(0);
    }

    private Hyperlink getSubterm(int row) {
        Widget widget = getWidget(row, HeaderName.ENTITY.getIndex());
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
        Widget widget = getWidget(row, HeaderName.ENTITY.getIndex());
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

    public void setPileStructureClickListener(ConstructionZone pileStructureClickListener) {
        this.pileStructureClickListener = pileStructureClickListener;
    }

    // ********* Click Handler, etc

    private class AddActionButtonListener implements ClickHandler {

        private PhenotypePileStructureDTO pileStructure;
        private int row;

        private AddActionButtonListener(int row, PhenotypePileStructureDTO pileStructure) {
            super();
            this.row = row;
            this.pileStructure = pileStructure;
        }

        public void onClick(ClickEvent event) {
        }
    }

    private class InternalPileStructureClickHandler implements ClickHandler {

        private PhenotypeStatementDTO phenotypeTerm;
        private EntityPart pileEntity;

        public InternalPileStructureClickHandler(PhenotypeStatementDTO phenotypeTerm, EntityPart pileEntity) {
            this.phenotypeTerm = phenotypeTerm;
            this.pileEntity = pileEntity;
        }

        public void onClick(ClickEvent event) {
            DOM.eventCancelBubble(Event.getCurrentEvent(), true);
            pileStructureClickListener.prepopulateConstructionZoneWithPhenotype(phenotypeTerm, pileEntity);
        }

    }


    enum HeaderName {
        NOTHING(0, ""),
        REMOVE_FROM_EXPRESSION(1, ""),
        ADD(2, ""),
        ENTITY(3, "Entity"),
        QUALITY(4, "Quality"),
        RELATED_ENTITY(5, "Related Entity"),
        TAG(6, "Tag"),
        REMOVE(7, "Remove");

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