package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

import java.util.HashMap;
import java.util.Map;

/**
 */
class CallbackTimer extends Timer {

    private SuggestOracle.Request request;
    private ItemSuggestCallback callback;
    private LookupComposite lookup;


    private LookupRPCServiceAsync lookupServiceAsync = LookupRPCService.App.getInstance();

    public CallbackTimer(LookupComposite lookup) {
        this.lookup = lookup;
    }

    public void scheduleCallback(SuggestOracle.Request req, SuggestOracle.Callback callback, int time) {
        this.cancel();
        this.request = req;
        if (this.callback == null) {
            this.callback = new ItemSuggestCallback(req, callback, lookup);
        } else {
            this.callback.setRequest(req);
        }
        this.schedule(time);
    }

    @Override
    public void run() {
        if (lookup.getType().equals(LookupStrings.TYPE_SUPPLIER)) {
            lookupServiceAsync.getSupplierSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupStrings.MARKER_LOOKUP)) {
            lookupServiceAsync.getMarkerSuggestions(request, null, callback);
        } else if (lookup.getType().equals(LookupStrings.MARKER_LOOKUP_AND_TYPE)) {
            Map<String, String> options = new HashMap<String, String>();
            options.put(LookupComposite.SHOW_TYPE, "true");
            lookupServiceAsync.getMarkerSuggestions(request, options, callback);
        } else if (lookup.getType().equals(LookupStrings.ANTIBODY_LOOKUP)) {
            LookupRPCService.App.getInstance().getAntibodySuggestions(request, callback);
        } else if (lookup.getType().equals(LookupStrings.GENEDOM_AND_EFG)) {
            lookupServiceAsync.getGenedomAndEFGSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupStrings.GENEDOM)) {
            lookupServiceAsync.getGenedomSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupStrings.FEATURE_LOOKUP)) {
            lookupServiceAsync.getFeatureSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupStrings.GDAG_TERM_LOOKUP)) {
            if (lookup.isUseTermsWithDataOnly())
                lookupServiceAsync.getTermCompletionWithData(request, lookup.getOntology(), lookup.getUseIdAsValue(), callback);
            else
                lookupServiceAsync.getOntologySuggestions(request, lookup.getOntology(), lookup.getUseIdAsValue(), callback);
        } else if (lookup.getType().equals(LookupStrings.CONSTRUCT_LOOKUP)) {
            lookupServiceAsync.getConstructSuggestions(request, callback);
        }

        this.callback = null;
    }
}
