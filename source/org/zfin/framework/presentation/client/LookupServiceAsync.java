package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public interface LookupServiceAsync {


    void getAnatomySuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback<SuggestOracle.Response> async);

    void getGOSuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback<SuggestOracle.Response> async);

    void getQualitySuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback<SuggestOracle.Response> async);

    void getMarkerSuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback<SuggestOracle.Response> async);

    void getGenedomAndEFGSuggestions(SuggestOracle.Request req, boolean wildCard,
                                     AsyncCallback<SuggestOracle.Response> async);

    void getSupplierSuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback<SuggestOracle.Response> async);

    void getFeatureSuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback<SuggestOracle.Response> async);

    // validation methods
    void validateAnatomyTerm(String term, AsyncCallback<TermStatus> async);

    void validateMarkerTerm(String term, AsyncCallback<TermStatus> async);
}
