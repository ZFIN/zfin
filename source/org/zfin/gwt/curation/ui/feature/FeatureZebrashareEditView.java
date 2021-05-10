package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.ui.feature.FeatureZebrashareEditView;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

public class FeatureZebrashareEditView extends Composite {

    private static FeatureZebrashareEditView.MyUiBinder binder = GWT.create(FeatureZebrashareEditView.MyUiBinder.class);

    @UiTemplate("FeatureZebrashareEditView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureZebrashareEditView> {
    }

    public FeatureZebrashareEditView() {
        initWidget(binder.createAndBindUi(this));
    }
    private FeatureZebrashareEditPresenter presenter;
    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    Label noneDefined;
    @UiField
    Grid dataTable;


    // elementIndex starts with 0
    // but the grid starts with 1
    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }

    protected void addFeature(FeatureDTO feature, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        if (feature == null) {
            dataTable.setText(row, 0, "");
            return;
        }
        Anchor featureAnchor = new Anchor(SafeHtmlUtils.fromTrustedString(feature.getName()), "/" + "action/zebrashare/line-edit/"+feature.getZdbID());
        featureAnchor.setTitle(feature.getZdbID());
        dataTable.setWidget(row, 0, featureAnchor);

    }

    public void emptyDataTable() {
        dataTable.resizeRows(1);
    }


    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    public Label getNoneDefined() {
        return noneDefined;
    }
    public void setPresenter(FeatureZebrashareEditPresenter presenter) {
        this.presenter = presenter;
    }

}