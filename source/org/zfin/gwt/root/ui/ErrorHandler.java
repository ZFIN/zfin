package org.zfin.gwt.root.ui;

/**
 * Interface that defines an error handler.
 *
 * 1) it allows to set an error messages as well as clear the error message: clearError()  and setError()
 * 2) other error handler can be registered to this error handler that will
 * be clear out whenever appropriate: clearAllErrors()
 * 3) TO have another element handle your error display register the element with addErrorHandler().
 */
public interface ErrorHandler {

    /**
     * Set error message for this element that will be displayed.
     *
     * @param message error message
     */
    void setError(String message);

    /**
     * Clear the error message that may be currently displayed.
     */
    void clearError();

    /**
     * Clear all errors of the current element and all registered error handlers.
     */
    void clearAllErrors();

    /**
     * Register an error handler that needs to be notified when to call the clearError() method.
     *
     * @param errorHandler error handler
     */
    void addErrorHandler(ErrorHandler errorHandler);

}
