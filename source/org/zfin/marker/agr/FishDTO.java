package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class FishDTO extends ZfinDTO {

    private String genotypeID;
    private String name;
    private String nameText;
    private List<GenotypeFeature> genotypeComponents;
    private List<String> sequenceTargetingReagents;
    private Set<Genotype> backgrounds;
    private CrossReferenceDTO crossReference;

    public String getGenotypeID() {
        return genotypeID;
    }

    public void setGenotypeID(String genotypeID) {
        this.genotypeID = genotypeID;
    }


    public Set<Genotype> getBackgrounds() {
        return backgrounds;
    }

    public void setBackgrounds(Set<Genotype> backgrounds) {
        this.backgrounds = backgrounds;
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

    public List<GenotypeFeature> getGenotypeComponents() {
        return genotypeComponents;
    }

    public void setGenotypeComponents(List<GenotypeFeature> genotypeComponents) {
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
