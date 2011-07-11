package org.zfin.gwt.curation.ui;

import org.junit.Test;
import org.zfin.gwt.root.dto.EntityDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PhenotypeStatementDTO;
import org.zfin.gwt.root.dto.TermDTO;

import static junit.framework.Assert.assertTrue;
import static org.zfin.gwt.root.dto.OntologyDTO.*;

public class PatoPileStructureValidatorTest {

    @Test
    public void checkValidPhenotypesWithRelatedness() {
        PhenotypeStatementDTO phenotype = new PhenotypeStatementDTO();
        EntityDTO entity = new EntityDTO();
        TermDTO term = new TermDTO();
        term.setOntology(OntologyDTO.GO_MF);
        entity.setSuperTerm(term);
        phenotype.setEntity(entity);
        boolean isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        // Just MF is not a valid combination
        assertTrue(!isValidCombination);

        term.setOntology(ANATOMY);
        EntityDTO relatedEntity = new EntityDTO();
        TermDTO relatedTerm = new TermDTO();
        relatedTerm.setOntology(ANATOMY);
        relatedEntity.setSuperTerm(relatedTerm);
        phenotype.setRelatedEntity(relatedEntity);
        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        // AO : AO is a valid combination
        assertTrue(isValidCombination);

        TermDTO relatedTermSub = new TermDTO();
        relatedTermSub.setOntology(GO_MF);
        relatedEntity.setSubTerm(relatedTermSub);
        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        // AO : AO:MF is not a valid combination
        assertTrue(!isValidCombination);

        // BP : MF valid
        term.setOntology(GO_BP);
        relatedTerm.setOntology(GO_MF);
        relatedTermSub.setOntology(null);
        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        assertTrue(isValidCombination);

    }

}