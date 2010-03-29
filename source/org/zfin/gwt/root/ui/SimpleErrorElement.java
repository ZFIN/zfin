package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is a simple Error Handler composite, consisting of a Label that displays the
 * error message. As it implements ErrorHandler it handles other error handlers that
 * need to be cleared when this error message is cleared.
 */
public class SimpleErrorElement extends Label implements ErrorHandler {

    // store all external error handlers that need to be updated when
    // errors should be cleared.
    private Collection<ErrorHandler> handlesErrorListeners = new ArrayList<ErrorHandler>(5);

    /**
     * Default constructor. Use if no visible error element is used but you
     * still want to manage other error handler that are dependent on it.
     */
    public SimpleErrorElement(){

    }

    public SimpleErrorElement(String divElement) {
        RootPanel panel = RootPanel.get(divElement);
        panel.add(this);
        addStyleName(WidgetUtil.ERROR);
    }

    public void setError(String message) {
        setText(message);
    }

    public void clearError() {
        setText("");
    }

    public void clearAllErrors() {
        clearError();
        for (ErrorHandler handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    public void addErrorHandler(ErrorHandler handlesError) {
        handlesErrorListeners.add(handlesError);
    }
}
