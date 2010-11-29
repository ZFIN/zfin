package org.zfin.marker.webservice;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.webservice.WebserviceXmlMarshaller;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 */
public class MarkerWebServiceTest extends AbstractDatabaseTest{

    private Logger logger = Logger.getLogger(MarkerWebServiceTest.class) ;

    private String BASE_URL = ZfinPropertiesEnum.NON_SECURE_HTTP.toString()+ZfinPropertiesEnum.DOMAIN_NAME.toString();
    private String FULL_JUMP_URL = BASE_URL + "/" + ZfinPropertiesEnum.WEBDRIVER_LOC + EntityPresentation.ZFIN_JUMP_URL;
    private RestTemplate restTemplate = new RestTemplate();



    @Before
    public void setUp(){
        init();
        BASE_URL = ZfinPropertiesEnum.NON_SECURE_HTTP.toString()+ZfinPropertiesEnum.DOMAIN_NAME.toString();
        restTemplate = new RestTemplate();
    }

    @Test
    public void testGetMarkerXML(){
        String url = BASE_URL + "/webservice/gene/id/ZDB-GENE-980526-333.xml" ;
        String returnValue = restTemplate.getForObject(url,String.class) ;
        logger.debug(returnValue) ;
        Marker m = new Marker() ;
        m.setZdbID("ZDB-GENE-980526-333");
        m.setAbbreviation("sox3");
        m.setName("SRY-box containing gene 3");
        try {
            Gene returnObject = (Gene) WebserviceXmlMarshaller.unmarshal(returnValue,Gene.class) ;
            assertEquals(returnObject.getZdbId(),m.getZdbID());
            assertEquals(returnObject.getAbbreviation(),m.getAbbreviation());
            assertEquals(returnObject.getName(),m.getName());
        } catch (Exception e) {
            fail("Failed to read: "+e.toString());
        }
    }

    @Test
    public void testGetMarkerJSON(){
        String url = BASE_URL + "/webservice/gene/id/ZDB-GENE-980526-333.json" ;
        Marker m = new Marker() ;
        m.setZdbID("ZDB-GENE-980526-333");
        m.setAbbreviation("sox3");
        m.setName("SRY-box containing gene 3");
        Gene returnObject = restTemplate.getForObject(url,Gene.class) ;
        assertEquals(returnObject.getZdbId(),m.getZdbID());
        assertEquals(returnObject.getAbbreviation(),m.getAbbreviation());
        assertEquals(returnObject.getName(),m.getName());
    }


    @Test
    public void testGetMarkerListXML(){
        String url = BASE_URL + "/webservice/gene/search/name/sox11.xml" ;
        String returnValue = restTemplate.getForObject(url,String.class) ;
        logger.debug(returnValue);
        try {
            GeneList returnObject = (GeneList) WebserviceXmlMarshaller.unmarshal(returnValue,GeneList.class) ;
            for(Gene gene: returnObject){
                assertTrue(gene.getAbbreviation().contains("sox11")) ;
            }
        } catch (Exception e) {
            fail("Failed to read: "+e.toString());
        }
    }

    @Test
    public void testGetMarkerListJSON(){
        String url = BASE_URL + "/webservice/gene/search/name/sox11.json" ;
        GeneList returnObject = restTemplate.getForObject(url,GeneList.class) ;
        for(Gene gene: returnObject){
            assertTrue(gene.getAbbreviation().contains("sox11")) ;
        }
    }

    @Test
    public void testGetMarkerListJSONNoSuffix(){
        String url = BASE_URL + "/webservice/gene/search/name/sox11" ;
        GeneList returnObject = restTemplate.getForObject(url,GeneList.class) ;
        for(Gene gene: returnObject){
            assertTrue(gene.getAbbreviation().contains("sox11")) ;
        }
    }

    @Test
    public void testMarshallerForMarker(){
        Marker m = new Marker() ;
        m.setZdbID("ZDB-BAC-090114-627");
        m.setName("CH211-102C2");
        m.setAbbreviation("CH211-102C2");
        String assertString ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gene><name>CH211-102C2</name><link>"
        + FULL_JUMP_URL +"ZDB-BAC-090114-627</link>"+
                "<zdb-id>ZDB-BAC-090114-627</zdb-id><abbreviation>CH211-102C2</abbreviation></gene>" ;
        Gene gene = new Gene(m) ;
        logger.debug(WebserviceXmlMarshaller.marshal(gene));
        assertEquals(assertString, WebserviceXmlMarshaller.marshal(gene)) ;

    }

    @Test
    public void testMarshallerForMarkerList(){
        Marker m = new Marker() ;
        m.setZdbID("ZDB-BAC-090114-627");
        m.setName("CH211-102C2");
        m.setAbbreviation("CH211-102C2");
        Marker m2 = new Marker() ;
        m2.setZdbID("ZDB-GENE-090114-627");
        m2.setName("sox3");
        m2.setAbbreviation("sox 3 gene");
        List<Marker> markers = new ArrayList<Marker>() ;
        markers.add(m) ;
        markers.add(m2) ;
        GeneList geneList = new GeneList(markers) ;
        String assertString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<gene-list><gene xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:java=\"http://java.sun.com\" xsi:type=\"java:org.zfin.marker.webservice.Gene\"><name>CH211-102C2</name><link>"+
                FULL_JUMP_URL +"ZDB-BAC-090114-627</link><zdb-id>ZDB-BAC-090114-627</zdb-id><abbreviation>CH211-102C2</abbreviation></gene><gene xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:java=\"http://java.sun.com\" xsi:type=\"java:org.zfin.marker.webservice.Gene\"><name>sox3</name><link>" +
                FULL_JUMP_URL +"ZDB-GENE-090114-627</link><zdb-id>ZDB-GENE-090114-627</zdb-id><abbreviation>sox 3 gene</abbreviation></gene></gene-list>";

        logger.debug(WebserviceXmlMarshaller.marshal(geneList));
        assertEquals(assertString, WebserviceXmlMarshaller.marshal(geneList)) ;
    }

    @Test
    public void testGetMarkerListJson(){
        String url = BASE_URL + "/webservice/marker/search-name/sox1" ;
        GeneList geneList = restTemplate.getForObject(url,GeneList.class) ;
        assertTrue(geneList.size()>10) ;
        for(Gene gene: geneList){
            assertTrue(gene.getAbbreviation().contains("sox"));
        }
    }
}
