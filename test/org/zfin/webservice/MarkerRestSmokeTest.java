package org.zfin.webservice;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.webservice.schema.Anatomy;
import org.zfin.webservice.schema.Gene;
import org.zfin.webservice.schema.GeneSearchResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 */
public class MarkerRestSmokeTest extends AbstractDatabaseTest{

    private Logger logger = Logger.getLogger(MarkerRestSmokeTest.class) ;

    private String BASE_URL ;
    private String FULL_JUMP_URL = ZfinPropertiesEnum.NON_SECURE_HTTP.toString()+ZfinPropertiesEnum.DOMAIN_NAME.toString() + "/" + ZfinPropertiesEnum.WEBDRIVER_LOC + EntityPresentation.ZFIN_JUMP_URL;
    private RestTemplate restTemplate = new RestTemplate();

    private WebserviceXmlMarshaller marshaller = new WebserviceXmlMarshaller();



    @Before
    public void setUp(){
        init();
        BASE_URL = ZfinPropertiesEnum.NON_SECURE_HTTP.toString()+ZfinPropertiesEnum.DOMAIN_NAME.toString() + "/webapp";
        restTemplate = new RestTemplate();
    }

    @Test
    public void testGetMarkerXML(){
        String url = BASE_URL + "/gene/id/ZDB-GENE-980526-333.xml" ;
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
        String url = BASE_URL + "/gene/id/ZDB-GENE-980526-333.json" ;
        Marker m = new Marker() ;
        m.setZdbID("ZDB-GENE-980526-333");
        m.setAbbreviation("sox3");
        m.setName("SRY-box containing gene 3");
        Gene returnObject = restTemplate.getForObject(url,Gene.class) ;
        assertEquals(returnObject.getZdbId(),m.getZdbID());
        assertEquals(returnObject.getAbbreviation(),m.getAbbreviation());
        assertEquals(returnObject.getName(),m.getName());
        assertEquals(0,returnObject.getExpressionAnatomyWildType().size());
    }


    @Test
    public void testGetMarkerJSONWithExpression(){
        String url = BASE_URL + "/gene/id/ZDB-GENE-980526-333.json?showExpressionAnatomyWildtype=true" ;
        Marker m = new Marker() ;
        m.setZdbID("ZDB-GENE-980526-333");
        m.setAbbreviation("sox3");
        m.setName("SRY-box containing gene 3");
        Gene returnObject = restTemplate.getForObject(url,Gene.class) ;
        assertEquals(returnObject.getZdbId(),m.getZdbID());
        assertEquals(returnObject.getAbbreviation(),m.getAbbreviation());
        assertEquals(returnObject.getName(),m.getName());
        assertTrue(returnObject.getExpressionAnatomyWildType().size()>0);
    }


    @Test
    public void testGetMarkerListXML(){
        String url = BASE_URL + "/gene/search/name/sox11.xml" ;
        String returnValue = restTemplate.getForObject(url,String.class) ;
        logger.debug(returnValue);
        try {
            GeneSearchResponse returnObject = (GeneSearchResponse) WebserviceXmlMarshaller.unmarshal(returnValue,GeneSearchResponse.class) ;
            for(Gene gene: returnObject.getGenes()){
                assertTrue(gene.getAbbreviation().contains("sox11")) ;
                assertEquals(0,gene.getExpressionAnatomyWildType().size());
            }
        } catch (Exception e) {
            fail("Failed to read: "+e.toString());
        }
    }

    @Test
    public void testGetMarkerListXMLWithExpression(){
        String url = BASE_URL + "/gene/search/name/sox11.xml?showExpressionAnatomyWildtype=true" ;
        String returnValue = restTemplate.getForObject(url,String.class) ;
        logger.debug(returnValue);
        try {
            GeneSearchResponse returnObject = (GeneSearchResponse) WebserviceXmlMarshaller.unmarshal(returnValue,GeneSearchResponse.class) ;
            for(Gene gene: returnObject.getGenes()){
                assertTrue(gene.getAbbreviation().contains("sox11")) ;
                assertTrue(gene.getExpressionAnatomyWildType().size()>0);
            }
        } catch (Exception e) {
            fail("Failed to read: "+e.toString());
        }
    }

    @Test
    public void testGeneAnatomyExpressionXml(){
        String url = BASE_URL + "/gene/expression/anatomy/wildtype/ZDB-GENE-980526-333.xml" ;
        String returnValue = restTemplate.getForObject(url,String.class) ;
        logger.debug(returnValue) ;
        Marker m = new Marker() ;
        m.setZdbID("ZDB-GENE-980526-333");
        try {
            List<Anatomy> anatomyList = (List<Anatomy>) WebserviceXmlMarshaller.unmarshal(returnValue,AnatomyList.class) ;
            assertNotNull(anatomyList);
            assertTrue(anatomyList.size() > 10);
            assertTrue(anatomyList.size() < 1000);
            for(Anatomy anatomy : anatomyList){
                assertNotNull(anatomy.getZdbId());
                assertNotNull(anatomy.getName());
                assertNotNull(anatomy.getLink());
            }
        } catch (Exception e) {
            fail("Failed to read: "+e.toString());
        }
    }

