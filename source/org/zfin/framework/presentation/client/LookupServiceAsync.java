package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public interface LookupServiceAsync {

    public void getSuggestions(SuggestOracle.Request req, AsyncCallback callback); 
}
