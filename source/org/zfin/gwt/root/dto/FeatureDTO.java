package org.zfin.gwt.root.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.marker.agr.EvidenceDTO;
import org.zfin.util.JsonDateSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Transcript RPC object.
 */

public class FeatureDTO extends RelatedEntityDTO implements HasExternalNotes, FilterSelectionBoxEntry {


    private String optionalName; // this is the name entered from the GUI, not stored in the database
    private String abbreviation;  // this is the name (the full name) without the optionalName except for some cases
    private Boolean dominant = false;
    private String mutagen;
    private String mutagee;
    private String featureAssay;
    private Boolean knownInsertionSite = false;
    private NoteDTO publicNote;
    private List<NoteDTO> publicNoteList;
    private List<CuratorNoteDTO> curatorNotes;
    protected String alias;
    protected FeatureTypeEnum featureType;
    protected String lineNumber;
    protected String labPrefix;

    protected String evidence;
    protected String assembly;
    protected String fgmdSeqVar;
    protected String fgmdSeqRef;
    public String getFeatureAssembly() {
        return featureAssembly;
    }

    public void setFeatureAssembly(String featureAssembly) {
        this.featureAssembly = featureAssembly;
    }



    protected List<String> featureAliases;

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    protected String featureSequence;
    protected String featureChromosome;
    protected String featureAssembly;
    protected Integer featureStartLoc;

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    protected Integer featureEndLoc;

    public FeatureGenomeMutationDetailChangeDTO getFgmdChangeDTO() {
        return fgmdChangeDTO;
    }

    public void setFgmdChangeDTO(FeatureGenomeMutationDetailChangeDTO fgmdChangeDTO) {
        this.fgmdChangeDTO = fgmdChangeDTO;
    }

    protected List<String> featureSequences;
    protected String displayNameForGenotypeBase;
    protected String displayNameForGenotypeSuperior;
    protected MutationDetailDnaChangeDTO dnaChangeDTO;
    protected MutationDetailProteinChangeDTO proteinChangeDTO;
    protected FeatureGenomeMutationDetailChangeDTO fgmdChangeDTO;
    protected Set<MutationDetailTranscriptChangeDTO> transcriptChangeDTOSet;

    public String getFeatureSequence() {
        return featureSequence;
    }

    public void setFeatureSequence(String featureSequence) {
        this.featureSequence = featureSequence;
    }

    public List<String> getFeatureSequences() {
        return featureSequences;
    }

    public void setFeatureSequences(List<String> featureSequences) {
        this.featureSequences = featureSequences;
    }

    protected String labOfOrigin;
    protected String transgenicSuffix;

    public String getFeatureChromosome() {
        return featureChromosome;
    }

    public void setFeatureChromosome(String featureChromosome) {
        this.featureChromosome = featureChromosome;
    }

    protected int labPrefixID;

    public FeatureDTO() {
    }

    public FeatureDTO(RelatedEntityDTO relatedEntityDTO) {
        this.dataZdbID = relatedEntityDTO.getDataZdbID();
        this.name = relatedEntityDTO.getName();
        this.link = relatedEntityDTO.getLink();
        if (relatedEntityDTO.getPublicationZdbID() != null && true == relatedEntityDTO.getPublicationZdbID().startsWith("ZDB-PUB-")) {
            this.publicationZdbID = relatedEntityDTO.getPublicationZdbID();
        }
    }


    public String getOptionalName() {
        return optionalName;
    }

    public void setOptionalName(String optionalName) {
        this.optionalName = optionalName;
    }

    public Integer getFeatureStartLoc() {
        return featureStartLoc;
    }

    public void setFeatureStartLoc(Integer featureStartLoc) {
        this.featureStartLoc = featureStartLoc;
    }

    public Integer getFeatureEndLoc() {
        return featureEndLoc;
    }

    public void setFeatureEndLoc(Integer featureEndLoc) {
        this.featureEndLoc = featureEndLoc;
    }

    public List<String> getFeatureAliases() {
        return featureAliases;
    }

    public void setFeatureAliases(List<String> featureAliases) {
        this.featureAliases = featureAliases;
    }

    public String getTransgenicSuffix() {
        return transgenicSuffix;
    }

    public void setTransgenicSuffix(String transgenicSuffix) {
        this.transgenicSuffix = transgenicSuffix;
    }

    public String getFeatureAssay() {
        return featureAssay;
    }

    public void setFeatureAssay(String featureAssay) {
        this.featureAssay = featureAssay;
    }

    public List<CuratorNoteDTO> getCuratorNotes() {
        return curatorNotes;
    }

    public void setCuratorNotes(List<CuratorNoteDTO> curatorNotes) {
        this.curatorNotes = curatorNotes;
    }

    public NoteDTO getPublicNote() {
        return publicNote;
    }

