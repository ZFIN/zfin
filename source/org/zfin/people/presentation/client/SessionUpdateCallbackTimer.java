package org.zfin.people.presentation.client;

import com.google.gwt.user.client.Timer;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class SessionUpdateCallbackTimer extends Timer {

    private SessionUpdateCallback callback ;
    private List sessionList = new ArrayList() ;

    public void scheduleCallback(CuratorSessionDTO sessionUpdate,int time){
        cancel();
        sessionList.add(sessionUpdate) ;
        callback  = new SessionUpdateCallback() ; 
        schedule(time);
    }

    public void run(){
            if(sessionList.size()>0){
                SessionSaveService.App.getInstance().saveCuratorUpdate( sessionList, callback);
                sessionList.clear();
            }
    }
}
