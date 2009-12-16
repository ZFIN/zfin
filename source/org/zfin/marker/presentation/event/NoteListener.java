package org.zfin.marker.presentation.event;

/**
 */
public interface NoteListener {

    public void addNote(final NoteEvent noteEvent);

    public void removeNote(final NoteEvent noteEvent);

    public void editNote(final NoteEvent noteEvent);
}
