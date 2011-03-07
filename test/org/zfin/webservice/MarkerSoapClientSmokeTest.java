package org.zfin.webservice;

import org.junit.Test;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.zfin.TestConfiguration;
import org.zfin.webservice.client.*;

import java.util.List;

import static org.junit.Assert.*;

/**
 * This tests using the generated / distributed soap client (against the wsdl)
 */
public class MarkerSoapClientSmokeTest extends WebServiceGatewaySupport {

    private org.zfin.webservice.client.Zfin service;


    public MarkerSoapClientSmokeTest() {
        TestConfiguration.configure();
        service = new ZfinService().getZfinSoap11();
    }

    @Test
    public void geneRequest() {
        GeneRetrieveRequest geneRetrieveRequest = new GeneRetrieveRequest();
        geneRetrieveRequest.setGeneName("pax6a");
        Gene pax6aGene = service.geneRetrieve(geneRetrieveRequest).getGene();
        assertEquals("pax6a", pax6aGene.getAbbreviation());
        assertEquals("ZDB-GENE-990415-200", pax6aGene.getZdbId());

        geneRetrieveRequest.setGeneName("ZDB-GENE-990415-200");
        pax6aGene = service.geneRetrieve(geneRetrieveRequest).getGene();
        assertEquals("pax6a", pax6aGene.getAbbreviation());
        assertEquals("ZDB-GENE-990415-200", pax6aGene.getZdbId());

    }

    @Test
    public void geneSearch() {
        GeneSearchRequest geneSearchRequest = new GeneSearchRequest();
        geneSearchRequest.setGeneName("pax6");
        for (Gene gene : service.geneSearch(geneSearchRequest).getGenes()) {
            assertTrue(gene.getAbbreviation().startsWith("pax6"));
        }
    }

    @Test
    public void geneExpressionSearch() {
        List<Anatomy> anatomyList = service.geneExpressionAnatomyWildType("pax6a").getAnatomy();
        assertNotNull(anatomyList);
        assertFalse(anatomyList.isEmpty());
    }


}
