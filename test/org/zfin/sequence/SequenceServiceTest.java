package org.zfin.sequence;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.service.SequenceService;
import org.zfin.framework.HibernateUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

/**
 * Class SequenceRepositoryTest.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class SequenceServiceTest extends AbstractDatabaseTest {

    private final static Logger logger = LogManager.getLogger(SequenceServiceTest.class);
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    @Autowired
    private SequenceService sequenceService;

    /**
     * This tests the behavior for sequence table generation for clone pages as
     * described in ZFIN-7799
     */
    @Test
    public void testSequenceServiceJsonResults() {
        var pagination = new Pagination();
        var markerZdbId = "ZDB-GENE-041014-357";

        var response = sequenceService.getMarkerDBLinkJsonResultResponse(markerZdbId, pagination, false, false);
        var results = response.getResults();

        assertEquals("ZDB-GENE-041014-357 should have 20 sequences, but has " + results.size(), 20, results.size());
    }

    @Test
    public void testSequenceServiceJsonAggregation() {
        //Testing this result set that is retrieved for ZDB-GENE-041014-357 (adgrg6)
//        ZDB-DBLINK-060223-115:CAI11751:GENPEPT
//        ZDB-DBLINK-090926-31203:GQ202546:GENBANK
//        ZDB-DBLINK-200410-110909:GDQH01030811:GENBANK
//        ZDB-DBLINK-200410-137186:NM_001163291:REFSEQ
//        ZDB-DBLINK-200410-139634:NM_001369129:REFSEQ
//        ZDB-DBLINK-200410-181279:NP_001156763:REFSEQ
//        ZDB-DBLINK-200410-183708:NP_001356058:REFSEQ
//        ZDB-DBLINK-200410-62470:ACS94979:GENPEPT
//        ZDB-DBLINK-200410-64440:C6KFA3:GENPEPT
//        ZDB-DBLINK-200410-81082:DX504044:GENBANK
//        ZDB-DBLINK-090218-130:DX504044:GENBANK
//        ZDB-DBLINK-041007-144:BX004780:GENBANK
//        ZDB-DBLINK-041007-144:BX004780:GENBANK
        String markerZdbID = "ZDB-GENE-041014-357";
        String bacZdbID = "ZDB-BAC-041007-134";

        List<Triple<String, String, String>> accessions = new ArrayList<>();
        accessions.add(new ImmutableTriple<>(markerZdbID, "CAI11751", "GenPept"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "GQ202546", "GenBank"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "GDQH01030811", "GenBank"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NM_001163291", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NM_001369129", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NP_001156763", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "NP_001356058", "RefSeq"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "ACS94979", "GenPept"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "C6KFA3", "GenPept"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "DX504044", "GenBank"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "C6KFA3", "UniProtKB"));
        accessions.add(new ImmutableTriple<>(markerZdbID, "DX504044", "GenBank"));
        accessions.add(new ImmutableTriple<>(bacZdbID, "BX004780", "GenBank"));
        accessions.add(new ImmutableTriple<>(bacZdbID, "BX004780", "GenBank"));

        List<MarkerDBLink> links = new ArrayList<>();
        List<String> accessionsNotFound = new ArrayList<>();
        for (Triple<String, String, String> acc : accessions) {
            MarkerDBLink mdl = (MarkerDBLink) getSequenceRepository().getDBLink(acc.getLeft(), acc.getMiddle(), acc.getRight());
            links.add(mdl);
            if (mdl == null) {
                accessionsNotFound.add(acc.getLeft() + " " + acc.getMiddle() + " " + acc.getRight());
            }
        }

        assertEquals("Accessions not found: " + accessionsNotFound.toString(), 0, accessionsNotFound.size());
        assertEquals(links.size(), 14);

        List<MarkerDBLink> aggregatedLinks = MarkerService.aggregateDBLinksByPub(links);

        assertEquals(aggregatedLinks.size(), 12);
    }

    @Test
    @Ignore
    public void testAllGenes() {

        List<String> allGeneIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("genes_without_ids.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                allGeneIds.add(values[0]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileWriter writer = new FileWriter("sequenceIDs.txt", false);


//            List<String> ids = List.of("ZDB-GENE-141216-102");
            allGeneIds.forEach(id -> {
                try {
                    JsonResultResponse<MarkerDBLink> response = sequenceService.getMarkerDBLinkJsonResultResponse(id, new Pagination(), true, false);
                    response.getResults().forEach(markerDBLink -> {
                        try {
                            String line = id + "," + markerDBLink.getAccessionNumber();
                            writer.write(line + "\n");
                        } catch (Exception e) {
                            logger.error("Error writing to file", e);
                        }
                    });
                } catch (Exception e) {
                    logger.error("Error writing to file", e);
                }
            });
            writer.close();
            List<Accession> accessions = RepositoryFactory.getSequenceRepository().getAccessionsByNumber("ENSDARG00000002898");
        } catch (Exception e) {
            logger.error("Error writing to file", e);
        }
        //assertNotNull(accessions);
    }

    @Test
    public void testRNAMapRetrieval() {
        Set<DBLink> dbLinks = new HashSet<>(sequenceRepository
                .getDBLinksForAllMarkers(ForeignDBDataType.SuperType.SEQUENCE));
        assertTrue(dbLinks.size() > 20000);
    }

    @Test
    public void testRNAMapRetrieval2() {
        var map = sequenceRepository
                .getAllDBLinksByFirstRelatedMarker(
                        DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE,
                        MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT,
                        MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT,
                        MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT
                );
        assertTrue(map.size() > 20000);
    }

    @Test
    public void testRNAMapRetrieval3() {
        var map = sequenceService.getMarkerRNAMapForNCBILoad();
        assertTrue(map.size() > 20000);
    }

    @Test
    public void testRNAMapRetrievalCombined() {
        var map = sequenceRepository.getAllRNADBLinksForAllMarkersInGenedom();
        assertTrue(map.size() > 20000);
    }

    @Test
    public void testRNAMapRetrievalCombinedAndMapped() {
        List<Pair<String, String>> list = sequenceRepository.getAllRNADBLinksForAllMarkersInGenedom();
        Map<String, List<String>> mapOfGeneIDToAccessionList = new HashMap<>();
        for (Pair<String, String> tuple : list) {
            String geneID = tuple.getLeft();
            String accession = tuple.getRight();
            if (!mapOfGeneIDToAccessionList.containsKey(geneID)) {
                mapOfGeneIDToAccessionList.put(geneID, new ArrayList<>());
            }
            mapOfGeneIDToAccessionList.get(geneID).add(accession);
        }

        assertTrue(mapOfGeneIDToAccessionList.size() > 20000);
    }

    @Test
    public void testRNAMapRetrievalMatchesNativeSQL() {
        // Get results from HQL method
        List<Pair<String, String>> hqlResults = sequenceRepository.getAllRNADBLinksForAllMarkersInGenedom();
        Set<String> hqlPairs = new TreeSet<>();
        for (Pair<String, String> tuple : hqlResults) {
            hqlPairs.add(tuple.getLeft() + "|" + tuple.getRight());
        }

        // Get results from native SQL
        String sql = """
            select * from (
                (select distinct dblink_linked_recid as marker_id, dblink_acc_num as acc
                 from db_link
                 join foreign_db_contains on dblink_fdbcont_zdb_id = fdbcont_zdb_id
                 join foreign_db_data_type on fdbcont_fdbdt_id = fdbdt_pk_id
                 join foreign_db on fdbcont_fdb_db_id = fdb_db_pk_id
                 where fdbdt_super_type = 'sequence'
                 and fdbdt_data_type = 'RNA'
                 and fdb_db_name <> 'FishMiRNA-Expression'
                 and dblink_linked_recid in (
                     select mrkr_zdb_id from marker
                     where mrkr_type in (
                         select mtgrpmem_mrkr_type from marker_type_group_member
                         where mtgrpmem_mrkr_type_group = 'GENEDOM_AND_NTR'
                     )
                 ))
                union
                (select distinct mrel_mrkr_1_zdb_id as marker_id, dblink_acc_num as acc
                 from db_link
                 join foreign_db_contains on dblink_fdbcont_zdb_id = fdbcont_zdb_id
                 join foreign_db_data_type on fdbcont_fdbdt_id = fdbdt_pk_id
                 join foreign_db_contains_display_group_member on fdbcdgm_fdbcont_zdb_id = fdbcont_zdb_id
                 join foreign_db_contains_display_group on fdbcdg_pk_id = fdbcdgm_group_id
                 join foreign_db on fdbcont_fdb_db_id = fdb_db_pk_id
                 join marker_relationship mr on db_link.dblink_linked_recid = mr.mrel_mrkr_2_zdb_id
                 where foreign_db_contains_display_group.fdbcdg_name = 'marker linked sequence'
                 and fdbdt_data_type = 'RNA'
                 and mr.mrel_type in ('gene contains small segment',
                                      'clone contains small segment',
                                      'gene encodes small segment'))
            ) as subq
            order by marker_id, acc
            """;

        List<Object[]> nativeResults = HibernateUtil.currentSession()
                .createNativeQuery(sql, Object[].class)
                .list();

        Set<String> nativePairs = new TreeSet<>();
        for (Object[] row : nativeResults) {
            nativePairs.add(row[0] + "|" + row[1]);
        }

        // Compare
        Set<String> inHqlNotInNative = new TreeSet<>(hqlPairs);
        inHqlNotInNative.removeAll(nativePairs);

        Set<String> inNativeNotInHql = new TreeSet<>(nativePairs);
        inNativeNotInHql.removeAll(hqlPairs);

        if (!inHqlNotInNative.isEmpty()) {
            System.out.println("In HQL but not in native SQL (" + inHqlNotInNative.size() + " items):");
            inHqlNotInNative.stream().limit(10).forEach(System.out::println);
        }

        if (!inNativeNotInHql.isEmpty()) {
            System.out.println("In native SQL but not in HQL (" + inNativeNotInHql.size() + " items):");
            inNativeNotInHql.stream().limit(10).forEach(System.out::println);
        }

        assertEquals("HQL and native SQL should return same number of results",
                nativePairs.size(), hqlPairs.size());
        assertEquals("HQL and native SQL results should match exactly",
                nativePairs, hqlPairs);
    }

}
