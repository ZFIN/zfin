package org.zfin.infrastructure;

import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@DiscriminatorValue("Pub    ")
public class PublicationAttribution extends RecordAttribution implements Serializable, Comparable<PublicationAttribution> {

    @ManyToOne
    @JoinColumn(name = "recattrib_source_zdb_id", insertable = false, updatable = false)
    protected Publication publication;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "recattrib_data_zdb_id", insertable = false, updatable = false)
    protected Marker marker;

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
        setSourceZdbID(publication.getZdbID());
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int compareTo(PublicationAttribution pubAttrib) {
        if (pubAttrib == null)
            return -1;
        if (publication == null)
            return +1;
        return publication.compareTo(pubAttrib.getPublication());
    }
}