package org.zfin.sequence.gff;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.zfin.framework.dao.BaseSQLDAO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Gff3NcbiDAO extends BaseSQLDAO<Gff3Ncbi> {

//    protected Session entityManager;

    public Gff3NcbiDAO(Session entityManager) {
        super(Gff3Ncbi.class);
        this.entityManager = entityManager;
    }

    public Gff3NcbiDAO() {
        super(Gff3Ncbi.class);
    }

    /**
     * Clear the GFF3 staging tables. The load only ever inserts, so without this each run left
     * another full copy of the file behind (~3.2M gff3_ncbi and ~25M gff3_ncbi_attribute rows per
     * run) and every consumer -- the feature histogram, the genome-location upsert, Gff3Writer --
     * silently read duplicated records.
     * <p>
     * Truncate rather than delete: at these row counts a DML delete and its foreign-key checks are
     * far slower, and both tables are pure staging for the file that is about to be re-read. Both
     * are named in one statement because gff3_ncbi_attribute has an FK to gff3_ncbi.
     */
    public void truncateStagingTables() {
        entityManager.createNativeQuery("truncate table gff3_ncbi, gff3_ncbi_attribute")
            .executeUpdate();
    }

    public List<Gff3Ncbi> findRecordsBySource(String chromosome, List<String> sourceName) {
        TypedQuery<Gff3Ncbi> query = entityManager.createQuery("""
            from Gff3Ncbi gff3
                        where gff3.source in :sourceName
                        AND gff3.chromosome = :chromosome
                        order by chromosome, start, end, gff3.id
            """, Gff3Ncbi.class);
        query.setParameter("sourceName", sourceName);
        query.setParameter("chromosome", chromosome);
        return query.getResultList();
    }

    public List<Gff3Ncbi> findRecordsByFeature(String featureName) {
        TypedQuery<Gff3Ncbi> query = entityManager.createQuery("""
            from Gff3Ncbi gff3
            join fetch gff3.attributePairs
                        where gff3.feature = :feature
            """, Gff3Ncbi.class);
        query.setParameter("feature", featureName);
        return query.getResultList();
    }

    public Map<String, Integer> getFeatureTypeHistogram() {
        TypedQuery<Tuple> query = entityManager.createQuery("""
            select gff3.feature, count(gff3)  from Gff3Ncbi gff3
            group by gff3.feature
            """, Tuple.class);
        List<Tuple> tuples = query.getResultList();
        return tuples.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(0, String.class),
                tuple -> tuple.get(1, Long.class).intValue()));
    }
}
