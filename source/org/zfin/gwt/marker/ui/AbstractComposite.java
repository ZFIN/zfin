package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides basic composite.
 */
public abstract class AbstractComposite<T extends RelatedEntityDTO> extends Composite implements HandlesError, PublicationChangeListener {

    // GUI elements
    final VerticalPanel panel = new VerticalPanel();
    final Label errorLabel = new Label();

    // listeners
    private final List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    // internal data
    String publicationZdbID;
    T dto ;

    // validator
    protected final PublicationValidator publicationValidator = new PublicationValidator();


    protected abstract void revertGUI();
    protected abstract void initGUI();
    protected abstract void setValues();
    protected abstract void addInternalListeners(HandlesError handlesError);

    public void setError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    @Override
    public void publicationChanged(PublicationChangeEvent event) {
        publicationZdbID = event.getPublication();
    }

    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    public T getDTO() {
        return dto;
    }

    public void setDTO(T dto) {
        this.dto = dto;
        revertGUI();
    }

}
