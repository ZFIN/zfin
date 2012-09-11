package org.zfin.profile;

import java.sql.Blob;

/**
 */
public interface HasSnapshot {

    String getZdbID();
    Blob getSnapshot();

}
