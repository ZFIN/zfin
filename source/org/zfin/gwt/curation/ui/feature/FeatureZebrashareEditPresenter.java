package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import org.zfin.gwt.curation.event.CloneFishEvent;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.curation.ui.FeatureRPCService;
import org.zfin.gwt.curation.ui.FeatureServiceGWT;
import org.zfin.gwt.curation.ui.FishServiceGWT;
import org.zfin.gwt.curation.ui.Presenter;
import org.zfin.gwt.curation.ui.fish.FishModule;
import org.zfin.gwt.curation.ui.fish.FishPresenter;
import org.zfin.gwt.curation.ui.fish.FishView;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;

import java.util.*;

public class FeatureZebrashareEditPresenter implements Presenter {


    private FeatureZebrashareEditView view;
    private String publicationID;


    private List<FeatureDTO> featureList = new ArrayList<>(10);


    public FeatureZebrashareEditPresenter(FeatureZebrashareEditView view, String publicationID) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {
        loadValues();
    }




    private void loadValues() {

        // get Feature-Marker-Relationships
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_START);
        FeatureServiceGWT.callServer(publicationID, new FeatureZebrashareEditPresenter.RetrieveFeatureListCallBack("Feature List",
                null, AjaxCallEventType.GET_FEATURE_LIST_STOP));

    }

    class RetrieveFeatureListCallBack extends ZfinAsyncCallback<List<FeatureDTO>> {

        RetrieveFeatureListCallBack(String errorMessage, ErrorHandler errorLabel, AjaxCallEventType eventType) {
            super(errorMessage, errorLabel, FeatureModule.getModuleInfo(), eventType);
        }
            @Override
            public void onSuccess(List<FeatureDTO> list) {
                super.onFinish();
                if (list == null) {
                    view.emptyDataTable();
                    return;
                }
                if (list.size() > 0) {
                    view.getNoneDefined().setVisible(false);

                }
                int elementIndex = 0;
                for (final FeatureDTO dto : list) {
                    view.addFeature(dto, elementIndex);

                    elementIndex++;
                }
                featureList = list;
            }
        }


        public List<FeatureDTO> getFeatureList() {
            return featureList;
        }


    }