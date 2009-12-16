package org.zfin.framework.presentation.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.HashSet;
import java.util.Set;

/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class Lookup implements EntryPoint {

    public static final String JSREF_DIV_NAME = "divName";
    public static final String JSREF_INPUT_NAME = "inputName";
    public static final String JSREF_TYPE = "type";
    public static final String JSREF_SHOWERROR = "showError";
    public static final String JSREF_TABLENAME = "tableName";
    public static final String JSREF_BUTTONTEXT = "buttonText";
    public static final String JSREF_WILDCARD = "wildcard";
    public static final String JSREF_WIDTH = "width";
    public static final String JSREF_LIMIT = "limit";
    public static final String JSREF_ACTION = "action";
    public static final String JSREF_ONCLICK = "onclick";
    public static final String JSREF_OID = "OID";

    //    protected LookupComposite lookup  ;
    //    private String divName ;
    protected final static String LOOKUP_STRING = "LookupProperties";
    private final static String NUMLOOKUPS_STRING = "NumLookups";
    private Set lookups = new HashSet();

    public void onModuleLoad() {
        // init gui

        int numLookups = 0;
        try {
            String numLookupStrings = Dictionary.getDictionary(LOOKUP_STRING).get(NUMLOOKUPS_STRING);
            numLookups = Integer.parseInt(numLookupStrings);
        }
        catch (Exception nfe) {
            // not a proper # so returning
            return;
        }

        for (int i = 0; i < numLookups; i++) {
            Dictionary lookupProperties = Dictionary.getDictionary(LOOKUP_STRING + i);
            if (lookupProperties == null) {
                return;
            }
            LookupComposite lookup = new LookupComposite();
            // set options
            Set keySet = lookupProperties.keySet();
            if (keySet.contains(JSREF_INPUT_NAME)) {
                lookup.setInputName(lookupProperties.get(JSREF_INPUT_NAME));
            }
            if (keySet.contains(JSREF_TYPE)) {
                lookup.setType(lookupProperties.get(JSREF_TYPE));
            }
            if (keySet.contains(JSREF_BUTTONTEXT)) {
                lookup.setButtonText(lookupProperties.get(JSREF_BUTTONTEXT));
            }
            if (keySet.contains(JSREF_SHOWERROR)) {
                lookup.setShowError(Boolean.valueOf(lookupProperties.get(JSREF_SHOWERROR)).booleanValue());
            }
            if (keySet.contains(JSREF_WILDCARD)) {
                lookup.setWildCard(Boolean.valueOf(lookupProperties.get(JSREF_WILDCARD)).booleanValue());
            }
            if (keySet.contains(JSREF_WIDTH)) {
                lookup.setSuggestBoxWidth(Integer.parseInt(lookupProperties.get(JSREF_WIDTH)));
            }
            if (keySet.contains(JSREF_OID)) {
                lookup.setOID((lookupProperties.get(JSREF_OID)));
            }
            if (keySet.contains(JSREF_LIMIT)) {
                lookup.setLimit(Integer.parseInt(lookupProperties.get(JSREF_LIMIT)));
            }

            if (keySet.contains(JSREF_ACTION)) {
                if (lookupProperties.get(JSREF_ACTION).equals(LookupComposite.ACTION_ANATOMY_SEARCH)) {
                    lookup.setAction(new AnatomySearchSubmitAction());
                } else if (lookupProperties.get(JSREF_ACTION).equals(LookupComposite.ACTION_MARKER_ATTRIBUTE)) {
                    lookup.setAction(new MarkerAttributeSubmitAction(lookup.getOID()));
                } else if (lookupProperties.get(JSREF_ACTION).equals(LookupComposite.ACTION_FEATURE_ATTRIBUTE)) {
                    lookup.setAction(new FeatureAttributeSubmitAction(lookup.getOID()));
                }
            }
            if (keySet.contains(JSREF_ONCLICK)) {
                String onclickEvent = lookupProperties.get(JSREF_ONCLICK);
                lookup.setOnclick(onclickEvent);
            }


            lookup.initGui();

            if (keySet.contains(JSREF_DIV_NAME)) {
                RootPanel.get(lookupProperties.get(JSREF_DIV_NAME)).add(lookup);
            }

            lookups.add(lookup);
        }
    }

}
