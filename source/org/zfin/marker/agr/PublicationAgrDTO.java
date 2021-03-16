package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class PublicationAgrDTO {
    public static final String PMID = "PMID:";


    private CrossReferenceDTO crossReference;
    private String publicationId;


    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }




}
