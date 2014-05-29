package org.zfin.gwt.curation.ui;

import org.junit.Test;
import org.zfin.gwt.root.dto.EntityDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PhenotypeStatementDTO;
import org.zfin.gwt.root.dto.TermDTO;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void checkValidPhenotypesWithMpath() {
        PhenotypeStatementDTO phenotype = new PhenotypeStatementDTO();
        EntityDTO entity = new EntityDTO();
        TermDTO term = new TermDTO();
        term.setOntology(ANATOMY);
        term.setName("eye");
        TermDTO subterm = new TermDTO();
        subterm.setOntology(MPATH_NEOPLASM);
        subterm.setName("hemangioblastoma");
        entity.setSuperTerm(term);
        entity.setSubTerm(subterm);
        phenotype.setEntity(entity);
        TermDTO quality = new TermDTO();
        quality.setOntology(QUALITY_QUALITIES);
        quality.setName("protruding into");
        phenotype.setQuality(quality);

        EntityDTO relatedEntity = new EntityDTO();
        TermDTO relatedTerm = new TermDTO();
        relatedTerm.setOntology(ANATOMY);
        relatedTerm.setName("brain");
        relatedEntity.setSuperTerm(relatedTerm);
        phenotype.setRelatedEntity(relatedEntity);

        boolean isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        assertTrue(isValidCombination);

        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        assertTrue(isValidCombination);

        // add E2b term
        TermDTO e2b = new TermDTO();
        e2b.setName("endoderm");
        relatedEntity.setSubTerm(e2b);
        e2b.setOntology(OntologyDTO.ANATOMY);
        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        assertTrue(isValidCombination);

        e2b.setOntology(OntologyDTO.SPATIAL);
        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        assertTrue(isValidCombination);

        e2b.setOntology(OntologyDTO.GO_CC);
        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        assertTrue(isValidCombination);

        e2b.setOntology(OntologyDTO.GO_MF);
        isValidCombination = PatoPileStructureValidator.EntityRelatedEntityOntologyPair.isValidCombination(phenotype);
        assertFalse(isValidCombination);
    }
}