package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.gwt.root.dto.PostComposedPart;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.List;
import java.util.Map;

/**
 * Structure Validator for Pato Pile Construction Zone.
 */
public class PatoPileStructureValidator extends AbstractPileStructureValidator<PhenotypeTermDTO> {


    public PatoPileStructureValidator(Map<PostComposedPart, List<OntologyDTO>> termEntryMap) {
        super(termEntryMap);
    }

    /**
     * If a subterm is provided check if it matches a valid quality term:
     * AO and GO-CC require 'Quality - Objects'
     * GO-MF and GO-BP require 'Quality - Processes'
     * If no subterm is provided check the ontology of the super term with the same
     * matching quality term.
     *
     * @param phenotypeTerm structure to be validated
     * @return true or false
     */
    @Override
    public boolean isValidNewPileStructure(PhenotypeTermDTO phenotypeTerm) {
        if (!super.isValidNewPileStructure(phenotypeTerm))
            return false;
        OntologyDTO superTerm = phenotypeTerm.getSuperterm().getOntology();
        OntologyDTO quality = phenotypeTerm.getQuality().getOntology();
        TermDTO subterm = phenotypeTerm.getSubterm();
        if (subterm != null) {
            if (subterm.getOntology().getAssociatedQualityOntology() != quality && quality != OntologyDTO.QUALITY) {
                errorMessages.add("A sub term from the [" + subterm.getOntology().getDisplayName() +
                        "] ontology cannot be composed with a term from the " +
                        " [" + quality.getDisplayName() + "] ontology. Please use [" +
                        subterm.getOntology().getAssociatedQualityOntology().getDisplayName() + "]");
                return false;
            }
        } else if (superTerm.getAssociatedQualityOntology() != quality && quality != OntologyDTO.QUALITY) {
            errorMessages.add("A super term from the [" + superTerm.getDisplayName() +
                    "] ontology cannot be composed with a term from the " +
                    " [" + quality.getDisplayName() + "] ontology. Please use [" +
                    superTerm.getAssociatedQualityOntology().getDisplayName() + "]");
            return false;
        }
        return true;
    }

}