package org.zfin.orthology.jobs;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Pure-function tests for the {@link OrthoUpdateReportJob} parsers — the only
 * fragile part of the wrapper. Exercises the block-format inconsistency parser
 * and the delimited-file reader against the exact shapes the Perl pipeline
 * emits (see {@code server_apps/data_transfer/ORTHO/reportOrthoNameChanges.pl}).
 */
public class OrthoUpdateReportJobTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File write(String name, String content) throws Exception {
        File f = new File(tmp.getRoot(), name);
        Files.writeString(f.toPath(), content, StandardCharsets.UTF_8);
        return f;
    }

    // ---------------- delimited reader ----------------

    @Test
    public void readDelimitedSplitsPipesAndKeepsTrailingEmptyField() throws Exception {
        write("orthoNamesUpdateList.txt",
            "ZDB-GENE-001|myga|Human|old name|new name|\n" +
            "ZDB-GENE-002|mygb|Mouse|foo|bar|\n");

        List<String[]> rows = OrthoUpdateReportJob.readDelimited(
            tmp.getRoot(), "orthoNamesUpdateList.txt", "\\|");

        assertEquals(2, rows.size());
        // -1 split limit keeps the trailing empty field after the last pipe.
        assertEquals(6, rows.get(0).length);
        assertEquals("ZDB-GENE-001", rows.get(0)[0]);
        assertEquals("myga",         rows.get(0)[1]);
        assertEquals("new name",     rows.get(0)[4]);
    }

    @Test
    public void readDelimitedSkipsBlankLinesAndMissingFileYieldsEmpty() throws Exception {
        write("ortho_statistics.txt", "a\tb\tc\n\n\nd\te\tf\n");
        List<String[]> rows = OrthoUpdateReportJob.readDelimited(
            tmp.getRoot(), "ortho_statistics.txt", "\t");
        assertEquals(2, rows.size());

        assertTrue(OrthoUpdateReportJob.readDelimited(
            tmp.getRoot(), "does-not-exist.txt", "\t").isEmpty());
    }

    // ---------------- block parser ----------------

    @Test
    public void parsesWeeklyBlockWithHumanAndMouse() throws Exception {
        write("orthoInconsistentZebrafishGeneNamesReport.txt",
            "ZDB-GENE-123     myga\n" +
            "gene name (z): my gene a\n" +
            "gene name (h): MY GENE ALPHA\n" +
            "gene symbol (h): MYGA\n" +
            "gene name (m): my gene m\n" +
            "gene symbol (m): Myga\n" +
            "\n");

        List<OrthoUpdateReportJob.IncRow> rows = OrthoUpdateReportJob.parseInconsistencyBlocks(
            tmp.getRoot(), "orthoInconsistentZebrafishGeneNamesReport.txt");

        assertEquals(1, rows.size());
        OrthoUpdateReportJob.IncRow r = rows.get(0);
        assertNull(r.firstSeen());
        assertEquals("ZDB-GENE-123", r.zdbId());
        assertEquals("myga",         r.symbol());
        assertEquals("my gene a",    r.zName());
        assertEquals("MY GENE ALPHA", r.hName());
        assertEquals("my gene m",    r.mName());
    }

    @Test
    public void parsesPersistentBlocksWithFirstSeenAndSkipsComments() throws Exception {
        write("orthoInconsistentZebrafishGeneNamesReport_persistent.txt",
            "# Persistent ZFIN/ortholog name inconsistencies\n" +
            "# Generated 2026-06-09; 2 inconsistencies; newest first.\n" +
            "\n" +
            "first seen: 2026-06-09\n" +
            "ZDB-GENE-aaa     genea\n" +
            "gene name (z): gene a\n" +
            "gene name (h): GENE AYE\n" +
            "gene symbol (h): GENEA\n" +
            "gene name (m):\n" +
            "gene symbol (m):\n" +
            "\n" +
            "first seen: 2026-05-01\n" +
            "ZDB-GENE-bbb     geneb\n" +
            "gene name (z): gene b\n" +
            "gene name (h):\n" +
            "gene symbol (h):\n" +
            "gene name (m): gene bee\n" +
            "gene symbol (m): Geneb\n" +
            "\n");

        List<OrthoUpdateReportJob.IncRow> rows = OrthoUpdateReportJob.parseInconsistencyBlocks(
            tmp.getRoot(), "orthoInconsistentZebrafishGeneNamesReport_persistent.txt");

        assertEquals(2, rows.size());

        OrthoUpdateReportJob.IncRow first = rows.get(0);
        assertEquals("2026-06-09",  first.firstSeen());
        assertEquals("ZDB-GENE-aaa", first.zdbId());
        assertEquals("GENE AYE",     first.hName());
        assertEquals("", first.mName());      // empty "gene name (m):" line

        OrthoUpdateReportJob.IncRow second = rows.get(1);
        assertEquals("2026-05-01",  second.firstSeen());
        assertEquals("",            second.hName());
        assertEquals("gene bee",    second.mName());
    }

    @Test
    public void emptyOrMissingBlockFileYieldsNoRows() throws Exception {
        assertTrue(OrthoUpdateReportJob.parseInconsistencyBlocks(
            tmp.getRoot(), "missing.txt").isEmpty());
    }

    // ---------------- full report build + render ----------------

    @Test
    public void buildsReportWithFourNavSectionsAndRenders() throws Exception {
        write("ortho_statistics.txt",
            "load name\t2026-06-09\tnumber of MGI links\tnumber of HGNC links\t" +
            "number of OMIM links\tnumber of GENE links\tnumber of FLYBASE links\n" +
            "ncbi ortho load\t2026-06-08\t100\t200\t50\t300\t40\n");
        write("orthoInconsistentZebrafishGeneNamesReport.txt",
            "ZDB-GENE-123     myga\n" +
            "gene name (z): my gene a\n" +
            "gene name (h): MY GENE ALPHA\n" +
            "gene symbol (h): MYGA\n" +
            "gene name (m):\n" +
            "gene symbol (m):\n\n");
        write("orthoInconsistentZebrafishGeneNamesReport_persistent.txt",
            "# header\n\nfirst seen: 2026-06-09\nZDB-GENE-123     myga\n" +
            "gene name (z): my gene a\ngene name (h): MY GENE ALPHA\n" +
            "gene symbol (h): MYGA\ngene name (m):\ngene symbol (m):\n\n");
        write("orthoNamesUpdateList.txt", "ZDB-GENE-999|mygz|Human|old name|new name|\n");
        write("ortho_obsolete.txt",
            "ZDB-GENE-777|abc|gene abc|ABC|12345|gene ABC|IEA|ZDB-PUB-1\n");
        write("orthoNcbiIdsNotFoundReport.txt", "ZDB-GENE-555\tdef\t67890\tMouse\n");

        OrthoUpdateReportJob job = new OrthoUpdateReportJob("Update-Orthology_w", tmp.getRoot());
        var report = job.buildReport(0);

        var children = report.getRoot().getChildren();
        assertEquals(4, children.size());
        assertEquals("Load statistics",                children.get(0).getTitle());
        assertEquals("Inconsistent ZF gene names",     children.get(1).getTitle());
        assertEquals("Ortholog names updated",         children.get(2).getTitle());
        assertEquals("Obsolete & NCBI ids not found",  children.get(3).getTitle());

        // Each section is a structural node (has a count → shows in the left nav).
        children.forEach(c -> assertNotNull("nav count for " + c.getTitle(), c.getCount()));

        String html = new org.zfin.report.ReportWriter().render(report);
        assertTrue(html.contains("window.REPORT_DATA_GZ"));
        // The payload is gzipped + Base64-encoded, so assert against the
        // inflated JSON rather than the (now opaque) HTML.
        String json = inflateReportData(html);
        assertTrue(json.contains("Inconsistent ZF gene names"));
        // Diff highlighting reached the output (OrthoNameDiff wraps changes in <u>).
        assertTrue(json.contains("<u>"));
        // Gene-symbol columns are present (ZFIN + human + mouse abbreviations) so
        // curators can spot abbreviation-only changes.
        assertTrue(json.contains("ZFIN symbol"));
        assertTrue(json.contains("Human symbol"));
        assertTrue(json.contains("Mouse symbol"));
        // The parsed human ortholog symbol made it into a row cell.
        assertTrue(json.contains("MYGA"));
        // The ZFIN gene symbol reached the "Ortholog names updated" table too.
        assertTrue(json.contains("mygz"));
    }

    /** Extract and gunzip the {@code window.REPORT_DATA_GZ} payload from rendered report HTML. */
    private static String inflateReportData(String html) throws Exception {
        var matcher = java.util.regex.Pattern
            .compile("window\\.REPORT_DATA_GZ = \"([^\"]*)\"")
            .matcher(html);
        assertTrue("REPORT_DATA_GZ payload present", matcher.find());
        byte[] gz = java.util.Base64.getDecoder().decode(matcher.group(1));
        try (var in = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(gz))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
