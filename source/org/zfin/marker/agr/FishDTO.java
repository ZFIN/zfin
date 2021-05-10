package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class FishDTO extends ZfinDTO {

    private String primaryID;
    private String name;
    private List<AffectedGenomicModelComponentDTO> affectedGenomicModelComponents;
    private List<String> sequenceTargetingReagentIDs;
    private List<String> parentalPopulationIDs;
    private CrossReferenceDTO crossReference;

    public String getPrimaryID() {
        return primaryID;
    }

    public void setPrimaryID(String genotypeID) {
        this.primaryID = genotypeID;
    }

    public List<String> getParentalPopulationIDs() {
        return parentalPopulationIDs;
    }

    public void setParentalPopulationIDs(List<String> backgroundIDs) {
        this.parentalPopulationIDs = backgroundIDs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<AffectedGenomicModelComponentDTO> getAffectedGenomicModelComponents() {
        return affectedGenomicModelComponents;
    }

    public void setAffectedGenomicModelComponents(List<AffectedGenomicModelComponentDTO> genotypeComponents) {
        this.affectedGenomicModelComponents = genotypeComponents;
    }

    public List<String> getSequenceTargetingReagentIDs() {
        return sequenceTargetingReagentIDs;
    }

    public void setSequenceTargetingReagentIDs(List<String> sequenceTargetingReagents) {
        this.sequenceTargetingReagentIDs = sequenceTargetingReagents;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }

}
