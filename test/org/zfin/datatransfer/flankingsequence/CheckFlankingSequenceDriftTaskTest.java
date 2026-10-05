package org.zfin.datatransfer.flankingsequence;

import org.junit.Test;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.report.InlineDiff;
import org.zfin.report.Report;
import org.zfin.report.ReportWriter;
import org.zfin.sequence.gff.AssemblyEnum;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Pure-function tests of the report build/render -- no DB, no FASTA files needed, just
 * synthetic rows. Confirms the character-level diff highlighting (ZFIN-10486 follow-up: a
 * 1-2bp difference inside a 500bp sequence was invisible in a plain before/after dump)
 * reaches the rendered HTML, and that long sequences get windowed rather than dumped whole.
 */
public class CheckFlankingSequenceDriftTaskTest {

    private static final String JOB = "Check-Flanking-Sequence-Drift_w";

    private static CheckFlankingSequenceDriftTask.DriftRow driftRow(
            String storedFive, String expectedFive, String storedVar, String expectedVar) {
        return new CheckFlankingSequenceDriftTask.DriftRow(
            "ZDB-ALT-260408-13", "dock10^uab216", FeatureTypeEnum.INDEL,
            AssemblyEnum.GRCZ12TU, "GRCz12tu", "15", 2028870, 2028873,
            storedFive, "GACAGTAGACTTCAC", storedVar,
            expectedFive, "GACAGTAGACTTCAC", expectedVar);
    }

    private static CheckFlankingSequenceDriftTask.ScanResult scanOf(
            List<CheckFlankingSequenceDriftTask.DriftRow> drift,
            List<CheckFlankingSequenceDriftTask.AmbiguousLocationRow> ambiguous,
            List<CheckFlankingSequenceDriftTask.SubmittedRow> submitted) {
        return new CheckFlankingSequenceDriftTask.ScanResult(
            drift, ambiguous, submitted, drift.size() + ambiguous.size() + submitted.size(), 0, 0, 0);
    }

    @Test
    public void diffMarkupWrapsTheChangedCharactersAndLeavesIdenticalOnesAlone() throws Exception {
        // 5' flank differs by the trailing "GG"; the 3' flank is identical on both sides.
        CheckFlankingSequenceDriftTask.DriftRow row =
            driftRow("TTCATCAGCTCCCTCTGG", "TTCATCAGCTCCCTCT", "CTGG/CCCT", "GG/CCCT");

        Report report = new CheckFlankingSequenceDriftTask(JOB).buildReport(scanOf(List.of(row), List.of(), List.of()));
        String json = inflateReportData(new ReportWriter().render(report));

        assertTrue(json.contains("ZDB-ALT-260408-13"));
        // The differing 5' flank is marked up...
        assertTrue("changed 5' flank highlighted", json.contains("<u>GG</u>"));
        // ...and the identical 3' flank is carried through verbatim, with no markup injected.
        assertTrue("identical 3' flank left alone", json.contains("GACAGTAGACTTCAC"));
        assertFalse("no markup inside the identical 3' flank",
            json.contains("GACAGTAGACTT<u>"));
    }

    @Test
    public void longWhollyDifferentSequencesAreWindowedNotDumpedWhole() throws Exception {
        // Two 500bp sequences that share no prefix: the cross-assembly case. Without
        // windowing this would emit ~1000 characters per cell, nearly all of it marked up.
        String stored = "A".repeat(500);
        String expected = "C".repeat(500);
        CheckFlankingSequenceDriftTask.DriftRow row = driftRow(stored, expected, "A/C", "A/C");

        Report report = new CheckFlankingSequenceDriftTask(JOB).buildReport(scanOf(List.of(row), List.of(), List.of()));
        String json = inflateReportData(new ReportWriter().render(report));

        assertFalse("the full 500bp run must not reach the report", json.contains("A".repeat(200)));
        assertTrue("windowed cell is marked as truncated", json.contains("…"));
    }

