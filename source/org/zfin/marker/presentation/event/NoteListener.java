package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.event.NoteEvent;

/**
 */
public interface NoteListener {

    public void addNote(final NoteEvent noteEvent) ;
    public void removeNote(final NoteEvent noteEvent) ;
    public void editNote(final NoteEvent noteEvent) ; 
}
