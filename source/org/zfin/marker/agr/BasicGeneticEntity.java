package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicGeneticEntity extends ZfinDTO {


    private List<CrossReferenceDTO> crossReferences;
    private Set<GenomeLocationDTO> genomeLocations;

    public List<CrossReferenceDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public Set<GenomeLocationDTO> getGenomeLocations() {
        return genomeLocations;
    }

    public void setGenomeLocations(Set<GenomeLocationDTO> genomeLocations) {
        this.genomeLocations = genomeLocations;
    }

}
