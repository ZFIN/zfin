package org.zfin.gwt.lookup.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.TermStatus;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.JavaScriptPropertyReader;
import org.zfin.gwt.root.util.LookupRPCService;

import java.util.*;


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
