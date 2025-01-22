package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.ChromosomalLocationBean;
import org.zfin.repository.RepositoryFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Marker Location .
 */
@Setter
@Getter
@Entity
@DiscriminatorValue("Mark")
public class MarkerLocation extends Location {

    @ManyToOne
    @JoinColumn(name = "sfcl_feature_zdb_id")
    protected Marker marker;

    /**
     *
     * Setters 
     * 
     **/

    public void setFieldsByChromosomalLocationBean(ChromosomalLocationBean chromosomalLocationBean) {
        this.setAssembly(chromosomalLocationBean.getAssembly());
        this.setChromosome(chromosomalLocationBean.getChromosome());

        try {
            int startLocation = chromosomalLocationBean.getStartLocation().intValue();
            this.setStartLocation(startLocation);
        } catch (NumberFormatException nfe) {
            //don't set start location
        }

        try {
            int endLocation = chromosomalLocationBean.getEndLocation().intValue();
            this.setEndLocation(endLocation);
        } catch (NumberFormatException nfe) {
            //don't set end location
        }

        this.setLocationEvidenceCode(chromosomalLocationBean);
        this.setLocationMarker(chromosomalLocationBean);
    }

    private void setLocationEvidenceCode(ChromosomalLocationBean chromosomalLocationBean) {
        String abbreviation = chromosomalLocationBean.getLocationEvidence();
        String evidenceCodeZdbID = DTOConversionService.abbreviationToEvidenceCodeId(abbreviation);
        if (evidenceCodeZdbID != null) {
            this.setLocationEvidence(RepositoryFactory.getOntologyRepository().getTermByZdbID(evidenceCodeZdbID));
        }
    }

    private void setLocationMarker(ChromosomalLocationBean chromosomalLocationBean) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(chromosomalLocationBean.getEntityID());
        this.setMarker(marker);
    }

    public void setLocationReferences(ChromosomalLocationBean chromosomalLocationBean) {

        Set<String> publicationIDs = chromosomalLocationBean.getReferences()
                .stream()
                .map(attributes -> attributes.get("zdbID"))
                .collect(Collectors.toSet());

        RepositoryFactory.getMarkerRepository().synchronizeGenomeLocationAttributions(this, publicationIDs);

    }    
}
