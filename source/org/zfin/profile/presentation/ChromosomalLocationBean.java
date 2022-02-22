package org.zfin.profile.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.mapping.MarkerLocation;

@Setter
@Getter
public class ChromosomalLocationBean {

    @JsonView(View.API.class)
    @JsonProperty("id")
    String ZdbID;

    @JsonView(View.API.class)
    String entityID;

    @JsonView(View.API.class)
    String assembly;

    @JsonView(View.API.class)
    String chromosome;

    @JsonView(View.API.class)
    Integer startLocation;

    @JsonView(View.API.class)
    Integer endLocation;

    public static ChromosomalLocationBean fromMarkerLocation(MarkerLocation persistedLocation) {
        ChromosomalLocationBean clBean = new ChromosomalLocationBean();
        clBean.setZdbID(persistedLocation.getZdbID());
        clBean.setEntityID(persistedLocation.getMarker().getZdbID());
        clBean.setAssembly(persistedLocation.getAssembly());
        clBean.setChromosome(persistedLocation.getChromosome());
        clBean.setStartLocation(persistedLocation.getStartLocation());
        clBean.setEndLocation(persistedLocation.getEndLocation());
        return clBean;
    }

    public MarkerLocation toMarkerLocation() {
        MarkerLocation markerLocation = new MarkerLocation();
        updateMarkerLocation(markerLocation);
        return markerLocation;
    }

    public void updateMarkerLocation(MarkerLocation markerLocation) {
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
    }
}
