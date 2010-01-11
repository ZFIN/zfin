package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.NoteEvent;
import org.zfin.gwt.marker.event.NoteListener;
import org.zfin.gwt.root.dto.NoteDTO;

/**
 * This widget contains all of its own listeners.
 */
public class PublicNoteBox extends NoteListBox {

    public PublicNoteBox(String div) {
        super(false);
        addInternalListeners();
        RootPanel.get(div).add(this);
    }

    public void resetInternalListeners() {
        clearNoteListeners();
        addInternalListeners();
    }

    protected void addInternalListeners() {
        addNoteListener(new NoteListener() {
            public void addNote(final NoteEvent noteEvent) {
                NoteDTO noteDTO = noteEvent.getNoteDTO();
                noteDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().editPublicNote(noteDTO,
                        new MarkerEditCallBack<Void>("failed to add note: ") {
                            public void onSuccess(Void o) {
                                addNoteToGui(noteEvent);
                            }
                        });
            }

            public void removeNote(final NoteEvent noteEvent) {
                NoteDTO noteDTO = noteEvent.getNoteDTO();
                noteDTO.setDataZdbID(getZdbID());
                noteDTO.setNoteData(null);
                MarkerRPCService.App.getInstance().editPublicNote(noteDTO,
                        new MarkerEditCallBack<Void>("failed to remove note: ") {
                            public void onSuccess(Void o) {
                                removeNoteInGUI(noteEvent);
                            }
                        });
            }

            public void editNote(final NoteEvent noteEvent) {
                NoteDTO noteDTO = noteEvent.getNoteDTO();
                noteDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().editPublicNote(noteDTO,
                        new MarkerEditCallBack<Void>("failed to remove note: ") {
                            public void onSuccess(Void o) {
                                editNoteInGUI(noteEvent);
                            }
                        });
            }
        });
    }

}