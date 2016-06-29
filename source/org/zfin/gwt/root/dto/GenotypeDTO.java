package org.zfin.gwt.root.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
public class GenotypeDTO extends RelatedEntityDTO {

    private String handle;
    private String nickName;
    private List<GenotypeDTO> backgroundGenotypeList;
    private List<FeatureDTO> featureList;
    private List<GenotypeFeatureDTO> genotypeFeatureList;
    private List<ExternalNoteDTO> publicNotes;
    private List<CuratorNoteDTO> privateNotes;
    private boolean wildtype;

    public boolean isWildtype() {
        return wildtype;
    }

    public void setWildtype(boolean wildtype) {
        this.wildtype = wildtype;
    }

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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public List<GenotypeFeatureDTO> getGenotypeFeatureList() {
        return genotypeFeatureList;
    }

    public void setGenotypeFeatureList(List<GenotypeFeatureDTO> genotypeFeatureList) {
        this.genotypeFeatureList = genotypeFeatureList;
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
        for (ExternalNoteDTO note : publicNotes) {
            if (note.getPublicationDTO().getZdbID().equals(publicationID))
                truncatedList.add(note);
        }
        return truncatedList;
    }

    public void setPublicNotes(List<ExternalNoteDTO> publicNotes) {
        this.publicNotes = publicNotes;
    }

    public List<GenotypeDTO> getBackgroundGenotypeList() {
        return backgroundGenotypeList;
    }

    public void setBackgroundGenotypeList(List<GenotypeDTO> backgroundGenotypeList) {
        this.backgroundGenotypeList = backgroundGenotypeList;
    }

    public String getNamePlusBackground() {
        StringBuilder builder = new StringBuilder(name);
        if (backgroundGenotypeList != null) {
            Collections.sort(backgroundGenotypeList, new Comparator<GenotypeDTO>() {
                @Override
                public int compare(GenotypeDTO o1, GenotypeDTO o2) {
                    return o1.getHandle().compareToIgnoreCase(o2.getHandle());
                }
            });
            for (GenotypeDTO genoDTO : backgroundGenotypeList) {
                builder.append(" (");
                builder.append(genoDTO.getHandle());
                builder.append("), ");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    public void addBackgroundGenotype(GenotypeDTO genotypeDTO) {
        if (backgroundGenotypeList == null)
            backgroundGenotypeList = new ArrayList<>(2);
        backgroundGenotypeList.add(genotypeDTO);
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof GenotypeDTO))
            throw new RuntimeException("Can only compare genotypeDTO objects");
        GenotypeDTO genotypeDTO = (GenotypeDTO) o;
        if (wildtype && !genotypeDTO.isWildtype())
            return -1;
        if (!wildtype && genotypeDTO.isWildtype())
            return 1;
        return name.compareTo(genotypeDTO.getName());
    }
}
