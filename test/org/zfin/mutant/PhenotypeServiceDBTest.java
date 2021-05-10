package org.zfin.mutant;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getFishRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * Tests for service class that deals with Phenotype-related logic
 */
public class PhenotypeServiceDBTest extends AbstractDatabaseTest {


    @Test
    public void retrievePhenotypeStatementsByGenoxAndTerm() {
        String genoxID = "ZDB-GENOX-050323-1";
        FishExperiment genox = RepositoryFactory.getMutantRepository().getGenotypeExperiment(genoxID);
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
        FishExperiment genox = RepositoryFactory.getMutantRepository().getGenotypeExperiment(genoxID);
        // actinotrichium
        String aoZdbID = "ZDB-TERM-100614-30";
        GenericTerm term = new GenericTerm();
        term.setZdbID(aoZdbID);

        Set<PhenotypeStatement> statements = PhenotypeService.getPhenotypeStatements(genox);
        assertNotNull(statements);
        assertTrue(statements.size() > 0);
    }

}
