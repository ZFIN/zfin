package org.zfin.gwt.root.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.dto.*;

import java.util.List;
import java.util.Map;

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

    // publication access method

    PublicationDTO getPublicationAbstract(String zdbID);

    SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req, Map<String,String> options);

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
     * @param request        request
     * @param showTermDetail true or false
     * @param goOntology     ontology name
     * @return suggestions
     */
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean showTermDetail, OntologyDTO goOntology,boolean useIdAsValue);

    // validation methods

    TermStatus validateTerm(String term,  OntologyDTO ontology);

    TermStatus validateMarkerTerm(String term);

    List<PublicationDTO> getRecentPublications(String key);

    PublicationDTO addRecentPublication(String zdbID, String key);

    List<RelatedEntityDTO> getAttributionsForPub(String publicationZdbID);

    Map<String,String> getAllZfinProperties();
}
