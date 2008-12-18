package org.zfin.expression;

/**
 * Image domain business object. This is an actual image in ZFIN being referenced
 * in a figure and a publication.
 */
public class Image {

    String zdbID;
    Figure figure;
    String imageFilename;
    String imageWithAnnotationsFilename;
    Integer width;
    Integer height;
    String thumbnail;


    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public String getImageWithAnnotationsFilename() {
        return imageWithAnnotationsFilename;
    }

    public void setImageWithAnnotationsFilename(String imageWithAnnotationsFilename) {
        this.imageWithAnnotationsFilename = imageWithAnnotationsFilename;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    
}
