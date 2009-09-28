package org.zfin.sequence.blast;

/**
 */
public class BlastDatabaseException extends Exception{
    public BlastDatabaseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BlastDatabaseException(String s) {
        super(s);
    }
}
