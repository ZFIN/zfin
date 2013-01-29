package org.zfin.gwt.root.server;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.ontology.AbstractOntologyTest;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;

import static org.hibernate.validator.util.Contracts.assertNotNull;
import static org.springframework.ws.test.support.AssertionErrors.assertTrue;


/**
 *
 */
public class LookupRPCServiceImplTest {

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
    public void testAoGoAutoComplete() {
        LookupRPCService service = new LookupRPCServiceImpl();
        SuggestOracle.Request request = new SuggestOracle.Request();
        request.setQuery("liver");
        SuggestOracle.Response response = service.getOntologySuggestions(request, OntologyDTO.AOGO, false);
        assertNotNull(response);
        assertTrue("Check that there are more than 3 auto-completed values for liver*", response.getSuggestions().size() > 3);
    }

    @Test
    public void testAoGoAutoCompletePureData() {
        LookupRPCService service = new LookupRPCServiceImpl();
        SuggestOracle.Request request = new SuggestOracle.Request();
        request.setQuery("liver");
        SuggestOracle.Response response = service.getTermCompletionWithData(request, OntologyDTO.AOGO, false);
        assertNotNull(response);
        assertTrue("Check that there are more than 3 auto-completed values for liver*", response.getSuggestions().size() > 3);
    }
}


