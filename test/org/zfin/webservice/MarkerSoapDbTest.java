package org.zfin.webservice;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.webservice.schema.*;

import javax.xml.bind.JAXBElement;

import static org.junit.Assert.*;

/**
 */
public class MarkerSoapDbTest extends AbstractDatabaseTest{

    private ObjectFactory objectFactory = new ObjectFactory();
    private MarkerEndpoint endpoint ;

    @Before
    public void createEndpoint() throws JDOMException{
        endpoint = new MarkerEndpoint();
    }

    @Test
    public void getGene() throws Exception{
        String testZdbID = "ZDB-GENE-980526-333" ;
        GeneRetrieveRequest geneRequest = objectFactory.createGeneRetrieveRequest();
        geneRequest.setGeneName(testZdbID);
        GeneRetrieveResponse geneResponse = endpoint.getGene(geneRequest);
        assertNotNull(geneResponse);
        assertEquals(testZdbID, geneResponse.getGene().getZdbId());
    }

    @Test
    public void getAnatomyExpressionForGene() throws Exception{
        String testZdbID = "ZDB-GENE-980526-333" ;
        JAXBElement<String> geneRequest = objectFactory.createGeneExpressionAnatomyWildTypeRequest(testZdbID);
        GeneExpressionAnatomyWildTypeResponse anatomyExpressionRetrieveResponse = endpoint.getGeneAnatomyExpression(geneRequest);
        assertNotNull(anatomyExpressionRetrieveResponse);
        assertTrue(anatomyExpressionRetrieveResponse.getAnatomy().size() > 40);
        assertTrue(anatomyExpressionRetrieveResponse.getAnatomy().size()<60);
    }

    @Test
    public void searchForGenes() throws Exception{
        GeneSearchRequest geneRequest = objectFactory.createGeneSearchRequest();
        geneRequest.setGeneName("sox1");
        GeneSearchResponse geneResponse = endpoint.getGenesForName(geneRequest);
        assertNotNull(geneResponse);
        assertTrue( geneResponse.getGenes().size() > 5);
        assertTrue( geneResponse.getGenes().size() < 100);
        for(Gene gene : geneResponse.getGenes()){
            assertTrue(gene.getAbbreviation().startsWith("sox1"));
        }
    }

}
