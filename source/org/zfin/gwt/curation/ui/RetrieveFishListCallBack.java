package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

public class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

    private ListBox fishList;
    private boolean showAll;

    public RetrieveFishListCallBack(ListBox fishList, String errorMessage, ErrorHandler errorLabel) {
        super(errorMessage, errorLabel);
        this.fishList = fishList;
    }

    public RetrieveFishListCallBack(ListBox fishList) {
        super("Error retrieving fish list", null);
        this.fishList = fishList;
    }

    public RetrieveFishListCallBack(ListBox fishList, boolean showAll) {
        super("Error retrieving fish list", null);
        this.fishList = fishList;
        this.showAll = showAll;
    }

    public void onSuccess(List<FishDTO> fish) {
        //Window.alert("brought back: " + experiments.size() );
        String selectedID = fishList.getSelectedValue();
        fishList.clear();
        int selectedItemIndex = 1;
        if(showAll)
            fishList.addItem("All", "");
        for (FishDTO fishDTO : fish) {
            String handle = fishDTO.getHandle();
            fishList.addItem(handle, fishDTO.getZdbID());
            if (fishDTO.getValue().equals(selectedID))
                fishList.setSelectedIndex(selectedItemIndex);
            selectedItemIndex++;
        }
    }

    public void onFailureCleanup() {
    }

}
