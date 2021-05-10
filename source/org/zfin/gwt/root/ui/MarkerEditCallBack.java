package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.util.AppUtils;

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

    ZfinModule module;
    AjaxCallEventType eventType;

    public MarkerEditCallBack(String message, HandlesError handlesError, ZfinModule module, AjaxCallEventType eventType) {
        super(message, handlesError);
        this.eventType = eventType;
        this.module = module;
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

    public void onFinish() {
        if (module != null && eventType != null)
            AppUtils.fireAjaxCall(module, eventType);
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
