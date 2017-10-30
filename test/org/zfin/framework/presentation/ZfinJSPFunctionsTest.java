package org.zfin.framework.presentation;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.Term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

public class ZfinJSPFunctionsTest extends AbstractDatabaseTest {

    @Test
    public void getSubstructureName() {
        // intestinal epithelium cell | detached from | intestinal epithelium
        PhenotypeStatement statement = getMutantRepository().getPhenotypeStatementById(3540L);
        // gut
        Term parentTerm = getOntologyRepository().getTermByOboID("ZFA:0000112");
        String substructureName = ZfinJSPFunctions.getSubstructure(statement, parentTerm);
        assertNotNull(substructureName);
        assertEquals("intestinal epithelium", substructureName);
    }
}
