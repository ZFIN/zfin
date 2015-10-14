package org.zfin.publication.presentation;

import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PublicationListAdapter extends PublicationListBean {

    private final Set<Publication> publications;

    public PublicationListAdapter(Collection<Publication> publications) {
        this.publications = new HashSet<>(publications);
    }

    @Override
    public Set<Publication> getPublications() {
        return publications;
    }
}
