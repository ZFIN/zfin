package org.zfin.gwt.lookup.ui;

import com.google.gwt.user.client.Window;
import org.zfin.publication.CurationPresentation;

/**
 * This class attributes a marker.
 */
public class MarkerAttributeSubmitAction extends AbstractAttributeSubmitAction {


    public MarkerAttributeSubmitAction(String OID) {
        super(OID);
    }

    public void doSubmit(String value) {

        if (OID == null) {
            Window.alert("Can not proceed OID is null");
            return;
        }
        value = value.replace("+", "%2B");
        if (value != null) {
            setCookieInPage("attribution_update", "update", "pubcur_c_");
            setCookieInPage("anchor", "attrib", "pubcur_c_");
            Window.open("action/curation/" + OID, "_self", "");
        }
    }

}
