package org.zfin.marker.presentation;

import org.zfin.gwt.root.dto.MarkerDTO;

import java.util.Collection;

public class MarkerRelationshipBean {

    private String relationship;
    private MarkerDTO first;
    private MarkerDTO second;
    private Collection<MarkerReferenceBean> references;

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public MarkerDTO getFirst() {
        return first;
    }

    public void setFirst(MarkerDTO first) {
        this.first = first;
    }

    public MarkerDTO getSecond() {
        return second;
    }

    public void setSecond(MarkerDTO second) {
        this.second = second;
    }

    public Collection<MarkerReferenceBean> getReferences() {
        return references;
    }

    public void setReferences(Collection<MarkerReferenceBean> references) {
        this.references = references;
    }
}
