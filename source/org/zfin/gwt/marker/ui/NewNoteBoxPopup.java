package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.marker.event.NoteEvent;
import org.zfin.gwt.root.dto.NoteDTO;

/**
 */
public class NewNoteBoxPopup extends NoteBoxPopup {

    public NewNoteBoxPopup(NoteListBox noteListBox) {
        super(noteListBox);
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

        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);

        panel.add(buttonPanel);

        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                NoteDTO noteDTO = new NoteDTO();
                noteDTO.setNoteData(noteArea.getText());
                parent.fireAddNoteListener(new NoteEvent(noteDTO));
                hide();
            }
        });

        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
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
            saveButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
        }
    }

    public boolean isDirty() {
        return noteArea.getText().trim().length() > 0;
    }

}
