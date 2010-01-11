package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.SequenceDTO;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class NewSequenceBox extends Composite {

    // gui components
    private VerticalPanel panel = new VerticalPanel();
    private HTML sequenceHTML = new HTML();
    private SequenceBox sequenceBox = new SequenceBox(SequenceBox.PROTEIN_SEQUENCE);
    private HorizontalPanel buttonPanel = new HorizontalPanel();
    private Label nameLabel = new Label();
    private Label publicationLabel = new Label();
    private Button addButton = new Button("Add Protein Sequence");
    private Button cancelButton = new Button("Cancel");


    // listeners
    private List<SequenceAddListener> sequenceAddListeners = new ArrayList<SequenceAddListener>();


    public NewSequenceBox() {
//        this(false) ;
        initGUI();
        initWidget(panel);
    }


    protected void initGUI() {
        sequenceHTML.setHTML("<b>Sequence:</b>");
        panel.add(sequenceHTML);
//        panel.setStyleName("newProteinBox");

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);


        addButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                SequenceAddEvent sequenceAddEvent = new SequenceAddEvent();
                SequenceDTO sequenceDTO = new SequenceDTO();
                sequenceDTO.setSequence(sequenceBox.getSequenceAsString());
                sequenceAddEvent.setSequenceDTO(sequenceDTO);
                fireSequenceAddListeners(sequenceAddEvent);
            }

        });

        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                reset();
                hideProteinBox();
                fireSequenceAddCancelListeners(new SequenceAddEvent());
            }
        });

        panel.add(sequenceBox);
        panel.setVisible(false);
    }

    public void reset() {
        nameLabel.setText("");
        sequenceBox.clearSequence();
    }

    public void activate() {
        addButton.setEnabled(true);
        cancelButton.setEnabled(true);
        sequenceBox.activate();
    }

    public void inactivate() {
        addButton.setEnabled(false);
        cancelButton.setEnabled(false);
        sequenceBox.inactivate();
    }

    public boolean isProteinBoxActive() {
        return panel.isVisible();
    }

    public void showProteinBox() {
        panel.setVisible(true);
    }

    public void hideProteinBox() {
        panel.setVisible(false);
    }

    public String getPublication() {
        return publicationLabel.getText();
    }

    public void setPublication(String publication) {
        publicationLabel.setText(publication);
    }

    public String getGeneratedName() {
        return nameLabel.getText();
    }

    public void setGeneratedName(String generatedName) {
        nameLabel.setText(generatedName);
    }

    protected void fireSequenceAddListeners(SequenceAddEvent sequenceAddEvent) {
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.add(sequenceAddEvent);
        }
    }

    protected void fireSequenceAddCancelListeners(SequenceAddEvent sequenceAddEvent) {
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.cancel(sequenceAddEvent);
        }
    }

    public void addSequenceAddListener(SequenceAddListener sequenceAddListener) {
        sequenceAddListeners.add(sequenceAddListener);
    }

    public void saveSuccessful() {
        sequenceBox.setVisible(false);

    }

    public String checkSequence() {
        return sequenceBox.checkSequence();
    }
}
