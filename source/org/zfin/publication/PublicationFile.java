package org.zfin.publication;

public class PublicationFile {

    private Publication publication;
    private String fileName;
    private PublicationFileType type;

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public PublicationFileType getType() {
        return type;
    }

    public void setType(PublicationFileType type) {
        this.type = type;
    }
}
