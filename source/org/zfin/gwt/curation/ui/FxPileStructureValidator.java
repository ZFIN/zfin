package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.OntologyDTO;

import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FxPileStructureValidator extends AbstractPileStructureValidator<ExpressedTermDTO> {

    public FxPileStructureValidator(Map<EntityPart, List<OntologyDTO>> termEntryMap) {
        super(termEntryMap);
    }

    @Override
    public boolean isValidNewPileStructure(ExpressedTermDTO expressedTerm) {
        if (!super.isValidNewPileStructure(expressedTerm))
            return false;
        return true;
    }

}
