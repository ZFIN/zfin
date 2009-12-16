package org.zfin.curation.client;

import org.zfin.framework.presentation.client.Ontology;
import org.zfin.framework.presentation.client.PostComposedPart;
import org.zfin.framework.presentation.dto.ExpressedTermDTO;

import java.util.List;
import java.util.Map;

/**
 * Structure Validator for Pato Pile Construction Zone.
 */
public class PatoPileStructureValidator extends AbstractPileStructureValidator {


    public PatoPileStructureValidator(Map<PostComposedPart, List<Ontology>> termEntryMap) {
        super(termEntryMap);
    }

    public boolean isValidNewPileStructure(ExpressedTermDTO expressedTerm) {
        if (!super.isValidNewPileStructure(expressedTerm))
            return false;
        return true;
    }

}