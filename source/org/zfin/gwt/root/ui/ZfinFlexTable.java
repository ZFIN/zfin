package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base zfin flex table. It has the following features:
 * 1) Method to clear all rows
 * 2) Hyperlink logic for 'Clear All' check marks
 * 3) Hyperlink logic to toggle between 'Show checked records only' and 'Show All Records'
 */
public class ZfinFlexTable extends FlexTable implements ClickHandler {

    public static final String CLEAR_ALL = "Clear";

    protected Hyperlink uncheckExperimentsTop = new Hyperlink("C", CLEAR_ALL + " Top");
    protected Hyperlink uncheckExperimentsBottom = new Hyperlink(CLEAR_ALL, CLEAR_ALL + " Bottom");
    protected ToggleHyperlink showSelectedRecords;

    protected int numberOfColumns;
    protected int selectionCheckBoxColumn;
    protected int[] selectionCheckBoxColumns;

    private UncheckAllExperimentsHandler uncheckAllExperimentsHandler = null;

    public
    @UiConstructor
    ZfinFlexTable(int numberOfColumns) {
        super();
        this.numberOfColumns = numberOfColumns;
        addClickHandler(this);
    }

    public ZfinFlexTable(int numberOfColumns, int selectionCheckBoxColumn) {
        this(numberOfColumns);
        this.selectionCheckBoxColumn = selectionCheckBoxColumn;
        if (selectionCheckBoxColumn >= 0)
            uncheckAllExperimentsHandler = new UncheckAllExperimentsHandler();
    }

    public ZfinFlexTable(int numberOfColumns, int[] selectionCheckBoxColumns) {
        super();
        this.numberOfColumns = numberOfColumns;
        this.selectionCheckBoxColumns = selectionCheckBoxColumns;
        if (selectionCheckBoxColumn >= 0)
            uncheckAllExperimentsHandler = new UncheckAllExperimentsHandler();
        addClickHandler(this);
    }

    /**
     * Clear the whole table from the last row to the first row.
     * If you need to keep certain rows override this method.
     */
    protected void clearTable() {
        int rowCount = getRowCount();
        // Note: make sure to remove rows in reverse order
        // otherwise you get random displays of records!
        if (rowCount < 2)
            return;

        for (int i = rowCount - 1; i >= 0; i--) {
            removeRow(i);
        }
    }

    // Returns the group index
    public int setRowStyle(int rowIndex, long currentID, long previousID, int groupIndex) {
        return setRowStyle(rowIndex, ""+currentID, ""+previousID, groupIndex);
    }

    public int setRowStyle(int rowIndex, String currentID, String previousID, int groupIndex) {
        StringBuilder sb = new StringBuilder(50);
        // check even/odd row
        if (rowIndex % 2 == 0)
            sb.append(CssStyles.EVEN.toString());
        else
            sb.append(CssStyles.ODD.toString());

        sb.append(" ");
        if (previousID == null && currentID == null) {
            sb.append(CssStyles.OLDGROUP.toString());
        } else if (previousID == null) {
            sb.append(CssStyles.NEWGROUP.toString());
            groupIndex++;
        } else if (previousID.equals(currentID)) {
            sb.append(CssStyles.OLDGROUP.toString());
        } else {
            sb.append(CssStyles.NEWGROUP.toString());
            groupIndex++;
        }

        // check if odd group or even group
        sb.append(" ");
        if (groupIndex % 2 == 0)
            sb.append(CssStyles.EVENGROUP.toString());
        else
            sb.append(CssStyles.ODDGROUP.toString());

        // add row
        sb.append(" ");
        sb.append(CssStyles.EXPERIMENT_ROW.toString());
        getRowFormatter().setStyleName(rowIndex, sb.toString());
        return groupIndex;
    }

