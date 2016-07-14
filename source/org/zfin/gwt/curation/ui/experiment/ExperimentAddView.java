package org.zfin.gwt.curation.ui.experiment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.ui.AbstractViewComposite;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.Set;

public class ExperimentAddView extends AbstractViewComposite {

    public static final String STANDARD = "_Standard";

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    private ExperimentAddPresenter presenter;

    @UiTemplate("ExperimentAddView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ExperimentAddView> {
    }

    public ExperimentAddView() {
        initWidget(binder.createAndBindUi(this));
    }

    @UiField
    Image loadingImage;
    @UiField
    Grid dataTable;
    @UiField
    Button addExperimentButton;
    @UiField
    TextBox experimentNameAddBox;
    @UiField
    ShowHideToggle showHideToggle;

    @UiHandler("addExperimentButton")
    void onClickCreateExperiment(@SuppressWarnings("unused") ClickEvent event) {
        presenter.createExperiment();
    }

    @UiHandler("experimentNameAddBox")
    void onKeyDown(@SuppressWarnings("unused") KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            onClickCreateExperiment(null);
        }
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }

    public void addExptTextBox(TextBox exptBox, ExperimentDTO dto, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        dataTable.getColumnFormatter().setWidth(0, "20px");
        dataTable.getColumnFormatter().setWidth(1, "75%");
        dataTable.setWidget(row, 1, exptBox);
        exptBox.setText(dto.getName());
    }

    public void addConstructionRow(int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        dataTable.getRowFormatter().setStyleName(row, "construction");
        dataTable.setWidget(row, 0, addExperimentButton);
        dataTable.setWidget(row, 1, experimentNameAddBox);
    }

    public void addDeleteButton(ExperimentDTO dto, DeleteImage deleteImage, int elementIndex) {
        int row = elementIndex + 1;
        if (!(dto.getIsUsedInExpression()) && !(dto.getIsUsedInPhenotype()) && !(dto.getIsUsedInDisease())) {
            dataTable.setWidget(row, 2, deleteImage);
        }

    }


    public void addUpdateButton(Button updateButton, int elementIndex) {
        int row = elementIndex + 1;
        updateButton.setText("Update");
        dataTable.setWidget(row, 0, updateButton);
    }

    public void emptyDataTable() {
        dataTable.resizeRows(2);
    }

    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    public void setPresenter(ExperimentAddPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Set<IsDirtyWidget> getValueFields() {
        return null;
    }


}



