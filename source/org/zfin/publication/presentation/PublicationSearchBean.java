package org.zfin.publication.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.List;

/**
 * Main bean that serves publication-related information.
 */
public class PublicationSearchBean extends PaginationBean {

    private Marker marker;
    private AnatomyItem anatomyItem;
    private List<Publication> publications;

    public AnatomyItem getAnatomyItem() {
        if (anatomyItem == null)
            anatomyItem = new AnatomyItem();
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
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
