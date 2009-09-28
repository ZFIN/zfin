package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import org.zfin.framework.presentation.client.TermNotFoundException;

/**
 * This class provides default failure behaviors
 */
public abstract class MarkerEditCallBack<T> implements AsyncCallback<T>{


    private String message = "";
    private HandlesError handlesError = null ;

    public MarkerEditCallBack(String message, HandlesError handlesError){
        this(message) ;
        this.handlesError = handlesError;
    }

    public MarkerEditCallBack(String message){
        this.message = message ;
    }

    public abstract void onSuccess(T result) ;



    public void onFailure(Throwable throwable) {
        if (handleConnectionError(throwable)) return;
        if (checkLogin(throwable)) return;
        if (handleTermNotFound(throwable)) return;
        if (handleDBLinkNotFound(throwable)) return;
        displayMessage(message + throwable);
    }

    public void displayMessage(String s){
        if(handlesError !=null){
            handlesError.setError(s) ;
        }
        else{
            Window.alert(s);
        }
    }


    private boolean handleConnectionError(Throwable t) {
        return (t instanceof RuntimeException
                &&
                t.getMessage().startsWith("Unable to read XmlHttpRequest.status; likely causes are a networking error or bad cross-domain request.")
        );
    }

    private boolean handleTermNotFound(Throwable t) {
        if (t instanceof TermNotFoundException ){
            TermNotFoundException termNotFoundException = (TermNotFoundException) t ;
            displayMessage(termNotFoundException.getType() +  "[" + termNotFoundException.getTerm()+ "] not found.");
            return true ;
        }
        return false ;
    }

    private boolean handleDBLinkNotFound(Throwable t) {
        if (t instanceof DBLinkNotFoundException){
            DBLinkNotFoundException dbLinkNotFoundException = (DBLinkNotFoundException) t ;
            displayMessage(dbLinkNotFoundException.getMessage() );
            return true ;
        }
        return false ;
    }


    private boolean checkLogin(Throwable t){
        String message = t.getMessage();
        if (message != null && message.indexOf("login required") > -1) {
            Window.open("/action/login", "_blank", "status=1,toolbar=1,menubar=1,location=1,resizable=0,height=400,width=600");
            return true ;
        }
        return false ;
    }
}
