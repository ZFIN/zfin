package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.NoteDTO;
import org.zfin.marker.presentation.event.NoteEvent;

/**
 */
public class EditNoteBoxPopup extends NoteBoxPopup {


    protected Button revertButton;

    public EditNoteBoxPopup(NoteListBox noteListBox, String note) {
        super(noteListBox, note);
    }


    public boolean isDirty() {
        return false == noteArea.getText().equals(note);
    }

    public void initGUI() {
        setTitle("Note");
        setStyleName("notePopup");

        topPanel.setWidth("300px"); // for some reason have to over-ride css here in order to foce the width
        topPanel.add(title, DockPanel.WEST);
        closeButton.setTitle("Close");
        closeButton.setStyleName("relatedEntityPubLink");
        topPanel.add(closeButton, DockPanel.EAST);
        topPanel.setCellHorizontalAlignment(title, HasAlignment.ALIGN_LEFT);
        topPanel.setCellHorizontalAlignment(closeButton, HasAlignment.ALIGN_RIGHT);
        panel.add(topPanel);

        noteArea.setStyleName("notePopupTextArea");
        noteArea.setVisibleLines(5);

        panel.add(noteArea);

        buttonPanel.add(saveButton);
        panel.add(buttonPanel);

        saveButton.setEnabled(false);

        revertButton = new Button("revert");
        revertButton.setEnabled(false);
        buttonPanel.add(revertButton);

        saveButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                save();
                hide();
            }
        });

        noteArea.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyUp(Widget widget, char c, int i) {
                super.onKeyUp(widget, c,
                        i);    //To change body of overridden methods use File | Settings | File Templates.
                checkButtonStatus();
            }
        });

        closeButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                revert();
                hide();
                checkButtonStatus();
            }
        });

        revertButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                revert();
                checkButtonStatus();
            }
        });


        setPopupPositionAndShow(new PositionCallback() {
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = (Window.getClientWidth() - offsetWidth) / 3;
                int top = (Window.getClientHeight() - offsetHeight) / 3;
                setPopupPosition(left, top);

            }
        });

    }

    public void checkButtonStatus() {
        if (isDirty()) {
            revertButton.setEnabled(true);
            saveButton.setEnabled(true);
        } else {
            revertButton.setEnabled(false);
            saveButton.setEnabled(false);
        }
    }

    public void revert() {
        noteArea.setText(note);
    }

    public void save() {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setNoteData(noteArea.getText());
        noteDTO.setIndexNote(note);
        parent.fireEditNoteListener(new NoteEvent(noteDTO));
    }
}
