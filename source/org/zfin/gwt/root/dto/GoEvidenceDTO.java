package org.zfin.gwt.root.dto;

import java.util.Date;
import java.util.Set;

/**
 */
public class GoEvidenceDTO extends RelatedEntityDTO{


    private MarkerDTO markerDTO;
    private GoTermDTO goTermDTO;
    private GoEvidenceCodeEnum evidenceCode;
    private GoFlagEnum flag;
    private String note;

    private String createdPersonName;
    private String modifiedPersonName;
    private Date createdDate;
    private Date modifiedDate ;

    Set<String> inferredFrom ;

    public MarkerDTO getMarkerDTO() {
        return markerDTO;
    }

    public void setMarkerDTO(MarkerDTO markerDTO) {
        this.markerDTO = markerDTO;
    }

    public GoTermDTO getGoTerm() {
        return goTermDTO;
    }

    public void setGoTerm(GoTermDTO goTermZdbID) {
        this.goTermDTO = goTermZdbID;
    }

    public GoEvidenceCodeEnum getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(GoEvidenceCodeEnum evidenceCodeEnum) {
        this.evidenceCode = evidenceCodeEnum;
    }

    public GoFlagEnum getFlag() {
        return flag;
    }

    public void setFlag(GoFlagEnum flagEnum) {
        this.flag = flagEnum;
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

    public GoEvidenceDTO deepCopy() {
        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO() ;
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
        return goEvidenceDTO;
    }
}
