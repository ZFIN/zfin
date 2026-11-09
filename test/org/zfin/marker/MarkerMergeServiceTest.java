package org.zfin.marker;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Merge conflict handling for UNIQUE constraints on child tables (ZFIN-10457).
 *
 * <p>Two merges, because they fail on different constraint widths: a 13-column one and a
 * single-column one. The service runs on the Hibernate session's connection, so
 * {@link AbstractDatabaseTest}'s rollback undoes everything here.</p>
 */
public class MarkerMergeServiceTest extends AbstractDatabaseTest {

    private static final String CRISPR2_CIAO3 = "ZDB-CRISPR-170609-8";  // merged away
    private static final String CRISPR1_CIAO3 = "ZDB-CRISPR-200103-1";  // survives
    private static final String MGT           = "ZDB-GENE-070117-2188"; // merged away
    private static final String PLOD3         = "ZDB-GENE-021031-4";    // survives

    private long count(String sql, Object... args) {
        var query = HibernateUtil.currentSession().createNativeQuery(sql, Long.class);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return query.getSingleResult();
    }

    /**
     * Both CRISPRs have one identical location row, NULL in several constraint columns. That
     * collides only under {@code uq_sfclg_unique_location} (NULLS NOT DISTINCT) and not under
     * {@code uk_sfclg_unique_location}, so a conflict check using plain {@code =} fails this test.
     */
    @Test
    public void mergesAcrossTheWideNullsNotDistinctLocationConstraint() {
        long before = count("select count(*) from sequence_feature_chromosome_location_generated "
                + "where sfclg_data_zdb_id in (?1, ?2)", CRISPR2_CIAO3, CRISPR1_CIAO3);
        assertEquals("fixture changed: expected one location row on each CRISPR", 2, before);

        List<String> sql = new MarkerMergeService(CRISPR2_CIAO3, CRISPR1_CIAO3, true).merge();

        assertTrue("expected a conflict DELETE against the location table, got:\n" + sql,
                sql.stream().anyMatch(s -> s.startsWith("delete from")
                        && s.contains("sequence_feature_chromosome_location_generated")));

        assertEquals("the merged-away CRISPR should have no location rows left", 0,
                count("select count(*) from sequence_feature_chromosome_location_generated "
                        + "where sfclg_data_zdb_id = ?1", CRISPR2_CIAO3));
        assertEquals("the survivor keeps exactly one location row, not two", 1,
                count("select count(*) from sequence_feature_chromosome_location_generated "
                        + "where sfclg_data_zdb_id = ?1", CRISPR1_CIAO3));
    }

    /**
     * {@code UNIQUE (gd_gene_zdb_id)} allows one description per gene, so after the merge only one
     * can exist. The survivor keeps its own rather than the merge aborting.
     */
    @Test
    public void mergesAcrossTheSingleColumnGeneDescriptionConstraint() {
        assertEquals("fixture changed: expected a description on each gene", 2,
                count("select count(*) from gene_description where gd_gene_zdb_id in (?1, ?2)",
                        MGT, PLOD3));
        String survivorDescriptionBefore = (String) HibernateUtil.currentSession()
                .createNativeQuery("select gd_description from gene_description where gd_gene_zdb_id = ?1", String.class)
                .setParameter(1, PLOD3)
                .getSingleResult();

        List<String> sql = new MarkerMergeService(MGT, PLOD3, true).merge();

        assertTrue("expected a conflict DELETE against gene_description, got:\n" + sql,
                sql.stream().anyMatch(s -> s.startsWith("delete from") && s.contains("gene_description")));

        assertEquals("the merged-away gene should have no description left", 0,
                count("select count(*) from gene_description where gd_gene_zdb_id = ?1", MGT));
        assertEquals("the survivor keeps exactly one description", 1,
                count("select count(*) from gene_description where gd_gene_zdb_id = ?1", PLOD3));
        assertEquals("the survivor keeps ITS OWN description, not the merged-away gene's",
                survivorDescriptionBefore,
                HibernateUtil.currentSession()
                        .createNativeQuery("select gd_description from gene_description where gd_gene_zdb_id = ?1", String.class)
                        .setParameter(1, PLOD3)
                        .getSingleResult());
    }

    /**
     * Non-conflicting rows must be remapped, not dropped -- a guard against the conflict handling
     * widening into deletes. The merged-away gene is given one marker_assembly row that the survivor does
     * not have, so nothing collides.
     */
    @Test
    public void preservesRowsThatDoNotConflict() {
        long grcz10 = count("select a_pk_id from assembly where a_name = 'GRCz10'");
        HibernateUtil.currentSession()
                .createNativeQuery("delete from marker_assembly where ma_a_pk_id = ?1 and ma_mrkr_zdb_id in (?2, ?3)")
                .setParameter(1, grcz10).setParameter(2, MGT).setParameter(3, PLOD3).executeUpdate();
        HibernateUtil.currentSession()
                .createNativeQuery("insert into marker_assembly (ma_mrkr_zdb_id, ma_a_pk_id) values (?1, ?2)")
                .setParameter(1, MGT).setParameter(2, grcz10).executeUpdate();

        new MarkerMergeService(MGT, PLOD3, true).merge();

        assertEquals("a non-conflicting row must be remapped, never deleted", 1,
                count("select count(*) from marker_assembly where ma_mrkr_zdb_id = ?1 and ma_a_pk_id = ?2", PLOD3, grcz10));
        assertEquals(0,
                count("select count(*) from marker_assembly where ma_mrkr_zdb_id = ?1 and ma_a_pk_id = ?2", MGT, grcz10));
    }
}
