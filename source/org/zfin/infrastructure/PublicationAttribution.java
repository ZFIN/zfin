package org.zfin.infrastructure;

import org.zfin.publication.Publication;

import java.io.Serializable;


/**
 */
public class PublicationAttribution extends RecordAttribution implements Serializable, Comparable<PublicationAttribution> {

    private Publication publication;

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