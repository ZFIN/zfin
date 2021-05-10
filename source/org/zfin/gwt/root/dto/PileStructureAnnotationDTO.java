package org.zfin.gwt.root.dto;

/**
 * This subclass is used to add a structure from the pile to one or more
 * figure annotations. It
 */
public class PileStructureAnnotationDTO extends ExpressionPileStructureDTO implements Comparable {

    private boolean expressed;
    private Action action;

    public boolean isExpressed() {
        return expressed;
    }

    public void setExpressed(boolean expressed) {
        this.expressed = expressed;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public PileStructureAnnotationDTO copy() {
        PileStructureAnnotationDTO dto = new PileStructureAnnotationDTO();
        dto.setZdbID(zdbID);
        return dto;
    }

    /**
     * Sort by action: first deletions then additions
     *
     * @param object
     * @return
     */
    @Override
    public int compareTo(Object object) {
        if (object == null)
            return -1;
        if (!(object instanceof PileStructureAnnotationDTO))
            return super.compareTo(object);
        PileStructureAnnotationDTO o = (PileStructureAnnotationDTO) object;
        if (getAction().equals(PileStructureAnnotationDTO.Action.REMOVE))
            return -1;
        else if (getAction().equals(o.getAction()))
            return 0;
        return 1;
    }

    public enum Action {
        ADD,
        REMOVE
    }
}
