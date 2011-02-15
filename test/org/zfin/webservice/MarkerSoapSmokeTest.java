package org.zfin.webservice;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.zfin.TestConfiguration;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.webservice.schema.*;

import javax.xml.bind.JAXBElement;

import static org.junit.Assert.*;

/**
 */
public class MarkerSoapSmokeTest extends WebServiceGatewaySupport{

    private String url ;

    private Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    private ObjectFactory objectFactory = new ObjectFactory();

    public MarkerSoapSmokeTest(){
        TestConfiguration.configure();

        url = ZfinPropertiesEnum.NON_SECURE_HTTP.toString() + ZfinPropertiesEnum.DOMAIN_NAME.toString() + "/webservice/definitions" ;
        marshaller = new Jaxb2Marshaller();

        getWebServiceTemplate().setMarshaller(marshaller);
        getWebServiceTemplate().setUnmarshaller(marshaller);
        marshaller.setClassesToBeBound(ObjectFactory.class
                , Anatomy.class
                , Gene.class
                , GeneRetrieveResponse.class
        );
    }


    @Test
    public void validateWsdl() throws Exception{
        GetMethod getMethod = new GetMethod(url + "/"+ MarkerEndpoint.WEBSERVICE_WSDL_URL) ;
        HttpClient client = new HttpClient();
        int status = client.executeMethod(getMethod);
        assertEquals(HttpStatus.SC_OK,status);
        String body = getMethod.getResponseBodyAsString();
        System.out.println(body) ;
    }


    @Test
    public void getGeneForId() throws Exception{
        String testZdbID = "ZDB-GENE-980526-333" ;
        GeneRetrieveRequest geneRequest = objectFactory.createGeneRetrieveRequest();
        geneRequest.setGeneName(testZdbID);
        GeneRetrieveResponse geneResponse = (GeneRetrieveResponse) getWebServiceTemplate().marshalSendAndReceive(url,geneRequest);
        assertNotNull(geneResponse);
        assertEquals(testZdbID,geneResponse.getGene().getZdbId());
    }


    @Test
    public void getAnatomyExpressionForId() throws Exception{
        String testZdbID = "ZDB-GENE-980526-333" ;
        JAXBElement<String> geneRequest = objectFactory.createGeneExpressionAnatomyWildTypeRequest(testZdbID);
        GeneExpressionAnatomyWildTypeResponse anatomyExpressionRetrieveResponse = (GeneExpressionAnatomyWildTypeResponse) getWebServiceTemplate().marshalSendAndReceive(url,geneRequest);
        assertNotNull(anatomyExpressionRetrieveResponse);
        assertTrue(anatomyExpressionRetrieveResponse.getAnatomy().size() > 40);
        assertTrue(anatomyExpressionRetrieveResponse.getAnatomy().size() < 60);
    }

    @Test
    public void getGeneForAbbreviation() throws Exception{
        String testName = "pax6a" ;
        GeneRetrieveRequest geneRequest = objectFactory.createGeneRetrieveRequest();
        geneRequest.setGeneName("pax6a");
        GeneRetrieveResponse geneResponse = (GeneRetrieveResponse) getWebServiceTemplate().marshalSendAndReceive(url,geneRequest);
        assertNotNull(geneResponse);
        assertEquals(testName, geneResponse.getGene().getAbbreviation());
        assertEquals("ZDB-GENE-990415-200",geneResponse.getGene().getZdbId());
        assertEquals(0,geneResponse.getGene().getExpressionAnatomyWildType().size());
        geneRequest.setExpressionAnatomyWildType(true);
        geneResponse = (GeneRetrieveResponse) getWebServiceTemplate().marshalSendAndReceive(url,geneRequest);
        assertNotNull(geneResponse);
        assertTrue(geneResponse.getGene().getExpressionAnatomyWildType().size()>40);
        assertTrue(geneResponse.getGene().getExpressionAnatomyWildType().size()<1000);


    }

    @Test
    public void searchForGenesViaNetwork() throws Exception{
        GeneSearchRequest geneRequest = objectFactory.createGeneSearchRequest();
        geneRequest.setGeneName("sox1");
        GeneSearchResponse geneResponse = (GeneSearchResponse) getWebServiceTemplate().marshalSendAndReceive(url,geneRequest);
        assertNotNull(geneResponse);
        assertTrue( geneResponse.getGenes().size() > 5);
        assertTrue( geneResponse.getGenes().size() < 100);
        for(Gene gene : geneResponse.getGenes()){
            assertTrue(gene.getAbbreviation().startsWith("sox1"));
            assertEquals(0,gene.getExpressionAnatomyWildType().size());
        }
        geneRequest.setExpressionAnatomyWildType(true);
        geneResponse = (GeneSearchResponse) getWebServiceTemplate().marshalSendAndReceive(url,geneRequest);
        assertNotNull(geneResponse);
        for(Gene gene : geneResponse.getGenes()){
            assertTrue(gene.getAbbreviation().startsWith("sox1"));
        }
    }
}
