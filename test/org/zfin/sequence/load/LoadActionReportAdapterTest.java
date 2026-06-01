package org.zfin.sequence.load;

import org.junit.Test;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.datatransfer.report.model.ZfinReport;

import java.util.HashMap;
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

public class LoadActionReportAdapterTest {

    @Test
    public void emptyContainerProducesValidReport() {
        LoadActionsContainer empty = LoadActionsContainer.builder()
            .actions(new TreeSet<>())
            .build();

        ZfinReport report = new LoadActionReportAdapter().adapt("My Load", empty);
        assertEquals("My Load", report.getMeta().getTitle());
        assertNotNull(report.getMeta().getCreationDate());
        assertTrue(report.getActions().isEmpty());
        assertTrue(report.getSummary().getTables().isEmpty());
    }

    @Test
    public void summaryDtoRendersAsKeyCountTable() {
        EnsemblLoadSummaryItemDTO dto = new EnsemblLoadSummaryItemDTO();
        dto.setDescription("Pre-load counts");
        dto.getCounts().put("ensemblTranscriptCount",  60_000L);
        dto.getCounts().put("zfinEnsemblGeneCount",    25_000L);

        LoadActionsContainer c = LoadActionsContainer.builder()
            .actions(new TreeSet<>())
            .summary(dto)
            .build();

        List<LoadReportSummaryTable> tables = new LoadActionReportAdapter().adapt("t", c).getSummary().getTables();
        assertEquals(1, tables.size());
        assertEquals("Pre-load counts", tables.get(0).getDescription());

        // EnsemblLoadSummaryItemDTO.counts is a HashMap, so row order isn't
        // guaranteed — fold into a name→count map and assert by key.
        Map<String, Object> byName = new LinkedHashMap<>();
        for (Map<String, Object> row : tables.get(0).getRows()) {
            byName.put((String) row.get("name"), row.get("count"));
        }
        assertEquals(2, byName.size());
        assertEquals(60_000L, byName.get("ensemblTranscriptCount"));
        assertEquals(25_000L, byName.get("zfinEnsemblGeneCount"));
    }

    @Test
    public void summaryDtoFallsBackOnDefaultDescription() {
        EnsemblLoadSummaryItemDTO dto = new EnsemblLoadSummaryItemDTO();
        dto.getCounts().put("k", 1L);

        LoadActionsContainer c = LoadActionsContainer.builder()
            .actions(new TreeSet<>()).summary(dto).build();

        List<LoadReportSummaryTable> tables = new LoadActionReportAdapter().adapt("t", c).getSummary().getTables();
        assertEquals("Counts", tables.get(0).getDescription());
    }

    @Test
    public void actionRoundTripIncludingLinksAndRelatedFields() {
        LoadAction a = new LoadAction(
            LoadAction.Type.LOAD,
            LoadAction.SubType.ENSDART_LOADED,
            "ENSDART00000123456",
            "ZDB-GENE-001",
            "Loaded transcript",
            987,
            null);
        Map<String, String> related = new LinkedHashMap<>();
        related.put("oldLength", "100");
        related.put("newLength", "987");
        a.setRelatedEntityFields(related);
        a.addLink(new LoadLink("ENSDART:ENSDART00000123456",
            "https://www.ensembl.org/Danio_rerio/Transcript/Summary?t=ENSDART00000123456"));

        LoadActionsContainer c = LoadActionsContainer.builder()
            .actions(new TreeSet<>(Set.of(a)))
            .build();

        LoadReportAction la = new LoadActionReportAdapter().adapt("t", c).getActions().get(0);
        assertEquals(LoadReportAction.Type.LOAD, la.getType());
        assertEquals(LoadAction.SubType.ENSDART_LOADED.getValue(), la.getSubType());
        assertEquals("ENSDART00000123456", la.getAccession());
        assertEquals("ZDB-GENE-001", la.getGeneZdbID());
        assertEquals("987", la.getLength());
        assertEquals("100", la.getRelatedEntityFields().get("oldLength"));
        assertEquals("987", la.getRelatedEntityFields().get("newLength"));
        assertEquals(1, la.getLinks().size());
        assertEquals("ENSDART:ENSDART00000123456", la.getLinks().get(0).getTitle());
        assertEquals(List.of("gene:ZDB-GENE-001", "gene:ZDB-GENE-001"), la.getRelatedActionsKeys());
    }

    @Test
    public void zeroLengthAndEmptyGeneStayUnset() {
        LoadAction a = new LoadAction(
            LoadAction.Type.INFO,
            LoadAction.SubType.NO_PRIORITY_FOUND,
            "ZDB-PUB-001", "", "No priority", 0, new HashMap<>());

        LoadActionsContainer c = LoadActionsContainer.builder()
            .actions(new TreeSet<>(Set.of(a)))
            .build();

        LoadReportAction la = new LoadActionReportAdapter().adapt("t", c).getActions().get(0);
        assertNull("length=0 must not serialize as \"0\"", la.getLength());
        assertNull("empty geneZdbID → no related-actions edge", la.getRelatedActionsKeys());
        // relatedEntityFields starts as an empty map from the constructor; the
        // adapter omits it when empty so the rendered field list stays clean.
        assertNull("empty relatedEntityFields stays unset", la.getRelatedEntityFields());
    }

    @Test
    public void allLoadActionTypesMapToLegacyByName() {
        // Fail-fast guard: a new LoadAction.Type value without a matching
        // LoadReportAction.Type entry would throw valueOf, failing this test.
        for (LoadAction.Type t : LoadAction.Type.values()) {
            LoadReportAction.Type mapped = LoadReportAction.Type.valueOf(t.name());
            assertFalse("mapped type must not be null for " + t, mapped == null);
        }
    }
}
