package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.zfin.datatransfer.report.util.ZfinReportSerializationUtil;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for LoadReportSummary class.
 */
public class LoadReportSummaryTest {

    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        objectMapper = ZfinReportSerializationUtil.getObjectMapper();
    }
    
    @Test
    public void testConstructorWithAllFields() {
        String description = "Test load summary";
        List<LoadReportSummaryTable> tables = createTestTables();
        
        LoadReportSummary summary = new LoadReportSummary(description, tables);
        
        assertEquals(description, summary.getDescription());
        assertEquals(tables, summary.getTables());
        assertEquals(1, summary.getTables().size());
    }
    
    @Test
    public void testSerializationWithEmptyTables() throws Exception {
        LoadReportSummary summary = new LoadReportSummary("Test description", new ArrayList<>());
        
        String json = objectMapper.writeValueAsString(summary);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals("Test description", jsonNode.get("description").asText());
        assertTrue(jsonNode.get("tables").isArray());
        assertEquals(0, jsonNode.get("tables").size());
    }
    
    @Test
    public void testSerializationWithTables() throws Exception {
        LoadReportSummary summary = new LoadReportSummary("Test description", createTestTables());
        
        String json = objectMapper.writeValueAsString(summary);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals("Test description", jsonNode.get("description").asText());
        assertTrue(jsonNode.get("tables").isArray());
        assertEquals(1, jsonNode.get("tables").size());
        
        JsonNode tableNode = jsonNode.get("tables").get(0);
        assertEquals("Test table", tableNode.get("description").asText());
        assertTrue(tableNode.get("rows").isArray());
        assertEquals(1, tableNode.get("rows").size());
    }
    
    @Test
    public void testDeserialization() throws Exception {
        String json = "{\"description\":\"Test description\",\"tables\":[{\"rows\":[{\"key1\":\"value1\"}]}]}";
        
        LoadReportSummary summary = objectMapper.readValue(json, LoadReportSummary.class);
        
        assertEquals("Test description", summary.getDescription());
        assertNotNull(summary.getTables());
        assertEquals(1, summary.getTables().size());
        assertEquals(1, summary.getTables().get(0).getRows().size());
    }
    
    @Test
    public void testPropertyOrder() throws Exception {
        LoadReportSummary summary = new LoadReportSummary("Test description", createTestTables());
        
        String json = objectMapper.writeValueAsString(summary);
        
        // Verify the JSON property order is: description, tables
        assertTrue(json.indexOf("\"description\"") < json.indexOf("\"tables\""));
    }
    
    @Test
    public void testEqualsAndHashCode() {
        List<LoadReportSummaryTable> tables = createTestTables();
        LoadReportSummary summary1 = new LoadReportSummary("Test description", tables);
        LoadReportSummary summary2 = new LoadReportSummary("Test description", tables);
        LoadReportSummary summary3 = new LoadReportSummary("Different description", tables);
        
        assertEquals(summary1, summary2);
        assertEquals(summary1.hashCode(), summary2.hashCode());
        assertNotEquals(summary1, summary3);
    }
    
    private List<LoadReportSummaryTable> createTestTables() {
        Map<String, Object> row = new HashMap<>();
        row.put("key1", "value1");
        row.put("key2", 123);
        
        List<Map<String, Object>> rows = Arrays.asList(row);
        LoadReportSummaryTable table = new LoadReportSummaryTable("Test table", null, rows);
        
        return Arrays.asList(table);
    }
}