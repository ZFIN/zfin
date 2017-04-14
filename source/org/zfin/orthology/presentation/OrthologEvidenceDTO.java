package org.zfin.orthology.presentation;

import org.zfin.gwt.root.dto.PublicationDTO;

public class OrthologEvidenceDTO {

    private String evidenceCode;
    private String evidenceName;
    private OrthologDTO ortholog;
    private PublicationDTO publication;

    public OrthologDTO getOrtholog() {
        return ortholog;
    }

    public void setOrtholog(OrthologDTO ortholog) {
        this.ortholog = ortholog;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getEvidenceName() {
        return evidenceName;
    }

    public void setEvidenceName(String evidenceName) {
        this.evidenceName = evidenceName;
    }

    public PublicationDTO getPublication() {
        return publication;
    }

    public void setPublication(PublicationDTO publication) {
        this.publication = publication;
    }
}
