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
    public void largeCategoryIsNotCappedInEitherErrorTable() {
        // "Duplicate annotation entry" can run into the hundreds of thousands of rows in a real
        // run (the file's own known ~53% duplication). Earlier this data was capped in Java at
        // 25 rows/category -- but that meant the CSV download (which exports table.rows, not
        // what's currently rendered) could only ever produce those same 25 rows, never the rest,
        // even though the UI text said "showing 25 of 51,457". Capping how much gets PAINTED is
        // report-template.html's job (INITIAL_RENDER_LIMIT/"show more"); the data itself -- and
        // so the CSV -- must carry every distinct row.
        GafJobData data = new GafJobData();
        int total = 30;
        for (int i = 0; i < total; i++) {
            data.addError(new GafValidationError(
                "A duplicate entry is being added:\n" +
                "MarkerGoTermEvidence{zdbID='ZDB-MRKRGOEV-" + i + "', marker='gene-" + i + "'," +
                " evidenceCode='IEA', source='ZDB-PUB-1', createdBy='InterPro'} from:\n" +
                "GafEntry{entryId='ZDB-GENE-" + i + "', goid='GO:0000001'}"));
        }

        GafErrorSummary summary = new GafErrorSummary();
        summary.processErrors(data.getErrors());

        Report report = new GafReportBuilder().build("test", "ZFIN",
            Collections.emptyList(), data, summary);
        ReportNode errors = findChild(report.getRoot(), "cat-errors");
        assertNotNull(errors);

        ReportTable counts = findTable(errors.getTables(), "Error counts by category");
        assertNotNull(counts);
        Map<String, Object> countRow = counts.getRows().get(0);
        assertEquals("Duplicate annotation entry", countRow.get("label"));
        assertEquals(total, countRow.get("count"));

        // The flat "All errors" table carries all 30 -- each of these is distinct (different
        // gene/zdbID), so distinct rows == raw errors here and the description says nothing
        // about collapsing (there's nothing to collapse in this particular dataset).
        ReportTable allErrors = findTable(errors.getTables(), "All errors (" + total + ")");
        assertNotNull(allErrors);
        assertEquals(30, allErrors.getRows().size());
        assertEquals(1, allErrors.getRows().get(0).get("count"));

        // The per-category drill-down carries all 30 too.
        ReportNode category = findChild(errors, "err-duplicate-annotation-entry");
        assertNotNull("Drill-down node for the category should exist", category);
        ReportTable matches = category.getTables().get(0);
        assertEquals(30, matches.getRows().size());
        assertEquals("Matching errors", matches.getTitle());
    }

    @Test
    public void identicalRowsCollapseWithACountInsteadOfRepeating() {
        // Same visible (category/createdBy/entryId/qualifier/goTerm/goTermID/evidence/source/
        // message) three times over -- differing only in zdbID, which isn't a rendered column.
        // This is exactly the file's own known ~53% row duplication (README finding 8): without
        // grouping, a curator sees the same line three times with nothing to explain why there
        // are three errors instead of one.
        GafJobData data = new GafJobData();
        for (int i = 0; i < 3; i++) {
            data.addError(new GafValidationError(
                "A duplicate entry is being added:\n" +
                "MarkerGoTermEvidence{zdbID='ZDB-MRKRGOEV-" + i + "', marker='gene-a'," +
                " evidenceCode='IEA', source='ZDB-PUB-1', createdBy='InterPro'} from:\n" +
                "GafEntry{entryId='ZDB-GENE-A', goid='GO:0000001'}"));
        }
        // One row in a different category, so it isn't accidentally swept into the same bucket.
        data.addError(new GafValidationError(
            "A duplicate entry is being added:\n" +
            "MarkerGoTermEvidence{zdbID='ZDB-MRKRGOEV-9', marker='gene-b'," +
            " evidenceCode='IEA', source='ZDB-PUB-1', createdBy='InterPro'} from:\n" +
            "GafEntry{entryId='ZDB-GENE-B', goid='GO:0000002'}"));

        GafErrorSummary summary = new GafErrorSummary();
        summary.processErrors(data.getErrors());
        Report report = new GafReportBuilder().build("test", "ZFIN",
            Collections.emptyList(), data, summary);

        ReportNode errors = findChild(report.getRoot(), "cat-errors");
        ReportTable allErrors = findTable(errors.getTables(), "All errors (4)");
        assertNotNull(allErrors);
        // 4 raw errors, but only 2 distinct rows.
        assertEquals(2, allErrors.getRows().size());

        Map<String, Object> geneARow = allErrors.getRows().stream()
            .filter(r -> "ZDB-GENE-A".equals(r.get("entryId")))
            .findFirst().orElse(null);
        assertNotNull(geneARow);
        assertEquals(3, geneARow.get("count"));

        Map<String, Object> geneBRow = allErrors.getRows().stream()
            .filter(r -> "ZDB-GENE-B".equals(r.get("entryId")))
            .findFirst().orElse(null);
        assertNotNull(geneBRow);
        assertEquals(1, geneBRow.get("count"));
    }

    @Test
    public void allDistinctRowsAreAvailableForCsvDownloadEvenWhenManyMoreThanTheOldCap() {
        // Regression for the exact bug a user hit: title said "showing 25 of 51,457 distinct",
        // but the CSV download (which exports table.rows verbatim) produced a 25-row file --
        // because the OLD Java-level cap meant those other 51,432 rows were never in the report
        // at all. 300 distinct rows here is arbitrary except that it's well past the old cap of
        // 25 and past report-template.html's own INITIAL_RENDER_LIMIT (200), so this also pins
        // down that the *data* layer (this class) is uncapped independent of what the viewer
        // chooses to paint first.
        GafJobData data = new GafJobData();
        int total = 300;
        for (int i = 0; i < total; i++) {
            data.addError(new GafValidationError(
                "A duplicate entry is being added:\n" +
                "MarkerGoTermEvidence{zdbID='ZDB-MRKRGOEV-" + i + "', marker='gene-" + i + "'," +
                " evidenceCode='IEA', source='ZDB-PUB-1', createdBy='InterPro'} from:\n" +
                "GafEntry{entryId='ZDB-GENE-" + i + "', goid='GO:0000001'}"));
        }

        GafErrorSummary summary = new GafErrorSummary();
        summary.processErrors(data.getErrors());
        Report report = new GafReportBuilder().build("test", "ZFIN",
            Collections.emptyList(), data, summary);
        ReportNode errors = findChild(report.getRoot(), "cat-errors");

        ReportTable allErrors = findTable(errors.getTables(), "All errors (" + total + ")");
        assertNotNull(allErrors);
        assertEquals("the flat table must carry every distinct row, not a capped subset",
            total, allErrors.getRows().size());

        ReportNode category = findChild(errors, "err-duplicate-annotation-entry");
        ReportTable matches = category.getTables().get(0);
        assertEquals("the per-category drill-down (and so its own CSV download) must match",
            total, matches.getRows().size());
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
