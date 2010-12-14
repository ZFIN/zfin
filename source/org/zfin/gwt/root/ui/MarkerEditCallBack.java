package org.zfin.gwt.root.ui;

/**
 * This class provides default failure behaviors.
 */
public abstract class MarkerEditCallBack<T> extends HandlesErrorCallBack<T> {


    public MarkerEditCallBack(String message, HandlesError handlesError,boolean showTrace) {
        super(message, handlesError,showTrace);
    }

    public MarkerEditCallBack(String message, HandlesError handlesError) {
        super(message, handlesError);
    }

    public MarkerEditCallBack(String message) {
        super(message);
    }

    @Override
    public void onFailure(Throwable throwable) {
        if (handleConnectionError(throwable)) return;
        if (checkLogin(throwable)) return;
        if (handleOutOfDateError(throwable)) return;
        if (handleTermNotFound(throwable)) return;
        // only new one here
        if (handleDBLinkNotFound(throwable)) return;
        if (handleDuplicateEntry(throwable)) return;
        displayMessage(message + (showTrace ? throwable : "") );
    }


    private boolean handleDBLinkNotFound(Throwable t) {
        if (t instanceof DBLinkNotFoundException) {
            DBLinkNotFoundException dbLinkNotFoundException = (DBLinkNotFoundException) t;
            displayMessage(dbLinkNotFoundException.getMessage());
            return true;
        }
        return false;
    }


}
