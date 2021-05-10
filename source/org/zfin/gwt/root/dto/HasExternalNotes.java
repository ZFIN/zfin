package org.zfin.gwt.root.dto;

import java.util.List;

public interface HasExternalNotes {

    List<NoteDTO> getPublicNoteList();

    void setPublicNoteList(List<NoteDTO> noteList);

    void addPublicNote(NoteDTO note);
}
