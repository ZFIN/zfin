package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.zfin.gwt.marker.event.NoteEvent;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.HandlesError;

/**
 */
public class PublicNoteEntry extends AbstractNoteEntry {
    public PublicNoteEntry(NoteDTO noteDTO, HandlesError handlesError) {
        super(noteDTO,handlesError);
        noteDTO.setEditMode(NoteBox.EditMode.PUBLIC.name());
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);

        removeNoteButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                NoteDTO updatedNoteDTO = noteDTO.clone();
                updatedNoteDTO.setNoteData("");
                MarkerRPCService.App.getInstance().editPublicNote(updatedNoteDTO, new MarkerEditCallBack<Void>("Failed to remove public note: "){
                    @Override
                    public void onSuccess(Void result) {
                        noteDTO.setNoteData("");
                        fireRemoveNote(new NoteEvent(noteDTO));
                    }
                });
            }
        });


        saveButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                final NoteDTO noteDTO = getNoteDTO().clone();
                noteDTO.setNoteData(noteText.getText());
                MarkerRPCService.App.getInstance().editPublicNote(noteDTO, new MarkerEditCallBack<Void>("Failed to update note: "){
                    @Override
                    public void onSuccess(Void result) {
                        getNoteDTO().setNoteData(noteText.getText());
                        revertGUI();
                        parent.fireEventSuccess();
                    }
                });
            }
        });

    }


}
