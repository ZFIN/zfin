package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class ItemSuggestion implements IsSerializable, Suggestion {
    private String display;
    private String replacement;

    // Required for IsSerializable to work
    public ItemSuggestion() {}

    // Convenience method for creation of a suggestion
    public ItemSuggestion(String display, String replace) {
        this.display = display;
        replacement = replace;
    }

    public String getDisplayString() {
        return display;
    }

    public String getReplacementString() {
        return replacement;
    }

} 
