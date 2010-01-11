package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.dto.CuratorSessionDTO;

import java.util.ArrayList;

/**
 */
public class SessionUpdateCallbackTimer extends Timer {

    private ArrayList<CuratorSessionDTO> sessionList = new ArrayList<CuratorSessionDTO>(10);

    public void scheduleCallback(CuratorSessionDTO sessionUpdate, int time) {
        cancel();
        sessionList.add(sessionUpdate);
        schedule(time);
    }

    public void run() {
        if (!sessionList.isEmpty()) {
            SessionSaveService.App.getInstance().saveCuratorUpdate(sessionList, new AsyncCallback() {
                public void onSuccess(Object object) {
                    //Window.alert("success");
                }

                public void onFailure(Throwable throwable) {
//                        Window.alert("SessionSave update failed: "+ throwable);
                }
            });
            sessionList.clear();
        }
    }
}
