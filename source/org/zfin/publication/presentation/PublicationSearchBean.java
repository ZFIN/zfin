package org.zfin.publication.presentation;

import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.List;

/**
 * Main bean that serves publication-related information.
 */
public class PublicationSearchBean extends PaginationBean {

    private Marker marker;
    private GenericTerm term;
    private List<Publication> publications;

    public GenericTerm getTerm() {
        if (term == null)
            term = new GenericTerm();
        return term;
    }

    public void setTerm(GenericTerm anatomyItem) {
        this.term = anatomyItem;
    }

    public Marker getMarker() {
        if (marker == null)
            marker = new Marker();
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
}
