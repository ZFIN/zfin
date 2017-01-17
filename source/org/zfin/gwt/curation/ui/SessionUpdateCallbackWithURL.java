package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback
 */
public class SessionUpdateCallbackWithURL implements AsyncCallback {


    private String url;

    public SessionUpdateCallbackWithURL(String url) {
        this.url = url;
    }

    public void onSuccess(Object object) {
        Window.open(url, "_self",
                "");
    }

    public void onFailure(Throwable throwable) {
        GWT.log("SessionSave update failed when URL handleCurationEvent: " + throwable);
    }
}
