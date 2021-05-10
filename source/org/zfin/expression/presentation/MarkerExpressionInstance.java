package org.zfin.expression.presentation;

import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;

public class MarkerExpressionInstance {
    private Publication singlePublication;
    private Marker marker;
    private int figureCount;
    private int publicationCount;
    private int imageCount;
    private FigureLink singleFigure;

    public Publication getSinglePublication() {
        return singlePublication;
    }

    public void setSinglePublication(Publication singlePublication) {
        this.singlePublication = singlePublication;
    }

    public int getFigureCount() {
        return figureCount;
    }

    public void setFigureCount(int figureCount) {
        this.figureCount = figureCount;
    }


    public int getPublicationCount() {
        return publicationCount;
    }

    public void setPublicationCount(int publicationCount) {
        this.publicationCount = publicationCount;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public int getTotalCount() {
        return figureCount + publicationCount + imageCount;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public FigureLink getSingleFigure() {
        return singleFigure;
    }

    public void setSingleFigure(FigureLink singleFigure) {
        this.singleFigure = singleFigure;
    }
}
