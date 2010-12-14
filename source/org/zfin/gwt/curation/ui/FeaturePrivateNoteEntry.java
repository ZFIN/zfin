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
public class FeaturePrivateNoteEntry extends AbstractNoteEntry {
    public FeaturePrivateNoteEntry(NoteDTO noteDTO, HandlesError handlesError) {
        super(noteDTO, handlesError);
        noteDTO.setNoteEditMode(NoteEditMode.PRIVATE);
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        super.addInternalListeners(handlesError);

        removeNoteButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                FeatureRPCService.App.getInstance().removeCuratorNote(noteDTO, new FeatureEditCallBack<Void>("Failed to remove curator note: ") {
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
                FeatureRPCService.App.getInstance().editCuratorNote(noteDTO, new FeatureEditCallBack<Void>("Failed to update note: ") {
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