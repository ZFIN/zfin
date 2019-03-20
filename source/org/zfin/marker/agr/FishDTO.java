package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class FishDTO extends ZfinDTO {

    private String genotypeID;
    private String name;
    private String nameText;
    private List<GenotypeComponentDTO> genotypeComponents;
    private List<String> sequenceTargetingReagents;
    private List<String> backgroundIDs;
    private CrossReferenceDTO crossReference;

    public String getGenotypeID() {
        return genotypeID;
    }

    public void setGenotypeID(String genotypeID) {
        this.genotypeID = genotypeID;
    }

    public List<String> getBackgroundIDs() {
        return backgroundIDs;
    }

    public void setBackgrounds(List<String> backgrounds) {
        this.backgroundIDs = backgroundIDs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameText() {
        return nameText;
    }

    public void setNameText(String nameText) {
        this.nameText = nameText;
    }

    public List<GenotypeComponentDTO> getGenotypeComponents() {
        return genotypeComponents;
    }

    public void setGenotypeComponents(List<GenotypeComponentDTO> genotypeComponents) {
        this.genotypeComponents = genotypeComponents;
    }

    public List<String> getSequenceTargetingReagents() {
        return sequenceTargetingReagents;
    }

    public void setSequenceTargetingReagents(List<String> sequenceTargetingReagents) {
        this.sequenceTargetingReagents = sequenceTargetingReagents;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }

}
