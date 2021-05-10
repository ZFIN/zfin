package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCService;
import org.zfin.gwt.root.ui.MarkerGoEvidenceRPCServiceAsync;

import java.util.ArrayList;
import java.util.List;

public class MarkerGoEvidenceServiceGWT {

    private static MarkerGoEvidenceRPCServiceAsync service = MarkerGoEvidenceRPCService.App.getInstance();

    private static List<GoEvidenceDTO> dtoList;
    private static List<AsyncCallback<List<GoEvidenceDTO>>> callbackList;

    public static void getMarkerGoTermEvidencesForPub(String publicationID, AsyncCallback<List<GoEvidenceDTO>> callback) {
        // if already one callback in list add it and return;
        if (callbackList == null)
            callbackList = new ArrayList<>();
        GWT.log("go: Number of callbacks: " + callbackList.size());
        callbackList.add(callback);
        // requests that came in after the first one will be handled
        if (callbackList.size() > 1) {
            return;
        }
        service.getMarkerGoTermEvidencesForPub(publicationID, new AsyncCallback<List<GoEvidenceDTO>>() {
            @Override
            public void onFailure(Throwable throwable) {
                for (AsyncCallback<List<GoEvidenceDTO>> callBack : callbackList)
                    callBack.onFailure(throwable);
                callbackList = null;
            }

            @Override
            public void onSuccess(List<GoEvidenceDTO> featureDTOs) {
                dtoList = featureDTOs;
                for (AsyncCallback<List<GoEvidenceDTO>> callBack : callbackList)
                    callBack.onSuccess(dtoList);
                callbackList = null;
            }
        });
    }
}
