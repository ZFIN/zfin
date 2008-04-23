package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.core.client.EntryPoint;

/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class Lookup implements EntryPoint {

    public static final String JSREF_DIV_NAME ="divName" ;
    public static final String JSREF_INPUT_NAME ="inputName" ;
    public static final String JSREF_TYPE ="type" ;
    public static final String JSREF_SHOWBUTTON ="showButton" ;
    public static final String JSREF_SHOWERROR ="showError" ;

    private String divName ;

    public void onModuleLoad() {

        LookupComposite lookup = new LookupComposite() ;

        Dictionary lookupProperties = Dictionary.getDictionary("LookupProperties") ;

        // set options
        setDivName(lookupProperties.get(JSREF_DIV_NAME));
        lookup.setInputName(lookupProperties.get(JSREF_INPUT_NAME));
        lookup.setType(lookupProperties.get(JSREF_TYPE));
        lookup.setShowButton(Boolean.valueOf(lookupProperties.get(JSREF_SHOWBUTTON)).booleanValue());
        lookup.setShowError(Boolean.valueOf(lookupProperties.get(JSREF_SHOWERROR)).booleanValue());

        // init gui
        lookup.initGui();

        RootPanel.get(getDivName()).add(lookup);

    }

    public String getDivName() {
        return divName;
    }

    public void setDivName(String divName) {
        this.divName = divName;
    }
}
