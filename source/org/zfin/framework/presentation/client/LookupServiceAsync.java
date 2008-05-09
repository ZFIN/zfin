package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public interface LookupServiceAsync {

    void getAnatomySuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback async);

    void getGOSuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback async);

    void getQualitySuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback async);

    void getMarkerSuggestions(SuggestOracle.Request req, boolean wildCard, AsyncCallback async);

    void validateTerm(String term, AsyncCallback async);
}
