package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.zfin.datatransfer.report.util.ZfinReportSerializationUtil;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for LoadReportAction class.
 */
public class LoadReportActionTest {

    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        objectMapper = ZfinReportSerializationUtil.getObjectMapper();
    }
    
    @Test
    public void testConstructorWithRequiredFields() {
        List<String> supplementalDataKeys = Arrays.asList("key1", "key2");
        
        LoadReportAction action = new LoadReportAction("action123", LoadReportAction.Type.LOAD, "GENE",
                                                      "ACC123", "ZDB-GENE-123", 
                                                      "Loaded gene data", "1000", 
                                                      supplementalDataKeys);
        
        assertEquals("action123", action.getId());
        assertEquals("LOAD", action.getType());
        assertEquals("GENE", action.getSubType());
        assertEquals("ACC123", action.getAccession());
        assertEquals("ZDB-GENE-123", action.getGeneZdbID());
        assertEquals("Loaded gene data", action.getDetails());
        assertEquals("1000", action.getLength());
        assertEquals(supplementalDataKeys, action.getSupplementalDataKeys());
        
        // Optional fields should be null
        assertNull(action.getUniprotAccessions());
        assertNull(action.getRelatedEntityID());
        assertNull(action.getDbName());
        assertNull(action.getMd5());
        assertNull(action.getRelatedEntityFields());
        assertNull(action.getRelatedActionsKeys());
        assertNull(action.getLinks());
        assertNull(action.getTags());
    }
    
    @Test
    public void testIdAsInteger() {
        List<String> supplementalDataKeys = Arrays.asList("key1");
        
        LoadReportAction action = new LoadReportAction(12345, LoadReportAction.Type.LOAD, "GENE",
                                                      "ACC123", "ZDB-GENE-123", 
                                                      "Loaded gene data", "1000", 
                                                      supplementalDataKeys);
        
        assertEquals(12345, action.getId());
    }
    
    @Test
    public void testSerializationWithRequiredFieldsOnly() throws Exception {
        List<String> supplementalDataKeys = Arrays.asList("key1", "key2");
        LoadReportAction action = new LoadReportAction("action123", LoadReportAction.Type.LOAD, "GENE",
                                                      "ACC123", "ZDB-GENE-123", 
                                                      "Loaded gene data", "1000", 
                                                      supplementalDataKeys);
        
        String json = objectMapper.writeValueAsString(action);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals("action123", jsonNode.get("id").asText());
        assertEquals("LOAD", jsonNode.get("type").asText());
        assertEquals("GENE", jsonNode.get("subType").asText());
        assertEquals("ACC123", jsonNode.get("accession").asText());
        assertEquals("ZDB-GENE-123", jsonNode.get("geneZdbID").asText());
        assertEquals("Loaded gene data", jsonNode.get("details").asText());
        assertEquals("1000", jsonNode.get("length").asText());
        assertTrue(jsonNode.get("supplementalDataKeys").isArray());
        assertEquals(2, jsonNode.get("supplementalDataKeys").size());
        
        // Optional fields should not be present
        assertFalse(jsonNode.has("uniprotAccessions"));
        assertFalse(jsonNode.has("relatedEntityID"));
        assertFalse(jsonNode.has("links"));
        assertFalse(jsonNode.has("tags"));
    }
    
    @Test
    public void testSerializationWithAllFields() throws Exception {
        LoadReportAction action = createFullAction();
        
        String json = objectMapper.writeValueAsString(action);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify all required fields
        assertEquals("action123", jsonNode.get("id").asText());
        assertEquals("LOAD", jsonNode.get("type").asText());
        
        // Verify optional fields
        assertTrue(jsonNode.has("uniprotAccessions"));
        assertEquals(2, jsonNode.get("uniprotAccessions").size());
        assertEquals("P12345", jsonNode.get("uniprotAccessions").get(0).asText());
        
        assertEquals("related123", jsonNode.get("relatedEntityID").asText());
        assertEquals("UniProt", jsonNode.get("dbName").asText());
        assertEquals("abc123def456", jsonNode.get("md5").asText());
        
        assertTrue(jsonNode.has("relatedEntityFields"));
        assertEquals("value1", jsonNode.get("relatedEntityFields").get("field1").asText());
        
        assertTrue(jsonNode.has("links"));
        assertEquals(1, jsonNode.get("links").size());
        assertEquals("UniProt Link", jsonNode.get("links").get(0).get("title").asText());
        
        assertTrue(jsonNode.has("tags"));
        assertEquals(1, jsonNode.get("tags").size());
        assertEquals("category", jsonNode.get("tags").get(0).get("name").asText());
    }
    
    @Test
    public void testDeserialization() throws Exception {
        String json = "{\"id\":\"action123\",\"type\":\"LOAD\",\"subType\":\"GENE\"," +
                     "\"accession\":\"ACC123\",\"geneZdbID\":\"ZDB-GENE-123\"," +
                     "\"details\":\"Loaded gene data\",\"length\":\"1000\"," +
                     "\"supplementalDataKeys\":[\"key1\",\"key2\"]}";
        
        LoadReportAction action = objectMapper.readValue(json, LoadReportAction.class);
        
        assertEquals("action123", action.getId());
        assertEquals("LOAD", action.getType());
        assertEquals("GENE", action.getSubType());
        assertEquals("ACC123", action.getAccession());
        assertEquals("ZDB-GENE-123", action.getGeneZdbID());
        assertEquals("Loaded gene data", action.getDetails());
        assertEquals("1000", action.getLength());
        assertEquals(2, action.getSupplementalDataKeys().size());
        assertEquals("key1", action.getSupplementalDataKeys().get(0));
    }
    
    @Test
    public void testPropertyOrder() throws Exception {
        LoadReportAction action = createFullAction();
        
        String json = objectMapper.writeValueAsString(action);
        
        // Verify some key property ordering
        assertTrue(json.indexOf("\"id\"") < json.indexOf("\"type\""));
        assertTrue(json.indexOf("\"type\"") < json.indexOf("\"subType\""));
        assertTrue(json.indexOf("\"subType\"") < json.indexOf("\"accession\""));
        assertTrue(json.indexOf("\"supplementalDataKeys\"") < json.indexOf("\"links\""));
    }
    
    @Test
    public void testSetOptionalFields() {
        LoadReportAction action = new LoadReportAction();
        
        // Set optional fields
        action.setUniprotAccessions(Arrays.asList("P12345", "Q67890"));
        action.setRelatedEntityID("related123");
        action.setDbName("UniProt");
        action.setMd5("abc123def456");
        
        Map<String, Object> entityFields = new HashMap<>();
        entityFields.put("field1", "value1");
        action.setRelatedEntityFields(entityFields);
        
        action.setRelatedActionsKeys(Arrays.asList("action456", "action789"));
        
        List<LoadReportActionLink> links = Arrays.asList(
            new LoadReportActionLink("UniProt Link", "https://uniprot.org/uniprot/P12345")
        );
        action.setLinks(links);
        
        List<LoadReportActionTag> tags = Arrays.asList(
            new LoadReportActionTag("category", "protein")
        );
        action.setTags(tags);
        
        // Verify all optional fields are set
        assertEquals(2, action.getUniprotAccessions().size());
        assertEquals("related123", action.getRelatedEntityID());
        assertEquals("UniProt", action.getDbName());
        assertEquals("abc123def456", action.getMd5());
        assertEquals(1, action.getRelatedEntityFields().size());
        assertEquals(2, action.getRelatedActionsKeys().size());
        assertEquals(1, action.getLinks().size());
        assertEquals(1, action.getTags().size());
    }
    
    private LoadReportAction createFullAction() {
        List<String> supplementalDataKeys = Arrays.asList("key1", "key2");
        LoadReportAction action = new LoadReportAction("action123", LoadReportAction.Type.LOAD, "GENE",
                                                      "ACC123", "ZDB-GENE-123", 
                                                      "Loaded gene data", "1000", 
                                                      supplementalDataKeys);
        
        // Set all optional fields
        action.setUniprotAccessions(Arrays.asList("P12345", "Q67890"));
        action.setRelatedEntityID("related123");
        action.setDbName("UniProt");
        action.setMd5("abc123def456");
        
        Map<String, Object> entityFields = new HashMap<>();
        entityFields.put("field1", "value1");
        entityFields.put("field2", 42);
        action.setRelatedEntityFields(entityFields);
        
        action.setRelatedActionsKeys(Arrays.asList("action456", "action789"));
        
        List<LoadReportActionLink> links = Arrays.asList(
            new LoadReportActionLink("UniProt Link", "https://uniprot.org/uniprot/P12345")
        );
        action.setLinks(links);
        
        List<LoadReportActionTag> tags = Arrays.asList(
            new LoadReportActionTag("category", "protein")
        );
        action.setTags(tags);
        
        return action;
    }
}