package org.zfin.gwt.root.dto;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class GenotypeDTO extends RelatedEntityDTO {

    private String handle;
    private List<FeatureDTO> featureList;
    private List<ExternalNoteDTO> publicNotes;
    private List<CuratorNoteDTO> privateNotes;

    public GenotypeDTO() {
    }

    public GenotypeDTO(RelatedEntityDTO relatedEntityDTO) {
        this.dataZdbID = relatedEntityDTO.getDataZdbID();
        this.name = relatedEntityDTO.getName();
        this.link = relatedEntityDTO.getLink();
        if (relatedEntityDTO.getPublicationZdbID() != null && relatedEntityDTO.getPublicationZdbID().startsWith("ZDB-PUB-")) {
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
        final StringBuilder builder = new StringBuilder("GenotypeDTO");
        builder.append("{zdbID='").append(zdbID).append('\'');
        builder.append(", name='").append(name).append('\'');
        builder.append('}');
        return builder.toString();
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public List<FeatureDTO> getFeatureList() {
        return featureList;
    }

    public void setFeatureList(List<FeatureDTO> featureList) {
        this.featureList = featureList;
    }

    public List<CuratorNoteDTO> getPrivateNotes() {
        return privateNotes;
    }

    public void setPrivateNotes(List<CuratorNoteDTO> privateNotes) {
        this.privateNotes = privateNotes;
    }

    public List<ExternalNoteDTO> getPublicNotes() {
        return publicNotes;
    }

    public List<ExternalNoteDTO> getPublicNotes(String publicationID) {
        if (publicNotes == null)
            return null;
        List<ExternalNoteDTO> truncatedList = new ArrayList<>();
        for (ExternalNoteDTO note : publicNotes)
            if (note.publicationZdbID.equals(publicationID))
                truncatedList.add(note);
        return truncatedList;
    }

    public void setPublicNotes(List<ExternalNoteDTO> publicNotes) {
        this.publicNotes = publicNotes;
    }
}
