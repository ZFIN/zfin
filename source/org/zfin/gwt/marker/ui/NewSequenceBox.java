package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.marker.event.SequenceAddEvent;
import org.zfin.gwt.marker.event.SequenceAddListener;
import org.zfin.gwt.root.dto.SequenceDTO;

import java.util.ArrayList;
import java.util.List;

/**
 */
class NewSequenceBox extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("NewSequenceBox.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, NewSequenceBox> {
    }

    @UiField(provided = true)
    SequenceBox sequenceBox = new SequenceBox(SequenceBox.PROTEIN_SEQUENCE);
    @UiField
    Button addButton;
    @UiField
    Button cancelButton;
    @UiField
    VerticalPanel sequencePanel;


    @UiHandler("addButton")
    void onAddSequence(@SuppressWarnings("unused") ClickEvent event) {
        SequenceAddEvent sequenceAddEvent = new SequenceAddEvent();
        SequenceDTO sequenceDTO = new SequenceDTO();
        sequenceDTO.setSequence(sequenceBox.getSequenceAsString());
        sequenceAddEvent.setSequenceDTO(sequenceDTO);
        fireSequenceAddListeners(sequenceAddEvent);
    }

    @UiHandler("cancelButton")
    void onCancelReset(@SuppressWarnings("unused") ClickEvent event) {
        reset();
        fireSequenceAddCancelListeners(new SequenceAddEvent());
    }

    // listeners
    private final List<SequenceAddListener> sequenceAddListeners = new ArrayList<>();


    public NewSequenceBox() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void reset() {
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

    public void hideProteinBox() {
        sequencePanel.setVisible(false);
    }

    void fireSequenceAddListeners(SequenceAddEvent sequenceAddEvent) {
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.add(sequenceAddEvent);
        }
    }

    void fireSequenceAddCancelListeners(SequenceAddEvent sequenceAddEvent) {
        for (SequenceAddListener sequenceAddListener : sequenceAddListeners) {
            sequenceAddListener.cancel(sequenceAddEvent);
        }
    }

    public void addSequenceAddListener(SequenceAddListener sequenceAddListener) {
        sequenceAddListeners.add(sequenceAddListener);
    }

    public String checkSequence() {
        return sequenceBox.checkSequence();
    }
}
