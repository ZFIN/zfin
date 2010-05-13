package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RetrieveGenotypeListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

    private ListBox fishList;

    public RetrieveGenotypeListCallBack(ListBox fishList, String errorMessage, ErrorHandler errorLabel) {
        super(errorMessage, errorLabel);
        this.fishList = fishList;
    }

    @Override
    public void onSuccess(List<FishDTO> genotypes) {
        //Window.alert("brought back: " + genotypes.size() );
        fishList.clear();
        for (FishDTO genotypeHandle : genotypes) {
            fishList.addItem(genotypeHandle.getName(), genotypeHandle.getZdbID());
        }
    }

}