    @Test
    public void everySectionIsItsOwnNavNodeEvenWhenEmpty() throws Exception {
        CheckFlankingSequenceDriftTask.ScanResult scan = scanOf(
            List.of(driftRow("AAAG", "AAAC", "A/C", "A/C")),
            List.of(new CheckFlankingSequenceDriftTask.AmbiguousLocationRow(
                "ZDB-ALT-130411-4363", "sa15827", "POINT_MUTATION", "GRCz12tu", 2,
                "5:25541390-25541390  |  25:40385783-40385783")),
            List.of(new CheckFlankingSequenceDriftTask.SubmittedRow(
                "ZDB-ALT-130411-9", "sa45022", "POINT_MUTATION", "GRCz12tu", 50)));

        Report report = new CheckFlankingSequenceDriftTask(JOB).buildReport(scan);

        List<String> sections = report.getRoot().getChildren().stream().map(n -> n.getTitle()).toList();
        assertEquals(List.of("Drift", "Ambiguous location", "Submitter-provided"), sections);
        report.getRoot().getChildren().forEach(n ->
            assertEquals("each section carries its own count", 1L, n.getCount().longValue()));

        String json = inflateReportData(new ReportWriter().render(report));
        // The ambiguous section shows both conflicting positions, including the chromosome
        // disagreement that makes them unfixable by recalculation.
        assertTrue(json.contains("25:40385783-40385783"));
        // The submitter section surfaces the 50bp offset that marks these as submitted data.
        assertTrue(json.contains("Submitter-provided"));
    }

    @Test
    public void driftIsOrderedByVariationChangeThenAbbreviation() {
        CheckFlankingSequenceDriftTask.DriftRow flankOnlyZeta = named("zeta", "GG/CC", "GG/CC");
        CheckFlankingSequenceDriftTask.DriftRow flankOnlyAlpha = named("alpha", "GG/CC", "GG/CC");
        CheckFlankingSequenceDriftTask.DriftRow variationDelta = named("delta", "CTGG/CCCT", "GG/CCCT");
        CheckFlankingSequenceDriftTask.DriftRow variationBeta = named("beta", "CTGG/CCCT", "GG/CCCT");

        List<CheckFlankingSequenceDriftTask.DriftRow> rows =
            new ArrayList<>(List.of(flankOnlyZeta, variationDelta, flankOnlyAlpha, variationBeta));
        rows.sort(CheckFlankingSequenceDriftTask.DRIFT_ORDER);

        // Both variation-changed rows first (alphabetical within), then the flank-only ones.
        assertEquals(List.of("beta", "delta", "alpha", "zeta"),
            rows.stream().map(CheckFlankingSequenceDriftTask.DriftRow::featureAbbrev).toList());
    }

    private static CheckFlankingSequenceDriftTask.DriftRow named(
            String abbrev, String storedVariation, String expectedVariation) {
        return new CheckFlankingSequenceDriftTask.DriftRow(
            "ZDB-ALT-" + abbrev, abbrev, FeatureTypeEnum.INDEL,
            AssemblyEnum.GRCZ12TU, "GRCz12tu", "15", 1, 2,
            "AAAG", "TTT", storedVariation,
            "AAAC", "TTT", expectedVariation);
    }

    @Test
    public void variationChangedIsWhatDrivesTheOrdering() {
        // Drives the sort in scan(): rows whose red-on-the-page ref/var notation changed lead.
        assertTrue(CheckFlankingSequenceDriftTask.variationChanged(
            driftRow("AAAG", "AAAC", "CTGG/CCCT", "GG/CCCT")));
        // A flank-only drift leaves the variation identical, so it sorts after.
        assertFalse(CheckFlankingSequenceDriftTask.variationChanged(
            driftRow("AAAG", "AAAC", "GG/CCCT", "GG/CCCT")));
        // Not-comparable variations are carried through as equal, so they sort after too.
        assertFalse(CheckFlankingSequenceDriftTask.variationChanged(
            driftRow("AAAG", "AAAC", "", "")));
    }

