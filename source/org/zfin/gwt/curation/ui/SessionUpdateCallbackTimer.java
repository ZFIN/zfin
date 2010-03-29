package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.CuratorSessionDTO;
import org.zfin.gwt.root.ui.SessionSaveService;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SessionUpdateCallbackTimer extends Timer {

    private List<CuratorSessionDTO> sessionList = new ArrayList<CuratorSessionDTO>(10);

    public void scheduleCallback(CuratorSessionDTO sessionUpdate, int time) {
        cancel();
        sessionList.add(sessionUpdate);
        schedule(time);
    }

    @Override
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
