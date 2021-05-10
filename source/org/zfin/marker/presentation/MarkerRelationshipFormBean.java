package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.stream.Collectors;

@Setter
@Getter
public class MarkerRelationshipFormBean {

    @JsonView(View.MarkerRelationshipAPI.class)
    private String zdbID;
    @JsonView(View.MarkerRelationshipAPI.class)
    private MarkerRelationshipType markerRelationshipType;
    @JsonView(View.MarkerRelationshipAPI.class)
    private Marker firstMarker;
    @JsonView(View.MarkerRelationshipAPI.class)
    private Marker secondMarker;
    @JsonView(View.MarkerRelationshipAPI.class)
    private Collection<Publication> references;

    public static MarkerRelationshipFormBean convert(MarkerRelationship relationship) {
        MarkerRelationshipFormBean bean = new MarkerRelationshipFormBean();
        bean.setZdbID(relationship.getZdbID());
        bean.setMarkerRelationshipType(relationship.getMarkerRelationshipType());
        bean.setFirstMarker(relationship.getFirstMarker());
        bean.setSecondMarker(relationship.getSecondMarker());
        bean.setReferences(relationship.getPublications().stream()
                .map(PublicationAttribution::getPublication)
                .collect(Collectors.toList()));
        return bean;
    }
}
