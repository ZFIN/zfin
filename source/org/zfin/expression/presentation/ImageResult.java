package org.zfin.expression.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResult {

    private String imageZdbId;
    private String imageThumbnail;

    @JsonProperty("zdbID")
    public String getZdbID() {
        return imageZdbId;
    }

    @JsonProperty("mediumUrl")
    public String getMediumUrl() {
        return "/imageLoadUp/" + imageThumbnail;
    }
}
