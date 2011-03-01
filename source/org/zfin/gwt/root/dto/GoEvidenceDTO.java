package org.zfin.gwt.root.dto;

import java.util.Date;
import java.util.Set;

/**
 * This class loosely mirrors MarkerGoTermEvidence.
 */
public class GoEvidenceDTO extends RelatedEntityDTO {

    public static final String ZFIN_ORGANIZATION_SOURCE = "ZFIN";

    private MarkerDTO markerDTO;
    private TermDTO goTermDTO;
    private GoEvidenceCodeEnum evidenceCode;
    private GoEvidenceQualifier flag;
    private String note;
    private String organizationSource;

    private String createdPersonName;
    private String modifiedPersonName;
    private Date createdDate;
    private Date modifiedDate;

    private Set<String> inferredFrom;
    private Set<String> inferredFromLinks;

    public String getFirstInference() {
        if (inferredFrom != null && inferredFrom.size() > 0) {
            return inferredFrom.iterator().next();
        }
        return null;
    }

    public MarkerDTO getMarkerDTO() {
        return markerDTO;
    }


    public void setMarkerDTO(MarkerDTO markerDTO) {
        this.markerDTO = markerDTO;
    }

    public TermDTO getGoTerm() {
        return goTermDTO;
    }

    public void setGoTerm(TermDTO goTermZdbID) {
        this.goTermDTO = goTermZdbID;
    }

    public GoEvidenceCodeEnum getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(GoEvidenceCodeEnum evidenceCodeEnum) {
        this.evidenceCode = evidenceCodeEnum;
    }

    public GoEvidenceQualifier getFlag() {
        return flag;
    }

    public void setFlag(GoEvidenceQualifier evidenceQualifier) {
        this.flag = evidenceQualifier;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedPersonName() {
        return createdPersonName;
    }

    public void setCreatedPersonName(String createdPersonName) {
        this.createdPersonName = createdPersonName;
    }

    public String getModifiedPersonName() {
        return modifiedPersonName;
    }

    public void setModifiedPersonName(String modifiedPersonName) {
        this.modifiedPersonName = modifiedPersonName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Set<String> getInferredFrom() {
        return inferredFrom;
    }

    public void setInferredFrom(Set<String> inferredFrom) {
        this.inferredFrom = inferredFrom;
    }

    public Set<String> getInferredFromLinks() {
        return inferredFromLinks;
    }

    public void setInferredFromLinks(Set<String> inferredFromLinks) {
        this.inferredFromLinks = inferredFromLinks;
    }

    public String getOrganizationSource() {
        return organizationSource;
    }

    public void setOrganizationSource(String organizationSource) {
        this.organizationSource = organizationSource;
    }

    public GoEvidenceDTO deepCopy() {
        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setDataZdbID(dataZdbID);
        goEvidenceDTO.setPublicationZdbID(publicationZdbID);
        goEvidenceDTO.setName(name);
        goEvidenceDTO.setLink(link);
        goEvidenceDTO.setZdbID(zdbID);
        goEvidenceDTO.setCreatedDate(createdDate);
        goEvidenceDTO.setCreatedPersonName(createdPersonName);
        goEvidenceDTO.setModifiedDate(modifiedDate);
        goEvidenceDTO.setModifiedPersonName(modifiedPersonName);
        goEvidenceDTO.setEvidenceCode(evidenceCode);
        goEvidenceDTO.setFlag(flag);
        goEvidenceDTO.setGoTerm(goTermDTO);
        goEvidenceDTO.setMarkerDTO(markerDTO);
        goEvidenceDTO.setNote(note);
        goEvidenceDTO.setInferredFrom(inferredFrom);
        goEvidenceDTO.setInferredFromLinks(inferredFromLinks);
        goEvidenceDTO.setOrganizationSource(organizationSource);
        return goEvidenceDTO;
    }

    public boolean isZfinOrganizationSource() {
        if (organizationSource != null) {
            return organizationSource.equals(ZFIN_ORGANIZATION_SOURCE);
        }
        return false;
    }
}
