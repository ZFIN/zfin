package org.zfin.orthology.presentation;

import java.util.List;

public class OrthologPublicationEvidenceCodeDTO {

    private List<String> evidenceCodeList;
    private String orthologID;
    private String publicationID;

    public List<String> getEvidenceCodeList() {
        return evidenceCodeList;
    }

    public void setEvidenceCodeList(List<String> evidenceCodeList) {
        this.evidenceCodeList = evidenceCodeList;
    }

    public String getOrthologID() {
        return orthologID;
    }

    public void setOrthologID(String orthologID) {
        this.orthologID = orthologID;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }
}
