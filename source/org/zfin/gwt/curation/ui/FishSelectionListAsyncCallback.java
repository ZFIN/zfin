package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;
import java.util.Map;

class FishSelectionListAsyncCallback extends ZfinAsyncCallback<List<FishDTO>> {

    Map<String, FishDTO> fishMap;
    SimpleErrorElement errorElement;
    StringListBox listBox;

    public FishSelectionListAsyncCallback(Map<String, FishDTO> fishMap, StringListBox listBox, SimpleErrorElement errorElement) {
        this(errorElement, listBox);
        this.fishMap = fishMap;
    }

    public FishSelectionListAsyncCallback(SimpleErrorElement errorElement, StringListBox listBox) {
        super("Error retrieving fish selection list", errorElement,
                ExpressionModule.getModuleInfo(), AjaxCallEventType.GET_FISH_LIST_STOP);
        this.listBox = listBox;
    }

    @Override
    public void onSuccess(List<FishDTO> fishDTOList) {
        super.onFinish();
        fishMap.clear();
        listBox.clear();

        List<FishDTO> wildtype = CurationEntryPoint.getWildtypeFishList();
        // get WT first
        for (FishDTO fish : wildtype) {
            if (fish.getHandle().equals("WT")) {
                listBox.addItem(fish.getHandle(), fish.getZdbID());
                fishMap.put(fish.getZdbID(), fish);
            }
        }
        listBox.addItem("-----");
        listBox.getElement().getElementsByTagName("option").getItem(listBox.getItemCount() - 1).setAttribute("disabled", "disabled");
        for (FishDTO fish : fishDTOList) {
            // do not include wildtype fish as they are grouped separately
            if (fish.isWildtype())
                continue;
            listBox.addItem(fish.getHandle(), fish.getZdbID());
            fishMap.put(fish.getZdbID(), fish);
        }
        listBox.addItem("-----");
        listBox.getElement().getElementsByTagName("option").getItem(listBox.getItemCount() - 1).setAttribute("disabled", "disabled");
        for (FishDTO fish : wildtype) {
            if (!fish.getHandle().equals("WT")) {
                listBox.addItem(fish.getHandle(), fish.getZdbID());
                fishMap.put(fish.getZdbID(), fish);
            }
        }
    }
}

