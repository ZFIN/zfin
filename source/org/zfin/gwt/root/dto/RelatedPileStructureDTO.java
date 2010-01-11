package org.zfin.gwt.root.dto;

/**
 * Data Transfer Object individual structures on the pile related to a given structure.
 */
public class RelatedPileStructureDTO extends PileStructureDTO {

    private PileStructureDTO relatedStructure;
    private String relationship;

    public PileStructureDTO getRelatedStructure() {
        return relatedStructure;
    }

    public void setRelatedStructure(PileStructureDTO relatedStructure) {
        this.relatedStructure = relatedStructure;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}