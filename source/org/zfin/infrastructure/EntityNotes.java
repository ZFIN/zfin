package org.zfin.infrastructure;

import java.util.Set;

/**
 */
public interface EntityNotes {
    public Set<DataNote> getDataNotes() ;
    public Set<DataNote> getSortedDataNotes() ;
    public String getPublicComments() ;
}
