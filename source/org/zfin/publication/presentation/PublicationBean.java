package org.zfin.publication.presentation;

import org.zfin.publication.Publication;

/**
 * Serving Def Pub section
 */
public class PublicationBean {

    private String zdbID;
    private Publication publication;
    private boolean validPublication;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Publication getPublication() {
        if (publication == null)
            publication = new Publication();
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public boolean isValidPublication() {
        return validPublication;
    }

    public void setValidPublication(boolean validPublication) {
        this.validPublication = validPublication;
    }
}
