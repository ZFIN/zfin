package org.zfin.publication.presentation;

import org.zfin.publication.Publication;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Aug 1, 2006
 * Time: 4:35:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationBean {

    private Publication publication;

    public Publication getPublication() {
        if (publication == null)
            publication = new Publication();
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }
}
