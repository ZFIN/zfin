package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.zfin.gwt.marker.event.NoteEvent;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.HandlesError;

/**
 */
public class PrivateNoteEntry extends AbstractNoteEntry {
    public PrivateNoteEntry(NoteDTO noteDTO,HandlesError handlesError) {
        super(noteDTO,handlesError);
        noteDTO.setEditMode(NoteBox.EditMode.PRIVATE.name());
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);

        removeNoteButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                MarkerRPCService.App.getInstance().removeCuratorNote(noteDTO, new MarkerEditCallBack<Void>("Failed to remove curator note: "){
                    @Override
                    public void onSuccess(Void result) {
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
                MarkerRPCService.App.getInstance().editCuratorNote(noteDTO, new MarkerEditCallBack<Void>("Failed to update note: "){
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