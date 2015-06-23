package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Table of associated genotypes
 */
public class FishPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();;
    private final HandlerManager eventBus;
    private FishView view;
    private String publicationID;

    private List<FishDTO> fishList = new ArrayList<>(10);

    public FishPresenter(HandlerManager eventBus, FishView view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.publicationID = publicationID;
        view.setPublicationID(publicationID);
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

        private boolean initiatedFromNewFishCreation;

        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, (Widget) null);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            if (list != null && list.size() > 0)
                view.getNoneDefined().setVisible(false);
            view.setData(list);
            fishList = list;
            bind();
        }

        public void setInitiatedFromNewFishCreation(boolean initiatedFromNewFishCreation) {
            this.initiatedFromNewFishCreation = initiatedFromNewFishCreation;
        }
    }

    public List<FishDTO> getFishList() {
        return fishList;
    }
}
