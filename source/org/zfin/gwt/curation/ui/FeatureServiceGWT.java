package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.FeatureDTO;

import java.util.ArrayList;
import java.util.List;

public class FeatureServiceGWT {

    private static FeatureRPCServiceAsync service = FeatureRPCService.App.getInstance();

    private static List<FeatureDTO> dtoList;
    private static List<AsyncCallback<List<FeatureDTO>>> callbackList;

    public static void getFeatureList(String publicationID, AsyncCallback<List<FeatureDTO>> callback) {
        getFeatureList(publicationID, callback, false);
    }

    public static void getFeatureList(String publicationID, final AsyncCallback<List<FeatureDTO>> callback, boolean ignoreCache) {
        // if already one callback in list add it and return;
        if (callbackList == null)
            callbackList = new ArrayList<>();
        //GWT.log("Number of callbacks: " + callbackList.size());
        callbackList.add(callback);
        // requests that came in after the first one will be handled
        if (callbackList.size() > 1) {
            return;
        }
        if (dtoList == null || ignoreCache) {
            service.getFeaturesForPub(publicationID, new AsyncCallback<List<FeatureDTO>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    for (AsyncCallback<List<FeatureDTO>> callBack : callbackList)
                        callBack.onFailure(throwable);
                    callbackList = null;
                }

                @Override
                public void onSuccess(List<FeatureDTO> featureDTOs) {
                    dtoList = featureDTOs;
                    for (AsyncCallback<List<FeatureDTO>> callBack : callbackList)
                        callBack.onSuccess(dtoList);
                    callbackList = null;
                }
            });
        } else {
            callback.onSuccess(dtoList);
        }
    }
}
