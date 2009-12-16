package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.NoteDTO;
import org.zfin.marker.presentation.event.NoteEvent;

/**
 */
public class NoteLine extends Composite {

    private HorizontalPanel panel = new HorizontalPanel();
    private String imageURL = "/images/";
    private Image removeNoteButton = new Image(imageURL + "delete-button.png");
    private NoteListBox parent;
    private Label noteLabel = new Label();
    private Image editNoteButton = new Image(imageURL + "edit.png");
    private NoteBoxPopup noteBoxPopup = null;

    private final int MAX_LENGTH = 30;
    private final String APPEND_STRING = "...";

    // internal data
    private String note;


    public NoteLine(String note, NoteListBox parent) {
        this.parent = parent;
        this.note = note;
        noteLabel.setText(note);
        initGui();
        initWidget(panel);
    }

    public void initGui() {

        removeNoteButton.setStyleName("relatedEntityPubLink");
        removeNoteButton.setTitle("Delete note.");
        editNoteButton.setStyleName("relatedEntityPubLink");
        editNoteButton.setTitle("Edit note.");

        panel.add(removeNoteButton);

        panel.add(noteLabel);
        editNoteButton.setPixelSize(20, 20);
        panel.add(editNoteButton);

        removeNoteButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                DeferredCommand.addCommand(new Command() {
                    public void execute() {
                        NoteDTO noteDTO = new NoteDTO();
                        noteDTO.setIndexNote(note);
                        noteDTO.setNoteData(note);
                        parent.fireRemoveNoteListener(new NoteEvent(noteDTO));
                    }
                });
            }
        });


        editNoteButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                if (noteBoxPopup == null) {
                    noteBoxPopup = new EditNoteBoxPopup(parent, note);
                } else {
                    noteBoxPopup.setNote(note);
                    noteBoxPopup.checkButtonStatus();
                    noteBoxPopup.show();
                }
            }
        });


        updateNoteStatus();

    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        noteLabel.setText(note);
        updateNoteStatus();
    }

    protected void updateNoteStatus() {
        if (note.length() > MAX_LENGTH) {
            noteLabel.setText(note.substring(0, MAX_LENGTH) + APPEND_STRING);
        } else {
            noteLabel.setText(note);
        }
    }


}
