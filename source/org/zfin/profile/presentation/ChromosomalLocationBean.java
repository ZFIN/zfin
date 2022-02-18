package org.zfin.profile.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.mapping.GenomeLocation;
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

//    public static ChromosomalLocationBean fromGenomeLocation(GenomeLocation persistedLocation) {
//        ChromosomalLocationBean clBean = new ChromosomalLocationBean();
//        clBean.setID(persistedLocation.getID());
//        clBean.setEntityID(persistedLocation.getEntityID());
//        clBean.setAssembly(persistedLocation.getAssembly());
//        clBean.setChromosome(persistedLocation.getChromosome());
//        clBean.setStartLocation(persistedLocation.getStart());
//        clBean.setEndLocation(persistedLocation.getEnd());
//        return clBean;
//    }

    public static ChromosomalLocationBean fromMarkerLocation(MarkerLocation persistedLocation) {
        ChromosomalLocationBean clBean = new ChromosomalLocationBean();
        clBean.setZdbID(persistedLocation.getZdbID());
        clBean.setEntityID(persistedLocation.getMarker().getZdbID());
        clBean.setAssembly(persistedLocation.getFtrAssembly());
        clBean.setChromosome(persistedLocation.getFtrChromosome());
        clBean.setStartLocation(persistedLocation.getFtrStartLocation());
        clBean.setEndLocation(persistedLocation.getFtrEndLocation());
        return clBean;
    }

    public GenomeLocation toGenomeLocation() {
        GenomeLocation genomeLocation = new GenomeLocation();
        genomeLocation.setAssembly(this.getAssembly());
        genomeLocation.setChromosome(this.getChromosome());

        try {
            int startLocation = this.getStartLocation();
            genomeLocation.setStart(startLocation);
        } catch (NumberFormatException nfe) {
            //don't set start location
        }

        try {
            int endLocation = this.getEndLocation();
            genomeLocation.setEnd(endLocation);
        } catch (NumberFormatException nfe) {
            //don't set end location
        }
        return genomeLocation;
    }
}
