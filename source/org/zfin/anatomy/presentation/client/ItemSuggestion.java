package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class ItemSuggestion implements IsSerializable, Suggestion {
    private String display;
    private String replacement;

    // Required for IsSerializable to work
    public ItemSuggestion() {}

    // Convenience method for creation of a suggestion
    public ItemSuggestion(String disp, String rep) {
        display = disp;
        replacement = rep;
    }

    public String getDisplayString() {
        return display;
    }

    public String getReplacementString() {
        return replacement;
    }

} 
