package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.FlexibleIntegerDeserializer;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mapping.MarkerLocation;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

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
    Integer startLocation;

    @JsonDeserialize(using = FlexibleIntegerDeserializer.class)
    @JsonView(View.API.class)
    Integer endLocation;

    @JsonView(View.API.class)
    String locationEvidence;

    public static ChromosomalLocationBean fromMarkerLocation(MarkerLocation persistedLocation) {
        ChromosomalLocationBean clBean = new ChromosomalLocationBean();
        clBean.setZdbID(persistedLocation.getZdbID());
        clBean.setEntityID(persistedLocation.getMarker().getZdbID());
        clBean.setAssembly(persistedLocation.getAssembly());
        clBean.setChromosome(persistedLocation.getChromosome());
        clBean.setStartLocation(persistedLocation.getStartLocation());
        clBean.setEndLocation(persistedLocation.getEndLocation());
        clBean.setLocationEvidenceByMarkerLocation(persistedLocation);
        return clBean;
    }

    private void setLocationEvidenceByMarkerLocation(MarkerLocation persistedLocation) {
        String abbreviation = DTOConversionService.evidenceCodeIdToAbbreviation(
                persistedLocation.getLocationEvidence().getZdbID());
        this.setLocationEvidence(abbreviation);
    }

    public MarkerLocation toMarkerLocation() {
        MarkerLocation markerLocation = new MarkerLocation();
        setMarkerLocation(markerLocation);
        return markerLocation;
    }

    public void setMarkerLocation(MarkerLocation markerLocation) {
        markerLocation.setAssembly(this.getAssembly());
        markerLocation.setChromosome(this.getChromosome());

        try {
            int startLocation = this.getStartLocation();
            markerLocation.setStartLocation(startLocation);
        } catch (NumberFormatException nfe) {
            //don't set start location
        }

        try {
            int endLocation = this.getEndLocation();
            markerLocation.setEndLocation(endLocation);
        } catch (NumberFormatException nfe) {
            //don't set end location
        }

        this.setMarkerLocationEvidenceCode(markerLocation);
        this.setMarkerLocationMarker(markerLocation);
    }

    private void setMarkerLocationMarker(MarkerLocation markerLocation) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(this.getEntityID());
        markerLocation.setMarker(marker);
    }

    private void setMarkerLocationEvidenceCode(MarkerLocation markerLocation) {
        String abbreviation = this.getLocationEvidence();
        String evidenceCodeZdbID = DTOConversionService.abbreviationToEvidenceCodeId(abbreviation);
        if (evidenceCodeZdbID != null) {
            markerLocation.setLocationEvidence(RepositoryFactory.getOntologyRepository().getTermByZdbID(evidenceCodeZdbID));
        }
    }

}
