package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

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
        if (lookup.getType().equals(LookupComposite.TYPE_SUPPLIER)) {
            lookupServiceAsync.getSupplierSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.MARKER_LOOKUP)) {
            lookupServiceAsync.getMarkerSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.ANTIBODY_LOOKUP)) {
            LookupRPCService.App.getInstance().getAntibodySuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.GENEDOM_AND_EFG)) {
            lookupServiceAsync.getGenedomAndEFGSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.FEATURE_LOOKUP)) {
            lookupServiceAsync.getFeatureSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.GDAG_TERM_LOOKUP)) {
            lookupServiceAsync.getOntologySuggestions(request, lookup.isShowTermDetail(), lookup.getOntology(), callback);
        }

        this.callback = null;
    }
}