    // Returns the boolean: isOddGroup
    protected int setRowStyle(int rowIndex, int groupIndex) {
        StringBuilder sb = new StringBuilder(50);
        // check even/odd row
        if (rowIndex % 2 == 0) {
            sb.append(CssStyles.EVEN.toString());
            sb.append(" ");
            sb.append(CssStyles.NEWGROUP.toString());
            sb.append(" ");
            sb.append(CssStyles.EVENGROUP.toString());
        } else {
            sb.append(CssStyles.ODD.toString());
            sb.append(" ");
            sb.append(CssStyles.NEWGROUP.toString());
            sb.append(" ");
            sb.append(CssStyles.ODDGROUP.toString());
        }
        //
        sb.append(" ");
        sb.append(CssStyles.EXPERIMENT_ROW.toString());
        sb.append(" ");
        getRowFormatter().setStyleName(rowIndex, sb.toString());
        return groupIndex;
    }

    /**
     * Set a toggle hyperlink at the bottom.
     *
     * @param textTrue  for default (true)
     * @param textFalse for second option (false)
     * @return toggle link
     */
    protected ToggleHyperlink setToggleHyperlink(String textTrue, String textFalse) {
        showSelectedRecords = new ToggleHyperlink(textTrue, textFalse);
        return showSelectedRecords;
    }

    /**
     * Set a toggle hyperlink at the bottom.
     *
     * @return toggle link
     */
    protected void setToggleHyperlink(ToggleHyperlink showSelectedRecords) {
        this.showSelectedRecords = showSelectedRecords;
    }

    /**
     * Set a toggle hyperlink at the bottom.
     *
     * @param clickHandler click handler
     */
    protected void addToggleHyperlinkClickHandler(ClickHandler clickHandler) {
        showSelectedRecords.addClickHandler(clickHandler);
    }

    protected void addClickListenerToClearAllEvent(ClickHandler clickHandler) {
        uncheckExperimentsTop.addClickHandler(clickHandler);
        uncheckExperimentsBottom.addClickHandler(clickHandler);
    }

    protected void createTableHeader() {
        setWidth("100%");
        StringBuilder styleName = new StringBuilder(50);
        styleName.append(CssStyles.SEARCHRESULTS);
        styleName.append(" ");
        styleName.append(CssStyles.GROUPSTRIPES_HOVER.toString());
        addStyleName(styleName.toString());
        getRowFormatter().setStyleName(0, CssStyles.TABLE_HEADER.toString());
        setHeaderRow();
    }

