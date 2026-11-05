package org.zfin.publication;

/** A publication already has a file stored under the name an upload would use. */
@SuppressWarnings("serial")
public class PublicationFileExistsException extends RuntimeException {

    private final String fileName;

    public PublicationFileExistsException(String fileName) {
        super("A file named '" + fileName + "' already exists for this publication.");
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
