package org.zfin.expression.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageResult {

    private String imageZdbId;
    private String imageThumbnail;

    public String getImageZdbId() {
        return imageZdbId;
    }

    public void setImageZdbId(String imageZdbId) {
        this.imageZdbId = imageZdbId;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    @JsonProperty("zdbID")
    public String getZdbID() {
        return imageZdbId;
    }

    @JsonProperty("mediumUrl")
    public String getMediumUrl() {
        return "/imageLoadUp/" + imageThumbnail;
    }
}
