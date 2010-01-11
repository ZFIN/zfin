package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * Data Transfer Object individual structures on the pile.
 */
public class PileStructureDTO implements IsSerializable, Comparable<PileStructureDTO> {

    private ExpressedTermDTO expressedTerm;
    protected String zdbID;
    private StageDTO start;
    private StageDTO end;
    private String creator;
    private Date date;

    public ExpressedTermDTO getExpressedTerm() {
        return expressedTerm;
    }

    public void setExpressedTerm(ExpressedTermDTO expressedTerm) {
        this.expressedTerm = expressedTerm;
    }

    /**
     * Return the stage range in the format:
     * [start stage] - [end stage]
     * if start and end stage are the same only return the start stage name.
     *
     * @return stage range.
     */
    public String getStageRange() {
        if (start.getZdbID().equals(end.getZdbID()))
            return start.getName();
        return start.getName() + " - " + end.getName();
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

    public PileStructureDTO copy() {
        PileStructureDTO dto = new PileStructureDTO();
        dto.zdbID = zdbID;
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PileStructureDTO structureDTO = (PileStructureDTO) o;

        if (zdbID != null ? !zdbID.equals(structureDTO.zdbID) : structureDTO.zdbID != null) return false;

        return true;
    }

    public StageDTO getStart() {
        return start;
    }

    public void setStart(StageDTO start) {
        this.start = start;
    }

    public StageDTO getEnd() {
        return end;
    }

    public void setEnd(StageDTO end) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        int result;
        result = 31 + (zdbID != null ? zdbID.hashCode() : 0);
        return result;
    }

    public int compareTo(PileStructureDTO o) {
        if (o == null)
            return -1;
        if (o.getExpressedTerm() == null)
            return -1;
        if (expressedTerm == null)
            return 1;
        return expressedTerm.compareTo(o.getExpressedTerm());
    }
}