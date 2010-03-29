package org.zfin.ontology;

import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.properties.ZfinProperties;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test the OntologyManager class.
 */
public class OntologyManagerTest {

    private OntologyManager manager;

    @Before
    public void setUp() throws Exception {
        TestConfiguration.configure();
        // load the ontology manager with test serialized file in
        // test/WEB_INF/data-transfer/serialized-ontologies.ser
        ZfinProperties.setWebRootDirectory("test");
        manager = OntologyManager.getInstance(OntologyManager.LoadingMode.SERIALIZED_FILE);
    }

    @Test
    public void getMatchingTerms() {
        String query = "mel";
        long startTime = System.currentTimeMillis();
        List<MatchingTerm> qualityList = manager.getMatchingTerms(Ontology.ANATOMY, query);
        long endTime = System.currentTimeMillis();
        long timeToSearch = endTime - startTime;
        System.out.println("Search Duration: " + timeToSearch);
        //OntologyManager.serializeOntology();
        assertNotNull(qualityList);
        assertEquals(21, qualityList.size());

    }


}
