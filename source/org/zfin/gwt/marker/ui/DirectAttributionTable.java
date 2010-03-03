package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.DirectAttributionListener;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite for adding direcct attributions.
 */
public class DirectAttributionTable extends Composite
        implements CanRemoveReference, PublicationChangeListener, HandlesError {

    // gui
    private final HorizontalPanel attributionPanel = new HorizontalPanel();
    private final Button attributePubButton = new Button("Attribute Pub");
    private final Label defaultPubLabel = new Label();
    private final Label errorLabel = new Label();
    private final FlexTable attributedPubTable = new FlexTable();// contains the pubs

    private final VerticalPanel panel = new VerticalPanel();

    // listeners
    private final List<DirectAttributionListener> directAttributionListeners = new ArrayList<DirectAttributionListener>();
    private final List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    // internal data
    private List<String> recordAttributions;
    private String zdbID;

    // validator
    private PublicationValidator publicationValidator = new PublicationValidator();

    DirectAttributionTable() {
        initGUI();
        initWidget(panel);
    }


    void initGUI() {
        attributionPanel.add(attributePubButton);
        attributionPanel.add(new HTML("&nbsp;"));
        defaultPubLabel.setStyleName("relatedEntityDefaultPub");
        attributionPanel.add(defaultPubLabel);

        panel.add(attributedPubTable);
        panel.add(attributionPanel);
        panel.setStyleName("gwt-editbox");

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

    void refreshGUI() {
        if(recordAttributions!=null){
            for (String recordAttribution : recordAttributions) {
                addPublicationToGUI(recordAttribution);
            }
        }
    }

    public boolean addPublication(final String publicationZdbID) {
        if(false==publicationValidator.validate(publicationZdbID,this)){
            return false ;
        }

        if (containsPublication(publicationZdbID)) {
            setError("Already contains publication: " + publicationZdbID);
            return false;
        }

        fireAttributionAdded(publicationZdbID);

        return true;
    }

    void setPublication(String publicationZdbID) {
        defaultPubLabel.setText(publicationZdbID);
    }

    int getPublicationIndex(String publicationZdbID) {
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

    boolean addPublicationToGUI(String publicationZdbID) {
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


    boolean removeReferenceFromGUI(String publicationZdbID) {
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
        fireAttributionRemoved(publicationZdbID);
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    void fireAttributionRemoved(String pubZdbID) {
        fireEventSuccess();
        for (DirectAttributionListener directAttributionListener : directAttributionListeners) {
            directAttributionListener.remove(pubZdbID);
        }
    }


    void fireAttributionAdded(String pubZdbID) {
        fireEventSuccess();
        for (DirectAttributionListener directAttributionListener : directAttributionListeners) {
            directAttributionListener.add(pubZdbID);
        }
    }

    public void addDirectAttributionListener(DirectAttributionListener directAttributionListener) {
        directAttributionListeners.add(directAttributionListener);
    }

    public void onPublicationChanged(PublicationChangeEvent event) {
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
