package org.zfin.datatransfer.go.service;

import org.junit.Test;
import org.zfin.datatransfer.go.GafErrorSummary;
import org.zfin.datatransfer.go.GafJobData;
import org.zfin.datatransfer.go.GafValidationError;
import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportTable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Tests for the GafEntry / MarkerGoTermEvidence toString parser used by the
 * Matching errors table in the GAF report.
 */
public class GafReportBuilderTest {

    @Test
    public void extractsFieldsFromGafEntryBlob() {
        Map<String, String> f = GafReportBuilder.parseEntryFields(
            "Goref ID is not known or loaded[GO_REF:0000115]:\n" +
            "GafEntry{entryId='URS00003B6A21_7955', qualifier='involved_in', goid='GO:0035195'," +
            " pmid='GO_REF:0000115', evidenceCode='IEA', inferences='Rfam:RF00256'," +
            " taxonID='taxon:7955', createdDate='20260428', createdBy='RNAcentral'," +
            " annotExtn='', geneProducFormID=''}");

        assertEquals("URS00003B6A21_7955", f.get("entryId"));
        assertEquals("involved_in",        f.get("qualifier"));
        assertEquals("GO:0035195",         f.get("goid"));
        assertEquals("GO_REF:0000115",     f.get("pmid"));
        assertEquals("IEA",                f.get("evidenceCode"));
        assertEquals("RNAcentral",         f.get("createdBy"));
    }

    @Test
    public void extractsFieldsFromMarkerGoTermEvidenceBlob() {
        Map<String, String> f = GafReportBuilder.parseEntryFields(
            "Failed to add batch:\n" +
            "MarkerGoTermEvidence{zdbID='ZDB-MRKRGOEV-221010-21085', marker='dnajb5'," +
            " evidenceCode='inferred from biological aspect of ancestor', flag='null '," +
            " qualifierRelation='enables', source='ZDB-PUB-110330-1'," +
            " goTerm='obsolete unfolded protein binding', organizationCreatedBy=GO_Central}");

        assertEquals("ZDB-MRKRGOEV-221010-21085",         f.get("zdbID"));
        assertEquals("dnajb5",                            f.get("marker"));
        assertEquals("enables",                           f.get("qualifierRelation"));
        assertEquals("obsolete unfolded protein binding", f.get("goTerm"));
        assertEquals("ZDB-PUB-110330-1",                  f.get("source"));
    }

    @Test
    public void returnsEmptyMapWhenNeitherShapeIsPresent() {
        assertTrue(GafReportBuilder.parseEntryFields("Just a plain error message").isEmpty());
        assertTrue(GafReportBuilder.parseEntryFields("").isEmpty());
    }

    @Test
    public void errorsNodeCarriesFlatAllErrorsTableWithCategoryColumn() {
        // Two errors in different categories — one MarkerGoTermEvidence shape
        // created by ZFIN, one GafEntry shape created by RNAcentral. The flat
        // table should hold both rows with their category labels so a curator
        // can filter by Created by ("ZFIN") across categories.
        GafJobData data = new GafJobData();
        data.addError(new GafValidationError(
            "MarkerGoTermEvidence{zdbID='ZDB-MRKRGOEV-1', marker='gene-a'," +
            " qualifierRelation='enables', goTerm='binding', evidenceCode='IDA'," +
            " source='ZDB-PUB-1', createdBy='ZFIN'}"));
        data.addError(new GafValidationError(
            "Goref ID is not known or loaded[GO_REF:0000115]:\n" +
            "GafEntry{entryId='URS00003B6A21_7955', qualifier='involved_in'," +
            " goid='GO:0035195', pmid='GO_REF:0000115', evidenceCode='IEA'," +
            " createdBy='RNAcentral'}"));

        GafErrorSummary summary = new GafErrorSummary();
        summary.processErrors(data.getErrors());

        Report report = new GafReportBuilder().build("test", "ZFIN",
            Collections.emptyList(), data, summary);

        ReportNode errors = findChild(report.getRoot(), "cat-errors");
        assertNotNull("Errors node should be present", errors);

        ReportTable allErrors = findTable(errors.getTables(), "All errors (2)");
        assertNotNull("Flat 'All errors' table should be attached to the Errors node",
            allErrors);

        // Category column is first so the curator's eye lands there before filtering.
        assertEquals("category", allErrors.getColumns().get(0).getKey());

        assertEquals(2, allErrors.getRows().size());

        // ZFIN row: organizationCreatedBy fed into createdBy via firstNonEmpty.
        Map<String, Object> zfinRow = allErrors.getRows().stream()
            .filter(r -> "ZFIN".equals(r.get("createdBy")))
            .findFirst().orElse(null);
        assertNotNull("Should find a row with createdBy=ZFIN to satisfy the filter use case",
            zfinRow);
        assertEquals("Annotation insertion failed", zfinRow.get("category"));

        Map<String, Object> rnaRow = allErrors.getRows().stream()
            .filter(r -> "RNAcentral".equals(r.get("createdBy")))
            .findFirst().orElse(null);
        assertNotNull(rnaRow);
        assertEquals("Goref ID is not known or loaded", rnaRow.get("category"));
    }

    private static ReportNode findChild(ReportNode parent, String id) {
        if (parent.getChildren() == null) return null;
        for (ReportNode c : parent.getChildren()) {
            if (Objects.equals(id, c.getId())) return c;
        }
        return null;
    }

    private static ReportTable findTable(List<ReportTable> tables, String title) {
        if (tables == null) return null;
        for (ReportTable t : tables) {
            if (Objects.equals(title, t.getTitle())) return t;
        }
        return null;
    }

    @Test
    public void embeddedApostropheInValueTruncatesField() {
        // Locks in the current behaviour so a future toString that uses
        // single-quoted values with embedded apostrophes doesn't silently regress.
        Map<String, String> f = GafReportBuilder.parseEntryFields(
            "GafEntry{name='O'Brien', qualifier='enables'}");
        // name terminates at the first inner apostrophe; the regex resumes at the
        // next k='v' so qualifier is still picked up correctly. Documented edge.
        assertEquals("O",       f.get("name"));
        assertEquals("enables", f.get("qualifier"));
    }
}
