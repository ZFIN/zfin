package org.zfin.marker.presentation;

import org.zfin.mutant.presentation.PostComposedPresentationBean;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.presentation.PublicationLink;

import java.util.List;

/**
 */
public class PhenotypeOnMarkerBean {

    private int numFigures;
    private int numPublications;
    private PublicationLink singlePublicationLink;
    private List<PostComposedPresentationBean> anatomy;
    private FigureLink singleFigureLink;

    public int getNumFigures() {
        return numFigures;
    }

    public void setNumFigures(int numFigures) {
        this.numFigures = numFigures;
    }

    public int getNumPublications() {
        return numPublications;
    }

    public void setNumPublications(int numPublications) {
        this.numPublications = numPublications;
    }

    public List<PostComposedPresentationBean> getAnatomy() {
        return anatomy;
    }

    public void setAnatomy(List<PostComposedPresentationBean> anatomy) {
        this.anatomy = anatomy;
    }

    public PublicationLink getSinglePublicationLink() {
        return singlePublicationLink;
    }

    public void setSinglePublicationLink(PublicationLink singlePublicationLink) {
        this.singlePublicationLink = singlePublicationLink;
    }

    public FigureLink getSingleFigureLink() {
        return singleFigureLink;
    }

    public void setSingleFigureLink(FigureLink singleFigureLink) {
        this.singleFigureLink = singleFigureLink;
    }
}
