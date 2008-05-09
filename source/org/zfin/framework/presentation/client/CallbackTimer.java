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
        if(lookup.getType().equals(LookupComposite.TYPE_ANATOMY_ONTOLOGY)){
            LookupService.App.getInstance().getAnatomySuggestions(request , lookup.isWildCard(),callback);
        }
        else
        if(lookup.getType().equals(LookupComposite.TYPE_GENE_ONTOLOGY)){
            LookupService.App.getInstance().getGOSuggestions(request , lookup.isWildCard(),callback);
        }
        else
        if(lookup.getType().equals(LookupComposite.TYPE_QUALITY)){
            LookupService.App.getInstance().getQualitySuggestions(request , lookup.isWildCard(),callback);
        }
        else
        if(lookup.getType().equals(LookupComposite.MARKER_LOOKUP)){
            LookupService.App.getInstance().getMarkerSuggestions(request , lookup.isWildCard(),callback);
        }

        this.callback = null ;
    }
}
