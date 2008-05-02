package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.core.client.EntryPoint;

import java.util.Set;

/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class Lookup implements EntryPoint {

    public static final String JSREF_DIV_NAME ="divName" ;
    public static final String JSREF_INPUT_NAME ="inputName" ;
    public static final String JSREF_TYPE ="type" ;
    public static final String JSREF_SHOWERROR ="showError" ;
    public static final String JSREF_TABLENAME ="tableName" ;
    public static final String JSREF_BUTTONTEXT ="buttonText" ;
    public static final String JSREF_WILDCARD ="wildcard" ;
    public static final String JSREF_WIDTH ="width" ;

    protected LookupComposite lookup  ;
    private String divName ;

    protected void handleProperties(){
        Dictionary lookupProperties = Dictionary.getDictionary("LookupProperties") ;
        // set options
        Set keySet = lookupProperties.keySet() ;
        if(keySet.contains(JSREF_DIV_NAME)){
            setDivName(lookupProperties.get(JSREF_DIV_NAME));
        }
        if(keySet.contains(JSREF_INPUT_NAME)){
            lookup.setInputName(lookupProperties.get(JSREF_INPUT_NAME));
        }
        if(keySet.contains(JSREF_TYPE)){
            lookup.setType(lookupProperties.get(JSREF_TYPE));
        }
        if(keySet.contains(JSREF_BUTTONTEXT)){
            lookup.setButtonText(lookupProperties.get(JSREF_BUTTONTEXT));
        }
        if(keySet.contains(JSREF_SHOWERROR)){
            lookup.setShowError(Boolean.valueOf(lookupProperties.get(JSREF_SHOWERROR)).booleanValue());
        }
        if(keySet.contains(JSREF_WILDCARD)){
            lookup.setWildCard(Boolean.valueOf(lookupProperties.get(JSREF_WILDCARD)).booleanValue());
        }
        if(keySet.contains(JSREF_WIDTH)){
            lookup.setSuggestBoxWidth(Integer.parseInt(lookupProperties.get(JSREF_WIDTH)));
        }
    }

    public void onModuleLoad() {
        // init gui
        lookup = new LookupComposite() ;
        handleProperties();
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
