package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class ItemSuggestCallback implements AsyncCallback<SuggestOracle.Response> {

    private static final int MAXIMUM_NUMBER_OF_MATCHES = 25;
    private SuggestOracle.Request request;
    private SuggestOracle.Callback callback;
    private LookupComposite lookup;

    public ItemSuggestCallback(SuggestOracle.Request req, SuggestOracle.Callback call, LookupComposite lookup) {
        request = req;
        callback = call;
        this.lookup = lookup;
    }

    public void onFailure(Throwable error) {
//        lookup.setErrorString("error contacting server . . .\n"+error.toString());
    }

    public void onSuccess(SuggestOracle.Response response) {
        lookup.clearError();
        lookup.clearNote();

        if (false == lookup.isSuggestBoxHasFocus()) {
            return;
        }

        if (lookup.isWildCard()) {
            @SuppressWarnings({"unchecked"})
            Collection<ItemSuggestion> suggestions = (Collection<ItemSuggestion>) response.getSuggestions();
            Collection<ItemSuggestion> newSuggestions = new ArrayList<ItemSuggestion>(MAXIMUM_NUMBER_OF_MATCHES);
            newSuggestions.add(new ItemSuggestion("*" + request.getQuery() + "*", request.getQuery() + "*"));
            newSuggestions.addAll(suggestions);
            response.setSuggestions(newSuggestions);
        }

        if (request.getLimit() != ItemSuggestOracle.NO_LIMIT && response.getSuggestions().size() > request.getLimit()) {
            Collection suggestions = response.getSuggestions();
            Iterator iterator = suggestions.iterator();
            int count;
            for (count = 0; count < request.getLimit() && iterator.hasNext(); count++) {
                iterator.next();
            }
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
            if (lookup.isWildCard()) {
                suggestions.add(new ItemSuggestion("...", request.getQuery()));
            } else {
                suggestions.add(new ItemSuggestion("...", null));
            }
        }

        if (lookup.getTextBox().getText().trim().equalsIgnoreCase(request.getQuery().trim())) {
            if (response.getSuggestions().isEmpty()) {
                if (!lookup.getType().equals(LookupComposite.TYPE_SUPPLIER))
                    lookup.setErrorString("Term not found '" + request.getQuery() + "'");
                else
                    lookup.setErrorString("Supplier name '" + request.getQuery() + "' not found.");
            }
            callback.onSuggestionsReady(request, response);
        }
    }

    public void setRequest(SuggestOracle.Request request) {
        this.request = request;
    }
}
