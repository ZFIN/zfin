package org.zfin.datatransfer.report.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for ZfinReport class.
 */
public class ZfinReportTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testConstructorWithAllFields() {
        // Given
        LoadReportMeta meta = new LoadReportMeta();
        LoadReportSummary summary = new LoadReportSummary();
        Map<String, Object> supplementalData = new HashMap<>();
        List<LoadReportAction> actions = new ArrayList<>();

        // When
        ZfinReport report = new ZfinReport(meta, summary, supplementalData, actions);

        // Then
        assertNotNull(report);
        assertEquals(meta, report.getMeta());
        assertEquals(summary, report.getSummary());
        assertEquals(supplementalData, report.getSupplementalData());
        assertEquals(actions, report.getActions());
    }

    @Test
    public void testDefaultConstructor() {
        // When
        ZfinReport report = new ZfinReport();

        // Then
        assertNotNull(report);
        assertNull(report.getMeta());
        assertNull(report.getSummary());
        assertNull(report.getSupplementalData());
        assertNull(report.getActions());
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        ZfinReport report = new ZfinReport();
        LoadReportMeta meta = new LoadReportMeta();
        LoadReportSummary summary = new LoadReportSummary();
        Map<String, Object> supplementalData = new HashMap<>();
        List<LoadReportAction> actions = new ArrayList<>();

        // When
        report.setMeta(meta);
        report.setSummary(summary);
        report.setSupplementalData(supplementalData);
        report.setActions(actions);

        // Then
        assertEquals(meta, report.getMeta());
        assertEquals(summary, report.getSummary());
        assertEquals(supplementalData, report.getSupplementalData());
        assertEquals(actions, report.getActions());
    }

    @Test
    public void testJsonSerializationBasicStructure() throws Exception {
        // Given
        ZfinReport report = new ZfinReport();
        report.setMeta(new LoadReportMeta());
        report.setSummary(new LoadReportSummary());
        report.setSupplementalData(new HashMap<>());
        report.setActions(new ArrayList<>());

        // When
        String json = objectMapper.writeValueAsString(report);

        // Then
        assertNotNull(json);
        JsonNode node = objectMapper.readTree(json);
        assertTrue("JSON should contain 'meta' field", node.has("meta"));
        assertTrue("JSON should contain 'summary' field", node.has("summary"));
        assertTrue("JSON should contain 'supplementalData' field", node.has("supplementalData"));
        assertTrue("JSON should contain 'actions' field", node.has("actions"));
    }

    @Test
    public void testJsonPropertyOrder() throws Exception {
        // Given
        ZfinReport report = new ZfinReport();
        report.setMeta(new LoadReportMeta());
        report.setSummary(new LoadReportSummary());
        report.setSupplementalData(new HashMap<>());
        report.setActions(new ArrayList<>());

        // When
        String json = objectMapper.writeValueAsString(report);

        // Then
        // Verify the fields appear in the correct order as specified by @JsonPropertyOrder
        String[] expectedOrder = {"meta", "summary", "supplementalData", "actions"};
        String jsonWithoutSpaces = json.replaceAll("\\s", "");
        
        int lastIndex = -1;
        for (String field : expectedOrder) {
            int currentIndex = jsonWithoutSpaces.indexOf("\"" + field + "\":");
            assertTrue("Field '" + field + "' should appear in JSON", currentIndex > -1);
            assertTrue("Fields should appear in correct order", currentIndex > lastIndex);
            lastIndex = currentIndex;
        }
    }

    @Test
    public void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"meta\":{},\"summary\":{},\"supplementalData\":{},\"actions\":[]}";

        // When
        ZfinReport report = objectMapper.readValue(json, ZfinReport.class);

        // Then
        assertNotNull(report);
        assertNotNull(report.getMeta());
        assertNotNull(report.getSummary());
        assertNotNull(report.getSupplementalData());
        assertNotNull(report.getActions());
    }

    @Test
    public void testToString() {
        // Given
        ZfinReport report = new ZfinReport();
        report.setMeta(new LoadReportMeta());
        report.setSummary(new LoadReportSummary());
        report.setSupplementalData(new HashMap<>());
        report.setActions(new ArrayList<>());

        // When
        String result = report.toString();

        // Then
        assertNotNull(result);
        assertTrue("toString should contain class name", result.contains("ZfinReport"));
        assertTrue("toString should contain 'meta'", result.contains("meta="));
        assertTrue("toString should contain 'summary'", result.contains("summary="));
        assertTrue("toString should contain 'supplementalData'", result.contains("supplementalData="));
        assertTrue("toString should contain 'actions'", result.contains("actions="));
    }
}