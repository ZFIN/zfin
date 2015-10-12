package org.zfin.gwt.root.dto;

import org.zfin.orthology.presentation.OrthologEvidenceDTO;

import java.util.Set;

public class OrthologDTO {

    private String zdbID;
    private MarkerDTO zebrafishGene;
    private NcbiOtherSpeciesGeneDTO ncbiOtherSpeciesGeneDTO;
    private Set<OrthologEvidenceDTO> evidenceSet;

    public MarkerDTO getZebrafishGene() {
        return zebrafishGene;
    }

    public void setZebrafishGene(MarkerDTO zebrafishGene) {
        this.zebrafishGene = zebrafishGene;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public NcbiOtherSpeciesGeneDTO getNcbiOtherSpeciesGeneDTO() {
        return ncbiOtherSpeciesGeneDTO;
    }

    public void setNcbiOtherSpeciesGeneDTO(NcbiOtherSpeciesGeneDTO ncbiGene) {
        this.ncbiOtherSpeciesGeneDTO = ncbiGene;
    }

    public Set<OrthologEvidenceDTO> getEvidenceSet() {
        return evidenceSet;
    }

    public void setEvidenceSet(Set<OrthologEvidenceDTO> evidenceSet) {
        this.evidenceSet = evidenceSet;
    }
}
