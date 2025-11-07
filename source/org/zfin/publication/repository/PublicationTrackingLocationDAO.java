package org.zfin.publication.repository;

import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.zfin.framework.dao.BaseSQLDAO;
import org.zfin.publication.PublicationTrackingLocation;

public class PublicationTrackingLocationDAO extends BaseSQLDAO<PublicationTrackingLocation> {

    public PublicationTrackingLocationDAO(Session entityManager) {
        super(PublicationTrackingLocation.class);
        this.entityManager = entityManager;
    }

    public PublicationTrackingLocationDAO() {
        super(PublicationTrackingLocation.class);
    }

    public PublicationTrackingLocation findByStatus(PublicationTrackingLocation.Name name) {
        String hql = """
            from PublicationTrackingLocation
            where name = :name
            """;
        TypedQuery<PublicationTrackingLocation> query = entityManager.createQuery(hql, PublicationTrackingLocation.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }
}
