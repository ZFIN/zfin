package org.zfin.orthology;

import org.zfin.Species;

import java.util.Set;

/**
 * External ortholog, i.e. the non-zebrafish ortholog found at NCBI
 */
public class NcbiOtherSpeciesGene {

    private String ID;
    private String name;
    private String abbreviation;
    private String chromosome;
    private Species organism;

    private Set<NcbiOrthoExternalReference> ncbiExternalReferenceList;

    public Species getOrganism() {
        return organism;
    }

    public void setOrganism(org.zfin.Species organism) {
        this.organism = organism;
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

    public Set<NcbiOrthoExternalReference> getNcbiExternalReferenceList() {
        return ncbiExternalReferenceList;
    }

    public void setNcbiExternalReferenceList(Set<NcbiOrthoExternalReference> referenceList) {
        this.ncbiExternalReferenceList = referenceList;
    }
}