    public void uncheckAllRecords() {
        int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            if (selectionCheckBoxColumns != null) {
                for (int checkBoxColumn : selectionCheckBoxColumns) {
                    //Window.alert("CheckBox: " + checkBoxColumn);
                    if (!uncheckCheckBox(rowIndex, checkBoxColumn))
                        break;
                }
            } else
                uncheckCheckBox(rowIndex, selectionCheckBoxColumn);
        }
        hideClearAllLink();
        showSelectedRecords.uncheckAllRecords();
    }

    private boolean uncheckCheckBox(int rowIndex, int column) {
        Widget widget = getWidget(rowIndex, column);
        if (widget == null)
            return false;
        //Window.alert("Clicker");
        if (!(widget instanceof CheckBox))
            return false;
        CheckBox checkBox = (CheckBox) widget;
        checkBox.setValue(false);
        return true;
    }

    /**
     * Check if at least one experiment is checked. If so, display the 'Clear All' link,
     * otherwise do nothing.
     * Also, display the toggle link as needed.
     */
    public void showHideClearAllLink() {
        int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Widget widget = getWidget(rowIndex, selectionCheckBoxColumn);
            //Window.alert("Clicker");
            if (!(widget instanceof CheckBox))
                continue;
            CheckBox checkBox = (CheckBox) widget;
            if (checkBox.getValue()) {
                showClearAllLink();
                showSelectionTrueLink();
                return;
            }
        }
        hideClearAllLink();
    }

    private void showSelectionTrueLink() {
        showSelectedRecords.setVisible(true);
    }

    protected boolean isAllUnchecked() {
        int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Widget widget = getWidget(rowIndex, selectionCheckBoxColumn);
            //Window.alert("Clicker");
            if (!(widget instanceof CheckBox))
                continue;
            CheckBox checkBox = (CheckBox) widget;
            if (checkBox.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set table header row, assuming it is rowIndex = 0;
     */
    public void setHeaderRow() {
        int rowIndex = 0;
        uncheckExperimentsTop.addClickHandler(uncheckAllExperimentsHandler);
        setWidget(rowIndex, selectionCheckBoxColumn, uncheckExperimentsTop);
    }

    public void createBottomClearAllLinkRow(int rowIndex) {
        uncheckExperimentsBottom.addClickHandler(uncheckAllExperimentsHandler);
        setWidget(rowIndex, 0, uncheckExperimentsBottom);
        getFlexCellFormatter().setColSpan(rowIndex, 0, numberOfColumns);
        getFlexCellFormatter().setHeight(rowIndex, 0, "25");
        //Window.alert("Toggle link exists: " + showSelectedRecords);
        if (showSelectedRecords != null) {
            rowIndex++;
            setWidget(rowIndex, 0, showSelectedRecords);
            getFlexCellFormatter().setColSpan(rowIndex, 0, numberOfColumns);
            getFlexCellFormatter().setHeight(rowIndex, 0, "25");
        }
    }

    public void createBottomClearAllLinkRow(int rowIndex, int columnSpan) {
        uncheckExperimentsBottom.addClickHandler(uncheckAllExperimentsHandler);
        setWidget(rowIndex, 0, uncheckExperimentsBottom);
        getFlexCellFormatter().setColSpan(rowIndex, 0, columnSpan);
        getFlexCellFormatter().setHeight(rowIndex, 0, "25");
        //Window.alert("Toggle link exists: " + showSelectedRecords);
        if (showSelectedRecords != null) {
            rowIndex++;
            setWidget(rowIndex, 0, showSelectedRecords);
            getFlexCellFormatter().setColSpan(rowIndex, 0, columnSpan);
            getFlexCellFormatter().setHeight(rowIndex, 0, "25");
        }
    }

    public void showBuffer(int rowIndex, int columnIndex, Widget widget) {
        setWidget(rowIndex, columnIndex, widget);
    }

    public void showBufferLastRow(int columnIndex, Widget widget) {
        int numberOfRows = getRowCount();
        setWidget(numberOfRows - 1, columnIndex, widget);
    }

    public void showClearAllLink() {
        uncheckExperimentsTop.setVisible(true);
        uncheckExperimentsBottom.setVisible(true);
    }

    protected void hideClearAllLink() {
        uncheckExperimentsTop.setVisible(false);
        uncheckExperimentsBottom.setVisible(false);
    }

    protected void resetShowAllRecords() {


    }

    @Override
    public void onClick(ClickEvent clickEvent) {

    }

// ************************** Handlers

    protected class UncheckAllExperimentsHandler implements ClickHandler {

        public void onClick(ClickEvent clickEvent) {
            uncheckAllRecords();
            showSelectedRecords.hideHyperlink(isAllUnchecked());
        }
    }

    public enum CssStyles {

        EVEN("even"),
        ODD("odd"),
        EVENGROUP("evengroup"),
        ODDGROUP("oddgroup"),
        NEWGROUP("newgroup"),
        OLDGROUP("oldgroup"),
        EXPERIMENT_ROW("experiment-row"),
        SEARCHRESULTS("searchresults"),
        GROUPSTRIPES_HOVER("groupstripes-hover"),
        TABLE_HEADER("table-header");

        private final String value;

        private CssStyles(String name) {
            this.value = name;
        }

        public String toString() {
            return this.value;
        }

        public static CssStyles getType(String type) {
            for (CssStyles t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No Css class named " + type + " found.");
        }

    }

}
