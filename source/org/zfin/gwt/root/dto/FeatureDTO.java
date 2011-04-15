package org.zfin.gwt.root.dto;


import java.util.List;


/**
 * Transcript RPC object.

 */

public class FeatureDTO extends RelatedEntityDTO {


    private String optionalName ; // this is the name entered from the GUI, not stored in the database
    private String abbreviation;  // this is the name (the full name) without the optionalName except for some cases
    private Boolean dominant = false;
    private String mutagen;
    private String mutagee;
    private String featureAssay;
    private Boolean knownInsertionSite = false ;
    private NoteDTO publicNote;
    private List<NoteDTO> curatorNotes;
    protected String alias;
    protected FeatureTypeEnum featureType;
    protected String lineNumber;
    protected String labPrefix;
    protected List<String> featureAliases;
    protected String featureSequence;
    protected List<String> featureSequences;

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
    protected int labPrefixID;

    public FeatureDTO() { }

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

    public List<NoteDTO> getCuratorNotes() {
        return curatorNotes;
    }

    public void setCuratorNotes(List<NoteDTO> curatorNotes) {
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




    /**
     * Only returning the shallow values.
     *
     * @return A MarkerDTO object that has a valid link assoicated with it.
     */
    public FeatureDTO deepCopy() {
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.zdbID = zdbID;
        featureDTO.dataZdbID = dataZdbID ;
        featureDTO.name = name;
        featureDTO.link = link;
        featureDTO.publicationZdbID = publicationZdbID;
        featureDTO.abbreviation = abbreviation ;
        featureDTO.alias = alias ;
        featureDTO.curatorNotes = curatorNotes;
        featureDTO.dominant = dominant ;
        featureDTO.featureAliases = featureAliases ;
        featureDTO.featureSequence = featureSequence ;
        featureDTO.featureAssay = featureAssay ;
        featureDTO.featureType = featureType ;
        featureDTO.knownInsertionSite = knownInsertionSite ;
        featureDTO.labOfOrigin = labOfOrigin ;
        featureDTO.labPrefix = labPrefix;
        featureDTO.labPrefixID = labPrefixID ;
        featureDTO.lineNumber = lineNumber ;
        featureDTO.publicNote = publicNote ;
        featureDTO.transgenicSuffix = transgenicSuffix ;
        featureDTO.mutagee = mutagee ;
        featureDTO.mutagen = mutagen ;
        return featureDTO;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeatureDTO");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }


}