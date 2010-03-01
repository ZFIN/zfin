package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.util.LookupRPCService;

/**
 */
class CallbackTimer extends Timer {

    private SuggestOracle.Request request;
    private ItemSuggestCallback callback;
    private LookupComposite lookup;

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

    public void run() {
        if (lookup.getType().equals(LookupComposite.TYPE_SUPPLIER)) {
            LookupRPCService.App.getInstance().getSupplierSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_ANATOMY_ONTOLOGY)) {
            LookupRPCService.App.getInstance().getAnatomySuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_GENE_ONTOLOGY)) {
            LookupRPCService.App.getInstance().getGOSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_QUALITY)) {
            LookupRPCService.App.getInstance().getQualitySuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.MARKER_LOOKUP)) {
            LookupRPCService.App.getInstance().getMarkerSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.GENEDOM_AND_EFG)) {
            LookupRPCService.App.getInstance().getGenedomAndEFGSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.FEATURE_LOOKUP)) {
            LookupRPCService.App.getInstance().getFeatureSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.GDAG_TERM_LOOKUP)) {
            LookupRPCService.App.getInstance().getOntologySuggestions(request, lookup.isWildCard(), lookup.getGoOntology(), callback);
        }

        this.callback = null;
    }
}
