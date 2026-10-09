package org.zfin.datatransfer.ncbi;

import org.junit.Test;
import org.zfin.datatransfer.ncbi.RefSeqAssemblyReconciler.Changes;
import org.zfin.datatransfer.ncbi.RefSeqAssemblyReconciler.Link;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RefSeqAssemblyReconcilerTest {

    private static final long GRCZ12TU_ID = 1L;
    private static final long GRCZ13AB_ID = 2L;
    private static final NcbiAssemblyResolver RESOLVER =
            new NcbiAssemblyResolver(Map.of("GRCz12tu", GRCZ12TU_ID, "GRCz13ab", GRCZ13AB_ID));

    private static Gene2AccessionDTO row(String status, String rnaAcc, String proteinAcc, String genomicAcc, String assembly) {
        return new Gene2AccessionDTO("7955", "12345", status, rnaAcc, "-", proteinAcc, "-", genomicAcc, "-", "-", "-", "-", assembly, "-", "-", "-");
    }

    @Test
    public void buildsAssemblyMapFromRnaProteinAndGenomicAccessions() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("VALIDATED", "NM_001040305.2", "NP_001035394.1", "NC_007112.7", "Alternate GRCz12tu")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyReconciler.buildAccessionAssemblyMap(dtos, RESOLVER);

        assertEquals(Set.of(GRCZ12TU_ID), result.get("NM_001040305"));
        assertEquals(Set.of(GRCZ12TU_ID), result.get("NP_001035394"));
        assertEquals(Set.of(GRCZ12TU_ID), result.get("NC_007112"));
    }

    @Test
    public void mergesMultipleAssembliesForTheSameCuratedAccession() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("VALIDATED", "NM_001040305.2", "-", "-", "Alternate GRCz12tu"),
                row("VALIDATED", "NM_001040305.2", "-", "-", "Reference GRCz13ab Primary Assembly")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyReconciler.buildAccessionAssemblyMap(dtos, RESOLVER);

        assertEquals(Set.of(GRCZ12TU_ID, GRCZ13AB_ID), result.get("NM_001040305"));
    }

    @Test
    public void skipsSuppressedGenBankAndNonZebrafishRows() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("SUPPRESSED", "NM_000001.1", "-", "-", "Alternate GRCz12tu"),
                row("-", "BC162172.1", "-", "-", "-"),
                new Gene2AccessionDTO("9606", "12345", "VALIDATED", "NM_000002.1", "-", "-", "-", "-", "-", "-", "-", "-", "Alternate GRCz12tu", "-", "-", "-")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyReconciler.buildAccessionAssemblyMap(dtos, RESOLVER);

        assertNull(result.get("NM_000001"));
        assertNull(result.get("BC162172"));
        assertNull(result.get("NM_000002"));
    }

    @Test
    public void keepsAccessionsWithNoRecognisedAssemblyAsEmpty() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("VALIDATED", "NM_001040305.2", "-", "-", "-")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyReconciler.buildAccessionAssemblyMap(dtos, RESOLVER);

        assertEquals(Set.of(), result.get("NM_001040305"));
    }

    @Test
    public void planAddsMissingAndRemovesStaleLinks() {
        Changes changes = RefSeqAssemblyReconciler.plan(
                Map.of("NM_1", Set.of(GRCZ13AB_ID, GRCZ12TU_ID), "XM_2", Set.of(GRCZ13AB_ID)),
                Map.of("ZDB-DBLINK-1", "NM_1", "ZDB-DBLINK-2", "XM_2"),
                Map.of("ZDB-DBLINK-1", Set.of(GRCZ12TU_ID), "ZDB-DBLINK-2", Set.of(GRCZ12TU_ID)));

        assertEquals(List.of(new Link("ZDB-DBLINK-1", GRCZ13AB_ID), new Link("ZDB-DBLINK-2", GRCZ13AB_ID)), changes.toAdd());
        assertEquals(List.of(new Link("ZDB-DBLINK-2", GRCZ12TU_ID)), changes.toRemove());
    }

    @Test
    public void planRemovesAllLinksWhenNcbiNamesNoAssembly() {
        Changes changes = RefSeqAssemblyReconciler.plan(
                Map.of("NM_1", Set.of()),
                Map.of("ZDB-DBLINK-1", "NM_1"),
                Map.of("ZDB-DBLINK-1", Set.of(GRCZ12TU_ID, GRCZ13AB_ID)));

        assertEquals(List.of(), changes.toAdd());
        assertEquals(List.of(new Link("ZDB-DBLINK-1", GRCZ12TU_ID), new Link("ZDB-DBLINK-1", GRCZ13AB_ID)), changes.toRemove());
    }

    @Test
    public void planLeavesAccessionsMissingFromGene2AccessionAlone() {
        Changes changes = RefSeqAssemblyReconciler.plan(
                Map.of(),
                Map.of("ZDB-DBLINK-1", "NM_1"),
                Map.of("ZDB-DBLINK-1", Set.of(GRCZ12TU_ID)));

        assertEquals(List.of(), changes.toAdd());
        assertEquals(List.of(), changes.toRemove());
    }

    @Test
    public void planAddsLinksForDbLinksThatHaveNone() {
        Changes changes = RefSeqAssemblyReconciler.plan(
                Map.of("NM_1", Set.of(GRCZ12TU_ID)),
                Map.of("ZDB-DBLINK-1", "NM_1"),
                Map.of());

        assertEquals(List.of(new Link("ZDB-DBLINK-1", GRCZ12TU_ID)), changes.toAdd());
        assertEquals(List.of(), changes.toRemove());
    }
}
