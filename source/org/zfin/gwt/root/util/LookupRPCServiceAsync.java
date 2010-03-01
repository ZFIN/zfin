package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.gwt.root.dto.Ontology;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.TermInfo;
import org.zfin.gwt.root.dto.TermStatus;

import java.util.List;

/**
 */
public interface LookupRPCServiceAsync {


    void getAnatomySuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    void getGOSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    void getQualitySuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

    void getMarkerSuggestions(SuggestOracle.Request req, AsyncCallback<SuggestOracle.Response> async);

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
    void getTermInfo(Ontology ontology, String termID, AsyncCallback<TermInfo> async);

    // validation methods

    void validateAnatomyTerm(String term, AsyncCallback<TermStatus> async);

    void validateMarkerTerm(String term, AsyncCallback<TermStatus> async);

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table).
     *
     * @param request    request
     * @param wildCard   true or false
     * @param goOntology ontology name
     * @param async      callback
     */
    void getOntologySuggestions(SuggestOracle.Request request, boolean wildCard, Ontology goOntology, AsyncCallback<SuggestOracle.Response> async);

    /**
     * Retrieve the term info for a given ontology and term name.
     *
     * @param ontology         Ontology
     * @param termName         term Name
     * @param termInfoCallback callback
     */
    void getTermInfoByName(Ontology ontology, String termName, AsyncCallback<TermInfo> termInfoCallback);

    void getRecentPublications(AsyncCallback<List<PublicationDTO>> asyncCallback);

    void setRecentPublication(String zdbID, AsyncCallback<PublicationDTO > async);
}
