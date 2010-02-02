package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for data boxes (e.g., clone and antibody).
 */
public abstract class AbstractDataBox<T extends MarkerDTO> extends AbstractRevertibleComposite<T> {

    // error label
    protected Label errorLabel = new Label();

    // listeners
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();



    protected abstract T createDTOFromGUI() ;


    protected void initGUI() {
        buttonPanel.add(saveButton);
        buttonPanel.add(revertButton);
        errorLabel.setStyleName("error");
    }

    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    public void fireEventSuccess() {
        clearError();
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

}