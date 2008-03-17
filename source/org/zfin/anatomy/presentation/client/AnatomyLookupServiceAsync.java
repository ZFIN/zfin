package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public interface AnatomyLookupServiceAsync {

    public void getSuggestions(SuggestOracle.Request req, AsyncCallback callback); 
}
