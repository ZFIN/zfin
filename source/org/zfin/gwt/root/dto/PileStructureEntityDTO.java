package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * Data Transfer Object individual structures on the pile.
 */
public class PileStructureEntityDTO implements IsSerializable, Comparable<PileStructureEntityDTO> {

    private ExpressedTermDTO annotationTerm;
    protected String zdbID;
    private String creator;
    private Date date;

    public ExpressedTermDTO getAnnotationTerm() {
        return annotationTerm;
    }

    public void setAnnotationTerm(ExpressedTermDTO annotationTerm) {
        this.annotationTerm = annotationTerm;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PileStructureEntityDTO copy() {
        PileStructureEntityDTO dto = new PileStructureEntityDTO();
        dto.zdbID = zdbID;
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PileStructureEntityDTO structureDTO = (PileStructureEntityDTO) o;

        if (zdbID != null ? !zdbID.equals(structureDTO.zdbID) : structureDTO.zdbID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = 31 + (zdbID != null ? zdbID.hashCode() : 0);
        return result;
    }

    public int compareTo(PileStructureEntityDTO o) {
        if (o == null)
            return -1;
        if (o.getAnnotationTerm() == null)
            return -1;
        if (annotationTerm == null)
            return 1;
        return annotationTerm.compareTo(o.getAnnotationTerm());
    }
}