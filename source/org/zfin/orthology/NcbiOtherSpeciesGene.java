package org.zfin.orthology;

import java.util.Set;

/**
 * External ortholog, i.e. the non-zebrafish ortholog found at NCBI
 */
public class NcbiOtherSpeciesGene {

    private String ID;
    private String name;
    private String abbreviation;
    private String chromosome;
    private String position;
    private org.zfin.Species organism;

    private Set<NcbiExternalReference> ncbiExternalReferenceList;

    public org.zfin.Species getOrganism() {
        return organism;
    }

    public void setOrganism(org.zfin.Species organism) {
        this.organism = organism;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Set<NcbiExternalReference> getNcbiExternalReferenceList() {
        return ncbiExternalReferenceList;
    }

    public void setNcbiExternalReferenceList(Set<NcbiExternalReference> referenceList) {
        this.ncbiExternalReferenceList = referenceList;
    }
}
