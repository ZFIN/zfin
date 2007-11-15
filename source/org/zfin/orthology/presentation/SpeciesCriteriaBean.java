package org.zfin.orthology.presentation;

import java.io.Serializable;

/**
 * This bean holds the user-provided info for the search for a specific species.
 */
public class SpeciesCriteriaBean implements Serializable {

    private String name;
    private String geneSymbolFilterType;
    private String geneSearchTerm;
    private String chromosomeFilterType;
    private String chromosome;
    private String positionFilterType;
    private String position;
    // Todo: Need a better way to convey OR or AND
    // This boolean is only use for zebrafish and indicates
    // true only zebrafish contraint are checked. Return all other species
    // false all provided species criteria are used in an AND relationship.
    private boolean orRelationhip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGeneSymbolFilterType() {
        return geneSymbolFilterType;
    }

    public void setGeneSymbolFilterType(String geneSymbolFilterType) {
        this.geneSymbolFilterType = geneSymbolFilterType;
    }

    public String getGeneSearchTerm() {
        return geneSearchTerm;
    }

    public void setGeneSearchTerm(String geneSearchTerm) {
        this.geneSearchTerm = geneSearchTerm;
    }

    public String getChromosomeFilterType() {
        return chromosomeFilterType;
    }

    public void setChromosomeFilterType(String chromosomeFilterType) {
        this.chromosomeFilterType = chromosomeFilterType;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getPositionFilterType() {
        return positionFilterType;
    }

    public void setPositionFilterType(String positionFilterType) {
        this.positionFilterType = positionFilterType;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isOrRelationhip() {
        return orRelationhip;
    }

    public void setOrRelationhip(boolean orRelationhip) {
        this.orRelationhip = orRelationhip;
    }

    public String toString() {
        return "SpeciesCriteriaBean{" +
                "name='" + name + '\'' +
                ", geneSymbolFilterType='" + geneSymbolFilterType + '\'' +
                ", geneSearchTerm='" + geneSearchTerm + '\'' +
                ", chromosomeFilterType='" + chromosomeFilterType + '\'' +
                ", chromosome='" + chromosome + '\'' +
                ", positionFilterType='" + positionFilterType + '\'' +
                ", position='" + position + '\'' +
                ", orRelationhip=" + orRelationhip +
                '}';
    }
}