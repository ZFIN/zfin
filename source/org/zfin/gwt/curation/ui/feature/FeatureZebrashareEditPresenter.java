package org.zfin.gwt.curation.ui.feature;

import org.zfin.gwt.curation.ui.Presenter;
import org.zfin.gwt.curation.ui.ZebrashareFeatureServiceGWT;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

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




    public void loadValues() {

        // get Feature-Marker-Relationships
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_ZEBRASHARE_FEATURE_LIST_START);
        ZebrashareFeatureServiceGWT.getFeatureList(publicationID, new FeatureZebrashareEditPresenter.RetrieveFeatureListCallBack("Feature List",
                null, AjaxCallEventType.GET_ZEBRASHARE_FEATURE_LIST_STOP));

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