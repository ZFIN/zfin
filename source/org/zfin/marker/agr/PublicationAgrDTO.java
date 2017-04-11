package org.zfin.marker.agr;

import org.zfin.infrastructure.ActiveSource;

public class PublicationAgrDTO {

    public static final String PMID = "PMID:";
    private String publicationModId;
    private String pubMedId;

    public PublicationAgrDTO(String publicationModId, String pubMedId) {
        if (ActiveSource.validateActiveData(publicationModId))
            this.publicationModId = ZfinDTO.ZFIN;
        this.publicationModId += publicationModId;
        this.pubMedId = PMID + pubMedId;
    }

    public String getPublicationModId() {
        return publicationModId;
    }

    public String getPubMedId() {
        return pubMedId;
    }

}
