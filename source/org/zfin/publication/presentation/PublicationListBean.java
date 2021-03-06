package org.zfin.publication.presentation;

import org.zfin.infrastructure.EntityZdbID;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationAuthorComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class PublicationListBean {

    protected EntityZdbID entity;
    private List<Publication> sortedPublications;
    private String orderBy;
    private String disassociatedPubId;
    private String entityID;
    public static final String ORDER_BY_AUTHOR = "author";


    abstract public Collection<Publication> getPublications();

    public List<Publication> getSortedPublications() {
        if (sortedPublications == null) {
            sortedPublications = new ArrayList<>();
            sortedPublications.addAll(getPublications());
        }
        sortPublications(sortedPublications, getOrderBy());
        return sortedPublications;
    }

    public void setSortedPublications(List<Publication> sortedPublications) {
        this.sortedPublications = sortedPublications;
    }

    public Publication getSinglePublication() {
        if (getNumOfPublications() == 1) {
            for (Publication pub : getSortedPublications()) {
                return pub;
            }
        }
        return null;
    }

    public int getNumOfPublications() {
        if (getSortedPublications() == null || getSortedPublications().isEmpty()) {
            return 0;
        }
        return getSortedPublications().size();
    }

    public int getNumOfPublishedPublications() {
        return getSortedPublishedPublications().size();
    }

    public int getNumOfUnpublishedPublications() {
        return getSortedUnpublishedPublications().size();
    }

    public String getOrderBy() {
        if (orderBy == null) {
            return ORDER_BY_AUTHOR;
        }
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void sortPublications(List<Publication> publications, String sortBy) {
        if (sortBy.equalsIgnoreCase(ORDER_BY_AUTHOR)) {
            Collections.sort(publications, new PublicationAuthorComparator());
        } else {
            Collections.sort(publications);
        }
    }

    public List<Publication> getSortedPublishedPublications() {
        List<Publication> sortedPublishedPublications = new ArrayList<Publication>();
        for (Publication pub : getSortedPublications()) {
            if (!pub.isUnpublished()) {
                sortedPublishedPublications.add(pub);
            }
        }

        sortPublications(sortedPublishedPublications, ORDER_BY_AUTHOR);
        return sortedPublishedPublications;
    }

    public List<Publication> getSortedUnpublishedPublications() {
        List<Publication> sortedUnpublishedPublications = new ArrayList<Publication>();
        for (Publication pub : getSortedPublications()) {
            if (pub.isUnpublished()) {
                sortedUnpublishedPublications.add(pub);
            }
        }

        sortPublications(sortedUnpublishedPublications, ORDER_BY_AUTHOR);
        return sortedUnpublishedPublications;
    }

    public String getDisassociatedPubId() {
        return disassociatedPubId;
    }

    public void setDisassociatedPubId(String disassociatedPubId) {
        this.disassociatedPubId = disassociatedPubId;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public EntityZdbID getEntity() {
        return entity;
    }

    public void setEntity(EntityZdbID entity) {
        this.entity = entity;
    }

    public List<Publication> getPublishedPublicationsByDate() {
        List<Publication>publications = getSortedPublishedPublications();
        Collections.sort(publications);
        return publications;
    }

    public List<Publication> getUnpublishedByDate() {
        List<Publication>publications = getSortedUnpublishedPublications();
        Collections.sort(publications);
        return publications;
    }
}
