package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

/**
 * Generic Change Handler that can be used to clear out error messages upon
 * any change in a widget.
 */
public class ClearErrorMessagesChangeHandler implements ChangeHandler {


    private ErrorHandler errorElement;

    public ClearErrorMessagesChangeHandler(ErrorHandler errorElement) {
        this.errorElement = errorElement;
    }

    public void onChange(ChangeEvent changeEvent) {
        errorElement.clearAllErrors();
    }

}
