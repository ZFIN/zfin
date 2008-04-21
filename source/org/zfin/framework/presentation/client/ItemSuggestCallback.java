package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

class ItemSuggestCallback implements AsyncCallback {
    private SuggestOracle.Request request;
    private SuggestOracle.Callback callback;
    private LookupComposite lookup ;

    public ItemSuggestCallback(SuggestOracle.Request req, SuggestOracle.Callback call,LookupComposite lookup) {
        request = req;
        callback = call;
        this.lookup = lookup ;
    }

    public void onFailure(Throwable error) {
//        lookup.setErrorString("error contacting server . . .\n"+error.toString());
    }

    public void onSuccess(Object retValue) {
        lookup.clearError();

        if(true==lookup.getTextBox().getText().equalsIgnoreCase(request.getQuery())){
            SuggestOracle.Response response = (SuggestOracle.Response)retValue ;
            if(response.getSuggestions().size()==0){
                lookup.setNoteString("No matches for '"+request.getQuery()+"'");
            }
            callback.onSuggestionsReady(request, (SuggestOracle.Response)retValue);
        }
    }

    public SuggestOracle.Request getRequest() {
        return request;
    }

    public void setRequest(SuggestOracle.Request request) {
        this.request = request;
    }
}
