package org.zfin.gwt.marker.ui;

/**
 * Impled that an object has an error div.
 */
public interface HandlesError {

    void setError(String message) ;
    void clearError() ;
    void fireEventSuccess() ;
    void addHandlesErrorListener(HandlesError handlesError) ;
}
