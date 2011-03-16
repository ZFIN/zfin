package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.dto.*;

import java.util.List;
import java.util.Map;

/**
 */
public interface LookupRPCServiceAsync {
                                                          
    // publication access method

    // publication access method

    void getPublicationAbstract(String zdbID, AsyncCallback<PublicationDTO> async);

    void getMarkerSuggestions(SuggestOracle.Request req, Map<String,String> options, AsyncCallback<SuggestOracle.Response> async);

    void getAntibodySuggestions(SuggestOracle.Request request, AsyncCallback<SuggestOracle.Response> async);

    void getGenedomAndEFGSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    void getGenedomSuggestions(SuggestOracle.Request request, AsyncCallback<SuggestOracle.Response> async);

    void getSupplierSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    void getConstructSuggestions(SuggestOracle.Request req,String pubZdbID, AsyncCallback<SuggestOracle.Response> async);

    void getFeatureSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    /**
     * Retrieve the terminfo for a given term id and ontology.
     *
     * @param ontology Ontology
     * @param termID   term ID
     * @param async    callback
     */
    void getTermInfo(OntologyDTO ontology, String termID, AsyncCallback<TermDTO> async);

    // validation methods

    void validateTerm(String term, OntologyDTO ontology, AsyncCallback<TermStatus> async);

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table).
     *
     * @param request        request
     * @param ontologyDTO    ontology name
     * @param async          callback
     */
    void getOntologySuggestions(SuggestOracle.Request request, OntologyDTO ontologyDTO, boolean useIdAsValue, AsyncCallback<SuggestOracle.Response> async);

    void getRecentPublications(String key, AsyncCallback<List<PublicationDTO>> asyncCallback);

    void addRecentPublication(String zdbID, String key, AsyncCallback<PublicationDTO> async);

    void getAttributionsForPub(String publicationZdbID, AsyncCallback<List<RelatedEntityDTO>> markerEditCallBack);

    void getAllZfinProperties(AsyncCallback<Map<String, String>> async);

    void getTermByName(OntologyDTO ontologyDTO, String value, AsyncCallback<TermDTO> async);
}
