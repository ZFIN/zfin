package org.zfin.people.presentation.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class SessionUpdateCallbackTimer extends Timer {

    private List sessionList = new ArrayList() ;

    public void scheduleCallback(CuratorSessionDTO sessionUpdate,int time){
        cancel();
        sessionList.add(sessionUpdate) ;
        schedule(time);
    }

    public void run(){
            if(sessionList.size()>0){
                SessionSaveService.App.getInstance().saveCuratorUpdate( sessionList, new AsyncCallback(){
                    public void onSuccess(Object object) {
                        //Window.alert("success");
                    }
                    public void onFailure(Throwable throwable) {
//                        Window.alert("SessionSave update failed: "+ throwable);
                    }
                }) ;
                sessionList.clear();
            }
    }
}
