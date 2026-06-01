package org.zfin.uniprot.secondary;

import org.junit.Test;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.datatransfer.report.model.ZfinReport;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.UniProtLoadLink;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;
import org.zfin.uniprot.dto.UniProtLoadSummaryListDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UniProtSecondaryReportAdapterTest {

    @Test
    public void emptyContainerProducesValidReport() {
        SecondaryTermLoadActionsContainer empty = new SecondaryTermLoadActionsContainer();
        empty.setActions(new ArrayList<>());
        empty.setUniprotDatFile(Map.of());

        ZfinReport report = new UniProtSecondaryReportAdapter()
            .adapt("UniProt Secondary Term Load", 42L, empty);

        assertEquals("UniProt Secondary Term Load", report.getMeta().getTitle());
        assertEquals("42", report.getMeta().getReleaseID());
        assertNotNull(report.getMeta().getCreationDate());
        assertTrue(report.getActions().isEmpty());
        assertTrue(report.getSummary().getTables().isEmpty());
        assertTrue(report.getSupplementalData().isEmpty());
    }

    @Test
    public void containerCreationDateOverridesNow() {
        SecondaryTermLoadActionsContainer c = new SecondaryTermLoadActionsContainer();
        c.setActions(new ArrayList<>());
        c.setUniprotDatFile(Map.of());
        c.setCreationDate(new Date(1_700_000_000_000L));

        ZfinReport report = new UniProtSecondaryReportAdapter().adapt("t", null, c);
        assertEquals(Long.valueOf(1_700_000_000_000L), report.getMeta().getCreationDate());
        assertNull("null releaseID stays null", report.getMeta().getReleaseID());
    }

    @Test
    public void summaryRendersFromUniProtLoadSummaryListDTO() {
        UniProtLoadSummaryListDTO summary = new UniProtLoadSummaryListDTO();
        summary.putBeforeSummary(new UniProtLoadSummaryItemDTO("db_link records", 10_000L, 0L));
        summary.putAfterSummary(new UniProtLoadSummaryItemDTO("db_link records", 0L, 10_500L));

        SecondaryTermLoadActionsContainer c = new SecondaryTermLoadActionsContainer();
        c.setActions(new ArrayList<>());
        c.setUniprotDatFile(Map.of());
        c.setSummary(summary);

        List<LoadReportSummaryTable> tables = new UniProtSecondaryReportAdapter()
            .adapt("t", null, c).getSummary().getTables();
        assertEquals(1, tables.size());
        Map<String, Object> row = tables.get(0).getRows().get(0);
        assertEquals("db_link records", row.get("description"));
        assertEquals(10_000L, row.get("before"));
        assertEquals(10_500L, row.get("after"));
        assertEquals(500L,    row.get("change"));
        assertEquals("5.00%", row.get("percent"));
    }

    @Test
    public void actionRoundTripWithMultipleUniProtAccessions() {
        Map<String, String> related = new LinkedHashMap<>();
        related.put("goTermName", "DNA binding");
        related.put("goID", "GO:0003677");

        Set<UniProtLoadLink> links = new TreeSet<>();
        links.add(new UniProtLoadLink("InterPro:IPR000001", "https://example.com/IPR000001"));

        SecondaryTermLoadAction a = SecondaryTermLoadAction.builder()
            .type(SecondaryTermLoadAction.Type.LOAD)
            .subType(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE)
            .dbName(ForeignDB.AvailableName.INTERPRO)
            .accession("IPR000001")
            .geneZdbID("ZDB-GENE-001")
            .relatedEntityID("ZDB-MGTE-001")
            .details("Created marker GO term evidence.")
            .length(120)
            .uniprotAccessions(new LinkedHashSet<>(List.of("P12345", "Q67890", "MISSING")))
            .relatedEntityFields(related)
            .links(links)
            .build();

        SecondaryTermLoadActionsContainer c = new SecondaryTermLoadActionsContainer();
        List<SecondaryTermLoadAction> actions = new ArrayList<>();
        actions.add(a);
        c.setActions(actions);
        Map<String, String> dat = new LinkedHashMap<>();
        dat.put("P12345",  "ID   FOO ...\n");
        dat.put("Q67890",  "ID   BAR ...\n");
        // "MISSING" intentionally absent: its key shouldn't appear in supplementalDataKeys.
        c.setUniprotDatFile(dat);

        ZfinReport report = new UniProtSecondaryReportAdapter().adapt("t", null, c);
        LoadReportAction la = report.getActions().get(0);

        assertEquals(LoadReportAction.Type.LOAD, la.getType());
        assertEquals(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE.getValue(), la.getSubType());
        assertEquals("IPR000001", la.getAccession());
        assertEquals("ZDB-GENE-001", la.getGeneZdbID());
        assertEquals("ZDB-MGTE-001", la.getRelatedEntityID());
        assertEquals(ForeignDB.AvailableName.INTERPRO.toString(), la.getDbName());
        assertEquals("120", la.getLength());
        assertEquals(List.of("P12345", "Q67890", "MISSING"), la.getUniprotAccessions());

        // Only accessions present in the DAT map produce blob refs, but all
        // accessions stay on the action for completeness.
        assertEquals(List.of("P12345", "Q67890"), la.getSupplementalDataKeys());

        assertEquals("DNA binding", la.getRelatedEntityFields().get("goTermName"));
        assertEquals("GO:0003677",  la.getRelatedEntityFields().get("goID"));

        // Only static links carry through; dynamic links (which would need
        // Hibernate to resolve ZFIN/InterPro URL prefixes) are intentionally
        // skipped — see UniProtSecondaryReportAdapter#buildActions.
        List<String> linkTitles = la.getLinks().stream().map(l -> l.getTitle()).toList();
        assertEquals(List.of("InterPro:IPR000001"), linkTitles);
    }

    @Test
    public void noBlobRefsWhenUniProtAccessionsEmpty() {
        SecondaryTermLoadAction a = SecondaryTermLoadAction.builder()
            .type(SecondaryTermLoadAction.Type.INFO)
            .subType(SecondaryTermLoadAction.SubType.PROTEIN)
            .accession("X")
            .build();

        SecondaryTermLoadActionsContainer c = new SecondaryTermLoadActionsContainer();
        c.setActions(List.of(a));
        c.setUniprotDatFile(Map.of("X", "..."));

        LoadReportAction la = new UniProtSecondaryReportAdapter().adapt("t", null, c).getActions().get(0);
        assertNull("no uniprotAccessions → no blob refs even when dat has a match by accession",
            la.getSupplementalDataKeys());
    }

    @Test
    public void allSecondaryTypesMapToLegacyByName() {
        // Same fail-fast guard as the primary adapter test: ensures a new
        // SecondaryTermLoadAction.Type value gets a matching legacy enum entry
        // (or this test fails at valueOf and forces a deliberate decision).
        for (SecondaryTermLoadAction.Type t : SecondaryTermLoadAction.Type.values()) {
            LoadReportAction.Type mapped = LoadReportAction.Type.valueOf(t.name());
            assertFalse("mapped type must not be null for " + t, mapped == null);
        }
    }
}
