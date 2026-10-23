package org.zfin.datatransfer.ensembl;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EnsemblMappingFileTest {

    private static final String HEADER = "z11,z12tu,z12ab,zdb_id,ncbi_gene_id,conflict\n";

    private EnsemblMappingFile read(String body, boolean includeConflicts) throws IOException {
        return EnsemblMappingFile.read(new StringReader(HEADER + body), includeConflicts);
    }

    @Test
    public void loadsEachAssemblyColumnAndNeverZ11() throws IOException {
        EnsemblMappingFile file = read(
                "ENSDARG00000000001,ENSDARG00160013050,ENSDARG00180020189,ZDB-GENE-030616-55,368418,\n", false);
        assertEquals(2, file.entries().size());
        assertEquals("GRCz12tu", file.entries().get(0).assemblyName());
        assertEquals("ENSDARG00160013050", file.entries().get(0).ensdarg());
        assertEquals("GRCz12ab", file.entries().get(1).assemblyName());
        assertEquals("ZDB-GENE-030616-55", file.entries().get(1).zdbId());
    }

    @Test
    public void emptyCellsAreSkippedSilently() throws IOException {
        EnsemblMappingFile file = read("ENSDARG00000000001,,ENSDARG00180020189,ZDB-GENE-030616-55,368418,\n", false);
        assertEquals(1, file.entries().size());
        assertTrue(file.rejected().isEmpty());
    }

    @Test
    public void rejectsBadIdsAndIdsFromTheWrongAssembly() throws IOException {
        EnsemblMappingFile file = read(
                "ENSDARG00000000001,ENSDARG00180020189,ENSDARG00160013050,ZDB-GENE-030616-55,1,\n"
                        + "ENSDARG00000000002,notanid,,ZDB-GENE-030616-56,2,\n"
                        + "ENSDARG00000000003,ENSDARG00160013051,,,3,\n", false);
        assertEquals(0, file.entries().size());
        assertEquals(4, file.rejected().size());
    }

    @Test
    public void conflictRowsAreHeldBackUnlessRequested() throws IOException {
        String row = "ENSDARG00000000001,ENSDARG00160013050,,ZDB-GENE-030616-55,1,gene_mismatch\n";
        EnsemblMappingFile held = read(row, false);
        assertEquals(0, held.entries().size());
        assertEquals(1, held.conflicts().size());
        assertEquals("gene_mismatch", held.conflicts().get(0).conflict());
        assertEquals(1, read(row, true).entries().size());
    }

    @Test
    public void repeatedAssociationsAreLoadedOnce() throws IOException {
        EnsemblMappingFile file = read(
                "ENSDARG00000000001,ENSDARG00160013050,,ZDB-GENE-030616-55,1,\n"
                        + "ENSDARG00000000009,ENSDARG00160013050,,ZDB-GENE-030616-55,1,\n", false);
        assertEquals(1, file.entries().size());
        assertEquals(1, file.duplicateCount());
        assertEquals(2, file.rowCount());
    }

    @Test
    public void anEnsdargGivenToSeveralGenesIsNotLoadedForAny() throws IOException {
        EnsemblMappingFile file = read(
                "ENSDARG00000000001,ENSDARG00160013050,,ZDB-GENE-030616-55,1,\n"
                        + "ENSDARG00000000002,ENSDARG00160013050,,ZDB-GENE-030616-56,2,\n"
                        + "ENSDARG00000000003,ENSDARG00160013051,,ZDB-GENE-030616-57,3,\n", false);
        assertEquals(1, file.entries().size());
        assertEquals("ENSDARG00160013051", file.entries().get(0).ensdarg());
        assertEquals(2, file.ambiguous().size());
    }

    @Test
    public void aConflictRowStillMakesItsEnsdargAmbiguous() throws IOException {
        EnsemblMappingFile file = read(
                "ENSDARG00000000001,ENSDARG00160013050,,ZDB-GENE-030616-55,1,\n"
                        + "ENSDARG00000000002,ENSDARG00160013050,,ZDB-GENE-030616-56,2,gene_mismatch\n", false);
        assertEquals(0, file.entries().size());
        assertEquals(1, file.ambiguous().size());
        assertEquals(1, file.conflicts().size());
    }

    @Test
    public void z13abColumnIsLoadedWhenPresent() throws IOException {
        EnsemblMappingFile file = EnsemblMappingFile.read(
                new StringReader("z11,z12tu,z13ab,zdb_id\nENSDARG00000000001,,ENSDARG00200000001,ZDB-GENE-030616-55\n"), false);
        assertEquals("GRCz13ab", file.entries().get(0).assemblyName());
    }

    @Test
    public void requiresZdbIdAndAnAssemblyColumn() {
        assertThrows(IllegalArgumentException.class,
                () -> EnsemblMappingFile.read(new StringReader("z11,z12tu\nENSDARG00000000001,ENSDARG00160013050\n"), false));
        assertThrows(IllegalArgumentException.class,
                () -> EnsemblMappingFile.read(new StringReader("z11,zdb_id\nENSDARG00000000001,ZDB-GENE-030616-55\n"), false));
    }
}
