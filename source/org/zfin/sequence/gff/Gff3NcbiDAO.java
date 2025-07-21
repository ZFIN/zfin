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

    public List<Gff3Ncbi> findRecordsBySource(String sourceName) {
        TypedQuery<Gff3Ncbi> query = entityManager.createQuery("""
            from Gff3Ncbi gff3
            join fetch gff3.attributePairs
                        where gff3.source = :sourceName
            """, Gff3Ncbi.class);
        query.setParameter("sourceName", sourceName);
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
        List<Tuple> tuples =  query.getResultList();
        return tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, String.class),
                        tuple -> tuple.get(1, Long.class).intValue()));
    }
}
