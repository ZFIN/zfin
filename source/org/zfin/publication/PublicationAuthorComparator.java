package org.zfin.publication;

import java.util.Comparator;

public class PublicationAuthorComparator implements Comparator<Publication> {

    public int compare(Publication pub1, Publication pub2) {
        if(pub1 == null && pub2 == null)
            return 0;
        if(pub1 == null)
            return -1;
        if(pub2 == null)
            return 1;

        return pub1.getAuthors().compareToIgnoreCase(pub2.getAuthors());
    }
}
