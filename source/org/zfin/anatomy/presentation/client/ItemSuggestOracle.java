package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 */
public class ItemSuggestOracle extends SuggestOracle {
       public boolean isDisplayStringHTML() { return true; }

    public void requestSuggestions(SuggestOracle.Request req,
SuggestOracle.Callback callback) {
        ItemSuggestCallback itemSuggestCallback = new ItemSuggestCallback(req,callback) ; 
        AnatomyLookupService.App.getInstance().getSuggestions(req, itemSuggestCallback );
    }

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
}
