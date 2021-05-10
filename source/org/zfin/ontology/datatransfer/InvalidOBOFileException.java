package org.zfin.ontology.datatransfer;

public class InvalidOBOFileException extends RuntimeException {

    public InvalidOBOFileException(String message) {
        super("Aborting load due to invalid OBO file. Report this as a bug to the ontology developer:\n" + message);
    }
}
