package org.zfin.gwt.root.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class MutationDetailProteinChangeDTO implements IsSerializable {

    private String zdbID;
    private Integer positionStart;
    private Integer positionEnd;
    private String sequenceReferenceAccessionNumber;
    private Integer numberAddedAminoAcid;
    private Integer numberRemovedAminoAcid;
    private String consequenceTermOboID;
    private String wildtypeAATermOboID;
    private String mutantAATermOboID;


    public String getConsequenceTermOboID() {
        return consequenceTermOboID;
    }

    public void setConsequenceTermOboID(String consequenceTermOboID) {
        this.consequenceTermOboID = consequenceTermOboID;
    }

    public String getMutantAATermOboID() {
        return mutantAATermOboID;
    }

    public void setMutantAATermOboID(String mutantAATermOboID) {
        this.mutantAATermOboID = mutantAATermOboID;
    }

    public String getWildtypeAATermOboID() {
        return wildtypeAATermOboID;
    }

    public void setWildtypeAATermOboID(String wildtypeAATermOboID) {
        this.wildtypeAATermOboID = wildtypeAATermOboID;
    }

    public Integer getNumberAddedAminoAcid() {
        return numberAddedAminoAcid;
    }

    public void setNumberAddedAminoAcid(Integer numberAddedAminoAcid) {
        this.numberAddedAminoAcid = numberAddedAminoAcid;
    }

    public Integer getNumberRemovedAminoAcid() {
        return numberRemovedAminoAcid;
    }

    public void setNumberRemovedAminoAcid(Integer numberRemovedAminoAcid) {
        this.numberRemovedAminoAcid = numberRemovedAminoAcid;
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