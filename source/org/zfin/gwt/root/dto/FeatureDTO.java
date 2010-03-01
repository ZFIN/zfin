package org.zfin.gwt.root.dto;

/**
 * Transcript RPC object.
 */
public class FeatureDTO extends RelatedEntityDTO {


    public FeatureDTO() { }

    public FeatureDTO(RelatedEntityDTO relatedEntityDTO) {
        this.dataZdbID = relatedEntityDTO.getDataZdbID();
        this.name = relatedEntityDTO.getName();
        this.link = relatedEntityDTO.getLink();
        if (relatedEntityDTO.getPublicationZdbID() != null && true == relatedEntityDTO.getPublicationZdbID().startsWith("ZDB-PUB-")) {
            this.publicationZdbID = relatedEntityDTO.getPublicationZdbID();
        }
    }


    /**
     * Only returning the shallow values.
     *
     * @return A MarkerDTO object that has a valid link assoicated with it.
     */
    public FeatureDTO deepCopy() {
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.zdbID = zdbID;
        featureDTO.name = name;
        featureDTO.setLink(link);
        featureDTO.setPublicationZdbID(publicationZdbID);
        return featureDTO;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeatureDTO");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}