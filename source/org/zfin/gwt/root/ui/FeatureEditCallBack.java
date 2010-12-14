package org.zfin.gwt.root.ui;

/**
 * This class provides default failure behaviors.
 */
public abstract class FeatureEditCallBack<T> extends HandlesErrorCallBack<T> {


    public FeatureEditCallBack(String message, HandlesError handlesError) {
        super(message, handlesError);
    }

    public FeatureEditCallBack(String message) {
        super(message);
    }

    @Override
    public void onFailure(Throwable throwable) {
        if (handleConnectionError(throwable)) return;
        if (checkLogin(throwable)) return;
        if (handleDuplicateEntry(throwable)) return;
        if (handleOutOfDateError(throwable)) return;
        if (handleTermNotFound(throwable)) return;
        // only new one here

        displayMessage(message + (showTrace ? throwable : "") );
    }

}




   

