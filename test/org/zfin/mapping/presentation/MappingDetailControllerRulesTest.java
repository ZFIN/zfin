package org.zfin.mapping.presentation;

import org.junit.Test;
import org.zfin.feature.Feature;
import org.zfin.mapping.AssemblyOrder;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.mapping.GenomeLocation.Source;
import org.zfin.mapping.MarkerGenomeLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The rules the mapping detail page uses to decide which locations and which browser to show. They compare
 * assemblies by the order of the assembly table, so none of them names an assembly. The order is written out
 * here with the values the table has.
 */
public class MappingDetailControllerRulesTest {

    private final AssemblyOrder order = new AssemblyOrder(Map.of(
            "GRCz13ab", 5, "GRCz12tu", 10, "GRCz11", 20, "GRCz10", 30, "Zv9", 40));

    private MarkerGenomeLocation marker(String assembly, Source source) {
        MarkerGenomeLocation location = new MarkerGenomeLocation();
        location.setAssembly(assembly);
        location.setSource(source);
        return location;
    }

    private FeatureGenomeLocation feature(String featureId, String assembly, Source source) {
        Feature feature = new Feature();
        feature.setZdbID(featureId);
        FeatureGenomeLocation location = new FeatureGenomeLocation();
        location.setFeature(feature);
        location.setAssembly(assembly);
        location.setSource(source);
        return location;
    }

    // --- which location the browser is centered on

    @Test
    public void browserUsesTheNewestAssemblyThatHasABrowser() {
        MarkerGenomeLocation tu = marker("GRCz12tu", Source.ZFIN_NCBI);
        MarkerGenomeLocation z11 = marker("GRCz11", Source.ZFIN);
        assertSame(tu, MappingDetailController.chooseBrowserLocation(List.of(z11, tu), order).orElseThrow());
    }

    @Test
    public void browserFallsBackToAnOlderAssemblyWhenItIsTheOnlyOne() {
        MarkerGenomeLocation z11 = marker("GRCz11", Source.ZFIN);
        assertSame(z11, MappingDetailController.chooseBrowserLocation(List.of(z11), order).orElseThrow());
    }

    @Test
    public void aLandmarkOnAnAssemblyWithNoBrowserIsPassedOver() {
        MarkerGenomeLocation newer = marker("GRCz13ab", Source.ZFIN_NCBI);
        MarkerGenomeLocation tu = marker("GRCz12tu", Source.ZFIN_NCBI);
        assertSame(tu, MappingDetailController.chooseBrowserLocation(List.of(newer, tu), order).orElseThrow());
        assertTrue(MappingDetailController.chooseBrowserLocation(List.of(newer), order).isEmpty());
    }

    @Test
    public void onTheSameAssemblyAZfinNcbiLocationComesBeforeAZfinOne() {
        MarkerGenomeLocation zfin = marker("GRCz12tu", Source.ZFIN);
        MarkerGenomeLocation zfinNcbi = marker("GRCz12tu", Source.ZFIN_NCBI);
        assertSame(zfinNcbi, MappingDetailController.chooseBrowserLocation(List.of(zfin, zfinNcbi), order).orElseThrow());
    }

    @Test
    public void onlyLandmarkSourcesCountForTheBrowser() {
        assertTrue(MappingDetailController.chooseBrowserLocation(
                List.of(marker("GRCz12tu", Source.ENSEMBL), marker("GRCz11", Source.NCBI)), order).isEmpty());
    }

    // --- marker locations: a ZFIN location is superseded by a ZFIN_NCBI one on the same or a newer assembly

    @Test
    public void aZfinLocationOnAnOlderAssemblyIsDroppedWhenThereIsAZfinNcbiOne() {
        List<MarkerGenomeLocation> locations = new ArrayList<>(List.of(
                marker("GRCz12tu", Source.ZFIN_NCBI), marker("GRCz11", Source.ZFIN), marker("GRCz11", Source.ENSEMBL)));
        MappingDetailController.removeSupersededZfinLocations(locations, order);
        assertEquals(List.of(Source.ZFIN_NCBI, Source.ENSEMBL), locations.stream().map(MarkerGenomeLocation::getSource).toList());
    }

