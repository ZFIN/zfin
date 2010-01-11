package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.Ontology;
import org.zfin.gwt.root.dto.PostComposedPart;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
abstract public class AbstractPileStructureValidator implements StructureValidator {

    // injected
    private Map<PostComposedPart, List<Ontology>> termEntryMap;
    // calculated
    private List<String> errorMessages = new ArrayList<String>(5);


    public AbstractPileStructureValidator(Map<PostComposedPart, List<Ontology>> termEntryMap) {
        this.termEntryMap = termEntryMap;
    }

    /**
     * Check if the combination of terms is valid.
     * This method body checks for non-null superterm and valid ontology
     * Override for more elaborate validation logic. Include call to this super method.
     * The check duplicate is handled elsewhere.// Todo: Maybe we should combined them here????
     *
     * @param expressedTerm structure to be validated
     * @return true or false new structure.
     */
    public boolean isValidNewPileStructure(ExpressedTermDTO expressedTerm) {
        if (expressedTerm == null || StringUtils.isEmpty(expressedTerm.getSupertermName())) {
            errorMessages.add("No Superterm provided");
            return false;
        }
        List<Ontology> validSupertermOntologies = termEntryMap.get(PostComposedPart.SUPERTERM);
        for (Ontology ontology : validSupertermOntologies) {
            if (ontology == expressedTerm.getSupertermOntology())
                return true;
        }
        errorMessages.add("Ontology " + expressedTerm.getSupertermOntology().getDisplayName() + " not found in list" +
                "of allowed ontologies: " + validSupertermOntologies);
        return false;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public String getErrorMessage() {
        if (errorMessages == null)
            return null;

        return errorMessages.get(0);
    }
}