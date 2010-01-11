package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.Ontology;
import org.zfin.gwt.root.dto.PostComposedPart;

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
