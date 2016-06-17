package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.Set;

public class ConditionAddView extends AbstractViewComposite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    private ConditionAddPresenter presenter;

    @UiTemplate("ConditionAddView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ConditionAddView> {
    }

    public ConditionAddView() {
        initWidget(binder.createAndBindUi(this));
    }

    @UiField
    Image loadingImage;
    @UiField
    Grid dataTable;
    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    Button createExperimentConditionButton;
    @UiField
    StringListBox experimentSelectionList;
    @UiField
    TermEntry zecoTermEntry;
    @UiField
    TermInfoComposite termInfoBox;
    @UiField
    TermEntry chebiTermEntry;
    @UiField
    Button resetButton;
    @UiField
    TermEntry goCcTermEntry;
    @UiField
    TermEntry aoTermEntry;
    @UiField
    TermEntry taxonTermEntry;

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }

    @UiHandler("resetButton")
    void onClickReset(@SuppressWarnings("unused") ClickEvent event) {
        presenter.resetGUI();
    }

    @UiHandler("createExperimentConditionButton")
    void onClickCreateCondition(@SuppressWarnings("unused") ClickEvent event) {
        presenter.createCondition();
    }


    protected void addCondition(EnvironmentDTO experimentDTO, ConditionDTO dto, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        int col = 0;
        dataTable.setText(row, col++, "");
        if (experimentDTO != null)
            dataTable.setText(row, col++, experimentDTO.getName());
        else
            dataTable.setText(row, col++, "");
            dataTable.setText(row, col++, dto.getName());
    }

    public void emptyDataTable() {
        dataTable.resizeRows(2);
    }

    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    public void removeAllDataRows() {
        dataTable.resizeRows(1);
    }

    protected void endTableUpdate() {
        int rows = dataTable.getRowCount() + 3;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
/*
        dataTable.setWidget(lastRow, col++, updateConditionNameButton);
        dataTable.setWidget(lastRow, col++, conditionNameAddBox);
        dataTable.setWidget(lastRow, col, deleteConditionButton);
*/

    }

    private void addDeleteButton(int elementIndex, ClickHandler clickHandler, String title, String zdbID) {
        DeleteImage deleteImage;
        deleteImage = new DeleteImage(title);
        deleteImage.setTitle(deleteImage.getTitle() + ": " + zdbID);
        deleteImage.addClickHandler(clickHandler);
        dataTable.setWidget(elementIndex + 1, 5, deleteImage);
    }

    public void resetUI() {
        errorLabel.clearAllErrors();
//        conditionNameAddBox.setText("");

    }

    public void setPresenter(ConditionAddPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Set<IsDirtyWidget> getValueFields() {
        return null;
    }


}



