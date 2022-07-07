package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.FlexibleIntegerDeserializer;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.MarkerLocation;

import java.util.*;

@Setter
@Getter
public class ChromosomalLocationBean {

    @JsonView(View.API.class)
    String zdbID;

    @JsonView(View.API.class)
    String entityID;

    @JsonView(View.API.class)
    String assembly;

    @JsonView(View.API.class)
    String chromosome;

    @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
    @JsonView(View.API.class)
    Long startLocation;

    @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
    @JsonView(View.API.class)
    Long endLocation;

    @JsonView(View.API.class)
    String locationEvidence;

    @JsonView(View.API.class)
    Set<Map<String, String>> references;

    public static ChromosomalLocationBean fromMarkerLocation(MarkerLocation persistedLocation) {
        ChromosomalLocationBean clBean = new ChromosomalLocationBean();
        clBean.setZdbID(persistedLocation.getZdbID());
        clBean.setEntityID(persistedLocation.getMarker().getZdbID());
        clBean.setAssembly(persistedLocation.getAssembly());
        clBean.setChromosome(persistedLocation.getChromosome());
        clBean.setStartLocation( persistedLocation.getStartLocation().longValue() );
        clBean.setEndLocation(persistedLocation.getEndLocation().longValue() );
        clBean.setLocationEvidenceByMarkerLocation(persistedLocation);
        clBean.setReferencesByMarkerLocation(persistedLocation);
        return clBean;
    }

    private void setReferencesByMarkerLocation(MarkerLocation location) {
        this.references = new HashSet<>();
        if (location == null || location.getReferences() == null) {
            return;
        }
        for (RecordAttribution reference : location.getReferences()) {
            Map<String, String> jsonRepresentation = new HashMap<>();
            jsonRepresentation.put("zdbID", reference.getSourceZdbID());
            this.references.add(jsonRepresentation);
        }
    }

    private void setLocationEvidenceByMarkerLocation(MarkerLocation persistedLocation) {
        String abbreviation = DTOConversionService.evidenceCodeIdToAbbreviation(
                persistedLocation.getLocationEvidence().getZdbID());
        this.setLocationEvidence(abbreviation);
    }

    public MarkerLocation toMarkerLocation() {
        MarkerLocation markerLocation = new MarkerLocation();
        markerLocation.setFieldsByChromosomalLocationBean(this);
        return markerLocation;
    }


}
