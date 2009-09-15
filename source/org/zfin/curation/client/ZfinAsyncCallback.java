package org.zfin.curation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;

/**
 * Basic Callback class that implements the onFailure() method that
 * can be inherited in most cases.
 * The constructor expects an error message in case somethings goes wrong and
 * second the GWT label that is used to show error messages.
 */
public abstract class ZfinAsyncCallback<T> implements AsyncCallback<T> {

    private String message;
    private Label errorLabel;
    private static final String LOGIN_REQUIRED = "login required";

    protected ZfinAsyncCallback(String errorMessage, Label errorLabel) {
        this.message = errorMessage;
        this.errorLabel = errorLabel;
    }

    public void onFailure(Throwable throwable){
        onFailureCleanup();
        if (handleConnectionError(throwable)) return;
        if (handleDuplicateRecords(throwable)) return;
        if (checkLogin(throwable)) return;
        displayMessage(message + throwable);
    }

    private boolean handleDuplicateRecords(Throwable throwable) {
        String message = throwable.getMessage();
        if(throwable instanceof Exception && message!= null && !message.equals(LOGIN_REQUIRED)){
            displayMessage(throwable.getMessage());
            return true;
        }
        return false;
    }

    private boolean handleConnectionError(Throwable t) {
        return (t instanceof RuntimeException
                &&
                t.getMessage().startsWith("Unable to read XmlHttpRequest.status; " +
                        "likely causes are a networking error or bad cross-domain request.")
        );
    }

    public void displayMessage(String s){
        if(errorLabel !=null){
            errorLabel.setText(s);
        }
        else{
            Window.alert(s);
        }
    }

    private boolean checkLogin(Throwable t){
        String message = t.getMessage();
        if (message != null && message.indexOf(LOGIN_REQUIRED) > -1) {
            Window.open("/action/login", "_blank", "status=1,toolbar=1,menubar=1,location=1,resizable=0,height=400,width=600");
            return true ;
        }
        return false ;
    }

    protected abstract void onFailureCleanup();
}
