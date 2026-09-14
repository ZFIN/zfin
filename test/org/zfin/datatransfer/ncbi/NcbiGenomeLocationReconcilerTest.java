package org.zfin.datatransfer.ncbi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDangerousDatabaseTest;
import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.framework.HibernateUtil;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Exercises {@link NcbiGenomeLocationReconciler} itself, against a database.
 *
 * <p>{@link NCBIGenomeLocationReconcileTest} covers the decisions in
 * {@link GenomeLocationDrift}, but it asserts against a hand-written copy of the loop, so it
 * passes whether or not the reconciler agrees with it. This drives the real thing: it plants
 * one row of each kind of drift, runs {@code reconcile()}, and checks what actually happened
 * to each row and what the report says.
 *
 * <p>Not in the default suite. {@code reconcile()} manages and commits its own transaction, so
 * this cannot be wrapped in a rollback the way {@code AbstractDatabaseTest} does - it writes
 * for real and cleans up after itself. It also reconciles the <em>whole</em> table, so it
 * refuses to run unless the database starts with no drift of its own; otherwise it would
 * delete rows it did not plant. Run the NCBI gene load first, then:
 *
 * <pre>
 * gradle -PncbiLoadTests test --tests org.zfin.datatransfer.ncbi.NcbiGenomeLocationReconcilerTest
 * </pre>
 */
public class NcbiGenomeLocationReconcilerTest extends AbstractDangerousDatabaseTest {

    private static final String SOURCE = "NCBILoader";
    private static final String NCBI_GENE_FDBCONT = "ZDB-FDBCONT-040412-1";
    /** Every row this test creates carries an accession with this prefix, so cleanup is exact. */
    private static final String TEST_ACC_PREFIX = "ZLOCTEST";

    private String geneOwner;      // holds the test accessions in db_link
    private String geneWrong;      // the gene the planted rows are mis-attributed to
    private String geneThird;      // a third party, for the ambiguous case

    private long orphanPk;
    private long remapPk;
    private long duplicatePk;
    private long duplicateTargetPk;
    private long ambiguousPk;

    @Before
    public void plantDrift() {
        requireNoPreExistingDrift();
        // AbstractDangerousDatabaseTest deliberately opens none, so the planting owns its own.
        HibernateUtil.createTransaction();

        List<String> genes = nativeList(
                "select mrkr_zdb_id from marker where mrkr_zdb_id like 'ZDB-GENE-%' order by mrkr_zdb_id limit 3");
        assertEquals("need three genes to work with", 3, genes.size());
        geneOwner = genes.get(0);
        geneWrong = genes.get(1);
        geneThird = genes.get(2);

        // ORPHANED: an accession db_link knows nothing about.
        orphanPk = insertLocation(geneOwner, TEST_ACC_PREFIX + "1", "1", 1000, 2000);

        // REMAPPED, no collision: the accession belongs to geneOwner, the row says geneWrong,
        // and geneOwner holds nothing else for it.
        linkAccession(TEST_ACC_PREFIX + "2", geneOwner);
        remapPk = insertLocation(geneWrong, TEST_ACC_PREFIX + "2", "2", 3000, 4000);

        // REMAPPED, collides with an identical row: geneOwner already holds this very location,
        // so moving the geneWrong row onto it duplicates a row that is already correct.
        linkAccession(TEST_ACC_PREFIX + "3", geneOwner);
        duplicateTargetPk = insertLocation(geneOwner, TEST_ACC_PREFIX + "3", "3", 5000, 6000);
        duplicatePk = insertLocation(geneWrong, TEST_ACC_PREFIX + "3", "3", 5000, 6000);

        // AMBIGUOUS: the accession maps to two genes, so there is no single right answer.
        linkAccession(TEST_ACC_PREFIX + "4", geneOwner);
        linkAccession(TEST_ACC_PREFIX + "4", geneWrong);
        ambiguousPk = insertLocation(geneThird, TEST_ACC_PREFIX + "4", "4", 7000, 8000);

        HibernateUtil.flushAndCommitCurrentSession();
    }

    @Test
    public void reconcileResolvesEachKindOfDrift() {
        List<LoadReportAction> actions =
                new NcbiGenomeLocationReconciler(new BufferedWriter(new StringWriter())).reconcile();

        // reconcile() committed and closed its own transaction; the checks below need one.
        HibernateUtil.createTransaction();

        // orphan: the accession links to nothing, so the location belongs to nothing
        assertNull("orphaned row should be deleted", geneOf(orphanPk));

        // remapped: coordinates belong to the accession, so the row moves to its owner
        assertEquals("row should be re-pointed at the accession's owner", geneOwner, geneOf(remapPk));

        // duplicate: the target already holds this location, so the leftover is dropped and
        // the correct row is left alone
        assertNull("duplicate of the target's row should be deleted", geneOf(duplicatePk));
        assertEquals("the target's own row must survive", geneOwner, geneOf(duplicateTargetPk));

        // ambiguous: nothing is guessed at
        assertEquals("ambiguous row must be left exactly as it was", geneThird, geneOf(ambiguousPk));

        // and it reports what it did
        Map<String, LoadReportAction> bySubType = actions.stream()
                .collect(Collectors.toMap(LoadReportAction::getSubType, a -> a, (a, b) -> a));
        assertNotNull("expected a re-pointed action", bySubType.get("Re-pointed NCBI Genome Location"));
        assertNotNull("expected a deleted action", bySubType.get("Deleted NCBI Genome Location"));
        assertNotNull("ambiguous rows must be reported for a curator",
                bySubType.get("Unreconciled NCBI Genome Location"));

        assertEquals("one row re-pointed", 1, rowCount(bySubType.get("Re-pointed NCBI Genome Location")));
        assertEquals("orphan and duplicate deleted", 2, rowCount(bySubType.get("Deleted NCBI Genome Location")));
        assertEquals("the ambiguous row reported", 1, rowCount(bySubType.get("Unreconciled NCBI Genome Location")));

        // Everything reconcilable is gone. The ambiguous row is deliberately still drift:
        // reconcile() refuses to guess, so it stays until a curator resolves the accession.
        assertEquals("only the ambiguous row should still be drift", 1, driftCount());
        assertEquals("and it should be that row", geneThird, geneOf(ambiguousPk));
        HibernateUtil.flushAndCommitCurrentSession();
    }

