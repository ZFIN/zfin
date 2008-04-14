package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

class ItemSuggestCallback implements AsyncCallback {
    private SuggestOracle.Request request;
    private SuggestOracle.Callback callback;
    private LookupComposite anatomyLookup ;

    public ItemSuggestCallback(SuggestOracle.Request req, SuggestOracle.Callback call,LookupComposite anatomyLookup) {
        request = req;
        callback = call;
        this.anatomyLookup = anatomyLookup ;
    }

    public void onFailure(Throwable error) {
        anatomyLookup.setErrorString("error contacting server . . .\n"+error.toString());
    }

    public void onSuccess(Object retValue) {
        anatomyLookup.clearError();
        callback.onSuggestionsReady(request, (SuggestOracle.Response)retValue);
    }
}