    public void setPublicNote(NoteDTO publicNote) {
        this.publicNote = publicNote;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Boolean getDominant() {
        return dominant;
    }

    public void setDominant(Boolean dominant) {
        this.dominant = dominant;
    }

    public Boolean getKnownInsertionSite() {
        return knownInsertionSite;
    }

    public void setKnownInsertionSite(Boolean knownInsertionSite) {
        this.knownInsertionSite = knownInsertionSite;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLabOfOrigin() {
        return labOfOrigin;
    }

    public void setLabOfOrigin(String labOfOrigin) {
        this.labOfOrigin = labOfOrigin;
    }

    public int getLabPrefixID() {
        return labPrefixID;
    }

    public void setLabPrefixID(int labPrefixID) {
        this.labPrefixID = labPrefixID;
    }

    public String getLabPrefix() {
        return labPrefix;
    }

    public void setLabPrefix(String labPrefix) {
        this.labPrefix = labPrefix;
    }

    public FeatureTypeEnum getFeatureType() {
        return featureType;
    }

    public void setFeatureType(FeatureTypeEnum featureType) {
        this.featureType = featureType;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    public String getMutagen() {
        return mutagen;
    }

    public void setMutagen(String mutagen) {
        this.mutagen = mutagen;
    }

    public String getMutagee() {
        return mutagee;
    }

    public void setMutagee(String mutagee) {
        this.mutagee = mutagee;
    }


    public List<NoteDTO> getPublicNoteList() {
        return publicNoteList;
    }

    public void setPublicNoteList(List<NoteDTO> publicNoteList) {
        this.publicNoteList = publicNoteList;
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

    public String getFgmdSeqVar() {
        return fgmdSeqVar;
    }

    public void setFgmdSeqVar(String fgmdSeqVar) {
        this.fgmdSeqVar = fgmdSeqVar;
    }

    public String getFgmdSeqRef() {
        return fgmdSeqRef;
    }

    public void setFgmdSeqRef(String fgmdSeqRef) {
        this.fgmdSeqRef = fgmdSeqRef;
    }

    public Set<MutationDetailTranscriptChangeDTO> getTranscriptChangeDTOSet() {
        return transcriptChangeDTOSet;
    }

    public void setTranscriptChangeDTOSet(Set<MutationDetailTranscriptChangeDTO> transcriptChangeDTOSet) {
        this.transcriptChangeDTOSet = transcriptChangeDTOSet;
    }

    public void addTranscriptChange(MutationDetailTranscriptChangeDTO dto) {
        if (transcriptChangeDTOSet == null)
            transcriptChangeDTOSet = new HashSet<>(5);
        transcriptChangeDTOSet.add(dto);
    }

    /**
     * Only returning the shallow values.
     *
     * @return A FeatureDTO object that has a valid link associated with it.
     */
    public FeatureDTO deepCopy() {
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.zdbID = zdbID;
        featureDTO.dataZdbID = dataZdbID;
        featureDTO.name = name;
        featureDTO.link = link;
        featureDTO.publicationZdbID = publicationZdbID;
        featureDTO.abbreviation = abbreviation;
        featureDTO.alias = alias;
        featureDTO.curatorNotes = curatorNotes;
        featureDTO.dominant = dominant;
        featureDTO.featureAliases = featureAliases;
        featureDTO.featureSequence = featureSequence;
        featureDTO.featureAssay = featureAssay;
        featureDTO.featureType = featureType;
        featureDTO.knownInsertionSite = knownInsertionSite;
        featureDTO.labOfOrigin = labOfOrigin;
        featureDTO.labPrefix = labPrefix;
        featureDTO.labPrefixID = labPrefixID;
        featureDTO.lineNumber = lineNumber;
        featureDTO.publicNote = publicNote;
        featureDTO.transgenicSuffix = transgenicSuffix;
        featureDTO.mutagee = mutagee;
        featureDTO.mutagen = mutagen;
        return featureDTO;
    }

    public String getDisplayNameForGenotypeBase() {
        return displayNameForGenotypeBase;
    }

    public void setDisplayNameForGenotypeBase(String displayNameForGenotypeBase) {
        this.displayNameForGenotypeBase = displayNameForGenotypeBase;
    }

    public String getDisplayNameForGenotypeSuperior() {
        return displayNameForGenotypeSuperior;
    }

    public void setDisplayNameForGenotypeSuperior(String displayNameForGenotypeSuperior) {
        this.displayNameForGenotypeSuperior = displayNameForGenotypeSuperior;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeatureDTO");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }


    public void addPublicNote(NoteDTO publicNoteDTO) {
        if (publicNoteList == null)
            publicNoteList = new ArrayList<>(2);
        publicNoteList.add(publicNoteDTO);
    }

    @Override
    public String getLabel() {
        return abbreviation;
    }

    @Override
    public String getValue() {
        return zdbID;
    }
}