    @After
    public void removePlantedRows() {
        // A failed assertion can leave a transaction open, and createTransaction() would then
        // throw and skip the cleanup entirely - which strands planted rows and makes the next
        // run fail its precondition instead of the assertion that actually broke.
        HibernateUtil.rollbackTransaction();
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().createNativeMutationQuery(
                        "delete from sequence_feature_chromosome_location_generated"
                        + " where sfclg_acc_num like :prefix")
                .setParameter("prefix", TEST_ACC_PREFIX + "%").executeUpdate();
        HibernateUtil.currentSession().createNativeMutationQuery(
                        "delete from zdb_active_data where zactvd_zdb_id in"
                        + " (select dblink_zdb_id from db_link where dblink_acc_num like :prefix)")
                .setParameter("prefix", TEST_ACC_PREFIX + "%").executeUpdate();
        HibernateUtil.currentSession().createNativeMutationQuery(
                        "delete from db_link where dblink_acc_num like :prefix")
                .setParameter("prefix", TEST_ACC_PREFIX + "%").executeUpdate();
        HibernateUtil.flushAndCommitCurrentSession();
    }

    // ---------- helpers ----------

    /**
     * reconcile() works on the whole table, so any drift the database already carries would be
     * deleted by this test. Refuse to run rather than destroy rows we did not plant.
     */
    private void requireNoPreExistingDrift() {
        long existing = driftCount();
        assertEquals("database must start with no NCBILoader drift - run the NCBI gene load first,"
                     + " otherwise this test would reconcile rows it did not plant", 0, existing);
    }

    private long driftCount() {
        return ((Number) HibernateUtil.currentSession().createNativeQuery(
                        "select count(*) from sequence_feature_chromosome_location_generated l"
                        + " where l.sfclg_location_source = :source"
                        + "   and not exists (select 1 from db_link d"
                        + "                    where d.dblink_acc_num = l.sfclg_acc_num"
                        + "                      and d.dblink_fdbcont_zdb_id = :fdbcont"
                        + "                      and d.dblink_linked_recid = l.sfclg_data_zdb_id)")
                .setParameter("source", SOURCE)
                .setParameter("fdbcont", NCBI_GENE_FDBCONT)
                .getSingleResult()).longValue();
    }

    private long insertLocation(String gene, String accession, String chromosome, int start, int end) {
        return ((Number) HibernateUtil.currentSession().createNativeQuery(
                        "insert into sequence_feature_chromosome_location_generated"
                        + " (sfclg_data_zdb_id, sfclg_acc_num, sfclg_chromosome, sfclg_start,"
                        + "  sfclg_end, sfclg_location_source, sfclg_assembly)"
                        + " values (:gene, :acc, :chr, :start, :end, :source, 'GRCz12tu')"
                        + " returning sfclg_pk_id")
                .setParameter("gene", gene).setParameter("acc", accession)
                .setParameter("chr", chromosome).setParameter("start", start)
                .setParameter("end", end).setParameter("source", SOURCE)
                .getSingleResult()).longValue();
    }

    private void linkAccession(String accession, String gene) {
        HibernateUtil.currentSession().createNativeMutationQuery(
                        "insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_acc_num,"
                        + " dblink_fdbcont_zdb_id)"
                        + " values (get_id_and_insert_active_data('DBLINK'), :gene, :acc, :fdbcont)")
                .setParameter("gene", gene).setParameter("acc", accession)
                .setParameter("fdbcont", NCBI_GENE_FDBCONT)
                .executeUpdate();
    }

    private String geneOf(long pkId) {
        List<?> rows = HibernateUtil.currentSession().createNativeQuery(
                        "select sfclg_data_zdb_id from sequence_feature_chromosome_location_generated"
                        + " where sfclg_pk_id = :pk")
                .setParameter("pk", pkId).list();
        return rows.isEmpty() ? null : (String) rows.get(0);
    }

    private int rowCount(LoadReportAction action) {
        return action.getTables().get(0).getRows().size();
    }

    @SuppressWarnings("unchecked")
    private List<String> nativeList(String sql) {
        return HibernateUtil.currentSession().createNativeQuery(sql).list();
    }
}
