package org.zfin.curation.client;

import org.zfin.framework.presentation.dto.ExpressedTermDTO;
import org.zfin.framework.presentation.gwtutils.StringUtils;
import org.zfin.framework.presentation.client.PostComposedPart;
import org.zfin.framework.presentation.client.Ontology;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * ToDo: ADD DOCUMENTATION!
 */
abstract public class AbstractPileStructureValidator implements StructureValidator {

    // injected
    private Map<PostComposedPart, List<Ontology>> termEntryMap;
    // calculated
    private List<String> errorMessages = new ArrayList<String>();


    public AbstractPileStructureValidator(Map<PostComposedPart, List<Ontology>> termEntryMap) {
        this.termEntryMap = termEntryMap;
    }

    /**
     * Check if the combination of terms is valid.
     * This method body checks for non-null superterm and valid ontology
     * Override for more elaborate validation logic. Include call to this super method.
     * The check dublicate is handle elsewhere.// Todo: Maybe we should combined them here????
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