    @Test
    public void expectedVariationKeepsNotationsThatCarrySequence() {
        CheckFlankingSequenceDriftTask task = new CheckFlankingSequenceDriftTask(JOB);
        // A deletion has no variant sequence and an insertion no reference, by definition --
        // the empty side is normal and these must still be compared.
        assertEquals("ACGT/-", task.expectedVariation(FeatureTypeEnum.DELETION, "ACGT", null));
        assertEquals("-/ACGT", task.expectedVariation(FeatureTypeEnum.INSERTION, null, "ACGT"));
        assertEquals("AC/GT", task.expectedVariation(FeatureTypeEnum.POINT_MUTATION, "AC", "GT"));
        assertEquals("ACGT/", task.expectedVariation(FeatureTypeEnum.INDEL, "ACGT", ""));
    }

    @Test
    public void expectedVariationNullWhenTheNotationWouldCarryNoSequenceAtAll() {
        CheckFlankingSequenceDriftTask task = new CheckFlankingSequenceDriftTask(JOB);
        // A point mutation whose detail has neither sequence would collapse to a bare "/",
        // which is not drift worth reporting (nor worth writing to the feature page) -- it's
        // a missing-mutation-detail problem, covered by its own check.
        assertNull(task.expectedVariation(FeatureTypeEnum.POINT_MUTATION, "", ""));
        assertNull(task.expectedVariation(FeatureTypeEnum.POINT_MUTATION, null, null));
        assertNull(task.expectedVariation(FeatureTypeEnum.MNV, "", ""));
        // Same for the structural-characters-only forms of the other types.
        assertNull(task.expectedVariation(FeatureTypeEnum.INDEL, "", ""));
        assertNull(task.expectedVariation(FeatureTypeEnum.DELETION, "", null));
        assertNull(task.expectedVariation(FeatureTypeEnum.INSERTION, null, ""));
        // Types that never get a flanking sequence at all.
        assertNull(task.expectedVariation(FeatureTypeEnum.TRANSGENIC_INSERTION, "AC", "GT"));
    }

    @Test
    public void firstDifferenceFindsWhereTwoSequencesDiverge() {
        assertEquals(-1, InlineDiff.firstDifference("ACGT", "ACGT"));
        assertEquals(2, InlineDiff.firstDifference("ACGT", "ACTT"));
        // A pure truncation diverges at the end of the shorter string.
        assertEquals(4, InlineDiff.firstDifference("ACGT", "ACGTGG"));
        assertEquals(0, InlineDiff.firstDifference("", "A"));
    }

    @Test
    public void excerptWindowsAroundTheDifferenceAndMarksTruncation() {
        String sequence = "A".repeat(200) + "G" + "A".repeat(200);
        String windowed = CheckFlankingSequenceDriftTask.excerpt(sequence, 200);

        assertTrue("keeps the differing base", windowed.contains("G"));
        assertTrue("much shorter than the original", windowed.length() < sequence.length() / 2);
        // An elided start carries the 1-based position of the first base shown, so the
        // fragment can be located within the flank; the elided tail is a bare ellipsis.
        assertTrue("start elision is positioned", windowed.startsWith("…[141]"));
        assertTrue("tail elision marked", windowed.endsWith("…"));
        // A sequence short enough to show whole is returned untouched.
        assertEquals("ACGT", CheckFlankingSequenceDriftTask.excerpt("ACGT", 2));
    }

    @Test
    public void excerptOmitsThePositionWhenNothingIsElidedFromTheStart() {
        // Difference inside the first window: there is no leading elision, so no "[n]".
        String sequence = "A".repeat(300);
        String windowed = CheckFlankingSequenceDriftTask.excerpt(sequence, 10);
        assertFalse("no leading elision marker", windowed.startsWith("…"));
        assertTrue("tail still elided", windowed.endsWith("…"));
    }

    /** Extract and gunzip the {@code window.REPORT_DATA_GZ} payload from rendered report HTML. */
    static String inflateReportData(String html) throws Exception {
        Matcher matcher = Pattern.compile("window\\.REPORT_DATA_GZ = \"([^\"]*)\"").matcher(html);
        assertTrue("REPORT_DATA_GZ payload present", matcher.find());
        byte[] gz = java.util.Base64.getDecoder().decode(matcher.group(1));
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
