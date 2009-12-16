package org.zfin.framework.presentation.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.SuggestOracle;

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
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/anatomylookup");
            }
            return ourInstance;
        }
    }

    public SuggestOracle.Response getAnatomySuggestions(SuggestOracle.Request req);

    public SuggestOracle.Response getGOSuggestions(SuggestOracle.Request req, Ontology ontology);

    public SuggestOracle.Response getQualitySuggestions(SuggestOracle.Request req);

    public SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req);

    public SuggestOracle.Response getGenedomAndEFGSuggestions(SuggestOracle.Request req);

    public SuggestOracle.Response getSupplierSuggestions(SuggestOracle.Request req);

    public SuggestOracle.Response getFeatureSuggestions(SuggestOracle.Request req);

    /**
     * Retrieve the terminfo for a given term id and ontology.
     *
     * @param ontology ontology
     * @param termID   term ID
     * @return term info
     */
    public TermInfo getTermInfo(Ontology ontology, String termID);


    /**
     * Retrieve the term info for a given ontology and term name.
     *
     * @param ontology Ontology
     * @param termName term Name
     * @return TermInfo
     */
    TermInfo getTermInfoByName(Ontology ontology, String termName);

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request    request
     * @param wildCard   true or false
     * @param goOntology ontology name
     * @return suggestions
     */
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean wildCard, Ontology goOntology);

    // validation methods

    public TermStatus validateAnatomyTerm(String term);

    public TermStatus validateMarkerTerm(String term);
}
