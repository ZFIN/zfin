package org.zfin.marker.presentation.client;

import org.zfin.marker.presentation.event.NoteEvent;
import org.zfin.marker.presentation.event.NoteListener;
import org.zfin.marker.presentation.dto.NoteDTO;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This widget contains all of its own listeners.
 */
public class CuratorNoteBox extends NoteListBox{

    public CuratorNoteBox(String div){
        super(true) ;
        addInternalListeners() ;
        RootPanel.get(div).add(this);
    }

    public void resetInternalListeners(){
        clearNoteListeners();
        addInternalListeners();
    }

    protected void addInternalListeners(){
        addNoteListener(new NoteListener() {
            public void addNote(final NoteEvent noteEvent) {
                NoteDTO noteDTO = noteEvent.getNoteDTO();
                noteDTO.setCuratorZdbID(getCuratorZdbID());
                noteDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().addCuratorNote(noteDTO,
                        new MarkerEditCallBack<Void>("failed to add note: ") {
                            public void onSuccess(Void o) {
                                addNoteToGui(noteEvent);
                            }
                        });


            }

            public void removeNote(final NoteEvent noteEvent) {
                NoteDTO noteDTO = noteEvent.getNoteDTO();
                noteDTO.setCuratorZdbID(getCuratorZdbID());
                noteDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().removeCuratorNote(noteDTO,
                        new MarkerEditCallBack<Void>("failed to remove note: ") {
                            public void onSuccess(Void o) {
                                removeNoteInGUI(noteEvent);
                            }
                        });
            }

            public void editNote(final NoteEvent noteEvent) {
                NoteDTO noteDTO = noteEvent.getNoteDTO();
                noteDTO.setCuratorZdbID(getCuratorZdbID());
                noteDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().editCuratorNote(noteDTO,
                        new MarkerEditCallBack<Void>("failed to remove note: ") {
                            public void onSuccess(Void o) {
                                editNoteInGUI(noteEvent);
                            }
                        });
            }
        });

    }
}