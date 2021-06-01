package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicGeneticEntity extends ZfinDTO {


    private Set<CrossReferenceDTO> crossReferences;
    private Set<GenomeLocationDTO> genomeLocations;

    public Set<CrossReferenceDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(Set<CrossReferenceDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public Set<GenomeLocationDTO> getGenomeLocations() {
        return genomeLocations;
    }

    public void setGenomeLocations(Set<GenomeLocationDTO> genomeLocations) {
        this.genomeLocations = genomeLocations;
    }

}
