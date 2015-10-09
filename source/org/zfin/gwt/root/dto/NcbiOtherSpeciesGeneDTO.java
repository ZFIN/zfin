package org.zfin.gwt.root.dto;

import java.util.List;

/**
 * External ortholog, i.e. the non-zebrafish ortholog found at NCBI
 */
public class NcbiOtherSpeciesGeneDTO {

    private String ID;
    private String name;
    private String abbreviation;
    private String chromosome;
    private String position;
    private String organism;
    private List<NcbiExternalReferenceDTO> referenceDTOList;

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
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

    public List<NcbiExternalReferenceDTO> getReferenceDTOList() {
        return referenceDTOList;
    }

    public void setReferenceDTOList(List<NcbiExternalReferenceDTO> referenceDTOList) {
        this.referenceDTOList = referenceDTOList;
    }
}
