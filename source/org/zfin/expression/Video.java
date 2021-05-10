package org.zfin.expression;


public class Video {
    Long id;
    String videoFilename;
    Image still;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoFilename() {
        return videoFilename;
    }
    public void setVideoFilename(String videoFilename) {
        this.videoFilename = videoFilename;
    }

    public Image getStill() {
        return still;
    }
    public void setStill(Image still) {
        this.still = still;
    }
}
