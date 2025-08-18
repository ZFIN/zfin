package org.zfin.sequence.gff;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.dao.BaseSQLDAO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Gff3NcbiAttributesDAO extends BaseSQLDAO<Gff3NcbiAttributePair> {

    public EntityManager entityManager = HibernateUtil.currentSession();

    public Gff3NcbiAttributesDAO(Session entityManager) {
        super(Gff3NcbiAttributePair.class);
        this.entityManager = entityManager;
    }

    public Gff3NcbiAttributesDAO() {
        super(Gff3NcbiAttributePair.class);
    }

    /**
     * Returns a map of gene_id to attribute value pairs.
     * <id of NCBI
     */
    public Map<Long, String> getGeneIDAttributePairsMap() {
        TypedQuery<Tuple> query = entityManager.createQuery("""
            select gff3Ncbi.id, value from Gff3NcbiAttributePair
                        where key = 'gene_id'
            """, Tuple.class);
        List<Tuple> list = query.getResultList();
        return list.stream().collect(Collectors.toMap(
            tuple -> (Long) tuple.get(0), tuple -> (String) tuple.get(1)
        ));
    }
}
