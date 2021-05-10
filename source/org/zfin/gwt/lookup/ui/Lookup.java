package org.zfin.gwt.lookup.ui;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.ui.LookupComposite;

import java.util.Set;

/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class Lookup extends Composite {

    public void onModuleLoad(Dictionary lookupProperties) {

        LookupComposite lookup = new LookupComposite(false);
        // set options
        Set keySet = lookupProperties.keySet();
        if (keySet.contains(LookupStrings.JSREF_INPUT_NAME)) {
            lookup.setInputName(lookupProperties.get(LookupStrings.JSREF_INPUT_NAME));
        }
        if (keySet.contains(LookupStrings.JSREF_TYPE)) {
            lookup.setType(lookupProperties.get(LookupStrings.JSREF_TYPE));
        }
        if (keySet.contains(LookupStrings.JSREF_BUTTONTEXT)) {
            lookup.setButtonText(lookupProperties.get(LookupStrings.JSREF_BUTTONTEXT));
        }
        if (keySet.contains(LookupStrings.JSREF_SHOWERROR)) {
            lookup.setShowError(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_SHOWERROR)));
        }
        if (keySet.contains(LookupStrings.JSREF_WILDCARD)) {
            lookup.setWildCard(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_WILDCARD)));
        }
        if (keySet.contains(LookupStrings.JSREF_WIDTH)) {
            lookup.setSuggestBoxWidth(Integer.parseInt(lookupProperties.get(LookupStrings.JSREF_WIDTH)));
        }
        if (keySet.contains(LookupStrings.JSREF_OID)) {
            lookup.setOId((lookupProperties.get(LookupStrings.JSREF_OID)));
        }
        if (keySet.contains(LookupStrings.JSREF_LIMIT)) {
            lookup.setLimit(Integer.parseInt(lookupProperties.get(LookupStrings.JSREF_LIMIT)));
        }
        if (keySet.contains(LookupStrings.JSREF_ONTOLOGY_NAME)) {
            lookup.setOntologyName(lookupProperties.get(LookupStrings.JSREF_ONTOLOGY_NAME));
        }
        if (keySet.contains(LookupStrings.JSREF_USE_ID_AS_TERM)) {
            lookup.setUseIdAsValue(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_USE_ID_AS_TERM)));
        }
        if (keySet.contains(LookupStrings.JSREF_TERMS_WITH_DATA_ONLY)) {
            lookup.setUseTermsWithDataOnly(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_TERMS_WITH_DATA_ONLY)));
        }
        if (keySet.contains(LookupStrings.JSREF_ANATOMY_TERMS_ONLY)) {
            lookup.setUseAnatomyTermsOnly(Boolean.valueOf(lookupProperties.get(LookupStrings.JSREF_ANATOMY_TERMS_ONLY)));
        }

        if (keySet.contains(LookupStrings.JSREF_ACTION)) {
            if (lookupProperties.get(LookupStrings.JSREF_ACTION).equals(LookupStrings.ACTION_ANATOMY_SEARCH)) {
                lookup.setAction(new AnatomySearchSubmitAction());
            } else if (lookupProperties.get(LookupStrings.JSREF_ACTION).equals(LookupStrings.ACTION_TERM_SEARCH)) {
                lookup.setAction(new TermSearchSubmitAction(lookup.getOntology()));
            } else if (lookupProperties.get(LookupStrings.JSREF_ACTION).equals(LookupStrings.ACTION_MARKER_ATTRIBUTE)) {
                lookup.setAction(new MarkerAttributeSubmitAction(lookup.getOId()));
            } else if (lookupProperties.get(LookupStrings.JSREF_ACTION).equals(LookupStrings.ACTION_FEATURE_ATTRIBUTE)) {
                lookup.setAction(new FeatureAttributeSubmitAction(lookup.getOId()));
            }
        }
        if (keySet.contains(LookupStrings.JSREF_ONCLICK)) {
            String onclickEvent = lookupProperties.get(LookupStrings.JSREF_ONCLICK);
            lookup.setOnclick(onclickEvent);
        }

        lookup.initGui();

        if (keySet.contains(LookupStrings.JSREF_DIV_NAME)) {
            RootPanel.get(lookupProperties.get(LookupStrings.JSREF_DIV_NAME)).add(lookup);
        }
    }


}
