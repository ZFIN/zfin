package org.zfin.publication.presentation;

public class PublicationFilePresentationBean {

    private String pubZdbId;
    private String type;
    private String fileName;

    public String getPubZdbId() {
        return pubZdbId;
    }

    public void setPubZdbId(String pubZdbId) {
        this.pubZdbId = pubZdbId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
