package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.DirectAttributionEvent;
import org.zfin.gwt.marker.event.DirectAttributionListener;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

import java.util.ArrayList;
import java.util.List;

public class DirectAttributionTable extends Composite
        implements CanRemoveReference, PublicationChangeListener, HandlesError {

    // gui
    private HorizontalPanel attributionPanel = new HorizontalPanel();
    private Button attributePubButton = new Button("Attribute Pub");
    private Label defaultPubLabel = new Label();
    private Label errorLabel = new Label();
    private FlexTable attributedPubTable = new FlexTable();// contains the pubs

    private VerticalPanel panel = new VerticalPanel();

    // listeners
    private List<DirectAttributionListener> directAttributionListeners = new ArrayList<DirectAttributionListener>();
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    // internal data
    private List<String> recordAttributions;
    private String zdbID;

    public DirectAttributionTable() {
        initGUI();
        initWidget(panel);
    }


    public void initGUI() {
        attributionPanel.add(attributePubButton);
        attributionPanel.add(new HTML("&nbsp;"));
        defaultPubLabel.setStyleName("relatedEntityDefaultPub");
        attributionPanel.add(defaultPubLabel);

        panel.add(attributedPubTable);
        panel.add(attributionPanel);

        errorLabel.setStyleName("error");
        panel.add(errorLabel);

        attributePubButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                addPublication(defaultPubLabel.getText());
            }
        });
    }


    public void setRecordAttributions(List<String> recordAttributions) {
        this.recordAttributions = recordAttributions;
        refreshGUI();
    }

    public void refreshGUI() {
        for (String recordAttribution : recordAttributions) {
            addPublicationToGUI(recordAttribution);
        }
    }

    public boolean addPublication(final String publicationZdbID) {
        if (publicationZdbID == null || publicationZdbID.length() < 16) {
            setError("Must select a valid publication: " + publicationZdbID);
            return false;
        } else if (true == containsPublication(publicationZdbID)) {
            setError("Already contains publication: " + publicationZdbID);
            return false;
        }

        fireAttributionAdded(new DirectAttributionEvent(publicationZdbID));

        return true;
    }

    public void setPublication(String publicationZdbID) {
        defaultPubLabel.setText(publicationZdbID);
    }

    public int getPublicationIndex(String publicationZdbID) {
        for (int i = 0; i < attributedPubTable.getRowCount(); i++) {
            if (((PublicationAttributionLabel) attributedPubTable.getWidget(i, 0)).getPublication().equals(publicationZdbID)) {
                return i;
            }
        }
        return -1;
    }

    public int getNumberOfPublications() {
        return attributedPubTable.getRowCount();
    }

    public boolean containsPublication(String publicationZdbID) {
        return getPublicationIndex(publicationZdbID) >= 0;
    }

    public boolean addPublicationToGUI(String publicationZdbID) {
        if (containsPublication(publicationZdbID)) {
            return false;
        }

        // create a blank one
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setEditable(true);
        PublicationAttributionLabel publicationAttributionLabel = new PublicationAttributionLabel(this, publicationZdbID, "", relatedEntityDTO);
        int numRows = attributedPubTable.getRowCount();
        attributedPubTable.insertRow(numRows);
        attributedPubTable.setWidget(numRows, 0, publicationAttributionLabel);

        return true;
    }


    public boolean removeReferenceFromGUI(String publicationZdbID) {
        int size = attributedPubTable.getRowCount();
        for (int i = 0; i < size; i++) {
            if (((PublicationAttributionLabel) attributedPubTable.getWidget(i, 0)).getPublication().equals(publicationZdbID)) {
                attributedPubTable.removeRow(i);
                return true;
            }
        }
        return false;

    }

    public void removeAttribution(RelatedEntityDTO relatedEntityDTO) {
        String publicationZdbID = relatedEntityDTO.getPublicationZdbID();
        fireAttributionRemoved(new DirectAttributionEvent(publicationZdbID));
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public void fireAttributionRemoved(DirectAttributionEvent directAttributionEvent) {
        fireEventSuccess();
        for (DirectAttributionListener directAttributionListener : directAttributionListeners) {
            directAttributionListener.remove(directAttributionEvent);
        }
    }


    public void fireAttributionAdded(DirectAttributionEvent directAttributionEvent) {
        fireEventSuccess();
        for (DirectAttributionListener directAttributionListener : directAttributionListeners) {
            directAttributionListener.add(directAttributionEvent);
        }
    }

    public void addDirectAttributionListener(DirectAttributionListener directAttributionListener) {
        directAttributionListeners.add(directAttributionListener);
    }

    public void publicationChanged(PublicationChangeEvent event) {
        fireEventSuccess();
        setPublication(event.getPublication());
    }

    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public void fireEventSuccess() {
        clearError();
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }
}
