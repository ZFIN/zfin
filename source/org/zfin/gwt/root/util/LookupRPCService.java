package org.zfin.gwt.root.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.dto.Ontology;
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

    SuggestOracle.Response getAnatomySuggestions(SuggestOracle.Request req);

    SuggestOracle.Response getGOSuggestions(SuggestOracle.Request req);

    SuggestOracle.Response getQualitySuggestions(SuggestOracle.Request req);

    SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req);

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
    TermInfo getTermInfo(Ontology ontology, String termID);


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
    SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean wildCard, Ontology goOntology);

    // validation methods

    TermStatus validateAnatomyTerm(String term);

    TermStatus validateMarkerTerm(String term);

    List<PublicationDTO> getRecentPublications();

    PublicationDTO setRecentPublication(String zdbID);
}
