package org.zfin.figure.presentation;

import java.util.Collection;

public class PublicationFigureSet {

    private boolean pubCanShowImages;
    private Collection<FigurePresentationBean> figures;

    public boolean isPubCanShowImages() {
        return pubCanShowImages;
    }

    public void setPubCanShowImages(boolean pubCanShowImages) {
        this.pubCanShowImages = pubCanShowImages;
    }

    public Collection<FigurePresentationBean> getFigures() {
        return figures;
    }

    public void setFigures(Collection<FigurePresentationBean> figures) {
        this.figures = figures;
    }
}
