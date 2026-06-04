package org.zfin.report;

import org.junit.Test;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportMeta;
import org.zfin.datatransfer.report.model.ZfinReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Pure-function tests for the legacy → new report adapter.
 *
 * <p>Locks in the parser semantics from the polish commit (k:v / arrow split,
 * size cap, leaf fallback) plus the relatedActionsKeys → edges translation
 * (pair dedup, key-size cap).
 */
public class LegacyReportAdapterTest {

    // ---------------- tryParseKeyValueTable ----------------

    @Test
    public void kvBodyWithNoArrowsProducesFieldValueTable() {
        ReportTable t = LegacyReportAdapter.tryParseKeyValueTable(
            "Accession: CAE49527\n" +
            "Gene ZDB ID: ZDB-GENE-040724-240\n" +
            "Database: GenPept (ZDB-FDBCONT-040412-42)\n");

        assertNotNull(t);
        assertEquals(Arrays.asList("Field", "Value"),
            t.getColumns().stream().map(ReportTable.Column::getTitle).collect(Collectors.toList()));
        assertEquals(3, t.getRows().size());
        assertEquals("Accession",                          t.getRows().get(0).get("field"));
        assertEquals("CAE49527",                           t.getRows().get(0).get("value"));
        assertEquals("GenPept (ZDB-FDBCONT-040412-42)",    t.getRows().get(2).get("value"));
    }

    @Test
    public void kvBodyWithArrowProducesFieldBeforeAfterTable() {
        ReportTable t = LegacyReportAdapter.tryParseKeyValueTable(
            "ZDB ID          : ZDB-GENE-050419-154\n" +
            "Length          : 9069 -> 9069\n" +
            "Pub ZDB ID      : ZDB-PUB-130725-2 -> ZDB-PUB-020723-3\n");

        assertEquals(Arrays.asList("Field", "Before", "After"),
            t.getColumns().stream().map(ReportTable.Column::getTitle).collect(Collectors.toList()));
        assertEquals(3, t.getRows().size());
        // identifier-style row: value in Before, After blank
        assertEquals("ZDB ID",              t.getRows().get(0).get("field"));
        assertEquals("ZDB-GENE-050419-154", t.getRows().get(0).get("before"));
        assertEquals("",                    t.getRows().get(0).get("after"));
        // arrow-style row: split across Before / After
        assertEquals("Pub ZDB ID",         t.getRows().get(2).get("field"));
        assertEquals("ZDB-PUB-130725-2",   t.getRows().get(2).get("before"));
        assertEquals("ZDB-PUB-020723-3",   t.getRows().get(2).get("after"));
    }

    @Test
    public void returnsNullWhenAnyNonBlankLineIsntKv() {
        assertNull(LegacyReportAdapter.tryParseKeyValueTable(
            "Accession: X\n" +
            "this line has no colon and should bail\n"));
    }

    @Test
    public void returnsNullOnEmptyOrNullInput() {
        assertNull(LegacyReportAdapter.tryParseKeyValueTable(null));
        assertNull(LegacyReportAdapter.tryParseKeyValueTable(""));
        assertNull(LegacyReportAdapter.tryParseKeyValueTable("   \n\n"));
    }

    @Test
    public void capsAtMaxKvLinesSoPathologicalBodyFallsBack() {
        StringBuilder body = new StringBuilder();
        for (int i = 1; i <= 2000; i++) body.append("k").append(i).append(": v").append(i).append('\n');
        assertNull(LegacyReportAdapter.tryParseKeyValueTable(body.toString()));
    }

    // ---------------- addRelatedEdges (via adapt()) ----------------

    @Test
    public void keySharedByTwoActionsEmitsOneRelatedEdge() {
        Report r = new LegacyReportAdapter().adapt(report(Arrays.asList(
            action("a1", LoadReportAction.Type.LOAD, "subA", Arrays.asList("ZDB-GENE-X")),
            action("a2", LoadReportAction.Type.LOAD, "subB", Arrays.asList("ZDB-GENE-X"))
        )));
        assertNotNull(r.getEdges());
        assertEquals(1, r.getEdges().size());
        assertEquals("a1",      r.getEdges().get(0).getFrom());
        assertEquals("a2",      r.getEdges().get(0).getTo());
        assertEquals("related", r.getEdges().get(0).getType());
    }

    @Test
    public void pairsAreDedupedWhenActionsShareMultipleKeys() {
        Report r = new LegacyReportAdapter().adapt(report(Arrays.asList(
            action("a1", LoadReportAction.Type.LOAD, "sub", Arrays.asList("ZDB-GENE-X", "ZDB-PUB-Y")),
            action("a2", LoadReportAction.Type.LOAD, "sub", Arrays.asList("ZDB-GENE-X", "ZDB-PUB-Y"))
        )));
        assertEquals(1, r.getEdges().size());
    }

    @Test
    public void skipsKeysWithMoreThanFiftyActions() {
        // 51 actions sharing one key — over the cap, no edges emitted.
        List<LoadReportAction> actions = IntStream.rangeClosed(1, 51)
            .mapToObj(i -> action("a" + i, LoadReportAction.Type.LOAD, "sub", Arrays.asList("hot-key")))
            .collect(Collectors.toList());
        Report r = new LegacyReportAdapter().adapt(report(actions));
        assertNull("No edges expected for over-cap key", r.getEdges());
    }

    @Test
    public void exactlyFiftyActionsSharingKeyEmitAllPairs() {
        List<LoadReportAction> actions = IntStream.rangeClosed(1, 50)
            .mapToObj(i -> action("a" + i, LoadReportAction.Type.LOAD, "sub", Arrays.asList("cluster")))
            .collect(Collectors.toList());
        Report r = new LegacyReportAdapter().adapt(report(actions));
        assertNotNull(r.getEdges());
        assertEquals(50 * 49 / 2, r.getEdges().size());
    }

    // ---------------- helpers ----------------

    private static ZfinReport report(List<LoadReportAction> actions) {
        ZfinReport r = new ZfinReport();
        r.setMeta(new LoadReportMeta("test report", "", System.currentTimeMillis()));
        r.setActions(new ArrayList<>(actions));
        return r;
    }

    private static LoadReportAction action(String id, LoadReportAction.Type type,
                                           String subType, List<String> relatedKeys) {
        LoadReportAction a = new LoadReportAction();
        a.setId(id);
        a.setType(type);
        a.setSubType(subType);
        a.setRelatedActionsKeys(new ArrayList<>(relatedKeys));
        return a;
    }
}
