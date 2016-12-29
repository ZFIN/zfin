package org.zfin.gwt.curation.ui.fish;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import org.zfin.gwt.curation.event.CloneFishEvent;
import org.zfin.gwt.curation.ui.CurationDiseaseRPC;
import org.zfin.gwt.curation.ui.CurationDiseaseRPCAsync;
import org.zfin.gwt.curation.ui.Presenter;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;

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

    FishPresenter(FishView view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
    }

    @Override
    public void go() {
        createFishList();
    }

    private void createFishList() {
        AppUtils.fireAjaxCall(FishModule.getModuleInfo(), AjaxCallEventType.GET_FISH_LIST_START);
        diseaseRpcService.getFishList(publicationID, new RetrieveFishListCallBack("Fish List",
                null,
                AjaxCallEventType.GET_FISH_LIST_STOP));

    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

        RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel, AjaxCallEventType eventType) {
            super(errorMessage, errorLabel, FishModule.getModuleInfo(), eventType);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            super.onFinish();
            if (list == null) {
                view.emptyDataTable();
                return;
            }
            if (list.size() > 0) {
                view.getNoneDefined().setVisible(false);
                Collections.sort(list, new Comparator<FishDTO>() {
                    @Override
                    public int compare(FishDTO o1, FishDTO o2) {
                        return o1.compareToWildtypeFirst(o2);
                    }
                });
            }
            int elementIndex = 0;
            for (final FishDTO dto : list) {
                view.addFish(dto, elementIndex);
                Anchor cloneLink = view.getCloneLink();
                cloneLink.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        CloneFishEvent event = new CloneFishEvent();
                        event.setFish(dto);
                        AppUtils.EVENT_BUS.fireEvent(event);
                    }
                });
                view.addCloneLink(cloneLink, elementIndex);
                view.addDeleteButton(dto, elementIndex);
                elementIndex++;
            }
            fishList = list;
        }
    }

    public List<FishDTO> getFishList() {
        return fishList;
    }
}
