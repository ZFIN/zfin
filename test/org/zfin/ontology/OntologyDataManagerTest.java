package org.zfin.ontology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.repository.RepositoryFactory;

import static org.junit.Assert.assertTrue;

public class OntologyDataManagerTest extends AbstractDatabaseTest {

    private String oldIoTempDir;

    @Before
    public void before() throws Exception {
        oldIoTempDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", AbstractOntologyTest.testTempDirectory);
        OntologyManager ontologyManager = OntologyManager.getEmptyInstance();
        ontologyManager.deserializeOntology(Ontology.ANATOMY);
        ontologyManager.deserializeOntology(Ontology.GO_CC);
        ontologyManager.deserializeOntology(Ontology.GO_MF);
        ontologyManager.deserializeOntology(Ontology.GO_BP);
    }

    @After
    public void after() {
        System.setProperty("java.io.tmpdir", oldIoTempDir);
    }

    @Test
    public void checkDataTerm() {
        // brain
        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID("ZFA:0000008");
        TermDTO termDto = DTOConversionService.convertToTermDTO(term);
        assertTrue("No expression data found for 'brain'", OntologyDataManager.getInstance().hasExpressionData(termDto));
    }

}
