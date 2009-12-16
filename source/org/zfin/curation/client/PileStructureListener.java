package org.zfin.curation.client;

import org.zfin.framework.presentation.dto.PileStructureDTO;

import java.util.EventListener;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface PileStructureListener extends EventListener {

    /**
     * This method takes a new PileStructureDTO object:
     * 1) adds it to the structure pile
     * 2) re-displays the pile
     * @param pileStructure PileStructure
     */
    public void onPileStructureCreation(PileStructureDTO pileStructure);

}