    @Test
    public void testGeneAnatomyExpression(){
        String url = BASE_URL + "/gene/expression/anatomy/wildtype/ZDB-GENE-980526-333" ;
        List<Anatomy> anatomyList = restTemplate.getForObject(url,AnatomyList.class) ;
        logger.debug(anatomyList) ;
        try {
            assertNotNull(anatomyList);
            assertTrue(anatomyList.size()>10);
            assertTrue(anatomyList.size()<1000);
            for(Anatomy anatomy : anatomyList){
                assertNotNull(anatomy.getZdbId());
                assertNotNull(anatomy.getName());
                assertNotNull(anatomy.getLink());
            }
        } catch (Exception e) {
            fail("Failed to read: "+e.toString());
        }
    }

    @Test
    public void testGetMarkerListJSON(){
        String url = BASE_URL + "/gene/search/name/sox11.json" ;
        List<Gene> genes = restTemplate.getForObject(url,GeneList.class) ;
        for(Gene gene: genes){
            assertTrue(gene.getAbbreviation().contains("sox11")) ;
        }
    }

    @Test
    public void testGetMarkerListJSONNoSuffix(){
        String url = BASE_URL + "/gene/search/name/sox11" ;
        List<Gene> genes = restTemplate.getForObject(url,GeneList.class) ;
        for(Gene gene: genes ){
            assertTrue(gene.getAbbreviation().contains("sox11")) ;
            assertEquals(0,gene.getExpressionAnatomyWildType().size());
        }
    }


    @Test
    public void testGetMarkerListJSONNoSuffixWithExpression(){
        String url = BASE_URL + "/gene/search/name/sox11?showExpressionAnatomyWildtype=true" ;
        List<Gene> genes = restTemplate.getForObject(url,GeneList.class) ;
        for(Gene gene: genes ){
            assertTrue(gene.getAbbreviation().contains("sox11")) ;
            assertTrue(gene.getExpressionAnatomyWildType().size()>0);
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
        Gene gene = SchemaMapper.createGeneFromMarker(m) ;
        logger.debug(marshaller.marshal(gene));
        assertEquals(assertString, marshaller.marshal(gene)) ;

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
        GeneSearchResponse geneList = SchemaMapper.convertMarkersToGeneWebObjects(new GeneSearchResponse(),markers) ;
        String assertString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<gene-search-response><genes xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:java=\"http://java.sun.com\" xsi:type=\"java:org.zfin.webservice.schema.Gene\"><name>CH211-102C2</name><link>"+
                FULL_JUMP_URL +"ZDB-BAC-090114-627</link><zdb-id>ZDB-BAC-090114-627</zdb-id><abbreviation>CH211-102C2</abbreviation></genes><genes xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:java=\"http://java.sun.com\" xsi:type=\"java:org.zfin.webservice.schema.Gene\"><name>sox3</name><link>" +
                FULL_JUMP_URL +"ZDB-GENE-090114-627</link><zdb-id>ZDB-GENE-090114-627</zdb-id><abbreviation>sox 3 gene</abbreviation></genes></gene-search-response>";

        logger.debug(marshaller.marshal(geneList));
        assertEquals(assertString, marshaller.marshal(geneList)) ;
    }

    @Test
    public void testGetMarkerListJson(){
        String url = BASE_URL + "/gene/search/name/sox1" ;
        List<Gene> geneList = restTemplate.getForObject(url, GeneList.class) ;
        assertTrue(geneList.size()>10) ;
        for(Gene gene: geneList){
            assertTrue(gene.getAbbreviation().contains("sox"));
            assertEquals(0,gene.getExpressionAnatomyWildType().size());
        }
    }

    @Test
    public void testGetMarkerListJsonWithExpression(){
        String url = BASE_URL + "/gene/search/name/sox1?showExpressionAnatomyWildtype=true" ;
        List<Gene> geneList = restTemplate.getForObject(url, GeneList.class) ;
        assertTrue(geneList.size()>10) ;
        for(Gene gene: geneList){
            assertTrue(gene.getAbbreviation().contains("sox1"));

            // not all of the genes will have expression, but at least this one should
            if(gene.getAbbreviation().equals("sox1a")){
                assertTrue(gene.getExpressionAnatomyWildType().size()>0);
            }
        }
    }
}
