package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.ExpressedTermDTO;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface StructureValidator {

    public boolean isValidNewPileStructure(ExpressedTermDTO expressedTerm);

    public List<String> getErrorMessages();

    public String getErrorMessage();

}
