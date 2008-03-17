package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.core.client.GWT;

import java.util.List;

/**
 */
public interface AnatomyLookupService extends RemoteService {
    /**
     * Utility/Convenience class.
     * Use AnatomyLookupService.App.getInstance() to access static instance of AnatomyLookupServiceAsync
     */
    public static class App {
        private static AnatomyLookupServiceAsync ourInstance = null;

        public static synchronized AnatomyLookupServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (AnatomyLookupServiceAsync) GWT.create(AnatomyLookupService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(  "/ajax/anatomylookup");
            }
            return ourInstance;
        }
    }

    public SuggestOracle.Response getSuggestions(SuggestOracle.Request req) ;
}
