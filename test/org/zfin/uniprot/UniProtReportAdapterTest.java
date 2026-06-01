package org.zfin.uniprot;

import org.junit.Test;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.datatransfer.report.model.ZfinReport;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UniProtReportAdapterTest {

    @Test
    public void emptyContainerProducesValidReport() {
        UniProtLoadActionsContainer empty = UniProtLoadActionsContainer.builder()
            .actions(new TreeSet<>())
            .summary(List.of())
            .uniprotDatFile(Map.of())
            .build();

        ZfinReport report = new UniProtReportAdapter().adapt("UniProt Diff Load", "2026_01", empty);

        assertEquals("UniProt Diff Load", report.getMeta().getTitle());
        assertEquals("2026_01", report.getMeta().getReleaseID());
        assertNotNull(report.getMeta().getCreationDate());
        assertTrue(report.getActions().isEmpty());
        assertTrue(report.getSummary().getTables().isEmpty());
        assertTrue(report.getSupplementalData().isEmpty());
    }

    @Test
    public void summaryItemsRenderAsRows() {
        UniProtLoadActionsContainer c = UniProtLoadActionsContainer.builder()
            .actions(new TreeSet<>())
            .summary(List.of(
                new UniProtLoadSummaryItemDTO("db_link records", 40_000L, 41_500L),
                new UniProtLoadSummaryItemDTO("zero-base item", 0L, 5L)
            ))
            .uniprotDatFile(Map.of())
            .build();

        ZfinReport report = new UniProtReportAdapter().adapt("UniProt Diff Load", null, c);

        List<LoadReportSummaryTable> tables = report.getSummary().getTables();
        assertEquals(1, tables.size());
        List<Map<String, Object>> rows = tables.get(0).getRows();
        assertEquals(2, rows.size());

        Map<String, Object> first = rows.get(0);
        assertEquals("db_link records", first.get("description"));
        assertEquals(40_000L, first.get("before"));
        assertEquals(41_500L, first.get("after"));
        assertEquals(1_500L, first.get("change"));
        assertEquals("3.75%", first.get("percent"));

        // Guard against the division-by-zero edge: before=0 yields empty percent
        // so the renderer doesn't show "Infinity%" or "NaN%".
        assertEquals("", rows.get(1).get("percent"));
    }

    @Test
    public void actionFieldsRoundTrip() {
        UniProtLoadAction a = UniProtLoadAction.builder()
            .type(UniProtLoadAction.Type.LOAD)
            .subType(UniProtLoadAction.SubType.MATCH_BY_REFSEQ)
            .accession("Q9I8N1")
            .geneZdbID("ZDB-GENE-040426-1")
            .details("matched by NM_001001819")
            .length(287)
            .build();
        a.addLink(new UniProtLoadLink("UniProtKB:Q9I8N1", "https://www.uniprot.org/uniprotkb/Q9I8N1"));
        a.addTag(UniProtLoadAction.CategoryTag.NEW_GENE);

        UniProtLoadActionsContainer c = UniProtLoadActionsContainer.builder()
            .actions(new TreeSet<>(Set.of(a)))
            .summary(List.of())
            .uniprotDatFile(Map.of("Q9I8N1", "ID   ZNF_HUMAN ...\nSQ   SEQUENCE 287 AA;\n"))
            .build();

        ZfinReport report = new UniProtReportAdapter().adapt("UniProt Diff Load", "x", c);

        assertEquals(1, report.getActions().size());
        LoadReportAction la = report.getActions().get(0);
        assertEquals(LoadReportAction.Type.LOAD, la.getType());
        assertEquals(UniProtLoadAction.SubType.MATCH_BY_REFSEQ.getValue(), la.getSubType());
        assertEquals("Q9I8N1", la.getAccession());
        assertEquals("ZDB-GENE-040426-1", la.getGeneZdbID());
        assertEquals("matched by NM_001001819", la.getDetails());
        assertEquals("287", la.getLength());
        assertEquals(1, la.getLinks().size());
        assertEquals("UniProtKB:Q9I8N1", la.getLinks().get(0).getTitle());
        assertEquals(1, la.getTags().size());
        assertEquals(UniProtLoadAction.CategoryTag.NEW_GENE.name(), la.getTags().get(0).getName());

        // supplementalDataKeys carry the accession so the renderer surfaces the
        // matching DAT-format blob under the action page. Equivalent to the
        // <pre>${uniprotRecord}</pre> rendering of the legacy load-report.html.
        assertEquals(List.of("Q9I8N1"), la.getSupplementalDataKeys());
        assertTrue(report.getSupplementalData().containsKey("Q9I8N1"));

        // gene scope adds a related-actions key so siblings on the same gene
        // get pairwise edges via LegacyReportAdapter.addRelatedEdges.
        assertEquals(List.of("gene:ZDB-GENE-040426-1", "gene:ZDB-GENE-040426-1"),
            la.getRelatedActionsKeys());
    }

    @Test
    public void zeroLengthAndMissingAccessionStayUnset() {
        UniProtLoadAction a = UniProtLoadAction.builder()
            .type(UniProtLoadAction.Type.INFO)
            .subType(UniProtLoadAction.SubType.GENE_LOST_ALL_UNIPROTS)
            .accession(null)
            .geneZdbID(null)
            .length(0)
            .build();

        UniProtLoadActionsContainer c = UniProtLoadActionsContainer.builder()
            .actions(new TreeSet<>(Set.of(a)))
            .summary(List.of())
            .uniprotDatFile(Map.of())
            .build();

        LoadReportAction la = new UniProtReportAdapter().adapt("t", null, c).getActions().get(0);
        assertNull("length=0 must not serialize as \"0\"", la.getLength());
        assertNull("no accession -> no supplementalDataKeys", la.getSupplementalDataKeys());
        assertNull("no geneZdbID -> no related key", la.getRelatedActionsKeys());
    }

    @Test
    public void datFileBlobsOnlyPopulateWhenAccessionPresent() {
        UniProtLoadAction a = UniProtLoadAction.builder()
            .type(UniProtLoadAction.Type.LOAD)
            .subType(UniProtLoadAction.SubType.MATCH_BY_REFSEQ)
            .accession("ABSENT_FROM_DAT")
            .build();

        Map<String, String> dat = new LinkedHashMap<>();
        dat.put("OTHER", "...");

        UniProtLoadActionsContainer c = UniProtLoadActionsContainer.builder()
            .actions(new TreeSet<>(Set.of(a)))
            .summary(List.of())
            .uniprotDatFile(dat)
            .build();

        LoadReportAction la = new UniProtReportAdapter().adapt("t", null, c).getActions().get(0);
        assertNull("dat map without our accession leaves the keys null",
            la.getSupplementalDataKeys());
        assertTrue(new UniProtReportAdapter().adapt("t", null, c)
            .getSupplementalData().containsKey("OTHER"));
    }

    @Test
    public void releaseIDStaysNullWhenAbsent() {
        UniProtLoadActionsContainer c = UniProtLoadActionsContainer.builder()
            .actions(new TreeSet<>())
            .summary(List.of())
            .uniprotDatFile(Map.of())
            .build();

        ZfinReport r1 = new UniProtReportAdapter().adapt("t", null, c);
        ZfinReport r2 = new UniProtReportAdapter().adapt("t", "", c);
        assertNull("null releaseID stays null on the report meta", r1.getMeta().getReleaseID());
        assertNull("empty releaseID treated the same as null", r2.getMeta().getReleaseID());
    }

    @Test
    public void allUniProtTypesMapToLegacyByName() {
        // Guard: if a new value lands on UniProtLoadAction.Type without a matching
        // LoadReportAction.Type, valueOf throws and this test fails fast — which
        // is what we want, since the adapter relies on enum-name parity.
        for (UniProtLoadAction.Type t : UniProtLoadAction.Type.values()) {
            LoadReportAction.Type mapped = LoadReportAction.Type.valueOf(t.name());
            assertFalse("mapped type must not be null for " + t, mapped == null);
        }
    }
}
