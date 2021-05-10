package org.zfin.publication.presentation;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.List;

public class ShowPublicationBean extends PublicationListBean {

    private List<Publication> publicationList;

    public ShowPublicationBean(List<Publication> publicationList) {
        this.publicationList = publicationList;
    }

    @Override
    public Collection<Publication> getPublications() {
        return publicationList;
    }
}
