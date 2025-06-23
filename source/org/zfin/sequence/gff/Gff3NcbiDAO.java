package org.zfin.sequence.gff;

import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.zfin.framework.dao.BaseSQLDAO;

import java.util.List;

public class Gff3NcbiDAO extends BaseSQLDAO<Gff3Ncbi> {

//    protected Session entityManager;

    public Gff3NcbiDAO(Session entityManager) {
        super(Gff3Ncbi.class);
        this.entityManager = entityManager;
    }

    public Gff3NcbiDAO() {
        super(Gff3Ncbi.class);
    }

    public List<Gff3Ncbi> findRecordsBySource(String bestRefSeq) {
        TypedQuery<Gff3Ncbi> query = entityManager.createQuery("""
            from Gff3Ncbi gff3
            join fetch gff3.attributePairs
                        where gff3.source = :bestRefSeq
            """, Gff3Ncbi.class);
        query.setParameter("bestRefSeq", bestRefSeq);
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
}
