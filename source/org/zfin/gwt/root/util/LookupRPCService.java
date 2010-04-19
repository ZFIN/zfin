package org.zfin.gwt.root.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.TermInfo;
import org.zfin.gwt.root.dto.TermStatus;

import java.util.List;

/**
 */
public interface LookupRPCService extends RemoteService {

    /**
     * Utility/Convenience class.
     * Use AnatomyLookupService.App.getInstance() to access static instance of AnatomyLookupServiceAsync
     */
    static class App {
        private static LookupRPCServiceAsync ourInstance = null;

        public static synchronized LookupRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (LookupRPCServiceAsync) GWT.create(LookupRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/anatomylookup");
            }
            return ourInstance;
        }
    }

    SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req);

    SuggestOracle.Response getAntibodySuggestions(SuggestOracle.Request request);

    SuggestOracle.Response getGenedomAndEFGSuggestions(SuggestOracle.Request req);

    SuggestOracle.Response getSupplierSuggestions(SuggestOracle.Request req);

    SuggestOracle.Response getFeatureSuggestions(SuggestOracle.Request req);

    /**
     * Retrieve the terminfo for a given term id and ontology.
     *
     * @param ontology ontology
     * @param termID   term ID
     * @return term info
     */
    public TermInfo getTermInfo(OntologyDTO ontology, String termID);


    /**
     * Retrieve the term info for a given ontology and term name.
     *
     * @param ontology Ontology
     * @param termName term Name
     * @return TermInfo
     */
    TermInfo getTermInfoByName(OntologyDTO ontology, String termName);

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request    request
     * @param showTermDetail   true or false
     * @param goOntology ontology name
     * @return suggestions
     */
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean showTermDetail, OntologyDTO goOntology);

    // validation methods

    TermStatus validateAnatomyTerm(String term);

    TermStatus validateMarkerTerm(String term);

    List<PublicationDTO> getRecentPublications(String key);

    PublicationDTO addRecentPublication(String zdbID,String key);
}
