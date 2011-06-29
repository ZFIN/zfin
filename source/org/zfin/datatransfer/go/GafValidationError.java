package org.zfin.datatransfer.go;

/**
 */
public class GafValidationError extends Exception {


    public GafValidationError(String s) {
        super(s);
    }

    public GafValidationError(String s, GafEntry gafEntry) {
        super(s + ":\n" + gafEntry);
    }

    public GafValidationError(String s, GafEntry gafEntry, Exception e) {
        super(s + ":\n" + gafEntry, e.fillInStackTrace());
    }

    public GafValidationError(String s, Exception e) {
        super(s, e.fillInStackTrace());
    }
}
