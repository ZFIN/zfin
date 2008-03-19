package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

class ItemSuggestCallback implements AsyncCallback {
    private SuggestOracle.Request request;
    private SuggestOracle.Callback callback;

    public ItemSuggestCallback(SuggestOracle.Request req, SuggestOracle.Callback call) {
        request = req;
        callback = call;
    }

    public void onFailure(Throwable error) {
        callback.onSuggestionsReady(request, new SuggestOracle.Response());
    }

    public void onSuccess(Object retValue) {
        callback.onSuggestionsReady(request, (SuggestOracle.Response)retValue);
    }
}
