package org.zfin.gwt.curation.ui.fish;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.util.ShowHideWidget;
import org.zfin.gwt.root.util.WidgetUtil;

/**
 * Table of associated genotypes
 */
public abstract class SingleGridBaseComposite extends Composite {

    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Image loadingImage;
    @UiField
    Grid dataTable;

    protected ShowHideWidget sectionVisibilityToggle;

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }


    protected void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    /**
     * Returns the last row index.
     *
     * @return
     */
    protected int getLastRow() {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        dataTable.getRowFormatter().setStyleName(lastRow, "table-header");
        return lastRow;
    }

    public void removeAllDataRows() {
        dataTable.resizeRows(1);
    }

    public void resetGUI() {
        errorLabel.setError("");
    }

    public void setError(String message) {
        errorLabel.setError(message);
    }

    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }
}
