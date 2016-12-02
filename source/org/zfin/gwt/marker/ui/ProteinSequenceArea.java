package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.curation.event.AddNewFeatureEvent;
import org.zfin.gwt.curation.event.AddNewFeatureEventHandler;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.event.PublicationChangeEvent;
import org.zfin.gwt.root.event.PublicationChangeListener;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class ProteinSequenceArea extends Composite implements HandlesError, RequiresAttribution, PublicationChangeListener {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ProteinSequenceArea.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, ProteinSequenceArea> {
    }

    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    Label listBoxLabel;
    @UiField
    StringListBox databaseListBoxWrapper;
    @UiField
    NewSequenceBox newSequenceBox;
    @UiField()
    public Label errorLabel;

    // listeners
    private final List<SequenceAddListener> sequenceAddListeners = new ArrayList<>();
    private final List<HandlesError> handlesErrorListeners = new ArrayList<>();
    private boolean attributionRequired = true;
    private String publicationZdbID = "";

    // validator
    private final PublicationValidator publicationValidator = new PublicationValidator();


    public ProteinSequenceArea(String div) {
        VerticalPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(div).add(outer);
    }

    public ProteinSequenceArea() {
        initWidget(uiBinder.createAndBindUi(this));
//        initGUI();
        addInternalListeners(this);
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        if (showHideToggle.isVisible()) {
            fireSequenceAddStartListeners(new SequenceAddEvent());
        } else
            fireSequenceAddCancelListeners(new SequenceAddEvent());
    }

    private void addInternalListeners(final HandlesError handlesError) {

        newSequenceBox.addSequenceAddListener(new SequenceAddListener() {
            public void add(final SequenceAddEvent sequenceAddEvent) {
                if (!publicationValidator.validate(publicationZdbID, handlesError)) return;
                if (databaseListBoxWrapper.getSelected() == null
                        ||
                        AbstractListBox.NULL_STRING.equals(databaseListBoxWrapper.getSelected())) {
                    setError("Please select a blast database.");
                    return;
                }
                String validationError = newSequenceBox.checkSequence();
                if (validationError != null) {
                    setError(validationError);
                    return;
                }


                // on the way back, we should set the reference Database
                ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO();
                referenceDatabaseDTO.setZdbID(databaseListBoxWrapper.getSelected());
                sequenceAddEvent.setReferenceDatabaseDTO(referenceDatabaseDTO);

                // now we can fire the listeners
                fireSequenceAddListeners(sequenceAddEvent);
            }

            public void cancel(SequenceAddEvent sequenceAddEvent) {
                fireSequenceAddCancelListeners(sequenceAddEvent);
                showHideToggle.setVisibilityToHide();
            }

            public void start(SequenceAddEvent sequenceAddEvent) {
                fireSequenceAddStartListeners(sequenceAddEvent);
            }
        });
    }

    public String checkSequence() {
        return newSequenceBox.checkSequence();
    }

    public void activate() {
        newSequenceBox.activate();

        if (databaseListBoxWrapper.getItemCount() == 2) {
            databaseListBoxWrapper.setSelectedIndex(1);
        } else {
            databaseListBoxWrapper.setSelectedIndex(0);
        }
    }

    public void inactivate() {
        newSequenceBox.inactivate();
    }

    public void resetAndHide() {
        newSequenceBox.reset();
        newSequenceBox.setVisible(false);
    }

    private void fireSequenceAddListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.add(sequenceAddEvent);
        }
    }


    private void fireSequenceAddStartListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.start(sequenceAddEvent);
        }
    }

    private void fireSequenceAddCancelListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.cancel(sequenceAddEvent);
        }
    }

    public void addSequenceAddListener(SequenceAddListener sequenceAddListener) {
        clearError();
        sequenceAddListeners.add(sequenceAddListener);
    }

    public AbstractListBox getDatabaseListBoxWrapper() {
        return databaseListBoxWrapper;
    }

    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public boolean isAttributionRequired() {
        return attributionRequired;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAttributionRequired(boolean isAttributionRequired) {
        this.attributionRequired = isAttributionRequired;
    }

    public void onPublicationChanged(PublicationChangeEvent event) {
        this.publicationZdbID = event.getPublication();
    }

    public String getPublication() {
        return publicationZdbID;
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }


}