    @Test
    public void aZfinLocationOnTheSameAssemblyIsDroppedToo() {
        List<MarkerGenomeLocation> locations = new ArrayList<>(List.of(
                marker("GRCz12tu", Source.ZFIN_NCBI), marker("GRCz12tu", Source.ZFIN)));
        MappingDetailController.removeSupersededZfinLocations(locations, order);
        assertEquals(1, locations.size());
        assertEquals(Source.ZFIN_NCBI, locations.get(0).getSource());
    }

    @Test
    public void aZfinLocationOnANewerAssemblyIsKept() {
        List<MarkerGenomeLocation> locations = new ArrayList<>(List.of(
                marker("GRCz12tu", Source.ZFIN_NCBI), marker("GRCz13ab", Source.ZFIN)));
        MappingDetailController.removeSupersededZfinLocations(locations, order);
        assertEquals(2, locations.size());
    }

    @Test
    public void nothingIsDroppedWithoutAZfinNcbiLocation() {
        List<MarkerGenomeLocation> locations = new ArrayList<>(List.of(
                marker("GRCz11", Source.ZFIN), marker("GRCz11", Source.ENSEMBL)));
        MappingDetailController.removeSupersededZfinLocations(locations, order);
        assertEquals(2, locations.size());
    }

    // --- feature locations: a genetic-map location is superseded by any location on a newer assembly

    @Test
    public void aGeneticMapLocationIsDroppedWhenTheFeatureHasANewerLocation() {
        List<FeatureGenomeLocation> locations = new ArrayList<>(List.of(
                feature("ZDB-ALT-1", "GRCz12tu", Source.DIRECT),
                feature("ZDB-ALT-1", "GRCz11", Source.OTHER_MAPPING),
                feature("ZDB-ALT-1", "GRCz11", Source.DIRECT)));
        MappingDetailController.removeSupersededOtherMappingLocations(locations, order);
        assertEquals(List.of("GRCz12tu", "GRCz11"), locations.stream().map(FeatureGenomeLocation::getAssembly).toList());
        assertTrue(locations.stream().noneMatch(l -> l.getSource() == Source.OTHER_MAPPING));
    }

    @Test
    public void aGeneticMapLocationIsKeptWhenNothingIsNewer() {
        List<FeatureGenomeLocation> sameAssembly = new ArrayList<>(List.of(
                feature("ZDB-ALT-1", "GRCz11", Source.DIRECT), feature("ZDB-ALT-1", "GRCz11", Source.OTHER_MAPPING)));
        MappingDetailController.removeSupersededOtherMappingLocations(sameAssembly, order);
        assertEquals(2, sameAssembly.size());

        List<FeatureGenomeLocation> olderOnly = new ArrayList<>(List.of(
                feature("ZDB-ALT-2", "Zv9", Source.DIRECT), feature("ZDB-ALT-2", "GRCz11", Source.OTHER_MAPPING)));
        MappingDetailController.removeSupersededOtherMappingLocations(olderOnly, order);
        assertEquals(2, olderOnly.size());
    }

    @Test
    public void eachFeatureIsJudgedOnItsOwnLocations() {
        List<FeatureGenomeLocation> locations = new ArrayList<>(List.of(
                feature("ZDB-ALT-1", "GRCz12tu", Source.DIRECT), feature("ZDB-ALT-1", "GRCz11", Source.OTHER_MAPPING),
                feature("ZDB-ALT-2", "GRCz11", Source.OTHER_MAPPING)));
        MappingDetailController.removeSupersededOtherMappingLocations(locations, order);
        assertEquals(2, locations.size());
        assertEquals("ZDB-ALT-2", locations.get(1).getFeature().getZdbID());
    }

    @Test
    public void aNewerAssemblyThanGrcz12tuSupersedesWithoutAnyCodeChange() {
        List<FeatureGenomeLocation> locations = new ArrayList<>(List.of(
                feature("ZDB-ALT-1", "GRCz13ab", Source.DIRECT), feature("ZDB-ALT-1", "GRCz12tu", Source.OTHER_MAPPING)));
        MappingDetailController.removeSupersededOtherMappingLocations(locations, order);
        assertEquals(1, locations.size());
    }

    @Test
    public void emptyAndNullListsAreLeftAlone() {
        MappingDetailController.removeSupersededOtherMappingLocations(null, order);
        MappingDetailController.removeSupersededOtherMappingLocations(new ArrayList<>(), order);
        MappingDetailController.removeSupersededZfinLocations(new ArrayList<MarkerGenomeLocation>(), order);
    }
}
