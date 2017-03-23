package org.zfin.marker.agr;

public class PublicationAgrDTO {

    private String publicationModId;
    private String pubMedId;

    public PublicationAgrDTO(String publicationModId, String pubMedId) {
        this.publicationModId = publicationModId;
        this.pubMedId = pubMedId;
    }

    public String getPublicationModId() {
        return publicationModId;
    }

    public String getPubMedId() {
        return pubMedId;
    }

}
