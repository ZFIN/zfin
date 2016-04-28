package org.zfin.gwt.root.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class MutationDetailDnaChangeDTO implements IsSerializable {

    private String zdbID;
    private Integer positionStart;
    private Integer positionEnd;
    private String sequenceReferenceAccessionNumber;
    private Integer numberAddedBasePair;
    private Integer numberRemovedBasePair;
    private Integer exonNumber;
    private Integer intronNumber;
    private String localizationTermOboID;
    private String changeTermOboId;


    public String getChangeTermOboId() {
        return changeTermOboId;
    }

    public void setChangeTermOboId(String changeTermOboId) {
        this.changeTermOboId = changeTermOboId;
    }

    public Integer getExonNumber() {
        return exonNumber;
    }

    public void setExonNumber(Integer exonNumber) {
        this.exonNumber = exonNumber;
    }

    public Integer getIntronNumber() {
        return intronNumber;
    }

    public void setIntronNumber(Integer intronNumber) {
        this.intronNumber = intronNumber;
    }

    public String getLocalizationTermOboID() {
        return localizationTermOboID;
    }

    public void setLocalizationTermOboID(String localizationTermOboID) {
        this.localizationTermOboID = localizationTermOboID;
    }

    public Integer getNumberAddedBasePair() {
        return numberAddedBasePair;
    }

    public void setNumberAddedBasePair(Integer numberAddedBasePair) {
        this.numberAddedBasePair = numberAddedBasePair;
    }

    public Integer getNumberRemovedBasePair() {
        return numberRemovedBasePair;
    }

    public void setNumberRemovedBasePair(Integer numberRemovedBasePair) {
        this.numberRemovedBasePair = numberRemovedBasePair;
    }

    public Integer getPositionEnd() {
        return positionEnd;
    }

    public void setPositionEnd(Integer positionEnd) {
        this.positionEnd = positionEnd;
    }

    public Integer getPositionStart() {
        return positionStart;
    }

    public void setPositionStart(Integer positionStart) {
        this.positionStart = positionStart;
    }

    public String getSequenceReferenceAccessionNumber() {
        return sequenceReferenceAccessionNumber;
    }

    public void setSequenceReferenceAccessionNumber(String sequenceReferenceAccessionNumber) {
        this.sequenceReferenceAccessionNumber = sequenceReferenceAccessionNumber;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}