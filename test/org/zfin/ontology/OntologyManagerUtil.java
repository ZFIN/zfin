package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDbUnitTest;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the OntologyManager class.
 */
public class OntologyManagerUtil extends AbstractDbUnitTest {

    private OntologyManager ontologyManager = null;
    private static final Logger LOG = RootLogger.getLogger(OntologyManagerUtil.class);

    @Before
    public void setUp() {
        super.setUp();
        // load the ontology manager with test serialized file in
        // test/WEB_INF/data-transfer/serialized-ontologies.ser
    }

    @Test
    public void createAoOntologyFile() {
        Ontology ontology = Ontology.ANATOMY;
        ontologyManager = OntologyManager.getInstance(ontology);
        ontologyManager.serializeOntology(getSerializedFile(ontology));
    }

    @Test
    public void createPatoOntologyFile() {
        Ontology ontology = Ontology.QUALITY;
        ontologyManager = OntologyManager.getInstance(ontology);
        ontologyManager.serializeOntology(getSerializedFile(ontology));
    }

    protected static File getSerializedFile(Ontology anatomy) {
        String serializedFileName = anatomy.getOntologyName();
//        serializedFileName += "-ontology.ser";
        serializedFileName += "-ontology.xml";
        return new File("test", serializedFileName);
    }


}