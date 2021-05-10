package org.zfin.marker.agr;

import java.util.List;

public class EvidenceDTO {

    private PublicationAgrDTO publication;
    private List<String> evidenceCodes;

    public EvidenceDTO(PublicationAgrDTO publication) {
        this.publication = publication;
    }

    public List<String> getEvidenceCodes() {
        return evidenceCodes;
    }

    public void setEvidenceCodes(List<String> evidenceCodes) {
        this.evidenceCodes = evidenceCodes;
    }

    public PublicationAgrDTO getPublication() {
        return publication;
    }
}
