package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.dto.NoteEditMode;
import org.zfin.gwt.root.event.NoteEvent;
import org.zfin.gwt.root.ui.AbstractNoteEntry;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;

/**
 */
public class FeaturePublicNoteEntry extends AbstractNoteEntry {
    public FeaturePublicNoteEntry(NoteDTO noteDTO, HandlesError handlesError) {
        super(noteDTO, handlesError);
        noteDTO.setNoteEditMode(NoteEditMode.PUBLIC);
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);

        removeNoteButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                NoteDTO updatedNoteDTO = noteDTO.clone();
                updatedNoteDTO.setNoteData("");
                FeatureRPCService.App.getInstance().editPublicNote(updatedNoteDTO, new FeatureEditCallBack<Void>("Failed to remove public note: ") {
                    @Override
                    public void onSuccess(Void result) {
                        noteDTO.setNoteData("");
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
                FeatureRPCService.App.getInstance().editPublicNote(noteDTO, new FeatureEditCallBack<Void>("Failed to update note: ") {
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
