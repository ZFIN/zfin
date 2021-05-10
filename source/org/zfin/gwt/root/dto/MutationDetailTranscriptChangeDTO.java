package org.zfin.gwt.root.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class MutationDetailTranscriptChangeDTO implements IsSerializable {

    private String zdbID;
    private Integer exonNumber;
    private Integer intronNumber;
    private TermDTO consequence;
    private String consequenceOboID;
    private String consequenceName;

    public String getConsequenceOboID() {
        return consequenceOboID;
    }

    public String getConsequenceName() {
        return consequenceName;
    }

    public void setConsequenceName(String consequenceName) {
        this.consequenceName = consequenceName;
    }

    public void setConsequenceOboID(String consequenceOboID) {
        this.consequenceOboID = consequenceOboID;
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

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

}