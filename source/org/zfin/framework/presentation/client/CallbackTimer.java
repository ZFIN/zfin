package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class CallbackTimer extends Timer {

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
        request.setLimit(20);
        if (lookup.getType().equals(LookupComposite.TYPE_SUPPLIER)) {
            LookupService.App.getInstance().getSupplierSuggestions(request, lookup.isWildCard(), callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_ANATOMY_ONTOLOGY)) {
            LookupService.App.getInstance().getAnatomySuggestions(request, lookup.isWildCard(), callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_GENE_ONTOLOGY)) {
            LookupService.App.getInstance().getGOSuggestions(request, lookup.isWildCard(), lookup.getGoOntology(), callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_QUALITY)) {
            LookupService.App.getInstance().getQualitySuggestions(request, lookup.isWildCard(), callback);
        } else if (lookup.getType().equals(LookupComposite.MARKER_LOOKUP)) {
            LookupService.App.getInstance().getMarkerSuggestions(request, lookup.isWildCard(), callback);
        } else if (lookup.getType().equals(LookupComposite.GENEDOM_AND_EFG)) {
            LookupService.App.getInstance().getGenedomAndEFGSuggestions(request, lookup.isWildCard(), callback);
        } else if (lookup.getType().equals(LookupComposite.FEATURE_LOOKUP)) {
            LookupService.App.getInstance().getFeatureSuggestions(request, lookup.isWildCard(), callback);
        } else if (lookup.getType().equals(LookupComposite.GDAG_TERM_LOOKUP)) {
            LookupService.App.getInstance().getOntologySuggestions(request, lookup.isWildCard(), lookup.getGoOntology(), callback);
        }

        this.callback = null;
    }
}
