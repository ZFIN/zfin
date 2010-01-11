package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.marker.event.NoteEvent;
import org.zfin.gwt.root.dto.NoteDTO;

/**
 */
public class NoteEntryField extends Composite {

    private HorizontalPanel panel = new HorizontalPanel();
    private String imageURL = "/images/";
//    private Image removeNoteButton = new Image(imageURL+"minimize.gif");
    private Image removeNoteButton = new Image(imageURL + "x-symbol.png");
    private NoteListBox parent;
//    private Hyperlink noteHTML = new Hyperlink();
    private Label noteLabel = new Label();
    private Image editNoteButton = new Image(imageURL + "edit.png");
    private NoteBoxPopup noteBoxPopup = null;

    private final int MAX_LENGTH = 30;
    private final String APPEND_STRING = "...";

    // internal data
    private String note;


    public NoteEntryField(String note, NoteListBox parent) {
        this.parent = parent;
        this.note = note;
        noteLabel.setText(note);
        initGui();
        initWidget(panel);
    }

    public void initGui() {

        removeNoteButton.setStyleName("attributionPubLink");
        removeNoteButton.setTitle("Delete note.");
        editNoteButton.setStyleName("attributionPubLink");
        editNoteButton.setTitle("Edit note.");

        panel.add(removeNoteButton);

//        noteHTML.setStyleName("noteField");
//        noteHTML.setStylePrimaryName("noteField");

        panel.add(noteLabel);
        editNoteButton.setPixelSize(20, 20);
        panel.add(editNoteButton);

        removeNoteButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                DeferredCommand.addCommand(new Command() {
                    public void execute() {
                        NoteDTO noteDTO = new NoteDTO();
                        noteDTO.setDataZdbID(note);
                        parent.fireRemoveNoteListener(new NoteEvent(noteDTO));
                    }
                });
            }
        });


        editNoteButton.addClickHandler(new ClickHandler() {
            @Override
            public int hashCode() {
                return super.hashCode();    //To change body of overridden methods use File | Settings | File Templates.
            }

            public void onClick(ClickEvent event) {
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