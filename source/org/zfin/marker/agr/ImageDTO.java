package org.zfin.marker.agr;

public class ImageDTO {
    private String imageZdbId;
    private String imageFileUrl;
    private String imagePageUrl;

    public String getImageZdbId() {
        return imageZdbId;
    }

    public void setImageZdbId(String imageZdbId) {
        this.imageZdbId = imageZdbId;
    }

    public String getImageFileUrl() {
        return imageFileUrl;
    }

    public void setImageFileUrl(String imageFileUrl) {
        this.imageFileUrl = imageFileUrl;
    }

    public String getImagePageUrl() {
        return imagePageUrl;
    }

    public void setImagePageUrl(String imagePageUrl) {
        this.imagePageUrl = imagePageUrl;
    }
}
