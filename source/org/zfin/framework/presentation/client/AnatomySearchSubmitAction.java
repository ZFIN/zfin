package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.Window;

/**
 */
public class AnatomySearchSubmitAction implements SubmitAction{

    public void doSubmit(String value) {
        if (value!= null) {
            Window.open("/action/anatomy/search?action=term-search&searchTerm=" + value.replaceAll(" ", "%20"), "_self",
                    "");
        }
    }
}
