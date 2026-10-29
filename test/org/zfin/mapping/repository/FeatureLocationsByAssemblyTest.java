package org.zfin.mapping.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * {@link LinkageRepository#getFeatureLocations(Marker)} takes its assemblies and their order from the
 * assembly table. Runs inside the transaction {@link AbstractDatabaseTest} rolls back.
 *
 * <p>ZDB-GENE-000128-8 has an allele with locations on GRCz12tu, GRCz11, GRCz10 and Zv9.
 */
public class FeatureLocationsByAssemblyTest extends AbstractDatabaseTest {

    private static final String GENE = "ZDB-GENE-000128-8";

    private Map<String, Integer> assemblyRank() {
        Map<String, Integer> rank = new HashMap<>();
        List<Object[]> rows = HibernateUtil.currentSession()
                .createNativeQuery("select a_name, a_order from assembly", Object[].class).getResultList();
        for (Object[] row : rows) {
            rank.put((String) row[0], ((Number) row[1]).intValue());
        }
        return rank;
    }

    private List<FeatureGenomeLocation> locations() {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(GENE);
        return RepositoryFactory.getLinkageRepository().getFeatureLocations(gene);
    }

    /** Asserts the locations of each feature are in assembly rank order, newest first. */
    private void assertNewestAssemblyFirstForEachFeature(List<FeatureGenomeLocation> locations) {
        Map<String, Integer> rank = assemblyRank();
        for (int i = 1; i < locations.size(); i++) {
            FeatureGenomeLocation previous = locations.get(i - 1);
            FeatureGenomeLocation current = locations.get(i);
            if (previous.getFeature().getZdbID().equals(current.getFeature().getZdbID())) {
                assertTrue(previous.getAssembly() + " should not come before " + current.getAssembly(),
                        rank.get(previous.getAssembly()) <= rank.get(current.getAssembly()));
            }
        }
    }

    @Test
    public void returnsEveryAssemblyThatHasALocation() {
        List<String> assemblies = locations().stream().map(FeatureGenomeLocation::getAssembly).distinct().toList();
        assertTrue(assemblies.toString(), assemblies.containsAll(List.of("GRCz12tu", "GRCz11", "GRCz10", "Zv9")));
    }

    @Test
    public void listsEachFeatureNewestAssemblyFirst() {
        List<FeatureGenomeLocation> locations = locations();
        assertFalse(locations.isEmpty());
        assertNewestAssemblyFirstForEachFeature(locations);
    }

    @Test
    public void aLocationOnANewerAssemblyThanGrcz12tuComesFirstWithoutAnyCodeChange() {
        HibernateUtil.currentSession().createNativeQuery("""
                insert into sequence_feature_chromosome_location_generated
                    (sfclg_chromosome, sfclg_data_zdb_id, sfclg_acc_num, sfclg_start, sfclg_end,
                     sfclg_location_source, sfclg_assembly, sfclg_strand)
                select l.sfclg_chromosome, l.sfclg_data_zdb_id, l.sfclg_acc_num, l.sfclg_start, l.sfclg_end,
                       l.sfclg_location_source, 'GRCz13ab', l.sfclg_strand
                  from sequence_feature_chromosome_location_generated l
                  join feature_marker_relationship fmr on fmr.fmrel_ftr_zdb_id = l.sfclg_data_zdb_id
                 where fmr.fmrel_mrkr_zdb_id = :gene and fmr.fmrel_type = 'is allele of' and l.sfclg_assembly = 'GRCz12tu'
                 limit 1
                """).setParameter("gene", GENE).executeUpdate();

        List<FeatureGenomeLocation> locations = locations();
        assertTrue(locations.stream().anyMatch(l -> "GRCz13ab".equals(l.getAssembly())));
        assertNewestAssemblyFirstForEachFeature(locations);
        String plantedFeature = locations.stream().filter(l -> "GRCz13ab".equals(l.getAssembly()))
                .findFirst().get().getFeature().getZdbID();
        assertEquals("GRCz13ab should be the first location of its feature", "GRCz13ab", locations.stream()
                .filter(l -> l.getFeature().getZdbID().equals(plantedFeature)).findFirst().get().getAssembly());
    }

    @Test
    public void anAssemblyThatTheTableDoesNotListIsLeftOut() {
        HibernateUtil.currentSession().createNativeQuery("""
                insert into sequence_feature_chromosome_location_generated
                    (sfclg_chromosome, sfclg_data_zdb_id, sfclg_acc_num, sfclg_start, sfclg_end,
                     sfclg_location_source, sfclg_assembly, sfclg_strand)
                select l.sfclg_chromosome, l.sfclg_data_zdb_id, l.sfclg_acc_num, l.sfclg_start, l.sfclg_end,
                       l.sfclg_location_source, 'GRCz99xx', l.sfclg_strand
                  from sequence_feature_chromosome_location_generated l
                  join feature_marker_relationship fmr on fmr.fmrel_ftr_zdb_id = l.sfclg_data_zdb_id
                 where fmr.fmrel_mrkr_zdb_id = :gene and fmr.fmrel_type = 'is allele of' and l.sfclg_assembly = 'GRCz12tu'
                 limit 1
                """).setParameter("gene", GENE).executeUpdate();

        assertTrue(locations().stream().noneMatch(l -> "GRCz99xx".equals(l.getAssembly())));
    }
}
