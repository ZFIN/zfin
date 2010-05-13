package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.event.PublicationChangeEvent;
import org.zfin.gwt.root.event.PublicationChangeListener;
import org.zfin.gwt.root.ui.AbstractListBox;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.PublicationValidator;
import org.zfin.gwt.root.ui.StringListBox;

import java.util.ArrayList;
import java.util.List;

public class ProteinSequenceArea extends Composite implements HandlesError, RequiresAttribution, PublicationChangeListener {

    // gui
    private final VerticalPanel panel = new VerticalPanel();
    private final HorizontalPanel blastDatabasePanel = new HorizontalPanel();
    private final Label listBoxLabel = new Label("Blast Database:");
    private final StringListBox databaseListBoxWrapper = new StringListBox();
    private final HTML link = new HTML();
    private final NewSequenceBox newSequenceBox = new NewSequenceBox();
    private final Label errorLabel = new Label();

    private final static String RIGHT_ARROW = "<a href=#proteinLookup><img align=\"top\" src=\"/images/right.gif\" >Add Protein</a>";
    private final static String DOWN_ARROW = "<a href=#proteinLookup><img align=\"top\" src=\"/images/down.gif\" >Add Protein</a>";

    // listeners
    private final List<SequenceAddListener> sequenceAddListeners = new ArrayList<SequenceAddListener>();
    private final List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();
    private boolean attributionRequired = true;
    private String publicationZdbID = "";

    // validator
    private final PublicationValidator publicationValidator = new PublicationValidator();


    public ProteinSequenceArea(String div) {
        this();
        RootPanel.get(div).add(this);
    }

    public ProteinSequenceArea() {
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }


    void initGUI() {
//        link.setTargetHistoryToken("proteinLookup");
        link.setHTML(RIGHT_ARROW);

        link.setVisible(true);

        blastDatabasePanel.add(listBoxLabel);
        blastDatabasePanel.add(databaseListBoxWrapper);
        blastDatabasePanel.setVisible(false);
        newSequenceBox.setVisible(false);
        blastDatabasePanel.setVisible(false);

        panel.add(link);

        panel.add(blastDatabasePanel);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);
        panel.add(newSequenceBox);


    }

    private void addInternalListeners(final HandlesError handlesError) {

        link.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (newSequenceBox.isVisible()) {
                    link.setHTML(RIGHT_ARROW);
                    newSequenceBox.setVisible(false);
                    blastDatabasePanel.setVisible(false);
                    fireSequenceAddCancelListeners(new SequenceAddEvent());
                } else {
                    link.setHTML(DOWN_ARROW);
                    newSequenceBox.setVisible(true);
                    blastDatabasePanel.setVisible(true);
                    fireSequenceAddStartListeners(new SequenceAddEvent());
                }
            }
        });


        newSequenceBox.addSequenceAddListener(new SequenceAddListener() {
            public void add(final SequenceAddEvent sequenceAddEvent) {
                if (false == publicationValidator.validate(publicationZdbID, handlesError)) return;
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
        newSequenceBox.hideProteinBox();
        blastDatabasePanel.setVisible(false);
        link.setHTML(RIGHT_ARROW);
    }

    void fireSequenceAddListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.add(sequenceAddEvent);
        }
    }


    void fireSequenceAddStartListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.start(sequenceAddEvent);
        }
    }

    void fireSequenceAddCancelListeners(SequenceAddEvent sequenceAddEvent) {
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
