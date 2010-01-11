package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.NoteEvent;
import org.zfin.gwt.marker.event.NoteListener;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class NoteListBox extends Composite {

    // gui elements
    private VerticalPanel panel = new VerticalPanel();
    private FlexTable noteTable = new FlexTable();
    private HorizontalPanel addNewNoteField = new HorizontalPanel();
    private Button addButton = new Button("Add Note");
    private NoteBoxPopup newNoteBoxPopup = null;
    private boolean multipleNotes = true;

    // internal data
    private List notes;
    private String zdbID;

    // listeners
    private List<NoteListener> noteListeners = new ArrayList<NoteListener>();

    public NoteListBox(boolean multipleNotes) {
        this.multipleNotes = multipleNotes;
        initGui();
        initWidget(panel);
    }


    public NoteListBox() {
        this(true);
    }

    protected void initGui() {

        panel.add(noteTable);

        addNewNoteField.add(addButton);
        panel.add(addNewNoteField);

        addButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (newNoteBoxPopup == null) {
                    newNoteBoxPopup = new NewNoteBoxPopup(getNoteListBoxInstance());
                } else {
                    newNoteBoxPopup.reset();
                    newNoteBoxPopup.checkButtonStatus();
                    newNoteBoxPopup.show();
                }
            }
        });
    }


    public NoteListBox getNoteListBoxInstance() {
        return this;
    }

    public void addNoteListener(NoteListener noteListener) {
        noteListeners.add(noteListener);
    }

    public void removeNoteListener(NoteListener noteListener) {
        noteListeners.remove(noteListener);
    }

    public void clearNoteListeners() {
        noteListeners.clear();
    }

    public void fireAddNoteListener(NoteEvent noteEvent) {
        for (NoteListener noteListener : noteListeners) {
            noteListener.addNote(noteEvent);
        }
    }

    public void fireRemoveNoteListener(NoteEvent noteEvent) {
        for (NoteListener noteListener : noteListeners) {
            noteListener.removeNote(noteEvent);
        }
    }

    public void fireEditNoteListener(NoteEvent noteEvent) {
        for (NoteListener noteListener : noteListeners) {
            noteListener.editNote(noteEvent);
        }
    }

    public void updateNoteTable() {
        // remove all notes
        while (noteTable.getRowCount() > 0) {
            noteTable.removeRow(noteTable.getRowCount() - 1);
        }

        // add all notes
        for (int i = 0; (notes != null && i < notes.size()); i++) {
            if (notes.get(i) != null) {
                NoteLine noteLine = new NoteLine(notes.get(i).toString(), this);
//                noteTable.insertRow(i) ;
                noteTable.setWidget(i, 0, noteLine);
            }
        }

        if (noteTable.getRowCount() > 0) {
            addButton.setVisible(multipleNotes);
        }
    }


    public void setNotes(List notes) {
        this.notes = notes;
        updateNoteTable();
    }


    public void addNoteToGui(NoteEvent noteEvent) {
        NoteLine noteLine = new NoteLine(noteEvent.getNoteDTO().getNoteData(), this);
        int rowCount = noteTable.getRowCount();
        noteTable.insertRow(rowCount);
        noteTable.setWidget(rowCount, 0, noteLine);

        if (noteTable.getRowCount() > 0) {
            addButton.setVisible(multipleNotes);
        }
    }

    public int getRowForNote(String note) {
        for (int i = 0; i < noteTable.getRowCount(); i++) {
            if (((NoteLine) noteTable.getWidget(i, 0)).getNote().equals(note)) {
                return i;
            }
        }
        return -1;
    }

    public NoteLine getNoteLineForNote(String note) {
        int i = getRowForNote(note);
        if (i >= 0) {
            return (NoteLine) noteTable.getWidget(i, 0);
        } else {
            return null;
        }
    }

    public void removeNoteInGUI(NoteEvent noteEvent) {
        int index = getRowForNote(noteEvent.getNoteDTO().getIndexNote());
        if (index >= 0) {
            noteTable.removeRow(index);
        }

        // if it was not visible, make sure to show, will assume that it was more than 1 prior
        if (noteTable.getRowCount() == 0 || multipleNotes == false) {
            addButton.setVisible(true);
        }
    }

    public void editNoteInGUI(NoteEvent noteEvent) {
        NoteLine noteLine = getNoteLineForNote(noteEvent.getNoteDTO().getIndexNote());
        if (noteLine != null) {
            noteLine.setNote(noteEvent.getNoteDTO().getNoteData());
        }
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
