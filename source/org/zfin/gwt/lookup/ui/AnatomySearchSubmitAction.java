package org.zfin.gwt.lookup.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.ui.SubmitAction;

/**
 */
public class AnatomySearchSubmitAction implements SubmitAction {

    // We cannot use URLEncoder as it is not serializable in GWT...
    public void doSubmit(String value) {
        value = value.replaceAll(" ", "%20");
        if (value!= null) {
            Window.open("/action/ontology/term-detail/" + value.replaceAll(" ", "%20"), "_self",
                    "");
        }
    }
}
