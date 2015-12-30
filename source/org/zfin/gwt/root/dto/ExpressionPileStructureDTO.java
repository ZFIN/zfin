package org.zfin.gwt.root.dto;

/**
 * Data Transfer Object individual structures on the pile.
 */
public class ExpressionPileStructureDTO extends AbstractPileStructureDTO implements Comparable {

    private StageDTO start;
    private StageDTO end;

    /**
     * Return the stage range in the format:
     * [start stage] - [end stage]
     * if start and end stage are the same only return the start stage name.
     *
     * @return stage range.
     */
    public String getStageRange() {
        String startStageName = start.getDisplay();
        if (start.getZdbID().equals(end.getZdbID())) {
            return startStageName;
        }
        return startStageName + " - " + end.getDisplay();
    }

    public ExpressionPileStructureDTO copy() {
        ExpressionPileStructureDTO dto = new ExpressionPileStructureDTO();
        dto.zdbID = zdbID;
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionPileStructureDTO structureDTO = (ExpressionPileStructureDTO) o;

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

    public int compareTo(Object object) {
        if (!(object instanceof ExpressionPileStructureDTO))
            return -1;
        ExpressionPileStructureDTO o = (ExpressionPileStructureDTO) object;
        if (o == null)
            return -1;
        if (o.getExpressedTerm() == null)
            return -1;
        if (getExpressedTerm() == null)
            return 1;
        return getExpressedTerm().compareTo(o.getExpressedTerm());
    }
}