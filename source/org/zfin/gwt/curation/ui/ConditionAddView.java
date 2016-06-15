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
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.Set;

public class ConditionAddView extends AbstractViewComposite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    private ExperimentAddPresenter presenter;

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
    Button createExperimentCondition;

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }


    protected void addFish(FishDTO fish, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        if (fish == null) {
            dataTable.setText(row, 0, "");
            return;
        }
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

    public void setPresenter(ExperimentAddPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Set<IsDirtyWidget> getValueFields() {
        return null;
    }


}



