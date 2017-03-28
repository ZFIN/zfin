package org.zfin.marker.agr;

import java.util.List;

public class EvidenceDTO {

    private String evidence;
    private List<PublicationAgrDTO> publications;

    public EvidenceDTO(String evidence) {
        this.evidence = evidence;
    }


    public List<PublicationAgrDTO> getPublications() {
        return publications;
    }

    public void setPublications(List<PublicationAgrDTO> publications) {
        this.publications = publications;
    }

    public String getEvidence() {
        return evidence;
    }
}
