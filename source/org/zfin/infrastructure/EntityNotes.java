package org.zfin.infrastructure;

import java.util.Set;

/**
 */
public interface EntityNotes {
    Set<DataNote> getDataNotes() ;
    Set<DataNote> getSortedDataNotes() ;
    String getPublicComments() ;
    String getExternalNotes() ;
}
