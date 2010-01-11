package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;

import java.util.ArrayList;
import java.util.List;

public class ProteinSequenceArea extends Composite implements HandlesError, RequiresAttribution, PublicationChangeListener {

    // gui
    private VerticalPanel panel = new VerticalPanel();
    private HorizontalPanel blastDatabasePanel = new HorizontalPanel();
    private Label listBoxLabel = new Label("Blast Database:");
    private EasyListBox databaseListBoxWrapper = new EasyListBox();
    private Hyperlink link = new Hyperlink();
    private NewSequenceBox newSequenceBox = new NewSequenceBox();
    private Label errorLabel = new Label();

    private final static String RIGHT_ARROW = "<a href=#proteinLookup><img align=\"top\" src=\"/images/right.gif\" >Add Protein</a>";
    private final static String DOWN_ARROW = "<a href=#proteinLookup><img align=\"top\" src=\"/images/down.gif\" >Add Protein</a>";

    // listeners
    private List<SequenceAddListener> sequenceAddListeners = new ArrayList<SequenceAddListener>();
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();
    private boolean attributionRequired = true;
    private String publicationZdbID = "";

    public ProteinSequenceArea(String div) {
        this();
        RootPanel.get(div).add(this);
    }

    public ProteinSequenceArea() {
        initGUI();
        initWidget(panel);
    }


    protected void initGUI() {
        link.setTargetHistoryToken("proteinLookup");
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


        link.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (true == newSequenceBox.isVisible()) {
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
                if (publicationZdbID == null || publicationZdbID.length() < 16) {
                    setError("Attribution required to add sequence.");
                    return;
                }
                if (databaseListBoxWrapper.getSelectedString() == null
                        ||
                        EasyListBox.NULL_STRING.equals(databaseListBoxWrapper.getSelectedString())) {
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
                referenceDatabaseDTO.setZdbID(databaseListBoxWrapper.getSelectedString());
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

    protected void fireSequenceAddListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.add(sequenceAddEvent);
        }
    }


    protected void fireSequenceAddStartListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.start(sequenceAddEvent);
        }
    }

    protected void fireSequenceAddCancelListeners(SequenceAddEvent sequenceAddEvent) {
        clearError();
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.cancel(sequenceAddEvent);
        }
    }

    public void addSequenceAddListener(SequenceAddListener sequenceAddListener) {
        clearError();
        sequenceAddListeners.add(sequenceAddListener);
    }

    public EasyListBox getDatabaseListBoxWrapper() {
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

    public void publicationChanged(PublicationChangeEvent event) {
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
