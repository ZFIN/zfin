package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import org.zfin.marker.presentation.event.NoteEvent;
import org.zfin.marker.presentation.dto.NoteDTO;

/**
 */
public class NewNoteBoxPopup extends NoteBoxPopup {

    public NewNoteBoxPopup(NoteListBox noteListBox){
        super(noteListBox) ;
    }


    public void initGUI(){
        setTitle("Note");
        setStyleName("notePopup");
        topPanel.setWidth("300px"); // for some reason have to over-ride css here in order to foce the width
        topPanel.add(title, DockPanel.WEST);
        closeButton.setTitle("Close") ;
        closeButton.setStyleName("relatedEntityPubLink");
        topPanel.add(closeButton,DockPanel.EAST) ;
        topPanel.setCellHorizontalAlignment(title,HasAlignment.ALIGN_LEFT);
        topPanel.setCellHorizontalAlignment(closeButton,HasAlignment.ALIGN_RIGHT);
        panel.add(topPanel);

        noteArea.setStyleName("notePopupTextArea");
        noteArea.setVisibleLines(5);

        panel.add(noteArea);

        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);

        panel.add(buttonPanel);

        saveButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                NoteDTO noteDTO = new NoteDTO() ;
                noteDTO.setNoteData(noteArea.getText());
                parent.fireAddNoteListener(new NoteEvent(noteDTO));
                hide();
            }
        });

		closeButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                hide();
            }
        });

        noteArea.addKeyboardListener(new KeyboardListenerAdapter(){
            public void onKeyUp(Widget widget, char c, int i) {
                super.onKeyUp(widget, c,
                        i);    //To change body of overridden methods use File | Settings | File Templates.
                checkButtonStatus();
            }
        });


        setPopupPositionAndShow(new PositionCallback(){
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = (Window.getClientWidth() - offsetWidth) / 3;
                int top = (Window.getClientHeight() - offsetHeight) / 3;
                setPopupPosition(left, top);
            }
        });
    }

    public void checkButtonStatus(){
        if(isDirty()){
            saveButton.setEnabled(true);
        }
        else{
            saveButton.setEnabled(false);
        }
    }

    public boolean isDirty(){
        return noteArea.getText().trim().length() > 0 ;
    }

}
