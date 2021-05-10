package org.zfin.mapping;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class EnsemblDataServiceTest {

    @Test
    public void getEnsemblSoftware() {
        EnsemblDataService service = new EnsemblDataService();
        String release = service.getRelease();
        assertNotNull(release);
        assertEquals("Software release number", "74", release);

        String build = service.getBuild();
        assertNotNull(build);
        assertEquals("Assembly Name", "Zv9", build);
        assertEquals("Assembly Name", "Zv9", service.getGenomeBrowserMetaData().getBuild());
    }

    @Test
    public void getEnsemblMarkerInfo() {
        // pax2a
        String geneID = "ENSDARG00000028148";
        EnsemblDataService service = new EnsemblDataService();
        GenomeLocation genomeLocations = service.getGenomeLocation(geneID);
        assertNotNull(genomeLocations);
        assertEquals("Chromosome Number", "13", genomeLocations.getChromosome());
        assertEquals("Start", "29994439", genomeLocations.getStart());
        assertEquals("End", "30027366", genomeLocations.getEnd());
    }
}
