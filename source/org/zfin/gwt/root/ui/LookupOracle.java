package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public abstract class LookupOracle extends SuggestOracle {

    public abstract void doLookup(final Request request, final Callback callback);

    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        String query = request.getQuery();
        if (query.length() >= 3) {
            doLookup(request, callback);
        }
    }

}
