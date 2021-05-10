package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
abstract public class AbstractPileStructureValidator<T extends ExpressedTermDTO> implements StructureValidator<T> {

    // injected
    private Map<EntityPart, List<OntologyDTO>> termEntryMap;
    // calculated
    protected List<String> errorMessages = new ArrayList<String>(5);


    public AbstractPileStructureValidator(Map<EntityPart, List<OntologyDTO>> termEntryMap) {
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
    public boolean isValidNewPileStructure(T expressedTerm) {
        errorMessages.clear();
        TermDTO superTerm = expressedTerm.getEntity().getSuperTerm();
        if (superTerm == null || StringUtils.isEmpty(expressedTerm.getEntity().getSuperTerm().getTermName())) {
            errorMessages.add("No Superterm provided.");
            return false;
        }
        TermDTO subTerm = expressedTerm.getEntity().getSubTerm();
        if (subTerm != null && subTerm.getOntology().equals(OntologyDTO.SPATIAL) &&
                !superTerm.getOntology().equals(OntologyDTO.ANATOMY)) {
            errorMessages.add("A spatial modifier (ontology) can only be combined with a super term from the anatomical ontology.");
            return false;
        }
        List<OntologyDTO> validSupertermOntologies = termEntryMap.get(EntityPart.ENTITY_SUPERTERM);
        for (OntologyDTO ontology : validSupertermOntologies) {
            if (ontology == expressedTerm.getEntity().getSuperTerm().getOntology())
                return true;
        }
        errorMessages.add("Ontology " + expressedTerm.getEntity().getSuperTerm().getOntology().getDisplayName() + " not found in list" +
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