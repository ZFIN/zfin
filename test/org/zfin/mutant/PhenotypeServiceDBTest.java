package org.zfin.mutant;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for service class that deals with Phenotype-related logic
 */
public class PhenotypeServiceDBTest extends AbstractDatabaseTest {


    @Test
    public void retrievePhenotypeStatementsByGenoxAndTerm() {
        String genoxID = "ZDB-GENOX-050323-1";
        GenotypeExperiment genox = RepositoryFactory.getMutantRepository().getGenotypeExperiment(genoxID);
        // actinotrichium
        String aoZdbID = "ZDB-TERM-100614-30";
        GenericTerm term = new GenericTerm();
        term.setZdbID(aoZdbID);

        Set<PhenotypeStatement> statements = PhenotypeService.getPhenotypeStatements(genox);
        assertNotNull(statements);
        assertTrue(statements.size() > 0);
    }

    @Test
    public void phenotypeStatmentHasSubstructure() {
        String genoxID = "ZDB-GENOX-050323-1";
        GenotypeExperiment genox = RepositoryFactory.getMutantRepository().getGenotypeExperiment(genoxID);
        // actinotrichium
        String aoZdbID = "ZDB-TERM-100614-30";
        GenericTerm term = new GenericTerm();
        term.setZdbID(aoZdbID);

        Set<PhenotypeStatement> statements = PhenotypeService.getPhenotypeStatements(genox);
        assertNotNull(statements);
        assertTrue(statements.size() > 0);
    }

}
