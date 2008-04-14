package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class CallbackTimer extends Timer {

    private SuggestOracle.Request request;
    private ItemSuggestCallback callback;
    private LookupComposite anatomyLookup ;

    public CallbackTimer(LookupComposite anatomyLookup){
       this.anatomyLookup = anatomyLookup ;
    }

    public void scheduleCallback(SuggestOracle.Request req, SuggestOracle.Callback callback,int time){
        this.cancel();
        this.request = req ;
        this.callback = new ItemSuggestCallback(req,callback,anatomyLookup) ; 
        this.schedule(time);
    }

    public void run(){
        LookupService.App.getInstance().getSuggestions(request , callback);
    }
}
