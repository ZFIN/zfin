package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.core.client.GWT;

/**
 */
public interface LookupService extends RemoteService {
    /**
     * Utility/Convenience class.
     * Use AnatomyLookupService.App.getInstance() to access static instance of AnatomyLookupServiceAsync
     */
    public static class App {
        private static LookupServiceAsync ourInstance = null;

        public static synchronized LookupServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (LookupServiceAsync) GWT.create(LookupService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(  "/ajax/anatomylookup");
            }
            return ourInstance;
        }
    }

    public SuggestOracle.Response getSuggestions(SuggestOracle.Request req,boolean wildCard) ;
}
