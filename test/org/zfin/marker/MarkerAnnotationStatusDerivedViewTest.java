package org.zfin.marker;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;

import static org.junit.Assert.assertEquals;

/**
 * Checks the marker_annotation_status_derived view against the data in the test database.
 * Runs inside the transaction {@link AbstractDatabaseTest} rolls back, so planted rows do not survive.
 *
 * <p>The planted-data tests use ZDB-GENE-000112-47 (NCBI Gene ID 30754), a gene the NCBI load attributes to the
 * RNA-matching publication and that has an NCBILoader location on a current assembly.
 */
public class MarkerAnnotationStatusDerivedViewTest extends AbstractDatabaseTest {

    private static final String GENE = "ZDB-GENE-000112-47";
    private static final String GENE_NCBI_ID = "30754";
    private static final String RNA_PUB = "ZDB-PUB-020723-3";

    private long count(String sql) {
        return ((Number) HibernateUtil.currentSession().createNativeQuery(sql, Object.class).getSingleResult()).longValue();
    }

    /** "status|basis" for the gene, or "Unknown" when the view has no row for it. */
    private String statusOf(String markerZdbId) {
        var rows = HibernateUtil.currentSession().createNativeQuery(
                "select vt.vt_name || '|' || d.mas_basis from marker_annotation_status_derived d " +
                "join vocabulary_term vt on vt.vt_id = d.mas_vt_pk_id where d.mas_mrkr_zdb_id = :id", String.class)
                .setParameter("id", markerZdbId).getResultList();
        return rows.isEmpty() ? "Unknown" : rows.get(0);
    }

    @Test
    public void everyMarkerHasAtMostOneRow() {
        assertEquals(0, count("select count(*) from (select mas_mrkr_zdb_id from marker_annotation_status_derived " +
                "group by 1 having count(*) > 1) q"));
    }

    @Test
    public void everyRowBelongsToAMarkerWithAnNcbiGeneLink() {
        assertEquals(0, count("select count(*) from marker_annotation_status_derived d where not exists (" +
                "select 1 from db_link where dblink_linked_recid = d.mas_mrkr_zdb_id " +
                "and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1')"));
    }

    @Test
    public void aGeneOnTheNotInReleaseListIsNeverCurrent() {
        assertEquals(0, count("select count(*) from external_resource.ncbi_not_in_current_release n " +
                "join db_link l on l.dblink_acc_num = n.ncbi_gene_id and l.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1' " +
                "join marker_annotation_status_derived d on d.mas_mrkr_zdb_id = l.dblink_linked_recid " +
                "join vocabulary_term vt on vt.vt_id = d.mas_vt_pk_id where vt.vt_name <> 'Not in current annotation release'"));
    }

    @Test
    public void currentViaLoadPublication() {
        assertEquals("Current|load publication", statusOf(GENE));
    }

    @Test
    public void theNotInReleaseListWinsOverEverythingElse() {
        HibernateUtil.currentSession().createNativeQuery(
                "insert into external_resource.ncbi_not_in_current_release (ncbi_gene_id) values (:id) on conflict do nothing")
                .setParameter("id", GENE_NCBI_ID).executeUpdate();
        assertEquals("Not in current annotation release|NCBI not-in-release list", statusOf(GENE));
    }

    @Test
    public void withoutTheLoadPublicationTheCurrentAssemblyLocationStillMakesItCurrent() {
        HibernateUtil.currentSession().createNativeQuery(
                "update load_publication set lp_confers_current_gene_status_via_ncbi = false where lp_pub_zdb_id = :pub")
                .setParameter("pub", RNA_PUB).executeUpdate();
        assertEquals("Current|current-assembly location", statusOf(GENE));
    }

    @Test
    public void withNeitherTheGeneIsUnknown() {
        HibernateUtil.currentSession().createNativeQuery(
                "update load_publication set lp_confers_current_gene_status_via_ncbi = false where lp_pub_zdb_id = :pub")
                .setParameter("pub", RNA_PUB).executeUpdate();
        HibernateUtil.currentSession().createNativeQuery("update assembly set a_is_current = false").executeUpdate();
        assertEquals("Unknown", statusOf(GENE));
    }

    @Test
    public void repositoryReportsTheStatusFromTheView() {
        assertEquals("Current", RepositoryFactory.getMarkerRepository().getAnnotationStatus(GENE));
    }

    @Test
    public void repositoryReportsUnknownForAMarkerWithNoRow() {
        String unrelated = HibernateUtil.currentSession().createNativeQuery(
                "select mrkr_zdb_id from marker where mrkr_zdb_id not in " +
                "(select mas_mrkr_zdb_id from marker_annotation_status_derived) limit 1", String.class).getSingleResult();
        assertEquals("Unknown", RepositoryFactory.getMarkerRepository().getAnnotationStatus(unrelated));
    }

    @Test
    public void markerAnswersFromTheViewAndRemembersIt() {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(GENE);
        assertEquals("Current", gene.getAnnotationStatus());
        HibernateUtil.currentSession().createNativeQuery(
                "insert into external_resource.ncbi_not_in_current_release (ncbi_gene_id) values (:id) on conflict do nothing")
                .setParameter("id", GENE_NCBI_ID).executeUpdate();
        assertEquals("Current", gene.getAnnotationStatus());
        Marker fresh = new Marker();
        fresh.setZdbID(GENE);
        assertEquals("Not in current annotation release", fresh.getAnnotationStatus());
    }
}
