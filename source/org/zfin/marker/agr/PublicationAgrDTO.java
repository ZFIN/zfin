package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.zfin.infrastructure.ActiveSource;

public class PublicationAgrDTO {

    public static final String PMID = "PMID:";
    private String modPublicationId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pubMedId;

    public PublicationAgrDTO(String publicationModId, Integer pubMedId) {
        if (ActiveSource.validateActiveData(publicationModId))
            this.modPublicationId = ZfinDTO.ZFIN;
        this.modPublicationId += publicationModId;
        if (pubMedId != null)
            this.pubMedId = PMID + pubMedId;
    }

    public String getModPublicationId() {
        return modPublicationId;
    }

    public String getPubMedId() {
        return pubMedId;
    }

}
