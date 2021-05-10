package org.zfin.gwt.root.ui;

/**
 * Component is able to show error and fire success is no error.
 */
public interface HandlesError {

    void setError(String message) ;
    void clearError() ;
    void fireEventSuccess() ;
    void addHandlesErrorListener(HandlesError handlesError) ;
}
