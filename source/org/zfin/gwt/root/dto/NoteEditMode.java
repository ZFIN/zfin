package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Edit mode for the note.
*/
public enum NoteEditMode implements IsSerializable {
    PUBLIC,
    PRIVATE,
    EXTERNAL,;
}
