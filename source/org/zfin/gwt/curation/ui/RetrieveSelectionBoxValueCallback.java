package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.FilterSelectionBoxEntry;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.ui.ZfinModule;

import java.util.List;

public class RetrieveSelectionBoxValueCallback extends ZfinAsyncCallback<List<FilterSelectionBoxEntry>> {

    private ListBox listBox;
    private boolean addAllItem = true;

    public RetrieveSelectionBoxValueCallback(ListBox listBox, ErrorHandler handler, ZfinModule module, AjaxCallEventType eventType) {
        super("Error while reading Filter values", handler, module, eventType);
        this.listBox = listBox;
    }

    public RetrieveSelectionBoxValueCallback(ListBox listBox, boolean addAllItem, ErrorHandler handler) {
        super("Error while reading Filter values", handler);
        this.listBox = listBox;
        this.addAllItem = addAllItem;
    }

    public RetrieveSelectionBoxValueCallback(ListBox listBox, boolean addAllItem, ErrorHandler handler, ZfinModule module, AjaxCallEventType eventType) {
        super("Error while reading Filter values", handler, module, eventType);
        this.listBox = listBox;
        this.addAllItem = addAllItem;
    }

    public RetrieveSelectionBoxValueCallback(ListBox listBox) {
        super("Error while reading Filter values", null);
        this.listBox = listBox;
    }

    public void onSuccess(List<FilterSelectionBoxEntry> valuesDTO) {
        super.onFinish();
        String selectedID = listBox.getSelectedValue();
        listBox.clear();
        int selectedItemIndex = 0;
        if (addAllItem) {
            listBox.addItem("All", "");
            selectedItemIndex = 1;
        }
        for (FilterSelectionBoxEntry featureDTO : valuesDTO) {
            listBox.addItem(featureDTO.getLabel(), featureDTO.getValue());
            if (featureDTO.getValue().equals(selectedID))
                listBox.setSelectedIndex(selectedItemIndex);
            selectedItemIndex++;
        }
    }
}
