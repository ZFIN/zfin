package org.zfin.curation.client;

import org.zfin.framework.presentation.dto.ExpressedTermDTO;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface StructureValidator {

    public boolean isValidNewPileStructure(ExpressedTermDTO expressedTerm);

    public List<String> getErrorMessages();

    public String getErrorMessage();

}
