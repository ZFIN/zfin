package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Table of associated genotypes
 */
public class FishPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private FishView view;
    private String publicationID;

    private List<FishDTO> fishList = new ArrayList<>(10);

    public FishPresenter(FishView view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
    }

    public void bind() {
    }


    @Override
    public void go() {
        createFishList();
    }

    private void createFishList() {
        diseaseRpcService.getFishList(publicationID, new RetrieveFishListCallBack("Fish List", null));

    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, (Widget) null);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            if (list != null && list.size() > 0) {
                view.getNoneDefined().setVisible(false);
                Collections.sort(list, new Comparator<FishDTO>() {
                    @Override
                    public int compare(FishDTO o1, FishDTO o2) {
                        return o1.compareToWildtypeFirst(o2);
                    }
                });
            }
            view.setData(list);
            fishList = list;
            bind();
        }
    }

    public List<FishDTO> getFishList() {
        return fishList;
    }
}
