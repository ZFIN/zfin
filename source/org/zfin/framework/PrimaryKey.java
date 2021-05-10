package org.zfin.framework;

/**
 * Every business object eing persisted should implement this class to ensure that the
 * primary key is always named the same way. It also defines the ownerID to allow ownership
 * checking.
 */
public interface PrimaryKey {

    String getZdbID();

    void setZdbID(String id);

    String getOwnerID();

    void setOwnerID(String id);
    
}
