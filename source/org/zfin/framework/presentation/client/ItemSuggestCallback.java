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

    private boolean containsPunctuation(String term){
        char[] charArray = term.toCharArray() ;
        char c ;
        for(int i = 0 ; i < charArray.length ;i++){
            c = charArray[i] ;
           if(
              false==Character.isLetterOrDigit(c)
           &&
               c!=' '
           )
           {
               return true ; 
           }
        }
        return false ;
    }

    public void onSuccess(Object retValue) {
        lookup.clearError();
        lookup.clearNote();

        if(true==lookup.getTextBox().getText().equalsIgnoreCase(request.getQuery())){
            SuggestOracle.Response response = (SuggestOracle.Response)retValue ;
            if(response.getSuggestions().size()==0){
                if(true==containsPunctuation(request.getQuery())){
                    lookup.setErrorString("Please select one term at a time without punctuation") ;
                }
                else{
                    lookup.setErrorString("Term not found '"+request.getQuery()+"'") ;
                }
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
