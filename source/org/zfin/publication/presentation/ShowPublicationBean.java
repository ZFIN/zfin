package org.zfin.publication.presentation;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShowPublicationBean extends PublicationListBean {

    private List<PublicationAttribution> publicationAttributionList;
    private List<Publication> publicationList;


    public ShowPublicationBean(List<PublicationAttribution> publicationAttributionList) {
        this.publicationAttributionList = publicationAttributionList;
    }

    @Override
    public Collection<Publication> getPublications() {
        if (publicationList == null) {
            publicationList = new ArrayList<>(publicationAttributionList.size());
            for (PublicationAttribution attribution : publicationAttributionList)
                publicationList.add(attribution.getPublication());
        }
        return publicationList;
    }
}
