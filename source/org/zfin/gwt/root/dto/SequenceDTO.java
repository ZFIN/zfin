package org.zfin.gwt.root.dto;

/**
 */
public class SequenceDTO extends DBLinkDTO{

    private String defLine ;
    private String sequence;
    private String attributionType ;

    public String getDefLine() {
        return defLine;
    }

    public void setDefLine(String defLine) {
        this.defLine = defLine;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getAttributionType() {
        return attributionType;
    }

    public void setAttributionType(String attributionType) {
        this.attributionType = attributionType;
    }

    public SequenceDTO deepCopy() {
        SequenceDTO dto = new SequenceDTO();

        dto.setDataZdbID(dataZdbID);
        dto.setDataName(dataName);
        dto.setDbLinkZdbID(dbLinkZdbID);
        dto.setLength(length);
        dto.setEditable(isEditable());
        dto.setLink(getLink());
        dto.setName(name);
        dto.setPublicationZdbID(publicationZdbID);
        dto.setRecordAttributions(getRecordAttributions());
        dto.setReferenceDatabaseDTO(getReferenceDatabaseDTO().clone());
        dto.setView(getView());

        dto.defLine = defLine;
        dto.sequence = sequence;
        dto.attributionType = attributionType;

        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequenceDTO sequenceDTO = (SequenceDTO) o;

        if (defLine != null ? !defLine.equals(sequenceDTO.defLine) : sequenceDTO.defLine != null) return false;
        if (sequence != null ? !sequence.equals(sequenceDTO.sequence) : sequenceDTO.sequence != null) return false;
        if (publicationZdbID != null ? !publicationZdbID.equals(sequenceDTO.publicationZdbID) : sequenceDTO.publicationZdbID!= null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = defLine != null ? defLine.hashCode() : 0;
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        return result;
    }
}
