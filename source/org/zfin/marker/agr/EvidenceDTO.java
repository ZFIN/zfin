package org.zfin.marker.agr;

import java.util.List;

public class EvidenceDTO {

    private String evidenceCode;
    private List<PublicationAgrDTO> publications;

    public EvidenceDTO(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }


    public List<PublicationAgrDTO> getPublications() {
        return publications;
    }

    public void setPublications(List<PublicationAgrDTO> publications) {
        this.publications = publications;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }
}
