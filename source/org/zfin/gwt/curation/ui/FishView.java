package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

/**
 * Table of associated genotypes
 */
public class FishView extends Composite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);

    @UiTemplate("FishView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FishView> {
    }

    public FishView() {
        initWidget(binder.createAndBindUi(this));
    }

    @UiField
    Label noneDefined;
    @UiField
    Grid dataTable;

    // elementIndex starts with 0
    // but the grid starts with 1
    protected void addFish(FishDTO fish, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        if (fish == null) {
            dataTable.setText(row, 0, "");
            return;
        }
        Anchor fishAnchor = new Anchor(SafeHtmlUtils.fromTrustedString(fish.getName()), "/" + fish.getZdbID());
        fishAnchor.setTitle(fish.getZdbID());
        dataTable.setWidget(row, 0, fishAnchor);
        InlineHTML handle = new InlineHTML(fish.getHandle());
        handle.setTitle(fish.getZdbID());
        dataTable.setWidget(row, 1, handle);
/*
        HTML html = new HTML("<i class='fa fa-clone' aria-hidden='true'></i>Clone");
        Anchor cloneLink = new Anchor(SafeHtmlUtils.fromTrustedString(html.getHTML()));
        dataTable.setWidget(row, 2, cloneLink);
*/
    }

    public void addDeleteButton(FishDTO fish, int elementIndex) {
        int row = elementIndex + 1;
        DeleteImage deleteFish = new DeleteImage("/action/infrastructure/deleteRecord/" + fish.getZdbID(), "Delete Fish");
        dataTable.setWidget(row, 2, deleteFish);
    }


    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    public Label getNoneDefined() {
        return noneDefined;
    }

}
