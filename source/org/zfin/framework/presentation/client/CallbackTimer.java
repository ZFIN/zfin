package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class CallbackTimer extends Timer {

    private SuggestOracle.Request request;
    private ItemSuggestCallback callback;
    private LookupComposite lookup;

    public CallbackTimer(LookupComposite lookup){
       this.lookup = lookup;
    }

    public void scheduleCallback(SuggestOracle.Request req, SuggestOracle.Callback callback,int time){
        this.cancel();
        this.request = req ;
        if(this.callback==null){
            this.callback = new ItemSuggestCallback(req,callback, lookup) ;
        }
        else{
            this.callback.setRequest(req);
        }
        this.schedule(time);
    }

    public void run(){
        LookupService.App.getInstance().getSuggestions(request , lookup.isWildCard(),callback);
        this.callback = null ;
    }
}
