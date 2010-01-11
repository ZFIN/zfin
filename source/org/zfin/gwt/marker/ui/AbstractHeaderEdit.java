package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class AbstractHeaderEdit extends Composite implements HandlesError, PublicationChangeListener {


    // GUI
    protected final String TEXT_WORKING = "working...";
    protected final String TEXT_UPDATE = "update";

    // GUI elements
    protected VerticalPanel panel = new VerticalPanel();

    // GUI name/type elements
    protected Label zdbIDLabel = new Label("ZdbID: ");
    protected HTML zdbIDHTML = new HTML();
    protected TextBox nameBox = new TextBox();
    protected HorizontalPanel buttonPanel = new HorizontalPanel();
    protected Button updateButton = new Button(TEXT_UPDATE);
    protected Button revertButton = new Button("revert");
    protected Label errorLabel = new Label();


    // listeners
    protected List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    // internal data
    protected String publicationZdbID;

    // interactive elements
    protected PreviousNamesBox previousNamesBox = null ;

    protected abstract void initGUI();
    protected abstract void addInternalListeners(HandlesError handlesError);
    protected abstract void refreshGUI();
    protected abstract void revert();
    protected abstract boolean isDirty();
    protected abstract void sendUpdates();



    public void working() {
        updateButton.setText(TEXT_WORKING);
        updateButton.setEnabled(false);
        revertButton.setEnabled(false);
    }

    public void notWorking() {
        updateButton.setText(TEXT_UPDATE);
    }

    protected class CompareCommand implements Command {
        public void execute() {
            boolean isDirty = isDirty();

            if (true == isDirty) {
                updateButton.setEnabled(true);
                revertButton.setEnabled(true);
            } else {
                updateButton.setEnabled(false);
                revertButton.setEnabled(false);
            }
        }
    }

    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

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

    public void setPreviousNamesBox(PreviousNamesBox previousNamesBox) {
        this.previousNamesBox = previousNamesBox;
    }
}
