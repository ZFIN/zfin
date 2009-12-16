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
        if (lookup.getType().equals(LookupComposite.TYPE_SUPPLIER)) {
            LookupService.App.getInstance().getSupplierSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_ANATOMY_ONTOLOGY)) {
            LookupService.App.getInstance().getAnatomySuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_GENE_ONTOLOGY)) {
            LookupService.App.getInstance().getGOSuggestions(request, lookup.getGoOntology(), callback);
        } else if (lookup.getType().equals(LookupComposite.TYPE_QUALITY)) {
            LookupService.App.getInstance().getQualitySuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.MARKER_LOOKUP)) {
            LookupService.App.getInstance().getMarkerSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.GENEDOM_AND_EFG)) {
            LookupService.App.getInstance().getGenedomAndEFGSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.FEATURE_LOOKUP)) {
            LookupService.App.getInstance().getFeatureSuggestions(request, callback);
        } else if (lookup.getType().equals(LookupComposite.GDAG_TERM_LOOKUP)) {
            LookupService.App.getInstance().getOntologySuggestions(request, lookup.isWildCard(), lookup.getGoOntology(), callback);
        }

        this.callback = null;
    }
}
