package org.zfin.publication.presentation;

public class PublicationFilePresentationBean {

    private long id;
    private String pubZdbId;
    private String type;
    private String fileName;
    private String originalFileName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
}
