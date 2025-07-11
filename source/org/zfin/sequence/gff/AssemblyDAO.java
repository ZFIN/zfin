package org.zfin.sequence.gff;

import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.SearchResponse;
import org.zfin.framework.dao.BaseSQLDAO;

import java.util.List;

public class AssemblyDAO extends BaseSQLDAO<Assembly> {

//    protected Session entityManager;

    public AssemblyDAO(Session entityManager) {
        super(Assembly.class);
        this.entityManager = entityManager;
    }

    public AssemblyDAO() {
        super(Assembly.class);
    }

    public SearchResponse<Assembly> findZ12AssembliesRefSeq(Pagination pagination) {
        String hql = """
            from Assembly
            join fetch markerDBLinks
            where
            markerDBLinks is not empty and
            id = 1
            """;
        TypedQuery<Assembly> query = entityManager.createQuery(hql, Assembly.class);
        query.setMaxResults(pagination.getLimit());
        SearchResponse<Assembly> response = new SearchResponse<>();
        response.setResults(query.getResultList());
        response.setTotalResults((long) response.getResults().size());
        return response;
    }

    public List<Assembly> findAllSortedAssemblies() {
        String hql = """
            from Assembly
            where
            order by order
            """;
        TypedQuery<Assembly> query = entityManager.createQuery(hql, Assembly.class);
        return query.getResultList();
    }
}
