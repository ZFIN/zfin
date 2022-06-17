package org.zfin.publication.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.publication.Publication;

/**
 * Serving Def Pub section
 */
@Getter
@Setter
public class PublicationBean {

    private String zdbID;
    private Publication publication;
    private boolean validPublication;
    private String accessionNumber;

    public Publication getPublication() {
        if (publication == null)
            publication = new Publication();
        return publication;
    }

}
