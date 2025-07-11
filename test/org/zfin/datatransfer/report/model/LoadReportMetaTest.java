package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.zfin.datatransfer.report.util.ZfinReportSerializationUtil;

import static org.junit.Assert.*;

/**
 * Unit tests for LoadReportMeta class.
 */
public class LoadReportMetaTest {

    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        objectMapper = ZfinReportSerializationUtil.getObjectMapper();
    }
    
    @Test
    public void testConstructorWithRequiredFieldsOnly() {
        String title = "Test Report";
        Long creationDate = System.currentTimeMillis();
        
        LoadReportMeta meta = new LoadReportMeta(title, creationDate);
        
        assertEquals(title, meta.getTitle());
        assertNull(meta.getReleaseID());
        assertEquals(creationDate, meta.getCreationDate());
    }
    
    @Test
    public void testConstructorWithAllFields() {
        String title = "Test Report";
        String releaseID = "2024.1";
        Long creationDate = System.currentTimeMillis();
        
        LoadReportMeta meta = new LoadReportMeta(title, releaseID, creationDate);
        
        assertEquals(title, meta.getTitle());
        assertEquals(releaseID, meta.getReleaseID());
        assertEquals(creationDate, meta.getCreationDate());
    }
    
    @Test
    public void testSerializationWithRequiredFieldsOnly() throws Exception {
        LoadReportMeta meta = new LoadReportMeta("Test Report", 1640995200000L);
        
        String json = objectMapper.writeValueAsString(meta);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals("Test Report", jsonNode.get("title").asText());
        assertEquals(1640995200000L, jsonNode.get("creationDate").asLong());
        assertFalse(jsonNode.has("releaseID"));
    }
    
    @Test
    public void testSerializationWithAllFields() throws Exception {
        LoadReportMeta meta = new LoadReportMeta("Test Report", "2024.1", 1640995200000L);
        
        String json = objectMapper.writeValueAsString(meta);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals("Test Report", jsonNode.get("title").asText());
        assertEquals("2024.1", jsonNode.get("releaseID").asText());
        assertEquals(1640995200000L, jsonNode.get("creationDate").asLong());
    }
    
    @Test
    public void testDeserialization() throws Exception {
        String json = "{\"title\":\"Test Report\",\"releaseID\":\"2024.1\",\"creationDate\":1640995200000}";
        
        LoadReportMeta meta = objectMapper.readValue(json, LoadReportMeta.class);
        
        assertEquals("Test Report", meta.getTitle());
        assertEquals("2024.1", meta.getReleaseID());
        assertEquals(Long.valueOf(1640995200000L), meta.getCreationDate());
    }
    
    @Test
    public void testPropertyOrder() throws Exception {
        LoadReportMeta meta = new LoadReportMeta("Test Report", "2024.1", 1640995200000L);
        
        String json = objectMapper.writeValueAsString(meta);
        
        // Verify the JSON property order is: title, releaseID, creationDate
        assertTrue(json.indexOf("\"title\"") < json.indexOf("\"releaseID\""));
        assertTrue(json.indexOf("\"releaseID\"") < json.indexOf("\"creationDate\""));
    }
    
    @Test
    public void testEqualsAndHashCode() {
        LoadReportMeta meta1 = new LoadReportMeta("Test Report", "2024.1", 1640995200000L);
        LoadReportMeta meta2 = new LoadReportMeta("Test Report", "2024.1", 1640995200000L);
        LoadReportMeta meta3 = new LoadReportMeta("Different Report", "2024.1", 1640995200000L);
        
        assertEquals(meta1, meta2);
        assertEquals(meta1.hashCode(), meta2.hashCode());
        assertNotEquals(meta1, meta3);
    }
}