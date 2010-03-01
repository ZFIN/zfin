package org.zfin.gwt.root.dto;

import java.util.List;

/**
 * Transcript RPC object.
 */
public class MarkerDTO extends RelatedEntityDTO {

    protected List<DBLinkDTO> supportingSequenceLinks;
    private String abbreviation;
    protected String abbreviationOrder;
    protected String markerType;
    protected List<String> recordAttributions;
    protected NoteDTO publicNote;
    protected List<NoteDTO> curatorNotes;
    protected List<RelatedEntityDTO> aliasRelatedEntities;
    protected List<MarkerDTO> relatedGeneAttributes;
    private List<MarkerDTO> relatedCloneAttributes;
    private List<MarkerDTO> targetedGeneAttributes;
    private List<RelatedEntityDTO> proteinRelatedEntities;
    private List<SequenceDTO> proteinSequences;
    protected List<String> suppliers;
    protected List<SequenceDTO> rnaSequences;
    protected String markerRelationshipType;
    protected boolean zdbIDThenAbbrev;

    public MarkerDTO() {
    }

    public MarkerDTO(RelatedEntityDTO relatedEntityDTO) {
        this.dataZdbID = relatedEntityDTO.getDataZdbID();
        this.name = relatedEntityDTO.getName();
        this.link = relatedEntityDTO.getLink();
        if (relatedEntityDTO.getPublicationZdbID() != null && true == relatedEntityDTO.getPublicationZdbID().startsWith("ZDB-PUB-")) {
            this.publicationZdbID = relatedEntityDTO.getPublicationZdbID();
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviationOrder() {
        return abbreviationOrder;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }

    public String getMarkerType() {
        return markerType;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public NoteDTO getPublicNote() {
        return publicNote;
    }

    public void setPublicNote(NoteDTO publicNote) {
        this.publicNote = publicNote;
    }

    public List<NoteDTO> getCuratorNotes() {
        return curatorNotes;
    }

    public void setCuratorNotes(List<NoteDTO> curatorNotes) {
        this.curatorNotes = curatorNotes;
    }

    public List<RelatedEntityDTO> getAliasAttributes() {
        return aliasRelatedEntities;
    }

    public void setAliasAttributes(List<RelatedEntityDTO> aliasRelatedEntities) {
        this.aliasRelatedEntities = aliasRelatedEntities;
    }

    public List<MarkerDTO> getRelatedGeneAttributes() {
        return relatedGeneAttributes;
    }

    public void setRelatedGeneAttributes(List<MarkerDTO> relatedGeneAttributes) {
        this.relatedGeneAttributes = relatedGeneAttributes;
    }

    public List<MarkerDTO> getRelatedCloneAttributes() {
        return relatedCloneAttributes;
    }

    public void setRelatedCloneAttributes(List<MarkerDTO> relatedCloneAttributes) {
        this.relatedCloneAttributes = relatedCloneAttributes;
    }

    public List<MarkerDTO> getTargetedGeneAttributes() {
        return targetedGeneAttributes;
    }

    public void setTargetedGeneAttributes(List<MarkerDTO> targetedGeneAttributes) {
        this.targetedGeneAttributes = targetedGeneAttributes;
    }

    public List<RelatedEntityDTO> getRelatedProteinAttributes() {
        return proteinRelatedEntities;
    }

    public void setRelatedProteinAttributes(List<RelatedEntityDTO> proteinRelatedEntities) {
        this.proteinRelatedEntities = proteinRelatedEntities;
    }

    public List<SequenceDTO> getProteinSequences() {
        return proteinSequences;
    }

    public void setProteinSequences(List<SequenceDTO> proteinSequences) {
        this.proteinSequences = proteinSequences;
    }


    public List<String> getRecordAttributions() {
        return recordAttributions;
    }

    public void setRecordAttributions(List<String> recordAttributions) {
        this.recordAttributions = recordAttributions;
    }

    public List<String> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<String> suppliers) {
        this.suppliers = suppliers;
    }

    public List<DBLinkDTO> getSupportingSequenceLinks() {
        return supportingSequenceLinks;
    }

    public void setSupportingSequenceLinks(List<DBLinkDTO> supportingSequenceLinks) {
        this.supportingSequenceLinks = supportingSequenceLinks;
    }

    public List<SequenceDTO> getRnaSequences() {
        return rnaSequences;
    }

    public void setRnaSequences(List<SequenceDTO> rnaSequences) {
        this.rnaSequences = rnaSequences;
    }

    public String getOrderingValue() {
        return abbreviationOrder;
    }

    public String getMarkerRelationshipType() {
        return markerRelationshipType;
    }

    public void setMarkerRelationshipType(String markerRelationshipType) {
        this.markerRelationshipType = markerRelationshipType;
    }

    public boolean isZdbIDThenAbbrev() {
        return zdbIDThenAbbrev;
    }

    public void setZdbIDThenAbbrev(boolean zdbIDThenAbbrev) {
        this.zdbIDThenAbbrev = zdbIDThenAbbrev;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Only returning the shallow values.
     *
     * @return A MarkerDTO object that has a valid link assoicated with it.
     */
    public MarkerDTO deepCopy() {
        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.supportingSequenceLinks = supportingSequenceLinks;
        markerDTO.zdbID = zdbID;
        markerDTO.name = name;
        markerDTO.abbreviationOrder = abbreviationOrder;
        markerDTO.markerType = markerType;
        markerDTO.setLink(link);
        markerDTO.setPublicationZdbID(publicationZdbID);
        markerDTO.markerRelationshipType = markerRelationshipType;
        markerDTO.zdbIDThenAbbrev = zdbIDThenAbbrev;
        return markerDTO;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MarkerDTO");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", abbreviationOrder='").append(abbreviationOrder).append('\'');
        sb.append(", markerType='").append(markerType).append('\'');
        sb.append(", recordAttributions=").append(recordAttributions);
        sb.append(", publicNote=").append(publicNote);
        sb.append(", curatorNotes=").append(curatorNotes);
        sb.append(", relatedGeneAttributes=").append(relatedGeneAttributes);
        sb.append(", suppliers=").append(suppliers);
        sb.append(", rnaSequences=").append(rnaSequences);
        sb.append(", markerRelationshipType='").append(markerRelationshipType).append('\'');
        sb.append(", zdbIDThenAbbrev=").append(zdbIDThenAbbrev);
        sb.append(", aliasRelatedEntities=").append(aliasRelatedEntities);
        sb.append(", supportingSequenceLinks=").append(supportingSequenceLinks);
        sb.append('}');
        return sb.toString();
    }
}
