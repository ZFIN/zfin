package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.event.NoteEvent;
import org.zfin.gwt.root.event.RemovableNoteListener;

import java.util.ArrayList;
import java.util.List;


/**
 * This class contains the text area field and the update/revert buttons
 */
public abstract class AbstractNoteEntry extends AbstractRevertibleComposite {

    private final HorizontalPanel panel = new HorizontalPanel();
    private final String imageURL = "/images/";
    protected Image removeNoteButton = new Image(imageURL + "delete-button.png");
    protected TextArea noteText = new TextArea();

    // internal data
    protected NoteDTO noteDTO;
    protected HandlesError parent;

    // listeners
    private final List<RemovableNoteListener> removableNoteListeners = new ArrayList<RemovableNoteListener>();

    protected AbstractNoteEntry(NoteDTO noteDTO, HandlesError parent) {
        this.noteDTO = noteDTO;
        this.parent = parent;
        noteText.setText(noteDTO.getNoteData());
        initGUI();
        addInternalListeners(this);
        initWidget(panel);
    }

    protected void initGUI() {

        noteText.setWidth("400");
        panel.add(noteText);

        removeNoteButton.setStyleName("relatedEntityPubLink");
        removeNoteButton.setTitle("Delete note");
        panel.add(removeNoteButton);

        panel.add(saveButton);
        panel.add(revertButton);

        noteText.setText(noteDTO.getNoteData());

        handleDirty();
//        updateNoteStatus();

    }

    @Override
    protected void setValues() {
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {

        noteText.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });

        noteText.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handleDirty();
            }
        });

        noteText.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                boolean dirty = isDirty();
                saveButton.setEnabled(dirty);
                revertButton.setEnabled(dirty);
                noteText.setFocus(true);
                // changing the style messes stuff up
            }
        });

        revertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                revertGUI();
            }
        });

    }

    public NoteDTO getNoteDTO() {
        return noteDTO;
    }


    public boolean handleDirty() {
        boolean dirty = isDirty();
        saveButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
        if (dirty) {
            noteText.setStyleName(IsDirty.DIRTY_STYLE);
        } else {
            noteText.setStyleName(IsDirty.CLEAN_STYLE);
            fireEventSuccess();
        }
        return dirty;
    }


    @Override
    public boolean isDirty() {
        return (false == noteText.getText().equals(noteDTO.getNoteData()));
    }

    @Override
    protected void revertGUI() {
        noteText.setText(noteDTO.getNoteData());
        handleDirty();
    }

    public void addNoteListener(RemovableNoteListener removableNoteListener) {
        removableNoteListeners.add(removableNoteListener);
    }


    protected void fireRemoveNote(NoteEvent noteEvent) {
        for (RemovableNoteListener removableNoteListener : removableNoteListeners) {
            removableNoteListener.removeNote(noteEvent);
        }
    }
}