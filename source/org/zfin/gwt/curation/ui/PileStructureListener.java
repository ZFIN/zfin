package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.AbstractPileStructureDTO;

import java.util.EventListener;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface PileStructureListener<T extends AbstractPileStructureDTO> extends EventListener {

    /**
     * This method takes a new PileStructureDTO object:
     * 1) adds it to the structure pile
     * 2) re-displays the pile
     * @param pileStructure PileStructure
     */
    public void onPileStructureCreation(T pileStructure);

}
