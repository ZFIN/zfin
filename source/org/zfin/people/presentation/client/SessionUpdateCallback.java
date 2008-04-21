package org.zfin.people.presentation.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback
 */
public class SessionUpdateCallback implements AsyncCallback {

    public SessionUpdateCallback(){

    }

    public void onSuccess(Object object) {
        //Window.alert("success");
    }
    public void onFailure(Throwable throwable) {
        Window.alert("SessionSave update failed: "+ throwable);
    }
}
