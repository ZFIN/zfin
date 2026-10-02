package org.zfin.datatransfer.ncbi;

import org.junit.Test;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RefSeqAssemblyBackfillTaskTest {

    private static Gene2AccessionDTO row(String status, String rnaAcc, String proteinAcc, String genomicAcc, String assembly) {
        return new Gene2AccessionDTO("7955", "12345", status, rnaAcc, "-", proteinAcc, "-", genomicAcc, "-", "-", "-", "-", assembly, "-", "-", "-");
    }

    @Test
    public void buildsAssemblyMapFromRnaProteinAndGenomicAccessions() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("VALIDATED", "NM_001040305.2", "NP_001035394.1", "NC_007112.7", "Reference GRCz12tu Primary Assembly")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyBackfillTask.buildAccessionAssemblyMap(dtos);

        assertEquals(Set.of(NcbiAssemblyResolver.GRCZ12TU_ASSEMBLY_ID), result.get("NM_001040305"));
        assertEquals(Set.of(NcbiAssemblyResolver.GRCZ12TU_ASSEMBLY_ID), result.get("NP_001035394"));
        assertEquals(Set.of(NcbiAssemblyResolver.GRCZ12TU_ASSEMBLY_ID), result.get("NC_007112"));
    }

    @Test
    public void mergesMultipleAssembliesForTheSameCuratedAccession() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("VALIDATED", "NM_001040305.2", "-", "-", "Reference GRCz12tu Primary Assembly"),
                row("VALIDATED", "NM_001040305.2", "-", "-", "Reference GRCz12ab Primary Assembly")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyBackfillTask.buildAccessionAssemblyMap(dtos);

        assertEquals(Set.of(NcbiAssemblyResolver.GRCZ12TU_ASSEMBLY_ID, NcbiAssemblyResolver.GRCZ12AB_ASSEMBLY_ID), result.get("NM_001040305"));
    }

    @Test
    public void skipsSuppressedAndNonZebrafishRows() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("SUPPRESSED", "NM_000001.1", "-", "-", "Reference GRCz12tu Primary Assembly"),
                new Gene2AccessionDTO("9606", "12345", "VALIDATED", "NM_000002.1", "-", "-", "-", "-", "-", "-", "-", "-", "Reference GRCz12tu Primary Assembly", "-", "-", "-")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyBackfillTask.buildAccessionAssemblyMap(dtos);

        assertNull(result.get("NM_000001"));
        assertNull(result.get("NM_000002"));
    }

    @Test
    public void skipsRowsWithUnresolvedAssembly() {
        List<Gene2AccessionDTO> dtos = List.of(
                row("VALIDATED", "NM_001040305.2", "-", "-", "-")
        );

        Map<String, Set<Long>> result = RefSeqAssemblyBackfillTask.buildAccessionAssemblyMap(dtos);

        assertEquals(0, result.size());
    }
}
