package org.zfin.gwt.root.ui;

import java.io.Serializable;

/**
 */
public class BlastDatabaseAccessException extends Exception implements Serializable {

    /**
     * Provided for serialization.
     */
    public BlastDatabaseAccessException() {
    }

    public BlastDatabaseAccessException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BlastDatabaseAccessException(String s) {
        super(s);
    }
}
