package org.zfin.datatransfer.report.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.zfin.datatransfer.report.model.ZfinReport;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Unit tests for ZfinReportSerializationUtil.
 */
public class ZfinReportSerializationUtilTest {

    @Test
    public void testGetObjectMapper() {
        // When
        ObjectMapper mapper = ZfinReportSerializationUtil.getObjectMapper();

        // Then
        assertNotNull("ObjectMapper should not be null", mapper);
        assertSame("Should return the same instance", mapper, ZfinReportSerializationUtil.getObjectMapper());
    }

    @Test
    public void testToJson() throws JsonProcessingException {
        // Given
        ZfinReport report = new ZfinReport();
        report.setSupplementalData(new HashMap<>());
        report.setActions(new ArrayList<>());

        // When
        String json = ZfinReportSerializationUtil.toJson(report);

        // Then
        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain supplementalData", json.contains("supplementalData"));
        assertTrue("JSON should contain actions", json.contains("actions"));
    }

    @Test
    public void testToPrettyJson() throws JsonProcessingException {
        // Given
        ZfinReport report = new ZfinReport();
        report.setSupplementalData(new HashMap<>());
        report.setActions(new ArrayList<>());

        // When
        String prettyJson = ZfinReportSerializationUtil.toPrettyJson(report);

        // Then
        assertNotNull("Pretty JSON should not be null", prettyJson);
        assertTrue("Pretty JSON should contain newlines", prettyJson.contains("\n"));
        assertTrue("Pretty JSON should contain indentation", prettyJson.contains("  "));
    }

    @Test
    public void testFromJson() throws JsonProcessingException {
        // Given
        String json = "{\"supplementalData\":{},\"actions\":[]}";

        // When
        ZfinReport report = ZfinReportSerializationUtil.fromJson(json, ZfinReport.class);

        // Then
        assertNotNull("Deserialized report should not be null", report);
        assertNotNull("SupplementalData should not be null", report.getSupplementalData());
        assertNotNull("Actions should not be null", report.getActions());
    }

    @Test
    public void testNullValueExclusion() throws JsonProcessingException {
        // Given
        ZfinReport report = new ZfinReport();
        // Leave all fields as null

        // When
        String json = ZfinReportSerializationUtil.toJson(report);

        // Then
        assertNotNull("JSON should not be null", json);
        // With NON_NULL inclusion, null fields should not appear in JSON
        assertEquals("Should only contain empty braces for null fields", "{}", json);
    }

    @Test
    public void testRoundTripSerialization() throws JsonProcessingException {
        // Given
        ZfinReport originalReport = new ZfinReport();
        originalReport.setSupplementalData(new HashMap<>());
        originalReport.setActions(new ArrayList<>());

        // When
        String json = ZfinReportSerializationUtil.toJson(originalReport);
        ZfinReport deserializedReport = ZfinReportSerializationUtil.fromJson(json, ZfinReport.class);

        // Then
        assertNotNull("Original report should not be null", originalReport);
        assertNotNull("Deserialized report should not be null", deserializedReport);
        
        // Verify structure is preserved
        assertNotNull("Deserialized supplementalData should not be null", deserializedReport.getSupplementalData());
        assertNotNull("Deserialized actions should not be null", deserializedReport.getActions());
        assertEquals("SupplementalData should be empty", 0, deserializedReport.getSupplementalData().size());
        assertEquals("Actions should be empty", 0, deserializedReport.getActions().size());
    }
}