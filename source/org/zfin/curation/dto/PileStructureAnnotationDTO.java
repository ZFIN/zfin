package org.zfin.curation.dto;

/**
 * This cubclass is used to add a structure from the pile to one or more
 * figure annotations. It
 */
public class PileStructureAnnotationDTO extends PileStructureDTO {

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

    public PileStructureAnnotationDTO copy() {
        PileStructureAnnotationDTO dto = new PileStructureAnnotationDTO();
        dto.setZdbID(zdbID);
        return dto;
    }


    public enum Action {
        ADD,
        REMOVE
    }
}
