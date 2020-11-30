package org.zfin.orthology.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.NcbiOtherSpeciesGeneDTO;

import java.util.Set;

public class OrthologDTO {

    @JsonView(View.API.class)
    private String zdbID;
    private MarkerDTO zebrafishGene;
    @JsonView(View.OrthologyAPI.class)
    @JsonProperty("orthologousGene")
    private NcbiOtherSpeciesGeneDTO ncbiOtherSpeciesGeneDTO;
    @JsonView(View.OrthologyAPI.class)
    private Set<OrthologEvidenceDTO> evidenceSet;
    @JsonView(View.OrthologyAPI.class)
    @JsonProperty("evidence")
    private Set<OrthologEvidenceGroupedByCode> evidenceSetGroupedByCode;
    @JsonView(View.OrthologyAPI.class)
    @JsonProperty("orthologousGeneReference")
    private Set<OrthologExternalReferenceDTO> orthologExternalReferenceDTOSet;
    private String name;
    @JsonView(View.API.class)
    private String symbol;
    @JsonView(View.API.class)
    private String chromosome;
    private String position;

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

    public Set<OrthologExternalReferenceDTO> getOrthologExternalReferenceDTOSet() {
        return orthologExternalReferenceDTOSet;
    }

    public void setOrthologExternalReferenceDTOSet(Set<OrthologExternalReferenceDTO> orthologExternalReferenceDTOSet) {
        this.orthologExternalReferenceDTOSet = orthologExternalReferenceDTOSet;
    }

    public Set<OrthologEvidenceDTO> getEvidenceSet() {
        return evidenceSet;
    }

    public void setEvidenceSet(Set<OrthologEvidenceDTO> evidenceSet) {
        this.evidenceSet = evidenceSet;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Set<OrthologEvidenceGroupedByCode> getEvidenceSetGroupedByCode() {
        return evidenceSetGroupedByCode;
    }

    public void setEvidenceSetGroupedByCode(Set<OrthologEvidenceGroupedByCode> evidenceSetGroupedByCode) {
        this.evidenceSetGroupedByCode = evidenceSetGroupedByCode;
    }
}
