package org.zfin.infrastructure;

import org.zfin.ExternalNote;

import java.util.Set;

/**
 */
public interface EntityNotes {
    Set<DataNote> getDataNotes() ;
    Set<DataNote> getSortedDataNotes() ;
    Set<? extends ExternalNote> getExternalNotes() ;
    String getPublicComments() ;
}
