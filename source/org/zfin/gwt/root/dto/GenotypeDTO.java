package org.zfin.gwt.root.dto;

/**
 */
public class GenotypeDTO extends RelatedEntityDTO {

    private String handle;

    public GenotypeDTO() {
    }

    public GenotypeDTO(RelatedEntityDTO relatedEntityDTO) {
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
    public GenotypeDTO deepCopy() {
        GenotypeDTO featureDTO = new GenotypeDTO();
        featureDTO.zdbID = zdbID;
        featureDTO.name = name;
        featureDTO.setLink(link);
        featureDTO.setPublicationZdbID(publicationZdbID);
        return featureDTO;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GenotypeDTO");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

}
