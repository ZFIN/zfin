package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.HashSet;
import java.util.Set;

/**
 * One variant of a Complex feature on the feature curation form (ZFIN-10623): its type, its
 * chromosome location and its genomic, DNA, transcript and protein mutation details. A feature of
 * any other type keeps these on FeatureDTO itself.
 */
public class VariantDTO implements IsSerializable {

    private String zdbID;
    private int variantNumber;
    private FeatureTypeEnum variantType;

    private String chromosome;
    private String assembly;
    private Integer startLocation;
    private Integer endLocation;
    private String evidence;

    private FeatureGenomeMutationDetailChangeDTO fgmdChangeDTO;
    private MutationDetailDnaChangeDTO dnaChangeDTO;
    private MutationDetailProteinChangeDTO proteinChangeDTO;
    private Set<MutationDetailTranscriptChangeDTO> transcriptChangeDTOSet = new HashSet<>();

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public int getVariantNumber() {
        return variantNumber;
    }

    public void setVariantNumber(int variantNumber) {
        this.variantNumber = variantNumber;
    }

    public FeatureTypeEnum getVariantType() {
        return variantType;
    }

    public void setVariantType(FeatureTypeEnum variantType) {
        this.variantType = variantType;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public Integer getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Integer startLocation) {
        this.startLocation = startLocation;
    }

    public Integer getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Integer endLocation) {
        this.endLocation = endLocation;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public boolean hasLocation() {
        return chromosome != null && !chromosome.trim().isEmpty();
    }

    public FeatureGenomeMutationDetailChangeDTO getFgmdChangeDTO() {
        return fgmdChangeDTO;
    }

    public void setFgmdChangeDTO(FeatureGenomeMutationDetailChangeDTO fgmdChangeDTO) {
        this.fgmdChangeDTO = fgmdChangeDTO;
    }

    public MutationDetailDnaChangeDTO getDnaChangeDTO() {
        return dnaChangeDTO;
    }

    public void setDnaChangeDTO(MutationDetailDnaChangeDTO dnaChangeDTO) {
        this.dnaChangeDTO = dnaChangeDTO;
    }

    public MutationDetailProteinChangeDTO getProteinChangeDTO() {
        return proteinChangeDTO;
    }

    public void setProteinChangeDTO(MutationDetailProteinChangeDTO proteinChangeDTO) {
        this.proteinChangeDTO = proteinChangeDTO;
    }

    public Set<MutationDetailTranscriptChangeDTO> getTranscriptChangeDTOSet() {
        return transcriptChangeDTOSet;
    }

    public void setTranscriptChangeDTOSet(Set<MutationDetailTranscriptChangeDTO> transcriptChangeDTOSet) {
        this.transcriptChangeDTOSet = transcriptChangeDTOSet == null ? new HashSet<>() : transcriptChangeDTOSet;
    }

    /** True when nothing at all has been entered: no type, location or mutation detail. */
    public boolean isEmpty() {
        return variantType == null
                && !hasLocation()
                && (assembly == null || assembly.trim().isEmpty())
                && (evidence == null || evidence.trim().isEmpty())
                && startLocation == null
                && endLocation == null
                && fgmdChangeDTO == null
                && dnaChangeDTO == null
                && proteinChangeDTO == null
                && (transcriptChangeDTOSet == null || transcriptChangeDTOSet.isEmpty());
    }
}
