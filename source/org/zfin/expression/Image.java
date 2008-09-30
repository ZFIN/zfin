package org.zfin.expression;

/**
 * Image domain business object. This is an actual image in ZFIN being referenced
 * in a figure and a publication.
 */
public class Image {

    String zdbID;
    Figure figure;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    
}
