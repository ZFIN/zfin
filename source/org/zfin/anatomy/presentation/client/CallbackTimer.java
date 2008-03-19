package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class CallbackTimer extends Timer {

    private SuggestOracle.Request request;
    private ItemSuggestCallback callback;

    public void scheduleCallback(SuggestOracle.Request req, SuggestOracle.Callback callback,int time){
        this.cancel();
        this.request = req ;
        this.callback = new ItemSuggestCallback(req,callback) ; 
        this.schedule(time);
    }

    public void run(){
        AnatomyLookupService.App.getInstance().getSuggestions(request , callback);
    }
}
