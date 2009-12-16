package org.zfin.curation.client;

import org.zfin.framework.presentation.dto.ExpressedTermDTO;
import org.zfin.framework.presentation.client.PostComposedPart;
import org.zfin.framework.presentation.client.Ontology;

import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FxPileStructureValidator extends AbstractPileStructureValidator {

    public FxPileStructureValidator(Map<PostComposedPart, List<Ontology>> termEntryMap) {
        super(termEntryMap);
    }

    public boolean isValidNewPileStructure(ExpressedTermDTO expressedTerm) {
        if (!super.isValidNewPileStructure(expressedTerm))
            return false;
        return true;
    }

}
