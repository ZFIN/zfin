package org.zfin.marker.presentation;

import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.MarkerRelationship;

import java.util.ArrayList;
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

    public static MarkerRelationshipBean convert(MarkerRelationship relationship) {
        MarkerRelationshipBean bean = new MarkerRelationshipBean();
        bean.setRelationship(relationship.getType().toString());
        bean.setFirst(DTOConversionService.convertToMarkerDTO(relationship.getFirstMarker()));
        bean.setSecond(DTOConversionService.convertToMarkerDTO(relationship.getSecondMarker()));
        Collection<MarkerReferenceBean> references = new ArrayList<>();
        for (PublicationAttribution reference : relationship.getPublications()) {
            MarkerReferenceBean referenceBean = new MarkerReferenceBean();
            referenceBean.setZdbID(reference.getSourceZdbID());
            referenceBean.setTitle(reference.getPublication().getTitle());
            references.add(referenceBean);
        }
        bean.setReferences(references);
        return bean;
    }
}
