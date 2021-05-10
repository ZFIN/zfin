package org.zfin.orthology.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.TermDTO;

public class OrthologEvidenceDTO {

    @JsonView(View.OrthologyAPI.class)
    private String evidenceCode;
    @JsonView(View.OrthologyAPI.class)
    private String evidenceName;
    @JsonView(View.OrthologyAPI.class)
    private TermDTO evidenceTerm;
    private OrthologDTO ortholog;
    @JsonView(View.OrthologyAPI.class)
    private PublicationDTO publication;

    public TermDTO getEvidenceTerm() {
        return evidenceTerm;
    }

    public void setEvidenceTerm(TermDTO evidenceTerm) {
        this.evidenceTerm = evidenceTerm;
    }

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
