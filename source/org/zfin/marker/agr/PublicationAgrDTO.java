package org.zfin.marker.agr;

import org.zfin.infrastructure.ActiveSource;

public class PublicationAgrDTO {

    private String publicationModId;
    private String pubMedId;

    public PublicationAgrDTO(String publicationModId, String pubMedId) {
        if (ActiveSource.validateActiveData(publicationModId))
            this.publicationModId = ZfinDTO.ZFIN;
        this.publicationModId += publicationModId;
        this.pubMedId = pubMedId;
    }

    public String getPublicationModId() {
        return publicationModId;
    }

    public String getPubMedId() {
        return pubMedId;
    }

}
