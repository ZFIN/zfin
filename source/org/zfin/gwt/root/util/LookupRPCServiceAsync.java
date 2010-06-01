package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ItemSuggestCallback;

import java.util.List;
import java.util.Map;

/**
 */
public interface LookupRPCServiceAsync {

    // publication access method

    void getPublicationAbstract(String zdbID, AsyncCallback<PublicationDTO> async);

    void getMarkerSuggestions(SuggestOracle.Request req, Map<String,String> options, AsyncCallback<SuggestOracle.Response> async);

    void getAntibodySuggestions(SuggestOracle.Request request, AsyncCallback<SuggestOracle.Response> async);

    void getGenedomAndEFGSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    void getSupplierSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    void getFeatureSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    /**
     * Retrieve the terminfo for a given term id and ontology.
     *
     * @param ontology Ontology
     * @param termID   term ID
     * @param async    callback
     */
    void getTermInfo(OntologyDTO ontology, String termID, AsyncCallback<TermInfo> async);

    // validation methods

    void validateAnatomyTerm(String term, AsyncCallback<TermStatus> async);

    void validateMarkerTerm(String term, AsyncCallback<TermStatus> async);

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table).
     *
     * @param request        request
     * @param showTermDetail true or false
     * @param ontologyDTO    ontology name
     * @param async          callback
     */
    void getOntologySuggestions(SuggestOracle.Request request, boolean showTermDetail, OntologyDTO ontologyDTO, AsyncCallback<SuggestOracle.Response> async);

    /**
     * Retrieve the term info for a given ontology and term name.
     *
     * @param ontology         Ontology
     * @param termName         term Name
     * @param termInfoCallback callback
     */
    void getTermInfoByName(OntologyDTO ontology, String termName, AsyncCallback<TermInfo> termInfoCallback);

    void getRecentPublications(String key, AsyncCallback<List<PublicationDTO>> asyncCallback);

    void addRecentPublication(String zdbID, String key, AsyncCallback<PublicationDTO> async);

    void getAttributionsForPub(String publicationZdbID, AsyncCallback<List<RelatedEntityDTO>> markerEditCallBack);
}
