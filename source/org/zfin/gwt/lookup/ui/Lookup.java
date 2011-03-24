package org.zfin.gwt.lookup.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.ui.LookupComposite;

import java.util.Set;

/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class Lookup extends Composite {

    public static final String JSREF_DIV_NAME = "divName";
    public static final String JSREF_INPUT_NAME = "inputName";
    public static final String JSREF_TYPE = "type";
    public static final String JSREF_SHOWERROR = "showError";
    public static final String JSREF_BUTTONTEXT = "buttonText";
    public static final String JSREF_WILDCARD = "wildcard";
    public static final String JSREF_WIDTH = "width";
    public static final String JSREF_LIMIT = "limit";
    public static final String JSREF_ACTION = "action";
    public static final String JSREF_ONCLICK = "onclick";
    public static final String JSREF_OID = "OID";
    public static final String JSREF_ONTOLOGY_NAME = "ontologyName";

    public void onModuleLoad(Dictionary lookupProperties) {

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
                lookup.setShowError(Boolean.valueOf(lookupProperties.get(JSREF_SHOWERROR)));
            }
            if (keySet.contains(JSREF_WILDCARD)) {
                lookup.setWildCard(Boolean.valueOf(lookupProperties.get(JSREF_WILDCARD)));
            }
            if (keySet.contains(JSREF_WIDTH)) {
                lookup.setSuggestBoxWidth(Integer.parseInt(lookupProperties.get(JSREF_WIDTH)));
            }
            if (keySet.contains(JSREF_OID)) {
                lookup.setOId((lookupProperties.get(JSREF_OID)));
            }
            if (keySet.contains(JSREF_LIMIT)) {
                lookup.setLimit(Integer.parseInt(lookupProperties.get(JSREF_LIMIT)));
            }
            if (keySet.contains(JSREF_ONTOLOGY_NAME)) {
                lookup.setOntologyName(lookupProperties.get(JSREF_ONTOLOGY_NAME));
            }

            if (keySet.contains(JSREF_ACTION)) {
                if (lookupProperties.get(JSREF_ACTION).equals(LookupComposite.ACTION_ANATOMY_SEARCH)) {
                    lookup.setAction(new AnatomySearchSubmitAction());
                } else if (lookupProperties.get(JSREF_ACTION).equals(LookupComposite.ACTION_TERM_SEARCH)) {
                    lookup.setAction(new TermSearchSubmitAction(lookup.getOntology()));
                } else if (lookupProperties.get(JSREF_ACTION).equals(LookupComposite.ACTION_MARKER_ATTRIBUTE)) {
                    lookup.setAction(new MarkerAttributeSubmitAction(lookup.getOId()));
                } else if (lookupProperties.get(JSREF_ACTION).equals(LookupComposite.ACTION_FEATURE_ATTRIBUTE)) {
                    lookup.setAction(new FeatureAttributeSubmitAction(lookup.getOId()));
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
    }


}
