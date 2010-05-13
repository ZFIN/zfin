package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

import java.util.Collection;
import java.util.Iterator;


/**
 */
public class LookupCallback implements AsyncCallback<SuggestOracle.Response> {

    private SuggestOracle.Request request;
    private SuggestOracle.Callback callback;

    public LookupCallback(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
        this.request = request;
        this.callback = callback;
    }

    @Override
    public void onFailure(Throwable throwable) {
        callback.onSuggestionsReady(request, new SuggestOracle.Response());
    }

    @Override
    public void onSuccess(SuggestOracle.Response response) {
        Collection collection = response.getSuggestions();
        int limit = 15;
        if (collection.size() > limit) {
            Iterator iterator = collection.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                iterator.next();
                ++i;
                if (i > limit) {
                    iterator.remove();
                }
            }
            collection.add(new ItemSuggestion(ItemSuggestCallback.END_ELLIPSE, null));
        }


        callback.onSuggestionsReady(request, response);
    }
}
