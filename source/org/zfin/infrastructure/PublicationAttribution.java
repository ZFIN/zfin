package org.zfin.infrastructure;

import org.zfin.publication.Publication;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;


@Entity
@DiscriminatorValue("Pub    ")
public class PublicationAttribution extends RecordAttribution implements Serializable, Comparable<PublicationAttribution> {

    @ManyToOne
    @JoinColumn(name = "recattrib_source_zdb_id", insertable = false, updatable = false)
    protected Publication publication;

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
        setSourceZdbID(publication.getZdbID());
    }

    public int compareTo(PublicationAttribution pubAttrib) {
        if (pubAttrib == null)
            return -1;
        if (publication == null)
            return +1;
        return publication.compareTo(pubAttrib.getPublication());
    }
}