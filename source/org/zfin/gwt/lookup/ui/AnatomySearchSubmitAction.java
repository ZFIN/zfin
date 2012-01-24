package org.zfin.gwt.lookup.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.ui.SubmitAction;

/**
 */
public class AnatomySearchSubmitAction implements SubmitAction {

    public void doSubmit(String value) {
        if (value!= null) {
            Window.open("/action/anatomy/anatomy-do-search?searchTerm=" + value.replaceAll(" ", "%20"), "_self",
                    "");
        }
    }
}
