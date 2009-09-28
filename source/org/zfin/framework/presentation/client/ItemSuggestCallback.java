package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

class ItemSuggestCallback implements AsyncCallback<SuggestOracle.Response> {
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

    public void onSuccess(SuggestOracle.Response retValue) {
        lookup.clearError();
        lookup.clearNote();

        if(lookup.isSuggetBoxHasFocus()==false){
           return ;
        }

        if(true==lookup.getTextBox().getText().equalsIgnoreCase(request.getQuery())){
            SuggestOracle.Response response = (SuggestOracle.Response)retValue ;
            if(response.getSuggestions().size()==0){
                if(true==containsPunctuation(request.getQuery())){
                    lookup.setErrorString("Please select one term at a time without punctuation") ;
                }
                else{
                    if(!lookup.getType().equals(LookupComposite.TYPE_SUPPLIER))
                        lookup.setErrorString("Term not found '"+request.getQuery()+"'") ;
                    else
                        lookup.setErrorString("Supplier name '"+request.getQuery()+"' not found.") ;
                }
            }
            callback.onSuggestionsReady(request, retValue);
        }
    }

    public SuggestOracle.Request getRequest() {
        return request;
    }

    public void setRequest(SuggestOracle.Request request) {
        this.request = request;
    }
}
