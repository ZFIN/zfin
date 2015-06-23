package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.ShowHideWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    ZfinFlexTable fishListTable;

    private String publicationID;

    public void setData(List<FishDTO> fishList) {
        initFishListTable();
        int index = 1;
        int groupIndex = 0;
        int rowIndex = 1;
        for (FishDTO fish : fishList) {
            int col = 0;
            Anchor html = new Anchor(SafeHtmlUtils.fromTrustedString(fish.getName()), "/" + fish.getZdbID());
            fishListTable.setWidget(index, col++, html);
            InlineHTML handle = new InlineHTML(fish.getHandle());
            handle.setTitle(fish.getZdbID());
            fishListTable.setWidget(index, col++, handle);
            DeleteImage deleteFish = new DeleteImage("/action/infrastructure/deleteRecord/" + fish.getZdbID(), "Delete Fish");
            fishListTable.getCellFormatter().setHorizontalAlignment(index, col, HasHorizontalAlignment.ALIGN_CENTER);
            fishListTable.setWidget(index++, col++, deleteFish);
            groupIndex = fishListTable.setRowStyle(rowIndex++, null, fish.getZdbID(), groupIndex);

        }
    }

    private void initFishListTable() {
        int col = 0;
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Fish Name");
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Display Handle");
        fishListTable.getCellFormatter().setStyleName(0, col, "bold");
        fishListTable.setText(0, col++, "Delete");
        fishListTable.getRowFormatter().setStyleName(0, "table-header");
    }

    public Label getNoneDefined() {
        return noneDefined;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }


}
