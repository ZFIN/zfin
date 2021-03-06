package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.dto.NoteEditMode;
import org.zfin.gwt.root.event.NoteEvent;

/**
 */
public class ExternalNoteEntry extends AbstractNoteEntry {
    public ExternalNoteEntry(NoteDTO noteDTO, HandlesError handlesError) {
        super(noteDTO, handlesError);
        noteDTO.setNoteEditMode(NoteEditMode.EXTERNAL);
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);

        removeNoteButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                MarkerRPCService.App.getInstance().removeExternalNote(noteDTO, new MarkerEditCallBack<Void>("Failed to remove external note: ") {
                    @Override
                    public void onSuccess(Void result) {
                        fireRemoveNote(new NoteEvent(noteDTO));
                    }
                });
            }
        });


        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final NoteDTO noteDTO = getNoteDTO().clone();
                noteDTO.setNoteData(noteText.getText());
                MarkerRPCService.App.getInstance().editExternalNote(noteDTO, new MarkerEditCallBack<Void>("Failed to update note: ") {
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