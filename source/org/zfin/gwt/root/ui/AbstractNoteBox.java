package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.dto.NoteEditMode;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.NoteEvent;
import org.zfin.gwt.root.event.RemovableNoteListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Integrates curated/private, public, and external notes.
 * We need to configure this so that only a set number of things to add/edit.
 */
public abstract class AbstractNoteBox<T extends RelatedEntityDTO>
        extends AbstractRevertibleComposite<T> implements RemovableNoteListener {

    // gui components
    protected VerticalPanel panel = new VerticalPanel();
    protected FlexTable table = new FlexTable();

    // add stuff
    protected HorizontalPanel southPanel = new HorizontalPanel();
    protected Button addButton = new Button("Add Note");
    protected Button cancelButton = new Button("Cancel");
    protected StringListBox typeListBox = new StringListBox();
    protected TextArea newNoteTextArea = new TextArea();

    // internal data
    protected Set<NoteEditMode> noteEditModes = new HashSet<>();
    protected NoteEditMode defaultNoteEditMode = null;


    protected void initGUI() {

        panel.add(table);
        panel.setStyleName("gwt-editbox");


        newNoteTextArea.setWidth("400");
        southPanel.add(typeListBox);
        southPanel.add(newNoteTextArea);
        southPanel.add(addButton);
        southPanel.add(cancelButton);

        panel.add(southPanel);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);
    }


    protected void addInternalListeners(final HandlesError handlesError) {

        newNoteTextArea.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                handleDirty();
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                resetAddNote();
            }
        });
    }

    protected void resetAddNote() {
        newNoteTextArea.setText("");
        setValues();
        handleDirty();
//        typeListBox.setSelectedIndex(0);
    }


    @Override
    public boolean isDirty() {
        if (false == newNoteTextArea.getText().equals("")) return true;
//        if (typeListBox.getSelected()!=null) return true ;

        return false;
    }


    public void addEditMode(NoteEditMode noteEditMode) {
        noteEditModes.add(noteEditMode);
    }

    public boolean handleDirty() {
        boolean dirty = isDirty();
        saveButton.setEnabled(dirty);
        revertButton.setEnabled(dirty);
        if (dirty) {
            newNoteTextArea.setStyleName(IsDirtyWidget.DIRTY_STYLE);
        } else {
            newNoteTextArea.setStyleName(IsDirtyWidget.CLEAN_STYLE);
            fireEventSuccess();
        }
        return dirty;
    }

    public boolean hasDirtyNotes() {
        int numRows = table.getRowCount();
        for (int i = 0; i < numRows; i++) {
            if (((AbstractNoteEntry) table.getWidget(i, 1)).isDirty()) return true;
        }
        return false;
    }

    @Override
    public void removeNote(NoteEvent noteEvent) {
        NoteDTO noteDTO = noteEvent.getNoteDTO();
        int row = getRowForNote(noteDTO);
        table.removeRow(row);
        setValues();
    }

    private int getRowForNote(NoteDTO noteDTO) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (((AbstractNoteEntry) table.getWidget(i, 1)).getNoteDTO().equals(noteDTO)) {
                return i;
            }
        }
        return -1;
    }


    public void removeEditMode(NoteEditMode noteEditMode) {
        if (noteEditModes.contains(noteEditMode)) {
            noteEditModes.remove(noteEditMode);
        }
    }

    public void setDefaultEditMode(NoteEditMode noteEditMode) {
        this.defaultNoteEditMode = noteEditMode;
    }

    public void setPublicationID(String publicationID) {
        this.publicationZdbID = publicationID;
    }
}