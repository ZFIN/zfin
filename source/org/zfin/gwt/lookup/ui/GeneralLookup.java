package org.zfin.gwt.lookup.ui;

import com.google.gwt.i18n.client.Dictionary;
import org.zfin.gwt.root.util.JavaScriptPropertyReader;

import java.util.List;
import java.util.Set;


/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class GeneralLookup extends Lookup {

    public static final String JSREF_USE_TERM_TABLE = "useTermTable";

    public void onModuleLoad() {
        List<Dictionary> dictionaries = JavaScriptPropertyReader.getDictionaries();
        if (dictionaries == null || dictionaries.isEmpty())
            return;

        for (Dictionary lookupProperties : dictionaries) {
            Set keySet = lookupProperties.keySet();
            if (keySet.contains(JSREF_USE_TERM_TABLE)) {
                LookupTable lookupTable = new LookupTable();
                lookupTable.onModuleLoad(lookupProperties);
            }

        }
    }


}
