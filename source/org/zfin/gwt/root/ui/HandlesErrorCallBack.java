package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import org.zfin.gwt.root.dto.TermNotFoundException;

/**
 * This class provides default failure behaviors for an attached component.
 */
public abstract class HandlesErrorCallBack<T> implements AsyncCallback<T> {


    protected String message = "";
    protected HandlesError handlesError = null;
    protected boolean showTrace = true ;

    public HandlesErrorCallBack(String message, HandlesError handlesError,boolean showTrace) {
        this(message);
        this.handlesError = handlesError;
        this.showTrace = showTrace ;
    }

    public HandlesErrorCallBack(String message, HandlesError handlesError) {
        this(message);
        this.handlesError = handlesError;
    }

    public HandlesErrorCallBack(String message) {
        this.message = message;
    }

    public abstract void onSuccess(T result);

    public void displayMessage(String s) {
        if (handlesError != null) {
            handlesError.setError(s);
        } else {
            Window.alert(s);
        }
    }

    public void onFailure(Throwable throwable) {
        if (handleConnectionError(throwable)) return;
        if (checkLogin(throwable)) return;
        if (handleOutOfDateError(throwable)) return;
        if (handleTermNotFound(throwable)) return;
        displayMessage(message + (showTrace ? throwable : "") );
    }

    protected boolean handleConnectionError(Throwable t) {
        return (t instanceof RuntimeException
                &&
                t.getMessage().startsWith("Unable to read XmlHttpRequest.status; likely causes are a networking error or bad cross-domain request.")
        );
    }

    protected boolean handleTermNotFound(Throwable t) {
        if (t instanceof TermNotFoundException) {
            TermNotFoundException termNotFoundException = (TermNotFoundException) t;
            displayMessage(termNotFoundException.getType() + "[" + termNotFoundException.getTerm() + "] not found.");
            return true;
        }
        return false;
    }


    protected boolean checkLogin(Throwable t) {
        String message = t.getMessage();
        if (message != null && message.indexOf("login required") > -1) {
            Window.open("/action/login", "_blank", "status=1,toolbar=1,menubar=1,location=1,resizable=0,height=400,width=600");
            return true;
        }
        return false;
    }

    protected boolean handleOutOfDateError(Throwable t) {
        if (t instanceof IncompatibleRemoteServiceException) {
            IncompatibleRemoteServiceException remoteServiceException = (IncompatibleRemoteServiceException) t;
            Window.alert("This application is out of date, please click the refresh button on your browser");
            return true;
        }
        return false;
    }

}